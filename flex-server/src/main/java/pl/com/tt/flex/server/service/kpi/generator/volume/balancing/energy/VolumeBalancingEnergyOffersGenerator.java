package pl.com.tt.flex.server.service.kpi.generator.volume.balancing.energy;

import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.dataexport.util.CellUtils;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;
import pl.com.tt.flex.server.util.DateFormatter;
import pl.com.tt.flex.server.util.DateUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * KPI - Wolumen ofert na energię bilansującą/Volume of balancing energy offers
 */
@Component
@AllArgsConstructor
public class VolumeBalancingEnergyOffersGenerator extends KpiGenerator {

    private static final int START_ROW_INDEX = 1;
    private static final int DATE_OF_DELIVERY_COL_INDEX = 0;
    private static final int VOLUME = 1;

    private final VolumeBalancingEnergyOffersDataFactory volumeBalancingEnergyOffersDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        VolumeBalancingEnergyOffersData volumeBalancingEnergyOffersData = volumeBalancingEnergyOffersDataFactory
            .create(kpiDTO.getDateFrom(), kpiDTO.getDateTo());
        XSSFSheet sheet = workbook.getSheetAt(0);
        fillRows(volumeBalancingEnergyOffersData, sheet);
    }

    private void fillRows(VolumeBalancingEnergyOffersData volumeBalancingEnergyOffersData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        volumeBalancingEnergyOffersData.getEnergyVolumesGroupingByDate().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(DATE_OF_DELIVERY_COL_INDEX).setCellValue(DateFormatter.formatWithDot(key));
            row.createCell(VOLUME).setCellValue(value.doubleValue());
        });
        CellUtils.getRow(sheet, rowIndex.getAndIncrement())
                 .createCell(VOLUME)
                 .setCellValue(volumeBalancingEnergyOffersData.getVolumeSum().doubleValue());
    }

    @Override
    protected String getTemplate() {
        return "templates/xlsx/kpi/volume/balancing/energy/VolumeBalancingEnergyOffers.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        String from = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateFrom()));
        String to = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateTo(), true));
        return String.format("Volume_of_balancing_energy_offers_%s-%s.xlsx", from, to);
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.VOLUME_OF_BALANCING_ENERGY_OFFERS);
    }
}
