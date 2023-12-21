package pl.com.tt.flex.server.service.auction.offer.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionCategoryAndType;
import pl.com.tt.flex.server.service.auction.cmvc.dto.AuctionCmvcViewCriteria;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AuctionOfferViewCriteria implements Serializable, Criteria {
    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter auctionId;

    private StringFilter auctionName;

    private StringFilter productId;

    private StringFilter productName;

    private AuctionOfferCriteria.AuctionOfferStatusFilter status;

    private AuctionCmvcViewCriteria.AuctionStatusFilter auctionStatus;

    private BigDecimalFilter price;

    private StringFilter volume;

    private BooleanFilter volumeDivisibility;

    private InstantFilter deliveryPeriod;

    private BooleanFilter deliveryPeriodDivisibility;

    private StringFilter acceptedVolume;

    private InstantFilter acceptedDeliveryPeriod;

    private AuctionOfferCriteria.AuctionOfferViewTypeFilter offerCategory;

    private AuctionCategoryAndTypeFilter auctionCategoryAndType;

    private LongFilter fspId;

    private LongFilter bspId;

    private AuctionOfferCriteria.AuctionOfferTypeFilter type;

    private StringFilter companyName;

    private StringFilter schedulingUnitOrPotential;

    public static class AuctionCategoryAndTypeFilter extends Filter<AuctionCategoryAndType> {
        public AuctionCategoryAndTypeFilter() {
        }

        public AuctionCategoryAndTypeFilter(AuctionCategoryAndTypeFilter filter) {
            super(filter);
        }

        @Override
        public AuctionCategoryAndTypeFilter copy() {
            return new AuctionCategoryAndTypeFilter(this);
        }
    }

    public AuctionOfferViewCriteria(AuctionOfferViewCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.auctionId = other.auctionId == null ? null : other.auctionId.copy();
        this.auctionName = other.auctionName == null ? null : other.auctionName.copy();
        this.productId = other.productId == null ? null : other.productId.copy();
        this.productName = other.productName == null ? null : other.productName.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.auctionStatus = other.auctionStatus == null ? null : other.auctionStatus.copy();
        this.price = other.price == null ? null : other.price.copy();
        this.volume = other.volume == null ? null : other.volume.copy();
        this.volumeDivisibility = other.volumeDivisibility == null ? null : other.volumeDivisibility.copy();
        this.deliveryPeriod = other.deliveryPeriod == null ? null : other.deliveryPeriod.copy();
        this.deliveryPeriodDivisibility = other.deliveryPeriodDivisibility == null ? null : other.deliveryPeriodDivisibility.copy();
        this.acceptedVolume = other.acceptedVolume == null ? null : other.acceptedVolume.copy();
        this.acceptedDeliveryPeriod = other.acceptedDeliveryPeriod == null ? null : other.acceptedDeliveryPeriod.copy();
        this.offerCategory = other.offerCategory == null ? null : other.offerCategory.copy();
        this.auctionCategoryAndType = other.auctionCategoryAndType == null ? null : other.auctionCategoryAndType.copy();
        this.fspId = other.fspId == null ? null : other.fspId.copy();
        this.bspId = other.bspId == null ? null : other.bspId.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.companyName = other.companyName == null ? null : other.companyName.copy();
        this.schedulingUnitOrPotential = other.schedulingUnitOrPotential == null ? null : other.schedulingUnitOrPotential.copy();
    }

    @Override
    public AuctionOfferViewCriteria copy() {
        return new AuctionOfferViewCriteria(this);
    }
}
