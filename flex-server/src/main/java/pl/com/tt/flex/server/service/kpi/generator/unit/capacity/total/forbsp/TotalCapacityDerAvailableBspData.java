package pl.com.tt.flex.server.service.kpi.generator.unit.capacity.total.forbsp;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;

@Getter
public class TotalCapacityDerAvailableBspData {

    /**
     * Suma wolumenow z potencjalow dla produktow bilanusjacych
     */
    private final BigDecimal totalVolume;

    public TotalCapacityDerAvailableBspData(BigDecimal totalVolume) {
        Validate.notNull(totalVolume, "totalVolume cannot be null!");
        this.totalVolume = totalVolume;
    }
}
