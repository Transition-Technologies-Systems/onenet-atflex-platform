package pl.com.tt.flex.model.service.dto.auction.da;

import lombok.*;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuctionDayAheadMinDTO implements Serializable {

    private Long id;
    private String name;
    private ProductMinDTO product;
}
