package pl.com.tt.flex.server.service.auction.da;

import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionReminderType;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionReminderDTO;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service Interface for managing {@link AuctionDayAheadEntity}.
 */
public interface AuctionDayAheadService extends AbstractService<AuctionDayAheadEntity, AuctionDayAheadDTO, Long> {

    void createDayAheadsForSeries(AuctionsSeriesEntity auctionsSeriesEntity);

    void updateAuctionName(String auctionName, Long id);

    AuctionStatus findAuctionStatusById(Long id);

    List<ProductNameMinDTO> findAllProductsUsedInAuctionDayAheadByType(AuctionDayAheadType auctionDayAheadType);

    boolean existsOpenAuctionWithProductId(long productId);

    void updateScheduledAuctions(AuctionsSeriesEntity auctionsSeriesEntity);

    List<AuctionDayAheadDTO> findAllAuctionByTypeAndDeliveryDate(AuctionDayAheadType auctionType, Instant deliveryDate);

    //********************************************************************************** OFFERS ************************************************************************************
    AuctionDayAheadOfferDTO saveOffer(AuctionDayAheadOfferDTO offerDTO, AuctionStatus auctionStatus, boolean isManualUpdate) throws ObjectValidationException;

    Optional<AuctionDayAheadOfferDTO> findOfferById(Long id);

    void deleteOffer(Long offerId, AuctionStatus auctionStatus) throws ObjectValidationException;

    boolean canCurrentLoggedUserAddNewBid(AuctionDayAheadDTO auctionDayAheadDTO);

    List<FspCompanyMinDTO> findBspsWithRegisteredSchedulingUnitsForProductAndAuction(Long productId, Long auctionId);

    Set<AuctionDayAheadOfferDTO> findAllOfferByAuctionTypeAndDeliveryDate(AuctionDayAheadType type, Instant deliveryDate);

    Set<AuctionDayAheadOfferDTO> findAllOffersById(List<Long> offersId);

    AuctionReminderDTO getAuctionOfferReminderForBsp(Long bspId);

    boolean existsAlgorithmEvaluationsForGivenOfferId(Long id);

    void sendReminderInformationToBspUsers(List<MinimalDTO<Long, String>> usersOfBsp, AuctionReminderType type);

    boolean existsDaOfferByDeliveryDateAndUnitId(Instant deliveryDate, Long unitId);

    List<UnitEntity> getDersByOfferIds(List<Long> offerIds);

    List<UnitSelfScheduleEntity> getSelfSchedulesForDersInOffers(List<Long> offerIds);
    //********************************************************************************** OFFERS ************************************************************************************
}
