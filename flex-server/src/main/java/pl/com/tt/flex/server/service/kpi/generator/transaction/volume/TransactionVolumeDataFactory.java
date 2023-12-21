package pl.com.tt.flex.server.service.kpi.generator.transaction.volume;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionCmvcOfferRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.service.kpi.generator.utils.OfferVolumeSumData;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pl.com.tt.flex.server.service.kpi.generator.utils.OfferVolumeSumDataUtils.*;
import static pl.com.tt.flex.server.service.kpi.generator.utils.ValidatorUtils.checkValid;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class TransactionVolumeDataFactory {

    private final AuctionDayAheadOfferRepository auctionDayAheadOfferRepository;
    private final AuctionCmvcOfferRepository auctionCmvcOfferRepository;

    /**
     * Znajduje wszsytkie ofery DA-CAPACITY oraz CMVC z produktami na Moc
     */
    public TransactionVolumeData createForCapacityOffers(Instant acceptedDeliveryFrom, Instant acceptedDeliveryTo) {
        Validate.notNull(acceptedDeliveryTo, "AcceptedDeliveryFrom cannot be null");
        Validate.notNull(acceptedDeliveryFrom, "AcceptedDeliveryTo cannot be null");

        List<AuctionDayAheadOfferEntity> capacityDayAheadOffers = auctionDayAheadOfferRepository.findAllByTypeAndDeliveryDateFromAndTo(
            AuctionDayAheadType.CAPACITY, acceptedDeliveryFrom, acceptedDeliveryTo,
            Collections.singletonList(AuctionOfferStatus.ACCEPTED),
            List.of(Direction.UP, Direction.DOWN)
        );
        List<AuctionCmvcOfferEntity> cmvcOffer = auctionCmvcOfferRepository.findAllByDeliveryDateFromAndToAndProductBidSizeUnitAndStatusIn(
            acceptedDeliveryFrom, acceptedDeliveryTo, Collections.singletonList(ProductBidSizeUnit.kW), Collections.singletonList(AuctionOfferStatus.ACCEPTED));

        return provideTransactionVolumeData(getOffersVolumesSum(capacityDayAheadOffers, cmvcOffer));
    }

    /**
     * Znajduje wszsytkie ofery DA-ENERGY oraz CMVC z produktami na Energie
     */
    public TransactionVolumeData createForEnergyOffers(Instant acceptedDeliveryFrom, Instant acceptedDeliveryTo) {
        Validate.notNull(acceptedDeliveryTo, "AcceptedDeliveryFrom cannot be null");
        Validate.notNull(acceptedDeliveryFrom, "AcceptedDeliveryTo cannot be null");

        List<AuctionDayAheadOfferEntity> energyDayAheadOffers = auctionDayAheadOfferRepository.findAllByTypeAndDeliveryDateFromAndTo(
            AuctionDayAheadType.ENERGY, acceptedDeliveryFrom, acceptedDeliveryTo,
            Collections.singletonList(AuctionOfferStatus.ACCEPTED),
            Collections.singletonList(Direction.UNDEFINED)
        );
        List<AuctionCmvcOfferEntity> cmvcOffer = auctionCmvcOfferRepository.findAllByDeliveryDateFromAndToAndProductBidSizeUnitAndStatusIn(
            acceptedDeliveryFrom, acceptedDeliveryTo, Collections.singletonList(ProductBidSizeUnit.KWH), Collections.singletonList(AuctionOfferStatus.ACCEPTED));

        return provideTransactionVolumeData(getOffersVolumesSum(energyDayAheadOffers, cmvcOffer));
    }

    private TransactionVolumeData provideTransactionVolumeData(List<OfferVolumeSumData> offers) {
        checkValid(offers);
        Map<Pair<String, LocalDate>, BigDecimal> offersSumByProductAndDeliveryDate = getVolumesSumGroupingByProductAndDeliveryDate(offers);
        Map<String, BigDecimal> offersSumByProduct = getVolumesSumGroupingByProduct(offers);
        return new TransactionVolumeData(offersSumByProductAndDeliveryDate, offersSumByProduct);
    }

    /**
     * Oferty pogrupowane po nazwie produktu
     */
    private List<OfferVolumeSumData> getOffersVolumesSum(List<AuctionDayAheadOfferEntity> capacityDayAheadOffers, List<AuctionCmvcOfferEntity> cmvcOffer) {
        List<OfferVolumeSumData> daOfferVolumesData = capacityDayAheadOffers.stream().map(mapDayAheadOfferToOfferVolumeSumWithAcceptedVolume())
                                                                            .collect(Collectors.toUnmodifiableList());
        List<OfferVolumeSumData> cmvcOfferVolumesData = cmvcOffer.stream().map(mapCmvcOffersToOfferVolumeSum()).collect(Collectors.toUnmodifiableList());
        return Stream.concat(daOfferVolumesData.stream(), cmvcOfferVolumesData.stream()).collect(Collectors.toUnmodifiableList());
    }
}
