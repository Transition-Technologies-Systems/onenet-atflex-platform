package pl.com.tt.flex.server.service.kpi.generator.requested.flexibility.power.fillfactor;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Getter
class PowerFillFactorData {

    /**
     * Maksymalny mozliwy wolumen oraz oferowane wolumeny prze FSP/FSPA pogrupowane po produkcie i dacie dostawy
     */
    private final Map<Pair<String, LocalDate>, RequestedAndOfferedVolume> maxVolumeGroupingByProductNameAndDeliveryDate;

    /**
     * Suma wszystkich zlozonych wolumenów
     */
    private final BigDecimal offeredVolumeSum;

    /**
     * Suma wszystkich oczekiwanych wolumenów
     */
    private final BigDecimal requestedVolumeSum;

    /**
     * Liczba aukcji
     */
    private final BigDecimal numberOfAuctions;

    /**
     * Stopien wypełnienia = zlozone_wolumeny/oczekiwane_wolumenu/liczba_aukcji
     */
    private final BigDecimal fillFactor;

    PowerFillFactorData(Map<Pair<String, LocalDate>, RequestedAndOfferedVolume> maxVolumeGroupingByProductNameAndDeliveryDate, BigDecimal numberOfAuctions) {
        Validate.notNull(maxVolumeGroupingByProductNameAndDeliveryDate, "maxVolumeGroupingByProductNameAndDeliveryDate cannot be null!");
        Validate.notNull(numberOfAuctions, "numberOfAuctions cannot be null!");

        this.maxVolumeGroupingByProductNameAndDeliveryDate = maxVolumeGroupingByProductNameAndDeliveryDate;
        this.numberOfAuctions = numberOfAuctions;
        this.offeredVolumeSum = calculateSum(maxVolumeGroupingByProductNameAndDeliveryDate, RequestedAndOfferedVolume::getOfferedVolume);
        this.requestedVolumeSum = calculateSum(maxVolumeGroupingByProductNameAndDeliveryDate, RequestedAndOfferedVolume::getRequestedVolume);
        this.fillFactor = calculateFillFactor(numberOfAuctions);
    }

    /**
     * Stopien wypełnienia = zlozone_wolumeny/oczekiwane_wolumenu/liczba_aukcji
     */
    private BigDecimal calculateFillFactor(BigDecimal auctionNumbers) {
        return offeredVolumeSum.divide(requestedVolumeSum, 6, RoundingMode.HALF_UP).divide(auctionNumbers, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateSum(Map<Pair<String, LocalDate>, RequestedAndOfferedVolume> maxVolumeGroupingByProductNameAndDeliveryDate,
                                    Function<RequestedAndOfferedVolume, BigDecimal> toCalculate) {
        return maxVolumeGroupingByProductNameAndDeliveryDate.values().stream()
                                                            .map(toCalculate)
                                                            .filter(Objects::nonNull)
                                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
