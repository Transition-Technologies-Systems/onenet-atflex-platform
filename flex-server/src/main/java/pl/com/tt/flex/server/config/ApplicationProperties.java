package pl.com.tt.flex.server.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Flexserver.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@Getter
@NoArgsConstructor
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final ApplicationProperties.InvalidateExpiredToken invalidateExpiredToken = new InvalidateExpiredToken();
    private final ApplicationProperties.ActivationFsp activationFsp = new ActivationFsp();
    private final ApplicationProperties.ActivationProduct activationProduct = new ActivationProduct();
    private final ApplicationProperties.ActivationUnit activationUnit = new ActivationUnit();
    private final ApplicationProperties.ActivationFlexPotential activationFlexPotential = new ActivationFlexPotential();
    private final ApplicationProperties.UsersManual usersManual = new UsersManual();
    private final ApplicationProperties.Mail mail = new Mail();
    private final ApplicationProperties.RefreshView refreshView = new RefreshView();
    private final ApplicationProperties.MailSendReminder mailSendReminder = new MailSendReminder();
    private final ApplicationProperties.CheckFlexAgnoStatus checkFlexAgnoStatus = new CheckFlexAgnoStatus();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class InvalidateExpiredToken {

        private boolean enabled;
        private String cron;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ActivationFsp {
        private boolean enabled;
        private String cron;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ActivationProduct {
        private boolean enabled;
        private String cron;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ActivationUnit {
        private boolean enabled;
        private String cron;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class ActivationFlexPotential {
        private boolean enabled;
        private String cron;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class MailSendReminder {
        private boolean enabled;
        private String cron;
    }

    @Getter
    @Setter
    public static class RefreshView {

        private AuctionCmvcStatus auctionCmvcStatus = new AuctionCmvcStatus();
        private AuctionDayAheadStatus auctionDayAheadStatus = new AuctionDayAheadStatus();

        @Getter
        @Setter
        public static class AuctionCmvcStatus {
            private boolean enabled;
            private String cron;
        }

        @Getter
        @Setter
        public static class AuctionDayAheadStatus {
            private boolean enabled;
            private String cron;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class CheckFlexAgnoStatus {
        private boolean enabled;
        private String cron;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class UsersManual {
        private String filesPath;
        private String rodoFilePath;
        private String rulesFilePath;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Mail {
        private String baseUrlAdmin;
        private String baseUrlUser;
        private String subjectPrefix;
    }
}
