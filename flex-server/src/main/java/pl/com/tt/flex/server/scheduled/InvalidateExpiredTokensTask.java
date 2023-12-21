package pl.com.tt.flex.server.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.service.user.UserOnlineService;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("application.invalidate-expired-token.enabled")
public class InvalidateExpiredTokensTask {

    private final UserOnlineService userOnlineService;

    @Scheduled(cron = "${application.invalidate-expired-token.cron}")
    public void execute() {
        log.debug("Invalidating expired tokens...");
        userOnlineService.invalidateExpiredTokens();
    }

}
