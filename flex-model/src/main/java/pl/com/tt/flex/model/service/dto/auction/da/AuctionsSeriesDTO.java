package pl.com.tt.flex.model.service.dto.auction.da;

import lombok.*;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AuctionsSeriesDTO extends AbstractAuctionSeriesDTO implements Serializable {

    private Long id;

    private String name;

    @NotNull
    private AuctionDayAheadType type;

    private Instant energyGateOpeningTime;

    private Instant energyGateClosureTime;

    private Instant capacityGateOpeningTime;

    private Instant capacityGateClosureTime;

    private Instant capacityAvailabilityFrom;

    private Instant capacityAvailabilityTo;

    private Instant energyAvailabilityFrom;

    private Instant energyAvailabilityTo;

    @NotNull
    private Instant firstAuctionDate;

    @NotNull
    private Instant lastAuctionDate;

    private boolean deletable;
}
