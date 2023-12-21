package pl.com.tt.flex.model.service.dto.auction.offer;

import lombok.*;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcMinDTO;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadMinDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuctionOfferDTO extends AbstractAuditingDTO {

    private Long id;

    //id, name, product(id, shortName)
    private AuctionCmvcMinDTO auctionCmvc;
    //id, fsp(id, companyName, role)
    private FlexPotentialMinDTO flexPotential;

    //id, name, product(id, shortName)
    private AuctionDayAheadMinDTO auctionDayAhead;
    //id, bsp(id, companyName, role)
    private SchedulingUnitMinDTO schedulingUnit;

    private List<AuctionOfferDersDTO> ders;

    @NotNull
    AuctionOfferStatus status;
    @NotNull
    private AuctionOfferType type;
    @NotNull
    private BigDecimal price;
    @NotNull
    private String volume;
    @NotNull
    private Boolean volumeTooltipVisible;
    @NotNull
    private Boolean volumeDivisibility;
    @NotNull
    private String acceptedVolume;
    @NotNull
    private Boolean acceptedVolumeTooltipVisible;
    @NotNull
    private Instant deliveryPeriodFrom;
    @NotNull
    private Instant deliveryPeriodTo;
    @NotNull
    private Boolean deliveryPeriodDivisibility;
    @NotNull
    private Instant acceptedDeliveryPeriodFrom;
    @NotNull
    private Instant acceptedDeliveryPeriodTo;
    private Integer verifiedVolumesPercent;
}
