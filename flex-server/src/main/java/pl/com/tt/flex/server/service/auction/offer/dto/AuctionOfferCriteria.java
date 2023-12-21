package pl.com.tt.flex.server.service.auction.offer.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewType;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.service.auction.da.dto.AuctionDayAheadCriteria.AuctionStatusFilter;
import pl.com.tt.flex.server.web.rest.auction.offer.AuctionOfferResource;

import java.io.Serializable;

/**
 * Criteria class for the {@link AuctionCmvcOfferEntity} entity. This class is used
 * in {@link AuctionOfferResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /offers?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AuctionOfferCriteria implements Serializable, Criteria {

    /**
     * Class for filtering AuctionOfferStatus
     */
    public static class AuctionOfferStatusFilter extends Filter<AuctionOfferStatus> {

        public AuctionOfferStatusFilter() {
        }

        public AuctionOfferStatusFilter(AuctionOfferCriteria.AuctionOfferStatusFilter filter) {
            super(filter);
        }

        @Override
        public AuctionOfferStatusFilter copy() {
            return new AuctionOfferCriteria.AuctionOfferStatusFilter(this);
        }

    }

    /**
     * Class for filtering AuctionCmvcType
     */
    public static class AuctionOfferTypeFilter extends Filter<AuctionOfferType> {

        public AuctionOfferTypeFilter() {
        }

        public AuctionOfferTypeFilter(AuctionOfferTypeFilter filter) {
            super(filter);
        }

        @Override
        public AuctionOfferTypeFilter copy() {
            return new AuctionOfferTypeFilter(this);
        }

    }

    public static class AuctionOfferViewTypeFilter extends Filter<AuctionOfferViewType> {
        public AuctionOfferViewTypeFilter() {
        }

        public AuctionOfferViewTypeFilter(AuctionOfferViewTypeFilter filter) {
            super(filter);
        }

        @Override
        public AuctionOfferViewTypeFilter copy() {
            return new AuctionOfferViewTypeFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter auctionId;

    private StringFilter auctionName;

    private StringFilter productId;

    private StringFilter productName;

    private StringFilter companyName;

    private AuctionOfferViewTypeFilter offerCategory;

    private LongFilter auctionDayAhead;

    private LongFilter auctionCmvc;

    private StringFilter flexPotentialCompanyName;

    private StringFilter schedulingUnitCompanyName;

    private AuctionOfferStatusFilter status;

    private AuctionStatusFilter auctionStatus;

    private AuctionOfferTypeFilter type;

    private BigDecimalFilter price;

    private BigDecimalFilter volume;

    private BigDecimalFilter acceptedVolume;

    private BooleanFilter volumeDivisibility;

    private InstantFilter deliveryPeriod;

    private InstantFilter acceptedDeliveryPeriod;

    private BooleanFilter deliveryPeriodDivisibility;

    private LongFilter fspId;

    private LongFilter bspId;

    public AuctionOfferCriteria(AuctionOfferCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.auctionCmvc = other.auctionCmvc == null ? null : other.auctionCmvc.copy();
        this.auctionDayAhead = other.auctionDayAhead == null ? null : other.auctionDayAhead.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.auctionStatus = other.auctionStatus == null ? null : other.auctionStatus.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.flexPotentialCompanyName = other.flexPotentialCompanyName == null ? null : other.flexPotentialCompanyName.copy();
        this.schedulingUnitCompanyName = other.schedulingUnitCompanyName == null ? null : other.schedulingUnitCompanyName.copy();
        this.price = other.price == null ? null : other.price.copy();
        this.volume = other.volume == null ? null : other.volume.copy();
        this.acceptedVolume = other.volume == null ? null : other.volume.copy();
        this.volumeDivisibility = other.volumeDivisibility == null ? null : other.volumeDivisibility.copy();
        this.deliveryPeriod = other.deliveryPeriod == null ? null : other.deliveryPeriod.copy();
        this.acceptedDeliveryPeriod = other.acceptedDeliveryPeriod == null ? null : other.acceptedDeliveryPeriod.copy();
        this.deliveryPeriodDivisibility = other.deliveryPeriodDivisibility == null ? null : other.deliveryPeriodDivisibility.copy();
        this.auctionId = other.auctionId == null ? null : other.auctionId.copy();
        this.auctionName = other.auctionName == null ? null : other.auctionName.copy();
        this.productId = other.productId == null ? null : other.productId.copy();
        this.productName = other.productName == null ? null : other.productName.copy();
        this.companyName = other.companyName == null ? null : other.companyName.copy();
        this.offerCategory = other.offerCategory == null ? null : other.offerCategory.copy();
        this.fspId = other.fspId == null ? null : other.fspId.copy();
        this.bspId = other.bspId == null ? null : other.bspId.copy();

    }

    @Override
    public AuctionOfferCriteria copy() {
        return new AuctionOfferCriteria(this);
    }
}
