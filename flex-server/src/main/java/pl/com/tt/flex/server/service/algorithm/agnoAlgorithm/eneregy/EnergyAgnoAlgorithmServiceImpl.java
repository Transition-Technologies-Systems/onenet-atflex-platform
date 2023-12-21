package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy;

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
import pl.com.tt.flex.server.validator.algorithm.agnoAlgorithm.AgnoAlgorithmValidator;
import pl.com.tt.flex.server.web.rest.algorithm.resource.FlexAgnoAlgorithmResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@Transactional
public class EnergyAgnoAlgorithmServiceImpl extends AlgorithmAbstract implements EnergyAgnoAlgorithmService {

    private final AuctionDayAheadService auctionDayAheadService;
    private final AgnoBmFileGenerator agnoBmFileGenerator;
    private final AlgorithmEvaluationService algorithmEvaluationService;
    private final FlexAgnoAlgorithmResource flexAgnoAlgorithmResource;
    private final ProductService productService;
    private final UnitService unitService;
    private final UnitSelfScheduleService unitSelfScheduleService;
    private final AgnoAlgorithmValidator agnoAlgorithmValidator;

    public EnergyAgnoAlgorithmServiceImpl(AuctionDayAheadService auctionDayAheadService, ProductService productService, UnitService unitService,
                                          UnitSelfScheduleService unitSelfScheduleService, AgnoBmFileGenerator agnoBmFileGenerator,
                                          AgnoAlgorithmValidator agnoAlgorithmValidator, AlgorithmEvaluationService algorithmEvaluationService,
                                          FlexAgnoAlgorithmResource flexAgnoAlgorithmResource) {
        super(unitService);
        this.auctionDayAheadService = auctionDayAheadService;
        this.agnoBmFileGenerator = agnoBmFileGenerator;
        this.algorithmEvaluationService = algorithmEvaluationService;
        this.flexAgnoAlgorithmResource = flexAgnoAlgorithmResource;
        this.productService = productService;
        this.unitService = unitService;
        this.unitSelfScheduleService = unitSelfScheduleService;
        this.agnoAlgorithmValidator = agnoAlgorithmValidator;
    }

    /**
     * Tworzenie pliku wsadowego dla agortymu BM
     */
    @Override
    public FileDTO getAlgorithmInputFiles(AlgorithmEvaluationConfigDTO evaluationConfigDTO, Set<AuctionDayAheadOfferDTO> offers) throws IOException, ObjectValidationException {
        AgnoInputDataDTO agnoInputDataDTO = prepareDataToCreateAgnoFile(offers, evaluationConfigDTO);
        List<FileDTO> agnoBmFiles = new ArrayList<>();
        LocalDate deliveryLocalDate = LocalDate.ofInstant(agnoInputDataDTO.getDeliveryDate(), ZoneId.systemDefault());
        for (AgnoCouplingPointDTO couplingPoint : agnoInputDataDTO.getCouplingPoints()) {
            for (AgnoHourNumberDTO agnoHourNumberDTO : couplingPoint.getAgnoHourNumbers()) {
                FileDTO bmFile = agnoBmFileGenerator.getBmFile(couplingPoint, agnoHourNumberDTO, deliveryLocalDate);
                agnoBmFiles.add(bmFile);
                log.debug("getBmInputFiles() Add file {} with: couplingPoint: {}, deliveryDate: {}, hourNumber: {}",
                    bmFile.getFileName(), couplingPoint.getCouplingPointId().getName(), evaluationConfigDTO.getDeliveryDate(), agnoHourNumberDTO.getHourNumber());
            }
        }
        String extension = ".zip";
        String filename = "input_bm_" + evaluationConfigDTO.getDeliveryDate() + extension;
        return new FileDTO(filename, ZipUtil.filesToZip(agnoBmFiles));
    }

    /**
     * Uruchomienie algorytmu BM po stronie modułu flex-agno.
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
        }
    }

    /**
     * Przygotowanie danych do generowania pliku wsadowego BM.
     * Metoda ta grupuje wszystkie oferty według CouplingPointId złożonych w ofertach DERow
     * Każda oferta rospatrywana jest per godzina gieldowa.
     */
    private AgnoInputDataDTO prepareDataToCreateAgnoFile(Set<AuctionDayAheadOfferDTO> offers, AlgorithmEvaluationConfigDTO evaluationConfigDTO) throws ObjectValidationException {
        agnoAlgorithmValidator.checkValid(offers, evaluationConfigDTO);
        KdmModelMinimalDTO kdmModelMinimalDTO = flexAgnoAlgorithmResource.getKdmFileMinimal(evaluationConfigDTO.getKdmModelId())
            .orElseThrow(() -> new IllegalStateException("Cannot find KdmModel for id: " + evaluationConfigDTO.getKdmModelId()));
        AgnoInputDataDTO agnoInputDataDTO = new AgnoInputDataDTO();
        agnoInputDataDTO.setDeliveryDate(evaluationConfigDTO.getDeliveryDate());
        // Zbieranie DERów w celu dalszej walidacji
        List<String> invalidDers = new ArrayList<>();
        boolean isLvModel = kdmModelMinimalDTO.isLvModel();
        offers.forEach(offer -> offer.getDers().forEach(offerDer -> {
            log.debug("prepareDataToCreateAgnoFile() START - analyze offer with id: {} and derID: {}", offer.getId(), offerDer.getDer().getId());
            //grupowanie DERa zlozonego do oferty per godzina gieldowa
            Map<String, List<AuctionOfferBandDataDTO>> offerDerBandDataGroupingByHourNumber = getBandDataGroupingByHourNumber(offerDer);
            UnitDTO der = unitService.findById(offerDer.getDer().getId())
                .orElseThrow(() -> new IllegalStateException("Cannot find Der with id: " + offerDer.getDer().getId()));
            List<MinimalDTO<String, BigDecimal>> selfSchedule = unitSelfScheduleService.findVolumesForDerAndSelfScheduleDate(offerDer.getDer().getId(), evaluationConfigDTO.getDeliveryDate());
            ProductDTO productDTO = productService.findById(offer.getAuctionDayAhead().getProduct().getId())
                .orElseThrow(() -> new IllegalStateException("Cannot find Product with id: " + offer.getAuctionDayAhead().getProduct().getId()));
            offerDerBandDataGroupingByHourNumber.forEach((hourNumber, bandData) ->
                der.getCouplingPointIdTypes().forEach(cpId -> {
                    KdmModelTimestampsMinimalDTO kdmTimestamp = kdmModelMinimalDTO.getTimestamps().stream().filter(t -> t.getTimestamp().equals(hourNumber)).findFirst().get();
                    if (isDerInvalid(der, kdmTimestamp.getStations(), invalidDers, isLvModel)) {
                        log.error("prepareDataToCreateAgnoFile() DER {} cannot be found in chosen KDM model", der.getName());
                        return; // gdy DER jest nieprawidłowy, przechodzimy do następnej iteracji
                    }
                    boolean isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV =
                        agnoAlgorithmValidator.isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV(der, kdmTimestamp.getStations());
                    AgnoCouplingPointDTO agnoCouplingPointDTO = getAgnoCouplingPointDTO(agnoInputDataDTO, cpId);
                    AgnoHourNumberDTO agnoHourNumberDTO = getAgnoHourNumberDTO(agnoCouplingPointDTO, hourNumber);
                    setAgnoOfferDetailDTO(agnoHourNumberDTO, offer, bandData, der, productDTO);
                    setUnitDetail(agnoHourNumberDTO, der, selfSchedule, hourNumber, kdmModelMinimalDTO.isLvModel(), isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV);
                    agnoCouplingPointDTO.addHourNumber(agnoHourNumberDTO);
                    agnoInputDataDTO.addAgnoCouplingPoint(agnoCouplingPointDTO);
            }));
            log.debug("prepareDataToCreateAgnoFile() END - analyze offer with id: {} and derID: {}", offer.getId(), offerDer.getDer().getId());
        }));
        agnoAlgorithmValidator.checkIfAllDersHavePOCsWithLvOrPowerStationIds(invalidDers);
        return agnoInputDataDTO;
    }

    private boolean isDerInvalid(UnitDTO der, List<String> stations, List<String> invalidDers, boolean isLvModel) {
        if (!agnoAlgorithmValidator.isAgnoValidDERPowerStationsAndPointsOfConnectionWithLV(der, stations, isLvModel)) {
            invalidDers.add(der.getName());
            return true;
        }
        return false;
    }

    private Set<AuctionDayAheadOfferDTO> getOffers(AlgorithmEvaluationConfigDTO evaluationConfigDTO) {
        if (CollectionUtils.isEmpty(evaluationConfigDTO.getOffers())) {
            return auctionDayAheadService.findAllOfferByAuctionTypeAndDeliveryDate(AuctionDayAheadType.ENERGY, evaluationConfigDTO.getDeliveryDate());
        } else {
            return auctionDayAheadService.findAllOffersById(evaluationConfigDTO.getOffers());
        }
    }

    @Override
    public boolean isSupport(AlgorithmType algorithmType) {
        return AlgorithmType.BM.equals(algorithmType);
    }
}
