package pl.com.tt.flex.server.service.product.forecastedPrices.dto;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ForecastedPricesMinDTO implements Serializable {
    private String productName;
    private Instant forecastedPricesDate;
}
