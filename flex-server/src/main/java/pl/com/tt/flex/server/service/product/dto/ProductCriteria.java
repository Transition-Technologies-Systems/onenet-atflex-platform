package pl.com.tt.flex.server.service.product.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.web.rest.product.ProductResource;

import java.io.Serializable;

/**
 * Criteria class for the {@link ProductEntity} entity. This class is used
 * in {@link ProductResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /products?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class ProductCriteria implements Serializable, Criteria {

    /**
     * Class for filtering ProductBidSizeUnit
     */
    @NoArgsConstructor
    public static class ProductBidSizeUnitFilter extends Filter<ProductBidSizeUnit> {

        public ProductBidSizeUnitFilter(ProductBidSizeUnitFilter filter) {
            super(filter);
        }

        @Override
        public ProductBidSizeUnitFilter copy() {
            return new ProductBidSizeUnitFilter(this);
        }

    }

    /**
     * Class for filtering Direction
     */
    @NoArgsConstructor
    public static class DirectionFilter extends Filter<Direction> {

        public DirectionFilter(DirectionFilter filter) {super(filter);}

        @Override
        public DirectionFilter copy() {return new DirectionFilter(this);}

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter fullName;

    private StringFilter shortName;

    private BooleanFilter locational;

    private BigDecimalFilter minBidSize;

    private BigDecimalFilter maxBidSize;

    private ProductBidSizeUnitFilter bidSizeUnit;

    private IntegerFilter maxFullActivationTime;

    private IntegerFilter minRequiredDeliveryDuration;

    private BooleanFilter hasAsmReport;

    private BooleanFilter active;

    private InstantFilter validFrom;

    private InstantFilter validTo;

    private LongFilter version;

    private LongFilter psoUserId;

    private LongFilter ssoUserId;

    private StringFilter createdBy;

    private InstantFilter createdDate;

    private StringFilter lastModifiedBy;

    private InstantFilter lastModifiedDate;

    private BooleanFilter balancing;

    private BooleanFilter cmvc;

    private DirectionFilter direction;


    public ProductCriteria(ProductCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.fullName = other.fullName == null ? null : other.fullName.copy();
        this.shortName = other.shortName == null ? null : other.shortName.copy();
        this.locational = other.locational == null ? null : other.locational.copy();
        this.minBidSize = other.minBidSize == null ? null : other.minBidSize.copy();
        this.maxBidSize = other.maxBidSize == null ? null : other.maxBidSize.copy();
        this.bidSizeUnit = other.bidSizeUnit == null ? null : other.bidSizeUnit.copy();
        this.maxFullActivationTime = other.maxFullActivationTime == null ? null : other.maxFullActivationTime.copy();
        this.minRequiredDeliveryDuration = other.minRequiredDeliveryDuration == null ? null : other.minRequiredDeliveryDuration.copy();
        this.hasAsmReport = other.hasAsmReport == null ? null : other.hasAsmReport.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.validFrom = other.validFrom == null ? null : other.validFrom.copy();
        this.validTo = other.validTo == null ? null : other.validTo.copy();
        this.version = other.version == null ? null : other.version.copy();
        this.psoUserId = other.psoUserId == null ? null : other.psoUserId.copy();
        this.ssoUserId = other.ssoUserId == null ? null : other.ssoUserId.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.lastModifiedBy = other.lastModifiedBy == null ? null : other.lastModifiedBy.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.balancing = other.balancing == null ? null : other.balancing.copy();
        this.cmvc = other.cmvc == null ? null : other.cmvc.copy();
        this.direction = other.direction == null ? null : other.direction.copy();
    }

    @Override
    public ProductCriteria copy() {
        return new ProductCriteria(this);
    }
}
