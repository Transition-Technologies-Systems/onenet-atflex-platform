package pl.com.tt.flex.server.service.kpi.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.domain.kpi.KpiEntity;
import pl.com.tt.flex.server.web.rest.kpi.KpiResource;

import java.io.Serializable;

/**
 * Criteria class for the {@link KpiEntity} entity. This class is used
 * in {@link KpiResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code api/kpis?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class KpiCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    @NoArgsConstructor
    public static class KpiTypeFilter extends Filter<KpiType> {

        public KpiTypeFilter(KpiCriteria.KpiTypeFilter filter) {
            super(filter);
        }

        @Override
        public KpiCriteria.KpiTypeFilter copy() {
            return new KpiCriteria.KpiTypeFilter(this);
        }
    }

    private LongFilter id;

    private KpiTypeFilter type;

    private InstantFilter dateFrom;

    private InstantFilter dateTo;

    private StringFilter createdBy;

    private InstantFilter createdDate;

    public KpiCriteria(KpiCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.dateFrom = other.dateFrom == null ? null : other.dateFrom.copy();
        this.dateTo = other.dateTo == null ? null : other.dateTo.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
    }

    @Override
    public KpiCriteria copy() {
        return new KpiCriteria(this);
    }
}
