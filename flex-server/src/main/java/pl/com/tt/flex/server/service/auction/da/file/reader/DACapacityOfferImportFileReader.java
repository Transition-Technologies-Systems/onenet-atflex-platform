package pl.com.tt.flex.server.service.auction.da.file.reader;

import static pl.com.tt.flex.model.service.dto.product.type.Direction.DOWN;
import static pl.com.tt.flex.model.service.dto.product.type.Direction.UP;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DACapacityOfferImportTemplateFactory.CAPACITY_DER_SECTION_HEIGHT;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DACapacityOfferImportTemplateFactory.CAPACITY_FIRST_DER_START_ROW;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DACapacityOfferImportTemplateFactory.DOWN_BAND_RANGE;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DACapacityOfferImportTemplateFactory.DOWN_SELF_SCHEDULE_ROW_OFFSET;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DACapacityOfferImportTemplateFactory.UP_BAND_RANGE;
import static pl.com.tt.flex.server.service.auction.da.file.factory.DACapacityOfferImportTemplateFactory.UP_SELF_SCHEDULE_ROW_OFFSET;

import java.time.Instant;

import org.apache.commons.lang3.Range;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;

public class DACapacityOfferImportFileReader extends AbstractDAOfferImportFileReader {

    public static AuctionDayAheadOfferDTO readCapacityOffer(AuctionDayAheadDTO dbAuction,
                                                            SchedulingUnitDTO dbSchedulingUnit,
                                                            XSSFWorkbook workbook,
                                                            AuctionDayAheadOfferDTO dbOffer,
                                                            Range<Instant> deliveryPeriod,
                                                            Direction productDirection) throws ObjectValidationException {
        if (productDirection.equals(UP)) {
            return readCapacityUpOffer(workbook, dbAuction, dbSchedulingUnit, dbOffer, deliveryPeriod);
        } else if (productDirection.equals(DOWN)) {
            return readCapacityDownOffer(workbook, dbAuction, dbSchedulingUnit, dbOffer, deliveryPeriod);
        }
        throw new IllegalStateException("Unsupported product direction");
    }

    private static AuctionDayAheadOfferDTO readCapacityUpOffer(XSSFWorkbook workbook,
                                                               AuctionDayAheadDTO dbAuction,
                                                               SchedulingUnitDTO dbSchedulingUnit,
                                                               AuctionDayAheadOfferDTO dbOffer,
                                                               Range<Instant> deliveryPeriod) throws ObjectValidationException {
        return readDers(workbook,
            UP_BAND_RANGE,
            dbSchedulingUnit,
            dbAuction,
            dbOffer,
            CAPACITY_DER_SECTION_HEIGHT,
            UP_SELF_SCHEDULE_ROW_OFFSET,
            CAPACITY_FIRST_DER_START_ROW,
            deliveryPeriod);
    }

    private static AuctionDayAheadOfferDTO readCapacityDownOffer(XSSFWorkbook workbook,
                                                                 AuctionDayAheadDTO dbAuction,
                                                                 SchedulingUnitDTO dbSchedulingUnit,
                                                                 AuctionDayAheadOfferDTO dbOffer,
                                                                 Range<Instant> deliveryPeriod) throws ObjectValidationException {
        return readDers(workbook,
            DOWN_BAND_RANGE,
            dbSchedulingUnit,
            dbAuction,
            dbOffer,
            CAPACITY_DER_SECTION_HEIGHT,
            DOWN_SELF_SCHEDULE_ROW_OFFSET,
            CAPACITY_FIRST_DER_START_ROW,
            deliveryPeriod);
    }

}
