package pl.com.tt.flex.model.service.dto.product;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.product.type.Direction;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
public class ProductMinDTO {

    private Long id;
    private String shortName;
    private String fullName;
    private boolean locational;
    private BigDecimal minBidSize;
    private BigDecimal maxBidSize;
    private Integer maxFullActivationTime;
    private Integer minRequiredDeliveryDuration;
    private Direction direction;
    private boolean active;
}
