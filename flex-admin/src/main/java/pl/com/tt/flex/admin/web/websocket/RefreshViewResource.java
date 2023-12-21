package pl.com.tt.flex.admin.web.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.com.tt.flex.admin.refreshView.OfferFilterDTO;
import pl.com.tt.flex.admin.refreshView.service.RefreshViewService;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationViewDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.model.service.dto.chat.message.ChatMessageDTO;
import pl.com.tt.flex.model.service.dto.file.FileContentDTO;

@Controller
@Slf4j
@RequestMapping("/api/broadcast/refresh-view")
public class RefreshViewResource {

  private final RefreshViewService refreshViewService;

  public RefreshViewResource(RefreshViewService refreshViewService) {
    this.refreshViewService = refreshViewService;
  }

  @MessageMapping("/refresh-view/offers")
  public void handleOfferFilter(Message<OfferFilterDTO> criteria) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(criteria, StompHeaderAccessor.class);
    log.info("handleOfferFilter() Request to save user {} filter", accessor.getUser().getName());
    refreshViewService.rememberUserFilter(criteria);
  }

  @PostMapping(value = "/auctions/cmvc", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> postModifiedCmvcAuction(@RequestBody AuctionCmvcDTO auction) {
    log.debug(String.format("WebSocket postModifiedCmvcAuctions() -> Prepare message to send %s", auction));
    refreshViewService.postModifiedCmvcAuctions(auction);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/auctions/day-ahead", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> postModifiedDayAheadAuction(@RequestBody AuctionDayAheadDTO auction) {
    log.debug(String.format("WebSocket postModifiedDayAheadAuctions() -> Prepare message to send %s", auction));
    refreshViewService.postModifiedDayAheadAuctions(auction);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/auctions/offer", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> postModifiedOffer(@RequestBody AuctionOfferDTO offerDTO) {
    log.debug(String.format("WebSocket postModifiedOfferAuction() -> Prepare message to send %s", offerDTO));
    refreshViewService.postModifiedOffer(offerDTO);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/auctions/algorithm-evaluations", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> postModifiedAlgorithmEvaluation(@RequestBody AlgorithmEvaluationViewDTO algorithmEvaluationDTO) {
    log.debug(String.format("WebSocket postModifiedAlgorithmEvaluation() -> Prepare message to send %s", algorithmEvaluationDTO));
    refreshViewService.postModifiedAlgorithmEvaluation(algorithmEvaluationDTO);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/auctions/algorithm-evaluations/logs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> postAlgorithmEvaluationLogs(@RequestParam("evaluationID") Long evaluationId, @RequestBody FileContentDTO fileContentDTO) {
    log.debug(String.format("WebSocket postAlgorithmEvaluationLogs() -> Prepare message to send %s", fileContentDTO));
    refreshViewService.postAlgorithmEvaluationLogs(evaluationId, fileContentDTO);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/auctions/algorithm-evaluations/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> postAlgorithmEvaluationStatus(@RequestBody MinimalDTO<Long, AlgorithmStatus> evaluationStatus) {
    log.debug(String.format("WebSocket postAlgorithmEvaluationStatus() -> Prepare message to send %s", evaluationStatus));
    refreshViewService.postAlgorithmEvaluationStatus(evaluationStatus);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/chat/{login}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> postChat(@PathVariable String login, @RequestBody ChatDTO chatDTO) {
    log.debug(String.format("WebSocket postChat() -> Prepare message to send %s", chatDTO));
    refreshViewService.postChat(login, chatDTO);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/chat/unread/{login}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> postUnreadMessagesNumber(@PathVariable String login, @RequestBody Long unreadMessagesNumber) {
    log.debug(String.format("WebSocket postUnreadMessagesNumber() -> Prepare message to send %s", unreadMessagesNumber));
    refreshViewService.postUnreadMessagesNumber(login, unreadMessagesNumber);
    return ResponseEntity.ok().build();
  }

  @PostMapping(value = "/chat/{chatId}/message/{login}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> postChatMessage(@PathVariable Long chatId, @PathVariable String login, @RequestBody ChatMessageDTO chatMessageDTO) {
    log.debug(String.format("WebSocket postChatMessage() -> Prepare message to send %s", chatMessageDTO));
    refreshViewService.postChatMessage(login, chatId, chatMessageDTO);
    return ResponseEntity.ok().build();
  }
}
