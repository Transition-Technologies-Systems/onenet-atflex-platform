package pl.com.tt.flex.user.security.jwt;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;


@RequiredArgsConstructor
@Slf4j
public class JWTWebsocketInterceptor implements ChannelInterceptor {

  public static final String AUTHORIZATION_HEADER = "Authorization";
  public static final String BEARER_PREFIX = "Bearer ";
  private static final int BEARER_PREFIX_LENGTH = 7;

  private final TokenProvider tokenProvider;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor stompAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (stompAccessor != null && StompCommand.CONNECT.equals(stompAccessor.getCommand())) {
      String jwt = resolveToken(stompAccessor);
      if (StringUtils.hasText(jwt) && this.tokenProvider.validateToken(jwt)) {
        Authentication authentication = this.tokenProvider.getAuthentication(jwt);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        stompAccessor.setUser(authentication);
      }
    }
    return message;
  }

  private String resolveToken(StompHeaderAccessor accessor) {
    String bearerToken = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
    if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
      return bearerToken.substring(BEARER_PREFIX_LENGTH);
    }
    return null;
  }
}
