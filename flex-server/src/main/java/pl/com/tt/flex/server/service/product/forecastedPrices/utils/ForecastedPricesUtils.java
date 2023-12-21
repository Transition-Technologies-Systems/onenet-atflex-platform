package pl.com.tt.flex.server.service.product.forecastedPrices.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesErrorDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesMinDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.DateUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.util.Locale.forLanguageTag;
import static pl.com.tt.flex.server.service.product.forecastedPrices.utils.ForecastedPricesCellLocalization.*;
import static pl.com.tt.flex.server.validator.forecastedPrices.ForecastedPricesFileValidator.getBigDecimalFromCell;
import static pl.com.tt.flex.server.validator.forecastedPrices.ForecastedPricesFileValidator.hasNextSection;

@Component
@Slf4j
@AllArgsConstructor
public class ForecastedPricesUtils {

    private final UserService userService;
    private final ProductService productService;
    private final MessageSource messageSource;

    /**
     * Pobieranie prognozowanych cen plik po pliku
     */
    public List<ForecastedPricesDTO> getForecastedPricesFromFiles(MultipartFile[] files) throws IOException {
        List<ForecastedPricesDTO> forecastedPrices = new ArrayList<>();
        for (MultipartFile file : files) {
            forecastedPrices.addAll(getForecastedPricesFromFile(file));
        }
        return forecastedPrices;
    }

    private List<ForecastedPricesDTO> getForecastedPricesFromFile(MultipartFile file) throws IOException {
        List<ForecastedPricesDTO> forecastedPrices = new ArrayList<>();
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);
        AtomicInteger atomicInteger = new AtomicInteger(0);
        while (hasNextSection(sheet, atomicInteger.get())) {
            int forecastedPriceSection = atomicInteger.get();
            ProductMinDTO productMinDTO = getProductFromSheet(sheet, forecastedPriceSection);
            Instant date = getDateFromSheet(sheet, forecastedPriceSection);
            List<MinimalDTO<String, BigDecimal>> listOfHoursPowerFromSheet = getListOfHoursPriceFromSheet(sheet, forecastedPriceSection);
            ForecastedPricesDTO forecastedPricesDTO = new ForecastedPricesDTO();
            forecastedPricesDTO.setForecastedPricesDate(date);
            forecastedPricesDTO.setProduct(productMinDTO);
            forecastedPricesDTO.setPrices(listOfHoursPowerFromSheet);
            forecastedPrices.add(forecastedPricesDTO);
            atomicInteger.addAndGet(SECTION_SIZE.getRowNumber());
        }

        return forecastedPrices;
    }

    public Instant getDateFromSheet(Sheet sheet, int forecastedPriceSection) {
        int rowNumber = FORECASTED_PRICES_DATE.getRowNumber() + forecastedPriceSection;
        Cell dateCell = sheet
            .getRow(rowNumber)
            .getCell(FORECASTED_PRICES_DATE.getColNumber()); // komorka B1
        if (dateCell.getCellTypeEnum() != CellType.NUMERIC) {
            try {
                Date date = DateUtil.getDateFromString(dateCell.getStringCellValue());
                return date.toInstant();
            } catch (Exception e) {
                log.debug("isValidDate() Date {} in cell is incorrect", dateCell.getStringCellValue());
                return null;
            }
        }
        return dateCell.getDateCellValue().toInstant();
    }

    public ProductMinDTO getProductFromSheet(Sheet sheet, int forecastedPriceSection) {
        int rowNumber = PRODUCT_NAME.getRowNumber() + forecastedPriceSection;
        Cell cell = sheet
            .getRow(rowNumber)
            .getCell(PRODUCT_NAME.getColNumber());
        String productName = cell.getCellTypeEnum() == CellType.STRING ? cell.getStringCellValue() : String.valueOf((int) cell.getNumericCellValue()); // komorka B2
        Optional<Long> productIdOpt = Optional.ofNullable(productService.findByFullNameOrShortName(productName));
        if (productIdOpt.isPresent()) {
            ProductMinDTO productMinDTO = new ProductMinDTO();
            productMinDTO.setId(productIdOpt.get());
            productMinDTO.setFullName(productName);
            productMinDTO.setShortName(productName);
            return productMinDTO;
        } else {
            throw new RuntimeException("Cannot find product with name: " + productName);
        }
    }

    // zwraca sciezke do szablonu w jezyku jaki uzywa uzytkownik
    public String getTemplatePath() {
        String userLang = userService.getLangKeyForCurrentLoggedUser();
        String templatePathFormat = "templates/xlsx/import_forecasted_prices_%s.xlsx";
        return String.format(templatePathFormat, userLang);
    }

    // uzupelnienie w szablonie komorke z Data prognozowanej ceny (aktualny dzien + 1)
    public ByteArrayOutputStream fillSelfScheduleDataInTemplate(String templatePath) throws IOException {
        Resource templateFileResource = new ClassPathResource(templatePath);
        InputStream template = templateFileResource.getInputStream();
        XSSFWorkbook workbook = new XSSFWorkbook(template);
        XSSFSheet sheet = workbook.getSheetAt(0);
        Instant instantToSet = Instant.now().plus(1, DAYS);
        Date dateToSet = DateUtil.trunc(Date.from(instantToSet), DAYS);
        sheet.getRow(FORECASTED_PRICES_DATE.getRowNumber())
            .getCell(FORECASTED_PRICES_DATE.getColNumber())
            .setCellValue(dateToSet);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        return outputStream;
    }

    // pobieranie z zaimportowanego pliku prognozowane ceny na dana godzine dnia
    @SneakyThrows
    public List<MinimalDTO<String, BigDecimal>> getListOfHoursPriceFromSheet(Sheet sheet, int forecastedPriceSection) {
        Row hoursRow = sheet.getRow(DAY_HOURS_FIRST_CELL.getRowNumber() + forecastedPriceSection);
        Row pricesRow = sheet.getRow(PRICE_FIRST_CELL.getRowNumber() + forecastedPriceSection);
        LinkedList<MinimalDTO<String, BigDecimal>> hoursList = new LinkedList<>();
        Instant date = getDateFromSheet(sheet, forecastedPriceSection);
        int calculatesHour = DateUtil.calculate(date.atZone(ZoneId.systemDefault()).toLocalDate());
        for (int i = 1; i <= calculatesHour; i++) {
            Cell pricesCell = pricesRow.getCell(i);
            if (DateUtil.calculate(date.atZone(ZoneId.systemDefault()).toLocalDate()) == 23 && Objects.isNull(pricesCell)) {
                continue;
            }
            BigDecimal price = getBigDecimalFromCell(pricesRow.getCell(i));
            Cell cell = hoursRow.getCell(i);
            String hours = cell.getCellTypeEnum().equals(CellType.STRING) ? cell.getStringCellValue() : String.valueOf((int) cell.getNumericCellValue());
            hoursList.add(new MinimalDTO<>(hours, price));
        }
        return hoursList;
    }

    public String getTemplateFilename() {
        Locale locale = forLanguageTag(userService.getCurrentUser().getLangKey());
        return messageSource.getMessage("template.forecastedPrices.filename", null, locale);
    }

    public ForecastedPricesMinDTO toMinDTO(ForecastedPricesDTO forecastedPricesDTO) {
        ForecastedPricesMinDTO forecastedPricesMinDTO = new ForecastedPricesMinDTO();
        forecastedPricesMinDTO.setProductName(forecastedPricesDTO.getProduct().getFullName());
        forecastedPricesMinDTO.setForecastedPricesDate(forecastedPricesDTO.getForecastedPricesDate());
        return forecastedPricesMinDTO;
    }

    public String forecastedPricesErrorToJson(ForecastedPricesErrorDTO forecastedPricesErrorDTO) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return objectMapper.writeValueAsString(forecastedPricesErrorDTO);
    }
}
