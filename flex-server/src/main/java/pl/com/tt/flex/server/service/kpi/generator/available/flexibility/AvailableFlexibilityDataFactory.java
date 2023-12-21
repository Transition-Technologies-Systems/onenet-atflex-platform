package pl.com.tt.flex.server.service.kpi.generator.available.flexibility;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionCmvcOfferRepository;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcMapper;
import pl.com.tt.flex.server.util.DateUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit.KWH;
import static pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit.kW;
import static pl.com.tt.flex.server.service.auction.cmvc.AuctionCmvcServiceImpl.isThereAnyDerWithAuctionLocalization;
import static pl.com.tt.flex.server.service.kpi.generator.utils.SortUtils.sortMapWitStringLocalDatePair;
import static pl.com.tt.flex.server.service.kpi.generator.utils.SortUtils.sortMapWithString;
import static pl.com.tt.flex.server.service.kpi.generator.utils.ValidatorUtils.checkValid;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class AvailableFlexibilityDataFactory {

    private final AuctionCmvcOfferRepository auctionCmvcOfferRepository;
    private final AuctionCmvcMapper auctionCmvcMapper;
    private final FlexPotentialRepository flexPotentialRepository;

    public AvailableFlexibilityData create(Instant dateFrom, Instant dateTo) {
        List<AuctionCmvcOfferEntity> cmvcOffers = auctionCmvcOfferRepository.findAllByDeliveryDateFromAndToAndProductBidSizeUnit(
            dateFrom,
            dateTo,
            List.of(KWH, kW)
        );
        checkValid(cmvcOffers);
        List<AuctionCmvcEntity> auctions = cmvcOffers.stream().map(AuctionCmvcOfferEntity::getAuctionCmvc).distinct().collect(Collectors.toUnmodifiableList());
        Map<AuctionCmvcEntity, BigDecimal> auctionFpSum = findAllRegisteredFlexPotentialsForAuctions(auctions, dateFrom, dateTo);
        Map<AuctionCmvcEntity, BigDecimal> offersGroupingByAuction = cmvcOffers
            .stream()
            .map(offer -> {
                BigDecimal volume = offer.getAcceptedVolume();
                AuctionCmvcEntity auction = offer.getAuctionCmvc();
                return Pair.of(auction, volume);
            }).collect(Collectors.groupingBy(Pair::getKey, Collectors.reducing(BigDecimal.ZERO, Pair::getRight, BigDecimal::add)));
        Map<String, FlexibilityData> flexibilityDataForProduct = createAvailableDateGroupingByProduct(auctionFpSum, offersGroupingByAuction);
        Map<Pair<String, LocalDate>, FlexibilityData> flexibilityDataForProductAndDate = createAvailableDataGroupingByProductAndDate(auctionFpSum, offersGroupingByAuction);
        return new AvailableFlexibilityData(flexibilityDataForProductAndDate, flexibilityDataForProduct);
    }

    /**
     * Metoda ta grupuje FlexibilityData po nazwie produktu oraz dacie dostawy
     */
    private LinkedHashMap<Pair<String, LocalDate>, FlexibilityData> createAvailableDataGroupingByProductAndDate(Map<AuctionCmvcEntity, BigDecimal> auctionFpSum,
                                                                                                                Map<AuctionCmvcEntity, BigDecimal> offersGroupingByAuction) {
        Map<Pair<String, LocalDate>, BigDecimal> auctionVolumesGroupingByProductAndDate = auctionFpSum.entrySet()
                                                                                                      .stream()
                                                                                                      .collect(groupByProductAndDeliveryDate());

        Map<Pair<String, LocalDate>, BigDecimal> offerGroupingByProductAndDate = offersGroupingByAuction.entrySet()
                                                                                                        .stream()
                                                                                                        .collect(groupByProductAndDeliveryDate());
        Map<Pair<String, LocalDate>, FlexibilityData> collect = auctionVolumesGroupingByProductAndDate
            .entrySet().stream()
            .map(entry -> Pair.of(entry.getKey(), new FlexibilityData(entry.getValue(), offerGroupingByProductAndDate.getOrDefault(entry.getKey(), BigDecimal.ZERO))))
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return sortMapWitStringLocalDatePair(collect);
    }

    /**
     * Metoda ta grupuje FlexibilityData po nazwie produktów
     */
    private LinkedHashMap<String, FlexibilityData> createAvailableDateGroupingByProduct(Map<AuctionCmvcEntity, BigDecimal> auctionFpSum,
                                                                                        Map<AuctionCmvcEntity, BigDecimal> offersGroupingByAuction) {
        Map<String, BigDecimal> auctionVolumesGroupingByProduct = auctionFpSum.entrySet().stream().collect(groupByProduct());
        Map<String, BigDecimal> offerGroupingByProduct = offersGroupingByAuction.entrySet().stream().collect(groupByProduct());
        Map<String, FlexibilityData> collect = auctionVolumesGroupingByProduct
            .entrySet()
            .stream()
            .map(entry -> Pair.of(entry.getKey(), new FlexibilityData(entry.getValue(), offerGroupingByProduct.getOrDefault(entry.getKey(), BigDecimal.ZERO))))
            .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
        return sortMapWithString(collect);
    }

    /**
     * Metoda znajduje wszystkie możliwe potencjały możliwe do złożenia w ramch danej aukcji oraz sumuje ich wolumeny
     */
    private Map<AuctionCmvcEntity, BigDecimal> findAllRegisteredFlexPotentialsForAuctions(List<AuctionCmvcEntity> auctionCmvcEntities, Instant validFrom, Instant validTo) {
        List<Long> productIds = auctionCmvcEntities.stream().map(a -> a.getProduct().getId()).distinct().collect(Collectors.toUnmodifiableList());
        List<FlexPotentialEntity> registeredFp = flexPotentialRepository.findAllByProductIdsAndRegisteredIsTrueAndValidFromAndValidTo(productIds, validFrom, validTo);
        return auctionCmvcEntities.stream().map(auction -> {
            AuctionCmvcDTO auctionCmvcDTO = auctionCmvcMapper.toDto(auction);
            List<FlexPotentialEntity> flexPotential = registeredFp.stream()
                                                                  .filter(rfp -> auction.getDeliveryDateFrom().compareTo(rfp.getValidFrom()) >= 0)
                                                                  .filter(rfp -> auction.getDeliveryDateTo().compareTo(rfp.getValidTo()) <= 0)
                                                                  .filter(rfp -> rfp.getProduct().getId().equals(auction.getProduct().getId()))
                                                                  .collect(Collectors.toList());
            List<FlexPotentialEntity> flexPotentialEntityStream = flexPotential.stream()
                                                                               .filter(fp -> isThereAnyDerWithAuctionLocalization(auctionCmvcDTO, fp.getUnits()))
                                                                               .distinct().collect(Collectors.toUnmodifiableList());
            BigDecimal volumeSum = flexPotentialEntityStream.stream().map(FlexPotentialEntity::getVolume).reduce(BigDecimal.ZERO, BigDecimal::add);
            return Pair.of(auction, volumeSum);
        }).collect(Collectors.groupingBy(Pair::getLeft, Collectors.reducing(BigDecimal.ZERO, Pair::getRight, BigDecimal::add)));
    }

    private Collector<Map.Entry<AuctionCmvcEntity, BigDecimal>, ?, Map<String, BigDecimal>> groupByProduct() {
        return Collectors.groupingBy(
            o -> o.getKey().getProduct().getShortName(),
            Collectors.reducing(BigDecimal.ZERO, Map.Entry::getValue, BigDecimal::add)
        );
    }

    private Collector<Map.Entry<AuctionCmvcEntity, BigDecimal>, ?, Map<Pair<String, LocalDate>, BigDecimal>> groupByProductAndDeliveryDate() {
        return Collectors.groupingBy(
            o -> Pair.of(o.getKey().getProduct().getShortName(), DateUtil.toLocalDate(o.getKey().getDeliveryDateFrom())),
            Collectors.reducing(BigDecimal.ZERO, Map.Entry::getValue, BigDecimal::add)
        );
    }
}
