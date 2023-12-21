package pl.com.tt.flex.server.service.dictionary.derType.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.domain.unit.enumeration.DerType;
import pl.com.tt.flex.server.web.rest.dictionary.derType.DerTypeResource;

import java.io.Serializable;
import java.util.Objects;

/**
 * Criteria class for the {@link DerTypeEntity} entity. This class is used
 * in {@link DerTypeResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /der-types?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
public class DerTypeCriteria implements Serializable, Criteria {
    /**
     * Class for filtering DerType
     */
    @NoArgsConstructor
    public static class DerTypeFilter extends Filter<DerType> {

        public DerTypeFilter(DerTypeCriteria.DerTypeFilter filter) {
            super(filter);
        }

        @Override
        public DerTypeCriteria.DerTypeFilter copy() {
            return new DerTypeCriteria.DerTypeFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter descriptionEn;

    private StringFilter descriptionPl;

    private InstantFilter lastModifiedDate;

    private InstantFilter createdDate;

    private DerTypeFilter type;

    public DerTypeCriteria(DerTypeCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.descriptionEn = other.descriptionEn == null ? null : other.descriptionEn.copy();
        this.descriptionPl = other.descriptionPl == null ? null : other.descriptionPl.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
    }

    @Override
    public DerTypeCriteria copy() {
        return new DerTypeCriteria(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DerTypeCriteria that = (DerTypeCriteria) o;
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
