package pl.com.tt.flex.admin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Properties specific to Flexadmin.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@Component
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

  private String websocketAddress;

  public String getWebsocketAddress() {
    return websocketAddress;
  }

  public void setWebsocketAddress(String websocketAddress) {
    this.websocketAddress = websocketAddress;
  }
}
