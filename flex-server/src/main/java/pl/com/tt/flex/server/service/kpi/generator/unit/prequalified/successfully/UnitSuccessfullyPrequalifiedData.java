package pl.com.tt.flex.server.service.kpi.generator.unit.prequalified.successfully;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
class UnitSuccessfullyPrequalifiedData {

    /**
     * Liczba prekwalifikowanych DERow
     */
    private final BigDecimal prequalifiedDers;

    /**
     * Liczba wszystkich zarejestrowanych DERow
     */
    private final BigDecimal allDers;

    /**
     * Wynik dzialania: Liczba prekwalifikowanych DERow / Liczba wszystkich zarejestrowanych DERow
     */
    private final BigDecimal successfullyPrequalifiedDers;

    UnitSuccessfullyPrequalifiedData(BigDecimal prequalifiedDers, BigDecimal allDers) {
        Validate.notNull(prequalifiedDers, "PrequalifiedDers cannot be null!");
        Validate.notNull(allDers, "AllDers cannot be null!");

        this.prequalifiedDers = prequalifiedDers;
        this.allDers = allDers;
        this.successfullyPrequalifiedDers = calculateSuccessfullyPrequalified(prequalifiedDers, allDers);
    }

    private BigDecimal calculateSuccessfullyPrequalified(BigDecimal prequalifiedDers, BigDecimal allDers) {
        if (allDers.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        return prequalifiedDers.divide(allDers, 4, RoundingMode.HALF_UP);
    }
}
