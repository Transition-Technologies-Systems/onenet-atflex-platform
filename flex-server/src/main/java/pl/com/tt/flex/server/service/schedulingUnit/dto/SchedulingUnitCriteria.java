package pl.com.tt.flex.server.service.schedulingUnit.dto;

import java.io.Serializable;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.web.rest.schedulingUnit.SchedulingUnitResource;

/**
 * Criteria class for the {@link SchedulingUnitEntity} entity. This class is used
 * in {@link SchedulingUnitResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /scheduling-units?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class SchedulingUnitCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private BooleanFilter active;

    private IntegerFilter numberOfDers;

    private LongFilter schedulingUnitTypeId;

    private LongFilter unitId;

    private LongFilter bspId;

    private StringFilter bspRepresentativeCompanyName;

    private BooleanFilter readyForTests;

    private BooleanFilter certified;

    private StringFilter createdBy;

    private InstantFilter createdDate;

    private StringFilter lastModifiedBy;

    private InstantFilter lastModifiedDate;

    public SchedulingUnitCriteria(SchedulingUnitCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.numberOfDers = other.numberOfDers == null ? null : other.numberOfDers.copy();
        this.schedulingUnitTypeId = other.schedulingUnitTypeId == null ? null : other.schedulingUnitTypeId.copy();
        this.unitId = other.unitId == null ? null : other.unitId.copy();
        this.bspId = other.bspId == null ? null : other.bspId.copy();
        this.bspRepresentativeCompanyName = other.bspRepresentativeCompanyName == null ? null : other.bspRepresentativeCompanyName.copy();
        this.readyForTests = other.readyForTests == null ? null : other.readyForTests.copy();
        this.certified = other.certified == null ? null : other.certified.copy();
        this.bspId = other.bspId == null ? null : other.bspId.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.lastModifiedBy = other.lastModifiedBy == null ? null : other.lastModifiedBy.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
    }

    @Override
    public SchedulingUnitCriteria copy() {
        return new SchedulingUnitCriteria(this);
    }
}
