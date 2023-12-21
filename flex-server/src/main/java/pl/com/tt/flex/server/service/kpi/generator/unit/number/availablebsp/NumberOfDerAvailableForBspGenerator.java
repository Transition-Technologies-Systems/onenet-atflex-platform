package pl.com.tt.flex.server.service.kpi.generator.unit.number.availablebsp;

import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;

/**
 * KPI - Liczba zasobów dostępnych dla BSP/Number of DERs available for BSPs
 */
@Component
@AllArgsConstructor
public class NumberOfDerAvailableForBspGenerator extends KpiGenerator {

    private static final int ROW_TO_FILL = 1;
    private static final int N_DER_AV = 0;

    private final NumberOfDerAvailableForBspDataFactory numberOfDerAvailableForBspDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        XSSFSheet sheet = workbook.getSheetAt(0);
        NumberOfDerAvailableForBspData numberOfDerAvailableForBspData = numberOfDerAvailableForBspDataFactory.create();
        sheet.getRow(ROW_TO_FILL).getCell(N_DER_AV).setCellValue(numberOfDerAvailableForBspData.getAvailableDerForBsp().doubleValue());
    }

    @Override
    protected String getTemplate() {
        return "/templates/xlsx/kpi/unit/NumberOfDerAvailableForBsp.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        return "Number_of_DERs_available_for_BSPs.xlsx";
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.NUMBER_OF_DER_AVAILABLE_FOR_BSP);
    }
}
