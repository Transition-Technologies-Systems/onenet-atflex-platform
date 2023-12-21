package pl.com.tt.flex.user.config.microservices;

import feign.Feign;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;

@RequiredArgsConstructor
public class MicroservicesProxyConfiguration {

  private final MicroservicesProxyAuthenticationInterceptor microservicesProxyAuthenticationInterceptor;

  @Bean
  public Feign.Builder feignBuilder() {
    return Feign.builder().requestInterceptor(microservicesProxyAuthenticationInterceptor);
  }

}
