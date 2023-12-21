package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.dto.AgnoHourNumberDTO;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public abstract class FileGeneratorAbstract {

    private static final int START_ROW = 1;

    // skoroszyt offers
    private static final String OFFERS_SHEET = "offers";
    private static final int OFFER_SHEET_UNIT_NAME = 0;
    private static final int OFFER_SHEET_PRODUCT_TYPE = 1; // w zaleznosci od numeru pasma: pasmo > 0: UP, pasmo < 0: DOWN
    private static final int OFFER_SHEET_POWER = 2;
    private static final int OFFER_SHEET_PRICE = 3;
    // skoroszyt units
    private static final String UNITS_SHEET = "units";
    private static final int UNITS_SHEET_DER_NAME = 0;
    private static final int UNITS_SHEET_NODE_ID = 1;
    private static final int UNITS_SHEET_P_MIN = 2;
    private static final int UNITS_SHEET_P_MAX = 3;
    private static final int UNITS_SHEET_Q_MIN = 4;
    private static final int UNITS_SHEET_Q_MAX = 5;
    private static final int UNITS_SHEET_SELF_SCHEDULE = 6;

    protected void fillOffersSheet(AgnoHourNumberDTO hourNumber, XSSFWorkbook workbook) {
        log.debug("fillOffersSheet() Start - fill offers sheet");
        XSSFSheet offersSheet = workbook.getSheet(OFFERS_SHEET);
        AtomicInteger startRowNumb = new AtomicInteger(START_ROW);
        hourNumber.getOfferDetails().forEach(details -> {
            XSSFRow row = offersSheet.createRow(startRowNumb.getAndIncrement());
            XSSFCellStyle cellStyle = getCellStyle(workbook);
            createCellWithStyle(row, OFFER_SHEET_UNIT_NAME, cellStyle).setCellValue(details.getDerName());
            createCellWithStyle(row, OFFER_SHEET_PRODUCT_TYPE, cellStyle).setCellValue(details.getProductDirection().name().toLowerCase());
            createCellWithStyle(row, OFFER_SHEET_POWER, cellStyle).setCellValue(convertKWtoMW(details.getBandVolume()).toString());
            createCellWithStyle(row, OFFER_SHEET_PRICE, cellStyle).setCellValue(details.getBandPrice().toString());
            log.debug("fillOffersSheet() Add offer to sheet: unitName: {}, type: {}", details.getDerName(), details.getProductShortName());
        });
        log.debug("fillOffersSheet() End - fill offers sheet");
    }

    protected void fillUnitsSheet(AgnoHourNumberDTO hourNumber, XSSFWorkbook workbook) {
        log.debug("fillUnitsSheet() Start - fill units sheet");
        XSSFSheet offersSheet = workbook.getSheet(UNITS_SHEET);
        AtomicInteger startRowNumb = new AtomicInteger(START_ROW);
        hourNumber.getDerList().forEach(der -> {
            XSSFRow row = offersSheet.createRow(startRowNumb.getAndIncrement());
            XSSFCellStyle cellStyle = getCellStyle(workbook);
            createCellWithStyle(row, UNITS_SHEET_DER_NAME, cellStyle).setCellValue(der.getName());
            createCellWithStyle(row, UNITS_SHEET_NODE_ID, cellStyle).setCellValue(der.getPowerStationType().getName());
            createCellWithStyle(row, UNITS_SHEET_P_MIN, cellStyle).setCellValue(convertKWtoMW(der.getPMin()).toString());
            createCellWithStyle(row, UNITS_SHEET_P_MAX, cellStyle).setCellValue(convertKWtoMW(der.getPMax()).toString());
            createCellWithStyle(row, UNITS_SHEET_Q_MIN, cellStyle).setCellValue(convertKWtoMW(der.getQMin()).toString());
            createCellWithStyle(row, UNITS_SHEET_Q_MAX, cellStyle).setCellValue(convertKWtoMW(der.getQMax()).toString());
            createCellWithStyle(row, UNITS_SHEET_SELF_SCHEDULE, cellStyle).setCellValue(convertKWtoMW(der.getSelfScheduleVolume()).toString());
            log.debug("fillUnitsSheet() Add der with: id: {} and name: {} to units sheet", der.getId(), der.getName());
        });
        log.debug("fillUnitsSheet() End - fill units sheet");
    }

    protected XSSFWorkbook getWorkbook(String templateFilePath) throws IOException {
        Resource templateFileResource = new ClassPathResource(templateFilePath);
        InputStream fileWithHeaders = templateFileResource.getInputStream();
        return new XSSFWorkbook(fileWithHeaders);
    }

    protected XSSFCell createCellWithStyle(XSSFRow row, int colIndex, XSSFCellStyle cellStyle) {
        XSSFCell cell = row.createCell(colIndex);
        cell.setCellStyle(cellStyle);
        return cell;
    }

    protected XSSFCellStyle getCellStyle(XSSFWorkbook workbook) {
        XSSFCellStyle rowStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeightInPoints((short) 11);
        rowStyle.setFont(font);
        rowStyle.setAlignment(HorizontalAlignment.LEFT);
        rowStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return rowStyle;
    }

    // Zamiana wartości z kW/kWh na MW/MWh
    protected static BigDecimal convertKWtoMW(BigDecimal value) {
        MathContext mc = new MathContext(5);
        return value.divide(BigDecimal.valueOf(1000), mc);
    }
}
