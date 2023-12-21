package pl.com.tt.flex.server.service.kpi.generator.unit.number.availablebsp;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;

@Getter
class NumberOfDerAvailableForBspData {

    private final BigDecimal availableDerForBsp;

    NumberOfDerAvailableForBspData(BigDecimal availableDerForBsp) {
        Validate.notNull(availableDerForBsp, "availableDerForBsp cannot be null!");
        this.availableDerForBsp = availableDerForBsp;
    }
}
