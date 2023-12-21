package pl.com.tt.flex.server.service.importData.auctionOffer.mapper;

import org.mapstruct.Mapper;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDTO;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;

@Mapper(componentModel = "spring", uses = {})
public interface AuctionOfferImportMapper {

    AuctionOfferImportDTO toDto(AuctionOfferImportDataResult result);
}
