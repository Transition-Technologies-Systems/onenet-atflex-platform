package pl.com.tt.flex.server.service.algorithm.danoAlgorithm;

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
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.UnitSelfScheduleService;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.algorithm.danoAlgorithm.DanoAlgorithmValidator;
import pl.com.tt.flex.server.web.rest.algorithm.resource.FlexAgnoAlgorithmResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@Transactional
public class EnergyDanoAlgorithmServiceImpl extends AlgorithmAbstract implements EnergyDanoAlgorithmService {
    private final AuctionDayAheadService auctionDayAheadService;
    private final DanoFileGenerator danoFileGenerator;
    private final DanoAlgorithmValidator danoAlgorithmValidator;
    private final ProductService productService;
    private final UnitService unitService;
    private final UnitSelfScheduleService unitSelfScheduleService;
    private final AlgorithmEvaluationService algorithmEvaluationService;
    private final FlexAgnoAlgorithmResource flexAgnoAlgorithmResource;

    public EnergyDanoAlgorithmServiceImpl(AuctionDayAheadService auctionDayAheadService, ProductService productService, UnitService unitService,
                                          UnitSelfScheduleService unitSelfScheduleService, DanoFileGenerator danoFileGenerator,
                                          DanoAlgorithmValidator danoAlgorithmValidator, AlgorithmEvaluationService algorithmEvaluationService,
                                          FlexAgnoAlgorithmResource flexAgnoAlgorithmResource) {
        super(unitService);
        this.auctionDayAheadService = auctionDayAheadService;
        this.danoFileGenerator = danoFileGenerator;
        this.danoAlgorithmValidator = danoAlgorithmValidator;
        this.productService = productService;
        this.unitService = unitService;
        this.unitSelfScheduleService = unitSelfScheduleService;
        this.algorithmEvaluationService = algorithmEvaluationService;
        this.flexAgnoAlgorithmResource = flexAgnoAlgorithmResource;
    }

    /**
     * Tworzenie pliku wsadowego dla agortymu DANO
     */
    @Override
    public FileDTO getAlgorithmInputFiles(AlgorithmEvaluationConfigDTO evaluationConfigDTO, Set<AuctionDayAheadOfferDTO> offers) throws IOException, ObjectValidationException {
        AgnoInputDataDTO agnoInputDataDTO = prepareDataToCreateDanoFile(offers, evaluationConfigDTO);
        List<FileDTO> danoFiles = new ArrayList<>();
        LocalDate localDeliveryDate = LocalDate.ofInstant(agnoInputDataDTO.getDeliveryDate(), ZoneId.systemDefault());
        for (AgnoCouplingPointDTO couplingPoint : agnoInputDataDTO.getCouplingPoints()) {
            for (AgnoHourNumberDTO agnoHourNumberDTO : couplingPoint.getAgnoHourNumbers()) {
                FileDTO danoFile = danoFileGenerator.getDanoFile(couplingPoint, agnoHourNumberDTO, localDeliveryDate);
                danoFiles.add(danoFile);
                log.debug("getAlgorithmInputFiles() Add file {} with: couplingPoint: {}, deliveryDate: {}, hourNumber: {}",
                    danoFile.getFileName(), couplingPoint.getCouplingPointId().getName(), evaluationConfigDTO.getDeliveryDate(), agnoHourNumberDTO.getHourNumber());
            }
        }
        String extension = ".zip";
        String filename = "input_dgia_" + evaluationConfigDTO.getDeliveryDate() + extension;
        return new FileDTO(filename, ZipUtil.filesToZip(danoFiles));
    }

    /**
     * Przygotowanie danych do generowania pliku wsadowego DANO.
     * Metoda ta grupuje wszystkie oferty według CouplingPointId złożonych w ofertach DERow.
     * Każda oferta rospatrywana jest per godzina gieldowa.
     * Brane są tylke te DERy ktore mają CouplingPoint {@link UnitDTO#getCouplingPointIdTypes()}
     * nalęzący do wybranego obaszaru kdm
     */
    private AgnoInputDataDTO prepareDataToCreateDanoFile(Set<AuctionDayAheadOfferDTO> offers, AlgorithmEvaluationConfigDTO configDTO) throws ObjectValidationException {
        danoAlgorithmValidator.checkValid(offers, configDTO);
        // pobieranie listy stacji transformatorowych/punktow przylaczenia do sieci nn
        KdmModelMinimalDTO kdmModelMinimalDTO = flexAgnoAlgorithmResource.getKdmFileMinimal(configDTO.getKdmModelId())
            .orElseThrow(() -> new IllegalStateException("Cannot find KdmModel for id: " + configDTO.getKdmModelId()));
        AgnoInputDataDTO agnoInputDataDTO = new AgnoInputDataDTO();
        Instant deliveryDate = configDTO.getDeliveryDate();
        agnoInputDataDTO.setDeliveryDate(deliveryDate);
        // Zbieranie DERów w celu dalszej walidacji
        List<String> validDers = new ArrayList<>();
        List<String> invalidDers = new ArrayList<>();
        boolean isLvModel = kdmModelMinimalDTO.isLvModel();
        offers.forEach(offerDTO -> offerDTO.getDers()
            .forEach(offerDer -> {
                log.debug("prepareDataToCreateDanoFile() START - analyze offer with id: {} and derID: {}", offerDTO.getId(), offerDer.getDer().getId());
                Map<String, List<AuctionOfferBandDataDTO>> offerDerBandDataGroupingByHourNumber = getBandDataGroupingByHourNumber(offerDer);
                UnitDTO der = unitService.findById(offerDer.getDer().getId())
                    .orElseThrow(() -> new IllegalStateException("Cannot find Der with id: " + offerDer.getDer().getId()));
                List<MinimalDTO<String, BigDecimal>> selfSchedule = unitSelfScheduleService.findVolumesForDerAndSelfScheduleDate(offerDer.getDer().getId(), deliveryDate);
                ProductDTO productDTO = productService.findById(offerDTO.getAuctionDayAhead().getProduct().getId())
                    .orElseThrow(() -> new IllegalStateException("Cannot find Product with id: " + offerDTO.getAuctionDayAhead().getProduct().getId()));
                offerDerBandDataGroupingByHourNumber.forEach((hourNumber, bandData) ->
                    der.getCouplingPointIdTypes().forEach(cpId -> {
                        KdmModelTimestampsMinimalDTO kdmTimestamp = kdmModelMinimalDTO.getTimestamps().stream().filter(t -> t.getTimestamp().equals(hourNumber)).findFirst().get();
                        if (isDerInvalid(der, kdmTimestamp.getStations(), invalidDers, isLvModel)) {
                            return; // gdy DER jest nieprawidłowy, przechodzimy do następnej iteracji
                        }
                        boolean isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV =
                            danoAlgorithmValidator.isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV(der, kdmTimestamp.getStations());
                        AgnoCouplingPointDTO agnoCouplingPointDTO = getAgnoCouplingPointDTO(agnoInputDataDTO, cpId);
                        AgnoHourNumberDTO agnoHourNumberDTO = getAgnoHourNumberDTO(agnoCouplingPointDTO, hourNumber);
                        setAgnoOfferDetailDTO(agnoHourNumberDTO, offerDTO, bandData, der, productDTO);
                        setUnitDetail(agnoHourNumberDTO, der, selfSchedule, hourNumber, kdmModelMinimalDTO.isLvModel(), isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV);
                        agnoCouplingPointDTO.addHourNumber(agnoHourNumberDTO);
                        agnoInputDataDTO.addAgnoCouplingPoint(agnoCouplingPointDTO);
                        validDers.add(der.getName());
                    }));
                log.debug("prepareDataToCreateDanoFile() END - analyze offer with id: {} and derID: {}", offerDTO.getId(), offerDer.getDer().getId());
            }));
        danoAlgorithmValidator.checkInvalidDers(validDers, invalidDers, isLvModel);
        return agnoInputDataDTO;
    }

    /**
     * Uruchomienie algorytmu DANO po stronie modułu flex-agno.
     * Jeżeli plik jest pusty zwraca bład z informacją o braku ofert do uruchomienia algorytmu
     */
    @Override
    @Transactional
    public void startAlgorithm(AlgorithmEvaluationConfigDTO evaluationConfigDTO) throws IOException, ObjectValidationException {
        log.info("startAlgorithm() Algorithm config: {}", evaluationConfigDTO);
        Set<AuctionDayAheadOfferDTO> offers = getOffers(evaluationConfigDTO);
        FileDTO inputFiles = getAlgorithmInputFiles(evaluationConfigDTO, offers);
        danoAlgorithmValidator.checkInputFile(inputFiles);
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

    @Override
    public boolean isSupport(AlgorithmType algorithmType) {
        return AlgorithmType.DANO.equals(algorithmType);
    }

    private Set<AuctionDayAheadOfferDTO> getOffers(AlgorithmEvaluationConfigDTO evaluationConfigDTO) {
        if (CollectionUtils.isEmpty(evaluationConfigDTO.getOffers())) {
            return auctionDayAheadService.findAllOfferByAuctionTypeAndDeliveryDate(AuctionDayAheadType.ENERGY, evaluationConfigDTO.getDeliveryDate());
        } else {
            return auctionDayAheadService.findAllOffersById(evaluationConfigDTO.getOffers());
        }
    }

    private boolean isDerInvalid(UnitDTO der, List<String> stations, List<String> invalidDers, boolean isLvModel) {
        if(danoAlgorithmValidator.isValidDERPowerStationsAndPointsOfConnectionWithLV(der, stations, isLvModel)){
            return false;
        }
        else if(danoAlgorithmValidator.isDerHasCorrectPowerStations(der, stations)){
            invalidDers.add(der.getName());
        }
        return true;
    }
}
