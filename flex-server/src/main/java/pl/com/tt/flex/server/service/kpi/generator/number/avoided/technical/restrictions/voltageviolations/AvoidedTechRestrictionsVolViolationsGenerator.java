package pl.com.tt.flex.server.service.kpi.generator.number.avoided.technical.restrictions.voltageviolations;


import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;

/**
 * KPI - Liczba unikniętych ograniczeń technicznych(przeciążenie napięciowe)/Number of avoided technical restrictions(voltage violations)
 */
@Component
@AllArgsConstructor
public class AvoidedTechRestrictionsVolViolationsGenerator extends KpiGenerator {

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
    }

    @Override
    protected String getTemplate() {
        return "/templates/xlsx/kpi/number/avoided/technical/restrictions/NumberAvoidedTechRestrictions.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        return "Number_of_avoided_technical_restrictions_voltage_violations.xlsx";
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.NUMBER_AVOIDED_TECHNICAL_RESTRICTIONS_VOL_VIOLATIONS);
    }
}
