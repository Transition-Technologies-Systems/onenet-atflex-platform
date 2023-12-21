package pl.com.tt.flex.flex.agno.algorithm;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Validated
@Configuration
@ConfigurationProperties(prefix = "application.algorithm")
@Getter
@Setter
public class AlgorithmProperties {

    private String path;

    private String configFilePath;

    private String processDirectoryPath;

    private Long agnoLogUpdateInSeconds;

    private Long agnoLogUpdateInitDelayInSeconds;
}
