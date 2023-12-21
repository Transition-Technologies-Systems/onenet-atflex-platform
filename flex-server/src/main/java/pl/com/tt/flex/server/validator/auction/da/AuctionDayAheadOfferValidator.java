package pl.com.tt.flex.server.validator.auction.da;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AbstractAuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.UnitSelfScheduleService;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.web.rest.auction.cmvc.AuctionCmvcResource;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.groupingBy;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_ADMIN;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.PENDING;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.VOLUMES_VERIFIED;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType.CAPACITY;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType.ENERGY;
import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.deliveryPeriodContainsHour;
import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.getDeliveryPeriodRange;
import static pl.com.tt.flex.server.util.DateUtil.getHourNumberList;
import static pl.com.tt.flex.server.validator.auction.cmvc.AuctionCmvcOfferValidatorUtil.modifyClosedAuctionRoles;
import static pl.com.tt.flex.server.validator.auction.da.AuctionDayAheadOfferValidatorUtil.isAuctionOpen;
import static pl.com.tt.flex.server.web.rest.auction.da.AuctionDayAheadResource.OFFER_ENTITY_NAME;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Slf4j
@Component
public class AuctionDayAheadOfferValidator {

    private final AuctionDayAheadService auctionDayAheadService;
    private final UserService userService;
    private final SchedulingUnitService schedulingUnitService;
    private final UnitSelfScheduleService unitSelfScheduleService;
    private final ProductService productService;


    public AuctionDayAheadOfferValidator(@Lazy AuctionDayAheadService auctionDayAheadService, UserService userService,
                                         SchedulingUnitService schedulingUnitService, UnitSelfScheduleService unitSelfScheduleService,
                                         @Lazy ProductService productService) {
        this.auctionDayAheadService = auctionDayAheadService;
        this.userService = userService;
        this.schedulingUnitService = schedulingUnitService;
        this.unitSelfScheduleService = unitSelfScheduleService;
        this.productService = productService;
    }

    public void checkValid(AuctionDayAheadOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        if (isNull(offerDTO.getId())) {
            validCreationOfNewOffer(offerDTO, auctionStatus);
        }
        validOfferDers(offerDTO);
        validDeliveryPeriodsOverlap(offerDTO);
        validProductOfAuctionAndOfferedScheduling(offerDTO);
        validAccessToSchedulingUnit(offerDTO);
        validOfferBandData(offerDTO);
        validDerLimits(offerDTO);
    }

    /**
     * Oferty moga edytowac uzytkownicy BSP, TA(Admin), TSO i DSO.
     * Maja oni w swoich kontenerach uprawnienia: FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_EDIT / FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_EDIT
     *
     * @see AuctionDayAheadOfferValidatorUtil overwriteOnlyAllowedOfferDtoFieldsForCurrentUser()
     */
    public void checkModifiable(AuctionDayAheadOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        validOfferStatusForModification(offerDTO);
        validAuctionStatusForModification(offerDTO, auctionStatus);
        checkValid(offerDTO, auctionStatus);
    }

    /**
     * Oferty moga usuwac uzytkownicy BSP i TA(Admin), maja oni w swoich kontenerach uprawnienia:
     * FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_DELETE / FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_DELETE.
     * Dodatkowo nie mozna usunac oferty, jeśli brała udział w obliczeniach algorytmu.
     */
    public void checkDeletable(Long offerId, AuctionStatus auctionStatus) throws ObjectValidationException {
        AuctionDayAheadOfferDTO dbOffer = auctionDayAheadService.findOfferById(offerId).get();
        if (!isAuctionOpen(dbOffer.getType(), auctionStatus)) {
            throw new ObjectValidationException("Cannot delete offer because auction is not open", AUCTION_DA_OFFER_CANNOT_DELETE_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN,
                AuctionCmvcResource.OFFER_ENTITY_NAME, ActivityEvent.AUCTIONS_CMVC_OFFER_DELETED_ERROR, offerId);
        }
        if (auctionDayAheadService.existsAlgorithmEvaluationsForGivenOfferId(offerId)) {
            throw new ObjectValidationException("Cannot delete offer because it's used in calculations", AUCTION_DA_OFFER_CANNOT_DELETE_OFFER_BECAUSE_ITS_USED_IN_CALCULATIONS,
                OFFER_ENTITY_NAME, ActivityEvent.AUCTIONS_DA_OFFER_DELETED_ERROR, offerId);
        }
    }

    public void isUserCanAddBid(AuctionDayAheadOfferDTO auctionDayAheadOfferDTO) throws ObjectValidationException {
        AuctionDayAheadDTO auctionDayAheadDTO = auctionDayAheadService.findById(auctionDayAheadOfferDTO.getAuctionDayAhead().getId()).get();
        UserDTO user = userService.getCurrentUserDTO().get();
        if (!auctionDayAheadService.canCurrentLoggedUserAddNewBid(auctionDayAheadDTO) && !user.getRoles().contains(ROLE_ADMIN)) {
            throw new ObjectValidationException("User cannot add new bid", AUCTION_DA_OFFER_USER_CANNOT_ADD_BID,
                OFFER_ENTITY_NAME, getActivityEvent(auctionDayAheadOfferDTO), auctionDayAheadDTO.getId());
        }
    }

    private void validAccessToSchedulingUnit(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        UserDTO user = userService.getCurrentUserDTO().get();
        if (user.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER) && !schedulingUnitService.existsBySchedulingUnitIdAndBspId(offerDTO.getSchedulingUnit().getId(), user.getFspId())) {
            throw new ObjectValidationException("User has no access to scheduling unit", AUCTION_DA_OFFER_USER_HAS_NO_ACCESS_TO_SCHEDULING_UNIT,
                OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    //oferty moga tworzyc uzytkownicy BSP i TA(Admin)
    //maja oni w swoich kontenerach uprawnienia: FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_CREATE / FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_CREATE
    public void validCreationOfNewOffer(AuctionDayAheadOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        isUserCanAddBid(offerDTO);
        validOfferStatusForNewOffer(offerDTO);
        validAuctionStatusForNewOffer(offerDTO, auctionStatus);
    }

    //przy tworzeniu nowej oferty musi byc ustawiony status PENDING
    private void validOfferStatusForNewOffer(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        if (isNull(offerDTO.getId())) {
            if (!PENDING.equals(offerDTO.getStatus())) {
                throw new ObjectValidationException("Wrong AuctionOfferStatus set for new AuctionDayAheadOffer", AUCTION_DA_OFFER_WRONG_OFFER_STATUS_FOR_NEW_OFFER,
                    OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
            }
        }
    }

    //oferty na aukcje mozna tworzyc tylko przy statusie aukcji OPEN
    private void validAuctionStatusForNewOffer(AuctionDayAheadOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        if (isNull(offerDTO.getId())) {
            if (!isAuctionOpen(offerDTO.getType(), auctionStatus)) {
                throw new ObjectValidationException("Cannot create offer because auction is not open", AUCTION_DA_OFFER_CANNOT_CREATE_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN,
                    OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
            }
        }
    }

    private void validDeliveryPeriodsOverlap(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        if (offerDTO.getDeliveryPeriodFrom().isAfter(offerDTO.getDeliveryPeriodTo())) {
            throw new ObjectValidationException("DeliveryPeriodFrom and DeliverPeriodTo are overlapped",
                AUCTION_DA_OFFER_DELIVERY_PERIOD_FROM_TO_ARE_OVERLAPPED, OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    //aukcja i scheduling musza byc podpiete pod ten sam produkt
    private void validProductOfAuctionAndOfferedScheduling(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        AuctionDayAheadDTO auctionDTO = auctionDayAheadService.findById(offerDTO.getAuctionDayAhead().getId()).get();
        if (!schedulingUnitService.existsBySchedulingUnitIdAndProductId(offerDTO.getSchedulingUnit().getId(), auctionDTO.getProduct().getId())) {
            throw new ObjectValidationException("Product of auction and scheduling must be the same",
                AUCTION_DA_OFFER_PRODUCT_OF_ACUTION_AND_SCHEDULING_MUST_BE_THE_SAME, OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    private void validOfferStatusForModification(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        AbstractAuctionOfferDTO dbOffer = auctionDayAheadService.findOfferById(offerDTO.getId()).get();
        UserDTO user = userService.getCurrentUserDTO().get();
        //mozna edytowac tylko oferty z statusem PENDING
        //uzytkownik z uprawnieniami TA(Admin) moze zawsze edytowac
        if (!Set.of(PENDING, VOLUMES_VERIFIED).contains(dbOffer.getStatus()) && !user.hasRole(ROLE_ADMIN)) {
            throw new ObjectValidationException("Modification of offer is only allowed if offer status is " + PENDING + " or " + VOLUMES_VERIFIED,
                AUCTION_DA_OFFER_MODIFICATION_OF_OFFER_IS_ONLY_ALLOWED_IF_OFFER_STATUS_IS_PENDING_OR_VOLUMES_VERIFIED, AuctionCmvcResource.OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    //oferty mozna edytowac tylko przy statusie aukcji OPEN
    //uzytkownik z uprawnieniami TA(Admin) moze zawsze edytowac
    private void validAuctionStatusForModification(AuctionDayAheadOfferDTO offerDTO, AuctionStatus auctionStatus) throws ObjectValidationException {
        UserDTO user = userService.getCurrentUserDTO().get();
        if (nonNull(offerDTO.getId())) {
            if (!isAuctionOpen(offerDTO.getType(), auctionStatus) && !user.hasAnyRole(modifyClosedAuctionRoles)) {
                throw new ObjectValidationException("Cannot modify offer because auction is not open", AUCTION_DA_OFFER_CANNOT_MODIFY_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN,
                    OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
            }
        }
    }

    private void validOfferDers(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        List<Long> offerDerIds = offerDTO.getDers().stream().map(offerDer -> offerDer.getDer().getId()).collect(Collectors.toList());
        List<UnitMinDTO> schedulingUnitDers = schedulingUnitService.getSchedulingUnitDers(offerDTO.getSchedulingUnit().getId());
        List<Long> schedulingUnitDerIds = schedulingUnitDers.stream().map(UnitMinDTO::getId).collect(Collectors.toList());
        if (offerDerIds.stream().anyMatch(id -> !schedulingUnitDerIds.contains(id))) {
            throw new ObjectValidationException("Der is not joined to scheduling unit", AUCTION_DA_OFFER_SELECTED_DER_IS_NOT_JOINED_TO_SCHEDULING_UNIT,
                OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    private void validOfferBandData(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        checkBandNumbers(offerDTO);
        var auctionType = offerDTO.getType();
        checkRequiredHoursFilled(offerDTO);
        if (auctionType.equals(ENERGY)) {
            checkFirstBandPresent(offerDTO);
            checkBandsFilledInOrder(offerDTO);
        }
        checkPriceInBand(offerDTO);
        checkAcceptedVolumeInBands(offerDTO);
    }

    /**
     * Zaakceptowany wolumen nie może być większy od wolumenu.
     * */
    private void checkAcceptedVolumeInBands(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        boolean isAcceptedVolumeHigherThanVolume = offerDTO.getDers()
            .stream().flatMap(der -> der.getBandData().stream())
            .filter(band -> Objects.nonNull(band.getAcceptedVolume()))
            .filter(band -> Objects.nonNull(band.getVolume()))
            .anyMatch(band -> band.getAcceptedVolume().compareTo(band.getVolume()) > 0);
        if(isAcceptedVolumeHigherThanVolume) {
            throw new ObjectValidationException("The accepted volume cannot be higher than the volume", AUCTION_DA_OFFER_ACCEPTED_VOLUME_HIGHER_THAN_VOLUME,
                OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    private void checkPriceInBand(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        if (!isCurrentUserDSO()) {  //DSO nie widzi cen i nie może ich edytować, dlatego front zwraca w nich null
            checkIfAllNonZeroBandsHavePrices(offerDTO);
        }

        AuctionDayAheadDTO auctionDayAheadDTO = auctionDayAheadService.findById(offerDTO.getAuctionDayAhead().getId())
            .orElseThrow(() -> new IllegalStateException("Cannot find AuctionDayAhead with id: " + offerDTO.getAuctionDayAhead().getId()));

        checkIfBandsZeroHaveVolumeFromSelfSchedule(offerDTO, auctionDayAheadDTO);
        checkIfBandsZeroNotHavePrice(offerDTO);
    }

    // Pasmo 0 musi miec ceny zgodne z Planem Pracy danego DERa
    private void checkIfBandsZeroHaveVolumeFromSelfSchedule(AuctionDayAheadOfferDTO offerDTO, AuctionDayAheadDTO auctionDayAheadDTO) throws ObjectValidationException {
        for (AuctionOfferDersDTO offerDer : offerDTO.getDers()) {
            Optional<UnitSelfScheduleDTO> selfSchedule = unitSelfScheduleService.findByDateAndUnitId(auctionDayAheadDTO.getDeliveryDate(), offerDer.getDer().getId());
            if (selfSchedule.isEmpty()) {
                throw new ObjectValidationException("Cannot find self schedule for unit with id: " + offerDer.getDer().getId(), AUCTION_DA_OFFER_CANNOT_FIND_SELF_SCHEDULE_FOR_DER,
                    OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
            }
            List<Pair<String, BigDecimal>> bandsVolumeGroupingByHours = offerDer.getBandData().stream().filter(band -> band.getBandNumber() == 0)
                .map(band -> Pair.of(band.getHourNumber(), band.getVolume())).collect(Collectors.toList());
            List<Pair<String, BigDecimal>> selfScheduleGroupingByHours = selfSchedule.get().getVolumes().stream().map(volume -> Pair.of(volume.getId(), volume.getValue())).collect(Collectors.toList());

            for (Pair<String, BigDecimal> bandHourVolume : bandsVolumeGroupingByHours) {
                Pair<String, BigDecimal> selfScheduleVolume = selfScheduleGroupingByHours.stream()
                    .filter(selfScheduleHour -> selfScheduleHour.getFirst().equals(bandHourVolume.getFirst())).findFirst()
                    .orElseThrow(() -> new IllegalStateException("Cannot find hour for SelfSchedule"));
                if (selfScheduleVolume.getSecond().compareTo(bandHourVolume.getSecond()) != 0) {
                    log.debug("checkIfBandsZeroHaveVolumeFromSelfSchedule() Band zero not have volume from selfSchedule");
                    log.debug("checkIfBandsZeroHaveVolumeFromSelfSchedule() DerId: {}, HourNr: {}, OfferVolume: {} , SelfScheduleVolume: {}",
                        offerDer.getDer().getId(), bandHourVolume.getFirst(), bandHourVolume.getSecond(), selfScheduleVolume.getSecond());
                    throw new ObjectValidationException("Band zero must have volume from Self Schedule", AUCTION_DA_OFFER_BAND_ZERO_MUST_HAVE_VOLUME_FROM_SELF_SCHEDULE,
                        OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
                }
            }
        }
    }

    // Kazde nie zerowe pasmo musi miec ustawioną cene
    private void checkIfAllNonZeroBandsHavePrices(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        var deliveryPeriod = getDeliveryPeriodRange(offerDTO.getAcceptedDeliveryPeriodFrom(), offerDTO.getAcceptedDeliveryPeriodTo());
        boolean isAnyBandHasNotPrice = offerDTO.getDers().stream()
            .flatMap(der -> der.getBandData().stream())
            .filter(band -> deliveryPeriodContainsHour(deliveryPeriod, band.getHourNumber()))
            .filter(band -> Objects.nonNull(band.getAcceptedVolume()))
            .anyMatch(band -> Objects.isNull(band.getAcceptedPrice()) && band.getBandNumber() != 0);
        if (isAnyBandHasNotPrice) {
            throw new ObjectValidationException("Not set price in all non zero bands", AUCTION_DA_OFFER_NOT_SET_PRICE_IN_ALL_NON_ZERO_BANDS,
                OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    // Pasmo zerowe nie moze miec ustawionej ceny
    private void checkIfBandsZeroNotHavePrice(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        boolean isAnyBandHasNotPrice = offerDTO.getDers().stream().flatMap(der -> der.getBandData().stream())
            .anyMatch(band -> Objects.nonNull(band.getAcceptedPrice()) && band.getBandNumber() == 0);
        if (isAnyBandHasNotPrice) {
            throw new ObjectValidationException("Band zero cannot have price", AUCTION_DA_OFFER_BAND_ZERO_CANNOT_HAVE_PRICE,
                OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    // Dla kazdego pasma sprawdzane jest czy sa uzupelnine wszystkie wymagane godziny w zakresie:
    //  od deliveryPeriodFrom do deliveryPeriodTo
    private void checkAllRequiredHoursInBand(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        List<String> hourNumberList = getHourNumberList(offerDTO.getDeliveryPeriodFrom(), offerDTO.getDeliveryPeriodTo());
        for (AuctionOfferDersDTO der : offerDTO.getDers()) {
            Map<Integer, List<AuctionOfferBandDataDTO>> bandsGroupingByBandNr = der.getBandData().stream().collect(groupingBy(AuctionOfferBandDataDTO::getBandNumber));
            for (Map.Entry<Integer, List<AuctionOfferBandDataDTO>> bandData : bandsGroupingByBandNr.entrySet()) {
                List<String> hoursInBand = bandData.getValue().stream().map(AuctionOfferBandDataDTO::getHourNumber).collect(Collectors.toList());
                if (!CollectionUtils.isEqualCollection(hoursInBand, hourNumberList)) {
                    throw new ObjectValidationException("Not all required hours have been typed", AUCTION_DA_OFFER_NOT_ALL_HOURS_HAVE_BEEN_TYPED,
                        OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
                }
            }
        }
    }

    // Dla każdej godziny z zakresu sprawdzane jest czy uzupełniono przynajmniej jedno pasmo
    private void checkRequiredHoursFilled(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        List<String> hourNumberList = getHourNumberList(offerDTO.getDeliveryPeriodFrom(), offerDTO.getDeliveryPeriodTo());
        var bandsGroupingByHour = getBandsByHour(offerDTO);
        var filledHours = bandsGroupingByHour.keySet();
        if (!CollectionUtils.isEqualCollection(filledHours, hourNumberList)) {
            throw new ObjectValidationException("Fill at least one band", AUCTION_DA_ENERGY_OFFER_NOT_SET_REQUIRED_BANDS,
                OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    // Oferta Energy musi mieć uzupełnione pasmo 1 lub -1
    private void checkFirstBandPresent(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        var bandsGroupingByHour = getBandsByHour(offerDTO);
        for (var bandData : bandsGroupingByHour.entrySet()) {
            var bandsInHour = bandData.getValue().stream().map(AuctionOfferBandDataDTO::getBandNumber).collect(Collectors.toList());
            if (!(bandsInHour.contains(1) || bandsInHour.contains(-1))) {
                throw new ObjectValidationException("Fill at least one band", AUCTION_DA_ENERGY_OFFER_NOT_SET_REQUIRED_BANDS,
                    OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
            }
        }
    }

    // Dla każdego dera i każdej godziny z zakresu sprawdzane jest czy żadne pasmo nie zostało pominięte
    // np. uzupełnienie pasm 1, 3, 4 jest błędne, powinny być ponumerowane 1, 2, 3
    private void checkBandsFilledInOrder(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        for (AuctionOfferDersDTO der : offerDTO.getDers()) {
            var bandsGroupingByHour = der.getBandData().stream().collect(groupingBy(AuctionOfferBandDataDTO::getHourNumber));
            for (var bandData : bandsGroupingByHour.entrySet()) {
                var bandsInHour = bandData.getValue().stream().map(AuctionOfferBandDataDTO::getBandNumber).collect(Collectors.toList());
                var biggestBandNum = bandsInHour.stream().max(Integer::compareTo).orElse(0);
                var smallestBandNum = bandsInHour.stream().min(Integer::compareTo).orElse(0);
                var expectedNumberOfBands = biggestBandNum - smallestBandNum + 1;
                if (bandsInHour.size() < expectedNumberOfBands) {
                    throw new ObjectValidationException("Previous bands must be filled before typing higher numbered band.", AUCTION_DA_ENERGY_OFFER_NOT_SET_REQUIRED_BANDS,
                        OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
                }
            }
        }
    }

    private void checkBandNumbers(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        List<AuctionOfferBandDataDTO> bandData = offerDTO.getDers().stream().flatMap(der -> der.getBandData().stream()).collect(Collectors.toList());
        if (offerDTO.getType().equals(ENERGY)) {
            checkBandNumberInEnergyOffer(offerDTO, bandData);
        }
        if (offerDTO.getType().equals(CAPACITY)) {
            checkRequiredBandNumberInCapacityOffer(offerDTO);
        }

    }

    // Przy skladaniu ofert na aukcje CAPACITY, obowiązkowe pasma to:
    // - Gdy produkt ma kierunek UP: 0, 1
    // - Gdy produkt ma kierunek DOWN: -1, 0
    private void checkRequiredBandNumberInCapacityOffer(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        ProductDTO productDTO = productService.findById(offerDTO.getAuctionDayAhead().getProduct().getId())
            .orElseThrow(() -> new IllegalStateException("Cannot find product with id: " + offerDTO.getAuctionDayAhead().getProduct().getId()));

        for (AuctionOfferDersDTO der : offerDTO.getDers()) {
            Set<Integer> bandNumbers = der.getBandData().stream().map(AuctionOfferBandDataDTO::getBandNumber).collect(Collectors.toSet());
            if (productDTO.getDirection().equals(Direction.UP)) {
                List<Integer> allowedBandsForProductUpDirection = Arrays.asList(0, 1);
                boolean isSetAllRequiredBandInUpDirection = bandNumbers.contains(0) && bandNumbers.contains(1);
                if (!isSetAllRequiredBandInUpDirection || !allowedBandsForProductUpDirection.containsAll(bandNumbers)) {
                    throw new ObjectValidationException("In capacity offers when the product has the UP direction the mandatory bands are 0, 1",
                        AUCTION_DA_CAPACITY_OFFER_INVALID_BAND_NUMBER_VALUE,
                        OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
                }
            }

            if (productDTO.getDirection().equals(Direction.DOWN)) {
                List<Integer> allowedBandsForProductDownDirection = Arrays.asList(-1, 0);
                boolean isSetAllRequiredBandInDownDirection = bandNumbers.contains(-1) && bandNumbers.contains(0);
                if (!isSetAllRequiredBandInDownDirection || !allowedBandsForProductDownDirection.containsAll(bandNumbers)) {
                    throw new ObjectValidationException("In capacity offers when the product has the DOWN direction the mandatory bands are -1, 0",
                        AUCTION_DA_CAPACITY_OFFER_INVALID_BAND_NUMBER_VALUE,
                        OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
                }
            }
        }
    }

    //w aukcjach na ENERGIE oferty moga posiadac pasma od -10 do 10
    private void checkBandNumberInEnergyOffer(AuctionDayAheadOfferDTO offerDTO, List<AuctionOfferBandDataDTO> bandData) throws ObjectValidationException {
        boolean isIllegalBandNumberInEnergyOffer = bandData.stream().anyMatch(band -> band.getBandNumber() > 10 || band.getBandNumber() < -10);
        if (isIllegalBandNumberInEnergyOffer) {
            throw new ObjectValidationException("The band can be between -10 and 10", AUCTION_DA_ENERGY_OFFER_INVALID_BAND_NUMBER_VALUE,
                OFFER_ENTITY_NAME, getActivityEvent(offerDTO), offerDTO.getId());
        }
    }

    /**
     * Sprawdzenie każdego DERa czy limit wolumenu nie został przekroczony.
     * Dla pasm dodatnich suma planów pracy i pasm ma być mniejsza od Pmax (source power)
     * Dla pasm ujemnych różnica planów pracy i pasm ma być większa od Pmin.
     */
    private void validDerLimits(AuctionDayAheadOfferDTO offerDTO) throws ObjectValidationException {
        offerDTO.getDers().forEach(der -> {
            BigDecimal dersPMin = der.getDer().getPMin();
            BigDecimal dersPMax = der.getDer().getSourcePower();
            List<AuctionOfferBandDataDTO> selfSchedules = der.getBandData().stream()
                .filter(auctionOfferBandDataDTO -> auctionOfferBandDataDTO.getBandNumber() == 0)
                .collect(Collectors.toList());
            selfSchedules.forEach(selfSchedule -> {
                List<AuctionOfferBandDataDTO> timestampBands = der.getBandData().stream()
                    .filter(auctionOfferBandDataDTO -> auctionOfferBandDataDTO.getBandNumber() != 0 &&
                        auctionOfferBandDataDTO.getHourNumber().equals(selfSchedule.getHourNumber()))
                    .collect(Collectors.toList());
                BigDecimal timestampSum = timestampBands.stream()
                    .filter(timestamp -> timestamp.getBandNumber() > 0)
                    .map(AuctionOfferBandDataDTO::getAcceptedVolume)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal timestampDiff = timestampBands.stream()
                    .filter(timestamp -> timestamp.getBandNumber() < 0)
                    .map(AuctionOfferBandDataDTO::getAcceptedVolume)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                boolean isSumSmallerThanPmax = selfSchedule.getAcceptedVolume().add(timestampSum).compareTo(dersPMax) <= 0;
                boolean isDiffLargerThanPmin = selfSchedule.getAcceptedVolume().subtract(timestampDiff).compareTo(dersPMin) >= 0;
                if (!isSumSmallerThanPmax || !isDiffLargerThanPmin) {
                    throw new ObjectValidationException("Limit exceeded in at least one timestamp", AUCTION_DA_OFFER_DER_LIMIT_EXCEEDED_IN_AT_LEAST_ONE_TIMESTAMP);
                }
            });
        });
    }

    private Map<String, List<AuctionOfferBandDataDTO>> getBandsByHour(AuctionDayAheadOfferDTO offerDTO) {
        return offerDTO.getDers().stream()
            .map(AuctionOfferDersDTO::getBandData)
            .flatMap(List::stream)
            .filter(band -> band.getBandNumber() != 0)
            .collect(groupingBy(AuctionOfferBandDataDTO::getHourNumber));
    }

    protected ActivityEvent getActivityEvent(AuctionDayAheadOfferDTO offerDTO) {
        return isNull(offerDTO.getId()) ? ActivityEvent.AUCTIONS_DA_OFFER_CREATED_ERROR : ActivityEvent.AUCTIONS_DA_OFFER_UPDATED_ERROR;
    }

    private boolean isCurrentUserDSO() {
        return userService.getCurrentUserDTO().filter(user -> user.hasRole(ROLE_DISTRIBUTION_SYSTEM_OPERATOR)).isPresent();
    }
}

