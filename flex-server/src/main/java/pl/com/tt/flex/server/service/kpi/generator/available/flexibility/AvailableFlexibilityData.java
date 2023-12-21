package pl.com.tt.flex.server.service.kpi.generator.available.flexibility;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;

@Getter
public class AvailableFlexibilityData {
    /**
     * Pogrupowane po produkcie oraz dacie dostawy dostepne potencjały
     */
    private final Map<Pair<String, LocalDate>, FlexibilityData> availableFlexibilityDateGroupingByProductAndDeliveryDate;

    /**
     * Pogrupowane po produkcie dostepne potencjały
     */
    private final Map<String, FlexibilityData> availableFlexibilityDateGroupingByProduct;

    private final BigDecimal availableFlexibilityAll;

    public AvailableFlexibilityData(Map<Pair<String, LocalDate>, FlexibilityData> availableFlexibilityDateGroupingByProductAndDeliveryDate,
                                    Map<String, FlexibilityData> availableFlexibilityDateGroupingByProduct) {
        Validate.notNull(availableFlexibilityDateGroupingByProductAndDeliveryDate, "AvailableFlexibilityDateGroupingByProductAndDeliveryDate cannot be null");
        Validate.notNull(availableFlexibilityDateGroupingByProduct, "AvailableFlexibilityDateGroupingByProduct cannot be null");

        this.availableFlexibilityDateGroupingByProductAndDeliveryDate = availableFlexibilityDateGroupingByProductAndDeliveryDate;
        this.availableFlexibilityDateGroupingByProduct = availableFlexibilityDateGroupingByProduct;
        this.availableFlexibilityAll = calculateAverageForProduct(availableFlexibilityDateGroupingByProduct);
    }

    private static BigDecimal calculateAverageForProduct(Map<String, FlexibilityData> availableFlexibilityDateGroupingByProduct) {
        BigDecimal availableFlexibilitySum = availableFlexibilityDateGroupingByProduct.values().stream()
                                                                                      .map(FlexibilityData::getAvailableFlexibility)
                                                                                      .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSum = availableFlexibilityDateGroupingByProduct.values().stream()
                                                                       .map(FlexibilityData::getTotal)
                                                                       .reduce(BigDecimal.ZERO, BigDecimal::add);
        return availableFlexibilitySum.multiply(BigDecimal.valueOf(100)).divide(totalSum, 2, RoundingMode.HALF_UP);
    }
}
