package pl.com.tt.flex.server.repository.auction.cmvc;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcViewEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data  repository for the AuctionCmvcViewEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuctionCmvcViewRepository extends AbstractJpaRepository<AuctionCmvcViewEntity, Long> {

    List<AuctionCmvcViewEntity> findAllByStatusAndProductId(AuctionStatus auctionStatus, Long productId);

    @Query("SELECT a.status FROM AuctionCmvcViewEntity a WHERE a.id = :id")
    AuctionStatus findStatusById(@Param("id") Long id);

    Long countAllByDeliveryDateFromBetweenAndProductName(Instant fromDate, Instant toDate, String productName);

    /** The query searches auctions whose start or end auction dates are in given range. */
    @Query("SELECT cmvc FROM AuctionCmvcViewEntity cmvc " +
        "WHERE cmvc.gateOpeningTime BETWEEN :fromDate AND :toDate " +
        "OR cmvc.gateClosureTime BETWEEN :fromDate AND :toDate")
    List<AuctionCmvcViewEntity> findAllByGateTimeInRange(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
}
