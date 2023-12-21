package pl.com.tt.flex.server.validator.auction.cmvc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.auction.offer.cmvc.AuctionCmvcOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.auction.cmvc.AuctionCmvcService;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportData;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.ACCEPTED;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.REJECTED;
import static pl.com.tt.flex.server.util.DateUtil.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Slf4j
@Component
public class AuctionCmvcOfferImportValidator {

    private final AuctionCmvcService auctionCmvcService;

    public AuctionCmvcOfferImportValidator(AuctionCmvcService auctionCmvcService) {
        this.auctionCmvcService = auctionCmvcService;
    }


    public void checkValid(AuctionOfferImportData importBid) throws ObjectValidationException {
        long bidId = checkAndGetBidId(importBid.getId());
//        validBidId(bidId);
        validAuctionStatus(bidId);
        AuctionCmvcOfferDTO dbOffer = auctionCmvcService.findOfferById(bidId)
            .orElseThrow(() -> new RuntimeException("AuctionCmvcOffer not found with id: " + importBid.getId()));
        validAcceptedVolume(importBid, dbOffer);
        validAcceptedDeliveryPeriod(importBid, dbOffer);
        validStatus(importBid, dbOffer);
        validChanges(importBid, dbOffer);
    }

    private void validBidId(Long bidId) throws ObjectValidationException {
        Optional<AuctionCmvcOfferDTO> dbOfferOpt = auctionCmvcService.findOfferById(bidId);
        if (dbOfferOpt.isEmpty()) {
            log.debug("validBidId() Could not find bid with ID: {}", bidId);
            throw new ObjectValidationException("Could not find matching id", IMPORT_OFFER_COULD_NOT_FIND_MATCHING_ID);
        }
    }

    private void validAuctionStatus(Long bidId) throws ObjectValidationException {
        AuctionCmvcOfferDTO dbOffer = auctionCmvcService.findOfferById(bidId)
            .orElseThrow(() -> new RuntimeException("AuctionCmvcOffer not found with id: " + bidId));
        AuctionStatus auctionStatus = auctionCmvcService.findAuctionStatusById(dbOffer.getAuctionCmvc().getId());
        if (AuctionStatus.OPEN.equals(auctionStatus)) {
            log.debug("validAuctionStatus() Could not import bid with ID: {}, because auction with id {} is OPEN",
                bidId, dbOffer.getAuctionCmvc().getId());
            throw new ObjectValidationException("Could not import offer because auction is OPEN", IMPORT_OFFER_COULD_NOT_IMPORT_BECAUSE_AUCTION_IS_OPEN);
        }
    }

    private void validStatus(AuctionOfferImportData importBid, AuctionCmvcOfferDTO dbOffer) throws ObjectValidationException {
        AuctionOfferStatus auctionOfferStatus = validAndGetStatus(importBid);
        List<AuctionOfferStatus> notChangedStatuses = List.of(ACCEPTED, REJECTED);
        if (notChangedStatuses.contains(dbOffer.getStatus()) && !dbOffer.getStatus().equals(auctionOfferStatus)) {
            log.debug("validStatus() Cannot change status of offer: {} when it status is {}",
                dbOffer.getId(), dbOffer.getStatus());
            throw new ObjectValidationException("Cannot change status of offer, when it is already Accepted or Rejected",
                IMPORT_OFFER_COULD_NOT_IMPORT_BECAUSE_NOT_ALLOWED_STATUS_CHANGE);
        }
        List<AuctionOfferStatus> blockingModificationStatuses = Arrays.asList(REJECTED, ACCEPTED);
        if (blockingModificationStatuses.contains(dbOffer.getStatus())) {
            log.debug("validBidStatus() Could not import bid={} with status: {}", dbOffer.getId(), dbOffer.getStatus());
            throw new ObjectValidationException("Cannot import bid with status " + dbOffer.getStatus(), IMPORT_CANNOT_IMPORT_OFFER_WITH_STATUS_REJECTED_OR_ACCEPTED);
        }
        List<AuctionOfferStatus> permittedStatuses = Arrays.asList(AuctionOfferStatus.values());
        if (!permittedStatuses.contains(dbOffer.getStatus())) {
            log.debug("validBidStatus() Could not import bid={} with status: {}", dbOffer.getId(), dbOffer.getStatus());
            throw new ObjectValidationException("Could not identify name of status " + dbOffer.getStatus(), IMPORT_COULD_NOT_IDENTIFY_NAME_OF_STATUS);
        }
    }

    private AuctionOfferStatus validAndGetStatus(AuctionOfferImportData importBid) throws ObjectValidationException {
        // walidacja statusu oferty
        Optional<AuctionOfferStatus> statusByDescriptionOpt = AuctionOfferStatus.findStatusByDescription(importBid.getStatus());
        if (statusByDescriptionOpt.isEmpty()) {
            log.debug("validStatus() Incorrect status {} [status {}]", importBid.getId(), importBid.getStatus());
            throw new ObjectValidationException("Could not identify name of status", IMPORT_COULD_NOT_IDENTIFY_NAME_OF_STATUS);
        }
        return statusByDescriptionOpt.get();
    }

    private void validAcceptedDeliveryPeriod(AuctionOfferImportData importBid, AuctionCmvcOfferDTO dbOffer) throws ObjectValidationException {

        if (!isValidPeriodDate(importBid.getAcceptedDeliveryPeriod())) {
            log.debug("validAcceptedDeliveryPeriod() Wrong format of accepted delivery period in bid={} [acceptedDeliveryPeriod={}]",
                importBid.getId(), importBid.getAcceptedDeliveryPeriod());
            throw new ObjectValidationException("Wrong format of accepted delivery period", IMPORT_WRONG_FORMAT_OF_ACCEPTED_DELIVERY_PERIOD);
        }

        Instant bidAcceptedDeliveryPeriodFrom = getAcceptedDeliveryPeriodFrom(importBid.getAcceptedDeliveryPeriod());
        Instant bidAcceptedDeliveryPeriodTo = getAcceptedDeliveryPeriodTo(importBid.getAcceptedDeliveryPeriod());
        // walidacja pola acceptedDeliveryPeriod gdy deliveryPeriodDivisibility ustawione na false
        boolean isChangeDeliveryPeriod = isChangeDeliveryPeriod(dbOffer, bidAcceptedDeliveryPeriodFrom, bidAcceptedDeliveryPeriodTo);
        if (!dbOffer.getDeliveryPeriodDivisibility() && isChangeDeliveryPeriod) {
            log.debug("validAcceptedDeliveryPeriod() Not allowed to change accepted deliver period in BidId={}", importBid.getId());
            log.debug("validAcceptedDeliveryPeriod() bidId={}, dbAcceptedDeliveryPeriodFrom={}, acceptedDeliveryPeriodFrom={},  dbAcceptedDeliveryPeriodTo={}, acceptedDeliveryPeriodTo={}",
                importBid.getId(), dbOffer.getAcceptedDeliveryPeriodFrom(), bidAcceptedDeliveryPeriodFrom, dbOffer.getAcceptedDeliveryPeriodTo(), bidAcceptedDeliveryPeriodTo);
            throw new ObjectValidationException("Not allowed to change accepted delivery period", IMPORT_NOT_ALLOWED_TO_CHANGE_ACCEPTED_DELIVERY_PERIOD);
        }

        // AcceptedDeliveryPeriod nie może przekraczać zakresu w "Delivery period"
        if (!AuctionCmvcOfferValidator.isValidAcceptedDeliveryPeriod(dbOffer.getDeliveryPeriodFrom(), bidAcceptedDeliveryPeriodFrom,
            dbOffer.getDeliveryPeriodTo(), bidAcceptedDeliveryPeriodTo)) {
            log.debug("validAcceptedDeliveryPeriod() Accepted delivery period cannot exceed the delivery period range. BidId={}", importBid.getId());
            log.debug("validAcceptedDeliveryPeriod() bidId={}, dbAcceptedDeliveryPeriodFrom={}, acceptedDeliveryPeriodFrom={}, dbAcceptedDeliveryPeriodTo={}, acceptedDeliveryPeriodTo={}",
                importBid.getId(), dbOffer.getAcceptedDeliveryPeriodFrom(), bidAcceptedDeliveryPeriodFrom, dbOffer.getAcceptedDeliveryPeriodTo(), bidAcceptedDeliveryPeriodTo);
            throw new ObjectValidationException("Accepted delivery period cannot exceed the delivery period range", IMPORT_ACCEPTED_DELIVERY_PERIOD_CANNOT_EXCEED_THE_DELIVERY_PERIOD_RANGE);
        }

        //AcceptedDeliveryPeriod nie może mieć zerowej długości
        if (!AuctionCmvcOfferValidator.isAcceptedDeliveryPeriodNonZero(bidAcceptedDeliveryPeriodFrom, bidAcceptedDeliveryPeriodTo)) {
            log.debug("validAcceptedDeliveryPeriod() Accepted delivery period cannot have zero length. BidId={}", importBid.getId());
            log.debug("validAcceptedDeliveryPeriod() bidId={}, dbAcceptedDeliveryPeriodFrom={}, acceptedDeliveryPeriodFrom={}, dbAcceptedDeliveryPeriodTo={}, acceptedDeliveryPeriodTo={}",
                importBid.getId(), dbOffer.getAcceptedDeliveryPeriodFrom(), bidAcceptedDeliveryPeriodFrom, dbOffer.getAcceptedDeliveryPeriodTo(), bidAcceptedDeliveryPeriodTo);
            throw new ObjectValidationException("Accepted delivery period cannot have zero length", IMPORT_ACCEPTED_DELIVERY_PERIOD_CANNOT_HAVE_ZERO_LENGTH);
        }
    }

    private boolean isChangeDeliveryPeriod(AuctionCmvcOfferDTO dbOffer, Instant bidAcceptedDeliveryPeriodFrom, Instant bidAcceptedDeliveryPeriodTo) {
        return !dbOffer.getAcceptedDeliveryPeriodFrom().equals(bidAcceptedDeliveryPeriodFrom) ||
            !dbOffer.getAcceptedDeliveryPeriodTo().equals(bidAcceptedDeliveryPeriodTo);
    }

    private void validAcceptedVolume(AuctionOfferImportData importBid, AuctionCmvcOfferDTO dbOffer) throws ObjectValidationException {
        // walidacja pola acceptedVolume gdy deliveryVolume ustawione na false
        if (!validBigDecimalNumber(importBid.getAcceptedVolume())) {
            log.debug("validAcceptedVolume() Wrong format of accepted volume in bidId={} [acceptedVolume={}]",
                importBid.getId(), importBid.getAcceptedVolume());
            throw new ObjectValidationException("Wrong format of accepted volume", IMPORT_WRONG_FORMAT_OF_ACCEPTED_VOLUME);
        }

        BigDecimal bidAcceptedVolume = new BigDecimal(importBid.getAcceptedVolume());
        if (!dbOffer.getVolumeDivisibility() && !dbOffer.getAcceptedVolume().equals(bidAcceptedVolume)) {
            log.debug("validAcceptedVolume() Not allowed to change accepted volume in bidId={} [volumeDivisibility={}, dbAcceptedVolume={}, acceptedVolume={}",
                importBid.getId(), dbOffer.getVolumeDivisibility(), dbOffer.getAcceptedVolume(), bidAcceptedVolume);
            throw new ObjectValidationException("Not allowed to change accepted volume", IMPORT_NOT_ALLOWED_TO_CHANGE_ACCEPTED_VOLUME);
        }
    }

    private void validChanges(AuctionOfferImportData importBid, AuctionCmvcOfferDTO dbOffer) throws ObjectValidationException {
        AuctionOfferStatus auctionOfferStatus = AuctionOfferStatus.findStatusByDescription(importBid.getStatus())
            .orElseThrow(() -> new RuntimeException("AuctionOfferStatus not found with description: " + importBid.getStatus()));
        Instant acceptedDeliveryPeriodFrom = getAcceptedDeliveryPeriodFrom(importBid.getAcceptedDeliveryPeriod());
        Instant acceptedDeliveryPeriodTo = getAcceptedDeliveryPeriodTo(importBid.getAcceptedDeliveryPeriod());
        BigDecimal acceptedVolume = new BigDecimal(importBid.getAcceptedVolume());
        if (auctionOfferStatus.equals(dbOffer.getStatus()) &&
            acceptedDeliveryPeriodFrom.equals(dbOffer.getAcceptedDeliveryPeriodFrom()) &&
            acceptedDeliveryPeriodTo.equals(dbOffer.getAcceptedDeliveryPeriodTo()) &&
            acceptedVolume.equals(dbOffer.getAcceptedVolume())) {
            log.debug("validChanges() Nothing changes in bid with ID: {}", importBid.getId());
            log.debug("validChanges() acceptedDeliveryPeriodFrom={}, dbAcceptedDeliveryPeriodFrom={}", acceptedDeliveryPeriodFrom, dbOffer.getAcceptedDeliveryPeriodFrom());
            log.debug("validChanges() acceptedDeliveryPeriodTo={}, dbAcceptedDeliveryPeriodTo={}", acceptedDeliveryPeriodTo, dbOffer.getAcceptedDeliveryPeriodTo());
            log.debug("validChanges() acceptedVolume={}, dbAcceptedVolume={}", acceptedVolume, dbOffer.getAcceptedVolume());
            throw new ObjectValidationException("Nothing changes in offer", IMPORT_NOTHING_CHANGED);
        }
    }

    /**
     * Sprawdzenie czy zaakceptowany wolumen jest liczbą i ma mniej niż 2 miejsca po przecinku.
     * W przeciwnym wypadku wyrzucamy błąd.
     */
    private boolean validBigDecimalNumber(String stringNumber) {
        BigDecimal bigDecimal;
        try {
            bigDecimal = new BigDecimal(stringNumber);
        } catch (Exception ex) {
            throw new ObjectValidationException(ex.getMessage(), IMPORT_WRONG_FORMAT_OF_ACCEPTED_VOLUME);
        }
        if (bigDecimal.scale() <= 2) {
            return true;
        } else {
            log.debug("getBigDecimalFromString() Wrong format of number: {}", stringNumber);
            return false;
        }
    }

    public static long checkAndGetBidId(String idToParse) throws ObjectValidationException {
        try {
            return Long.parseLong(idToParse);
        } catch (Exception e) {
            log.debug("getBidId: Invalid bid id: {}", idToParse);
            throw new ObjectValidationException("Invalid bid id", IMPORT_OFFER_COULD_NOT_FIND_MATCHING_ID);
        }
    }
}

