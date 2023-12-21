package pl.com.tt.flex.server.refreshView.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadMapper;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminRefreshViewWebsocketResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserRefreshViewWebsocketResource;

import javax.persistence.PostUpdate;

@Component
@Slf4j
public class AuctionDayAheadListener {

    private final AuctionDayAheadMapper auctionDayAheadMapper;
    private final FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource;
    private final FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource;

    public AuctionDayAheadListener(AuctionDayAheadMapper auctionDayAheadMapper, @Lazy FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource,
                                   @Lazy FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource) {
        this.auctionDayAheadMapper = auctionDayAheadMapper;
        this.adminRefreshViewWebsocketResource = adminRefreshViewWebsocketResource;
        this.userRefreshViewWebsocketResource = userRefreshViewWebsocketResource;
    }

    @PostUpdate
    @Async
    public void onPostUpdate(AuctionDayAheadEntity auctionDayAheadEntity) {
        if (!auctionDayAheadEntity.getStatus().equals(AuctionStatus.SCHEDULED)) {
            log.info("onPostUpdate() START - Send WebSocket messages with updated auction DayAhaed [auctionId={}]", auctionDayAheadEntity.getId());
            AuctionDayAheadDTO auction = auctionDayAheadMapper.toDto(auctionDayAheadEntity);
            postAuctionDayAheadToAdminApp(auction);
            postAuctionDayAheadToUserApp(auction);
            log.info("onPostUpdate() END - Send WebSocket messages with updated auction DayAhaed [auctionId={}]", auctionDayAheadEntity.getId());
        }
    }

    private void postAuctionDayAheadToAdminApp(AuctionDayAheadDTO auction) {
        try {
            adminRefreshViewWebsocketResource.postModifiedDayAheadAuction(auction);
        } catch (Exception e) {
            log.debug("postAuctionDayAheadToAdminApp() Error while post auction DA {} to {} app\n{}", auction.toString(), Constants.FLEX_ADMIN_APP_NAME, e.getMessage());
        }
    }

    private void postAuctionDayAheadToUserApp(AuctionDayAheadDTO auction) {
        try {
            userRefreshViewWebsocketResource.postModifiedDayAheadAuction(auction);
        } catch (Exception e) {
            log.debug("postAuctionDayAheadToUserApp() Error while post auction DA {} to {} app\n{}", auction.toString(), Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }
}
