package pl.com.tt.flex.server.web.rest.websocket;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.model.service.dto.chat.message.ChatMessageDTO;
import pl.com.tt.flex.server.config.microservices.MicroservicesProxyConfiguration;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;

@FeignClient(value = "flex-user", configuration = MicroservicesProxyConfiguration.class)
public interface FlexUserRefreshViewWebsocketResource {

    @PostMapping(value = "/api/broadcast/refresh-view/auctions/cmvc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> postModifiedCmvcAuction(@RequestBody AuctionCmvcDTO auction);

    @PostMapping(value = "/api/broadcast/refresh-view/auctions/day-ahead", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> postModifiedDayAheadAuction(@RequestBody AuctionDayAheadDTO auction);

    @PostMapping(value = "/api/broadcast/refresh-view/auctions/offer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> postModifiedOffer(@RequestBody AuctionOfferDTO offerDTO);

    @PostMapping(value = "/api/broadcast/refresh-view/unit", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> postModifiedUnit(@RequestBody UnitDTO offerDTO);

    @PostMapping(value = "/api/broadcast/refresh-view/chat/{login}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> postChat(@PathVariable("login") String login, @RequestBody ChatDTO chat);

    @PostMapping(value = "/api/broadcast/refresh-view/chat/unread/{login}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> postUnreadMessagesNumber(@PathVariable("login") String login, @RequestBody Long unreadMessagesNumber);

    @PostMapping(value = "/api/broadcast/refresh-view/chat/{chatId}/message/{login}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> postChatMessage(@PathVariable("login") String login, @PathVariable("chatId") Long chatId, @RequestBody ChatMessageDTO message);

}
