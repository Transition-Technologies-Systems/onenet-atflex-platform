package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.capacity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.algorithm.*;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelMinimalDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampsMinimalDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.algorithm.AlgorithmEvaluationService;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.AlgorithmAbstract;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoCouplingPointDTO;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoHourNumberDTO;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoInputDataDTO;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.ForecastedPricesService;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesDTO;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.UnitSelfScheduleService;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.algorithm.agnoAlgorithm.AgnoAlgorithmValidator;
import pl.com.tt.flex.server.web.rest.algorithm.resource.FlexAgnoAlgorithmResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@Slf4j
@Transactional
public class CapacityAgnoAlgorithmServiceImpl extends AlgorithmAbstract implements CapacityAgnoAlgorithmService {


    private final AgnoPbcmFileGenerator agnoPbcmFileGenerator;
    private final AuctionDayAheadService auctionDayAheadService;
    private final AlgorithmEvaluationService algorithmEvaluationService;
    private final FlexAgnoAlgorithmResource flexAgnoAlgorithmResource;
    private final AgnoAlgorithmValidator agnoAlgorithmValidator;
    private final UnitService unitService;
    private final ProductService productService;
    private final ForecastedPricesService forecastedPricesService;
    private final UnitSelfScheduleService unitSelfScheduleService;

    public CapacityAgnoAlgorithmServiceImpl(AuctionDayAheadService auctionDayAheadService, ProductService productService,
                                            UnitService unitService, UnitSelfScheduleService unitSelfScheduleService,
                                            ForecastedPricesService forecastedPricesService, AgnoPbcmFileGenerator agnoPbcmFileGenerator,
                                            AgnoAlgorithmValidator agnoAlgorithmValidator, AlgorithmEvaluationService algorithmEvaluationService, FlexAgnoAlgorithmResource flexAgnoAlgorithmResource) {
        super(unitService);
        this.agnoPbcmFileGenerator = agnoPbcmFileGenerator;
        this.auctionDayAheadService = auctionDayAheadService;
        this.algorithmEvaluationService = algorithmEvaluationService;
        this.flexAgnoAlgorithmResource = flexAgnoAlgorithmResource;
        this.agnoAlgorithmValidator = agnoAlgorithmValidator;
        this.unitService = unitService;
        this.productService = productService;
        this.forecastedPricesService = forecastedPricesService;
        this.unitSelfScheduleService = unitSelfScheduleService;
    }

    /**
     * Tworzenie pliku wsadowego dla agortymu PBCM
     */
    @Override
    public FileDTO getAlgorithmInputFiles(AlgorithmEvaluationConfigDTO evaluationConfigDTO, Set<AuctionDayAheadOfferDTO> offers) throws IOException, ObjectValidationException {
        AgnoInputDataDTO agnoInputDataDTO = prepareDataToCreateAgnoFile(offers, evaluationConfigDTO);
        List<FileDTO> agnoPbcmFiles = new ArrayList<>();
        LocalDate deliveryLocalDate = LocalDate.ofInstant(agnoInputDataDTO.getDeliveryDate(), ZoneId.systemDefault());
        for (AgnoCouplingPointDTO couplingPoint : agnoInputDataDTO.getCouplingPoints()) {
            for (AgnoHourNumberDTO agnoHourNumberDTO : couplingPoint.getAgnoHourNumbers()) {
                FileDTO pbcmFile = agnoPbcmFileGenerator.getPbcmFile(couplingPoint, agnoHourNumberDTO, deliveryLocalDate);
                agnoPbcmFiles.add(pbcmFile);
                log.debug("getPbcmInputFiles() Add file {} with: couplingPoint: {}, deliveryDate: {}, hourNumber: {}",
                    pbcmFile.getFileName(), couplingPoint.getCouplingPointId().getName(), evaluationConfigDTO.getDeliveryDate(), agnoHourNumberDTO.getHourNumber());
            }
        }
        String extension = ".zip";
        String filename = "input_pbcm_" + evaluationConfigDTO.getDeliveryDate() + extension;
        return new FileDTO(filename, ZipUtil.filesToZip(agnoPbcmFiles));
    }

    /**
     * Uruchomienie algorytmu PBCM po stronie modułu flex-agno.
     * Jeżeli plik jest pusty zwraca bład z informacją o braku ofert do uruchomienia algorytmu
     */
    @Override
    @Transactional
    public void startAlgorithm(AlgorithmEvaluationConfigDTO evaluationConfigDTO) throws IOException, ObjectValidationException {
        log.info("startAlgorithm() Algorithm config: {}", evaluationConfigDTO);
        Set<AuctionDayAheadOfferDTO> offers = getOffers(evaluationConfigDTO);
        FileDTO inputFiles = getAlgorithmInputFiles(evaluationConfigDTO, offers);
        agnoAlgorithmValidator.checkInputFile(inputFiles);
        AlgorithmEvaluationDTO evaluationDTO = algorithmEvaluationService
            .saveDayAheadAlgorithmEvaluation(evaluationConfigDTO.getDeliveryDate(), offers, inputFiles, evaluationConfigDTO.getAlgorithmType(), evaluationConfigDTO.getKdmModelId());
        AlgEvaluationModuleDTO algEvaluationModuleDTO = getAlgEvaluationModuleDTO(evaluationConfigDTO, inputFiles, evaluationDTO);
        try {
            flexAgnoAlgorithmResource.runAgnoAlgorithm(algEvaluationModuleDTO);
            log.info("startAlgorithm() Running algorithm with config: {}", evaluationConfigDTO);
        } catch (Exception e) {
            algEvaluationModuleDTO.setStatus(AlgorithmStatus.TECHNICAL_FAILURE);
            algEvaluationModuleDTO.setErrorMessage(e.getMessage());
            log.info("startAlgorithm() Problem with running algorithm: {}. Alg config: {}", e.getMessage(), evaluationConfigDTO);
            algorithmEvaluationService.saveAlgorithmResult(algEvaluationModuleDTO);
            e.printStackTrace();
        }
    }

    /**
     * Przygotowanie danych do generowania pliku wsadowego PBCM
     * Metoda ta grupuje wszystkie oferty według CouplingPointId złożonych w ofertach DERow, pomijane są te DERy
     * ktore nie należą do danego obszaru kdm.
     * Każda oferta rospatrywana jest per godzina gieldowa.
     */
    private AgnoInputDataDTO prepareDataToCreateAgnoFile(Set<AuctionDayAheadOfferDTO> offers, AlgorithmEvaluationConfigDTO evaluationConfigDTO) throws ObjectValidationException {
        Instant deliveryDate = evaluationConfigDTO.getDeliveryDate();
        agnoAlgorithmValidator.checkValid(offers, evaluationConfigDTO);
        KdmModelMinimalDTO kdmModelMinimalDTO = flexAgnoAlgorithmResource.getKdmFileMinimal(evaluationConfigDTO.getKdmModelId())
            .orElseThrow(() -> new IllegalStateException("Cannot find KdmModel for id: " + evaluationConfigDTO.getKdmModelId()));
        // Zbieranie DERów w celu dalszej walidacji
        List<String> validDers = new ArrayList<>();
        List<String> invalidDers = new ArrayList<>();
        AgnoInputDataDTO agnoInputDataDTO = new AgnoInputDataDTO();
        agnoInputDataDTO.setDeliveryDate(deliveryDate);
        boolean isLvModel = kdmModelMinimalDTO.isLvModel();
        offers.forEach(offer -> offer.getDers()
            .forEach(offerDer -> {
                log.debug("prepareDataToCreateAgnoFile() START - analyze offer with id: {} and derID: {}", offer.getId(), offerDer.getDer().getId());
                //grupowanie DERa zlozonego do oferty per godzina gieldowa
                Map<String, List<AuctionOfferBandDataDTO>> offerDerBandDataGroupingByHourNumber = getBandDataGroupingByHourNumber(offerDer);
                UnitDTO der = unitService.findById(offerDer.getDer().getId())
                    .orElseThrow(() -> new IllegalStateException("Cannot find Der with id: " + offerDer.getDer().getId()));
                List<MinimalDTO<String, BigDecimal>> selfSchedule = unitSelfScheduleService.findVolumesForDerAndSelfScheduleDate(offerDer.getDer().getId(), deliveryDate);
                ProductDTO productDTO = productService.findById(offer.getAuctionDayAhead().getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException("Cannot find Product with id: " + offer.getAuctionDayAhead().getProduct().getId()));
                Optional<ForecastedPricesDTO> forecastedPrices = forecastedPricesService.findByProductAndForecastedPriceDate(productDTO.getId(), deliveryDate);
                offerDerBandDataGroupingByHourNumber.forEach((hourNumber, bandData) ->
                    der.getCouplingPointIdTypes().forEach(cpId -> {
                            KdmModelTimestampsMinimalDTO kdmTimestamp = kdmModelMinimalDTO.getTimestamps().stream().filter(t -> t.getTimestamp().equals(hourNumber)).findFirst().get();
                            if (isDerInvalid(der, kdmTimestamp.getStations(), invalidDers, isLvModel)) {
                                return; // gdy DER jest nieprawidłowy, przechodzimy do następnej iteracji
                            }
                            boolean isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV =
                                agnoAlgorithmValidator.isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV(der, kdmTimestamp.getStations());
                            AgnoCouplingPointDTO agnoCouplingPointDTO = getAgnoCouplingPointDTO(agnoInputDataDTO, cpId);
                            AgnoHourNumberDTO agnoHourNumberDTO = getAgnoHourNumberDTO(agnoCouplingPointDTO, hourNumber);
                            setAgnoOfferDetailDTO(agnoHourNumberDTO, offer, bandData, der, productDTO);
                            setProductDetail(agnoHourNumberDTO, productDTO, forecastedPrices.get(), hourNumber);
                            setUnitDetail(agnoHourNumberDTO, der, selfSchedule, hourNumber, kdmModelMinimalDTO.isLvModel(), isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV);
                            agnoCouplingPointDTO.addHourNumber(agnoHourNumberDTO);
                            agnoInputDataDTO.addAgnoCouplingPoint(agnoCouplingPointDTO);
                            validDers.add(der.getName());
                        }
                    ));
                log.debug("prepareDataToCreateAgnoFile() END - analyze offer with id: {} and derID: {}", offer.getId(), offerDer.getDer().getId());
            }));
        agnoAlgorithmValidator.checkInvalidDers(validDers, invalidDers, isLvModel);
        return agnoInputDataDTO;
    }

    private Set<AuctionDayAheadOfferDTO> getOffers(AlgorithmEvaluationConfigDTO evaluationConfigDTO) {
        if (CollectionUtils.isEmpty(evaluationConfigDTO.getOffers())) {
            return auctionDayAheadService.findAllOfferByAuctionTypeAndDeliveryDate(AuctionDayAheadType.CAPACITY, evaluationConfigDTO.getDeliveryDate());
        } else {
            return auctionDayAheadService.findAllOffersById(evaluationConfigDTO.getOffers());
        }
    }

    private boolean isDerInvalid(UnitDTO der, List<String> stations, List<String> invalidDers, boolean isLvModel) {
        if(agnoAlgorithmValidator.isValidDERPowerStationsAndPointsOfConnectionWithLV(der, stations, isLvModel)){
            return false;
        }
        else if(agnoAlgorithmValidator.isDerHasCorrectPowerStations(der, stations)){
            invalidDers.add(der.getName());
        }
        return true;
    }

    @Override
    public boolean isSupport(AlgorithmType algorithmType) {
        return AlgorithmType.PBCM.equals(algorithmType);
    }
}
