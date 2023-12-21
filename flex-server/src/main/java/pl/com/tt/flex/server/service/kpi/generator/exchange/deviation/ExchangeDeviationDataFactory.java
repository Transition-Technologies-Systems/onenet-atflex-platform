package pl.com.tt.flex.server.service.kpi.generator.exchange.deviation;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.settlement.SettlementViewEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionCmvcOfferRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.repository.settlement.SettlementViewRepository;
import pl.com.tt.flex.server.util.DateUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public abstract class ExchangeDeviationDataFactory {

    protected final AuctionDayAheadOfferRepository auctionDayAheadOfferRepository;
    protected final AuctionCmvcOfferRepository auctionCmvcOfferRepository;
    protected final SettlementViewRepository settlementRepository;

    public abstract ExchangeDeviationData create(Instant acceptedDeliveryFrom, Instant acceptedDeliveryTo);

    protected ExchangeDeviationData provideExchangeDeviationData(List<AuctionDayAheadOfferEntity> daOffers,
        List<AuctionCmvcOfferEntity> cmvcOffers, List<SettlementViewEntity> settlements) {
        List<DeviationData> deviationData = daOffers.stream()
            .map(offer -> new DeviationData(offer.getAcceptedVolumeTo().subtract(offer.getAcceptedVolumeFrom()),
                getActivatedPower(offer.getId(), settlements), offer.getAuctionDayAhead().getProduct().getShortName(),
                DateUtil.toLocalDate(offer.getAcceptedDeliveryPeriodFrom()))).collect(Collectors.toList());
        deviationData.addAll(cmvcOffers.stream().map(offer -> new DeviationData(offer.getAcceptedVolume(),
            getActivatedPower(offer.getId(), settlements), offer.getFlexPotential().getProduct().getShortName(),
            DateUtil.toLocalDate(offer.getAcceptedDeliveryPeriodFrom()))).collect(Collectors.toList()));
        return new ExchangeDeviationData(deviationData);
    }

    private BigDecimal getActivatedPower(Long id, List<SettlementViewEntity> settlements) {
        return settlements.stream()
            .filter(settlement -> Objects.equals(settlement.getOfferId(), id))
            .filter(settlement -> Objects.nonNull(settlement.getActivatedVolume()))
            .map(SettlementViewEntity::getActivatedVolume)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
