package pl.com.tt.flex.server.service.user.registration.dto;

import java.io.Serializable;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.InstantFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.web.rest.user.registration.FspUserRegistrationResource;

/**
 * Criteria class for the {@link FspUserRegistrationEntity} entity. This class is used
 * in {@link FspUserRegistrationResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /fsp-user-registrations?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class FspUserRegistrationCriteria implements Serializable, Criteria {
    /**
     * Class for filtering FspUserRegistrationStatus
     */
    @NoArgsConstructor
    public static class FspUserRegistrationStatusFilter extends Filter<FspUserRegistrationStatus> {

        public FspUserRegistrationStatusFilter(FspUserRegistrationStatusFilter filter) {
            super(filter);
        }

        @Override
        public FspUserRegistrationStatusFilter copy() {
            return new FspUserRegistrationStatusFilter(this);
        }

    }

    @NoArgsConstructor
    public static class UserTargetRoleFilter extends Filter<Role> {

        public UserTargetRoleFilter(UserTargetRoleFilter filter) {
            super(filter);
        }

        @Override
        public UserTargetRoleFilter copy() {
            return new UserTargetRoleFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private StringFilter firstName;

    private StringFilter lastName;

    private StringFilter companyName;

    private StringFilter email;

    private StringFilter phoneNumber;

    private FspUserRegistrationStatusFilter status;

    private LongFilter filesId;

    private LongFilter fspUserId;

    private UserTargetRoleFilter userTargetRole;

    private StringFilter createdBy;

    private InstantFilter createdDate;

    private StringFilter lastModifiedBy;

    private InstantFilter lastModifiedDate;

    public FspUserRegistrationCriteria(FspUserRegistrationCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.firstName = other.firstName == null ? null : other.firstName.copy();
        this.lastName = other.lastName == null ? null : other.lastName.copy();
        this.companyName = other.companyName == null ? null : other.companyName.copy();
        this.email = other.email == null ? null : other.email.copy();
        this.phoneNumber = other.phoneNumber == null ? null : other.phoneNumber.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.filesId = other.filesId == null ? null : other.filesId.copy();
        this.fspUserId = other.fspUserId == null ? null : other.fspUserId.copy();
        this.userTargetRole = other.userTargetRole == null ? null : other.userTargetRole.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.lastModifiedBy = other.lastModifiedBy == null ? null : other.lastModifiedBy.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
    }

    @Override
    public FspUserRegistrationCriteria copy() {
        return new FspUserRegistrationCriteria(this);
    }
}
