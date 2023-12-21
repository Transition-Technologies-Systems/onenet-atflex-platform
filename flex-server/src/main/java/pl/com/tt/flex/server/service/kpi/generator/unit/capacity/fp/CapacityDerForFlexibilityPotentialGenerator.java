package pl.com.tt.flex.server.service.kpi.generator.unit.capacity.fp;

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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * KPI - Wolumen prekwalifikowanych zasobów posiadających potencjał elastyczności/Capacity of certified DERs for at least one flexibility product
 */
@Component
@AllArgsConstructor
public class CapacityDerForFlexibilityPotentialGenerator extends KpiGenerator {

    private static final int START_ROW_INDEX = 1;
    private static final int PRODUCT_COL_INDEX = 0;
    private static final int CAPACITY_OF_FP = 1;

    private final CapacityDerForFlexibilityPotentialDataFactory capacityDerForFlexibilityPotentialDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        CapacityDerForFlexibilityPotentialData data = capacityDerForFlexibilityPotentialDataFactory.create();
        XSSFSheet sheet = workbook.getSheetAt(0);
        fillRows(data, sheet);
    }

    private void fillRows(CapacityDerForFlexibilityPotentialData capacityDerForFlexibilityPotentialData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        capacityDerForFlexibilityPotentialData.getCapacityOfCertifiedDers().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(PRODUCT_COL_INDEX, CellType.STRING).setCellValue(key);
            row.createCell(CAPACITY_OF_FP, CellType.NUMERIC).setCellValue(value.doubleValue());
        });
        CellUtils.getRow(sheet, rowIndex.getAndIncrement())
                 .createCell(CAPACITY_OF_FP, CellType.NUMERIC)
                 .setCellValue(capacityDerForFlexibilityPotentialData.getSum().doubleValue());
    }

    @Override
    protected String getTemplate() {
        return "templates/xlsx/kpi/unit/CapacityOfCertifiedDersForFlexibilityPotential.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        return "Capacity_of_certified_DERs_for_at_least_one_flexibility_product.xlsx";
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.CAPACITY_OF_CERTIFIED_DERS_WITH_AT_LEAST_ONE_FP);
    }
}
