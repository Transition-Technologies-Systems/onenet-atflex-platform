package pl.com.tt.flex.server.service.potential.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
public class FlexPotentialImportDTO {

    private Long id;
    private String productShortName;
    private String fspCompanyName;
    private String unitCode;
    private BigDecimal volume;
    private ProductBidSizeUnit volumeUnit;
    private Instant validFrom;
    private Instant validTo;
    private boolean activated;
    private boolean productPrequalification;
    private boolean staticGridPrequalification;
}
