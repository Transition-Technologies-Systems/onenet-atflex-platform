package pl.com.tt.flex.server.refreshView.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadViewEntity;
import pl.com.tt.flex.server.repository.auction.da.AuctionDayAheadViewRepository;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadViewMapper;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminRefreshViewWebsocketResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserRefreshViewWebsocketResource;
import pl.com.tt.flex.server.util.InstantUtil;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("application.refresh-view.auction-dayAhead-status.enabled")
public class AuctionDayAheadStatusScheduler {

    private final AuctionDayAheadViewRepository auctionDayAheadViewRepository;
    private final AuctionDayAheadViewMapper auctionDayAheadViewMapper;
    private final FlexAdminRefreshViewWebsocketResource flexAdminRefreshViewWebsocketResource;
    private final FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource;

    /**
     * The methods searches the auctions whose start or end auction dates are in given range (Auction status changes)
     * Found auctions are sent to the WebSocket.
     */
    @Scheduled(cron = "${application.refresh-view.auction-dayAhead-status.cron}")
    public void updateAuctionDayAheadStatus() {
        log.debug("updateAuctionDayAheadStatus() START - Send WebSocket messages with updated auctions Day Ahead");
        Instant now = InstantUtil.now();
        List<AuctionDayAheadViewEntity> statusChanges = auctionDayAheadViewRepository.findAllByGateTimeInRange(now.minusSeconds(60), now.plusSeconds(60));
        statusChanges.stream().map(auctionDayAheadViewMapper::toDto).forEach(auction -> {
            log.debug("updateAuctionDayAheadStatus() send message with auction DayAhead with id={}, status={}", auction.getId(), auction.getStatus());
            flexAdminRefreshViewWebsocketResource.postModifiedDayAheadAuction(auction);
            userRefreshViewWebsocketResource.postModifiedDayAheadAuction(auction);
        });
        log.debug("updateAuctionDayAheadStatus() END - Send WebSocket messages with updated auctions Day Ahead");
    }
}
