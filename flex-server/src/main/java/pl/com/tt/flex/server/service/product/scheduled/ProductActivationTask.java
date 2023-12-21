package pl.com.tt.flex.server.service.product.scheduled;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.service.product.ProductService;

/**
 * Deactivation of Product if validTo date (UTC) is less than actual UTC time.
 * Activation of Product if validFrom date (UTC) is less or equal actual UTC time.
 */

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty("application.activation-product.enabled")
public class ProductActivationTask {

    private final ProductService productService;

    @Scheduled(cron = "${application.activation-product.cron}")
    public void execute() {
        log.debug("ProductActivationTask - deactivation START");
        productService.deactivateProductsByValidFromToDates();
        log.debug("ProductActivationTask - deactivation END");
        log.debug("ProductActivationTask - activation START");
        productService.activateProductsByValidFromToDates();
        log.debug("ProductActivationTask - activation END");
    }

}
