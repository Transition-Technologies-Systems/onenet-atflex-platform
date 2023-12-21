package pl.com.tt.flex.server.service.kpi.generator.requested.flexibility.power;

import lombok.AllArgsConstructor;
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
 * KPI - Oczekiwana elastyczność (moc)/Requested flexibility (power)
 */
@Component
@AllArgsConstructor
public class RequestedFlexibilityPowerGenerator extends KpiGenerator {

    private static final int START_ROW_INDEX = 1;
    private static final int PRODUCT_COL_INDEX = 0;
    private static final int DATE_OF_DELIVERY_COL_INDEX = 1;
    private static final int VOLUME = 2;

    private static final int SUM_PRODUCT_COL_INDEX = 5;
    private static final int SUM_VOLUME_COL_INDEX = 6;

    private final RequestedFlexibilityPowerDataFactory requestedFlexibilityPowerDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        RequestedFlexibilityPowerData requestedFlexibilityPowerData = requestedFlexibilityPowerDataFactory
            .create(kpiDTO.getDateFrom(), kpiDTO.getDateTo());
        XSSFSheet sheet = workbook.getSheetAt(0);
        fillProductAndDate(requestedFlexibilityPowerData, sheet);
        fillVolumeSum(requestedFlexibilityPowerData, sheet);
    }

    private void fillVolumeSum(RequestedFlexibilityPowerData requestedFlexibilityPowerData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        requestedFlexibilityPowerData.getSumMaxVolumeGroupingByProductName().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(SUM_PRODUCT_COL_INDEX).setCellValue(key);
            row.createCell(SUM_VOLUME_COL_INDEX).setCellValue(value.doubleValue());
        });
        CellUtils.getRow(sheet, rowIndex.getAndIncrement())
                 .createCell(SUM_VOLUME_COL_INDEX)
                 .setCellValue(requestedFlexibilityPowerData.getSumMaxVolume().doubleValue());
    }

    private void fillProductAndDate(RequestedFlexibilityPowerData requestedFlexibilityPowerData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        requestedFlexibilityPowerData.getMaxVolumeGroupingByProductNameAndDeliveryDate().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(PRODUCT_COL_INDEX).setCellValue(key.getLeft());
            row.createCell(DATE_OF_DELIVERY_COL_INDEX).setCellValue(DateFormatter.formatWithDot(key.getRight()));
            row.createCell(VOLUME).setCellValue(value.doubleValue());
        });
    }

    @Override
    protected String getTemplate() {
        return "templates/xlsx/kpi/requested/flexibility/power/RequestedFlexibilityPower.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        String from = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateFrom()));
        String to = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateTo(), true));
        return String.format("Requested_flexibility_power_%s-%s.xlsx", from, to);
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.REQUEST_FLEXIBILITY_POWER);
    }
}
