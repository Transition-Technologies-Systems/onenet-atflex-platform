package pl.com.tt.flex.server.repository.auction.offer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewEntity;

import java.math.BigDecimal;

@Repository
public interface AuctionOfferRepository extends JpaRepository<AuctionOfferViewEntity, Long> {

    @Query(value = "select count(distinct ID) from ( " +
        "select u.id as ID from auction_cmvc_offer " +
        "inner join flex_potential_units fp on fp.flex_potential_id = auction_cmvc_offer.flex_potential_id " +
        "inner join unit u on fp.unit_id = u.id " +
        "union " +
        "select unit_id as ID from auction_da_offer_ders)", nativeQuery = true)
    BigDecimal countDersUsedInAuctions();
}
