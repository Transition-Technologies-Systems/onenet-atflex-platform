package pl.com.tt.flex.server.service.kpi.generator.requested.flexibility.power.fillfactor;

import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFCell;
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
 * KPI - Potencjał oferowany przez FSP vs potencjał oczekiwany przez OSD/Flex volume offered by FSP vs Flex request by DSO
 */
@Component
@AllArgsConstructor
public class PowerFillFactorGenerator extends KpiGenerator {

    private static final int START_ROW_INDEX = 1;
    private static final int PRODUCT_COL_INDEX = 0;
    private static final int DATE_OF_DELIVERY_COL_INDEX = 1;
    private static final int REQUESTED_VOLUME = 2;
    private static final int OFFERED_VOLUME = 3;

    private static final int OFFERED_VOLUME_SUM = 5;
    private static final int REQUESTED_VOLUME_SUM = 6;
    private static final int AUCTION_NUMBER = 7;
    private static final int FILL_FACTOR = 8;

    private final PowerFillFactorDataFactory powerFillFactorDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        PowerFillFactorData powerFillFactorData = powerFillFactorDataFactory
            .create(kpiDTO.getDateFrom(), kpiDTO.getDateTo());
        XSSFSheet sheet = workbook.getSheetAt(0);
        fillProductAndDate(powerFillFactorData, sheet);
        fillCalculation(powerFillFactorData, sheet);
    }

    private void fillCalculation(PowerFillFactorData powerFillFactorData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
        row.createCell(OFFERED_VOLUME_SUM).setCellValue(powerFillFactorData.getOfferedVolumeSum().doubleValue());
        row.createCell(REQUESTED_VOLUME_SUM).setCellValue(powerFillFactorData.getRequestedVolumeSum().doubleValue());
        row.createCell(AUCTION_NUMBER).setCellValue(powerFillFactorData.getNumberOfAuctions().doubleValue());
        XSSFCell fillFactorCell = row.createCell(FILL_FACTOR);
        fillFactorCell.setCellValue(powerFillFactorData.getFillFactor().doubleValue());
        fillFactorCell.setCellStyle(CellUtils.getPercentageCellStyle(sheet));
    }

    private void fillProductAndDate(PowerFillFactorData powerFillFactorData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        powerFillFactorData.getMaxVolumeGroupingByProductNameAndDeliveryDate().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(PRODUCT_COL_INDEX).setCellValue(key.getLeft());
            row.createCell(DATE_OF_DELIVERY_COL_INDEX).setCellValue(DateFormatter.formatWithDot(key.getRight()));
            row.createCell(REQUESTED_VOLUME).setCellValue(value.getRequestedVolume().doubleValue());
            row.createCell(OFFERED_VOLUME).setCellValue(value.getOfferedVolume().doubleValue());
        });
    }

    @Override
    protected String getTemplate() {
        return "templates/xlsx/kpi/requested/flexibility/power/fill_factor/PowerFillFactor.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        String from = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateFrom()));
        String to = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateTo(), true));
        return String.format("Flex_volume_offered_by_FSP_vs_Flex_request_by_DSO_%s-%s.xlsx", from, to);
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.FLEX_VOLUME_OFFERED_VS_FLEX_REQUESTED_BY_DSO);
    }
}
