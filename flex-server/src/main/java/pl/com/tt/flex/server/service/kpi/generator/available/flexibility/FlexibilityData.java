package pl.com.tt.flex.server.service.kpi.generator.available.flexibility;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class FlexibilityData {

    /**
     * zsumowane wolumeny potencjalow zlozonych w danej aukcji
     */
    private final BigDecimal availableFlexibility;

    /**
     * zsumowane wolumeny potencjalow, ktore mogą zlozyc oferte w danej aukcji
     */
    private final BigDecimal total;

    /**
     * srednia procenotwa (availableFlexibility * 100 / total)
     */
    private final BigDecimal flexibilityPercentage;

    public FlexibilityData(BigDecimal total, BigDecimal availableFlexibility) {
        Validate.notNull(total, "Total cannot be null");
        Validate.notNull(availableFlexibility, "AvailableFlexibility cannot be null");

        this.total = total;
        this.availableFlexibility = availableFlexibility;
        this.flexibilityPercentage = calculateFlexibilityPercentage(total, availableFlexibility);
    }

    private BigDecimal calculateFlexibilityPercentage(BigDecimal total, BigDecimal availableFlexibility) {
        if (total.compareTo(BigDecimal.ZERO) == 0 && availableFlexibility.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(0);
        } else if (total.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        return availableFlexibility.multiply(BigDecimal.valueOf(100)).divide(total, 2, RoundingMode.HALF_UP);
    }
}
