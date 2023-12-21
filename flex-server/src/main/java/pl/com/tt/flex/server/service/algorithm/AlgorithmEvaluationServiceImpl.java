package pl.com.tt.flex.server.service.algorithm;

import io.github.jhipster.service.filter.LongFilter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.algorithm.*;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.file.FileContentDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferDersEntity;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.algorithm.AlgorithmEvaluationRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy.AgnoBmResultsFileReader;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy.AgnoResultsFileGenerator;
import pl.com.tt.flex.server.service.algorithm.mapper.AlgorithmEvaluationMapper;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadOfferMapper;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferViewQueryService;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferViewCriteria;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;
import pl.com.tt.flex.server.service.mail.dto.NotificationResultDTO;
import pl.com.tt.flex.server.service.mail.export.ExportResultMailService;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.settlement.SettlementService;
import pl.com.tt.flex.server.service.unit.selfSchedule.UnitSelfScheduleService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.algorithm.AlgorithmEvaluationValidator;
import pl.com.tt.flex.server.validator.algorithm.agnoAlgorithm.AgnoResultsValidator;
import pl.com.tt.flex.server.web.rest.algorithm.resource.FlexAgnoAlgorithmResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminRefreshViewWebsocketResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus.CANCELLED;
import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus.COMPLETED;
import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus.EVALUATING;
import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus.FAILURE;
import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus.TECHNICAL_FAILURE;
import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType.BM;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.ACCEPTED;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.ALGORITHM_RESULT_EXPORT;
import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.CONNECTION_TO_ALGORITHM_SERVICE_LOST;
import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.DISAGGREGATION_COMPLETED;
import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.DISAGGREGATION_FAILED;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.ID;
import static pl.com.tt.flex.server.service.algorithm.disaggregationAlgorithm.DisaggregationAlgorithmServiceImpl.extractOfferIdFromDisaggregationAlgEvaluation;
import static pl.com.tt.flex.server.service.algorithm.disaggregationAlgorithm.DisaggregationAlgorithmServiceImpl.updateOffer;

@Service
@Transactional
@Slf4j
public class AlgorithmEvaluationServiceImpl extends AbstractServiceImpl<AlgorithmEvaluationEntity, AlgorithmEvaluationDTO, Long> implements AlgorithmEvaluationService {

    private final AlgorithmEvaluationRepository algorithmEvaluationRepository;
    private final AlgorithmEvaluationMapper algorithmEvaluationMapper;
    private final AuctionDayAheadOfferMapper auctionDayAheadOfferMapper;
    private final AuctionOfferViewQueryService auctionOfferViewQueryService;
    private final AgnoResultsFileGenerator agnoResultsFileGenerator;
    private final AgnoResultsValidator agnoResultsValidator;
    private final AlgorithmEvaluationValidator validator;
    private final FlexAgnoAlgorithmResource flexAgnoAlgorithmResource;
    private final AuctionDayAheadOfferRepository auctionDayAheadOfferRepository;
    private final UnitSelfScheduleService unitSelfScheduleService;
    private final UserService userService;
    private final NotifierFactory notifierFactory;
    private final FlexAdminRefreshViewWebsocketResource flexAdminRefreshViewWebsocketResource;
    private final ExportResultMailService exportResultMailService;
    private final AgnoBmResultsFileReader agnoBmResultsFileReader;
    private final SettlementService settlementService;

    public AlgorithmEvaluationServiceImpl(final AlgorithmEvaluationRepository algorithmEvaluationRepository, final AlgorithmEvaluationMapper algorithmEvaluationMapper,
                                          final AuctionDayAheadOfferMapper auctionDayAheadOfferMapper, final AuctionOfferViewQueryService auctionOfferViewQueryService,
                                          final AlgorithmEvaluationValidator validator, final AgnoResultsFileGenerator agnoResultsFileGenerator,
                                          final AgnoResultsValidator agnoResultsValidator, final AuctionDayAheadOfferRepository auctionDayAheadOfferRepository,
                                          final UnitSelfScheduleService unitSelfScheduleService, final FlexAgnoAlgorithmResource flexAgnoAlgorithmResource,
                                          final NotifierFactory notifierFactory, final ExportResultMailService exportResultMailService,
                                          final FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource, final UserService userService,
                                          final AgnoBmResultsFileReader agnoBmResultsFileReader, final SettlementService settlementService) {
        this.algorithmEvaluationRepository = algorithmEvaluationRepository;
        this.algorithmEvaluationMapper = algorithmEvaluationMapper;
        this.auctionDayAheadOfferMapper = auctionDayAheadOfferMapper;
        this.auctionOfferViewQueryService = auctionOfferViewQueryService;
        this.validator = validator;
        this.agnoResultsFileGenerator = agnoResultsFileGenerator;
        this.agnoResultsValidator = agnoResultsValidator;
        this.flexAgnoAlgorithmResource = flexAgnoAlgorithmResource;
        this.auctionDayAheadOfferRepository = auctionDayAheadOfferRepository;
        this.unitSelfScheduleService = unitSelfScheduleService;
        this.userService = userService;
        this.notifierFactory = notifierFactory;
        this.flexAdminRefreshViewWebsocketResource = adminRefreshViewWebsocketResource;
        this.exportResultMailService = exportResultMailService;
        this.agnoBmResultsFileReader = agnoBmResultsFileReader;
        this.settlementService = settlementService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuctionOfferViewDTO> findOffersUsedInAlgorithmByCriteria(Long algorithmEvaluationId, AuctionOfferViewCriteria criteria, Pageable pageable) {
        List<Long> offerIds = findOfferIdsByAlgorithm(algorithmEvaluationId);
        var idFilter = (LongFilter) new LongFilter().setIn(offerIds);
        if (Objects.isNull(criteria)) {
            criteria = new AuctionOfferViewCriteria();
        }
        criteria.setId(idFilter);
        return auctionOfferViewQueryService.findByCriteria(criteria, pageable);
    }

    @Transactional
    public AlgorithmEvaluationDTO saveDayAheadAlgorithmEvaluation(Instant deliveryDate, Set<AuctionDayAheadOfferDTO> offers, FileDTO inputFile, AlgorithmType algorithmType, Long kdmModelId) {
        AlgorithmEvaluationEntity algorithmEvaluationEntity = new AlgorithmEvaluationEntity();
        algorithmEvaluationEntity.setKdmModelId(kdmModelId);
        algorithmEvaluationEntity.setTypeOfAlgorithm(algorithmType);
        algorithmEvaluationEntity.setDeliveryDate(deliveryDate);
        algorithmEvaluationEntity.setAlgorithmStatus(EVALUATING);
        algorithmEvaluationEntity.setInputFilesZip(inputFile.getBytesData());
        algorithmEvaluationEntity.setDaOffers(auctionDayAheadOfferMapper.toEntityFromDayAhead(offers));
        AlgorithmEvaluationDTO algorithmEvaluationDTO = algorithmEvaluationMapper.toDto(algorithmEvaluationRepository.save(algorithmEvaluationEntity));
        log.info("saveAlgorithmEvaluation() Saved algorithm evalutation with: evaluationId: {} deliveryDate: {} and aglorithmType: {}",
            algorithmEvaluationDTO.getEvaluationId(), algorithmEvaluationDTO.getDeliveryDate(), algorithmEvaluationDTO.getTypeOfAlgorithm());
        return algorithmEvaluationDTO;
    }

    @Override
    @Transactional
    public void saveAlgorithmResult(AlgEvaluationModuleDTO algEvaluationModuleDTO) throws IOException {
        log.info("saveAlgorithmResult() Save algorithm result for AlgorithmEvaluation id: {}", algEvaluationModuleDTO.getEvaluationId());
        AlgorithmEvaluationEntity evaluationEntity = getRepository().getOne(algEvaluationModuleDTO.getEvaluationId());
        evaluationEntity.setOutputFilesZip(getBytesDataFromFile(algEvaluationModuleDTO.getOutputFilesZip()));
        evaluationEntity.setProcessLogsZip(getBytesDataFromFile(algEvaluationModuleDTO.getLogFilesZip()));
        evaluationEntity.setEndDate(Instant.now());
        evaluationEntity.setAlgorithmStatus(algEvaluationModuleDTO.getStatus());
        updateOfferIfDisaggregation(evaluationEntity);
        saveVolumeTransferredToBmForAgnoAlgorithm(evaluationEntity);
        updateInputFiles(algEvaluationModuleDTO, evaluationEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuctionOfferViewDTO> findOffersUsedInAlgorithmByCriteria(Long algorithmId, AuctionOfferViewCriteria criteria) {
        List<Long> algorithmEvaluationOfferIds = findOfferIdsByAlgorithm(algorithmId);
        var idFilter = (LongFilter) new LongFilter().setIn(algorithmEvaluationOfferIds);
        if (Objects.isNull(criteria)) {
            criteria = new AuctionOfferViewCriteria();
        }
        criteria.setId(idFilter);
        return auctionOfferViewQueryService.findByCriteria(criteria);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDTO findInputFilesZip(Long algorithmId) throws ObjectValidationException {
        AlgorithmEvaluationEntity algorithmEvaluationEntity = findAlgorithmEvaluationEntityById(algorithmId);
        validator.checkInputFiles(algorithmEvaluationEntity);
        String fileName = getAlgInputFilename(algorithmId, algorithmEvaluationEntity.getTypeOfAlgorithm());
        return new FileDTO(fileName, algorithmEvaluationEntity.getInputFilesZip());
    }

    private String getAlgInputFilename(Long algorithmId, AlgorithmType type) {
        return String.format("ID_%s_input_%s.zip", algorithmId, type.name().toLowerCase(Locale.ROOT));
    }

    @Override
    @Transactional(readOnly = true)
    public FileDTO findOutputFilesZip(Long algorithmId) throws ObjectValidationException {
        AlgorithmEvaluationEntity algorithmEvaluationEntity = findAlgorithmEvaluationEntityById(algorithmId);
        validator.checkOutputFiles(algorithmEvaluationEntity);
        String fileName = getAlgOutputFilename(algorithmId, algorithmEvaluationEntity.getTypeOfAlgorithm());
        return new FileDTO(fileName, algorithmEvaluationEntity.getOutputFilesZip());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FileContentDTO> findLogFiles(Long algorithmId) throws ObjectValidationException {
        AlgorithmEvaluationEntity algorithmEvaluationEntity = findAlgorithmEvaluationEntityById(algorithmId);
        validator.checkLogFiles(algorithmEvaluationEntity);
        List<FileDTO> fileDTOS = ZipUtil.zipToFiles(algorithmEvaluationEntity.getProcessLogsZip());
        return fileDTOS.stream().map(FileContentDTO::new).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateLogFile(Long algorithmId, FileDTO fileDTO) {
        AlgorithmEvaluationEntity evaluationEntity = findAlgorithmEvaluationEntityById(algorithmId);
        byte[] processLogsZip = evaluationEntity.getProcessLogsZip();
        if (Objects.nonNull(processLogsZip)) {
            log.info("updateLogFile() Update log files. Update log file with name={} for evaluationId={}", fileDTO.getFileName(), evaluationEntity);
            processLogsZip = updateLogsFile(fileDTO, processLogsZip);
        } else {
            log.info("updateLogFile() Add log file with name={} for evaluationId={}", fileDTO.getFileName(), evaluationEntity);
            processLogsZip = ZipUtil.filesToZip(Collections.singleton(fileDTO));
        }
        algorithmEvaluationRepository.updateLogFile(algorithmId, processLogsZip);
    }

    @Override
    public void updateStatus(Long evaluationId, AlgorithmStatus status) {
        algorithmEvaluationRepository.updateStatus(evaluationId, status);
    }

    @Override
    public AlgorithmEvaluationEntity findAlgorithmEvaluationEntityById(Long algorithmId) {
        return algorithmEvaluationRepository.findById(algorithmId).orElseThrow(() -> new NoSuchElementException("Did not find algorithm evaluation"));
    }

    @Override
    @Transactional(readOnly = true)
    public FileDTO generateAgnoResultsFile(Long algEvaluationId) throws IOException, ObjectValidationException {
        AlgorithmEvaluationEntity algEvaluation = algorithmEvaluationRepository.findByIdFetchDaOffersFetchSchedulingUnit(algEvaluationId)
            .orElseThrow(() -> new IllegalArgumentException("Cannot find algorithm evaluation entity with id " + algEvaluationId));
        agnoResultsValidator.checkValid(algEvaluation.getDaOffers());
        return agnoResultsFileGenerator.getResultsFile(algEvaluation);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResultDTO generateAgnoResultsFileAndSendEmail(Long algEvaluationId) throws IOException, ObjectValidationException {
        FileDTO file = generateAgnoResultsFile(algEvaluationId);
        UserEntity currentUser = userService.getCurrentUser();
        exportResultMailService.informUserAboutExportResult(currentUser, file, ALGORITHM_RESULT_EXPORT);
        return new NotificationResultDTO(currentUser.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public AlgorithmEvaluationEntity getAlgorithmEvaluationEntity(Long id) {
        return algorithmEvaluationRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Cannot find algorithm evaluation entity with id " + id));
    }

    @Override
    @Transactional
    public void cancelAlgorithm(long evaluationId) {
        AlgorithmCancelStatus algorithmCancelStatus = flexAgnoAlgorithmResource.cancelAlgorithm(evaluationId);
        AlgorithmEvaluationEntity evaluationEntity = algorithmEvaluationRepository.getOne(evaluationId);
        // przypadek gdy zrestartowano flex-agno podczas wykonywania sie obliczen - algorytm został przerwany
        // a status w bazie jest cały czas równy EVALUATING
        if (evaluationEntity.getAlgorithmStatus().equals(EVALUATING) && algorithmCancelStatus.equals(AlgorithmCancelStatus.ALGORITHM_IS_NOT_RUNNING)) {
            evaluationEntity.setAlgorithmStatus(CANCELLED);
        }
    }

    @Override
    @Transactional
    public AlgorithmEvaluationEntity getLatestBmAlgorithmEvaluationEntityForOffer(Long offerId) {
        return algorithmEvaluationRepository.findAlgorithmEvaluationsByDaOfferId(offerId).stream()
            .filter(ae -> ae.getAlgorithmStatus().equals(COMPLETED))
            .filter(ae -> ae.getTypeOfAlgorithm().equals(BM))
            .max(Comparator.comparing(AlgorithmEvaluationEntity::getCreatedDate))
            .orElseThrow(() -> new IllegalArgumentException("Cannot find algorithm evaluation entity for offer with id " + offerId));
    }

    @Override
    public AbstractJpaRepository<AlgorithmEvaluationEntity, Long> getRepository() {
        return this.algorithmEvaluationRepository;
    }

    @Override
    public EntityMapper<AlgorithmEvaluationDTO, AlgorithmEvaluationEntity> getMapper() {
        return this.algorithmEvaluationMapper;
    }

    @Transactional
    public void setTechnicalFailureStatusForRunningAlgorithmsAndNotify() {
        for (AlgorithmEvaluationEntity algorithmEvaluation : algorithmEvaluationRepository.findByAlgorithmStatus(EVALUATING)) {
            algorithmEvaluation.setAlgorithmStatus(TECHNICAL_FAILURE);
            notifyUser(CONNECTION_TO_ALGORITHM_SERVICE_LOST, algorithmEvaluation.getId(), algorithmEvaluation.getCreatedBy());
            flexAdminRefreshViewWebsocketResource.updateStatus(new MinimalDTO<>(algorithmEvaluation.getId(), TECHNICAL_FAILURE));
        }
    }

    private void saveVolumeTransferredToBmForAgnoAlgorithm(AlgorithmEvaluationEntity evaluationEntity) throws IOException {
        if (evaluationEntity.getTypeOfAlgorithm().equals(BM)) {
            agnoBmResultsFileReader.getBandsByOfferId(evaluationEntity).forEach((bands, offerId) -> saveVolumeTransferredToBmInOffer(offerId, bands));
        }
    }

    private void saveVolumeTransferredToBmInOffer(Long offerId, List<AuctionOfferBandDataEntity> bands) {
        Map<Pair<String, String>, AuctionOfferBandDataEntity> bandsByHourAndBandNumber = bands.stream().collect(Collectors.toMap(band -> Pair.of(band.getHourNumber(), band.getBandNumber()), Function.identity()));
        auctionDayAheadOfferRepository.findById(offerId).stream()
            .map(AuctionDayAheadOfferEntity::getUnits).flatMap(List::stream)
            .map(AuctionOfferDersEntity::getBandData).flatMap(List::stream)
            .forEach(dbBand -> {
                AuctionOfferBandDataEntity agnoResultBand = bandsByHourAndBandNumber.get(Pair.of(dbBand.getHourNumber(), dbBand.getBandNumber()));
                BigDecimal volumeTransferredToBM = BigDecimal.ZERO;
                if (Objects.nonNull(agnoResultBand) && agnoResultBand.getAcceptedVolume().compareTo(BigDecimal.ZERO) > 0) {
                    volumeTransferredToBM = agnoResultBand.getAcceptedVolume().multiply(BigDecimal.valueOf(1000)).multiply(getGdfFactor(dbBand));
                }
                dbBand.setVolumeTransferredToBM(volumeTransferredToBM);
            });
    }

    private BigDecimal getGdfFactor(AuctionOfferBandDataEntity agnoResultBand) {
        String[] parts = agnoResultBand.getGdf().split("/");
        if(Arrays.stream(parts).noneMatch(x -> x.equals("0"))){
            return new BigDecimal(parts[0]).divide(new BigDecimal(parts[1]), 10, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private void updateOfferIfDisaggregation(AlgorithmEvaluationEntity evaluationEntity) throws IOException {
        if (evaluationEntity.getTypeOfAlgorithm().equals(AlgorithmType.DISAGGREGATION) && !Set.of(CANCELLED, FAILURE).contains(evaluationEntity.getAlgorithmStatus())) {
            AuctionOfferImportDataResult importResult = new AuctionOfferImportDataResult();
            Long offerId = extractOfferIdFromDisaggregationAlgEvaluation(evaluationEntity);
            try {
                AuctionDayAheadOfferEntity offer = auctionDayAheadOfferRepository.findById(offerId)
                    .orElseThrow(() -> new RuntimeException("Cannot find day ahead auction offer with id: " + offerId));
                Map<Long, List<MinimalDTO<String, BigDecimal>>> selfSchedulesByDerId = getSelfScheduleValuesByDerId(evaluationEntity, offer);
                AuctionOfferStatus statusBefore = offer.getStatus();
                updateOffer(evaluationEntity, offer, selfSchedulesByDerId);
                if(!statusBefore.equals(ACCEPTED) && offer.getStatus().equals(ACCEPTED)) {
                    settlementService.generateSettlementsForOffer(offer);
                }
                importResult.addImportedBids(offerId);
            } catch (Exception e) {
                log.debug("updateOfferIfDisaggregation() Problem with offer import! Error message: {}", e.getMessage());
                importResult.addNotImportedBids(new MinimalDTO<>(offerId.toString(), e.getMessage()));
            }
            sendNotificationAboutImportOffer(evaluationEntity.getId(), importResult, evaluationEntity.getCreatedBy());
        }
    }

    private void sendNotificationAboutImportOffer(Long algorithmId, AuctionOfferImportDataResult result, String createdBy) {
        NotificationEvent event = result.getNotImportedBids().isEmpty() ? DISAGGREGATION_COMPLETED : DISAGGREGATION_FAILED;
        notifyUser(event, algorithmId, createdBy);
    }

    private Map<Long, List<MinimalDTO<String, BigDecimal>>> getSelfScheduleValuesByDerId(AlgorithmEvaluationEntity evaluationEntity, AuctionDayAheadOfferEntity offer) {
        Map<Long, List<MinimalDTO<String, BigDecimal>>> selfSchedulesByDerId = new HashMap<>();
        offer.getUnits().stream()
            .map(AuctionOfferDersEntity::getUnit)
            .map(UnitEntity::getId)
            .forEach(id -> selfSchedulesByDerId.put(id, unitSelfScheduleService.findVolumesForDerAndSelfScheduleDate(id, evaluationEntity.getDeliveryDate())));
        return selfSchedulesByDerId;
    }

    private void updateInputFiles(AlgEvaluationModuleDTO algEvaluationModuleDTO, AlgorithmEvaluationEntity evaluationEntity) {
        if (Objects.nonNull(algEvaluationModuleDTO.getInputFilesZip()) && !evaluationEntity.getTypeOfAlgorithm().equals(AlgorithmType.DISAGGREGATION)) {
            evaluationEntity.setInputFilesZip(getBytesDataFromFile(algEvaluationModuleDTO.getInputFilesZip()));
        }
    }

    public void notifyUser(NotificationEvent status, Long algorithmEvaluationId, String createdBy) {
        try {
            Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create().addParam(ID, algorithmEvaluationId.toString()).build();
            List<MinimalDTO<Long, String>> usersToBeNotified = userService.getUsersByLogin(Set.of(createdBy));
            NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, status, notificationParams, usersToBeNotified);
        } catch (Exception e) {
            log.debug("Cannot send notification about algorithm result");
            e.printStackTrace();
        }
    }

    private byte[] getBytesDataFromFile(FileDTO fileDTO) {
        if (Objects.nonNull(fileDTO)) {
            return fileDTO.getBytesData();
        }
        return null;
    }

    private List<Long> findOfferIdsByAlgorithm(Long evaluationId) {
        List<Long> offerIds = algorithmEvaluationRepository.findDaOfferIdsByAlgorithmEvaluationId(evaluationId);
        offerIds.addAll(algorithmEvaluationRepository.findCmvcOfferIdsByAlgorithmEvaluationId(evaluationId));
        return offerIds;
    }

    private String getAlgOutputFilename(Long algorithmId, AlgorithmType type) {
        return String.format("ID_%s_output_%s.zip", algorithmId, type.name().toLowerCase(Locale.ROOT));
    }

    private byte[] updateLogsFile(FileDTO fileDTO, byte[] processLogsZip) {
        List<FileDTO> dbFiles = ZipUtil.zipToFiles(processLogsZip);
        Optional<FileDTO> dbFileToUpdate = dbFiles.stream()
            .filter(f -> f.getFileName().equals(fileDTO.getFileName()))
            .findAny();
        if (dbFileToUpdate.isPresent()) {
            log.debug("updateLogsFile() Replace log file={}", fileDTO.getFileName());
            Collections.replaceAll(dbFiles, dbFileToUpdate.get(), fileDTO);
        } else {
            log.debug("updateLogsFile() Add new log file={}", fileDTO.getFileName());
            dbFiles.add(fileDTO);
        }
        processLogsZip = ZipUtil.filesToZip(dbFiles);
        return processLogsZip;
    }
}
