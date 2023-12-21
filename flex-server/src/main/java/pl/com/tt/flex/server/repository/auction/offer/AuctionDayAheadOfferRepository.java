package pl.com.tt.flex.server.repository.auction.offer;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionDayAheadOfferRepository extends AbstractJpaRepository<AuctionDayAheadOfferEntity, Long> {

    @Modifying
    @Query("UPDATE AuctionDayAheadOfferEntity ao SET ao.status = :status WHERE ao.id = :auctionOfferId")
    void updateStatus(@Param("status") AuctionOfferStatus status, @Param("auctionOfferId") Long auctionOfferId);

    @Query(value = "SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM AuctionDayAheadOfferEntity daOffer " +
        "WHERE daOffer.auctionDayAhead.type = :type " +
        "AND daOffer.auctionDayAhead.deliveryDate = :deliveryDate " +
        "AND daOffer.schedulingUnit.bsp.id = :bspId ")
    boolean existsDaOfferByAuctionTypeDeliveryDateAndBspId(@Param(value = "type") AuctionDayAheadType type,
                                                           @Param(value = "deliveryDate") Instant deliveryDate,
                                                           @Param(value = "bspId") Long bspId);

    /**
     * @see AuctionOfferStatus#ACCEPTED
     */
    @Query(value = "SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM AuctionDayAheadOfferEntity daOffer " +
        "WHERE daOffer.auctionDayAhead.type = :type " +
        "AND daOffer.auctionDayAhead.deliveryDate = :deliveryDate " +
        "AND daOffer.schedulingUnit.bsp.id = :bspId " +
        "AND daOffer.status = 'ACCEPTED' ")
    boolean existsAcceptedDaOfferByAuctionTypeDeliveryDateAndBspId(@Param(value = "type") AuctionDayAheadType type,
                                                                   @Param(value = "deliveryDate") Instant deliveryDate,
                                                                   @Param(value = "bspId") Long bspId);

    @Query(value = "SELECT daOffer FROM AuctionDayAheadOfferEntity daOffer " +
        "WHERE daOffer.auctionDayAhead.type = :type " +
        "AND  daOffer.auctionDayAhead.deliveryDate = :deliveryDate ")
    List<AuctionDayAheadOfferEntity> findAllByTypeAndDeliveryDate(@Param(value = "type") AuctionDayAheadType type, @Param(value = "deliveryDate") Instant deliveryDate);

    @Query(value = "SELECT daOffer FROM AuctionDayAheadOfferEntity daOffer " +
        "WHERE daOffer.auctionDayAhead.type = :type " +
        "AND  daOffer.auctionDayAhead.deliveryDate >= :deliveryDateFrom " +
        "AND  daOffer.auctionDayAhead.deliveryDate < :deliveryDateTo " +
        "AND  daOffer.auctionDayAhead.product.direction in (:productDirections) " +
        "AND  status in (:statuses)")
    List<AuctionDayAheadOfferEntity> findAllByTypeAndDeliveryDateFromAndTo(@Param(value = "type") AuctionDayAheadType type,
                                                                           @Param(value = "deliveryDateFrom") Instant deliveryDateFrom,
                                                                           @Param(value = "deliveryDateTo") Instant deliveryDateTo,
                                                                           @Param(value = "statuses") List<AuctionOfferStatus> statuses,
                                                                           @Param(value = "productDirections") List<Direction> productDirection);

    @Query(value = "SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM AlgorithmEvaluationEntity ae JOIN ae.daOffers aeOffer WHERE aeOffer.id = :offerId")
    boolean existsAlgorithmEvaluationsForGivenOfferId(@Param(value = "offerId") Long offerId);

    @Modifying
    @Query(value = "DELETE FROM ALG_EVALUATION_DA_OFFERS WHERE da_offer_id = :offerId", nativeQuery = true)
    void deleteDaOffersFromAlgorithmEvaluationByOfferId(@Param(value = "offerId") Long offerId);

    @Modifying
    @Query(value = "DELETE FROM ALG_EVALUATION_CMVC_OFFERS WHERE cmvc_offer_id = :offerId", nativeQuery = true)
    void deleteCmvcOffersFromAlgorithmEvaluationByOfferId(@Param(value = "offerId") Long offerId);

    @Query(value = "SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM AuctionOfferDersEntity offerDers " +
        "WHERE offerDers.offer.auctionDayAhead.deliveryDate = :deliveryDate " +
        "AND offerDers.unit.id = :unitId ")
    boolean existsDaOfferByDeliveryDateAndUnitId(@Param(value = "deliveryDate") Instant deliveryDate, @Param(value = "unitId") Long unitId);

    @Query("SELECT CASE WHEN count(*) > 0 THEN true ELSE false END " +
        "FROM AuctionDayAheadOfferEntity daOffer " +
        "WHERE daOffer.schedulingUnit.id = :schedulingUnitId " +
        "AND daOffer.status IN ('PENDING', 'VOLUMES_VERIFIED')")
    boolean existsBySchedulingUnitIdAndStatusPendingOrVolumesVerified(@Param(value = "schedulingUnitId") Long schedulingUnitId);

    @Query("SELECT CASE WHEN count(*) > 0 THEN true ELSE false END " +
        "FROM AuctionDayAheadOfferEntity daOffer " +
        "WHERE daOffer.schedulingUnit.id = :schedulingUnitId " +
        "AND daOffer.status !='REJECTED' " +
        "AND daOffer.auctionDayAhead.deliveryDate > :deliveryDate")
    boolean existsBySchedulingUnitIdAndStatusNotRejectedAndDeliveryDateAfter(@Param(value = "schedulingUnitId") Long schedulingUnitId,
                                                                             @Param(value = "deliveryDate") Instant deliveryDate);

    @Query("SELECT DISTINCT der FROM AuctionDayAheadOfferEntity daOffer JOIN daOffer.units offerDer JOIN offerDer.unit der WHERE daOffer.id IN :offerIds")
    List<UnitEntity> findDersInOffers(@Param(value = "offerIds") List<Long> offerIds);

    @Query("SELECT ss FROM AuctionDayAheadOfferEntity daOffer JOIN daOffer.units offerDer JOIN UnitSelfScheduleEntity ss ON offerDer.unit.id = ss.unit.id " +
        "AND ss.selfScheduleDate=daOffer.auctionDayAhead.deliveryDate WHERE daOffer.id IN :offerIds")
    List<UnitSelfScheduleEntity> findSelfSchedulesForDersInOffers(@Param(value = "offerIds") List<Long> offerIds);

    @Query("SELECT daOffer FROM AuctionDayAheadOfferEntity daOffer JOIN FETCH daOffer.units WHERE daOffer.id = :id")
    Optional<AuctionDayAheadOfferEntity> findByIdFetchUnits(@Param(value = "id") Long id);

    @Query("SELECT daOffer FROM AuctionDayAheadOfferEntity daOffer JOIN FETCH daOffer.units WHERE daOffer.id IN :ids")
    List<AuctionDayAheadOfferEntity> findByIdInFetchUnits(@Param(value = "ids") List<Long> id);

    @Modifying
    @Query(value = "UPDATE AUCTION_DA_OFFER_BAND_DATA bd SET bd.volume_transferred_to_bm = bd.accepted_volume " +
        "WHERE bd.auction_offer_ders_id IN (SELECT od.id FROM AUCTION_DA_OFFER_DERS od WHERE od.offer_id in :offerIds)", nativeQuery = true)
    void saveVolumeTransferredToBmByOfferIdIn(@Param(value = "offerIds") List<Long> offerIds);
}
