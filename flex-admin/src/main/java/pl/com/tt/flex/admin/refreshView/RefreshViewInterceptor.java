package pl.com.tt.flex.admin.refreshView;

import lombok.SneakyThrows;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import pl.com.tt.flex.admin.refreshView.service.RefreshViewService;
import pl.com.tt.flex.admin.refreshView.service.RefreshViewServiceImpl;

public class RefreshViewInterceptor implements ChannelInterceptor {

  private final RefreshViewService refreshViewService;

  public RefreshViewInterceptor(RefreshViewService refreshViewService) {
    this.refreshViewService = refreshViewService;
  }

  @SneakyThrows
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    SimpMessageHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);

    if (accessor != null && SimpMessageType.DISCONNECT_ACK.equals(accessor.getMessageType())) {
      refreshViewService.removeUserFilter(accessor.getSessionId());
    }

    if (accessor != null && SimpMessageType.MESSAGE.equals(accessor.getMessageType())
      && (RefreshViewServiceImpl.TOPIC_REFRESH_OFFER).equals(accessor.getDestination())) {
      return refreshViewService.processAndFilterOfferUpdate(message, accessor);
    }
    return message;
  }
}
