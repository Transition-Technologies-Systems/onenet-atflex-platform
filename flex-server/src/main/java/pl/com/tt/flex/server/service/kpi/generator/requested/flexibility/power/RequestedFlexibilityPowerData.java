package pl.com.tt.flex.server.service.kpi.generator.requested.flexibility.power;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.service.kpi.generator.utils.SortUtils.sortMapWithString;

@Getter
public
class RequestedFlexibilityPowerData {

    /**
     * Maksymalne wartości oczekiwane - pogrupowane po nazwie porduktu i dacie dostawy
     */
    private final Map<Pair<String, LocalDate>, BigDecimal> maxVolumeGroupingByProductNameAndDeliveryDate;
    /**
     * Suma maksymalnych wartości oczekiwanych - pogrupowane po nazwie porduktu
     */
    private final Map<String, BigDecimal> sumMaxVolumeGroupingByProductName;
    /**
     * Suma maksymalnych wartości oczekiwanych
     */
    private final BigDecimal sumMaxVolume;
    /**
     * Liczba aukcji
     */
    private final BigDecimal numberOfAuctions;

    RequestedFlexibilityPowerData(Map<Pair<String, LocalDate>, BigDecimal> maxVolumeGroupingByProductNameAndDeliveryDate, BigDecimal numberOfAuctions) {
        Validate.notNull(maxVolumeGroupingByProductNameAndDeliveryDate, "maxVolumeGroupingByProductNameAndDeliveryDate cannot be null");
        Validate.notNull(numberOfAuctions, "auctionNumber cannot be null");

        this.maxVolumeGroupingByProductNameAndDeliveryDate = maxVolumeGroupingByProductNameAndDeliveryDate;
        this.sumMaxVolumeGroupingByProductName = getSumMaxVolumeGroupingByProductName(maxVolumeGroupingByProductNameAndDeliveryDate);
        this.sumMaxVolume = maxVolumeGroupingByProductNameAndDeliveryDate.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        this.numberOfAuctions = numberOfAuctions;
    }

    /**
     * Suma maksymalnych wartości oczekiwanych - pogrupowane po nazwie porduktu
     */
    private Map<String, BigDecimal> getSumMaxVolumeGroupingByProductName(Map<Pair<String, LocalDate>, BigDecimal> maxVolumeGroupingByProductNameAndDeliveryDate) {
        Map<String, BigDecimal> maxVolumeGroupingByProductAndDate = maxVolumeGroupingByProductNameAndDeliveryDate
            .entrySet().stream()
            .collect(Collectors.groupingBy(
                entry -> entry.getKey().getLeft(),
                Collectors.reducing(BigDecimal.ZERO, Map.Entry::getValue, BigDecimal::add)
            ));
        return sortMapWithString(maxVolumeGroupingByProductAndDate);
    }
}
