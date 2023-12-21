package pl.com.tt.flex.server.web.rest.auction.offer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferService;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferViewQueryService;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.user.UserService;

/**
 * Common REST controller for managing {@link AuctionCmvcOfferEntity}.
 */
@Slf4j
@RestController
@RequestMapping("/api/auctions")
public class AuctionOfferResource {

    protected static final String ENTITY_NAME = "auctionOffer";

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    protected final AuctionOfferService offerService;

    protected final AuctionOfferViewQueryService offerViewQueryService;

    protected final UserService userService;

    protected final FspService fspService;

    public AuctionOfferResource(AuctionOfferService offerService, AuctionOfferViewQueryService offerViewQueryService,
                                UserService userService, FspService fspService) {
        this.offerService = offerService;
        this.offerViewQueryService = offerViewQueryService;
        this.userService = userService;
        this.fspService = fspService;
    }
}
