package pl.com.tt.flex.server.service.kpi.generator.unit.prequalified.successfully;

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
 * KPI - Procent zasobów prekwalifikowanych z powodzeniem/Percentage of succesfully prequalified distributed energy resources
 */
@Component
@AllArgsConstructor
public class UnitSuccessfullyPrequalifiedGenerator extends KpiGenerator {

    private static final int ROW_TO_FILL = 1;
    private static final int N_DER_PREQ = 0;
    private static final int N_DER_REG = 1;
    private static final int K_DER = 2;

    private final UnitSuccessfullyPrequalifiedDataFactory unitSuccessfullyPrequalifiedDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        XSSFSheet sheet = workbook.getSheetAt(0);
        UnitSuccessfullyPrequalifiedData unitSuccessfullyPrequalifiedData = unitSuccessfullyPrequalifiedDataFactory.create();

        sheet.getRow(ROW_TO_FILL).getCell(N_DER_PREQ).setCellValue(unitSuccessfullyPrequalifiedData.getPrequalifiedDers().doubleValue());
        sheet.getRow(ROW_TO_FILL).getCell(N_DER_REG).setCellValue(unitSuccessfullyPrequalifiedData.getAllDers().doubleValue());
        XSSFCell percentagePrequalifiedDersCell = sheet.getRow(ROW_TO_FILL).getCell(K_DER);
        percentagePrequalifiedDersCell.setCellValue(unitSuccessfullyPrequalifiedData.getSuccessfullyPrequalifiedDers().doubleValue());
        percentagePrequalifiedDersCell.setCellStyle(CellUtils.getPercentageCellStyle(sheet));
    }

    @Override
    protected String getTemplate() {
        return "/templates/xlsx/kpi/unit/PercentageSuccessfullyPrequalifiedDers.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        return "Percentage_of_successfully_prequalified_distributed_energy_resources.xlsx";
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.PERCENTAGE_OF_SUCCESSFULLY_PREQUALIFIED_DERS);
    }
}
