package pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.web.rest.dictionary.schedulingUnitType.SchedulingUnitTypeResource;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the {@link SchedulingUnitTypeEntity} entity. This class is used
 * in {@link SchedulingUnitTypeResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /der-types?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@EqualsAndHashCode
public class SchedulingUnitTypeCriteria implements Serializable, Criteria {

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter descriptionEn;

    private StringFilter descriptionPl;

    private LongFilter productId;

    private InstantFilter lastModifiedDate;

    private InstantFilter createdDate;

    public SchedulingUnitTypeCriteria() {
    }

    public SchedulingUnitTypeCriteria(SchedulingUnitTypeCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.descriptionEn = other.descriptionEn == null ? null : other.descriptionEn.copy();
        this.descriptionPl = other.descriptionPl == null ? null : other.descriptionPl.copy();
        this.productId = other.productId == null ? null : other.productId.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
    }

    @Override
    public SchedulingUnitTypeCriteria copy() {
        return new SchedulingUnitTypeCriteria(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SchedulingUnitTypeCriteria that = (SchedulingUnitTypeCriteria) o;
        return
            Objects.equals(id, that.id) &&
                Objects.equals(descriptionEn, that.descriptionEn) &&
                Objects.equals(descriptionPl, that.descriptionPl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
            id,
            descriptionEn,
            descriptionPl
        );
    }
}
