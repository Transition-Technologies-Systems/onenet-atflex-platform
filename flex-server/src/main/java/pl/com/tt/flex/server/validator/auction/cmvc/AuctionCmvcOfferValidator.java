package pl.com.tt.flex.server.validator.auction.cmvc;


import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.cmvc.AuctionCmvcOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.service.auction.cmvc.AuctionCmvcService;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.web.rest.auction.da.AuctionDayAheadResource;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static pl.com.tt.flex.model.security.permission.Role.*;
import static pl.com.tt.flex.server.validator.auction.cmvc.AuctionCmvcOfferValidatorUtil.isAuctionOpen;
import static pl.com.tt.flex.server.validator.auction.cmvc.AuctionCmvcOfferValidatorUtil.modifyClosedAuctionRoles;
import static pl.com.tt.flex.server.web.rest.auction.cmvc.AuctionCmvcResource.OFFER_ENTITY_NAME;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Component
public class AuctionCmvcOfferValidator {

    private final AuctionCmvcService auctionCmvcService;
    private final UserService userService;
    private final FlexPotentialService flexPotentialService;

    public AuctionCmvcOfferValidator(@Lazy AuctionCmvcService auctionCmvcService, UserService userService, FlexPotentialService flexPotentialService) {
        this.auctionCmvcService = auctionCmvcService;
        this.userService = userService;
        this.flexPotentialService = flexPotentialService;
    }

    public void checkValid(AuctionCmvcOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        if (isNull(offerDTO.getId())) {
            validCreationOfNewOffer(offerDTO, auctionStatus);
        }
        validOfferType(offerDTO);
        validOfferVolume(offerDTO);
        validDeliveryPeriodsOverlap(offerDTO);
        validDeliveryPeriod(offerDTO);
        validProductOfAuctionAndPotential(offerDTO);
        validAcceptedVolume(offerDTO);
        validAcceptedDeliveryPeriod(offerDTO);
        validAcceptedDeliveryPeriodsOverlap(offerDTO);
        validAccessToFlexPotential(offerDTO);
    }

    /**
     * Oferty moga edytowac uzytkownicy FSP, FSPA, TA(Admin), TSO i DSO.
     * Maja oni w swoich kontenerach uprawnienia: FLEX_USER_AUCTIONS_CMVC_OFFER_EDIT / FLEX_ADMIN_AUCTIONS_CMVC_OFFER_EDIT
     *
     * @see AuctionCmvcOfferValidatorUtil overwriteOnlyAllowedOfferDtoFieldsForCurrentUser()
     */
    public void checkModifiable(AuctionCmvcOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        validOfferStatusForModification(offerDTO);
        validAuctionStatusForModification(offerDTO, auctionStatus);
        validAcceptedVolume(offerDTO, auctionStatus);
        validAcceptedDeliveryPeriod(offerDTO, auctionStatus);
        checkValid(offerDTO, auctionStatus);
    }

    /**
     * Oferty moga usuwac uzytkownicy FSP, FSPA i TA(Admin), maja oni w swoich kontenerach uprawnienia:
     * FLEX_USER_AUCTIONS_CMVC_OFFER_DELETE / FLEX_ADMIN_AUCTIONS_CMVC_OFFER_DELETE
     */
    public void checkDeletable(Long offerId, AuctionStatus auctionStatus) throws ObjectValidationException {
        if (!isAuctionOpen(auctionStatus)) {
            throw new ObjectValidationException("Cannot delete offer because auction is not open", AUCTION_CMVC_OFFER_CANNOT_DELETE_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN,
                OFFER_ENTITY_NAME, ActivityEvent.AUCTIONS_CMVC_OFFER_DELETED_ERROR, offerId);
        }
    }

    //oferty moga tworzyc uzytkownicy FSP/FSPA i TA(Admin)
    //maja oni w swoich kontenerach uprawnienia: FLEX_ADMIN_AUCTIONS_CMVC_OFFER_CREATE / FLEX_USER_AUCTIONS_CMVC_OFFER_CREATE
    private void validCreationOfNewOffer(AuctionCmvcOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        isUserCanAddBid(offerDTO);
        validOfferStatusForNewOffer(offerDTO);
        validAuctionStatusForNewOffer(offerDTO, auctionStatus);
        validOfferAcceptedFieldsForNewOffer(offerDTO);
    }

    //przy tworzeniu nowej oferty musi byc ustawiony status PENDING
    private void validOfferStatusForNewOffer(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        if (isNull(offerDTO.getId())) {
            if (!AuctionOfferStatus.PENDING.equals(offerDTO.getStatus())) {
                throw new ObjectValidationException("Wrong AuctionOfferStatus set for new AuctionCmvcOffer", AUCTION_CMVC_OFFER_WRONG_OFFER_STATUS_FOR_NEW_OFFER,
                    OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
            }
        }
    }

    //oferty na aukcje mozna tworzyc tylko przy statusie aukcji OPEN
    private void validAuctionStatusForNewOffer(AuctionCmvcOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        if (isNull(offerDTO.getId())) {
            if (!isAuctionOpen(auctionStatus)) {
                throw new ObjectValidationException("Cannot create offer because auction is not open", AUCTION_CMVC_OFFER_CANNOT_CREATE_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN,
                    OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
            }
        }
    }

    //pola accepted maja byc przeklejone z zwyklych np. volume --> volumeAccepted
    private void validOfferAcceptedFieldsForNewOffer(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        if (!offerDTO.getVolume().equals(offerDTO.getAcceptedVolume()) ||
            !offerDTO.getDeliveryPeriodFrom().equals(offerDTO.getAcceptedDeliveryPeriodFrom()) ||
            !offerDTO.getDeliveryPeriodTo().equals(offerDTO.getAcceptedDeliveryPeriodTo())) {
            throw new ObjectValidationException("Fields with prefix 'accepted' are not equal to its substitutes while creating new offer",
                AUCTION_CMVC_OFFER_ACCEPTED_FIELDS_WHILE_CREATING_NEW_OFFER_ARE_NOT_EQUAL_TO_ITS_SUBSTITUTES, OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    //aukcje Cmvc sa tylko na moc
    private void validOfferType(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        if (!AuctionOfferType.CAPACITY.equals(offerDTO.getType())) {
            throw new ObjectValidationException("Illegal AuctionCmvcOffer type",
                AUCTION_CMVC_OFFER_ILLEGAL_TYPE, OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }


    //Walidujemy po 3 zakresach;
    //0 - "Volume" z "Flex register"
    //Min desired power - Max desired power z aukcji
    //Min bid size - Max bid size z produktu
    //
    //min przedziału - z 3 zakresów wybrać najwyższą wartość dolną
    //max przedziału - z 3 zakresów wybrać najmniejszą wartość górną
    private void validOfferVolume(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        AuctionCmvcDTO auctionCmvc = auctionCmvcService.findById(offerDTO.getAuctionCmvc().getId()).get();
        FlexPotentialDTO flexPotentialDTO = flexPotentialService.findById(offerDTO.getFlexPotential().getId()).get();

        BigDecimal min = getMinimum(auctionCmvc);
        BigDecimal max = getMaximum(auctionCmvc, flexPotentialDTO);

        if (min.compareTo(offerDTO.getVolume()) > 0 || offerDTO.getVolume().compareTo(max) > 0) {
            throw new ObjectValidationException("Value should be set between " + min + " and " + max,
                AUCTION_CMVC_OFFER_VOLUME_OF_OFFER_CANNOT_EXCEED_THE_RANGE, OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    private static BigDecimal getMinimum(AuctionCmvcDTO auctionCmvc) {
        BigDecimal auctionMinDesiredPower = auctionCmvc.getMinDesiredPower();
        BigDecimal productMinBidSize = auctionCmvc.getProduct().getMinBidSize();

        List<BigDecimal> values = Lists.newArrayList(BigDecimal.ZERO, auctionMinDesiredPower, productMinBidSize);
        return values.stream().filter(Objects::nonNull).max(Comparator.naturalOrder()).get();
    }

    private static BigDecimal getMaximum(AuctionCmvcDTO auctionCmvc, FlexPotentialDTO flexPotentialDTO) {
        BigDecimal registerMaxVolume = flexPotentialDTO.getVolume();
        BigDecimal auctionMaxDesiredPower = auctionCmvc.getMaxDesiredPower();
        BigDecimal productMaxBidSize = auctionCmvc.getProduct().getMaxBidSize();

        List<BigDecimal> values = Lists.newArrayList(registerMaxVolume, auctionMaxDesiredPower, productMaxBidSize);
        return values.stream().filter(Objects::nonNull).min(Comparator.naturalOrder()).get();
    }

    private void validDeliveryPeriodsOverlap(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        if (offerDTO.getDeliveryPeriodFrom().isAfter(offerDTO.getDeliveryPeriodTo())) {
            throw new ObjectValidationException("DeliveryPeriodFrom and DeliverPeriodTo are overlapped",
                AUCTION_CMVC_OFFER_DELIVERY_PERIOD_FROM_TO_ARE_OVERLAPPED, OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    private void validDeliveryPeriod(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        int minuteOfDeliveryPeriodFrom = offerDTO.getDeliveryPeriodFrom().atZone(ZoneOffset.UTC).getMinute();
        int minuteOfDeliveryPeriodTo= offerDTO.getDeliveryPeriodTo().atZone(ZoneOffset.UTC).getMinute();
        if (Math.floorMod(minuteOfDeliveryPeriodFrom, 15) != 0 || Math.floorMod(minuteOfDeliveryPeriodTo, 15) != 0) {
            throw new ObjectValidationException("Delivery period - Invalid minute of hour",
                AUCTION_CMVC_OFFER_DELIVERY_PERIOD_HAS_INVALID_MINUTE,
                AuctionDayAheadResource.OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    //aukcja i potencjal musza byc podpiete pod ten sam produkt
    private void validProductOfAuctionAndPotential(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        AuctionCmvcDTO auctionDTO = auctionCmvcService.findById(offerDTO.getAuctionCmvc().getId()).get();
        FlexPotentialDTO flexPotentialDTO = flexPotentialService.findById(offerDTO.getFlexPotential().getId()).get();
        if (!auctionDTO.getProduct().getId().equals(flexPotentialDTO.getProduct().getId())) {
            throw new ObjectValidationException("Product of auction and potential must be the same",
                AUCTION_CMVC_OFFER_PRODUCT_OF_ACUTION_AND_POTENTIAL_MUST_BE_THE_SAME, OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    private void validOfferStatusForModification(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        AuctionCmvcOfferDTO dbOffer = auctionCmvcService.findOfferById(offerDTO.getId()).get();
        UserDTO user = userService.getCurrentUserDTO().get();
        //mozna edytowac tylko oferty z statusem PENDING
        //uzytkownik z uprawnieniami TA(Admin) moze zawsze edytowac
        if (!dbOffer.getStatus().equals(AuctionOfferStatus.PENDING) && !user.hasRole(ROLE_ADMIN)) {
            throw new ObjectValidationException("Modification of offer is only allowed if offer status is: " + AuctionOfferStatus.PENDING,
                AUCTION_CMVC_OFFER_MODIFICATION_OF_OFFER_IS_ONLY_ALLOWED_IF_OFFER_STATUS_IS_PENDING, OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    //oferty mozna edytowac tylko przy statusie aukcji OPEN
    //uzytkownik z uprawnieniami TA(Admin) moze zawsze edytowac
    private void validAuctionStatusForModification(AuctionCmvcOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        UserDTO user = userService.getCurrentUserDTO().get();
        if (nonNull(offerDTO.getId())) {
            if (!isAuctionOpen(auctionStatus) && !user.hasAnyRole(modifyClosedAuctionRoles)) {
                throw new ObjectValidationException("Cannot modify offer because auction is not open", AUCTION_CMVC_OFFER_CANNOT_MODIFY_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN,
                    AuctionDayAheadResource.OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
            }
        }
    }

    /**
     * Accepted volume - dostępne tylko jeżeli "Volume divisibility" jest ustawione na "Yes", status aukcji jest "Closed",
     */
    private void validAcceptedVolume(AuctionCmvcOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        if (auctionStatus.equals(AuctionStatus.CLOSED) && !offerDTO.getVolumeDivisibility()) {
            BigDecimal acceptedVolumeFromDatabase = auctionCmvcService.findOfferById(offerDTO.getId()).get().getAcceptedVolume();
            if (!offerDTO.getAcceptedVolume().equals(acceptedVolumeFromDatabase)) {
                throw new ObjectValidationException("Cannot modify acceptedVolume if volume divisibility is false or auction isn't closed",
                    AUCTION_CMVC_OFFER_CANNOT_MODIFY_ACCPTED_VOLUME_BECAUSE_VOLUME_DIVISIBILITY_IS_FALSE_OR_AUCTION_IS_NOT_CLOSED,
                    AuctionDayAheadResource.OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
            }
        }
    }

    /**
     * AcceptedVolume nie może być wyższe niż wartość w "Volume"
     * oraz z 3 zakresow (Fr, auckja, produkt) wybieramy najwyższą wartość dolną, acceptedVolume nie może być mniejsze od tej wartości
     */
    private void validAcceptedVolume(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        AuctionCmvcDTO auctionCmvc = auctionCmvcService.findById(offerDTO.getAuctionCmvc().getId()).get();
        FlexPotentialDTO flexPotentialDTO = flexPotentialService.findById(offerDTO.getFlexPotential().getId()).get();
        BigDecimal min = getMinimum(auctionCmvc);
        BigDecimal max = getMaximum(auctionCmvc, flexPotentialDTO).min(offerDTO.getVolume());
        if (min.compareTo(offerDTO.getAcceptedVolume()) > 0 || offerDTO.getAcceptedVolume().compareTo(max) > 0) {
            throw new ObjectValidationException("Value should be set between" + min + " and " + max,
                AUCTION_CMVC_OFFER_ACCEPTED_VOLUME_OF_OFFER_CANNOT_EXCEED_THE_RANGE, OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    /**
     * Accepted delivery period - dostępne tylko jeżeli "Delivery period divisibility" jest ustawione na "Yes", status aukcji jest "Closed",
     */
    private void validAcceptedDeliveryPeriod(AuctionCmvcOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        if (auctionStatus.equals(AuctionStatus.CLOSED) && !offerDTO.getDeliveryPeriodDivisibility()) {
            Instant acceptedDeliveryPeriodFromFromDatabase = auctionCmvcService.findOfferById(offerDTO.getId()).get().getAcceptedDeliveryPeriodFrom();
            Instant acceptedDeliveryPeriodToFromDatabase = auctionCmvcService.findOfferById(offerDTO.getId()).get().getAcceptedDeliveryPeriodTo();
            if (!offerDTO.getAcceptedDeliveryPeriodFrom().equals(acceptedDeliveryPeriodFromFromDatabase) || !offerDTO.getAcceptedDeliveryPeriodTo().equals(acceptedDeliveryPeriodToFromDatabase)) {
                throw new ObjectValidationException("Cannot modify acceptedDeliveryPeriod if delivery period divisibility is false or auction isn't closed",
                    AUCTION_CMVC_OFFER_CANNOT_MODIFY_ACCPTED_DELIVERY_PERIOD_BECAUSE_DELIVERY_PERIOD_DIVISIBILITY_IS_FALSE_OR_AUCTION_IS_NOT_CLOSED,
                    AuctionDayAheadResource.OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
            }
        }
    }

    /**
     * AcceptedDeliveryPeriod nie może przekraczać zakresu w "Delivery period"
     */
    private void validAcceptedDeliveryPeriod(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        if (!isValidAcceptedDeliveryPeriod(offerDTO.getDeliveryPeriodFrom(), offerDTO.getAcceptedDeliveryPeriodFrom(), offerDTO.getDeliveryPeriodTo(), offerDTO.getAcceptedDeliveryPeriodTo())) {
            throw new ObjectValidationException("Accepted delivery period cannot exceed the delivery period range ",
                AUCTION_CMVC_OFFER_ACCEPTED_DELIVERY_PERIOD_CANNOT_EXCEED_THE_DELIVERY_PERIOD_RANGE, OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }

        int minuteOfAcceptedDeliveryPeriodFrom = offerDTO.getAcceptedDeliveryPeriodFrom().atZone(ZoneOffset.UTC).getMinute();
        int minuteOfAcceptedDeliveryPeriodTo= offerDTO.getAcceptedDeliveryPeriodTo().atZone(ZoneOffset.UTC).getMinute();
        if (Math.floorMod(minuteOfAcceptedDeliveryPeriodFrom, 15) != 0 || Math.floorMod(minuteOfAcceptedDeliveryPeriodTo, 15) != 0) {
            throw new ObjectValidationException("Accepted delivery period - Invalid minute of hour",
                AUCTION_CMVC_OFFER_ACCEPTED_DELIVERY_PERIOD_HAS_INVALID_MINUTE,
                AuctionDayAheadResource.OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    public static boolean isValidAcceptedDeliveryPeriod(Instant deliveryPeriodFrom, Instant acceptedDeliveryPeriodFrom, Instant deliveryPeriodTo, Instant acceptedDeliveryPeriodTo) {
        return acceptedDeliveryPeriodFrom.compareTo(deliveryPeriodFrom) >= 0 && acceptedDeliveryPeriodTo.compareTo(deliveryPeriodTo) <= 0;
    }

    public static boolean isAcceptedDeliveryPeriodNonZero(Instant acceptedDeliveryPeriodFrom, Instant acceptedDeliveryPeriodTo) {
        return acceptedDeliveryPeriodFrom.compareTo(acceptedDeliveryPeriodTo) != 0;
    }

    private void validAcceptedDeliveryPeriodsOverlap(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        if (offerDTO.getAcceptedDeliveryPeriodFrom().isAfter(offerDTO.getAcceptedDeliveryPeriodTo())) {
            throw new ObjectValidationException("AcceptedDeliveryPeriodFrom and AcceptedDeliverPeriodTo are overlapped",
                AUCTION_CMVC_OFFER_ACCEPTED_DELIVERY_PERIOD_FROM_TO_ARE_OVERLAPPED, OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    private void validAccessToFlexPotential(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        UserDTO user = userService.getCurrentUserDTO().get();
        if (user.hasAnyRole(Sets.newHashSet(Role.ROLE_FLEX_SERVICE_PROVIDER, ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) &&
            !flexPotentialService.existsByFlexPotentialIdAndFspId(offerDTO.getFlexPotential().getId(), user.getFspId())) {
            throw new ObjectValidationException("User has no access to flex potential", AUCTION_CMVC_OFFER_USER_HAS_NO_ACCESS_TO_FLEX_POTENTIAL,
                AuctionDayAheadResource.OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    private void isUserCanAddBid(AuctionCmvcOfferDTO offerDTO) throws ObjectValidationException {
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcService.findById(offerDTO.getAuctionCmvc().getId()).get();
        if (isFspOrganisationUser(userService.getCurrentUser()) && !auctionCmvcService.canCurrentLoggedUserAddNewBid(auctionCmvcDTO, auctionCmvcDTO.getProduct().getId())) {
            throw new ObjectValidationException("User cannot add new bid", AUCTION_DA_OFFER_USER_CANNOT_ADD_BID,
                OFFER_ENTITY_NAME, getActivityEvent(offerDTO), auctionCmvcDTO.getId());
        }
    }

    public boolean isFspOrganisationUser(UserEntity maybefspUser) {
        return CollectionUtils.containsAny(maybefspUser.getRoles(), FSP_ORGANISATIONS_ROLES) && !maybefspUser.getRoles().contains(ROLE_ADMIN);
    }

    private ActivityEvent getActivityEvent(AuctionCmvcOfferDTO offerDTO) {
        return isNull(offerDTO.getId()) ? ActivityEvent.AUCTIONS_CMVC_OFFER_CREATED_ERROR : ActivityEvent.AUCTIONS_CMVC_OFFER_UPDATED_ERROR;
    }
}

