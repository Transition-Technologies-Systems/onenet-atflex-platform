package pl.com.tt.flex.server.refreshView.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcOfferMapper;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminRefreshViewWebsocketResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserRefreshViewWebsocketResource;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

@Component
@Slf4j
public class AuctionCmvcOfferListener {

    private final AuctionCmvcOfferMapper auctionOfferMapper;
    private final FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource;
    private final FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource;

    public AuctionCmvcOfferListener(AuctionCmvcOfferMapper auctionOfferMapper, @Lazy FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource,
                                    @Lazy FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource) {
        this.auctionOfferMapper = auctionOfferMapper;
        this.adminRefreshViewWebsocketResource = adminRefreshViewWebsocketResource;
        this.userRefreshViewWebsocketResource = userRefreshViewWebsocketResource;
    }

    @PostPersist
    @Async
    public void onPostPersist(AuctionCmvcOfferEntity auctionOfferEntity) {
        log.info("onPostPersist() START - Send WebSocket messages with CMVC new offer [offerId={}]", auctionOfferEntity.getId());
        AuctionOfferDTO offerDTO = auctionOfferMapper.toDto(auctionOfferEntity);
        postCmvcOfferToAdminApp(offerDTO);
        postCmvcOfferToUserApp(offerDTO);
        log.info("onPostPersist() END - Send WebSocket messages with CMVC new offer [offerId={}]", auctionOfferEntity.getId());
    }

    @PostUpdate
    @Async
    public void onPostUpdate(AuctionCmvcOfferEntity auctionOfferEntity) {
        log.info("onPostUpdate() START - Send WebSocket messages with CMVC updated offer [offerId={}]", auctionOfferEntity.getId());
        AuctionOfferDTO offerDTO = auctionOfferMapper.toDto(auctionOfferEntity);
        postCmvcOfferToAdminApp(offerDTO);
        postCmvcOfferToUserApp(offerDTO);
        log.info("onPostUpdate() END - Send WebSocket messages with CMVC updated offer [offerId={}]", auctionOfferEntity.getId());
    }

    private void postCmvcOfferToAdminApp(AuctionOfferDTO offerDTO) {
        try {
            adminRefreshViewWebsocketResource.postModifiedOffer(offerDTO);
        } catch (Exception e) {
            log.debug("postCmvcOfferToAdminApp() Error while post cmvc offer {} to {} app\n{}", offerDTO.toString(), Constants.FLEX_ADMIN_APP_NAME, e.getMessage());
        }
    }

    private void postCmvcOfferToUserApp(AuctionOfferDTO offerDTO) {
        try {
            userRefreshViewWebsocketResource.postModifiedOffer(offerDTO);
        } catch (Exception e) {
            log.debug("postCmvcOfferToUserApp() Error while post cmvc offer {} to {} app\n{}", offerDTO.toString(), Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }
}
