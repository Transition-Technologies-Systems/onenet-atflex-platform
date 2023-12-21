package pl.com.tt.flex.flex.agno.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Component
@NoArgsConstructor
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {

    private final Jwt jwt = new Jwt();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Jwt {
        private String base64Secret;
        private Long tokenValidityInSeconds;
    }
}