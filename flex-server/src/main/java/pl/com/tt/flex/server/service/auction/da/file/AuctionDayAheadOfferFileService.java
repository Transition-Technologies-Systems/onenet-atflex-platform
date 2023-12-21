package pl.com.tt.flex.server.service.auction.da.file;

import java.io.IOException;
import java.time.Instant;

import org.apache.commons.lang3.Range;
import org.springframework.web.multipart.MultipartFile;

import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;

public interface AuctionDayAheadOfferFileService {

    FileDTO getOfferImportTemplate(Long auctionId, Long schedulingUnitId, Long fspId) throws IOException;

    AuctionDayAheadOfferDTO importDayAheadOffer(MultipartFile multipartFile, AuctionDayAheadDTO dbAction, SchedulingUnitDTO dbSchedulingUnit, Long offerId, Range<Instant> deliveryPeriod) throws ObjectValidationException, IOException;

}
