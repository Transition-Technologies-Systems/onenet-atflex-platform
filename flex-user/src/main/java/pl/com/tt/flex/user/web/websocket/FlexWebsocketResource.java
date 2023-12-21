package pl.com.tt.flex.user.web.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionReminderType;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryUpdateDTO;

@RestController
@RequestMapping("/api/broadcast")
public class FlexWebsocketResource {

  private final Logger log = LoggerFactory.getLogger(FlexWebsocketResource.class);
  private final SimpMessageSendingOperations messagingTemplate;

  public FlexWebsocketResource(SimpMessageSendingOperations messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  @PostMapping(value = "/{login}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity postNewEvent(@PathVariable String login, @RequestBody String event) {
    log.debug(String.format("WebSocket -> Prepare message for user %s to send %s", login, event));
    messagingTemplate.convertAndSend(String.format("/topic/%s/events", login), event);
    return ResponseEntity.ok(null);
  }

  @PostMapping(value = "/dictionary-update", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> postDictionaryUpdate(@RequestBody DictionaryUpdateDTO dictionaryUpdate) {
    log.debug(String.format("WebSocket -> Send information about dictionary has been updated [dictionary type: %s]", dictionaryUpdate.getType()));
    messagingTemplate.convertAndSend("/topic/dictionary-update", dictionaryUpdate);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/auction-reminder/{login}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> postAuctionReminder(@PathVariable String login, @RequestBody AuctionReminderType auctionReminderType) {
    log.debug(String.format("WebSocket -> Send auction reminder of type: %s to user: %s", auctionReminderType, login));
    messagingTemplate.convertAndSend(String.format("/topic/%s/reminder", login), auctionReminderType);
    return ResponseEntity.ok().build();
  }
}
