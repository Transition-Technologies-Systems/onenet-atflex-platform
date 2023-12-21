package pl.com.tt.flex.server.service.importData.auctionOffer.cmvc;

import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportData;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;

import java.util.List;

public interface AuctionCmvcOfferImportService {

    AuctionOfferImportDataResult importBids(List<AuctionOfferImportData> cmvcBids);

}
