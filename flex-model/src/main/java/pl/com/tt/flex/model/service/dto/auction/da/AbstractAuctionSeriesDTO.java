package pl.com.tt.flex.model.service.dto.auction.da;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;

import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
public abstract class AbstractAuctionSeriesDTO extends AbstractAuditingDTO {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ProductMinDTO product;

    private BigDecimal minDesiredCapacity;

    private BigDecimal maxDesiredCapacity;

    private BigDecimal minDesiredEnergy;

    private BigDecimal maxDesiredEnergy;
}
