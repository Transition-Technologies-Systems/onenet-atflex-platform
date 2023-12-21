package pl.com.tt.flex.server.refreshView.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcViewEntity;
import pl.com.tt.flex.server.repository.auction.cmvc.AuctionCmvcViewRepository;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcViewMapper;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminRefreshViewWebsocketResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserRefreshViewWebsocketResource;
import pl.com.tt.flex.server.util.InstantUtil;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("application.refresh-view.auction-cmvc-status.enabled")
public class AuctionCmvcStatusScheduler {

    private final AuctionCmvcViewRepository auctionCmvcViewRepository;
    private final AuctionCmvcViewMapper auctionCmvcViewMapper;
    private final FlexAdminRefreshViewWebsocketResource flexAdminRefreshViewWebsocketResource;
    private final FlexUserRefreshViewWebsocketResource userRefreshViewWebsocketResource;

    /**
     * The methods searches the auctions whose start or end auction dates are in given range (Auction status changes)
     * Found auctions are sent to the WebSocket.
     */
    @Scheduled(cron = "${application.refresh-view.auction-cmvc-status.cron}")
    public void updateAuctionCmvcStatus() {
        log.debug("updateAuctionCmvcStatus() START - Send WebSocket messages with updated auctions CMVC");
        Instant now = InstantUtil.now();
        List<AuctionCmvcViewEntity> statusChanges = auctionCmvcViewRepository.findAllByGateTimeInRange(now.minusSeconds(60), now.plusSeconds(60));
        statusChanges.stream().map(auctionCmvcViewMapper::toDto).forEach(auction -> {
            log.debug("updateAuctionCmvcStatus() send message with auction Cmvc with id={}, status={}", auction.getId(), auction.getStatus());
            flexAdminRefreshViewWebsocketResource.postModifiedCmvcAuction(auction);
            userRefreshViewWebsocketResource.postModifiedCmvcAuction(auction);
        });
        log.debug("updateAuctionCmvcStatus() END - Send WebSocket messages with updated auctions CMVC");
    }
}
