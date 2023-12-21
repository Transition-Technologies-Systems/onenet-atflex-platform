package pl.com.tt.flex.user.refreshView.util;

import pl.com.tt.flex.user.refreshView.OfferFilterDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;

public class OfferFilterUtil {

  private OfferFilterUtil() {
  }

  public static boolean isMatching(OfferFilterDTO criteria, AuctionOfferDTO auctionOfferDTO) {
    boolean matching = true;
    if (criteria != null) {
      if (auctionOfferDTO.getAuctionCmvc() != null && criteria.getAuctionCmvcId() == null) {
        return false;
      }
      if (auctionOfferDTO.getAuctionDayAhead() != null && criteria.getAuctionDayAheadId() == null) {
        return false;
      }
      if (criteria.getAuctionType() != null) {
        matching = matching && criteria.getAuctionType().equals(auctionOfferDTO.getType());
      }
      if (criteria.getAuctionCmvcId() != null && auctionOfferDTO.getAuctionCmvc() != null) {
        matching = matching && criteria.getAuctionCmvcId().equals(auctionOfferDTO.getAuctionCmvc().getId());
      }
      if (criteria.getAuctionDayAheadId() != null && auctionOfferDTO.getAuctionDayAhead() != null) {
        matching = matching && criteria.getAuctionDayAheadId().equals(auctionOfferDTO.getAuctionDayAhead().getId());
      }
    }
    return matching;
  }
}
