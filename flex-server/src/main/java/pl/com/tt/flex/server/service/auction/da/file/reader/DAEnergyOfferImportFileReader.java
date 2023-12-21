package pl.com.tt.flex.server.service.auction.da.file.reader;

import static pl.com.tt.flex.server.service.auction.da.file.factory.DAEnergyOfferImportTemplateFactory.BAND_RANGE;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DAEnergyOfferImportTemplateFactory.ENERGY_DER_SECTION_HEIGHT;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DAEnergyOfferImportTemplateFactory.ENERGY_FIRST_DER_START_ROW;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DAEnergyOfferImportTemplateFactory.SELF_SCHEDULE_ROW_OFFSET;

import java.time.Instant;

import org.apache.commons.lang3.Range;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;

public class DAEnergyOfferImportFileReader extends AbstractDAOfferImportFileReader {

    public static AuctionDayAheadOfferDTO readEnergyOffer(XSSFWorkbook workbook,
                                                          AuctionDayAheadDTO dbAuction,
                                                          SchedulingUnitDTO dbSchedulingUnit,
                                                          AuctionDayAheadOfferDTO dbOffer,
                                                          Range<Instant> deliveryPeriod) throws ObjectValidationException {
        return readDers(workbook,
            BAND_RANGE,
            dbSchedulingUnit,
            dbAuction,
            dbOffer,
            ENERGY_DER_SECTION_HEIGHT,
            SELF_SCHEDULE_ROW_OFFSET,
            ENERGY_FIRST_DER_START_ROW,
            deliveryPeriod);
    }

}
