package pl.com.tt.flex.server.validator.forecastedPrices;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataexport.util.CellUtils;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.service.common.dto.FileDTOUtil;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.forecastedPrices.ForecastedPricesService;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesErrorDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesMinDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.utils.ForecastedPricesUtils;
import pl.com.tt.flex.server.util.DateUtil;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.common.CommonValidatorUtil;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.zone.ZoneRules;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent.FORECASTED_PRICES_CREATED_ERROR;
import static pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent.FORECASTED_PRICES_DELETED_ERROR;
import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.XLSX;
import static pl.com.tt.flex.server.service.product.forecastedPrices.utils.ForecastedPricesCellLocalization.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;
import static pl.com.tt.flex.server.web.rest.product.forecastedPrices.ForecastedPricesResourceAdmin.ENTITY_NAME;


@Slf4j
@Component
public class ForecastedPricesFileValidator {

    private static final Set<FileExtension> SUPPORTED_FILE_EXTENSIONS = Sets.newHashSet(XLSX);
    private static final String TEMPLATE_INCORRECT_MSG = "Template incorrect";
    private static final String LANG_PL = "pl";
    private static final String LANG_EN = "en";

    private final ProductService productService;
    private final ForecastedPricesService forecastedPricesService;
    private final ForecastedPricesUtils forecastedPricesUtils;

    public ForecastedPricesFileValidator(ProductService productService, ForecastedPricesService forecastedPricesService,
        ForecastedPricesUtils forecastedPricesUtils) {
        this.productService = productService;
        this.forecastedPricesService = forecastedPricesService;
        this.forecastedPricesUtils = forecastedPricesUtils;
    }

    public void checkValid(MultipartFile[] multipartFiles) throws ObjectValidationException, IOException {
        validFiles(multipartFiles);
        checkIfSavingSameForecastedPrices(multipartFiles);
    }

    public void checkValid(MultipartFile multipartFile) throws ObjectValidationException, IOException {
        checkFileExtensionValid(multipartFile);
        checkFileContextValid(multipartFile);
    }

    public void checkDeletable(long id) throws ObjectValidationException {
        ForecastedPricesDTO forecastedPricesDTO = forecastedPricesService.findById(id)
            .orElseThrow(() -> new RuntimeException("Cannot find ForecastedPrices with id: " + id));
        if (forecastedPricesDTO.getForecastedPricesDate().compareTo(InstantUtil.now()) <= 0) {
            throw new ObjectValidationException("Cannot delete because the forecast day has already started or ended",
                CANNOT_DELETE_BECAUSE_FORECASTED_DAY_HAS_ALREADY_STARTED_OR_ENDED, ENTITY_NAME, FORECASTED_PRICES_DELETED_ERROR, id);
        }
    }

    public void checkIfSavingSameForecastedPrices(MultipartFile[] multipartFiles) throws ObjectValidationException, IOException {
        List<ForecastedPricesDTO> forecastedPricesFromFiles = forecastedPricesUtils.getForecastedPricesFromFiles(multipartFiles);
        List<ForecastedPricesMinDTO> minForecastedPrices = forecastedPricesFromFiles.stream().map(forecastedPricesUtils::toMinDTO).collect(Collectors.toList());
        Set<ForecastedPricesMinDTO> notUniqueForecastedPrices = getNonUniqueForecastedPrices(minForecastedPrices);
        if (!CollectionUtils.isEmpty(notUniqueForecastedPrices)) {
            log.debug("checkIfSavingSameForecastedPrices() Found {} saving the same forecasted prices", notUniqueForecastedPrices.size());
            ForecastedPricesErrorDTO forecastedPricesErrorDTO = new ForecastedPricesErrorDTO();
            forecastedPricesErrorDTO.getInvalidForecastedPrices().put(SAVING_THE_SAME_FORECASTED_PRICES, new ArrayList<>(notUniqueForecastedPrices));
            throw new ObjectValidationException("Saving the same Forecasted prices", SELF_SCHEDULE_TEMPLATE_INCORRECT, forecastedPricesUtils.forecastedPricesErrorToJson(forecastedPricesErrorDTO));
        }
    }

    private Set<ForecastedPricesMinDTO> getNonUniqueForecastedPrices(List<ForecastedPricesMinDTO> minForecastedPricesList) {
        Set<ForecastedPricesMinDTO> notUniqueForecastedPrices = new HashSet<>();
        Set<ForecastedPricesMinDTO> uniqueForecastedPrices = new HashSet<>();
        for (ForecastedPricesMinDTO forecastedPricesMinDTO : minForecastedPricesList) {
            boolean isNotUnique = uniqueForecastedPrices.stream().anyMatch(forecastedPricesMin -> forecastedPricesMin.getForecastedPricesDate().equals(forecastedPricesMinDTO.getForecastedPricesDate()) && forecastedPricesMin.getProductName().equals(forecastedPricesMinDTO.getProductName()));
            if (isNotUnique) {
                notUniqueForecastedPrices.add(forecastedPricesMinDTO);
            } else {
                uniqueForecastedPrices.add(forecastedPricesMinDTO);
            }
        }
        return notUniqueForecastedPrices;
    }

    private void validFiles(MultipartFile[] multipartFiles) throws IOException, ObjectValidationException {
        ForecastedPricesErrorDTO forecastedPricesErrorDTO = new ForecastedPricesErrorDTO();
        for (MultipartFile multipartFile : multipartFiles) {
            try {
                CommonValidatorUtil.checkFileExtensionValid(multipartFile, SUPPORTED_FILE_EXTENSIONS);
                checkFileContextValid(multipartFile);
            } catch (ObjectValidationException e) {
                forecastedPricesErrorDTO.addInvalidFilename(e.getMsgKey(), multipartFile.getOriginalFilename());
            }
        }
        if (!CollectionUtils.isEmpty(forecastedPricesErrorDTO.getInvalidFiles())) {
            ObjectMapper objectMapper = new ObjectMapper();
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                objectMapper.writeValueAsString(forecastedPricesErrorDTO));
        }
    }

    private void checkFileExtensionValid(MultipartFile multipartFile) throws ObjectValidationException {
        ObjectValidationException exception = new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
            ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
        try {
            FileExtension fileExtension = FileDTOUtil.getFileExtension(multipartFile.getOriginalFilename());
            if (!SUPPORTED_FILE_EXTENSIONS.contains(fileExtension)) {
                log.error("checkFileExtensionValid() - Incorrect file extension");
                throw exception;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw exception;
        }
    }

    private void checkFileContextValid(MultipartFile multipartFile) throws ObjectValidationException, IOException {
        XSSFWorkbook workbook = new XSSFWorkbook(multipartFile.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        validTemplate(sheet);
        AtomicInteger sectionNumber = new AtomicInteger(0);
        while (hasNextSection(sheet, sectionNumber.get())) {
            validateDate(sheet, sectionNumber.get());
            validateProduct(sheet, sectionNumber.get());
            validPrice(sheet, sectionNumber.get());
            sectionNumber.addAndGet(SECTION_SIZE.getRowNumber());
        }
    }

    public static boolean hasNextSection(Sheet sheet, int sectionNumber) {
        return Objects.nonNull(sheet.getRow(sectionNumber));
    }

    private void validateProduct(Sheet sheet, int sectionNum) throws ObjectValidationException {
        int productRowNumber = PRODUCT_NAME.getRowNumber() + sectionNum;
        Cell productCell = sheet.getRow(productRowNumber).getCell(PRODUCT_NAME.getColNumber());
        if (Objects.isNull(productCell)) {
            log.debug("validateProduct() - Cell with Product is empty");
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
        }
        String productName = productCell.getCellTypeEnum() == CellType.STRING ? productCell.getStringCellValue() : String.valueOf((int) productCell.getNumericCellValue());
        Long productId = productService.findByFullName(productName);
        if (Objects.isNull(productId)) {
            log.error("validateProduct() - Cannot find Product by name: {}", productName);
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
        }
    }

    //sprawdzanie ilości godzin względem doby - jeśli doba ma 25h to na końcu ma być dodatkowo godzina 2a
    private void validateDate(Sheet sheet, int sectionNumber) throws ObjectValidationException {
        if (!isValidDate(sheet, sectionNumber)) {
            log.debug("validateDate() - Forecasted prices date is incorrect");
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
        }

        LocalDate date = getDateFromSheet(sheet, sectionNumber);
        //Użytkownik nie powinien móc zaimportować prognozowanej ceny, jeżeli dotyczy on aktualnego dnia lub dni poprzednich
        if (date.compareTo(LocalDate.now()) <= 0) {
            log.debug("validateDate() - forecasted prices cannot be related to the current and the past day");
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, NOT_ALLOWED_TO_IMPORT_FORECASTED_PRICES_WITH_PAST_OR_CURRENT_DATE,
                ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
        }
    }

    //sprawdzanie czy cena została wprowadzona dla każdej godziny
    private void validPrice(Sheet sheet, int sectionNumber) throws ObjectValidationException {
        Row valuesRow = sheet.getRow(PRICE_FIRST_CELL.getRowNumber() + sectionNumber);
        LocalDate date = getDateFromSheet(sheet, sectionNumber);
        for (int i = 1; i <= DateUtil.calculate(date); i++) {
            Cell cell = valuesRow.getCell(i);
            // gdy jest przejście czasu z zimowego na letni to pomijana jest komórka z timestampem 3 (sprawdzana będzie później)
            if (Objects.isNull(cell) && ((SPRING_FORWARD_PRICE.getRowNumber() + sectionNumber) == valuesRow.getRowNum() && SPRING_FORWARD_PRICE.getColNumber() == i)
                && isWinterToDaylightSavingTimeTransition(date)) {
                continue;
            } else if (Objects.isNull(cell) && !isWinterToDaylightSavingTimeTransition(date)) {
                log.debug("validPrice() - not all values have been completed. Problem with cell: [row={},col={}]", valuesRow.getRowNum(), i);
                throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                    ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
            }
            if (!isCellOfNumericType(cell) || BigDecimal.valueOf(cell.getNumericCellValue()).scale() > 2 || getUnscaledValueDigits(BigDecimal.valueOf(cell.getNumericCellValue())) > 10) {
                log.debug("validPrice() - not all values are valid. Problem with cell: [row={},col={}]", valuesRow.getRowNum(), i);
                throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                    ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
            } else if (BigDecimal.valueOf(cell.getNumericCellValue()).compareTo(BigDecimal.ZERO) < 0) {
                log.debug("validPrice() - price cannot be negative. Problem with cell: [row={},col={}]", valuesRow.getRowNum(), i);
                throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                    ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
            }
        }
        if (isWinterToDaylightSavingTimeTransition(date)) {
            validSpringForwardHour(sheet, sectionNumber);
        }
        validExtraHour(sheet, date, sectionNumber);
    }

    private int getUnscaledValueDigits(BigDecimal bigDecimal) {
        return bigDecimal.signum() == 0 ? 1 : bigDecimal.precision() - bigDecimal.scale();
    }

    private boolean isWinterToDaylightSavingTimeTransition(LocalDate date) {
        ZoneRules rules = ZoneId.systemDefault().getRules();
        int year = date.getYear();
        return DateUtil.getWinterToSummerTimeTransitionDay(rules, year).equals(date);
    }

    // sprawdzenie czy dodatkowa godzina (2a) jest wymagana
    private void validExtraHour(Sheet sheet, LocalDate date, int sectionNumber) throws ObjectValidationException {
        int rowNumber = EXTRA_HOUR_PRICE.getRowNumber() + sectionNumber;
        int colNumber = EXTRA_HOUR_PRICE.getColNumber();
        Cell cell = sheet.getRow(rowNumber).getCell(colNumber);
        if (DateUtil.calculate(date) == 24 && Objects.nonNull(cell) && cell.getCellTypeEnum() != CellType.BLANK) {
            log.debug("validPrice() - is not required to add extra hour. Problem with cell: [row={},col={}]", EXTRA_HOUR_PRICE.getRowNumber(), EXTRA_HOUR_PRICE.getColNumber());
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
        }
    }

    // sprawdzenie czy timestamp nr 3 (godz. 2:00-2:59) jest pusta w przypadku zmiany czasu z zimowego na letni
    private void validSpringForwardHour(Sheet sheet, int sectionNumber) throws ObjectValidationException {
        int rowNumber = SPRING_FORWARD_PRICE.getRowNumber() + sectionNumber;
        int colNumber = SPRING_FORWARD_PRICE.getColNumber();
        Cell cell = sheet.getRow(rowNumber).getCell(colNumber);
        if (Objects.nonNull(cell) && cell.getCellTypeEnum() != CellType.BLANK) {
            log.debug("validSpringForwardHour() - timestamp at cell: [row={},col={}] is unnecessary", rowNumber, colNumber);
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
        }
    }

    //sprawdzanie pierwszej kolumny excela
    private void validTemplate(Sheet sheet) throws ObjectValidationException, IOException {
        Sheet sheetExpectedPl = getExpectedSheet(LANG_PL);
        Sheet sheetExpectedEn = getExpectedSheet(LANG_EN);
        boolean importFileCompatibleWithTemplateEn = validFirstColumn(sheet, sheetExpectedEn) && validHours(sheet, sheetExpectedEn);
        boolean importFileCompatibleWithTemplatePl = validFirstColumn(sheet, sheetExpectedPl) && validHours(sheet, sheetExpectedPl);
        if (!(importFileCompatibleWithTemplateEn || importFileCompatibleWithTemplatePl)) {
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
        }
    }

    // walidacja pierwsze kolumny
    private boolean validFirstColumn(Sheet sheet, Sheet sheetExpected) {
        int maxHeaderExpectedRow = SECTION_SIZE.getRowNumber() - 1;
        int expectedHeaderRow = 0;
        for (int i = 0; i < sheet.getLastRowNum(); i++) {
            Cell cell = sheet.getRow(i).getCell(0);
            Cell cellExpected = sheetExpected.getRow(expectedHeaderRow).getCell(0);
            if (!(isNumberCellValid(cell, cellExpected) || isStringCellValid(cell, cellExpected))) {
                log.debug("validTemplate() - Incorrect template (headers are incorrect). Problem with cell: [row={},col={}]", i, 0);
                return false;
            }
            if (expectedHeaderRow >= maxHeaderExpectedRow) {
                expectedHeaderRow = 0;
            } else {
                expectedHeaderRow += 1;
            }
        }
        return true;
    }

    // walidacja sprawdzajaca poprawnosc godzin
    private boolean validHours(Sheet sheet, Sheet sheetExpected) {
        AtomicInteger atomicInteger = new AtomicInteger();
        while (Objects.nonNull(sheet.getRow(DAY_HOURS_FIRST_CELL.getRowNumber() + atomicInteger.get()))) {
            int rowNumber = DAY_HOURS_FIRST_CELL.getRowNumber() + atomicInteger.get();
            Row hoursRow = sheet.getRow(rowNumber);
            Row hoursRowExpected = sheetExpected.getRow(DAY_HOURS_FIRST_CELL.getRowNumber());
            LocalDate date = getDateFromSheet(sheet, hoursRow.getRowNum());
            for (int i = 1; i <= DateUtil.calculate(date); i++) {
                Cell cell = hoursRow.getCell(i);
                Cell cellExpected = hoursRowExpected.getCell(i);
                if (!(isNumberCellValid(cell, cellExpected) || isStringCellValid(cell, cellExpected))) {
                    log.debug("validHours() - hours in sheet are incorrect. Problem with cell: [row={},col={}]", hoursRow.getRowNum(), i);
                    return false;
                }
            }
            atomicInteger.addAndGet(SECTION_SIZE.getRowNumber());
        }
        return true;
    }

    private Sheet getExpectedSheet(String langKey) throws IOException {
        String extension = ".xlsx";
        String template = "templates/xlsx/import_forecasted_prices_" + langKey + extension;
        Resource templateFileResource = new ClassPathResource(template);
        InputStream fileWithHeaders = templateFileResource.getInputStream();
        XSSFWorkbook workbookExpected = new XSSFWorkbook(fileWithHeaders);
        return workbookExpected.getSheetAt(0);
    }

    private boolean isValidDate(Sheet sheet, int sectionNumber) {
        int rowNumber = FORECASTED_PRICES_DATE.getRowNumber() + sectionNumber;
        int colNumber = FORECASTED_PRICES_DATE.getColNumber();
        Cell dateCell = sheet.getRow(rowNumber).getCell(colNumber);
        try {
            if (dateCell.getCellTypeEnum() != CellType.NUMERIC) {
                Date date = DateUtil.getDateFromString(dateCell.getStringCellValue());
                date.toInstant();
            } else {
                dateCell.getDateCellValue().toInstant();
            }
        } catch (Exception e) {
            log.debug("isValidDate() Date {} in cell [row={},col={}] is incorrect", dateCell.getStringCellValue(), rowNumber, colNumber);
            return false;
        }
        return true;
    }

    private LocalDate getDateFromSheet(Sheet sheet, int sectionNumber) {
        int rowNumber = FORECASTED_PRICES_DATE.getRowNumber() + sectionNumber;
        Cell dateCell = sheet.getRow(rowNumber).getCell(FORECASTED_PRICES_DATE.getColNumber());
        if (dateCell.getCellTypeEnum() != CellType.NUMERIC) {
            try {
                return CellUtils.getDateFromCell(dateCell).atZone(ZoneId.systemDefault()).toLocalDate();
            } catch (Exception e) {
                log.debug("isValidDate() Date {} in cell is incorrect", dateCell.getStringCellValue());
                return null;
            }
        } else {
            return dateCell.getDateCellValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        }
    }

    private boolean isNumberCellValid(Cell cell, Cell cellExpected) {
        return Objects.nonNull(cell) && cell.getCellTypeEnum() == CellType.NUMERIC && cellExpected.getCellTypeEnum() == CellType.NUMERIC &&
            cell.getNumericCellValue() == cellExpected.getNumericCellValue();
    }

    private boolean isStringCellValid(Cell cell, Cell cellExpected) {
        return Objects.nonNull(cell) && cell.getCellTypeEnum() == CellType.STRING && cellExpected.getCellTypeEnum() == CellType.STRING &&
            cell.getStringCellValue().equals(cellExpected.getStringCellValue());
    }

    private static boolean isCellOfNumericType(Cell cell) {
        return cell != null && cell.getCellTypeEnum() != CellType.BLANK && cell.getCellTypeEnum() == CellType.NUMERIC;
    }

    private static void throwExceptionIfCellIsBlank(Cell cell) throws ObjectValidationException {
        if (Objects.isNull(cell) || cell.getCellTypeEnum() == CellType.BLANK) {
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
        }
    }

    public static BigDecimal getBigDecimalFromCell(Cell cell) throws ObjectValidationException {
        throwExceptionIfCellIsBlank(cell);
        if (isCellOfNumericType(cell)) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        return getBigDecimalFromStringCell(cell);
    }

    private static BigDecimal getBigDecimalFromStringCell(Cell cell) throws ObjectValidationException {
        String stringCellValue = cell.getStringCellValue();
        try {
            return BigDecimal.valueOf(Double.parseDouble(stringCellValue));
        } catch (Exception e) {
            log.info("getBigDecimalFromCell() Invalid BigDecimal value -> {}. CellAddress: {}", stringCellValue, cell.getAddress().toString());
            throw new ObjectValidationException(TEMPLATE_INCORRECT_MSG, FORECASTED_PRICES_TEMPLATE_INCORRECT,
                ENTITY_NAME, FORECASTED_PRICES_CREATED_ERROR, null);
        }
    }
}
