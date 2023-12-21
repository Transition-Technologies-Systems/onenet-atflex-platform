package pl.com.tt.flex.server.service.kpi.generator.active.participation;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class ActiveParticipationData {

    private final BigDecimal dersUsedInAuction;
    private final BigDecimal countCertifiedDers;
    private final BigDecimal usedDers;

    public ActiveParticipationData(BigDecimal dersUsedInAuction, BigDecimal countCertifiedDers) {
        Validate.notNull(dersUsedInAuction, "DersUsedInAuction cannot be null");
        Validate.notNull(countCertifiedDers, "CountCertifiedDers cannot be null");

        this.dersUsedInAuction = dersUsedInAuction;
        this.countCertifiedDers = countCertifiedDers;
        this.usedDers = calculateUsedDers(dersUsedInAuction, countCertifiedDers);
    }

    private BigDecimal calculateUsedDers(BigDecimal dersUsedInAuction, BigDecimal certificationDers) {
        if (certificationDers.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }
        return dersUsedInAuction.divide(certificationDers, 4, RoundingMode.HALF_UP);
    }
}
