package pl.com.tt.flex.server.refreshView.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcMapper;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminRefreshViewWebsocketResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserRefreshViewWebsocketResource;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

@Component
@Slf4j
public class AuctionCmvcListener {

    private final AuctionCmvcMapper auctionCmvcMapper;
    private final FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource;
    private final FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource;

    public AuctionCmvcListener(AuctionCmvcMapper auctionCmvcMapper, @Lazy FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource,
                               @Lazy FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource) {
        this.auctionCmvcMapper = auctionCmvcMapper;
        this.adminRefreshViewWebsocketResource = adminRefreshViewWebsocketResource;
        this.userRefreshViewWebsocketResource = userRefreshViewWebsocketResource;
    }

    @PostPersist
    @Async
    public void onPostPersist(AuctionCmvcEntity auctionCmvcEntity) {
        log.info("onPostPersist() START - Send WebSocket messages with new auction CMVC [auctionId={}]", auctionCmvcEntity.getId());
        AuctionCmvcDTO auction = auctionCmvcMapper.toDto(auctionCmvcEntity);
        postPersistToAdminApp(auction);
        postPersistToUserApp(auction);
        log.info("onPostPersist() END - Send WebSocket messages with new auction CMVC [auctionId={}]", auctionCmvcEntity.getId());
    }

    @PostUpdate
    @Async
    public void onPostUpdate(AuctionCmvcEntity auctionCmvcEntity) {
        log.info("onPostUpdate() START - Send WebSocket messages with updated auction CMVC [auctionId={}]", auctionCmvcEntity.getId());
        AuctionCmvcDTO auction = auctionCmvcMapper.toDto(auctionCmvcEntity);
        postPersistToAdminApp(auction);
        postPersistToUserApp(auction);
        log.info("onPostUpdate() END - Send WebSocket messages with updated auction CMVC [auctionId={}]", auctionCmvcEntity.getId());
    }

    private void postPersistToAdminApp(AuctionCmvcDTO auction) {
        try {
            adminRefreshViewWebsocketResource.postModifiedCmvcAuction(auction);
        } catch (Exception e) {
            log.debug("postPersistToAdminApp() Error while post auction cmvc {} to {} app\n{}", auction.toString(), Constants.FLEX_ADMIN_APP_NAME, e.getMessage());
        }
    }

    private void postPersistToUserApp(AuctionCmvcDTO auction) {
        try {
            userRefreshViewWebsocketResource.postModifiedCmvcAuction(auction);
        } catch (Exception e) {
            log.debug("postPersistToUserApp() Error while post auction cmvc {} to {} app\n{}", auction.toString(), Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }
}
