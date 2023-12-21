package pl.com.tt.flex.server.service.algorithm.disaggregationAlgorithm;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.Range;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.algorithm.*;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferDersEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.service.algorithm.AlgorithmEvaluationService;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.AlgorithmAbstract;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.common.XlsxUtil;
import pl.com.tt.flex.server.service.importData.auctionOffer.AuctionOfferImportService;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.web.rest.algorithm.resource.FlexAgnoAlgorithmResource;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType.*;
import static pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy.AgnoResultsFileGeneratorImpl.extractTimestampFromFileDTO;
import static pl.com.tt.flex.server.service.auction.da.file.reader.DAEnergyOfferDisaggregationFileReader.readInputFileBands;
import static pl.com.tt.flex.server.service.auction.da.file.reader.DAEnergyOfferDisaggregationFileReader.readOutputFilesUnitPowerByName;
import static pl.com.tt.flex.server.service.common.dto.FileDTOUtil.parseMultipartFile;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.IMPORT_ACTIVE_POWER_OUT_OF_RANGE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Slf4j
@Transactional
public class DisaggregationAlgorithmServiceImpl extends AlgorithmAbstract implements DisaggregationAlgorithmService {

    private static final String ZIP_FILENAME_FORMAT = "input_disaggregation_%s.zip"; // input_disaggregation_{delivery_date}.zip
    private static final int DEFAULT_NUMBER_OF_DECIMAL_PLACES_FOR_NOTIFICATIONS = 2;

    private final AuctionDayAheadService auctionDayAheadService;
    private final FlexAgnoAlgorithmResource flexAgnoAlgorithmResource;
    private final AlgorithmEvaluationService algorithmEvaluationService;
    private final AuctionDayAheadOfferRepository auctionOfferRepository;
    private final AuctionOfferImportService auctionOfferImportService;

    public DisaggregationAlgorithmServiceImpl(FlexAgnoAlgorithmResource flexAgnoAlgorithmResource,
                                              UnitService unitService,
                                              AuctionDayAheadService auctionDayAheadService,
                                              AlgorithmEvaluationService algorithmEvaluationService,
                                              AuctionDayAheadOfferRepository auctionOfferRepository,
                                              AuctionOfferImportService auctionOfferImportService) {
        super(unitService);
        this.auctionDayAheadService = auctionDayAheadService;
        this.flexAgnoAlgorithmResource = flexAgnoAlgorithmResource;
        this.algorithmEvaluationService = algorithmEvaluationService;
        this.auctionOfferRepository = auctionOfferRepository;
        this.auctionOfferImportService = auctionOfferImportService;
    }

    /**
     * Tworzy obiekt AlgorithmEvaluation zapisuje z importowanym plikiem jako input algorytmu, oraz wysyła do modułu agno zapytanie rozpoczynające obliczenia dezagregacji
     */
    @Transactional
    public void startOfferUpdateImport(MultipartFile multipartFile) throws IOException {
        log.debug("startOfferUpdateImport() Import offer update from balancing market");
        XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
        Long offerId = extractOfferId(workbook);
        AuctionDayAheadOfferDTO offer = auctionDayAheadService.findOfferById(offerId)
            .orElseThrow(() -> new RuntimeException("Cannot find day ahead auction offer with id: " + offerId));
        boolean offerRejected = rejectOfferIfFileEmpty(multipartFile, offerId);
        if (!offerRejected) {
            AlgorithmEvaluationConfigDTO algConfig = getAlgorithmConfig(offer);
            startAlgorithm(algConfig, offer, multipartFile);
        }
    }

    /**
     * Konfiguruje i uruchamia algorytm dezagregacji
     */
    public void startAlgorithm(AlgorithmEvaluationConfigDTO evaluationConfigDTO, AuctionDayAheadOfferDTO offer, MultipartFile importedFile) throws IOException {
        log.info("startAlgorithm() Algorithm config: {}", evaluationConfigDTO);
        AlgEvaluationModuleDTO algEvaluationModuleDTO = saveEvaluationAndPrepareModuleDTO(offer, importedFile, evaluationConfigDTO);
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
     * Zwraca plik wejściowy, wykorzystany wcześniej do obliczeń agno dla podanej oferty
     */
    @Override
    public FileDTO getAlgorithmInputFiles(AlgorithmEvaluationConfigDTO evaluationConfigDTO, Set<AuctionDayAheadOfferDTO> offers) {
        Long offerId = offers.stream().findAny().get().getId();
        AlgorithmEvaluationEntity evaluation = algorithmEvaluationService.getLatestBmAlgorithmEvaluationEntityForOffer(offerId);
        byte[] inputZipData = evaluation.getInputFilesZip();
        String filename = String.format(ZIP_FILENAME_FORMAT, evaluationConfigDTO.getDeliveryDate());
        return new FileDTO(filename, inputZipData);
    }

    /**
     * Zwraca id oferty zawartej w podanym pliku importu agno lub null
     */
    public static Long extractOfferId(Workbook workbook) {

        try {
            Sheet sheet = workbook.getSheetAt(0);
            String offerIdString = sheet.getRow(0).getCell(0).getStringCellValue().split("-")[0].strip();
            return Long.parseLong(offerIdString);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Zwraca id oferty zawartej pliku zapisanym jako input algorytmu dezagregacji
     */
    public static Long extractOfferIdFromDisaggregationAlgEvaluation(AlgorithmEvaluationEntity evaluation) throws IOException {
        if (!evaluation.getTypeOfAlgorithm().equals(DISAGGREGATION)) {
            throw new IllegalStateException("Algorithm type not supported");
        }
        FileDTO inputFileDTO = ZipUtil.zipToFiles(evaluation.getInputFilesZip()).get(0);
        Workbook inputWorkbook = XlsxUtil.getWorkbook(inputFileDTO);
        return extractOfferId(inputWorkbook);
    }

    /**
     * Jeśli podany obiekt jest typu dezagregacj, aktualizuje ofertę zgodnie z danymi z pliku wejściowego i wynikami dezagregacji
     */
    public static void updateOffer(AlgorithmEvaluationEntity evaluation, AuctionDayAheadOfferEntity offer,
                                   Map<Long, List<MinimalDTO<String, BigDecimal>>> selfSchedulesByDerId) throws IOException {
        FileDTO inputFileDTO = ZipUtil.zipToFiles(evaluation.getInputFilesZip()).get(0);
        Workbook inputWorkbook = XlsxUtil.getWorkbook(inputFileDTO);
        Map<Pair<String, String>, AuctionOfferBandDataEntity> updatedBandsByBandNum = readInputFileBands(inputWorkbook);
        List<FileDTO> outputFileDTOs = ZipUtil.zipToFiles(evaluation.getOutputFilesZip());
        Map<Pair<String, Integer>, BigDecimal> unitPowerByName = readOutputFilesUnitPowerByName(outputFileDTOs);
        setNewVolumesOrReject(evaluation, updatedBandsByBandNum, unitPowerByName, offer, selfSchedulesByDerId);
    }

    /**
     * Zwraca true jeśli oferta została odrzucona
     */
    private boolean rejectOfferIfFileEmpty(MultipartFile multipartFile, Long offerId) throws IOException {
        Workbook inputWorkbook = new XSSFWorkbook(multipartFile.getInputStream());
        Map<Pair<String, String>, AuctionOfferBandDataEntity> updatedBandsByBandNum = readInputFileBands(inputWorkbook);
        if (updatedBandsByBandNum.isEmpty()) {
            AuctionDayAheadOfferEntity offerEntity = auctionOfferRepository.findById(offerId)
                .orElseThrow(() -> new RuntimeException("Cannot find offer with id " + offerId));
            offerEntity.setStatus(AuctionOfferStatus.REJECTED);
            setBandsVolumeZero(offerEntity);
            AuctionOfferImportDataResult importResult = new AuctionOfferImportDataResult();
            importResult.addImportedBids(offerId);
            auctionOfferImportService.sendNotificationAboutImportOffer(importResult);
            return true;
        }
        return false;
    }

    private static void setBandsVolumeZero(AuctionDayAheadOfferEntity offerEntity) {
        for (AuctionOfferDersEntity offerDer : offerEntity.getUnits()) {
            for (AuctionOfferBandDataEntity band : offerDer.getBandData()) {
                if (!band.getBandNumber().equals("0")) {
                    band.setAcceptedVolume(BigDecimal.ZERO);
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private AlgEvaluationModuleDTO saveEvaluationAndPrepareModuleDTO(AuctionDayAheadOfferDTO offer, MultipartFile importedFile, AlgorithmEvaluationConfigDTO evaluationConfigDTO) throws IOException {
        FileDTO algorithmInputFiles = getAlgorithmInputFiles(evaluationConfigDTO, Set.of(offer));
        FileDTO algorithmOutputFiles = getAlgorithmOutputFiles(evaluationConfigDTO, offer);
        Long auctionId = offer.getAuctionDayAhead().getId();
        Instant deliveryDate = auctionDayAheadService.findById(auctionId)
            .map(AuctionDayAheadDTO::getDeliveryDate)
            .orElseThrow(() -> new RuntimeException("Cannot find day ahead auction with id: " + auctionId));
        FileDTO inputFileZip = getZipFileDTO(deliveryDate, parseMultipartFile(importedFile), algorithmInputFiles);
        AlgorithmEvaluationDTO evaluationDTO = algorithmEvaluationService
            .saveDayAheadAlgorithmEvaluation(deliveryDate, Set.of(offer), inputFileZip, DISAGGREGATION, evaluationConfigDTO.getKdmModelId());
        AlgEvaluationModuleDTO algEvaluationModuleDTO = getAlgEvaluationModuleDTO(evaluationConfigDTO, algorithmInputFiles, evaluationDTO);
        algEvaluationModuleDTO.setPowerArgsByTimestamp(getPowerArgsByTimestamp(importedFile, algorithmOutputFiles));
        return algEvaluationModuleDTO;
    }

    private Map<String, AlgorithmPowerArgsDTO> getPowerArgsByTimestamp(MultipartFile importedFile, FileDTO algorithmOutputFiles) throws IOException {
        List<FileDTO> agnoOutputFiles = ZipUtil.zipToFiles(algorithmOutputFiles.getBytesData());
        Workbook importedWorkbook = new XSSFWorkbook(importedFile.getInputStream());
        Sheet importedSheet = importedWorkbook.getSheetAt(0);
        Map<String, AlgorithmPowerArgsDTO> powerArgsByTimestamp = new HashMap<>();
        for (FileDTO agnoOutputFile : agnoOutputFiles) {
            if (BM.getFileNamePattern().matcher(agnoOutputFile.getFileName()).matches()) {
                String timestamp = extractTimestampFromFileDTO(agnoOutputFile);
                AlgorithmPowerArgsDTO powerArgs = getPowerArgs(importedSheet, timestamp, agnoOutputFile);
                powerArgsByTimestamp.put(timestamp, powerArgs);
            }
        }
        return powerArgsByTimestamp;
    }

    private AlgorithmPowerArgsDTO getPowerArgs(Sheet importedSheet, String timestamp, FileDTO agnoOutputFile) throws IOException {
        Workbook agnoOutputWorkbook = XlsxUtil.getWorkbook(agnoOutputFile);
        Sheet agnoOutputInfoSheet = agnoOutputWorkbook.getSheetAt(2);
        Row agnoInfoValueRow = agnoOutputInfoSheet.getRow(1);
        BigDecimal activePower = getActivePower(importedSheet, timestamp);
        BigDecimal pMin = BigDecimal.valueOf(agnoInfoValueRow.getCell(2).getNumericCellValue());
        BigDecimal pMax = BigDecimal.valueOf(agnoInfoValueRow.getCell(3).getNumericCellValue());
        validateActivePowerInRange(activePower, pMin, pMax);
        return new AlgorithmPowerArgsDTO(activePower.doubleValue(), pMin.doubleValue(), pMax.doubleValue());
    }

    private void validateActivePowerInRange(BigDecimal activePower, BigDecimal pMin, BigDecimal pMax) throws JsonProcessingException {
        int kWtoMWRatio = 1000;
        BigDecimal activePowerKW = BigDecimal.valueOf(kWtoMWRatio).multiply(activePower);
        BigDecimal pMinKW = BigDecimal.valueOf(kWtoMWRatio).multiply(pMin);
        BigDecimal pMaxKW = BigDecimal.valueOf(kWtoMWRatio).multiply(pMax);

        if(!Range.between(pMinKW, pMaxKW).contains(activePowerKW)) {
            ObjectMapper objectMapper = new ObjectMapper();
            int decimalPlacesToDisplay = getDecimalPlacesNumberToDisplay(pMinKW.doubleValue(), pMaxKW.doubleValue(), activePowerKW.doubleValue());
            AlgorithmPowerArgsDTO powerArgsKW = new AlgorithmPowerArgsDTO(
                getRoundedValue(activePowerKW.doubleValue(), decimalPlacesToDisplay),
                getRoundedValue(pMinKW.doubleValue(), decimalPlacesToDisplay),
                getRoundedValue(pMaxKW.doubleValue(), decimalPlacesToDisplay)
            );
            String errorParam = objectMapper.writeValueAsString(powerArgsKW);
            throw new ObjectValidationException("Active power is out of range!", IMPORT_ACTIVE_POWER_OUT_OF_RANGE, errorParam);
        }
    }

    private int getDecimalPlacesNumberToDisplay(double pMinKW, double pMaxKW, double activePowerKW) {
        String[] pMinKWParts = Double.toString(pMinKW).split("\\.");
        String[] pMaxKWParts = Double.toString(pMaxKW).split("\\.");
        String[] activePowerKWParts = Double.toString(activePowerKW).split("\\.");

        if(activePowerKWParts[0].equals(pMinKWParts[0])) {
            return getDistinctDecimalPlaceNumber(pMinKWParts, activePowerKWParts);
        }
        if(activePowerKWParts[0].equals(pMaxKWParts[0])) {
            return getDistinctDecimalPlaceNumber(pMaxKWParts, activePowerKWParts);
        }
        return DEFAULT_NUMBER_OF_DECIMAL_PLACES_FOR_NOTIFICATIONS;
    }

    private int getDistinctDecimalPlaceNumber(String[] number1Parts, String[] number2Parts) {
        for (int i = 0; i < Math.max(number1Parts[1].length(), number2Parts[1].length()); i++) {
            char c1 = getCharOrZero(number1Parts[1], i);
            char c2 = getCharOrZero(number2Parts[1], i);
            if (c1 != c2) {
                return i + 1;
            }
        }
        return DEFAULT_NUMBER_OF_DECIMAL_PLACES_FOR_NOTIFICATIONS;
    }

    private char getCharOrZero(String numberDecimalPart, int index) {
        return Optional.of(numberDecimalPart)
            .filter(string -> string.length() > index)
            .map(string -> string.charAt(index))
            .orElse('0');
    }

    private double getRoundedValue(double value, int decimalPlaces) {
        double scale = Math.pow(10, decimalPlaces);
        return Math.round(value * scale) / scale;
    }

    private BigDecimal getActivePower(Sheet sheet, String timestamp) {
        final int FIRST_BAND_ROW_NUM = 2;
        final int BAND_LABEL_COL_NUM = 1;
        final Set<String> SELF_SCHEDULE_LABELS = Set.of("S", "P");
        BigDecimal activePower = BigDecimal.ZERO;
        for (int rowNum = FIRST_BAND_ROW_NUM; rowNum <= sheet.getLastRowNum(); rowNum++) {
            Row row = sheet.getRow(rowNum);
            Cell bandLabelCell = row.getCell(BAND_LABEL_COL_NUM);
            String bandDirection = bandLabelCell.getStringCellValue().substring(3, 4);
            Cell volumeCell = row.getCell(Integer.parseInt(timestamp) * 2);
            BigDecimal bandVolume = BigDecimal.valueOf(volumeCell.getNumericCellValue());
            if (bandDirection.equals("+") || SELF_SCHEDULE_LABELS.contains(bandDirection)) {
                activePower = activePower.add(bandVolume);
            } else if (bandDirection.equals("-")) {
                activePower = activePower.subtract(bandVolume);
            }
        }
        return activePower;
    }

    /**
     * Zgodnie z wynikami dezagregacji odrzuca ofertę lub aktualizuje wartości wolumenu
     */
    private static void setNewVolumesOrReject(AlgorithmEvaluationEntity evaluation, Map<Pair<String, String>, AuctionOfferBandDataEntity> updatedBandsByBandNum,
                                              Map<Pair<String, Integer>, BigDecimal> unitPowerByName, AuctionDayAheadOfferEntity offer, Map<Long, List<MinimalDTO<String, BigDecimal>>> selfSchedulesByDerId) {
        if (isOfferRejected(unitPowerByName)) {
            offer.setStatus(AuctionOfferStatus.REJECTED);
            setBandsVolumeZero(offer);
        } else {
            offer.setStatus(AuctionOfferStatus.ACCEPTED);
            for (AuctionOfferDersEntity offerDer : offer.getUnits()) {
                UnitEntity der = offerDer.getUnit();
                List<MinimalDTO<String, BigDecimal>> selfSchedule = selfSchedulesByDerId.get(der.getId());
                for (int timestamp = 1; timestamp < 25; timestamp++) {
                    final String timestampKey = String.valueOf(timestamp);
                    final BigDecimal dersSelfSchedule = selfSchedule.stream()
                        .filter(dto -> dto.getId().equals(timestampKey))
                        .map(MinimalDTO::getValue)
                        .findAny()
                        .orElseThrow(() -> new RuntimeException("Cannot find self schedule for der " + der.getId() + " on " + evaluation.getDeliveryDate()));
                    calculateBandsIfPUnitPresent(updatedBandsByBandNum, unitPowerByName, offerDer, der, timestamp, timestampKey, dersSelfSchedule);
                }
            }
        }
    }

    /**
     * Jeśli @param unitPowerByName zawiera moc dla podanych @param
     *
     */
    private static void calculateBandsIfPUnitPresent(Map<Pair<String, String>, AuctionOfferBandDataEntity> updatedBandsByBandNum,
                                                     Map<Pair<String, Integer>, BigDecimal> unitPowerByName, AuctionOfferDersEntity offerDer,
                                                     UnitEntity der, int timestamp, String timestampKey, BigDecimal dersSelfSchedule) {
        Pair<String, Integer> pUnitKey = Pair.of(der.getName(), timestamp);
        if(unitPowerByName.containsKey(pUnitKey)) {
            BigDecimal pUnit = unitPowerByName.get(pUnitKey);
            calculateUpBands(offerDer, timestampKey, dersSelfSchedule, updatedBandsByBandNum, pUnit);
            calculateDownBands(offerDer, timestampKey, dersSelfSchedule, updatedBandsByBandNum, pUnit);
        }
    }

    /**
     * Zwraca true jeśli algorytm dezagregacji odrzucił ofertę (jeśli moc wszystkich derów wynosi 0)
     * @param unitPowerByName
     */
    private static boolean isOfferRejected(Map<Pair<String, Integer>, BigDecimal> unitPowerByName) {
        return unitPowerByName.values().stream().noneMatch(power -> power.compareTo(BigDecimal.ZERO) != 0);
    }

    /**
     * Oblicza i zapisuje nowe wartości pasm dodatnich
     */
    private static void calculateUpBands(AuctionOfferDersEntity offerDer, String timestampKey, BigDecimal dersSelfSchedule,
                                         Map<Pair<String, String>, AuctionOfferBandDataEntity> updatedBandsByBandNum, BigDecimal pUnit) {
        List<AuctionOfferBandDataEntity> positiveBandsInTimestamp = offerDer.getBandData().stream()
            .filter(band -> band.getHourNumber().equals(timestampKey))
            .filter(band -> Integer.parseInt(band.getBandNumber()) > 0)
            .sorted(Comparator.comparing(AuctionOfferBandDataEntity::getBandNumber))
            .collect(Collectors.toList());
        String extremeTimestampNumber;
        if (positiveBandsInTimestamp.size() > 0) {
            extremeTimestampNumber = positiveBandsInTimestamp.get(positiveBandsInTimestamp.size() - 1).getBandNumber();
        } else {
            return;
        }
        BigDecimal powerLeftToAssign = pUnit.subtract(dersSelfSchedule);
        for (AuctionOfferBandDataEntity band : positiveBandsInTimestamp) {
            Pair<String, String> key = Pair.of("+" + band.getBandNumber(), band.getHourNumber());
            if (powerLeftToAssign.compareTo(BigDecimal.ZERO) <= 0 | !updatedBandsByBandNum.containsKey(key)) {
                band.setAcceptedVolume(BigDecimal.ZERO);
            } else {
                if (band.getBandNumber().equals(extremeTimestampNumber) && powerLeftToAssign.compareTo(BigDecimal.ZERO) >= 0) {
                    band.setAcceptedVolume(powerLeftToAssign);
                } else {
                    BigDecimal calculatedVolume = band.getAcceptedVolume();
                    powerLeftToAssign = powerLeftToAssign.subtract(calculatedVolume);
                    if (powerLeftToAssign.compareTo(BigDecimal.ZERO) < 0) {
                        calculatedVolume = calculatedVolume.add(powerLeftToAssign);
                        band.setAcceptedVolume(calculatedVolume);
                    }
                }
            }
            band.markAsEdited();
        }
    }

    /**
     * Oblicza i zapisuje nowe wartości pasm ujemnych
     */
    private static void calculateDownBands(AuctionOfferDersEntity offerDer, String timestampKey, BigDecimal dersSelfSchedule,
                                           Map<Pair<String, String>, AuctionOfferBandDataEntity> updatedBandsByBandNum, BigDecimal pUnit) {
        List<AuctionOfferBandDataEntity> negativeBandsInTimestamp = offerDer.getBandData().stream()
            .filter(band -> band.getHourNumber().equals(timestampKey))
            .filter(band -> Integer.parseInt(band.getBandNumber()) < 0)
            .sorted(Comparator.comparing(AuctionOfferBandDataEntity::getBandNumber))
            .collect(Collectors.toList());
        String extremeTimestampNumber;
        if (negativeBandsInTimestamp.size() > 0) {
            extremeTimestampNumber = negativeBandsInTimestamp.get(negativeBandsInTimestamp.size() - 1).getBandNumber();
        } else {
            return;
        }
        BigDecimal powerLeftToAssign = dersSelfSchedule.subtract(pUnit);
        for (AuctionOfferBandDataEntity band : negativeBandsInTimestamp) {
            Pair<String, String> key = Pair.of(band.getBandNumber(), band.getHourNumber());
            if (powerLeftToAssign.compareTo(BigDecimal.ZERO) <= 0 | !updatedBandsByBandNum.containsKey(key)) {
                band.setAcceptedVolume(BigDecimal.ZERO);
            } else {
                if (band.getBandNumber().equals(extremeTimestampNumber) && powerLeftToAssign.compareTo(BigDecimal.ZERO) >= 0) {
                    band.setAcceptedVolume(powerLeftToAssign);
                } else {
                    BigDecimal calculatedVolume = band.getAcceptedVolume();
                    powerLeftToAssign = powerLeftToAssign.subtract(calculatedVolume);
                    if (powerLeftToAssign.compareTo(BigDecimal.ZERO) < 0) {
                        calculatedVolume = calculatedVolume.add(powerLeftToAssign);
                        band.setAcceptedVolume(calculatedVolume);
                    }
                }
            }
            band.markAsEdited();
        }
    }

    /**
     * Tworzy konfigurację algorytmu dezagregacji dla podanej oferty
     */
    private AlgorithmEvaluationConfigDTO getAlgorithmConfig(AuctionDayAheadOfferDTO offer) {
        Long offerId = offer.getId();
        Long kdmModelId = getKdmForOffer(offerId);
        AlgorithmEvaluationConfigDTO algConfig = new AlgorithmEvaluationConfigDTO();
        algConfig.setAlgorithmType(DISAGGREGATION);
        algConfig.setDeliveryDate(offer.getAcceptedDeliveryPeriodFrom());
        algConfig.setKdmModelId(kdmModelId);
        algConfig.setOffers(List.of(offerId));
        return algConfig;
    }

    /**
     * Zwraca plik zip zawierający podane pliki
     * nazwa zwracanego pliku zip: input_disaggregation_{data dostawy}.zip
     */
    private FileDTO getZipFileDTO(Instant deliveryDate, FileDTO userImportedFile, FileDTO generatedInputZip) {
        String filename = String.format(ZIP_FILENAME_FORMAT, deliveryDate);
        return new FileDTO(
            filename,
            ZipUtil.filesToZip(
                Collections.singleton(userImportedFile),
                Collections.singleton(generatedInputZip)
            ));
    }

    /**
     * Zwraca typ kdm dla oferty
     */
    private Long getKdmForOffer(Long offerId) {
        AlgorithmEvaluationEntity evaluationEntity = algorithmEvaluationService.getLatestBmAlgorithmEvaluationEntityForOffer(offerId);
        return evaluationEntity.getKdmModelId();
    }

    /**
     * Zwraca plik wyjściowy zwrócony z obliczeń agno dla podanej oferty
     */
    private FileDTO getAlgorithmOutputFiles(AlgorithmEvaluationConfigDTO evaluationConfigDTO, AuctionDayAheadOfferDTO offer) {
        Long offerId = offer.getId();
        AlgorithmEvaluationEntity evaluation = algorithmEvaluationService.getLatestBmAlgorithmEvaluationEntityForOffer(offerId);
        byte[] outputZipData = evaluation.getOutputFilesZip();
        String filename = String.format(ZIP_FILENAME_FORMAT, evaluationConfigDTO.getDeliveryDate());
        return new FileDTO(filename, outputZipData);
    }

    /**
     * Metoda nieużywana, patrz {@link DisaggregationAlgorithmServiceImpl#startAlgorithm(AlgorithmEvaluationConfigDTO, AuctionDayAheadOfferDTO, MultipartFile)}
     */
    @Override
    public void startAlgorithm(AlgorithmEvaluationConfigDTO evaluationConfigDTO) {
    }

    @Override
    public boolean isSupport(AlgorithmType algorithmType) {
        return DISAGGREGATION.equals(algorithmType);
    }

}
