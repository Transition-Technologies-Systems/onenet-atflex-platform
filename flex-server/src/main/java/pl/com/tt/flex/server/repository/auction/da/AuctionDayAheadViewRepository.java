package pl.com.tt.flex.server.repository.auction.da;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadViewEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data  repository for the AuctionDayAheadEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuctionDayAheadViewRepository extends AbstractJpaRepository<AuctionDayAheadViewEntity, Long> {

    /**
     * Types used in query : {@link AuctionStatus#SCHEDULED }
     */
    @Query("SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM AuctionDayAheadViewEntity dayAhead " +
        "WHERE dayAhead.status != 'SCHEDULED' " +
        "AND dayAhead.auctionSeriesId = :seriesId")
    boolean existsNotScheduledByAuctionSeriesId(@Param("seriesId") long auctionSeriesId);

    List<AuctionDayAheadViewEntity> findAllByStatusInAndProductId(List<AuctionStatus> auctionStatuses, Long productId);

    @Query("SELECT a.status FROM AuctionDayAheadViewEntity a WHERE a.id = :id")
    AuctionStatus findStatusById(@Param("id") Long id);

    /**
     * Types used in query : {@link AuctionStatus#SCHEDULED }, {@link AuctionStatus#NEW_CAPACITY }, {@link AuctionStatus#NEW_ENERGY }
     */
    @Query("SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM AuctionDayAheadViewEntity dayAhead " +
        "WHERE dayAhead.auctionSeriesId = :seriesId " +
        "AND dayAhead.status not in ('SCHEDULED','NEW_CAPACITY','NEW_ENERGY') " +
        "AND dayAhead.day > :date")
    boolean existsNotOpenAuctionBySeriesIdAndDayIsAfter(@Param("seriesId") Long seriesId, @Param("date") Instant date);

    /**
     * The query searches auctions whose start or end auction dates are in given range.
     */
    @Query("SELECT dayAhead FROM AuctionDayAheadViewEntity dayAhead " +
        "WHERE dayAhead.energyGateOpeningTime BETWEEN :fromDate AND :toDate " +
        "OR dayAhead.energyGateClosureTime BETWEEN :fromDate AND :toDate " +
        "OR dayAhead.capacityGateOpeningTime BETWEEN :fromDate AND :toDate " +
        "OR dayAhead.capacityGateClosureTime BETWEEN :fromDate AND :toDate")
    List<AuctionDayAheadViewEntity> findAllByGateTimeInRange(@Param("fromDate") Instant fromDate, @Param("toDate") Instant toDate);
}
