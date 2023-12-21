package pl.com.tt.flex.server.service.kpi.generator.volume.balancing.energy.transferredtobm;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.service.kpi.generator.utils.OfferVolumeSumData;
import pl.com.tt.flex.server.service.kpi.generator.volume.balancing.energy.VolumeBalancingEnergyOffersData;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.product.type.Direction.UNDEFINED;
import static pl.com.tt.flex.server.service.kpi.generator.utils.OfferVolumeSumDataUtils.*;
import static pl.com.tt.flex.server.service.kpi.generator.utils.ValidatorUtils.checkValid;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
class VolumeBalancingEnergyOffersToBmDataFactory {

    private final AuctionDayAheadOfferRepository auctionDayAheadOfferRepository;

    VolumeBalancingEnergyOffersData create(Instant acceptedDeliveryFrom, Instant acceptedDeliveryTo) {
        Validate.notNull(acceptedDeliveryFrom, "AcceptedDeliveryTo cannot be null");
        Validate.notNull(acceptedDeliveryTo, "AcceptedDeliveryFrom cannot be null");

        List<AuctionDayAheadOfferEntity> offers = auctionDayAheadOfferRepository.findAllByTypeAndDeliveryDateFromAndTo(
            AuctionDayAheadType.ENERGY,
            acceptedDeliveryFrom, acceptedDeliveryTo,
            Arrays.stream(AuctionOfferStatus.values()).collect(Collectors.toUnmodifiableList()),
            Collections.singletonList(UNDEFINED)
        );

        List<OfferVolumeSumData> daOfferVolumesData = offers.stream()
                                                            .map(mapDayAheadOfferToOfferVolumeSumWithVolumeTransferredToBm())
                                                            .collect(Collectors.toUnmodifiableList());
        checkValid(daOfferVolumesData);
        return new VolumeBalancingEnergyOffersData(getVolumesSumGroupingByDate(daOfferVolumesData));
    }
}
