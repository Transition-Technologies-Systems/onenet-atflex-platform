package pl.com.tt.flex.server.service.kpi.generator.fsp.number;

import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;

/**
 * KPI - Liczba FSP/Number of FSPs
 */
@Component
@AllArgsConstructor
public class NumberOfFspGenerator extends KpiGenerator {

    public static final int ROW_TO_FILL = 1;
    public static final int NUMBER_OF_FSPS_COL_INDEX = 0;

    private final NumberOfFspDataFactory numberOfFspDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        XSSFSheet sheet = workbook.getSheetAt(0);

        NumberOfFspData numberOfFspData = numberOfFspDataFactory.create();
        sheet.getRow(ROW_TO_FILL).getCell(NUMBER_OF_FSPS_COL_INDEX).setCellValue(numberOfFspData.getNumberOfFsp().intValue());
    }

    @Override
    protected String getTemplate() {
        return "/templates/xlsx/kpi/fsp/number/NumberOfFSPs.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        return "Number_of_FSPs.xlsx";
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.NUMBER_OF_FSPS);
    }
}
