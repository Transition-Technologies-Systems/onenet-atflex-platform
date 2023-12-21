package pl.com.tt.flex.server.service.dictionary.localizationType.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.localization.LocalizationType;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.web.rest.dictionary.localizationType.LocalizationTypeResourceAdmin;

import java.io.Serializable;

/**
 * Criteria class for the {@link LocalizationTypeEntity} entity. This class is used
 * in {@link LocalizationTypeResourceAdmin} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /localization-types?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@EqualsAndHashCode
public class LocalizationTypeCriteria implements Serializable, Criteria {

    /**
     * Class for filtering LocalizationType
     */
    @NoArgsConstructor
    public static class LocalizationTypeFilter extends Filter<LocalizationType> {

        public LocalizationTypeFilter(LocalizationTypeCriteria.LocalizationTypeFilter filter) {
            super(filter);
        }

        @Override
        public LocalizationTypeCriteria.LocalizationTypeFilter copy() {
            return new LocalizationTypeCriteria.LocalizationTypeFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private LocalizationTypeFilter type;

    private InstantFilter lastModifiedDate;

    private InstantFilter createdDate;

    public LocalizationTypeCriteria() {
    }

    public LocalizationTypeCriteria(LocalizationTypeCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.name = other.name == null ? null : other.name.copy();
        this.type = other.type == null ? null : other.type.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
    }

    @Override
    public LocalizationTypeCriteria copy() {
        return new LocalizationTypeCriteria(this);
    }
}
