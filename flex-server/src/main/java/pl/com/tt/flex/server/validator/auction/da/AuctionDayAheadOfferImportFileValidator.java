package pl.com.tt.flex.server.validator.auction.da;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Range;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.UnitSelfScheduleService;
import pl.com.tt.flex.server.validator.common.CommonValidatorUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType.CAPACITY;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType.ENERGY;
import static pl.com.tt.flex.model.service.dto.product.type.Direction.DOWN;
import static pl.com.tt.flex.model.service.dto.product.type.Direction.UP;
import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.XLSX;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DACapacityOfferImportTemplateFactory.*;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DAEnergyOfferImportTemplateFactory.*;
import static pl.com.tt.flex.server.util.DateUtil.EXTRA_HOUR_CONSTANT;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Slf4j
@Component
public class AuctionDayAheadOfferImportFileValidator {

    private final Set<FileExtension> SUPPORTED_FILE_EXTENSIONS = Sets.newHashSet(XLSX);
    private final Set<String> SELF_SCHEDULE_LABELS = Set.of("SS", "PP");
    private final Set<String> SCHEDULING_UNIT_LABELS = Set.of("SU", "JG");
    private final Set<String> SCHEDULING_UNIT_SCHEDULE_LABELS = Set.of("SU SS", "JG PP");
    private final Set<String> SCHEDULING_UNIT_OFFER_LABELS = Set.of("SU offer", "JG oferta");

    private final UnitSelfScheduleService unitSelfScheduleService;

    public AuctionDayAheadOfferImportFileValidator(UnitSelfScheduleService unitSelfScheduleService) {
        this.unitSelfScheduleService = unitSelfScheduleService;
    }

    public void checkOfferImportFileValid(MultipartFile multipartFile, AuctionDayAheadDTO auction, SchedulingUnitDTO schedulingUnitDTO) throws ObjectValidationException, IOException {
        CommonValidatorUtil.checkFileExtensionValid(multipartFile, SUPPORTED_FILE_EXTENSIONS);
        checkOfferImportFileContextValid(multipartFile, auction, schedulingUnitDTO);
    }

    private void checkOfferImportFileContextValid(MultipartFile multipartFile, AuctionDayAheadDTO auction, SchedulingUnitDTO schedulingUnitDTO) throws ObjectValidationException, IOException {
        Workbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        validateColumns(sheet);
        validateDersForAuctionType(auction, schedulingUnitDTO, sheet);
        validateSchedulingUnitSection(sheet, auction.getType(), auction.getProduct().getDirection());
    }

    /**
     * Metoda sprawdza poprawność wypełnienia sekcji jednostki grafikowej
     */
    private void validateSchedulingUnitSection(Sheet sheet, AuctionDayAheadType type, Direction direction) throws ObjectValidationException {
        String unitLabel;
        int unitLabelRow = 1, columnLabelRow = 1;
        try {
            unitLabel = sheet.getRow(unitLabelRow).getCell(columnLabelRow).getStringCellValue();
        } catch (Exception ex) {
            throw new ObjectValidationException("Wrong scheduling unit label", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
        }

        if (!SCHEDULING_UNIT_LABELS.contains(unitLabel)) {
            throw new ObjectValidationException("Wrong scheduling unit label: " + unitLabel, AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
        }
        validateSchedulingUnitSectionForAuctionType(sheet, type, direction);
    }

    /**
     * Metoda sprawdza poprawność wypełnienia sekcji jednostki grafikowej w zależności od typu aukcji
     */
    private void validateSchedulingUnitSectionForAuctionType(Sheet sheet, AuctionDayAheadType type, Direction direction) throws ObjectValidationException {
        switch (type) {
            case ENERGY:
                validateEnergySuOfferAndScheduleLabels(sheet);
                break;
            case CAPACITY:
                validateCapacitySuOfferAndScheduleLabels(sheet, direction);
                break;
            default:
                throw new ObjectValidationException("Unsupported auction type", AUCTION_DA_OFFER_IMPORT_UNSUPPORTED_AUCTION_TYPE);
        }
    }

    /**
     * Metoda sprawdza, czy etykiety pasm w sekcji jednostki grafikowej dla oferty na energię są prawidłowe
     */
    private void validateEnergySuOfferAndScheduleLabels(Sheet sheet) throws ObjectValidationException {
        int bandColumn = 2, selfScheduleRow = 12;
        for (int band = BAND_RANGE.getMinimum(); band <= BAND_RANGE.getMaximum(); band++) {
            String expectedBandLabel = getExpectedBandLabel(band);
            boolean bandsInvalid;
            String bandLabelCellValue = getBandLabelCellValue(sheet, bandColumn, selfScheduleRow, band);
            if (band == 0) {
                bandsInvalid = !SCHEDULING_UNIT_SCHEDULE_LABELS.contains(bandLabelCellValue);
            } else {
                bandsInvalid = !bandLabelCellValue.equals("SU " + expectedBandLabel) && !bandLabelCellValue.equals("JG " + expectedBandLabel);
            }
            if (bandsInvalid) {
                log.debug("validateDers() - invalid band labels, expected: {}, actual: {}", expectedBandLabel, bandLabelCellValue);
                throw new ObjectValidationException("Cannot import because of invalid band labels", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
            }
        }
    }

    private static String getExpectedBandLabel(int band) {
        if (band > 0) {
            return "+" + band;
        } else {
            return Integer.toString(band);
        }
    }

    private static String getBandLabelCellValue(Sheet sheet, int bandColumn, int selfScheduleRow, int band) throws ObjectValidationException {
        String bandLabelCellValue;
        int bandLabelRow = selfScheduleRow - band;
        if (band >= 0) {
            bandLabelCellValue = sheet.getRow(bandLabelRow).getCell(bandColumn).getStringCellValue().strip();
        } else {
            log.debug("validateDers() - reading cell value, row: {}, col: {} for band {}", bandLabelRow, bandColumn, band);
            try {
                bandLabelCellValue = sheet.getRow(bandLabelRow).getCell(bandColumn).getStringCellValue();
            } catch (Exception ex) {
                throw new ObjectValidationException("Cannot import because of invalid band number", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
            }
        }
        return bandLabelCellValue;
    }

    /**
     * Metoda sprawdza, czy etykiety pasm w sekcji jednostki grafikowej dla oferty na moc są prawidłowe
     */
    private void validateCapacitySuOfferAndScheduleLabels(Sheet sheet, Direction direction) throws ObjectValidationException {
        String unitOffer, unitSchedule;
        try {
            if (direction.equals(UP)) {
                unitOffer = sheet.getRow(2).getCell(2).getStringCellValue();
                unitSchedule = sheet.getRow(3).getCell(2).getStringCellValue();
            } else {
                unitOffer = sheet.getRow(3).getCell(2).getStringCellValue();
                unitSchedule = sheet.getRow(2).getCell(2).getStringCellValue();
            }
        } catch (Exception ex) {
            throw new ObjectValidationException("Wrong scheduling unit offer or schedule label", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
        }
        if (!SCHEDULING_UNIT_SCHEDULE_LABELS.contains(unitSchedule) || !SCHEDULING_UNIT_OFFER_LABELS.contains(unitOffer)) {
            throw new ObjectValidationException("Wrong scheduling unit offer or schedule label", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
        }
    }

    private void validateDersForAuctionType(AuctionDayAheadDTO auction, SchedulingUnitDTO schedulingUnitDTO, Sheet sheet) throws ObjectValidationException {
        AuctionDayAheadType auctionType = auction.getType();
        if (auctionType.equals(ENERGY)) {
            int firstSelfScheduleRow = 34;
            validateDers(sheet, schedulingUnitDTO, auction, ENERGY_DER_SECTION_HEIGHT,
                BAND_RANGE, firstSelfScheduleRow, ENERGY_FIRST_DER_START_ROW);
        } else if (auctionType.equals(CAPACITY)) {
            Direction direction = auction.getProduct().getDirection();
            validateDersForCapacityProductDirection(auction, schedulingUnitDTO, sheet, direction);
        } else {
            throw new ObjectValidationException("Unsupported auction type", AUCTION_DA_OFFER_IMPORT_UNSUPPORTED_AUCTION_TYPE);
        }
    }

    private void validateDersForCapacityProductDirection(AuctionDayAheadDTO auction, SchedulingUnitDTO schedulingUnitDTO,
        Sheet sheet, Direction direction) throws ObjectValidationException {
        if (direction.equals(UP)) {
            int firstSelfScheduleRow = CAPACITY_FIRST_DER_START_ROW + UP_SELF_SCHEDULE_ROW_OFFSET;
            validateDers(sheet, schedulingUnitDTO, auction, CAPACITY_DER_SECTION_HEIGHT,
                UP_BAND_RANGE, firstSelfScheduleRow, CAPACITY_FIRST_DER_START_ROW);
        } else if (direction.equals(DOWN)) {
            int firstSelfScheduleRow = CAPACITY_FIRST_DER_START_ROW + DOWN_SELF_SCHEDULE_ROW_OFFSET;
            validateDers(sheet, schedulingUnitDTO, auction, CAPACITY_DER_SECTION_HEIGHT,
                DOWN_BAND_RANGE, firstSelfScheduleRow, CAPACITY_FIRST_DER_START_ROW);
        } else {
            throw new ObjectValidationException("Unsupported product direction", AUCTION_DA_OFFER_IMPORT_UNSUPPORTED_PRODUCT_DIRECTION);
        }
    }

    private void validateColumns(Sheet sheet) throws ObjectValidationException {
        Set<String> volumeColLabels = Set.of("V", "W");
        Set<String> priceColLabels = Set.of("P", "C");
        Row colTypeRow = sheet.getRow(0);
        Row timestampRow = sheet.getRow(1);
        for (int hour = 1; hour <= 25; hour++) {
            boolean timestampsIncorrect = isTimestampNameValid(timestampRow, hour);
            boolean valueTypeNameIncorrect;
            try {
                valueTypeNameIncorrect = !volumeColLabels.contains(colTypeRow.getCell(hour * 2 + 1).getStringCellValue()) ||
                    !priceColLabels.contains(colTypeRow.getCell(hour * 2 + 2).getStringCellValue());
            } catch (Exception ex) {
                throw new ObjectValidationException("Cannot import because of invalid columns", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
            }
            if (timestampsIncorrect || valueTypeNameIncorrect) {
                log.debug("validateColumns() - columns incorrect for hour {}, timestamps incorrect: {}, value type name incorrect: {}", hour, timestampsIncorrect, valueTypeNameIncorrect);
                throw new ObjectValidationException("Cannot import because of invalid columns", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
            }
        }
    }

    private boolean isTimestampNameValid(Row timestampRow, int hour) throws ObjectValidationException {
        if (Objects.isNull(timestampRow)) {
            throw new ObjectValidationException("Cannot import because of invalid timestamp row", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
        } else if (hour == 25) {
            return !timestampRow.getCell(51).getStringCellValue().equals(EXTRA_HOUR_CONSTANT);
        } else {
            try {
                return timestampRow.getCell(hour * 2 + 1).getNumericCellValue() != hour;
            } catch (Exception ex) {
                throw new ObjectValidationException("Cannot import because of invalid timestamp row", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
            }
        }
    }

    private void validateDers(Sheet sheet, SchedulingUnitDTO schedulingUnitDTO, AuctionDayAheadDTO auction, int derSectionHeight,
        Range<Integer> expectedBandRange, int firstSelfScheduleRow, int firstDerNameRow) throws ObjectValidationException {
        CellAddress nextDerSelfScheduleCell = new CellAddress(firstSelfScheduleRow, 2);
        CellAddress nextDerNameCell = new CellAddress(firstDerNameRow, 1);
        List<UnitMinDTO> ders = schedulingUnitDTO.getUnits();
        Set<String> expectedDerNames = ders.stream()
            .map(UnitMinDTO::getName).map(String::toLowerCase)
            .collect(Collectors.toSet());
        Map<String, Map<String, BigDecimal>> derSelfSchedules = getSelfScheduleByDerNameLowerCase(schedulingUnitDTO, auction);
        for (int derNumber = 0; derNumber < ders.size(); derNumber++) {
            validateBandLabels(sheet, expectedBandRange, nextDerSelfScheduleCell);
            String derNameCellValue = validateDerName(sheet, nextDerNameCell, expectedDerNames);
            log.debug("validateDers() - validating der {}, with self schedule on row {}", derNameCellValue, nextDerSelfScheduleCell);
            validateSelfSchedule(sheet.getRow(nextDerSelfScheduleCell.getRow()), derSelfSchedules.get(derNameCellValue));
            expectedDerNames.remove(derNameCellValue);  // zabezpieczenie przed wpisaniem w szablon tego samego dera wielokrotnie
            nextDerNameCell = new CellAddress(nextDerNameCell.getRow() + derSectionHeight, nextDerNameCell.getColumn());
            nextDerSelfScheduleCell = new CellAddress(nextDerSelfScheduleCell.getRow() + derSectionHeight, nextDerSelfScheduleCell.getColumn());
        }
        validateAllDersUsed(expectedDerNames);
    }

    private void validateAllDersUsed(Set<String> expectedDerNames) throws ObjectValidationException {
        if (!expectedDerNames.isEmpty()) {
            log.debug("validateAllDersUsed() - missing der");
            throw new ObjectValidationException("Cannot import because der is missing from template", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
        }
    }

    private String validateDerName(Sheet sheet, CellAddress nextDerNameCell, Set<String> expectedDerNames) throws ObjectValidationException {
        String derNameCellValue = sheet.getRow(nextDerNameCell.getRow()).getCell(nextDerNameCell.getColumn()).getStringCellValue().strip().toLowerCase();
        if (!expectedDerNames.contains(derNameCellValue)) {
            log.debug("validateDerName() - invalid der name");
            throw new ObjectValidationException("Cannot import because of invalid der name", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
        }
        return derNameCellValue;
    }

    private Map<String, Map<String, BigDecimal>> getSelfScheduleByDerNameLowerCase(SchedulingUnitDTO schedulingUnitDTO, AuctionDayAheadDTO auction) {
        List<Long> unitsId = schedulingUnitDTO.getUnits().stream()
            .map(UnitMinDTO::getId)
            .collect(Collectors.toUnmodifiableList());
        return unitSelfScheduleService.findVolumesForDersAndDateMap(unitsId, auction.getDeliveryDate()).entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().getName().toLowerCase(), Map.Entry::getValue));
    }

    private void validateBandLabels(Sheet sheet, Range<Integer> expectedBandRange, CellAddress nextDerSelfScheduleCell) throws ObjectValidationException {
        for (int band = expectedBandRange.getMinimum(); band <= expectedBandRange.getMaximum(); band++) {
            String expectedBandLabel;
            if (band > 0) {
                expectedBandLabel = "+" + band;
            } else {
                expectedBandLabel = Integer.toString(band);
            }
            boolean bandsInvalid;
            String bandLabelCellValue;
            int bandLabelRow = nextDerSelfScheduleCell.getRow() - band;
            Cell bandLabelCell;
            try {
                bandLabelCell = sheet.getRow(bandLabelRow).getCell(nextDerSelfScheduleCell.getColumn());
            } catch (Exception ex) {
                throw new ObjectValidationException("Invalid self schedule cell", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
            }
            if (band == 0) {
                sheet.getRow(bandLabelRow).getCell(nextDerSelfScheduleCell.getColumn()).setCellType(CellType.STRING);
                bandLabelCellValue = bandLabelCell.getStringCellValue().trim();
            } else if (band > 0) {
                DataFormatter formatter = new DataFormatter(); // Excel automatycznie dodaje ukryty znak ' przed znakiem "+", zwykłe getStringCellValue() zwraca w takim przypadku pusty string
                bandLabelCellValue = formatter.formatCellValue(bandLabelCell);
            } else {
                log.debug("validateDers() - reading cell value, row: {}, col: {} for band {}", bandLabelRow, nextDerSelfScheduleCell.getColumn(), band);
                try {
                    bandLabelCellValue = Integer.toString((int) sheet.getRow(bandLabelRow).getCell(nextDerSelfScheduleCell.getColumn()).getNumericCellValue());
                } catch (Exception ex) {
                    log.debug("validateBandLabels() - validation failed with cause: {}", ex.getMessage());
                    throw new ObjectValidationException("Cannot import because of invalid band number", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
                }
            }
            if (band == 0) {
                bandsInvalid = !SELF_SCHEDULE_LABELS.contains(bandLabelCellValue);
            } else {
                bandsInvalid = !expectedBandLabel.equals(bandLabelCellValue);
            }
            if (bandsInvalid) {
                log.debug("validateDers() - invalid band labels, expected: {}, actual: {}", expectedBandLabel, bandLabelCellValue);
                throw new ObjectValidationException("Cannot import because of invalid band labels", AUCTION_DA_IMPORT_TEMPLATE_INCORRECT);
            }
        }
    }

    private void validateSelfSchedule(Row selfScheduleRow, Map<String, BigDecimal> selfSchedule) throws ObjectValidationException {
        for (int hour = 1; hour <= 25; hour++) {
            String key = Integer.toString(hour);
            if (hour == 25) {
                key = EXTRA_HOUR_CONSTANT;
            }
            Double expectedSelfScheduleValue = 0.0;
            if(Objects.nonNull(selfSchedule)) {
                expectedSelfScheduleValue = Optional.ofNullable(selfSchedule.get(key))
                    .map(BigDecimal::doubleValue)
                    .orElse(0.0);
            }
            double providedSelfScheduleValue = selfScheduleRow.getCell(1 + hour * 2).getNumericCellValue();
            if (expectedSelfScheduleValue != providedSelfScheduleValue) {
                log.debug("validateSelfSchedule() - invalid self schedule value! Expected: {}, actual: {} on hour {}", expectedSelfScheduleValue, providedSelfScheduleValue, hour);
                throw new ObjectValidationException("Cannot import because of invalid self schedule value", AUCTION_DA_OFFER_IMPORT_INCOHERENT_SELF_SCHEDULE);
            }
        }
    }

}
