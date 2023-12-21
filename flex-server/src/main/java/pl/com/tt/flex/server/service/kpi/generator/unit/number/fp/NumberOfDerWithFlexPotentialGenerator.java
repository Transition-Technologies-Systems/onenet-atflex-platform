package pl.com.tt.flex.server.service.kpi.generator.unit.number.fp;

import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;

/**
 * KPI - Liczba certyfikowanych zasobów posiadających potencjał elastyczności/Number of certified DERs for at least one flexibility product
 */
@Component
@AllArgsConstructor
public class NumberOfDerWithFlexPotentialGenerator extends KpiGenerator {

    private static final int ROW_TO_FILL = 1;
    private static final int N_DER_CER = 0;

    private final NumberOfDerWithFlexPotentialDataFactory numberOfDerWithFlexPotentialDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        XSSFSheet sheet = workbook.getSheetAt(0);
        NumberOfDerWithFlexPotentialData numberOfDerWithFlexPotentialData = numberOfDerWithFlexPotentialDataFactory.create();
        sheet.getRow(ROW_TO_FILL).getCell(N_DER_CER).setCellValue(numberOfDerWithFlexPotentialData.getNumberOfDers().doubleValue());
    }

    @Override
    protected String getTemplate() {
        return "/templates/xlsx/kpi/unit/NumberOfDersWithFlexPotential.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        return "Number_of_certified_DERs_for_at_least_one_flexibility_product.xlsx";
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.NUMBER_OF_DERS_WITH_AT_LEAST_ONE_FP);
    }
}
