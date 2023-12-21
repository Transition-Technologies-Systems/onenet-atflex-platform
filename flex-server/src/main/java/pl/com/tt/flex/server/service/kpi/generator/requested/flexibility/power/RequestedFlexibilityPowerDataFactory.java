package pl.com.tt.flex.server.service.kpi.generator.requested.flexibility.power;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.repository.auction.cmvc.AuctionCmvcRepository;
import pl.com.tt.flex.server.util.DateUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static pl.com.tt.flex.server.service.kpi.generator.utils.SortUtils.sortMapWitStringLocalDatePair;
import static pl.com.tt.flex.server.service.kpi.generator.utils.ValidatorUtils.checkValid;
import static pl.com.tt.flex.server.util.BigDecimalUtil.min;
import static pl.com.tt.flex.server.util.BigDecimalUtil.sumBigDecimals;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class RequestedFlexibilityPowerDataFactory {

    private final AuctionCmvcRepository auctionCmvcRepository;

    public RequestedFlexibilityPowerData create(Instant acceptedDeliveryFrom, Instant acceptedDeliveryTo) {
        Validate.notNull(acceptedDeliveryFrom, "AcceptedDeliveryTo cannot be null");
        Validate.notNull(acceptedDeliveryTo, "AcceptedDeliveryFrom cannot be null");

        List<AuctionCmvcEntity> cmvcAuctions =
                auctionCmvcRepository.findAllByDeliveryDateFromAndToAndProductBidSizeUnitAndStatusIn(acceptedDeliveryFrom, acceptedDeliveryTo);
        checkValid(cmvcAuctions);
        return new RequestedFlexibilityPowerData(getMaxVolumeGroupingByProductAndDate(cmvcAuctions), BigDecimal.valueOf(cmvcAuctions.size()));
    }

    /**
     * Maksymalne wartości oczekiwane - pogrupowane po nazwie porduktu i dacie dostawy
     */
    private Map<Pair<String, LocalDate>, BigDecimal> getMaxVolumeGroupingByProductAndDate(List<AuctionCmvcEntity> cmvcAuctions) {
        Map<Pair<String, LocalDate>, List<BigDecimal>> maxVolumesForProductAndAuction = getMaxVolumesForProductAndAuction(cmvcAuctions);
        Map<Pair<String, LocalDate>, BigDecimal> maxVolumeGroupingByProductAndDate =
                maxVolumesForProductAndAuction.entrySet().stream().collect(sumBigDecimals());
        maxVolumeGroupingByProductAndDate = sortMapWitStringLocalDatePair(maxVolumeGroupingByProductAndDate);
        return maxVolumeGroupingByProductAndDate;
    }

    /**
     * Maksymalne wartosci dla produktu oraz aukcji pogrupowane po nazwie produktu oraz dacie dostawy.
     * Gdy jest jedna aukcja na dany produkt i dzien wowczas lista BigDecimal jest lista jedną elementową,
     * gdy jest klika aukcji na ta sama date i produkt wowczas w liscie trzymane są max wartosc dla kazdej z aukcji
     */
    private Map<Pair<String, LocalDate>, List<BigDecimal>> getMaxVolumesForProductAndAuction(List<AuctionCmvcEntity> cmvcAuctions) {
        return cmvcAuctions.stream()
                .collect(Collectors.groupingBy(
                        a -> Pair.of(a.getProduct().getShortName(), DateUtil.toLocalDate(a.getDeliveryDateFrom())),
                        Collectors.flatMapping(l -> Stream.of(min(l.getMaxDesiredPower(), l.getProduct().getMaxBidSize())), toList()))
                );
    }
}
