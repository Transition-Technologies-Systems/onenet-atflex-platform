package pl.com.tt.flex.server.service.kpi.generator.requested.flexibility.power.fillfactor;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionCmvcOfferRepository;
import pl.com.tt.flex.server.service.kpi.generator.requested.flexibility.power.RequestedFlexibilityPowerData;
import pl.com.tt.flex.server.service.kpi.generator.requested.flexibility.power.RequestedFlexibilityPowerDataFactory;
import pl.com.tt.flex.server.util.DateUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static pl.com.tt.flex.server.service.kpi.generator.utils.SortUtils.sortMapWitStringLocalDatePair;
import static pl.com.tt.flex.server.util.BigDecimalUtil.sumBigDecimals;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
class PowerFillFactorDataFactory {

    private final RequestedFlexibilityPowerDataFactory requestedFlexibilityPowerDataFactory;
    private final AuctionCmvcOfferRepository auctionCmvcOfferRepository;

    PowerFillFactorData create(Instant acceptedDeliveryFrom, Instant acceptedDeliveryTo) {
        Validate.notNull(acceptedDeliveryFrom, "AcceptedDeliveryTo cannot be null");
        Validate.notNull(acceptedDeliveryTo, "AcceptedDeliveryFrom cannot be null");

        RequestedFlexibilityPowerData requestedFlexibilityPowerData = requestedFlexibilityPowerDataFactory.create(acceptedDeliveryFrom, acceptedDeliveryTo);
        List<AuctionCmvcOfferEntity> cmvcOffers = auctionCmvcOfferRepository.findAllByDeliveryDateFromAndToAndProductBidSizeUnitAndStatusIn(
            acceptedDeliveryFrom, acceptedDeliveryTo,
            Arrays.stream(ProductBidSizeUnit.values()).collect(Collectors.toUnmodifiableList()),
            List.of(AuctionOfferStatus.ACCEPTED, AuctionOfferStatus.PENDING, AuctionOfferStatus.REJECTED)
        );

        Map<Pair<String, LocalDate>, BigDecimal> requestedVolume = requestedFlexibilityPowerData.getMaxVolumeGroupingByProductNameAndDeliveryDate();
        Map<Pair<String, LocalDate>, BigDecimal> offeredVolume = getOfferedVolumeGroupingByProductAndDate(cmvcOffers);
        Map<Pair<String, LocalDate>, RequestedAndOfferedVolume> requestedAndOfferedVolumeMap = getRequestedAndOfferedVolumeMap(requestedVolume, offeredVolume);
        return new PowerFillFactorData(requestedAndOfferedVolumeMap, requestedFlexibilityPowerData.getNumberOfAuctions());

    }

    private Map<Pair<String, LocalDate>, RequestedAndOfferedVolume> getRequestedAndOfferedVolumeMap(Map<Pair<String, LocalDate>, BigDecimal> requestedVolume,
                                                                                                    Map<Pair<String, LocalDate>, BigDecimal> offeredVolume) {
        Map<Pair<String, LocalDate>, RequestedAndOfferedVolume> requestedAndOfferedVolumeMap = requestedVolume
            .entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    BigDecimal offered = offeredVolume.getOrDefault(entry.getKey(), BigDecimal.ZERO);
                    BigDecimal requested = entry.getValue();
                    return new RequestedAndOfferedVolume(requested, offered);
                }
            ));
        return sortMapWitStringLocalDatePair(requestedAndOfferedVolumeMap);
    }

    private Map<Pair<String, LocalDate>, BigDecimal> getOfferedVolumeGroupingByProductAndDate(List<AuctionCmvcOfferEntity> cmvcOffers) {
        Map<Pair<String, LocalDate>, List<BigDecimal>> offeredVolumesByProductAndDate =
            cmvcOffers
                .stream()
                .collect(Collectors.groupingBy(
                    o -> Pair.of(o.getAuctionCmvc().getProduct().getShortName(), DateUtil.toLocalDate(o.getAcceptedDeliveryPeriodFrom())),
                    Collectors.mapping(AuctionCmvcOfferEntity::getVolume, toList())
                ));
        return offeredVolumesByProductAndDate.entrySet().stream().collect(sumBigDecimals());
    }
}

