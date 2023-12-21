package pl.com.tt.flex.server.service.auction.da.series.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.web.rest.auction.da.AuctionsSeriesResource;

import java.io.Serializable;

/**
 * Criteria class for the {@link AuctionsSeriesEntity} entity. This class is used
 * in {@link AuctionsSeriesResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /auctions-series?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AuctionsSeriesCriteria implements Serializable, Criteria {
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

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private StringFilter productName;

    private LongFilter productId;

    private AuctionTypeFilter type;

    private InstantFilter energyGateOpeningTime;

    private InstantFilter energyGateClosureTime;

    private InstantFilter capacityGateOpeningTime;

    private InstantFilter capacityGateClosureTime;

    private BigDecimalFilter minDesiredCapacity;

    private BigDecimalFilter maxDesiredCapacity;

    private BigDecimalFilter minDesiredEnergy;

    private BigDecimalFilter maxDesiredEnergy;

    private InstantFilter capacityAvailabilityFrom;

    private InstantFilter capacityAvailabilityTo;

    private InstantFilter energyAvailabilityFrom;

    private InstantFilter energyAvailabilityTo;

    private InstantFilter firstAuctionDate;

    private InstantFilter lastAuctionDate;

    private InstantFilter createdDate;

    private StringFilter createdBy;

    private InstantFilter lastModifiedDate;

    private StringFilter lastModifiedBy;


    public AuctionsSeriesCriteria(AuctionsSeriesCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.productName = other.productName == null ? null : other.productName.copy();
        this.productId = other.productId == null ? null : other.productId.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.energyGateOpeningTime = other.energyGateOpeningTime == null ? null : other.energyGateOpeningTime.copy();
        this.energyGateClosureTime = other.energyGateClosureTime == null ? null : other.energyGateClosureTime.copy();
        this.capacityGateOpeningTime = other.capacityGateOpeningTime == null ? null : other.capacityGateOpeningTime.copy();
        this.capacityGateClosureTime = other.capacityGateClosureTime == null ? null : other.capacityGateClosureTime.copy();
        this.minDesiredCapacity = other.minDesiredCapacity == null ? null : other.minDesiredCapacity.copy();
        this.maxDesiredCapacity = other.maxDesiredCapacity == null ? null : other.maxDesiredCapacity.copy();
        this.minDesiredEnergy = other.minDesiredEnergy == null ? null : other.minDesiredEnergy.copy();
        this.maxDesiredEnergy = other.maxDesiredEnergy == null ? null : other.maxDesiredEnergy.copy();
        this.capacityAvailabilityFrom = other.capacityAvailabilityFrom == null ? null : other.capacityAvailabilityFrom.copy();
        this.capacityAvailabilityTo = other.capacityAvailabilityTo == null ? null : other.capacityAvailabilityTo.copy();
        this.energyAvailabilityFrom = other.energyAvailabilityFrom == null ? null : other.energyAvailabilityFrom.copy();
        this.energyAvailabilityTo = other.energyAvailabilityTo == null ? null : other.energyAvailabilityTo.copy();
        this.firstAuctionDate = other.firstAuctionDate == null ? null : other.firstAuctionDate.copy();
        this.lastAuctionDate = other.lastAuctionDate == null ? null : other.lastAuctionDate.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.lastModifiedBy = other.lastModifiedBy == null ? null : other.lastModifiedBy.copy();
    }

    @Override
    public AuctionsSeriesCriteria copy() {
        return new AuctionsSeriesCriteria(this);
    }
}
