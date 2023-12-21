package pl.com.tt.flex.server.repository.auction.da;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data  repository for the AuctionEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuctionDayAheadRepository extends AbstractJpaRepository<AuctionDayAheadEntity, Long> {


    /**
     * Types used in query : {@link AuctionStatus#NEW_CAPACITY }, {@link AuctionStatus#NEW_ENERGY}, {@link AuctionStatus#SCHEDULED}
     */
    @Query(value = "select dayAhead from AuctionDayAheadEntity dayAhead " +
        "where dayAhead.auctionSeriesId = :auctionSeriesId and dayAhead.status in('NEW_CAPACITY', 'NEW_ENERGY', 'SCHEDULED')")
    List<AuctionDayAheadEntity> findNotStartedAuctions(@Param("auctionSeriesId") long auctionSeriesId);

    @Modifying
    @Query("UPDATE AuctionDayAheadEntity a SET a.name = :auctionName WHERE a.id = :auctionId")
    void updateAuctionName(@Param("auctionName") String auctionName, @Param("auctionId") Long auctionId);

    List<AuctionDayAheadEntity> findAllByAuctionSeriesId(Long seriesId);

    /**
     * @see Role#ROLE_BALANCING_SERVICE_PROVIDER
     */
    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(fsp.id, fsp.companyName, fsp.role) FROM FspEntity fsp " +
        "WHERE fsp.active = true AND (fsp.role = 'ROLE_BALANCING_SERVICE_PROVIDER') " +
        "AND EXISTS (SELECT su.id FROM SchedulingUnitEntity su join su.schedulingUnitType.products prod " +
        "WHERE su.bsp = fsp.id AND prod.id in (:productId) AND su.certified = true)")
    List<FspCompanyMinDTO> findBspsWithRegisteredSchedulingUnitsForProduct(@Param(value = "productId") Long productId);

    /**
     * @see Role#ROLE_BALANCING_SERVICE_PROVIDER
     */
    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(fsp.id, fsp.companyName, fsp.role) FROM FspEntity fsp " +
        "WHERE fsp.active = true AND (fsp.role = 'ROLE_BALANCING_SERVICE_PROVIDER') " +
        "AND EXISTS (SELECT offer.id FROM AuctionDayAheadOfferEntity offer join offer.auctionDayAhead da " +
        "WHERE da.id = :auctionId AND offer.schedulingUnit.bsp = fsp.id)")
    List<FspCompanyMinDTO> findBspsWithSubmittedCapacityOfferToAuctionCE(@Param(value = "auctionId") Long auctionId);

    @Query(value = "SELECT DISTINCT NEW pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO(da.product.id, da.product.shortName) " +
        "FROM AuctionDayAheadEntity da " +
        "WHERE da.type = :auctionType " +
        "order by da.product.shortName")
    List<ProductNameMinDTO> findAllProductsUsedInAuctionDayAheadByType(@Param(value = "auctionType") AuctionDayAheadType auctionDayAheadType);

    /**
     * @see AuctionStatus#OPEN_CAPACITY
     * @see AuctionStatus#OPEN_ENERGY
     */
    @Query(value = "SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM AuctionDayAheadEntity dayAhead " +
        "WHERE dayAhead.product.id = :productId " +
        "AND (dayAhead.status in ('OPEN_CAPACITY','OPEN_ENERGY'))")
    boolean existsOpenAuctionsWithProductId(@Param(value = "productId") Long productId);

    boolean existsByDayAndAuctionSeriesId(Instant auctionDay, long seriesId);

    int countByAuctionSeriesId(Long id);

    List<AuctionDayAheadEntity> findAllByTypeAndDeliveryDate(AuctionDayAheadType type, Instant deliveryDate);
}
