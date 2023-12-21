package pl.com.tt.flex.server.service.importData.auctionOffer.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AuctionOfferSchedulingUnitDTO {

    String offerId;
    private int bandNumber;
    private String timestamp;
    private String volume;
    private String price;
}
