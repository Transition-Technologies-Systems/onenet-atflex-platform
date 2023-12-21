package pl.com.tt.flex.server.service.unit.selfSchedule.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.common.criteria.FilterCopyableCriteria;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.web.rest.unit.selfSchedule.UnitSelfScheduleResource;

import java.io.Serializable;

/**
 * Criteria class for the {@link UnitSelfScheduleEntity} entity. This class is used
 * in {@link UnitSelfScheduleResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /units?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UnitSelfScheduleCriteria implements Serializable, Criteria, FilterCopyableCriteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter selfScheduleDate;

    private LongFilter fspId;

    private LongFilter unitId;

    private StringFilter createdBy;

    private InstantFilter createdDate;

    private StringFilter lastModifiedBy;

    private InstantFilter lastModifiedDate;


    public UnitSelfScheduleCriteria(UnitSelfScheduleCriteria other) {
        this.id = copyFilter(other.id);
        this.fspId = copyFilter(other.fspId);
        this.createdBy = copyFilter(other.createdBy);
        this.createdDate = copyFilter(other.createdDate);
        this.lastModifiedBy = copyFilter(other.lastModifiedBy);
        this.lastModifiedDate = copyFilter(other.lastModifiedDate);
    }

    @Override
    public UnitSelfScheduleCriteria copy() {
        return new UnitSelfScheduleCriteria(this);
    }
}
