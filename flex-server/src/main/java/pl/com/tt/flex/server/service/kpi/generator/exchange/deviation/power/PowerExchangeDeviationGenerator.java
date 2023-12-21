package pl.com.tt.flex.server.service.kpi.generator.exchange.deviation.power;

import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.exchange.deviation.ExchangeDeviationData;
import pl.com.tt.flex.server.service.kpi.generator.exchange.deviation.ExchangeDeviationGenerator;
import pl.com.tt.flex.server.util.DateFormatter;
import pl.com.tt.flex.server.util.DateUtil;

/**
 * KPI - Odchylenie w aktywacji mocy/Power exchange deviation
 */
@Component
@AllArgsConstructor
public class PowerExchangeDeviationGenerator extends ExchangeDeviationGenerator {

    private final PowerExchangeDeviationDataFactory powerExchangeDeviationDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        ExchangeDeviationData exchangeDeviationData = powerExchangeDeviationDataFactory
            .create(kpiDTO.getDateFrom(), kpiDTO.getDateTo());
        XSSFSheet sheet = workbook.getSheetAt(0);
        fillProductAndDate(exchangeDeviationData, sheet);
        fillProductDeviation(exchangeDeviationData, sheet);
    }

    @Override
    protected String getTemplate() {
        return "templates/xlsx/kpi/exchange/deviation/PowerExchangeDeviation.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        String from = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateFrom()));
        String to = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateTo(), true));
        return String.format("Power_exchange_deviation_%s-%s.xlsx", from, to);
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.POWER_EXCHANGE_DEVIATION);
    }
}
