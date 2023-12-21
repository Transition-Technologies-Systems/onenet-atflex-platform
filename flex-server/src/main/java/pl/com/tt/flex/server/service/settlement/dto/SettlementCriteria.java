package pl.com.tt.flex.server.service.settlement.dto;

import java.io.Serializable;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferCriteria.AuctionOfferViewTypeFilter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class SettlementCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter derName;

    private LongFilter offerId;

    private StringFilter auctionName;

    private StringFilter companyName;

    private InstantFilter acceptedDeliveryPeriodFrom;

    private InstantFilter acceptedDeliveryPeriodTo;

    private AuctionOfferViewTypeFilter offerCategory;

    // filtry automatycznie uzupełniane na platformie user
    private LongFilter fspId;

    private StringFilter offerCreatedBy;

    private StringFilter bspCompanyName;

    public SettlementCriteria(SettlementCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.derName = other.derName == null ? null : other.derName.copy();
        this.offerId = other.offerId == null ? null : other.offerId.copy();
        this.auctionName = other.auctionName == null ? null : other.auctionName.copy();
        this.companyName = other.companyName == null ? null : other.companyName.copy();
        this.bspCompanyName = other.bspCompanyName == null ? null : other.bspCompanyName.copy();
        this.acceptedDeliveryPeriodFrom = other.acceptedDeliveryPeriodFrom == null ? null : other.acceptedDeliveryPeriodFrom.copy();
        this.acceptedDeliveryPeriodTo = other.acceptedDeliveryPeriodTo == null ? null : other.acceptedDeliveryPeriodTo.copy();
        this.offerCategory = other.offerCategory == null ? null : other.offerCategory.copy();
        this.fspId = other.fspId == null ? null : other.fspId.copy();
        this.offerCreatedBy = other.offerCreatedBy == null ? null : other.offerCreatedBy.copy();
    }

    @Override
    public Criteria copy() {
        return new SettlementCriteria(this);
    }

}
