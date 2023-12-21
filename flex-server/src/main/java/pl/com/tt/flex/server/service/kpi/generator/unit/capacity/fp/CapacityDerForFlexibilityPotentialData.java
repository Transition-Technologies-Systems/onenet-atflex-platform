package pl.com.tt.flex.server.service.kpi.generator.unit.capacity.fp;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Map;

@Getter
class CapacityDerForFlexibilityPotentialData {

    /**
     * Suma wolumenow wszystkich potencjalow per produkt
     */
    private final Map<String, BigDecimal> capacityOfCertifiedDers;
    /**
     * Suma wszystkich wolumenow
     */
    private final BigDecimal sum;

    CapacityDerForFlexibilityPotentialData(Map<String, BigDecimal> capacityOfCertifiedDers) {
        this.capacityOfCertifiedDers = capacityOfCertifiedDers;
        this.sum = calculateVolumeSum(capacityOfCertifiedDers);

    }

    private static BigDecimal calculateVolumeSum(Map<String, BigDecimal> capacityOfCertifiedDers) {
        return capacityOfCertifiedDers.values().stream()
                                      .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
