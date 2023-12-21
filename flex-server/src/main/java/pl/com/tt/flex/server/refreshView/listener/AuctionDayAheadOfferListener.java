package pl.com.tt.flex.server.refreshView.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadOfferMapper;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminRefreshViewWebsocketResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserRefreshViewWebsocketResource;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

@Component
@Slf4j
public class AuctionDayAheadOfferListener {

    private final AuctionDayAheadOfferMapper auctionOfferMapper;
    private final FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource;
    private final FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource;

    public AuctionDayAheadOfferListener(AuctionDayAheadOfferMapper auctionOfferMapper, @Lazy FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource,
                                        @Lazy FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource) {
        this.auctionOfferMapper = auctionOfferMapper;
        this.adminRefreshViewWebsocketResource = adminRefreshViewWebsocketResource;
        this.userRefreshViewWebsocketResource = userRefreshViewWebsocketResource;
    }

    @PostPersist
    @Async
    public void onPostPersist(AuctionDayAheadOfferEntity auctionOfferEntity) {
        log.info("onPostPersist() START - Send WebSocket messages with DA new offer [offerId={}]", auctionOfferEntity.getId());
        AuctionOfferDTO offerDTO = auctionOfferMapper.toDto(auctionOfferEntity);
        postAuctionDayAheadOfferToAdminApp(offerDTO);
        postAuctionDayAheadOfferToUserApp(offerDTO);
        log.info("onPostPersist() END - Send WebSocket messages with DA new offer [offerId={}]", auctionOfferEntity.getId());
    }


    @PostUpdate
    @Async
    public void onPostUpdate(AuctionDayAheadOfferEntity auctionOfferEntity) {
        log.info("onPostUpdate() START - Send WebSocket messages DA with updated offer [offerId={}]", auctionOfferEntity.getId());
        AuctionOfferDTO offerDTO = auctionOfferMapper.toDto(auctionOfferEntity);
        postAuctionDayAheadOfferToAdminApp(offerDTO);
        postAuctionDayAheadOfferToUserApp(offerDTO);
        log.info("onPostUpdate() END - Send WebSocket messages with DA updated offer [offerId={}]", auctionOfferEntity.getId());
    }

    private void postAuctionDayAheadOfferToAdminApp(AuctionOfferDTO offerDTO) {
        try {
            adminRefreshViewWebsocketResource.postModifiedOffer(offerDTO);
        } catch (Exception e) {
            log.debug("postAuctionDayAheadOfferToAdminApp() Error while post auction DA offer {} to {} app\n{}", offerDTO.toString(), Constants.FLEX_ADMIN_APP_NAME, e.getMessage());
        }
    }

    private void postAuctionDayAheadOfferToUserApp(AuctionOfferDTO offerDTO) {
        try {
            userRefreshViewWebsocketResource.postModifiedOffer(offerDTO);
        } catch (Exception e) {
            log.debug("postAuctionDayAheadOfferToUserApp() Error while post auction DA offer {} to {} app\n{}", offerDTO.toString(), Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }
}
