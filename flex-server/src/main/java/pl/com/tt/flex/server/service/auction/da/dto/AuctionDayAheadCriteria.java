package pl.com.tt.flex.server.service.auction.da.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.web.rest.auction.da.AuctionDayAheadResource;

import java.io.Serializable;

/**
 * Criteria class for the {@link AuctionDayAheadEntity} entity. This class is used
 * in {@link AuctionDayAheadResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /auctions?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AuctionDayAheadCriteria implements Serializable, Criteria {
    /**
     * Class for filtering AuctionType
     */
    public static class AuctionTypeFilter extends Filter<AuctionDayAheadType> {

        public AuctionTypeFilter() {
        }

        public AuctionTypeFilter(AuctionTypeFilter filter) {
            super(filter);
        }

        @Override
        public AuctionTypeFilter copy() {
            return new AuctionTypeFilter(this);
        }

    }

    /**
     * Class for filtering AuctionStatus
     */
    public static class AuctionStatusFilter extends Filter<AuctionStatus> {

        public AuctionStatusFilter() {
        }

        public AuctionStatusFilter(AuctionStatusFilter filter) {
            super(filter);
        }

        @Override
        public AuctionStatusFilter copy() {
            return new AuctionStatusFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private InstantFilter creationDate;

    private InstantFilter day;

    private InstantFilter deliveryDate;

    private AuctionTypeFilter auctionType;

    private AuctionStatusFilter status;

    private StringFilter productName;

    private LongFilter productId;

    private InstantFilter energyGateOpeningTime;

    private InstantFilter energyGateClosureTime;

    private InstantFilter capacityGateOpeningTime;

    private InstantFilter capacityGateClosureTime;

    private InstantFilter gateDate;

    private BigDecimalFilter minDesiredCapacity;

    private BigDecimalFilter maxDesiredCapacity;

    private BigDecimalFilter minDesiredEnergy;

    private BigDecimalFilter maxDesiredEnergy;

    private InstantFilter capacityAvailabilityFrom;

    private InstantFilter capacityAvailabilityTo;

    private InstantFilter energyAvailabilityFrom;

    private InstantFilter energyAvailabilityTo;


    public AuctionDayAheadCriteria(AuctionDayAheadCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.creationDate = other.creationDate == null ? null : other.creationDate.copy();
        this.auctionType = other.auctionType == null ? null : other.auctionType.copy();
        this.day = other.day == null ? null : other.day.copy();
        this.deliveryDate = other.deliveryDate == null ? null : other.deliveryDate.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.productName = other.productName == null ? null : other.productName.copy();
        this.productId = other.productId == null ? null : other.productId.copy();
        this.energyGateOpeningTime = other.energyGateOpeningTime == null ? null : other.energyGateOpeningTime.copy();
        this.energyGateClosureTime = other.energyGateClosureTime == null ? null : other.energyGateClosureTime.copy();
        this.capacityGateOpeningTime = other.capacityGateOpeningTime == null ? null : other.capacityGateOpeningTime.copy();
        this.capacityGateClosureTime = other.capacityGateClosureTime == null ? null : other.capacityGateClosureTime.copy();
        this.gateDate = other.gateDate == null ? null : other.gateDate.copy();
        this.minDesiredCapacity = other.minDesiredCapacity == null ? null : other.minDesiredCapacity.copy();
        this.maxDesiredCapacity = other.maxDesiredCapacity == null ? null : other.maxDesiredCapacity.copy();
        this.minDesiredEnergy = other.minDesiredEnergy == null ? null : other.minDesiredEnergy.copy();
        this.maxDesiredEnergy = other.maxDesiredEnergy == null ? null : other.maxDesiredEnergy.copy();
        this.capacityAvailabilityFrom = other.capacityAvailabilityFrom == null ? null : other.capacityAvailabilityFrom.copy();
        this.capacityAvailabilityTo = other.capacityAvailabilityTo == null ? null : other.capacityAvailabilityTo.copy();
        this.energyAvailabilityFrom = other.energyAvailabilityFrom == null ? null : other.energyAvailabilityFrom.copy();
        this.energyAvailabilityTo = other.energyAvailabilityTo == null ? null : other.energyAvailabilityTo.copy();
    }

    @Override
    public AuctionDayAheadCriteria copy() {
        return new AuctionDayAheadCriteria(this);
    }
}
