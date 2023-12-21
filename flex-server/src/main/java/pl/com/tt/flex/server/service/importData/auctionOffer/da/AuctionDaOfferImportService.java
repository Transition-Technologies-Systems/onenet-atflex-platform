package pl.com.tt.flex.server.service.importData.auctionOffer.da;

import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferSchedulingUnitDTO;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface AuctionDaOfferImportService {

    AuctionOfferImportDataResult importBids(List<AuctionOfferSchedulingUnitDTO> daBids) throws JsonProcessingException;

}
