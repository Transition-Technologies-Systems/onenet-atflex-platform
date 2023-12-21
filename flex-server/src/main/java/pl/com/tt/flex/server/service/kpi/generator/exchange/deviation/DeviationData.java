package pl.com.tt.flex.server.service.kpi.generator.exchange.deviation;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class DeviationData {
    private final BigDecimal acceptedVolume;
    private final BigDecimal activatedVolume;
    private final String product;
    private final LocalDate deliveryDate;


    public DeviationData(BigDecimal acceptedVolume, BigDecimal activatedVolume, String product, LocalDate deliveryDate){
        Validate.notNull(acceptedVolume, "acceptedVolume cannot be null!");
        Validate.notNull(activatedVolume, "activatedVolume cannot be null!");
        Validate.notNull(product, "product cannot be null!");
        Validate.notNull(deliveryDate, "deliveryDate cannot be null!");

        this.acceptedVolume = acceptedVolume;
        this.activatedVolume = activatedVolume;
        this.product = product;
        this.deliveryDate = deliveryDate;
    }
}
