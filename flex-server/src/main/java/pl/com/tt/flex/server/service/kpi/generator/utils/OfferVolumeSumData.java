package pl.com.tt.flex.server.service.kpi.generator.utils;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Data
public class OfferVolumeSumData {
    private final String productName;
    private final LocalDate deliveryDate;
    private final BigDecimal volumeSum;

    public OfferVolumeSumData(String productName, LocalDate deliveryDate, List<BigDecimal> volumes) {
        this.productName = productName;
        this.deliveryDate = deliveryDate;
        this.volumeSum = volumes.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
