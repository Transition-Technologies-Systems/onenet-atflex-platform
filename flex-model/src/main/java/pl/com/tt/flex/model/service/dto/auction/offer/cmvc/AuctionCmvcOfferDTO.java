package pl.com.tt.flex.model.service.dto.auction.offer.cmvc;

import lombok.*;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcMinDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AbstractAuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class AuctionCmvcOfferDTO extends AbstractAuctionOfferDTO {

    @NotNull
    private AuctionCmvcMinDTO auctionCmvc;
    @NotNull
    private FlexPotentialMinDTO flexPotential;
    @NotNull
    private AuctionOfferType type = AuctionOfferType.CAPACITY;
    @NotNull
    private BigDecimal price;
    @NotNull
    private BigDecimal volume;
    private BigDecimal acceptedVolume;
}
