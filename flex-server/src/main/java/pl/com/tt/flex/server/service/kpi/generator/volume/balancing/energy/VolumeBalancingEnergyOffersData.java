package pl.com.tt.flex.server.service.kpi.generator.volume.balancing.energy;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Getter
public class VolumeBalancingEnergyOffersData {

    /**
     * Suma wolumenow z aukcji na energię pogrupowane po dacie dostawy
     */
    private final Map<LocalDate, BigDecimal> energyVolumesGroupingByDate;
    /**
     * Suma wolumenow
     */
    private final BigDecimal volumeSum;

    public VolumeBalancingEnergyOffersData(Map<LocalDate, BigDecimal> energyVolumesGroupingByDate) {
        Validate.notNull(energyVolumesGroupingByDate, "energyVolumesGroupingByDate cannot be null!");

        this.energyVolumesGroupingByDate = energyVolumesGroupingByDate;
        this.volumeSum = energyVolumesGroupingByDate.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
