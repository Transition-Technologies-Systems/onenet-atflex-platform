package pl.com.tt.flex.admin.refreshView;

import lombok.*;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class OfferFilterDTO {

  private Long auctionCmvcId;
  private Long auctionDayAheadId;
  private AuctionOfferType auctionType;
}
