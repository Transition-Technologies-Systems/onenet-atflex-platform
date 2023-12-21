package pl.com.tt.flex.server.service.kpi.generator.active.participation;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.repository.auction.offer.AuctionOfferRepository;
import pl.com.tt.flex.server.repository.unit.UnitRepository;

import java.math.BigDecimal;

@Component
@Transactional(readOnly = true)
class ActiveParticipationDataFactory {
    private final UnitRepository unitRepository;
    private final AuctionOfferRepository auctionOfferRepository;

    ActiveParticipationDataFactory(UnitRepository unitRepository, AuctionOfferRepository auctionOfferRepository) {
        this.unitRepository = unitRepository;
        this.auctionOfferRepository = auctionOfferRepository;
    }

    ActiveParticipationData create() {
        BigDecimal countCertifiedDers = unitRepository.countCertified();
        BigDecimal countDersUsedInAuctions = auctionOfferRepository.countDersUsedInAuctions();
        return new ActiveParticipationData(countDersUsedInAuctions, countCertifiedDers);
    }
}
