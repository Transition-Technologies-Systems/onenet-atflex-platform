package pl.com.tt.flex.server.service.product.forecastedPrices.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.domain.product.ForecastedPricesEntity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * A DTO for the {@link ForecastedPricesEntity} entity.
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ForecastedPricesDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    private Instant forecastedPricesDate;

    private ProductMinDTO product;

    List<MinimalDTO<String, BigDecimal>> prices = new ArrayList<>();
}
