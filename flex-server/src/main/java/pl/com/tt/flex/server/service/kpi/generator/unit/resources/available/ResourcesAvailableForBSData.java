package pl.com.tt.flex.server.service.kpi.generator.unit.resources.available;

import lombok.Getter;
import org.apache.commons.lang3.Validate;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
class ResourcesAvailableForBSData {

    /**
     * Liczba DERow z przynajmniej jednym potencjalem na produkt bilansujacy
     */
    private final BigDecimal dersWithPottential;

    /**
     * Liczba wszystkich certyfikowanych DERow
     */
    private final BigDecimal dersWithCertification;

    /**
     * Wynik działania dersWithPottential/dersWithCertification
     */
    private final BigDecimal resourceAvailable;

    public ResourcesAvailableForBSData(BigDecimal dersWithPottential, BigDecimal dersWithCertification) {
        Validate.notNull(dersWithPottential, "dersWithPottential cannot be null!");
        Validate.notNull(dersWithCertification, "dersWithCertification cannot be null!");

        this.dersWithPottential = dersWithPottential;
        this.dersWithCertification = dersWithCertification;
        this.resourceAvailable = calculateResourceAvailable(dersWithPottential, dersWithCertification);
    }

    private BigDecimal calculateResourceAvailable(BigDecimal dersWithPottential, BigDecimal dersWithCertification) {
        if (dersWithCertification.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(1);
        }
        return dersWithPottential.divide(dersWithCertification, 4, RoundingMode.HALF_UP);
    }
}
