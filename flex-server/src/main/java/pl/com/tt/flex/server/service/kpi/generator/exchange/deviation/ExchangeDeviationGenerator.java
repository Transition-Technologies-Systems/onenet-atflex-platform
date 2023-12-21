package pl.com.tt.flex.server.service.kpi.generator.exchange.deviation;

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
import pl.com.tt.flex.server.service.kpi.generator.exchange.deviation.power.PowerExchangeDeviationDataFactory;
import pl.com.tt.flex.server.util.DateFormatter;
import pl.com.tt.flex.server.util.DateUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public abstract class ExchangeDeviationGenerator extends KpiGenerator {

    protected static final int START_ROW_INDEX = 1;
    protected static final int PRODUCT_COL_INDEX = 0;
    protected static final int DATE_OF_DELIVERY_COL_INDEX = 1;
    protected static final int ACCEPTED_VOLUME_COL_INDEX = 2;
    protected static final int ACTIVATED_VOLUME_COL_INDEX = 3;
    protected static final int DEVIATION_COL_INDEX = 4;

    protected static final int SUM_PRODUCT_COL_INDEX = 7;
    protected static final int SUM_DEVIATION_COL_INDEX = 8;

    @Override
    protected abstract void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO);

    @Override
    protected abstract String getTemplate();

    @Override
    protected abstract String getFilename(KpiDTO kpiDTO);

    @Override
    public abstract boolean isSupported(KpiType kpiType);

    protected void fillProductAndDate(ExchangeDeviationData exchangeDeviationData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        exchangeDeviationData.getDeviationByProductNameAndDeliveryDate().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(PRODUCT_COL_INDEX, CellType.STRING).setCellValue(key.getLeft());
            row.createCell(DATE_OF_DELIVERY_COL_INDEX, CellType.STRING).setCellValue(DateFormatter.formatWithDot(key.getRight()));
            row.createCell(ACCEPTED_VOLUME_COL_INDEX, CellType.NUMERIC).setCellValue(getAcceptedVolume(value).doubleValue());
            row.createCell(ACTIVATED_VOLUME_COL_INDEX, CellType.NUMERIC).setCellValue(getActivatedVolume(value).doubleValue());
            row.createCell(DEVIATION_COL_INDEX, CellType.NUMERIC).setCellValue(getDeviation(value));
        });
    }

    protected void fillProductDeviation(ExchangeDeviationData exchangeDeviationData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        exchangeDeviationData.getDeviationGroupingByProductName().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(SUM_PRODUCT_COL_INDEX, CellType.STRING).setCellValue(key);
            row.createCell(SUM_DEVIATION_COL_INDEX, CellType.NUMERIC).setCellValue(getDeviation(value));
        });
        CellUtils.getRow(sheet, rowIndex.getAndIncrement()).createCell(SUM_DEVIATION_COL_INDEX, CellType.NUMERIC)
            .setCellValue(getDeviation(exchangeDeviationData.getDeviationGroupingByProductName().values()
                .stream().flatMap(Collection::stream).collect(Collectors.toList())));
    }

    private BigDecimal getAcceptedVolume(List<DeviationData> value) {
        return value.stream().map(DeviationData::getAcceptedVolume).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getActivatedVolume(List<DeviationData> value) {
        return value.stream().map(DeviationData::getActivatedVolume).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private double getDeviation(List<DeviationData> value) {
        BigDecimal acceptedPower = getAcceptedVolume(value);
        BigDecimal activatedPower = getActivatedVolume(value);
        BigDecimal deviation = acceptedPower.subtract(activatedPower).divide(acceptedPower, 3, RoundingMode.HALF_UP);
        return deviation.compareTo(BigDecimal.ZERO) > 0 ? deviation.doubleValue() * 100 : deviation.doubleValue() * (-100);
    }
}
