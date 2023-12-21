package pl.com.tt.flex.server.service.kpi.generator.unit.number.fp;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;

@Getter
class NumberOfDerWithFlexPotentialData {

    /**
     * Liczba derow ktore posiadaja przynajmniej jeden prekwalifikowany FP
     */
    private final BigDecimal numberOfDers;

    NumberOfDerWithFlexPotentialData(BigDecimal numberOfDers) {
        Validate.notNull(numberOfDers, "NumberOfDers cannot be null!");
        this.numberOfDers = numberOfDers;
    }
}
