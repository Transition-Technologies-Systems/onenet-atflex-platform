package pl.com.tt.flex.model.service.dto.auction.type;

import lombok.Getter;

/**
 * The AuctionCategoryAndType enumeration.
 * It is a combination of offer category (CMVC, Day-Ahead) and offer type (capacity, energy).
 */
@Getter
public enum AuctionCategoryAndType {
    CMVC_CAPACITY,
    DAY_AHEAD_CAPACITY,
    DAY_AHEAD_ENERGY;
}
