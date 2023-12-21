package pl.com.tt.flex.server.service.kpi.generator.available.flexibility;

import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.dataexport.util.CellUtils;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;
import pl.com.tt.flex.server.util.DateFormatter;
import pl.com.tt.flex.server.util.DateUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * KPI - Liczba FSP/Number of FSPs
 */
@Component
@AllArgsConstructor
public class AvailableFlexibilityGenerator extends KpiGenerator {

    private static final int START_ROW_INDEX = 1;
    private static final int PRODUCT_COL_INDEX = 0;
    private static final int DATE_OF_DELIVERY_COL_INDEX = 1;
    private static final int AVAILABLE_FLEXIBILITY_COL_INDEX = 2;
    private static final int TOTAL_COL_INDEX = 3;
    private static final int FLEXIBILITY_PERCENTAGE_COL_INDEX = 4;

    private static final int SUM_PRODUCT_COL_INDEX = 7;
    private static final int SUM_FLEXIBILITY_PERCENTAGE_COL_INDEX = 8;

    private final AvailableFlexibilityDataFactory availableFlexibilityDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        AvailableFlexibilityData availableFlexibilityData = availableFlexibilityDataFactory.create(kpiDTO.getDateFrom(), kpiDTO.getDateTo());
        XSSFSheet sheet = workbook.getSheetAt(0);
        fillProductAndDate(availableFlexibilityData, sheet);
        fillSum(availableFlexibilityData, sheet);
    }

    private void fillSum(AvailableFlexibilityData availableFlexibilityData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        availableFlexibilityData.getAvailableFlexibilityDateGroupingByProduct().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(SUM_PRODUCT_COL_INDEX, CellType.STRING).setCellValue(key);
            row.createCell(SUM_FLEXIBILITY_PERCENTAGE_COL_INDEX, CellType.NUMERIC).setCellValue(value.getFlexibilityPercentage().doubleValue());
        });
        CellUtils.getRow(sheet, rowIndex.getAndIncrement())
                 .createCell(SUM_FLEXIBILITY_PERCENTAGE_COL_INDEX, CellType.NUMERIC)
                 .setCellValue(availableFlexibilityData.getAvailableFlexibilityAll().doubleValue());
    }

    private void fillProductAndDate(AvailableFlexibilityData availableFlexibilityData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        availableFlexibilityData.getAvailableFlexibilityDateGroupingByProductAndDeliveryDate().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(PRODUCT_COL_INDEX, CellType.STRING).setCellValue(key.getLeft());
            row.createCell(DATE_OF_DELIVERY_COL_INDEX, CellType.STRING).setCellValue(DateFormatter.formatWithDot(key.getRight()));
            row.createCell(AVAILABLE_FLEXIBILITY_COL_INDEX, CellType.NUMERIC).setCellValue(value.getAvailableFlexibility().doubleValue());
            row.createCell(TOTAL_COL_INDEX, CellType.NUMERIC).setCellValue(value.getTotal().doubleValue());
            row.createCell(FLEXIBILITY_PERCENTAGE_COL_INDEX, CellType.NUMERIC).setCellValue(value.getFlexibilityPercentage().doubleValue());
        });
    }

    @Override
    protected String getTemplate() {
        return "/templates/xlsx/kpi/available/flexibility/AvailableFlexibility.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        String from = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateFrom()));
        String to = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateTo(), true));
        return String.format("Available_flexibility_%s-%s.xlsx", from, to);
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.AVAILABLE_FLEXIBILITY);
    }
}
