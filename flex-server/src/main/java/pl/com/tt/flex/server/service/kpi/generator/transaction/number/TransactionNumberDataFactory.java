package pl.com.tt.flex.server.service.kpi.generator.transaction.number;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionOfferViewRepository;
import pl.com.tt.flex.server.util.DateUtil;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.service.kpi.generator.utils.SortUtils.sortMapWitStringLocalDatePair;
import static pl.com.tt.flex.server.service.kpi.generator.utils.SortUtils.sortMapWithString;
import static pl.com.tt.flex.server.service.kpi.generator.utils.ValidatorUtils.checkValid;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class TransactionNumberDataFactory {

    private final AuctionOfferViewRepository auctionOfferViewRepository;

    public TransactionNumberData create(Instant acceptedDeliveryFrom, Instant acceptedDeliveryTo) {
        Validate.notNull(acceptedDeliveryTo, "AcceptedDeliveryFrom cannot be null");
        Validate.notNull(acceptedDeliveryFrom, "AcceptedDeliveryTo cannot be null");

        List<AuctionOfferViewEntity> auctionOffers = auctionOfferViewRepository.findByAcceptedDeliveryDateFromAndTo(acceptedDeliveryFrom, acceptedDeliveryTo);
        checkValid(auctionOffers);

        Map<Pair<String, LocalDate>, Long> groupingByProductNameAndDeliveryDate = groupByProductNameAndDeliveryDate(auctionOffers);
        Map<String, Long> groupByProductName = groupByProductName(auctionOffers);
        return new TransactionNumberData(groupingByProductNameAndDeliveryDate, groupByProductName);
    }

    /**
     * Oferty pogrupowane po nazwie produktu oraz dacie dostawy
     */
    private LinkedHashMap<Pair<String, LocalDate>, Long> groupByProductNameAndDeliveryDate(List<AuctionOfferViewEntity> auctionOffers) {
        Map<Pair<String, LocalDate>, Long> collect = auctionOffers.stream().collect(
            Collectors
                .groupingBy(
                    o -> Pair.of(o.getProductName(), DateUtil.toLocalDate(o.getAcceptedDeliveryPeriodFrom())), Collectors.counting())
        );
        return sortMapWitStringLocalDatePair(collect);
    }

    /**
     * Oferty pogrupowane po nazwie produktu
     */
    private Map<String, Long> groupByProductName(List<AuctionOfferViewEntity> auctionOffers) {
        Map<String, Long> collect = auctionOffers.stream().collect(Collectors.groupingBy(AuctionOfferViewEntity::getProductName, Collectors.counting()));
        return sortMapWithString(collect);
    }
}
