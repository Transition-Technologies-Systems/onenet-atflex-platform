package pl.com.tt.flex.user.refreshView.service;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import pl.com.tt.flex.user.refreshView.OfferFilterDTO;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.chat.ChatDTO;
import pl.com.tt.flex.model.service.dto.chat.message.ChatMessageDTO;

import java.io.IOException;

public interface RefreshViewService {

    void rememberUserFilter(Message<OfferFilterDTO> criteria);

    void removeUserFilter(String sessionId);

    Message<?> processAndFilterOfferUpdate(Message<?> message, SimpMessageHeaderAccessor accessor) throws IOException;

    void postModifiedDayAheadAuctions(AuctionDayAheadDTO auctionDayAheadDTO);

    void postModifiedCmvcAuctions(AuctionCmvcDTO auctionCmvcDTO);

    void postModifiedOffer(AuctionOfferDTO auctionOfferDTO);

    void postChat(String login, ChatDTO chat);

    void postUnreadMessagesNumber(String login, Long unreadMessagesNumber);

    void postChatMessage(String login, Long chatId, ChatMessageDTO message);
}
