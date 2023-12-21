package pl.com.tt.flex.server.repository.auction.da;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data  repository for the AuctionsSeriesEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuctionsSeriesRepository extends AbstractJpaRepository<AuctionsSeriesEntity, Long> {

    @Query(value = "SELECT series from AuctionsSeriesEntity series " +
        "WHERE :utcSysTime BETWEEN series.firstAuctionDate AND series.lastAuctionDate + 1 " +
        "AND (select dayAhead.id from AuctionDayAheadEntity dayAhead " +
        "WHERE :utcSysTime BETWEEN dayAhead.day AND dayAhead.day + 1 " +
        "AND series.id = dayAhead.auctionSeriesId) is null")
    List<AuctionsSeriesEntity> findAuctionsToCreate(@Param("utcSysTime") Instant utcSysTime);

    /**
     * Types used in query : {@link AuctionStatus#NEW_ENERGY }, {@link AuctionStatus#NEW_CAPACITY }, {@link AuctionStatus#SCHEDULED }
     */
    @Modifying
    @Query(value = "delete from AuctionDayAheadEntity dayAhead " +
        "where dayAhead.auctionSeriesId = :auctionSeriesId " +
        "and (select v.status from AuctionDayAheadViewEntity v where v.id = dayAhead.id) in ('NEW_CAPACITY', 'NEW_ENERGY', 'SCHEDULED')")
    void deleteNotStartedAuctionBySeriesId(@Param("auctionSeriesId") Long auctionSeriesId);

    /**
     * Types used in query : {@link AuctionStatus#NEW_ENERGY }, {@link AuctionStatus#NEW_CAPACITY }, {@link AuctionStatus#SCHEDULED }
     */
    @Query(value = "select auction_day_ahead.auction_day from auction_day_ahead " +
        "where auction_day_ahead.auctions_series_id = :auctionSeriesId " +
        "and (select v.status from auction_day_ahead_view v where v.id = auction_day_ahead.id) not in ('NEW_CAPACITY', 'NEW_ENERGY', 'SCHEDULED') " +
        "order by auction_day_ahead.auction_day DESC " +
        "fetch next 1 rows only", nativeQuery = true)
    Instant findLastAuctionDateBySeriesId(@Param("auctionSeriesId") Long auctionSeriesId);

    List<AuctionsSeriesEntity> findAllByProductId(Long productId);

    @Modifying
    @Query("UPDATE AuctionsSeriesEntity a SET a.name = :auctionName WHERE a.id = :seriesId")
    void updateAuctionName(@Param("auctionName") String auctionName, @Param("seriesId") Long seriesId);

    /**
     * Types used in query : {@link AuctionStatus#NEW_ENERGY }, {@link AuctionStatus#NEW_CAPACITY }, {@link AuctionStatus#SCHEDULED }
     */
    @Query("SELECT CASE WHEN count(v) > 0 THEN true ELSE false END FROM AuctionDayAheadViewEntity v " +
        "WHERE v.auctionSeriesId = :seriesId " +
        "AND v.status not in ('NEW_ENERGY', 'NEW_CAPACITY', 'SCHEDULED')")
    boolean existAuctionOpenOrClosureFromSeriesId(@Param("seriesId") Long seriesId);
}
