package pl.com.tt.flex.server.service.kpi.generator.volume.balancing.capacity.reserves.down;

import lombok.AllArgsConstructor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.dataexport.util.CellUtils;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;
import pl.com.tt.flex.server.service.kpi.generator.volume.balancing.capacity.reserves.VolumeBalancingOffersReservesData;
import pl.com.tt.flex.server.util.DateFormatter;
import pl.com.tt.flex.server.util.DateUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * KPI - Wolumen usług bilansujących na rezerwy w dół/Volume of balancing service offers for DOWN reserves
 */
@Component
@AllArgsConstructor
public class VolumeBalancingOffersDownReservesGenerator extends KpiGenerator {

    private static final int START_ROW_INDEX = 1;
    private static final int PRODUCT_COL_INDEX = 0;
    private static final int DATE_OF_DELIVERY_COL_INDEX = 1;
    private static final int VOLUME = 2;

    private static final int SUM_PRODUCT_COL_INDEX = 5;
    private static final int SUM_VOLUME_COL_INDEX = 6;

    private final VolumeBalancingOffersDownReservesDataFactory volumeBalancingOffersDownReservesDataFactory;

    @Override
    protected void fillSheet(XSSFWorkbook workbook, KpiDTO kpiDTO) {
        VolumeBalancingOffersReservesData volumeBalancingOffersReservesData = volumeBalancingOffersDownReservesDataFactory
            .create(kpiDTO.getDateFrom(), kpiDTO.getDateTo());
        XSSFSheet sheet = workbook.getSheetAt(0);
        fillProductAndDate(volumeBalancingOffersReservesData, sheet);
        fillVolumeSum(volumeBalancingOffersReservesData, sheet);
    }

    private void fillVolumeSum(VolumeBalancingOffersReservesData volumeBalancingOffersReservesData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        volumeBalancingOffersReservesData.getVolumeSumGroupingByProductName().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(SUM_PRODUCT_COL_INDEX).setCellValue(key);
            row.createCell(SUM_VOLUME_COL_INDEX).setCellValue(value.doubleValue());
        });
        CellUtils.getRow(sheet, rowIndex.getAndIncrement())
                 .createCell(SUM_VOLUME_COL_INDEX)
                 .setCellValue(volumeBalancingOffersReservesData.getVolumeSum().doubleValue());
    }

    private void fillProductAndDate(VolumeBalancingOffersReservesData volumeBalancingOffersReservesData, XSSFSheet sheet) {
        AtomicInteger rowIndex = new AtomicInteger(START_ROW_INDEX);
        volumeBalancingOffersReservesData.getVolumeSumGroupingByProductNameAndDeliveryDate().forEach((key, value) -> {
            XSSFRow row = CellUtils.getRow(sheet, rowIndex.getAndIncrement());
            row.createCell(PRODUCT_COL_INDEX).setCellValue(key.getLeft());
            row.createCell(DATE_OF_DELIVERY_COL_INDEX).setCellValue(DateFormatter.formatWithDot(key.getRight()));
            row.createCell(VOLUME).setCellValue(value.doubleValue());
        });
    }

    @Override
    protected String getTemplate() {
        return "templates/xlsx/kpi/volume/balancing/capacity/reserves/down/VolumeBalancingOffersDownReserves.xlsx";
    }

    @Override
    protected String getFilename(KpiDTO kpiDTO) {
        String from = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateFrom()));
        String to = DateFormatter.formatWithUnderscore(DateUtil.toLocalDate(kpiDTO.getDateTo(), true));
        return String.format("Volume_of_balancing_service_offers_for_DOWN_reserves_%s-%s.xlsx", from, to);
    }

    @Override
    public boolean isSupported(KpiType kpiType) {
        return kpiType.equals(KpiType.VOLUME_OF_BALANCING_SERVICE_OFFERS_DOWN_RESERVES);
    }
}
