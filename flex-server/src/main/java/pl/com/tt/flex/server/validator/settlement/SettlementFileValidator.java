package pl.com.tt.flex.server.validator.settlement;

import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.XLSX;
import static pl.com.tt.flex.server.service.common.XlsxUtil.getBigDecimalValueOrNullFromCellWithUnit;
import static pl.com.tt.flex.server.service.common.XlsxUtil.getCellStringValue;
import static pl.com.tt.flex.server.util.DateUtil.getSameDayDateRangeString;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.DUPLICATE_ACTIVATION_SETTLEMENTS;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SAVING_THE_SAME_ACTIVATION_SETTLEMENTS;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SETTLEMENTS_NOTHING_CHANGED;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SETTLEMENTS_TEMPLATE_INCORRECT;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.collect.Sets;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataexport.exporter.SettlementDataExporter;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.dataimport.impl.SettlementImport;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.domain.settlement.SettlementViewEntity;
import pl.com.tt.flex.server.repository.settlement.SettlementViewRepository;
import pl.com.tt.flex.server.service.common.XlsxUtil;
import pl.com.tt.flex.server.service.settlement.dto.SettlementErrorDTO;
import pl.com.tt.flex.server.service.settlement.dto.SettlementMinDTO;
import pl.com.tt.flex.server.service.settlement.dto.SettlementViewDTO;
import pl.com.tt.flex.server.service.settlement.mapper.SettlementViewMapper;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;
import pl.com.tt.flex.server.validator.common.CommonValidatorUtil;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementFileValidator {

    private static final Set<FileExtension> SUPPORTED_FILE_EXTENSIONS = Sets.newHashSet(XLSX);
    private static final Set<Locale> SUPPORTED_LOCALE = Sets.newHashSet(Locale.forLanguageTag("en"), Locale.forLanguageTag("pl"));

    private final SettlementImport settlementImport;
    private final SettlementViewRepository settlementViewRepository;
    private final SettlementViewMapper settlementViewMapper;
    private final MessageSource messageSource;
    private final SettlementDataExporter settlementDataExporter;

    public void checkValid(MultipartFile[] multipartFiles, boolean force) throws IOException, ImportDataException {
        validFiles(multipartFiles);
        checkIfChangesMade(multipartFiles);
        List<SettlementViewDTO> importedSettlements = settlementImport.doImport(multipartFiles);
        validateSettlementsUniqueForFiles(importedSettlements);
        if(!force) {
            validateSettlementsNotAlreadyUpdated(importedSettlements);
        }
    }

    private void checkIfChangesMade(MultipartFile[] multipartFiles) throws IOException {
        boolean changesMade = false;
        for (MultipartFile file : multipartFiles) {
            if (changesMade(file)) {
                changesMade = true;
            }
        }
        if (!changesMade) {
            throw new ObjectValidationException("Nothing to import", SETTLEMENTS_NOTHING_CHANGED);
        }
    }

    private boolean changesMade(MultipartFile file) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        Map<Long, SettlementViewEntity> dbSettlementById = getSettlementUsedInSheetByIdMap(sheet);
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next(); //Pomiń wiersz z nazwami kolumn
        boolean changesMade = false;
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Optional<Long> id = getIdFromRow(row);
            if(id.isEmpty() || !dbSettlementById.containsKey(id.get())) {
                throw new ObjectValidationException("Imported id does not exist in the database", SETTLEMENTS_TEMPLATE_INCORRECT);
            }
            SettlementViewEntity settlementInRow = dbSettlementById.get(id.get());
            boolean activatedVolumeChanged = !areNullableBigDecimalsEqual(getBigDecimalValueOrNullFromCellWithUnit(row.getCell(7)), settlementInRow.getActivatedVolume());
            boolean settlementAmountChanged = !areNullableBigDecimalsEqual(getBigDecimalValueOrNullFromCellWithUnit(row.getCell(8)), settlementInRow.getSettlementAmount());
            if (activatedVolumeChanged || settlementAmountChanged) {
                changesMade = true;
            }
        }
        return changesMade;
    }

    private void validFiles(MultipartFile[] multipartFiles) throws IOException {
        SettlementErrorDTO settlementErrorDTO = new SettlementErrorDTO();
        for (MultipartFile file : multipartFiles) {
            try {
                CommonValidatorUtil.checkFileExtensionValid(file, SUPPORTED_FILE_EXTENSIONS);
                checkFileContextValid(file);
            } catch (ObjectValidationException e) {
                settlementErrorDTO.addInvalidFile(e.getMsgKey(), file.getOriginalFilename());
            }
        }
        if (!CollectionUtils.isEmpty(settlementErrorDTO.getInvalidFiles())) {
            throw new ObjectValidationException("Template incorrect", SETTLEMENTS_TEMPLATE_INCORRECT, settlementErrorToJson(settlementErrorDTO));
        }
    }

    private void checkFileContextValid(MultipartFile file) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        validColumnNames(sheet);
        validSettlementData(sheet);
    }

    private void validColumnNames(Sheet sheet) {
        Row colNameRow = sheet.getRow(0);
        for (ScreenColumnDTO col : settlementDataExporter.defaultColumnList) {
            String cellValue = colNameRow.getCell(col.getOrderNr() - 1).getStringCellValue();
            if (!getExpectedColumnNames(col).contains(cellValue)) {
                throw new ObjectValidationException("Column names incorrect", SETTLEMENTS_TEMPLATE_INCORRECT);
            }
        }
    }

    private List<String> getExpectedColumnNames(ScreenColumnDTO col) {
        String columnCode = settlementDataExporter.getPrefix() + col.getColumnName();
        return SUPPORTED_LOCALE.stream()
            .map(locale -> messageSource.getMessage(columnCode, null, locale))
            .collect(Collectors.toList());
    }

    private void validSettlementData(Sheet sheet) {
        Map<Long, SettlementViewEntity> dbSettlementById = getSettlementUsedInSheetByIdMap(sheet);
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next(); //Pomiń wiersz z nazwami kolumn
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Optional<Long> id = getIdFromRow(row);
            if(id.isEmpty() || !dbSettlementById.containsKey(id.get())) {
                throw new ObjectValidationException("Imported id does not exist in the database", SETTLEMENTS_TEMPLATE_INCORRECT);
            }
            validRowData(row, dbSettlementById.get(id.get()));
        }
    }

    private void validRowData(Row row, SettlementViewEntity settlement) {
        boolean derNameValid = Objects.equals(getCellStringValue(row.getCell(1)),settlement.getDerName());
        boolean offerIdValid = Objects.equals(getCellStringValue(row.getCell(2)),settlement.getOfferId().toString());
        boolean auctionNameValid = Objects.equals(getCellStringValue(row.getCell(3)),settlement.getAuctionName());
        boolean companyNameValid = Objects.equals(getCellStringValue(row.getCell(4)),settlement.getCompanyName());
        boolean acceptedDeliveryPeriodValid = Objects.equals(getCellStringValue(row.getCell(5)),getSameDayDateRangeString(settlement.getAcceptedDeliveryPeriodFrom(), settlement.getAcceptedDeliveryPeriodTo()));
        boolean acceptedVolumeValid = Objects.equals(getCellStringValue(row.getCell(6)),settlement.getAcceptedVolume() + " " + settlement.getUnit());
        boolean decimalPlacesValid = decimalValuesValid(row);
        boolean valuesNotNegative = validActivatedVolumeAndSettlementAmountNotNegative(row);
        if (!(derNameValid && offerIdValid && auctionNameValid && companyNameValid && acceptedDeliveryPeriodValid && acceptedVolumeValid && decimalPlacesValid && valuesNotNegative)) {
            throw new ObjectValidationException("Invalid settlement data", SETTLEMENTS_TEMPLATE_INCORRECT);
        }
    }

    private boolean validActivatedVolumeAndSettlementAmountNotNegative(Row row) {
        try {
            boolean activatedVolumeValueValid = Optional.ofNullable(row.getCell(7)).map(XlsxUtil::getBigDecimalValueOrNullFromCellWithUnit).map(volume -> volume.compareTo(BigDecimal.ZERO) >= 0).orElse(true);
            boolean settlementAmountValueValid = Optional.ofNullable(row.getCell(8)).map(XlsxUtil::getBigDecimalValueOrNullFromCellWithUnit).map(amount -> amount.compareTo(BigDecimal.ZERO) >= 0).orElse(true);
            return activatedVolumeValueValid && settlementAmountValueValid;
        } catch (NumberFormatException e) {
            throw new ObjectValidationException(e.getMessage(), SETTLEMENTS_TEMPLATE_INCORRECT);
        }
    }

    private boolean decimalValuesValid(Row row) {
        BigDecimal max = BigDecimal.valueOf(9_999_999_999L);
        try {
            boolean activatedVolumeValueValid = Optional.ofNullable(row.getCell(7)).map(XlsxUtil::getBigDecimalValueOrNullFromCellWithUnit).map(volume -> volume.scale() <= 3 && volume.compareTo(max) <= 0).orElse(true);
            boolean settlementAmountValueValid = Optional.ofNullable(row.getCell(8)).map(XlsxUtil::getBigDecimalValueOrNullFromCellWithUnit).map(amount -> amount.scale() <= 2 && amount.compareTo(max) <= 0).orElse(true);
            return activatedVolumeValueValid && settlementAmountValueValid;
        } catch (NumberFormatException e) {
            throw new ObjectValidationException(e.getMessage(), SETTLEMENTS_TEMPLATE_INCORRECT);
        }
    }


    private void validateSettlementsUniqueForFiles(List<SettlementViewDTO> importedSettlements) throws IOException, ObjectValidationException {
        List<SettlementMinDTO> nonUniqueSettlements = getNonUniqueSettlements(importedSettlements);
        if (!nonUniqueSettlements.isEmpty()) {
            log.debug("validateSettlementsUniqueForFiles() Found duplicated Settlements");
            SettlementErrorDTO settlementErrorDTO = new SettlementErrorDTO();
            settlementErrorDTO.getInvalidActivationSettlements().put(SAVING_THE_SAME_ACTIVATION_SETTLEMENTS, nonUniqueSettlements.stream().distinct().collect(Collectors.toList()));
            throw new ObjectValidationException("Importing duplicated Settlements", SETTLEMENTS_TEMPLATE_INCORRECT, settlementErrorToJson(settlementErrorDTO));
        }
    }

    private List<SettlementMinDTO> getNonUniqueSettlements(List<SettlementViewDTO> importedSettlements) {
        Set<Long> ids = new HashSet<>();
        List<SettlementMinDTO> nonUniqueSettlements = new ArrayList<>();
        for (SettlementViewDTO dto : importedSettlements) {
            Long id = dto.getId();
            if (ids.contains(id)) {
                nonUniqueSettlements.add(settlementViewMapper.toSettlementMinDTO(dto));
            } else {
                ids.add(id);
            }
        }
        return nonUniqueSettlements;
    }

    /**
     * Jeżeli przynajmniej jeden obiekt rozliczenia/aktywacji jest już zaktualizowany to rzuca bład: DUPLICATE_ACTIVATION_SETTLEMENTS
     * W odpowiedzi z bledem do params dodawany jest obiekt {@link SettlementMinDTO} ktory zawiera informacje ktore obiekty spowodowały błąd.
     */
    private void validateSettlementsNotAlreadyUpdated(List<SettlementViewDTO> importedSettlements) throws IOException, ObjectValidationException {
        List<SettlementMinDTO> alreadyUpdatedSettlements = findAlreadyUpdatedSettlements(importedSettlements);
        if (!CollectionUtils.isEmpty(alreadyUpdatedSettlements)) {
            log.debug("throwExceptionIfExistSettlement() Found {} settlements already updated", alreadyUpdatedSettlements.size());
            SettlementErrorDTO settlementErrorDTO = new SettlementErrorDTO();
            settlementErrorDTO.getInvalidActivationSettlements().put(DUPLICATE_ACTIVATION_SETTLEMENTS, alreadyUpdatedSettlements.stream().distinct().collect(Collectors.toList()));
            throw new ObjectValidationException("Settlement already exists", SETTLEMENTS_TEMPLATE_INCORRECT, settlementErrorToJson(settlementErrorDTO));
        }
    }

    private List<SettlementMinDTO> findAlreadyUpdatedSettlements(List<SettlementViewDTO> settlementsFromFiles) {
        List<Long> importedSettlementIds = settlementsFromFiles.stream().map(SettlementViewDTO::getId).collect(Collectors.toList());
        Map<Long, SettlementViewEntity> dbSettlements = settlementViewRepository.findAllByIdIn(importedSettlementIds).stream()
            .collect(Collectors.toMap(SettlementViewEntity::getId, Function.identity()));
        List<SettlementMinDTO> alreadyUpdatedSettlements = new ArrayList<>();
        for (SettlementViewDTO importedSettlement : settlementsFromFiles) {
            SettlementViewEntity dbSettlement = dbSettlements.get(importedSettlement.getId());
            BigDecimal dbActivatedVolume = dbSettlement.getActivatedVolume();
            BigDecimal dbSettlementAmount = dbSettlement.getSettlementAmount();
            boolean overwritingAlreadyUpdatedActivatedVolume = Objects.nonNull(dbActivatedVolume) && !areNullableBigDecimalsEqual(dbActivatedVolume, importedSettlement.getActivatedVolume());
            boolean overwritingAlreadyUpdatedSettlementAmount = Objects.nonNull(dbSettlementAmount) && !areNullableBigDecimalsEqual(dbSettlementAmount, importedSettlement.getSettlementAmount());
            if(overwritingAlreadyUpdatedActivatedVolume || overwritingAlreadyUpdatedSettlementAmount) {
                alreadyUpdatedSettlements.add(settlementViewMapper.toSettlementMinDTO(importedSettlement));
            }
        }
        return alreadyUpdatedSettlements;
    }

    private String settlementErrorToJson(SettlementErrorDTO settlementErrorDTO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper.writeValueAsString(settlementErrorDTO);
    }

    private Optional<Long> getIdFromRow(Row row) {
        return Optional.ofNullable(row.getCell(0))
            .map(XlsxUtil::getCellStringValue)
            .map(idString -> StringUtils.isNumeric(idString) ? (long) Double.parseDouble(idString) : null);
    }

    private Map<Long, SettlementViewEntity> getSettlementUsedInSheetByIdMap(Sheet sheet) {
        Set<Long> importedIds = getImportedSettlementIds(sheet);
        return settlementViewRepository.findAllByIdIn(importedIds).stream()
            .collect(Collectors.toMap(SettlementViewEntity::getId, Function.identity()));
    }

    private Set<Long> getImportedSettlementIds(Sheet sheet) {
        Set<Long> ids = new HashSet<>();
        Iterator<Row> rowIterator = sheet.rowIterator();
        rowIterator.next(); //Pomiń wiersz z nazwami kolumn
        while (rowIterator.hasNext()) {
            getIdFromRow(rowIterator.next()).ifPresent(ids::add);
        }
        return ids;
    }

    private boolean areNullableBigDecimalsEqual(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) {
            return true;
        } else if (a == null || b == null) {
            return false;
        } else {
            return a.compareTo(b) == 0;
        }
    }

}
