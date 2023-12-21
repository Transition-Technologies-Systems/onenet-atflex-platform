package pl.com.tt.flex.server.service.importData.auctionOffer.da;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferSetoImportData;

public interface AuctionDaSetoOfferImportService {

    AuctionOfferImportDataResult importBids(List<AuctionOfferSetoImportData> daBids) throws JsonProcessingException;

}
