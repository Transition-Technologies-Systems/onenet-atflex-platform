package pl.com.tt.flex.server.service.kpi.generator.volume.balancing.capacity.reserves;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Getter
public
class VolumeBalancingOffersReservesData {

    /**
     * Suma wolumenow - pogrupowane po nazwie porduktu i dacie dostawy
     */
    private final Map<Pair<String, LocalDate>, BigDecimal> volumeSumGroupingByProductNameAndDeliveryDate;
    /**
     * Suma wolumenow - pogrupowane po nazwie porduktu
     */
    private final Map<String, BigDecimal> volumeSumGroupingByProductName;
    /**
     * Suma wolumenow dla wszystkich produktow
     */
    private final BigDecimal volumeSum;

    public VolumeBalancingOffersReservesData(Map<Pair<String, LocalDate>, BigDecimal> volumeSumGroupingByProductNameAndDeliveryDate,
                                             Map<String, BigDecimal> volumeSumGroupingByProductName) {
        Validate.notNull(volumeSumGroupingByProductNameAndDeliveryDate, "volumeSumGroupingByProductNameAndDeliveryDate cannot be null");
        Validate.notNull(volumeSumGroupingByProductName, "volumeSumGroupingByProductName cannot be null");

        this.volumeSumGroupingByProductNameAndDeliveryDate = volumeSumGroupingByProductNameAndDeliveryDate;
        this.volumeSumGroupingByProductName = volumeSumGroupingByProductName;
        this.volumeSum = volumeSumGroupingByProductName.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
