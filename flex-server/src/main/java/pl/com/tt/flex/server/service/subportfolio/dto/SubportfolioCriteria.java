package pl.com.tt.flex.server.service.subportfolio.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.web.rest.subportfolio.SubportfolioResource;

import java.io.Serializable;

/**
 * Criteria class for the {@link SubportfolioEntity} entity. This class is used
 * in {@link SubportfolioResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /subportfolio?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class SubportfolioCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private IntegerFilter numberOfDers;

    private BigDecimalFilter combinedPowerOfDers;

    private LongFilter couplingPointIdTypes;

    private StringFilter mrid;

    private LongFilter version;

    private LongFilter productId;

    private LongFilter unitId;

    private LongFilter fspaId;

    private StringFilter fspaRepresentativeCompanyName;

    private BooleanFilter active;

    private BooleanFilter certified;

    private StringFilter createdBy;

    private InstantFilter createdDate;

    private StringFilter lastModifiedBy;

    private InstantFilter lastModifiedDate;

    private InstantFilter validFrom;

    private InstantFilter validTo;

    public SubportfolioCriteria(SubportfolioCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.certified = other.certified == null ? null : other.certified.copy();
        this.numberOfDers = other.numberOfDers == null ? null : other.numberOfDers.copy();
        this.combinedPowerOfDers = other.combinedPowerOfDers == null ? null : other.combinedPowerOfDers.copy();
        this.couplingPointIdTypes = other.couplingPointIdTypes == null ? null : other.couplingPointIdTypes.copy();
        this.mrid = other.mrid == null ? null : other.mrid.copy();
        this.version = other.version == null ? null : other.version.copy();
        this.unitId = other.unitId == null ? null : other.unitId.copy();
        this.fspaId = other.fspaId == null ? null : other.fspaId.copy();
        this.fspaRepresentativeCompanyName = other.fspaRepresentativeCompanyName == null ? null : other.fspaRepresentativeCompanyName.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.lastModifiedBy = other.lastModifiedBy == null ? null : other.lastModifiedBy.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.validFrom = other.validFrom == null ? null : other.validFrom.copy();
        this.validTo = other.validTo == null ? null : other.validTo.copy();
    }

    @Override
    public SubportfolioCriteria copy() {
        return new SubportfolioCriteria(this);
    }
}
