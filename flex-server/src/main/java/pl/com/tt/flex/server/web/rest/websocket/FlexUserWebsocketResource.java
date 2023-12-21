package pl.com.tt.flex.server.web.rest.websocket;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionReminderType;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryUpdateDTO;
import pl.com.tt.flex.server.config.microservices.MicroservicesProxyConfiguration;

@FeignClient(value = "flex-user", configuration = MicroservicesProxyConfiguration.class)
public interface FlexUserWebsocketResource {

    @PostMapping(value = "/api/broadcast/{login}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity postNewEvent(@PathVariable("login") String login, @RequestBody String event);

    @PostMapping(value = "/api/broadcast/dictionary-update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity postDictionaryUpdate(@RequestBody DictionaryUpdateDTO dictionaryUpdate);

    @PostMapping(value = "/api/broadcast/auction-reminder/{login}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity postAuctionReminder(@PathVariable("login") String login, @RequestBody AuctionReminderType auctionReminderType);
}
