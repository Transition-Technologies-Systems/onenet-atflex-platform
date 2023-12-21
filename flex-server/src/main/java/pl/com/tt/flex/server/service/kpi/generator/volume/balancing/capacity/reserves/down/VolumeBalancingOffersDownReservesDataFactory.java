package pl.com.tt.flex.server.service.kpi.generator.volume.balancing.capacity.reserves.down;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.service.kpi.generator.utils.OfferVolumeSumData;
import pl.com.tt.flex.server.service.kpi.generator.volume.balancing.capacity.reserves.VolumeBalancingOffersReservesData;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.product.type.Direction.DOWN;
import static pl.com.tt.flex.server.service.kpi.generator.utils.OfferVolumeSumDataUtils.*;
import static pl.com.tt.flex.server.service.kpi.generator.utils.ValidatorUtils.checkValid;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
class VolumeBalancingOffersDownReservesDataFactory {

    private final AuctionDayAheadOfferRepository auctionDayAheadOfferRepository;

    VolumeBalancingOffersReservesData create(Instant acceptedDeliveryFrom, Instant acceptedDeliveryTo) {
        Validate.notNull(acceptedDeliveryFrom, "AcceptedDeliveryTo cannot be null");
        Validate.notNull(acceptedDeliveryTo, "AcceptedDeliveryFrom cannot be null");

        List<AuctionDayAheadOfferEntity> capacityDayAheadOffers = auctionDayAheadOfferRepository.findAllByTypeAndDeliveryDateFromAndTo(
            AuctionDayAheadType.CAPACITY,
            acceptedDeliveryFrom, acceptedDeliveryTo,
            Arrays.stream(AuctionOfferStatus.values()).collect(Collectors.toUnmodifiableList()),
            Collections.singletonList(DOWN)
        );
        List<OfferVolumeSumData> daOfferVolumesData = capacityDayAheadOffers.stream()
                                                                            .map(mapDayAheadOfferToOfferVolumeSumWithVolume())
                                                                            .collect(Collectors.toUnmodifiableList());
        checkValid(daOfferVolumesData);
        Map<String, BigDecimal> volumesSumGroupingByProduct = getVolumesSumGroupingByProduct(daOfferVolumesData);
        LinkedHashMap<Pair<String, LocalDate>, BigDecimal> volumesSumGroupingByProductAndDeliveryDate = getVolumesSumGroupingByProductAndDeliveryDate(daOfferVolumesData);
        return new VolumeBalancingOffersReservesData(volumesSumGroupingByProductAndDeliveryDate, volumesSumGroupingByProduct);
    }
}
