package pl.com.tt.flex.server.service.kpi.generator.unit.resources.available;

import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.dataexport.util.CellUtils;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;

/**
 * KPI - Procent zasobów dostępnych do usług bilansujących/Percentage of resources available for balancing
 */
@Component
@AllArgsConstructor
public class ResourcesAvailableForBSGenerator extends KpiGenerator {

    private static final int ROW_TO_FILL = 1;
    private static final int N_DER_BAL = 0;
    private static final int N_DER_ALL = 1;
    private static final int K_BAL = 2;

    private final ResourcesAvailableForBSDataFactory resourcesAvailableForBSDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        XSSFSheet sheet = workbook.getSheetAt(0);
        ResourcesAvailableForBSData resourcesAvailableForBSData = resourcesAvailableForBSDataFactory.create();

        sheet.getRow(ROW_TO_FILL).getCell(N_DER_BAL).setCellValue(resourcesAvailableForBSData.getDersWithPottential().doubleValue());
        sheet.getRow(ROW_TO_FILL).getCell(N_DER_ALL).setCellValue(resourcesAvailableForBSData.getDersWithCertification().doubleValue());
        XSSFCell percentagePrequalifiedDersCell = sheet.getRow(ROW_TO_FILL).getCell(K_BAL);
        percentagePrequalifiedDersCell.setCellValue(resourcesAvailableForBSData.getResourceAvailable().doubleValue());
        percentagePrequalifiedDersCell.setCellStyle(CellUtils.getPercentageCellStyle(sheet));
    }

    @Override
    protected String getTemplate() {
        return "/templates/xlsx/kpi/unit/PercentageResourceAvailable.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        return "Percentage_of_resources_available_for_balancing_services.xlsx";
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.PERCENTAGE_RESOURCES_AVAILABLE_FOR_BALANCING_SERVICES);
    }
}
