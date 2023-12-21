package pl.com.tt.flex.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import pl.com.tt.flex.admin.refreshView.RefreshViewInterceptor;
import pl.com.tt.flex.admin.refreshView.service.RefreshViewService;
import pl.com.tt.flex.admin.security.jwt.JWTWebsocketInterceptor;
import pl.com.tt.flex.admin.security.jwt.TokenProvider;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguration implements WebSocketMessageBrokerConfigurer {

  public static final String REFRESH_VIEW_DESTINATIONS_PREFIX = "/refresh-view";


  private final TokenProvider tokenProvider;
  private final RefreshViewService refreshViewService;

  public WebsocketConfiguration(TokenProvider tokenProvider, @Lazy RefreshViewService refreshViewService) {
    this.tokenProvider = tokenProvider;
    this.refreshViewService = refreshViewService;
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic", REFRESH_VIEW_DESTINATIONS_PREFIX);
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/broadcast").withSockJS();
  }

  @Override
  public void configureClientInboundChannel(ChannelRegistration registration) {
    registration.interceptors(new JWTWebsocketInterceptor(tokenProvider));
  }

  @Override
  public void configureClientOutboundChannel(ChannelRegistration registration) {
    registration.interceptors(new RefreshViewInterceptor(refreshViewService));
  }
}
