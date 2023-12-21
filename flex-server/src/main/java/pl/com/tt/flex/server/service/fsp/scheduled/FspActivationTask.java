package pl.com.tt.flex.server.service.fsp.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.service.fsp.FspService;

/**
 * Deactivation of Fsp if validTo date (UTC) is less than actual UTC time.
 * Activation of Fsp if validFrom date (UTC) is less or equal actual UTC time.
 */

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("application.activation-fsp.enabled")
public class FspActivationTask {

    private final FspService fspService;

    @Scheduled(cron = "${application.activation-fsp.cron}")
    public void execute() {
        log.debug("FspActivationTask - deactivation START");
        fspService.deactivateFspsByValidFromToDates();
        log.debug("FspActivationTask - deactivation END");
        log.debug("FspActivationTask - activation START");
        fspService.activateFspsByValidFromToDates();
        log.debug("FspActivationTask - activation END");
    }

}
