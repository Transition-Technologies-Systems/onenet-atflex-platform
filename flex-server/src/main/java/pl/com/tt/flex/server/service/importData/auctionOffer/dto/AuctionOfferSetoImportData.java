package pl.com.tt.flex.server.service.importData.auctionOffer.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode(callSuper = false)
public class AuctionOfferSetoImportData {
    private Long offerId;
    private String band;
    private String timestamp;
    private Double volume;
    private String derName;
}
