package pl.com.tt.flex.server.service.unit.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.common.criteria.FilterCopyableCriteria;
import pl.com.tt.flex.server.domain.unit.UnitDirectionOfDeviation;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.web.rest.unit.UnitResource;

import java.io.Serializable;

/**
 * Criteria class for the {@link UnitEntity} entity. This class is used
 * in {@link UnitResource} to receive all the possible filtering options from
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
public class UnitCriteria implements Serializable, Criteria, FilterCopyableCriteria {

    @NoArgsConstructor
    public static class DirectionOfDeviationFilter extends Filter<UnitDirectionOfDeviation> {

        public DirectionOfDeviationFilter(DirectionOfDeviationFilter filter) {
            super(filter);
        }

        @Override
        public DirectionOfDeviationFilter copy() {
            return new DirectionOfDeviationFilter(this);
        }
    }


    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter name;

    private StringFilter code;

    private StringFilter mridTso;

    private StringFilter mridDso;

    private BooleanFilter aggregated;

    private InstantFilter validFrom;

    private InstantFilter validTo;

    private BooleanFilter active;

    private BooleanFilter certified;

    private LongFilter version;

    private LongFilter fspId;

    private BooleanFilter fspActive;

    private StringFilter createdBy;

    private InstantFilter createdDate;

    private StringFilter lastModifiedBy;

    private InstantFilter lastModifiedDate;

    private StringFilter fspRepresentativeCompanyName;

    private BigDecimalFilter sourcePower;

    private BigDecimalFilter connectionPower;

    private StringFilter powerStation;

    private DirectionOfDeviationFilter directionOfDeviation;

    private LongFilter derTypeId;

    private StringFilter derTypeDescriptionEn;

    private StringFilter derTypeDescriptionPl;

    private LongFilter subportfolioId;

    private StringFilter subportfolioName;

    private BigDecimalFilter pmin;

    private BigDecimalFilter qmin;

    private BigDecimalFilter qmax;

    public UnitCriteria(UnitCriteria other) {
        this.id = copyFilter(other.id);
        this.name = copyFilter(other.name);
        this.code = copyFilter(other.code);
        this.mridTso = copyFilter(other.mridTso);
        this.mridDso = copyFilter(other.mridDso);
        this.aggregated = copyFilter(other.aggregated);
        this.fspId = copyFilter(other.fspId);
        this.fspActive = copyFilter(other.fspActive);
        this.validFrom = copyFilter(other.validFrom);
        this.validTo = copyFilter(other.validTo);
        this.active = copyFilter(other.active);
        this.certified = copyFilter(other.certified);
        this.version = copyFilter(other.version);
        this.createdBy = copyFilter(other.createdBy);
        this.createdDate = copyFilter(other.createdDate);
        this.lastModifiedBy = copyFilter(other.lastModifiedBy);
        this.lastModifiedDate = copyFilter(other.lastModifiedDate);
        this.fspRepresentativeCompanyName = copyFilter(other.fspRepresentativeCompanyName);
        this.derTypeId = copyFilter(other.derTypeId);
        this.derTypeDescriptionEn = copyFilter(other.derTypeDescriptionEn);
        this.derTypeDescriptionPl = copyFilter(other.derTypeDescriptionPl);
        sourcePower = copyFilter(other.sourcePower);
        connectionPower = copyFilter(other.connectionPower);
        directionOfDeviation = copyFilter(other.directionOfDeviation);
        powerStation = copyFilter(other.powerStation);
        this.subportfolioId = copyFilter(other.subportfolioId);
        this.subportfolioName = copyFilter(other.subportfolioName);
        this.pmin = copyFilter(other.pmin);
        this.qmin = copyFilter(other.qmin);
        this.qmax = copyFilter(other.qmax);
    }

    @Override
    public UnitCriteria copy() {
        return new UnitCriteria(this);
    }
}
