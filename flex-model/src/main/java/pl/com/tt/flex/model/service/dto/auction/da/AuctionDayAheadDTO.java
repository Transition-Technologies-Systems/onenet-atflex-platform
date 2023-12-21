package pl.com.tt.flex.model.service.dto.auction.da;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferMinDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuctionDayAheadDTO extends AbstractAuctionSeriesDTO implements Serializable {

    private Long id;

    private String name;

    private AuctionStatus status;

    private Instant day;

    private Instant deliveryDate;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String productId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String productName;

    private Instant energyGateOpeningTime;

    private Instant energyGateClosureTime;

    private Instant capacityGateOpeningTime;

    private Instant capacityGateClosureTime;

    private Instant capacityAvailabilityFrom;

    private Instant capacityAvailabilityTo;

    private Instant energyAvailabilityFrom;

    private Instant energyAvailabilityTo;

    private AuctionDayAheadType type;

    private long auctionSeriesId;

    //Flaga dla przycisku dodwania ofery do aukcji DA
    private boolean canAddBid;
    
    private List<AuctionOfferMinDTO> offers;
}
