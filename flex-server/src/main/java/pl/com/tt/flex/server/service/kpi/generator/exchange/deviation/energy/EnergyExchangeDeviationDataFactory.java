package pl.com.tt.flex.server.service.kpi.generator.exchange.deviation.energy;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.settlement.SettlementViewEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionCmvcOfferRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.repository.settlement.SettlementViewRepository;
import pl.com.tt.flex.server.service.kpi.generator.exchange.deviation.DeviationData;
import pl.com.tt.flex.server.service.kpi.generator.exchange.deviation.ExchangeDeviationData;
import pl.com.tt.flex.server.service.kpi.generator.exchange.deviation.ExchangeDeviationDataFactory;
import pl.com.tt.flex.server.util.DateUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Transactional(readOnly = true)
public class EnergyExchangeDeviationDataFactory  extends ExchangeDeviationDataFactory {

    public EnergyExchangeDeviationDataFactory(AuctionDayAheadOfferRepository auctionDayAheadOfferRepository, AuctionCmvcOfferRepository auctionCmvcOfferRepository, SettlementViewRepository settlementRepository) {
        super(auctionDayAheadOfferRepository, auctionCmvcOfferRepository, settlementRepository);
    }

    @Override
    public ExchangeDeviationData create(Instant acceptedDeliveryFrom, Instant acceptedDeliveryTo) {
        Validate.notNull(acceptedDeliveryTo, "AcceptedDeliveryFrom cannot be null");
        Validate.notNull(acceptedDeliveryFrom, "AcceptedDeliveryTo cannot be null");

        List<AuctionDayAheadOfferEntity> energyDayAheadOffers = auctionDayAheadOfferRepository.findAllByTypeAndDeliveryDateFromAndTo(
            AuctionDayAheadType.ENERGY, acceptedDeliveryFrom, acceptedDeliveryTo,
            Collections.singletonList(AuctionOfferStatus.ACCEPTED),
            Collections.singletonList(Direction.UNDEFINED)
        );
        List<AuctionCmvcOfferEntity> cmvcOffers = auctionCmvcOfferRepository.findAllByDeliveryDateFromAndToAndProductBidSizeUnitAndStatusIn(
            acceptedDeliveryFrom, acceptedDeliveryTo, Collections.singletonList(ProductBidSizeUnit.KWH), Collections.singletonList(AuctionOfferStatus.ACCEPTED));

        List<Long> offerIds = energyDayAheadOffers.stream().map(AuctionDayAheadOfferEntity::getId).collect(Collectors.toList());
        offerIds.addAll(cmvcOffers.stream().map(AuctionCmvcOfferEntity::getId).collect(Collectors.toList()));

        List<SettlementViewEntity> settlements = settlementRepository.findAllByOfferIdIn(offerIds);
        return provideExchangeDeviationData(energyDayAheadOffers, cmvcOffers, settlements);
    }
}
