package pl.com.tt.flex.server.service.auction.da.file.factory;

import static pl.com.tt.flex.model.service.dto.product.type.Direction.DOWN;
import static pl.com.tt.flex.model.service.dto.product.type.Direction.UP;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.Range;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;

public class DACapacityOfferImportTemplateFactory extends AbstractDAOfferImportTemplateFactory {

    public static final int CAPACITY_DER_SECTION_HEIGHT = 3;
    public static final int CAPACITY_FIRST_DER_START_ROW = 4;
    public static final int NUMBER_OF_BANDS = 2;
    public static final int UP_SELF_SCHEDULE_ROW_OFFSET = 2;
    public static final int DOWN_SELF_SCHEDULE_ROW_OFFSET = 1;
    public static final Range<Integer> UP_BAND_RANGE = Range.between(0, 1);
    public static final Range<Integer> DOWN_BAND_RANGE = Range.between(-1, 0);

    public static FileDTO getCapacityOfferImportTemplate(AuctionDayAheadDTO dbAuction, SchedulingUnitDTO dbSchedulingUnit,
                                                         String userLang, Map<Long, Map<String, BigDecimal>> derSelfSchedules,
                                                         Direction productDirection) throws IOException {
        if (productDirection.equals(UP)) {
            return getCapacityUpOfferImportTemplate(dbAuction, dbSchedulingUnit, derSelfSchedules, userLang);
        } else if (productDirection.equals(DOWN)) {
            return getCapacityDownOfferImportTemplate(dbAuction, dbSchedulingUnit, derSelfSchedules, userLang);
        }
        throw new IllegalStateException("Unsupported product direction");
    }

    private static FileDTO getCapacityUpOfferImportTemplate(AuctionDayAheadDTO dbAuction,
                                                            SchedulingUnitDTO dbSchedulingUnit,
                                                            Map<Long, Map<String, BigDecimal>> derSelfSchedules,
                                                            String userLang) throws IOException {
        XSSFWorkbook workbook = getWorkbook("capacity/up", "capacity_up", userLang);
        addDersToTemplate(dbSchedulingUnit, derSelfSchedules, workbook, CAPACITY_DER_SECTION_HEIGHT, CAPACITY_FIRST_DER_START_ROW, UP_SELF_SCHEDULE_ROW_OFFSET);
        setFormulas(workbook, dbSchedulingUnit.getNumberOfDers(), NUMBER_OF_BANDS, CAPACITY_DER_SECTION_HEIGHT);
        String filename = getTemplateFilename(dbAuction, dbSchedulingUnit, userLang);
        return writeFileAndGetDTO(workbook, filename);
    }

    private static FileDTO getCapacityDownOfferImportTemplate(AuctionDayAheadDTO dbAuction,
                                                              SchedulingUnitDTO dbSchedulingUnit,
                                                              Map<Long, Map<String, BigDecimal>> derSelfSchedules,
                                                              String userLang) throws IOException {
        XSSFWorkbook workbook = getWorkbook("capacity/down", "capacity_down", userLang);
        addDersToTemplate(dbSchedulingUnit, derSelfSchedules, workbook, CAPACITY_DER_SECTION_HEIGHT, CAPACITY_FIRST_DER_START_ROW, DOWN_SELF_SCHEDULE_ROW_OFFSET);
        setFormulas(workbook, dbSchedulingUnit.getNumberOfDers(), NUMBER_OF_BANDS, CAPACITY_DER_SECTION_HEIGHT);
        String filename = getTemplateFilename(dbAuction, dbSchedulingUnit, userLang);
        return writeFileAndGetDTO(workbook, filename);
    }

}
