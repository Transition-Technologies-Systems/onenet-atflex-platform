package pl.com.tt.flex.server.service.schedulingUnit.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitProposalEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalStatus;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalType;
import pl.com.tt.flex.server.web.rest.schedulingUnit.SchedulingUnitProposalResource;

import java.io.Serializable;

/**
 * Criteria class for the {@link SchedulingUnitProposalEntity} entity. This class is used
 * in {@link SchedulingUnitProposalResource} to receive all the possible filtering options from
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
public class SchedulingUnitProposalCriteria implements Serializable, Criteria {

    /**
     * Class for filtering SchedulingUnitProposalStatus
     */
    @NoArgsConstructor
    public static class SchedulingUnitProposalStatusFilter extends Filter<SchedulingUnitProposalStatus> {

        public SchedulingUnitProposalStatusFilter(SchedulingUnitProposalStatusFilter filter) {
            super(filter);
        }

        @Override
        public SchedulingUnitProposalStatusFilter copy() {
            return new SchedulingUnitProposalStatusFilter(this);
        }

    }

    /**
     * Class for filtering SchedulingUnitProposalType
     */
    @NoArgsConstructor
    public static class SchedulingUnitProposalTypeFilter extends Filter<SchedulingUnitProposalType> {

        public SchedulingUnitProposalTypeFilter(SchedulingUnitProposalTypeFilter filter) {
            super(filter);
        }

        @Override
        public SchedulingUnitProposalTypeFilter copy() {
            return new SchedulingUnitProposalTypeFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;
    private SchedulingUnitProposalStatusFilter status;
    private LongFilter bspId;
    private StringFilter bspCompanyName;
    private LongFilter schedulingUnitId;
    private LongFilter unitId;
    private StringFilter unitName;
    private LongFilter fspId;
    private StringFilter fspCompanyName;
    private SchedulingUnitProposalTypeFilter proposalType;
    private StringFilter createdBy;
    private InstantFilter createdDate;
    private StringFilter lastModifiedBy;
    private InstantFilter lastModifiedDate;
    private InstantFilter sentDate;

    public SchedulingUnitProposalCriteria(SchedulingUnitProposalCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.bspId = other.bspId == null ? null : other.bspId.copy();
        this.schedulingUnitId = other.schedulingUnitId == null ? null : other.schedulingUnitId.copy();
        this.unitId = other.unitId == null ? null : other.unitId.copy();
        this.unitId = other.unitId == null ? null : other.unitId.copy();
        this.unitName = other.unitName == null ? null : other.unitName.copy();
        this.fspId = other.fspId == null ? null : other.fspId.copy();
        this.fspCompanyName = other.fspCompanyName == null ? null : other.fspCompanyName.copy();
        this.bspId = other.bspId == null ? null : other.bspId.copy();
        this.bspCompanyName = other.bspCompanyName == null ? null : other.bspCompanyName.copy();
        this.proposalType = other.proposalType == null ? null : other.proposalType.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.lastModifiedBy = other.lastModifiedBy == null ? null : other.lastModifiedBy.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.sentDate = other.sentDate == null ? null : other.sentDate.copy();
    }

    @Override
    public SchedulingUnitProposalCriteria copy() {
        return new SchedulingUnitProposalCriteria(this);
    }
}
