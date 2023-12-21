package pl.com.tt.flex.server.service.kpi.generator.transaction.number;

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
 * KPI - Liczba transakcji/Number of transactions
 */
@Component
@AllArgsConstructor
public class TransactionNumberGenerator extends KpiGenerator {

    private static final int START_ROW_INDEX = 1;
    private static final int PRODUCT_COL_INDEX = 0;
    private static final int DATE_OF_DELIVERY_COL_INDEX = 1;
    private static final int NUMBER_OF_TRANSACTIONS_COL_INDEX = 2;

    private static final int SUM_PRODUCT_COL_INDEX = 5;
    private static final int SUM_NUMBER_OF_TRANSACTIONS_COL_INDEX = 6;

    private final TransactionNumberDataFactory transactionNumberDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        TransactionNumberData transactionNumberData = transactionNumberDataFactory.create(kpiDTO.getDateFrom(), kpiDTO.getDateTo());
        XSSFSheet sheet = workbook.getSheetAt(0);
        fillProductAndDate(transactionNumberData, sheet);
        fillTransactionSum(transactionNumberData, sheet);
    }

    private void fillTransactionSum(TransactionNumberData transactionNumberData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        transactionNumberData.getNumberOfTransactionGroupingByProductName().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(SUM_PRODUCT_COL_INDEX).setCellValue(key);
            row.createCell(SUM_NUMBER_OF_TRANSACTIONS_COL_INDEX).setCellValue(value);
        });
        long sumTransactions = transactionNumberData.getNumberOfTransactionGroupingByProductName().values()
                                                    .stream()
                                                    .reduce(0L, Long::sum);
        CellUtils.getRow(sheet, rowIndex.getAndIncrement()).createCell(SUM_NUMBER_OF_TRANSACTIONS_COL_INDEX).setCellValue(sumTransactions);
    }

    private void fillProductAndDate(TransactionNumberData transactionNumberData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        transactionNumberData.getNumberOfTransactionGroupingByProductNameAndDeliveryDate().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(PRODUCT_COL_INDEX).setCellValue(key.getLeft());
            row.createCell(DATE_OF_DELIVERY_COL_INDEX).setCellValue(DateFormatter.formatWithDot(key.getRight()));
            row.createCell(NUMBER_OF_TRANSACTIONS_COL_INDEX).setCellValue(value);
        });
    }

    @Override
    protected String getTemplate() {
        return "templates/xlsx/kpi/transaction/number/NumberOfTransactions.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        String from = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateFrom()));
        String to = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateTo(), true));
        return String.format("Number_of_transactions_%s-%s.xlsx", from, to);
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.NUMBER_OF_TRANSACTIONS);
    }
}
