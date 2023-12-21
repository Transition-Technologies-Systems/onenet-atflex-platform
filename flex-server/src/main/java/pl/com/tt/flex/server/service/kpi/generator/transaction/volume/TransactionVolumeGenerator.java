package pl.com.tt.flex.server.service.kpi.generator.transaction.volume;

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

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KPI - Wolumen transakcji/Volume of transactions
 */
@Component
@AllArgsConstructor
public class TransactionVolumeGenerator extends KpiGenerator {

    private static final int START_ROW_INDEX = 1;
    private static final int PRODUCT_COL_INDEX = 0;
    private static final int DATE_OF_DELIVERY_COL_INDEX = 1;
    private static final int NUMBER_OF_VOLUMES_COL_INDEX = 2;

    private static final int SUM_PRODUCT_COL_INDEX = 5;
    private static final int SUM_NUMBER_OF_VOLUMES_COL_INDEX = 6;

    private final TransactionVolumeDataFactory transactionVolumeDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        TransactionVolumeData transactionVolumeData = transactionVolumeDataFactory.createForCapacityOffers(kpiDTO.getDateFrom(), kpiDTO.getDateTo());
        XSSFSheet sheet = workbook.getSheetAt(0);
        fillProductAndDate(transactionVolumeData, sheet);
        fillVolumeSum(transactionVolumeData, sheet);
    }

    private void fillVolumeSum(TransactionVolumeData transactionVolumeData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        transactionVolumeData.getNumberOfVolumesnGroupingByProductName().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(SUM_PRODUCT_COL_INDEX, CellType.STRING).setCellValue(key);
            row.createCell(SUM_NUMBER_OF_VOLUMES_COL_INDEX, CellType.NUMERIC).setCellValue(value.doubleValue());
        });
        BigDecimal sumVolumes = transactionVolumeData.getNumberOfVolumesnGroupingByProductName().values()
                                                     .stream()
                                                     .reduce(BigDecimal.ZERO, BigDecimal::add);
        CellUtils.getRow(sheet, rowIndex.getAndIncrement()).createCell(SUM_NUMBER_OF_VOLUMES_COL_INDEX, CellType.NUMERIC).setCellValue(sumVolumes.doubleValue());
    }

    private void fillProductAndDate(TransactionVolumeData transactionNumberData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        transactionNumberData.getNumberOfVolumesGroupingByProductNameAndDeliveryDate().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(PRODUCT_COL_INDEX, CellType.STRING).setCellValue(key.getLeft());
            row.createCell(DATE_OF_DELIVERY_COL_INDEX, CellType.STRING).setCellValue(DateFormatter.formatWithDot(key.getRight()));
            row.createCell(NUMBER_OF_VOLUMES_COL_INDEX, CellType.NUMERIC).setCellValue(value.doubleValue());
        });
    }

    @Override
    protected String getTemplate() {
        return "templates/xlsx/kpi/transaction/volumes/VolumeOfTransactions.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        String from = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateFrom()));
        String to = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateTo(), true));
        return String.format("Volume_of_transactions_%s-%s.xlsx", from, to);
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.VOLUME_OF_TRANSACTIONS);
    }
}
