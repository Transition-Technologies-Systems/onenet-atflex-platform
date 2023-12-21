package pl.com.tt.flex.server.validator.auction.da;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionsSeriesDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.repository.auction.da.AuctionDayAheadViewRepository;
import pl.com.tt.flex.server.service.auction.da.series.AuctionsSeriesService;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.ObjectValidator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static pl.com.tt.flex.server.web.rest.auction.da.AuctionsSeriesResource.ENTITY_NAME;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Component
@RequiredArgsConstructor
public class AuctionsSeriesValidator implements ObjectValidator<AuctionsSeriesDTO, Long> {

    private final ProductService productService;
    private final AuctionsSeriesService auctionsSeriesService;
    private final AuctionDayAheadViewRepository auctionDayAheadViewRepository;

    @Override
    public void checkValid(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        checkNotNullConstraint(auctionsSeriesDTO);
        checkNullConstraint(auctionsSeriesDTO);
        checkGateOpeningTime(auctionsSeriesDTO);
        validProduct(auctionsSeriesDTO);
        validCapacity(auctionsSeriesDTO);
        validEnergy(auctionsSeriesDTO);
        validAuctionDate(auctionsSeriesDTO);
    }

    private void validProduct(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        ProductDTO productDTO = productService.findById(auctionsSeriesDTO.getProduct().getId()).get();
        if (!productDTO.isBalancing()) {
            throw new ObjectValidationException("Product does not have a balancing flag",
                PRODUCT_DOES_NOT_HAVE_BALANCING_FLAG, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
        if (auctionsSeriesDTO.getType().equals(AuctionDayAheadType.CAPACITY)) {
            List<Direction> capacityAuctionProductDirection = Arrays.asList(Direction.DOWN, Direction.UP);
            if (!capacityAuctionProductDirection.contains(productDTO.getDirection())) {
                throw new ObjectValidationException("Allowed product direction in Capacity Series: DOWN, UP",
                    CAPACITY_SERIES_HAS_PRODUCT_WITH_WRONG_DIRECTION, ENTITY_NAME,
                    getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
            }
        } else { // Seria typu ENERGY
            if (!productDTO.getDirection().equals(Direction.UNDEFINED)) {
                throw new ObjectValidationException("Allowed product direction in Capacity Series: UNDEFINED",
                    ENERGY_SERIES_HAS_PRODUCT_WITH_WRONG_DIRECTION, ENTITY_NAME,
                    getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
            }
        }
    }

    @Override
    public void checkModifiable(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        checkNotEditableFields(auctionsSeriesDTO);
        checkModifyLastAuctionDate(auctionsSeriesDTO);
        checkModifyFirstAuctionDate(auctionsSeriesDTO);
        checkValid(auctionsSeriesDTO);
    }

    private void validCapacity(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        if (Objects.equals(auctionsSeriesDTO.getType(), AuctionDayAheadType.CAPACITY)) {
            validCapacityGateTime(auctionsSeriesDTO);
            validCapacityAvailability(auctionsSeriesDTO);
            validDesiredCapacity(auctionsSeriesDTO);
        }
    }

    private void validEnergy(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        if (Objects.equals(auctionsSeriesDTO.getType(), AuctionDayAheadType.ENERGY)) {
            validEnergyGateTime(auctionsSeriesDTO);
            validEnergyAvailability(auctionsSeriesDTO);
            validDesiredEnergy(auctionsSeriesDTO);
        }
    }

    private void validAuctionDate(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        if (auctionsSeriesDTO.getId() == null && LocalDate.ofInstant(InstantUtil.now().plus(1, ChronoUnit.DAYS), ZoneId.systemDefault())
            .isAfter(LocalDate.ofInstant(auctionsSeriesDTO.getFirstAuctionDate(), ZoneId.systemDefault()))) {
            throw new ObjectValidationException("The first auctions may start tomorrow at the earliest",
                FIRST_AUCTION_DATE_MAY_START_TOMORROW_AT_THE_EARLIEST, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
        if (auctionsSeriesDTO.getLastAuctionDate().isBefore(auctionsSeriesDTO.getFirstAuctionDate())) {
            throw new ObjectValidationException("Last auction date is before first auction date",
                LAST_AUCTION_DATE_IS_BEFORE_FIRST_AUCTION_DATE, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }

        ProductDTO productDTO = productService.findById(auctionsSeriesDTO.getProduct().getId()).get();
        if (auctionsSeriesDTO.getFirstAuctionDate().isBefore(productDTO.getValidFrom())) {
            throw new ObjectValidationException("First auction date is before product valid from date",
                FIRST_AUCTION_DATE_IS_BEFORE_PRODUCT_VALID_FROM, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
        if (auctionsSeriesDTO.getLastAuctionDate().isAfter(productDTO.getValidTo())) {
            throw new ObjectValidationException("Last auction date is after product valid to date",
                LAST_AUCTION_DATE_IS_AFTER_PRODUCT_VALID_TO, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void validCapacityGateTime(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
//        checkGateOpeningTime(auctionsSeriesDTO);
        if (auctionsSeriesDTO.getCapacityGateClosureTime().isBefore(auctionsSeriesDTO.getCapacityGateOpeningTime())) {
            throw new ObjectValidationException("Capacity gate closure time is before capacity gate opening time",
                CAPACITY_GATE_CLOSURE_TIME_IS_BEFORE_CAPACITY_GATE_OPENING_TIME, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void validDesiredCapacity(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        BigDecimal minDesiredCapacity = auctionsSeriesDTO.getMinDesiredCapacity();
        BigDecimal maxDesiredCapacity = auctionsSeriesDTO.getMaxDesiredCapacity();

        if (Objects.nonNull(minDesiredCapacity) && Objects.nonNull(maxDesiredCapacity) && minDesiredCapacity.compareTo(maxDesiredCapacity) > 0) {
            throw new ObjectValidationException("Capacity min desired is greater than capacity max desired",
                CAPACITY_MIN_DESIRED_IS_GREATER_THAN_CAPACITY_MAX_DESIRED, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }

        ProductDTO productDTO = productService.findById(auctionsSeriesDTO.getProduct().getId()).get();
        if (Objects.nonNull(minDesiredCapacity) && minDesiredCapacity.compareTo(productDTO.getMinBidSize()) < 0) {
            throw new ObjectValidationException("Capacity min desired is less than product min bid size",
                CAPACITY_MIN_DESIRED_IS_LESS_THAN_PRODUCT_MIN_BID_SIZE, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
        if (Objects.nonNull(maxDesiredCapacity) && maxDesiredCapacity.compareTo(productDTO.getMaxBidSize()) > 0) {
            throw new ObjectValidationException("Capacity min desired is greater than product max bid size",
                CAPACITY_MAX_DESIRED_IS_GREATER_THAN_PRODUCT_MAX_BID_SIZE, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void validCapacityAvailability(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        if (auctionsSeriesDTO.getCapacityAvailabilityTo().isBefore(auctionsSeriesDTO.getCapacityAvailabilityFrom())) {
            throw new ObjectValidationException("'Capacity availability to' is before 'capacity availability from'",
                CAPACITY_AVAILABILITY_TO_IS_BEFORE_CAPACITY_AVAILABILITY_FROM, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void validEnergyGateTime(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
//        checkGateOpeningTime(auctionsSeriesDTO);
        if (auctionsSeriesDTO.getEnergyGateClosureTime().isBefore(auctionsSeriesDTO.getEnergyGateOpeningTime())) {
            throw new ObjectValidationException("Energy gate closure time is before energy gate opening time",
                ENERGY_GATE_CLOSURE_TIME_IS_BEFORE_ENERGY_GATE_OPENING_TIME, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void validDesiredEnergy(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        BigDecimal minDesiredEnergy = auctionsSeriesDTO.getMinDesiredEnergy();
        BigDecimal maxDesiredEnergy = auctionsSeriesDTO.getMaxDesiredEnergy();

        if (Objects.nonNull(minDesiredEnergy) && Objects.nonNull(maxDesiredEnergy) && minDesiredEnergy.compareTo(maxDesiredEnergy) > 0) {
            throw new ObjectValidationException("Energy min desired is greater than energy max desired",
                ENERGY_MIN_DESIRED_IS_GREATER_THAN_ENERGY_MAX_DESIRED, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }

        ProductDTO productDTO = productService.findById(auctionsSeriesDTO.getProduct().getId()).get();
        if (Objects.nonNull(minDesiredEnergy) && minDesiredEnergy.compareTo(productDTO.getMinBidSize()) < 0) {
            throw new ObjectValidationException("Energy min desired is less than product min bid size",
                ENERGY_MIN_DESIRED_IS_LESS_THAN_PRODUCT_MIN_BID_SIZE, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
        if (Objects.nonNull(maxDesiredEnergy) && maxDesiredEnergy.compareTo(productDTO.getMaxBidSize()) > 0) {
            throw new ObjectValidationException("Energy max desired is greater than product max bid size",
                ENERGY_MAX_DESIRED_IS_GREATER_THAN_PRODUCT_MAX_BID_SIZE, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void validEnergyAvailability(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        if (auctionsSeriesDTO.getEnergyAvailabilityTo().isBefore(auctionsSeriesDTO.getEnergyAvailabilityFrom())) {
            throw new ObjectValidationException("'Energy availability to' is before 'energy availability from'",
                ENERGY_AVAILABILITY_TO_IS_BEFORE_ENERGY_AVAILABILITY_FROM, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void checkNotEditableFields(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        AuctionsSeriesDTO dbAuctionSeries = auctionsSeriesService.findById(auctionsSeriesDTO.getId())
            .orElseThrow(() -> new RuntimeException("AuctionSeries to modify not found"));
        if (!Objects.equals(dbAuctionSeries.getProduct().getId(), auctionsSeriesDTO.getProduct().getId())) {
            throw new ObjectValidationException("Cannot modify product in auction series",
                CANNOT_MODIFY_PRODUCT_IN_AUCTION_SERIES, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
        if (!Objects.equals(dbAuctionSeries.getType(), auctionsSeriesDTO.getType())) {
            throw new ObjectValidationException("Cannot modify auction type in auction series",
                CANNOT_MODIFY_AUCTION_TYPE_IN_AUCTION_SERIES, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }

        if (!dbAuctionSeries.getFirstAuctionDate().equals(auctionsSeriesDTO.getFirstAuctionDate()) && auctionDayAheadViewRepository.existsNotScheduledByAuctionSeriesId(auctionsSeriesDTO.getId())) {
            throw new ObjectValidationException("Cannot modify first auction date, because Day Ahead auctions have already started",
                CANNOT_MODIFY_FIRST_AUCTION_DATE_BECAUSE_DAY_AHEAD_AUCTION_HAVE_ALREADY_STARTED, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void checkModifyLastAuctionDate(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        var dbLastAuctionDate = auctionsSeriesService.findById(auctionsSeriesDTO.getId())
            .orElseThrow(() -> new RuntimeException("AuctionSeries to modify not found"))
            .getLastAuctionDate();
        Instant modifyLastAuctionDate = auctionsSeriesDTO.getLastAuctionDate();
        if (!dbLastAuctionDate.equals(modifyLastAuctionDate)
            && auctionDayAheadViewRepository.existsNotOpenAuctionBySeriesIdAndDayIsAfter(auctionsSeriesDTO.getId(), modifyLastAuctionDate)) {
            throw new ObjectValidationException("Cannot modify last auction date, because exists auctions after given date",
                CANNOT_MODIFY_LAST_AUCTION_DATE_BECAUSE_EXISTS_AUCTIONS_AFTER_GIVEN_DATE, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
        if(Instant.now().isAfter(dbLastAuctionDate)) {
            throw new ObjectValidationException("Cannot modify last auction date, because auction series is expired",
                CANNOT_MODIFY_LAST_AUCTION_DATE_BECAUSE_AUCTION_SERIES_IS_EXPIRED, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void checkModifyFirstAuctionDate(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        AuctionsSeriesDTO dbAuctionSeries = auctionsSeriesService.findById(auctionsSeriesDTO.getId())
            .orElseThrow(() -> new RuntimeException("AuctionSeries to modify not found"));
        boolean isValidFirstAuctionDate = LocalDate.ofInstant(InstantUtil.now().plus(1, ChronoUnit.DAYS), ZoneId.systemDefault())
            .isAfter(LocalDate.ofInstant(auctionsSeriesDTO.getFirstAuctionDate(), ZoneId.systemDefault()));
        if (!dbAuctionSeries.getFirstAuctionDate().equals(auctionsSeriesDTO.getFirstAuctionDate()) && isValidFirstAuctionDate) {
            throw new ObjectValidationException("The first auctions may start tomorrow at the earliest",
                FIRST_AUCTION_DATE_MAY_START_TOMORROW_AT_THE_EARLIEST, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void checkNotNullConstraint(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        boolean isValid = true;
        if (AuctionDayAheadType.ENERGY.equals(auctionsSeriesDTO.getType())) {
            isValid = checkEnergyNotNullConstraint(auctionsSeriesDTO);
        } else if (AuctionDayAheadType.CAPACITY.equals(auctionsSeriesDTO.getType())) {
            isValid = checkCapacityNotNullConstraint(auctionsSeriesDTO);
        }
        if (!isValid) {
            throw new ObjectValidationException("Required fields are not completed",
                REQUIRED_FIELDS_ARE_NOT_COMPLETED, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void checkNullConstraint(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        boolean isValid = true;
        if (AuctionDayAheadType.ENERGY.equals(auctionsSeriesDTO.getType())) {
            isValid = checkEnergyNullConstraint(auctionsSeriesDTO);
        } else if (AuctionDayAheadType.CAPACITY.equals(auctionsSeriesDTO.getType())) {
            isValid = checkCapacityNullConstraint(auctionsSeriesDTO);
        }
        if (!isValid) {
            throw new ObjectValidationException("Non required fields are completed",
                NON_REQUIRED_FIELDS_ARE_COMPLETED, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    private void checkGateOpeningTime(AuctionsSeriesDTO auctionsSeriesDTO) throws ObjectValidationException {
        boolean isValid = true;
        if (AuctionDayAheadType.ENERGY.equals(auctionsSeriesDTO.getType())) {
            isValid = isValidGateOpeningTime(auctionsSeriesDTO.getEnergyGateOpeningTime());
        } else if (AuctionDayAheadType.CAPACITY.equals(auctionsSeriesDTO.getType())) {
            isValid = isValidGateOpeningTime(auctionsSeriesDTO.getCapacityGateOpeningTime());
        }

        if (!isValid) {
            throw new ObjectValidationException("Gate opening time must be greater or equal than 01:15:00",
                GATE_OPENING_TIME_MUST_BE_LATER, ENTITY_NAME,
                getActivityEvent(auctionsSeriesDTO), auctionsSeriesDTO.getId());
        }
    }

    // Aukcje najwczesniej moga rozpoczac sie o godzinie 1:15
    private boolean isValidGateOpeningTime(Instant openingTime) {
        LocalTime earliestHour = LocalTime.of(1, 15, 0);
        LocalTime timeToCheck = LocalTime.ofInstant(openingTime, ZoneId.systemDefault());
        return timeToCheck.equals(earliestHour) || timeToCheck.isAfter(earliestHour);
    }

    private boolean checkCapacityNotNullConstraint(AuctionsSeriesDTO auctionsSeriesDTO) {
        boolean isValid = true;
        if (auctionsSeriesDTO.getCapacityGateOpeningTime() == null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getCapacityGateClosureTime() == null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getCapacityAvailabilityFrom() == null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getCapacityAvailabilityTo() == null) {
            isValid = false;
        }
        return isValid;
    }

    private boolean checkEnergyNotNullConstraint(AuctionsSeriesDTO auctionsSeriesDTO) {
        boolean isValid = true;
        if (auctionsSeriesDTO.getEnergyGateOpeningTime() == null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getEnergyGateClosureTime() == null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getEnergyAvailabilityFrom() == null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getEnergyAvailabilityTo() == null) {
            isValid = false;
        }
        return isValid;
    }

    private boolean checkCapacityNullConstraint(AuctionsSeriesDTO auctionsSeriesDTO) {
        boolean isValid = true;
        if (auctionsSeriesDTO.getEnergyGateOpeningTime() != null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getEnergyGateClosureTime() != null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getEnergyAvailabilityFrom() != null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getEnergyAvailabilityTo() != null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getMinDesiredEnergy() != null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getMaxDesiredEnergy() != null) {
            isValid = false;
        }
        return isValid;
    }

    private boolean checkEnergyNullConstraint(AuctionsSeriesDTO auctionsSeriesDTO) {
        boolean isValid = true;
        if (auctionsSeriesDTO.getCapacityGateOpeningTime() != null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getCapacityGateClosureTime() != null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getCapacityAvailabilityFrom() != null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getCapacityAvailabilityTo() != null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getMinDesiredCapacity() != null) {
            isValid = false;
        }
        if (auctionsSeriesDTO.getMaxDesiredCapacity() != null) {
            isValid = false;
        }
        return isValid;
    }

    private ActivityEvent getActivityEvent(AuctionsSeriesDTO auctionsSeriesDTO) {
        return auctionsSeriesDTO.getId() == null ? ActivityEvent.AUCTIONS_SERIES_CREATED_ERROR : ActivityEvent.AUCTIONS_SERIES_UPDATED_ERROR;
    }
}

