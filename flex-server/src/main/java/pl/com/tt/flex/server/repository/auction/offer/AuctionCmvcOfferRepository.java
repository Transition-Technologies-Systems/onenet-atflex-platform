package pl.com.tt.flex.server.repository.auction.offer;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data  repository for the AuctionCmvcOfferEntity.
 */
@SuppressWarnings("unused")
@Repository
public interface AuctionCmvcOfferRepository extends AbstractJpaRepository<AuctionCmvcOfferEntity, Long> {

    @Modifying
    @Query("UPDATE AuctionCmvcOfferEntity ao SET ao.status = :status WHERE ao.id = :auctionOfferId")
    void updateStatus(@Param("status") AuctionOfferStatus status, @Param("auctionOfferId") Long auctionOfferId);

    @Query("SELECT cmvcOffer FROM AuctionCmvcOfferEntity cmvcOffer WHERE cmvcOffer.scheduledActivationEmail = true")
    List<AuctionCmvcOfferEntity> findAllByScheduledActivationEmail();

    @Query(value = "SELECT offer FROM AuctionCmvcOfferEntity offer " +
        "WHERE offer.acceptedDeliveryPeriodFrom >= :deliveryDateFrom AND offer.acceptedDeliveryPeriodTo <= :deliveryDateTo " +
        "AND offer.auctionCmvc.product.bidSizeUnit in (:bidSizeUnit)")
    List<AuctionCmvcOfferEntity> findAllByDeliveryDateFromAndToAndProductBidSizeUnit(@Param(value = "deliveryDateFrom") Instant deliveryDateFrom,
                                                                                     @Param(value = "deliveryDateTo") Instant deliveryDateTo,
                                                                                     @Param(value = "bidSizeUnit") List<ProductBidSizeUnit> bidSizeUnits);

    @Query(value = "SELECT offer FROM AuctionCmvcOfferEntity offer " +
        "WHERE offer.acceptedDeliveryPeriodFrom >= :deliveryDateFrom AND offer.acceptedDeliveryPeriodTo <= :deliveryDateTo " +
        "AND offer.auctionCmvc.product.bidSizeUnit in (:bidSizeUnit) " +
        "AND status in (:statuses)")
    List<AuctionCmvcOfferEntity> findAllByDeliveryDateFromAndToAndProductBidSizeUnitAndStatusIn(@Param(value = "deliveryDateFrom") Instant deliveryDateFrom,
                                                                                                @Param(value = "deliveryDateTo") Instant deliveryDateTo,
                                                                                                @Param(value = "bidSizeUnit") List<ProductBidSizeUnit> bidSizeUnits,
                                                                                                @Param(value = "statuses") List<AuctionOfferStatus> statuses);
}
