package pl.com.tt.flex.server.service.potential.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.web.rest.potential.FlexPotentialResource;

import java.io.Serializable;

/**
 * Criteria class for the {@link FlexPotentialEntity} entity. This class is used
 * in {@link FlexPotentialResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /flex-potentials?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@EqualsAndHashCode
public class FlexPotentialCriteria implements Serializable, Criteria {

    /**
     * Class for filtering FlexPotentialVolumeUnit
     */
    @NoArgsConstructor
    public static class FlexPotentialVolumeUnitFilter extends Filter<ProductBidSizeUnit> {

        public FlexPotentialVolumeUnitFilter(FlexPotentialVolumeUnitFilter filter) {
            super(filter);
        }

        @Override
        public FlexPotentialVolumeUnitFilter copy() {
            return new FlexPotentialVolumeUnitFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private LongFilter productId;

    private LongFilter fspId;

    private BigDecimalFilter volume;

    private FlexPotentialVolumeUnitFilter volumeUnit;

    private InstantFilter validFrom;

    private InstantFilter validTo;

    private BooleanFilter active;

    private BooleanFilter productPrequalification;

    private BooleanFilter staticGridPrequalification;

    private LongFilter version;

    private StringFilter createdBy;

    private StringFilter createdByRole;

    private InstantFilter createdDate;

    private StringFilter lastModifiedBy;

    private StringFilter lastModifiedByRole;

    private InstantFilter lastModifiedDate;

    private StringFilter productShortName;

    private StringFilter fspRepresentativeCompanyName;

    private IntegerFilter fullActivationTime;

    private IntegerFilter minDeliveryDuration;

    private BooleanFilter aggregated;

    private BooleanFilter isRegister;

    private BooleanFilter divisibility;

    private StringFilter unitName;

    public FlexPotentialCriteria() {
    }

    public FlexPotentialCriteria(FlexPotentialCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.productId = other.productId == null ? null : other.productId.copy();
        this.fspId = other.fspId == null ? null : other.fspId.copy();
        this.volume = other.volume == null ? null : other.volume.copy();
        this.volumeUnit = other.volumeUnit == null ? null : other.volumeUnit.copy();
        this.validFrom = other.validFrom == null ? null : other.validFrom.copy();
        this.validTo = other.validTo == null ? null : other.validTo.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.productPrequalification = other.productPrequalification == null ? null : other.productPrequalification.copy();
        this.staticGridPrequalification = other.staticGridPrequalification == null ? null : other.staticGridPrequalification.copy();
        this.version = other.version == null ? null : other.version.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.createdByRole = other.createdByRole == null ? null : other.createdByRole.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.lastModifiedBy = other.lastModifiedBy == null ? null : other.lastModifiedBy.copy();
        this.lastModifiedByRole = other.lastModifiedByRole == null ? null : other.lastModifiedByRole.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.productShortName = other.productShortName == null ? null : other.productShortName.copy();
        this.fspRepresentativeCompanyName = other.fspRepresentativeCompanyName == null ? null : other.fspRepresentativeCompanyName.copy();
        this.fullActivationTime = other.fullActivationTime == null ? null : other.fullActivationTime.copy();
        this.minDeliveryDuration = other.minDeliveryDuration == null ? null : other.minDeliveryDuration.copy();
        this.aggregated = other.aggregated == null ? null : other.aggregated.copy();
        this.isRegister = other.isRegister == null ? null : other.isRegister.copy();
        this.divisibility = other.divisibility == null ? null : other.divisibility.copy();
        this.unitName = other.unitName == null ? null : other.unitName.copy();
    }

    @Override
    public FlexPotentialCriteria copy() {
        return new FlexPotentialCriteria(this);
    }
}
