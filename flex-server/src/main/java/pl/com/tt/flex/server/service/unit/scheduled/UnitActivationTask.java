package pl.com.tt.flex.server.service.unit.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.service.unit.UnitService;

/**
 * Deactivation of Unit if validTo date (UTC) is less than actual UTC time.
 * Activation of Unit if validFrom date (UTC) is less or equal actual UTC time.
 */

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("application.activation-unit.enabled")
public class UnitActivationTask {

    private final UnitService unitService;

    @Scheduled(cron = "${application.activation-unit.cron}")
    public void execute() {
        log.debug("UnitActivationTask - activation START");
        unitService.activateUnitsByValidFromToDates();
        log.debug("UnitActivationTask - activation END");
        log.debug("UnitActivationTask - deactivation START");
        unitService.deactivateUnitsByValidFromToDates();
        log.debug("UnitActivationTask - deactivation END");
    }
}
