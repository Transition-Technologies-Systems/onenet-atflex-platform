package pl.com.tt.flex.model.service.dto.auction.type;

import java.util.List;

/**
 * The AuctionStatus enumeration.
 */
public enum AuctionStatus {
	SCHEDULED, NEW, OPEN, CLOSED, AFTER_AGGR_NET_OFFER_CREATION, AFTER_GRID_IMPACT_ASSESSMENT, SENT_TO_BM,

	/**
	 * Statuses for capacity and energy auctions
	 *
	 * @see AuctionType#CAPACITY_AND_ENERGY
	 */
	NEW_CAPACITY, OPEN_CAPACITY, CLOSED_CAPACITY, NEW_ENERGY, OPEN_ENERGY, CLOSED_ENERGY;

	public static List<AuctionStatus> getNewAuctionStatuses() {
		return List.of(NEW, NEW_ENERGY, NEW_CAPACITY);
	}

	public static List<AuctionStatus> getOpenAuctionStatuses() {
		return List.of(OPEN, OPEN_ENERGY, OPEN_CAPACITY);
	}

	public static List<AuctionStatus> getCloseAuctionStatuses() {
		return List.of(CLOSED, CLOSED_CAPACITY, CLOSED_ENERGY);
	}
}
