package pl.com.tt.flex.server.repository.auction.cmvc;


import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data  repository for the AuctionCmvcEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuctionCmvcRepository extends AbstractJpaRepository<AuctionCmvcEntity, Long> {

    @Modifying
    @Query("UPDATE AuctionCmvcEntity a SET a.name = :auctionName WHERE a.id = :auctionId")
    void updateAuctionName(@Param("auctionName") String auctionName, @Param("auctionId") Long auctionId);

    @Query(value = "SELECT cmvc FROM AuctionCmvcEntity cmvc " +
        "WHERE cmvc.deliveryDateFrom >= :deliveryDateFrom AND cmvc.deliveryDateTo <= :deliveryDateTo ")
    List<AuctionCmvcEntity> findAllByDeliveryDateFromAndToAndProductBidSizeUnitAndStatusIn(@Param(value = "deliveryDateFrom") Instant deliveryDateFrom,
                                                                                           @Param(value = "deliveryDateTo") Instant deliveryDateTo);
}
