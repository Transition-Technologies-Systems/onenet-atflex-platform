package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.capacity;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.FileGeneratorAbstract;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoCouplingPointDTO;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoHourNumberDTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class AgnoPbcmFileGeneratorImpl extends FileGeneratorAbstract implements AgnoPbcmFileGenerator {

    private static final String TEMPLATE_FILE_PATH = "templates/xlsx/agno_file_template/agno_pbcm_template.xlsx";
    private static final String FILENAME_FORMAT = "input_pbcm_%s_%s_%s"; // input_pbcm_{coupling_point}_{delivery_date}_{hour_nr}

    private static final int START_ROW = 1;
    // skoroszyt offers
    private static final String OFFERS_SHEET = "offers";
    private static final int OFFER_SHEET_UNIT_NAME = 0;
    private static final int OFFER_SHEET_TYPE = 1; // krotka nazwa produktu
    private static final int OFFER_SHEET_PRODUCT_TYPE = 2;
    private static final int OFFER_SHEET_POWER = 3;
    private static final int OFFER_SHEET_PRICE = 4;
    // skoroszyt forecast_prices
    private static final String FORECASTED_PRICES_SHEET_NAME = "forecast_prices";
    private static final int FORECASTED_PRICES_SHEET_TYPE = 0;
    private static final int FORECASTED_PRICES_SHEET_PRODUCT_TYPE = 1;
    private static final int FORECASTED_PRICES_SHEET_FORECASTED_PRICE = 2;

    @Override
    public FileDTO getPbcmFile(AgnoCouplingPointDTO couplingPoint, AgnoHourNumberDTO hourNumber, LocalDate deliveryDate) throws IOException {
        log.debug("getPbcmFile() START - generate PBCM file: couplingPointId: {}, deliveryDate{}, hourNumber: {}",
            couplingPoint.getCouplingPointId().getName(), deliveryDate, hourNumber.getHourNumber());
        XSSFWorkbook workbook = getWorkbook(TEMPLATE_FILE_PATH);
        fillOffersSheet(hourNumber, workbook);
        fillForecastedPricesSheet(hourNumber, workbook);
        fillUnitsSheet(hourNumber, workbook);
        FileDTO pbcmFile = getFileDtoFromWorkbook(couplingPoint, hourNumber, deliveryDate, workbook);
        log.debug("getPbcmFile() END - generate BM file: couplingPointId: {}, deliveryDate{}, hourNumber: {}. RESULT FILENAME: {}",
            couplingPoint.getCouplingPointId().getName(), deliveryDate, hourNumber.getHourNumber(), pbcmFile.getFileName());
        return pbcmFile;
    }

    private FileDTO getFileDtoFromWorkbook(AgnoCouplingPointDTO couplingPoint, AgnoHourNumberDTO hourNumber, LocalDate deliveryDate, XSSFWorkbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();
        String extension = ".xlsx";
        String filename = String.format(FILENAME_FORMAT, couplingPoint.getCouplingPointId().getName(), deliveryDate, hourNumber.getHourNumber()) + extension;
        return new FileDTO(filename, outputStream.toByteArray());
    }

    private void fillForecastedPricesSheet(AgnoHourNumberDTO hourNumber, XSSFWorkbook workbook) {
        log.debug("fillForecastedPricesSheet() Start - fill forecastedPrices sheet");
        XSSFSheet offersSheet = workbook.getSheet(FORECASTED_PRICES_SHEET_NAME);
        AtomicInteger startRowNumb = new AtomicInteger(START_ROW);
        hourNumber.getProductList().forEach(product -> {
            XSSFRow row = offersSheet.createRow(startRowNumb.getAndIncrement());
            XSSFCellStyle cellStyle = getCellStyle(workbook);
            createCellWithStyle(row, FORECASTED_PRICES_SHEET_TYPE, cellStyle).setCellValue(product.getProductName());
            createCellWithStyle(row, FORECASTED_PRICES_SHEET_PRODUCT_TYPE, cellStyle).setCellValue(product.getProductDirection().name().toLowerCase());
            createCellWithStyle(row, FORECASTED_PRICES_SHEET_FORECASTED_PRICE, cellStyle).setCellValue(product.getForecastedPrice().toString());
            log.debug("fillForecastedPricesSheet() Add product detail to forecastedPrices sheet: productID: {}, productName: {}",
                product.getId(), product.getProductName());
        });
        log.debug("fillForecastedPricesSheet() End - fill forecastedPrices sheet");
    }

    protected void fillOffersSheet(AgnoHourNumberDTO hourNumber, XSSFWorkbook workbook) {
        log.debug("fillOffersSheet() Start - fill offers sheet");
        XSSFSheet offersSheet = workbook.getSheet(OFFERS_SHEET);
        AtomicInteger startRowNumb = new AtomicInteger(START_ROW);
        hourNumber.getOfferDetails().forEach(details -> {
            XSSFRow row = offersSheet.createRow(startRowNumb.getAndIncrement());
            XSSFCellStyle cellStyle = getCellStyle(workbook);
            createCellWithStyle(row, OFFER_SHEET_UNIT_NAME, cellStyle).setCellValue(details.getDerName());
            createCellWithStyle(row, OFFER_SHEET_TYPE, cellStyle).setCellValue(details.getProductShortName());
            createCellWithStyle(row, OFFER_SHEET_PRODUCT_TYPE, cellStyle).setCellValue(details.getProductDirection().name().toLowerCase());
            createCellWithStyle(row, OFFER_SHEET_POWER, cellStyle).setCellValue(convertKWtoMW(details.getBandVolume()).toString());
            createCellWithStyle(row, OFFER_SHEET_PRICE, cellStyle).setCellValue(details.getBandPrice().toString());
            log.debug("fillOffersSheet() Add offer to sheet: unitName: {}, type: {}", details.getDerName(), details.getProductShortName());
        });
        log.debug("fillOffersSheet() End - fill offers sheet");
    }
}
