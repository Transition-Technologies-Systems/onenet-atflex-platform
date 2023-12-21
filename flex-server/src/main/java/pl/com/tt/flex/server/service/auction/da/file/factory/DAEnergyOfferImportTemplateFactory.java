package pl.com.tt.flex.server.service.auction.da.file.factory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang3.Range;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;

public class DAEnergyOfferImportTemplateFactory extends AbstractDAOfferImportTemplateFactory {

    public static final int ENERGY_DER_SECTION_HEIGHT = 22;
    public static final int ENERGY_FIRST_DER_START_ROW = 23;
    public static final int SELF_SCHEDULE_ROW_OFFSET = 11;
    public static final int NUMBER_OF_BANDS = 21;
    public static final Range<Integer> BAND_RANGE = Range.between(-10, 10);

    public static FileDTO getEnergyOfferImportTemplate(AuctionDayAheadDTO dbAuction,
                                                       SchedulingUnitDTO dbSchedulingUnit,
                                                       Map<Long, Map<String, BigDecimal>> derSelfSchedules,
                                                       String userLang) throws IOException {
        XSSFWorkbook workbook = getWorkbook("energy", "energy", userLang);
        addDersToTemplate(dbSchedulingUnit, derSelfSchedules, workbook, ENERGY_DER_SECTION_HEIGHT, ENERGY_FIRST_DER_START_ROW, SELF_SCHEDULE_ROW_OFFSET);
        setFormulas(workbook, dbSchedulingUnit.getNumberOfDers(), NUMBER_OF_BANDS, ENERGY_DER_SECTION_HEIGHT);
        String filename = getTemplateFilename(dbAuction, dbSchedulingUnit, userLang);
        return writeFileAndGetDTO(workbook, filename);
    }

}
