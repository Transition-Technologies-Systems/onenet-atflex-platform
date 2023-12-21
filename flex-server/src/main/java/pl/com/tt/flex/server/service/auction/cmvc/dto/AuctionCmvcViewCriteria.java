package pl.com.tt.flex.server.service.auction.cmvc.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionType;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.web.rest.auction.cmvc.AuctionCmvcResource;

import java.io.Serializable;

/**
 * Criteria class for the {@link AuctionCmvcEntity} entity. This class is used
 * in {@link AuctionCmvcResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /auction-cmvcs?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AuctionCmvcViewCriteria implements Serializable, Criteria {
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
    /**
     * Class for filtering AuctionCmvcType
     */
    public static class AuctionCmvcTypeFilter extends Filter<AuctionType> {

        public AuctionCmvcTypeFilter() {
        }

        public AuctionCmvcTypeFilter(AuctionCmvcTypeFilter filter) {
            super(filter);
        }

        @Override
        public AuctionCmvcTypeFilter copy() {
            return new AuctionCmvcTypeFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter version;

    private StringFilter name;

    private StringFilter localization;

    private InstantFilter deliveryDateFrom;

    private InstantFilter deliveryDateTo;

    private InstantFilter gateOpeningTime;

    private InstantFilter gateClosureTime;

    private BigDecimalFilter minDesiredPower;

    private BigDecimalFilter maxDesiredPower;

    private AuctionStatusFilter status;

    private AuctionCmvcTypeFilter auctionCmvcType;

    private StringFilter productName;

    private InstantFilter createdDate;

    private StringFilter createdBy;

    private InstantFilter lastModifiedDate;

    private StringFilter lastModifiedBy;

    private LongFilter statusCode;

    public AuctionCmvcViewCriteria(AuctionCmvcViewCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.version = other.version == null ? null : other.version.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.localization = other.localization == null ? null : other.localization.copy();
        this.deliveryDateFrom = other.deliveryDateFrom == null ? null : other.deliveryDateFrom.copy();
        this.deliveryDateTo = other.deliveryDateTo == null ? null : other.deliveryDateTo.copy();
        this.gateOpeningTime = other.gateOpeningTime == null ? null : other.gateOpeningTime.copy();
        this.gateClosureTime = other.gateClosureTime == null ? null : other.gateClosureTime.copy();
        this.minDesiredPower = other.minDesiredPower == null ? null : other.minDesiredPower.copy();
        this.maxDesiredPower = other.maxDesiredPower == null ? null : other.maxDesiredPower.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.auctionCmvcType = other.auctionCmvcType == null ? null : other.auctionCmvcType.copy();
        this.productName = other.productName == null ? null : other.productName.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.lastModifiedBy = other.lastModifiedBy == null ? null : other.lastModifiedBy.copy();
        this.statusCode = other.statusCode == null ? null : other.statusCode.copy();
    }

    @Override
    public AuctionCmvcViewCriteria copy() {
        return new AuctionCmvcViewCriteria(this);
    }

}
