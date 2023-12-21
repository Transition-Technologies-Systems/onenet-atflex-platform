package pl.com.tt.flex.server.service.auction.cmvc;

import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.cmvc.AuctionCmvcOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.service.AbstractService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link AuctionCmvcEntity}.
 */
public interface AuctionCmvcService extends AbstractService<AuctionCmvcEntity, AuctionCmvcDTO, Long> {

    void updateAuctionName(String auctionName, Long auctionId);

    boolean canCurrentLoggedUserAddNewBid(AuctionCmvcDTO auctionCmvcDTO, Long productId);

    AuctionStatus findAuctionStatusById(Long id);

    Long countByDeliveryDateAndProductName(Instant deliveryDateFrom, String productName);


    //********************************************************************************** OFFERS ************************************************************************************
	AuctionCmvcOfferDTO saveOffer(AuctionCmvcOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException;

    Optional<AuctionCmvcOfferDTO> findOfferById(Long id);

    void deleteOffer(Long offerId, AuctionStatus auctionStatus) throws ObjectValidationException;

    List<FspCompanyMinDTO> findFspsWithRegisteredPotentialsForAuction(Long auctionCmvcId);

    List<FlexPotentialMinDTO> findAllRegisteredFlexPotentialsForFspAndAuction(Long fspId, Long auctionCmvcId);
    //********************************************************************************** OFFERS ************************************************************************************
}
