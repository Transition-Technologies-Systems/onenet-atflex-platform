package pl.com.tt.flex.server.service.kpi.generator.unit.capacity.total.forbsp;

import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;

/**
 * KPI - Zsumowany wolumen zasobów dostępnych dla BSP/Total capacity of DER available for BSP
 */
@Component
@AllArgsConstructor
public class TotalCapacityDerAvailableBspGenerator extends KpiGenerator {

    private static final int ROW_TO_FILL = 1;
    private static final int TOTAL_VOLUME = 0;

    private final TotalCapacityDerAvailableBspDataFactory totalCapacityDerAvailableBspDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        XSSFSheet sheet = workbook.getSheetAt(0);
        TotalCapacityDerAvailableBspData totalCapacityDerAvailableBspData = totalCapacityDerAvailableBspDataFactory.create();
        sheet.getRow(ROW_TO_FILL).getCell(TOTAL_VOLUME).setCellValue(totalCapacityDerAvailableBspData.getTotalVolume().doubleValue());
    }

    @Override
    protected String getTemplate() {
        return "/templates/xlsx/kpi/unit/TotalCapacityOfDerAvailableForBsp.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        return "Total_capacity_of_DER_available_for_BSP.xlsx";
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.TOTAL_CAPACITY_OF_DERS_AVAILABLE_FOR_BSP);
    }
}
