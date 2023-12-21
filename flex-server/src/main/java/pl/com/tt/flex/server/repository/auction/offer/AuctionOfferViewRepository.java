package pl.com.tt.flex.server.repository.auction.offer;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data  repository for the AuctionOfferViewEntity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuctionOfferViewRepository extends AbstractJpaRepository<AuctionOfferViewEntity, Long> {

    @Query(value = "SELECT DISTINCT NEW pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO(cast(aov.productId as java.lang.Long), aov.productName) " +
        "FROM AuctionOfferViewEntity aov " +
        "order by aov.productName")
    List<ProductNameMinDTO> findAllProductsUsedInOffer();

    @Query("SELECT CASE WHEN COUNT(o) = 0 THEN true ELSE false END FROM AuctionOfferViewEntity o WHERE o.id IN :ids AND o.status NOT IN ('PENDING', 'VOLUMES_VERIFIED')")
    boolean areAllOffersPendingOrVerified(@Param("ids") List<Long> ids);

    @Query("SELECT CASE WHEN COUNT(o) = 0 THEN true ELSE false END FROM AuctionOfferViewEntity o WHERE o.id IN :ids AND o.auctionCategoryAndType != 'CMVC_CAPACITY' AND (o.status != 'VOLUMES_VERIFIED' OR o.verifiedVolumesPercent != 100)")
    boolean areAllDayAheadVolumesVerified(@Param("ids") List<Long> ids);

    @Query("SELECT CASE WHEN COUNT(o) = 0 THEN true ELSE false END FROM AuctionOfferViewEntity o WHERE o.id IN :ids AND o.auctionStatus NOT IN ('CLOSED', 'CLOSED_ENERGY', 'CLOSED_CAPACITY')")
    boolean areAllAuctionsClosed(@Param("ids") List<Long> ids);

    @Query("SELECT aov from AuctionOfferViewEntity aov where aov.acceptedDeliveryPeriodFrom >= :deliveryFrom and aov.acceptedDeliveryPeriodTo <= :deliveryTo")
    List<AuctionOfferViewEntity> findByAcceptedDeliveryDateFromAndTo(@Param("deliveryFrom") Instant acceptedDeliveryPeriodFrom,
                                                                     @Param("deliveryTo") Instant acceptedDeliveryPeriodTo);
}
