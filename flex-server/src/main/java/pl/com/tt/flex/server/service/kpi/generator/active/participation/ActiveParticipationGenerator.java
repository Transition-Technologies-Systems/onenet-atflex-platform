package pl.com.tt.flex.server.service.kpi.generator.active.participation;

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
 * KPI - Aktywne uczestnictwo/Active participation
 */
@Component
@AllArgsConstructor
public class ActiveParticipationGenerator extends KpiGenerator {

    private static final int ROW_TO_FILL = 1;
    private static final int NACTIVE_COL_INDEX = 0;
    private static final int NACCEPT_COL_INDEX = 1;
    private static final int R_COL_INDEX = 2;

    private final ActiveParticipationDataFactory activeParticipationDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        XSSFSheet sheet = workbook.getSheetAt(0);

        ActiveParticipationData activeParticipationData = activeParticipationDataFactory.create();

        sheet.getRow(ROW_TO_FILL).getCell(NACTIVE_COL_INDEX).setCellValue(activeParticipationData.getDersUsedInAuction().doubleValue());
        sheet.getRow(ROW_TO_FILL).getCell(NACCEPT_COL_INDEX).setCellValue(activeParticipationData.getCountCertifiedDers().doubleValue());
        XSSFCell percentageUsedDersCell = sheet.getRow(ROW_TO_FILL).getCell(R_COL_INDEX);
        percentageUsedDersCell.setCellValue(activeParticipationData.getUsedDers().doubleValue());
        percentageUsedDersCell.setCellStyle(CellUtils.getPercentageCellStyle(sheet));
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.ACTIVE_PARTICIPATION);
    }

    @Override
    protected String getTemplate() {
        return "/templates/xlsx/kpi/active/participation/Active_participation.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        return "Active_participation.xlsx";
    }
}
