package pl.com.tt.flex.server.service.auction.offer.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;

@NoArgsConstructor
@Getter
@Setter
public class AuctionReminderDTO implements Serializable {

    private boolean hasReminder;
    private String auctionName;
    private Long auctionId;
    private Instant auctionGateOpeningTime;
    private Instant auctionGateClosureTime;

    public AuctionReminderDTO(String auctionName, Long auctionId, Instant auctionGateOpeningTime, Instant auctionGateClosureTime) {
        this.auctionName = auctionName;
        this.auctionId = auctionId;
        this.auctionGateOpeningTime = auctionGateOpeningTime;
        this.auctionGateClosureTime = auctionGateClosureTime;
        this.hasReminder = true;
    }
}
