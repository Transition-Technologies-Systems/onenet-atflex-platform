package pl.com.tt.flex.server.validator.algorithm.agnoAlgorithm;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AGNO_ALGORITHM_COMPARISON_ONE_OFFER_PER_COUPLING_POINT_ALLOWED;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferDersEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;

@Component
public class AgnoResultsValidator {

    public void checkValid(Set<AuctionDayAheadOfferEntity> offers) throws ObjectValidationException {
        checkOffersCouplingPointsUnique(offers);
    }

    private void checkOffersCouplingPointsUnique(Set<AuctionDayAheadOfferEntity> offers) throws ObjectValidationException {
        Set<String> tmpSet = new HashSet<>();
        boolean couplingPointsNotUnique = offers.stream()
            .map(AuctionDayAheadOfferEntity::getUnits)
            .map(set -> set.stream().findAny().get())
            .map(AuctionOfferDersEntity::getUnit)
            .map(UnitEntity::getCouplingPointIdTypes)
            .map(set -> set.stream().findAny().get())
            .map(LocalizationTypeEntity::getName)
            .anyMatch(name -> !tmpSet.add(name));
        if (couplingPointsNotUnique) {
            throw new ObjectValidationException("Only one offer per coupling point is allowed", AGNO_ALGORITHM_COMPARISON_ONE_OFFER_PER_COUPLING_POINT_ALLOWED);
        }
    }

}
