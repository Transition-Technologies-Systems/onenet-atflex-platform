package pl.com.tt.flex.server.service.importData.auctionOffer.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@ToString
@Builder
public class AuctionOfferImportData implements Serializable {
    private String id;
    private String status;
    private String acceptedVolume;
    private String acceptedDeliveryPeriod;
}
