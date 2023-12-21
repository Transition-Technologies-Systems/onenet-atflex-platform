package pl.com.tt.flex.server.service.kpi.generator.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;
import pl.com.tt.flex.server.util.DateUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.service.kpi.generator.utils.SortUtils.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OfferVolumeSumDataUtils {

    /**
     * Tworzy OfferVolumeSumData z sumowanymi zaakceptowanymi wolumenami dla auckji CMVC
     */
    public static Function<AuctionCmvcOfferEntity, OfferVolumeSumData> mapCmvcOffersToOfferVolumeSum() {
        return o -> new OfferVolumeSumData(
            o.getAuctionCmvc().getProduct().getShortName(),
            DateUtil.toLocalDate(o.getAcceptedDeliveryPeriodFrom()),
            Collections.singletonList(o.getAcceptedVolume())
        );
    }

    /**
     * Tworzy OfferVolumeSumData z sumowanymi zaakceptowanymi wolumenami dla auckji DA
     */
    public static Function<AuctionDayAheadOfferEntity, OfferVolumeSumData> mapDayAheadOfferToOfferVolumeSumWithAcceptedVolume() {
        return o -> {
            String shortName = o.getAuctionDayAhead().getProduct().getShortName();
            LocalDate deliveryDate = DateUtil.toLocalDate(o.getAcceptedDeliveryPeriodFrom());
            List<BigDecimal> volumes = o.getUnits().stream().flatMap(units -> units.getBandData().stream())
                                        .filter(b -> !b.getBandNumber().equals("0"))
                                        .map(AuctionOfferBandDataEntity::getAcceptedVolume)
                                        .collect(Collectors.toList());
            return new OfferVolumeSumData(shortName, deliveryDate, volumes);
        };
    }

    /**
     * Tworzy OfferVolumeSumData z sumowanymi wolumenami dla auckji DA
     */
    public static Function<AuctionDayAheadOfferEntity, OfferVolumeSumData> mapDayAheadOfferToOfferVolumeSumWithVolume() {
        return o -> {
            String shortName = o.getAuctionDayAhead().getProduct().getShortName();
            LocalDate deliveryDate = DateUtil.toLocalDate(o.getAcceptedDeliveryPeriodFrom());
            List<BigDecimal> volumes = o.getUnits().stream().flatMap(units -> units.getBandData().stream())
                                        .filter(b -> !b.getBandNumber().equals("0"))
                                        .map(AuctionOfferBandDataEntity::getVolume)
                                        .collect(Collectors.toList());
            return new OfferVolumeSumData(shortName, deliveryDate, volumes);
        };
    }

    /**
     * Tworzy OfferVolumeSumData z sumowanymi wolumenami wyslanych do BM dla auckji DA
     */
    public static Function<AuctionDayAheadOfferEntity, OfferVolumeSumData> mapDayAheadOfferToOfferVolumeSumWithVolumeTransferredToBm() {
        return o -> {
            String shortName = o.getAuctionDayAhead().getProduct().getShortName();
            LocalDate deliveryDate = DateUtil.toLocalDate(o.getAcceptedDeliveryPeriodFrom());
            List<BigDecimal> volumes = o.getUnits().stream().flatMap(units -> units.getBandData().stream())
                                        .filter(b -> !b.getBandNumber().equals("0"))
                                        .map(AuctionOfferBandDataEntity::getVolumeTransferredToBM)
                                        .collect(Collectors.toList());
            return new OfferVolumeSumData(shortName, deliveryDate, volumes);
        };
    }

    /**
     * Suma wolumenow ofert pogrupowanych po nazwie produktu
     */
    public static Map<String, BigDecimal> getVolumesSumGroupingByProduct(List<OfferVolumeSumData> offers) {
        Map<String, BigDecimal> collect = offers.stream().collect(Collectors.groupingBy(
            OfferVolumeSumData::getProductName,
            Collectors.reducing(BigDecimal.ZERO, OfferVolumeSumData::getVolumeSum, BigDecimal::add)
        ));
        return sortMapWithString(collect);
    }

    /**
     * Suma wolumenow ofert pogrupowanych po dacie dostawy
     */
    public static Map<LocalDate, BigDecimal> getVolumesSumGroupingByDate(List<OfferVolumeSumData> offers) {
        Map<LocalDate, BigDecimal> collect = offers.stream().collect(Collectors.groupingBy(
            OfferVolumeSumData::getDeliveryDate,
            Collectors.reducing(BigDecimal.ZERO, OfferVolumeSumData::getVolumeSum, BigDecimal::add)
        ));
        return sortMapWithLocalDate(collect);
    }

    /**
     * Suma wolumenow ofert pogrupowanych po nazwie produktu oraz dacie dostawy
     */
    public static LinkedHashMap<Pair<String, LocalDate>, BigDecimal> getVolumesSumGroupingByProductAndDeliveryDate(List<OfferVolumeSumData> offers) {
        Map<Pair<String, LocalDate>, BigDecimal> collect = offers.stream().collect(Collectors.groupingBy(
            o -> Pair.of(o.getProductName(), o.getDeliveryDate()),
            Collectors.reducing(BigDecimal.ZERO, OfferVolumeSumData::getVolumeSum, BigDecimal::add)
        ));
        return sortMapWitStringLocalDatePair(collect);
    }
}
