package pl.com.tt.flex.server.service.potential.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;

/**
 * Deactivation of FlexPotential if validTo date (UTC) is less than actual UTC time.
 * Activation of FlexPotential if validFrom date (UTC) is less or equal actual UTC time.
 */

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("application.activation-flexPotential.enabled")
public class FlexPotentialActivationTask {

    private final FlexPotentialService flexPotentialService;

    @Scheduled(cron = "${application.activation-flexPotential.cron}")
    public void execute() {
        log.debug("FlexPotentialActivationTask - deactivation START");
        flexPotentialService.deactivateFlexPotentialsByValidFromToDates();
        log.debug("FlexPotentialActivationTask - deactivation END");
        log.debug("FlexPotentialActivationTask - activation START");
        flexPotentialService.activateFlexPotentialsByValidFromToDates();
        log.debug("FlexPotentialActivationTask - activation END");
    }

}
