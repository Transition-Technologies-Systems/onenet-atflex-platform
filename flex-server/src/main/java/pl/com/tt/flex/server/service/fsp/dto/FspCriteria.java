package pl.com.tt.flex.server.service.fsp.dto;

import java.io.Serializable;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.web.rest.fsp.FspResourceAdmin;

/**
 * Criteria class for the {@link FspEntity} entity. This class is used
 * in {@link FspResourceAdmin} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /fsps?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class FspCriteria implements Serializable, Criteria {

    @NoArgsConstructor
    public static class RoleFilter extends Filter<Role> {

        public RoleFilter(RoleFilter filter) {
            super(filter);
        }

        @Override
        public RoleFilter copy() {
            return new RoleFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter validFrom;

    private InstantFilter validTo;

    private BooleanFilter active;

    private LongFilter ownerId;

    private StringFilter createdBy;

    private InstantFilter createdDate;

    private StringFilter lastModifiedBy;

    private InstantFilter lastModifiedDate;

    private StringFilter representativeCompanyName;

    private StringFilter representativeFirstName;

    private StringFilter representativeLastName;

    private StringFilter representativeEmail;

    private StringFilter representativePhoneNumber;

    private BooleanFilter deleted;

    private RoleFilter role;

    public FspCriteria(FspCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.validFrom = other.validFrom == null ? null : other.validFrom.copy();
        this.validTo = other.validTo == null ? null : other.validTo.copy();
        this.active = other.active == null ? null : other.active.copy();
        this.ownerId = other.ownerId == null ? null : other.ownerId.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.lastModifiedBy = other.lastModifiedBy == null ? null : other.lastModifiedBy.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.representativeCompanyName = other.representativeCompanyName == null ? null : other.representativeCompanyName.copy();
        this.representativeFirstName = other.representativeFirstName == null ? null : other.representativeFirstName.copy();
        this.representativeLastName = other.representativeLastName == null ? null : other.representativeLastName.copy();
        this.representativeEmail = other.representativeEmail == null ? null : other.representativeEmail.copy();
        this.representativePhoneNumber = other.representativePhoneNumber == null ? null : other.representativePhoneNumber.copy();
        this.deleted = other.deleted == null ? null : other.deleted.copy();
        this.role = other.role == null ? null : other.role.copy();
    }

    @Override
    public FspCriteria copy() {
        return new FspCriteria(this);
    }
}
