package pl.com.tt.flex.user.refreshView.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.model.service.dto.chat.message.ChatMessageDTO;
import pl.com.tt.flex.user.refreshView.OfferFilterDTO;
import pl.com.tt.flex.user.refreshView.RememberUserFilterDetails;
import pl.com.tt.flex.user.refreshView.util.OfferFilterUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static pl.com.tt.flex.user.config.WebsocketConfiguration.REFRESH_VIEW_DESTINATIONS_PREFIX;

@Service
@Slf4j
public class RefreshViewServiceImpl implements RefreshViewService {

  public static final String TOPIC_REFRESH_OFFER = REFRESH_VIEW_DESTINATIONS_PREFIX + "/auctions/offer";
  public static final String TOPIC_REFRESH_AUCTION_DA = REFRESH_VIEW_DESTINATIONS_PREFIX + "/auctions/day-ahead";
  public static final String TOPIC_REFRESH_AUCTION_CMVC = REFRESH_VIEW_DESTINATIONS_PREFIX + "/auctions/cmvc";
  public static final String TOPIC_REFRESH_CHAT_FORMAT = REFRESH_VIEW_DESTINATIONS_PREFIX + "/chat/%s";
  public static final String TOPIC_REFRESH_CHAT_MESSAGE_FORMAT = REFRESH_VIEW_DESTINATIONS_PREFIX + "/chat/%s/message/%s";
  public static final String TOPIC_REFRESH_CHAT_UNREAD_COUNT_FORMAT = REFRESH_VIEW_DESTINATIONS_PREFIX + "/chat/unread/%s";


  private final Map<String, RememberUserFilterDetails> rememberedUserFilter = Maps.newConcurrentMap();
  private final SimpMessageSendingOperations messagingTemplate;
  private final ObjectMapper objectMapper;

  public RefreshViewServiceImpl(SimpMessageSendingOperations messagingTemplate, ObjectMapper objectMapper) {
    this.messagingTemplate = messagingTemplate;
    this.objectMapper = objectMapper;
  }

  @Override
  public void rememberUserFilter(Message<OfferFilterDTO> criteria) {
    StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(criteria, StompHeaderAccessor.class);
    if (accessor != null && accessor.getUser() != null && criteria.getPayload().getFspId() != null) {
      UsernamePasswordAuthenticationToken user = (UsernamePasswordAuthenticationToken) accessor.getUser();
      log.debug("rememberUserView() SessionId = {}, payload = {}", accessor.getSessionId(), criteria.getPayload());
      RememberUserFilterDetails rememberUserFilterDetails = new RememberUserFilterDetails();
      rememberUserFilterDetails.setOfferFilterDTO(criteria.getPayload());
      rememberUserFilterDetails.setLogin(user.getName());
      rememberUserFilterDetails.setFspId(criteria.getPayload().getFspId());
      rememberedUserFilter.put(accessor.getSessionId(), rememberUserFilterDetails);
      log.debug("rememberUserView() saved filter {} by the user {} [sessionId={}]", rememberUserFilterDetails, user.getName(), accessor.getSessionId());
    } else {
      log.debug("rememberUserView() not saved filter for criteria: {}", criteria);
    }
  }

  @Override
  public void removeUserFilter(String sessionId) {
    rememberedUserFilter.remove(sessionId);
    log.debug("removeUserFilter() sessionId {}", sessionId);
  }

  @Override
  public Message<?> processAndFilterOfferUpdate(Message<?> message, SimpMessageHeaderAccessor accessor) throws IOException {
    RememberUserFilterDetails rememberUserFilterDetails = rememberedUserFilter.get(accessor.getSessionId());
    //jezeli po id sesji (SessionId) nie znaleziono zapisanych filtrow to wiadomosc nie zostaje wyslana
    if (Objects.isNull(rememberUserFilterDetails)) {
      log.debug("processAndFilterOfferUpdate() Not found user saved filter by the sessionId {}", accessor.getSessionId());
      return null;
    }

    AuctionOfferDTO auctionOfferDTO = objectMapper.readValue((byte[]) message.getPayload(), AuctionOfferDTO.class);
    //jezeli uzytkownik nie ma uprawnien do podgladu oferty to wiadomosc nie zostaje wyslana
    if (!userHasPermissionToViewOffer(auctionOfferDTO, rememberUserFilterDetails.getFspId())) {
      log.debug("processAndFilterOfferUpdate() User {} has not permission to view offer with id {}", rememberUserFilterDetails.getLogin(), auctionOfferDTO.getId());
      return null;
    }
    //jezeli dodawana oferta nie jest zgodna z zapisamym filtrem to wiadomosc nie zostaje wyslana
    if (!OfferFilterUtil.isMatching(rememberUserFilterDetails.getOfferFilterDTO(), auctionOfferDTO)) {
      log.debug("processAndFilterOfferUpdate() Filter saved by the user {} not match to added offer with id {}", rememberUserFilterDetails.getLogin(), auctionOfferDTO.getId());
      return null;
    }
    log.debug("processAndFilterOfferUpdate() Filter saved by the user {} match to added offer with id {}", rememberUserFilterDetails.getLogin(), auctionOfferDTO.getId());
    return new GenericMessage<>(objectMapper.writeValueAsBytes(auctionOfferDTO), message.getHeaders());
  }

  private boolean userHasPermissionToViewOffer(AuctionOfferDTO auctionOfferDTO, Long fspId) {
    if (Objects.nonNull(auctionOfferDTO.getSchedulingUnit())) {
      return auctionOfferDTO.getSchedulingUnit().getBsp().getId().equals(fspId);
    }
    if (Objects.nonNull(auctionOfferDTO.getFlexPotential())) {
      return auctionOfferDTO.getFlexPotential().getFsp().getId().equals(fspId);
    }
    return true;
  }

  @Override
  public void postModifiedDayAheadAuctions(AuctionDayAheadDTO auctions) {
    log.debug("WebSocket postModifiedDayAheadAuctions() -> Start of sending message {} on topic {}", auctions, TOPIC_REFRESH_AUCTION_DA);
    messagingTemplate.convertAndSend(TOPIC_REFRESH_AUCTION_DA, auctions);
    log.debug("WebSocket postModifiedDayAheadAuctions() -> End of sending message {} on topic {}", auctions, TOPIC_REFRESH_AUCTION_DA);
  }

  @Override
  public void postModifiedCmvcAuctions(AuctionCmvcDTO auctions) {
    log.debug("WebSocket postModifiedCmvcAuctions() -> Start of sending message {} on topic {}", auctions, TOPIC_REFRESH_AUCTION_CMVC);
    messagingTemplate.convertAndSend(TOPIC_REFRESH_AUCTION_CMVC, auctions);
    log.debug("WebSocket postModifiedCmvcAuctions() -> End of sending message {} on topic {}", auctions, TOPIC_REFRESH_AUCTION_CMVC);
  }

  @Override
  public void postModifiedOffer(AuctionOfferDTO offer) {
    log.debug("WebSocket postModifiedOffer() -> Start of sending message {} on topic {}", offer, TOPIC_REFRESH_OFFER);
    messagingTemplate.convertAndSend(TOPIC_REFRESH_OFFER, offer);
    log.debug("WebSocket postModifiedOffer() -> End of sending message {} on topic {}", offer, TOPIC_REFRESH_OFFER);
  }

  @Override
  public void postChat(String login, ChatDTO chat) {
    log.debug("WebSocket postChat() -> Start of sending message {} on topic {}", chat, TOPIC_REFRESH_OFFER);
    messagingTemplate.convertAndSend(String.format(TOPIC_REFRESH_CHAT_FORMAT, login), chat);
    log.debug("WebSocket postChat() -> End of sending message {} on topic {}", chat, TOPIC_REFRESH_OFFER);
  }

  @Override
  public void postUnreadMessagesNumber(String login, Long unreadMessagesNumber) {
    log.debug("WebSocket postUnreadMessagesNumber() -> Start of sending message {} on topic {}", unreadMessagesNumber, TOPIC_REFRESH_OFFER);
    messagingTemplate.convertAndSend(String.format(TOPIC_REFRESH_CHAT_UNREAD_COUNT_FORMAT, login), unreadMessagesNumber);
    log.debug("WebSocket postUnreadMessagesNumber() -> End of sending message {} on topic {}", unreadMessagesNumber, TOPIC_REFRESH_OFFER);
  }

  @Override
  public void postChatMessage(String login, Long chatId, ChatMessageDTO message) {
    log.debug("WebSocket postChatMessage() -> Start of sending message {} on topic {}", message, TOPIC_REFRESH_OFFER);
    messagingTemplate.convertAndSend(String.format(TOPIC_REFRESH_CHAT_MESSAGE_FORMAT, chatId, login), message);
    log.debug("WebSocket postChatMessage() -> End of sending message {} on topic {}", message, TOPIC_REFRESH_OFFER);
  }

}
