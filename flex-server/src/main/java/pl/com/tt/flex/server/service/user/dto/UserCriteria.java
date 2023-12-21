package pl.com.tt.flex.server.service.user.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.security.permission.Role;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserCriteria implements Serializable, Criteria {

    @NoArgsConstructor
    public static class UserRoleFilter extends Filter<Role> {

        public UserRoleFilter(UserCriteria.UserRoleFilter filter) {
            super(filter);
        }

        @Override
        public UserCriteria.UserRoleFilter copy() {
            return new UserCriteria.UserRoleFilter(this);
        }
    }

    private LongFilter id;
    private StringFilter login;
    private StringFilter firstName;
    private StringFilter lastName;
    private StringFilter email;
    private StringFilter phoneNumber;
    private StringFilter companyName;
    private BooleanFilter activated;
    private UserRoleFilter roles;
    private StringFilter createdBy;
    private InstantFilter createdDate;
    private StringFilter lastModifiedBy;
    private InstantFilter lastModifiedDate;
    private BooleanFilter deleted;

    public UserCriteria(UserCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.login = other.login == null ? null : other.login.copy();
        this.firstName = other.firstName == null ? null : other.firstName.copy();
        this.lastName = other.lastName == null ? null : other.lastName.copy();
        this.email = other.email == null ? null : other.email.copy();
        this.phoneNumber = other.phoneNumber == null ? null : other.phoneNumber.copy();
        this.companyName = other.companyName == null ? null : other.companyName.copy();
        this.activated = other.activated == null ? null : other.activated.copy();
        this.roles = other.roles == null ? null : other.roles.copy();
        this.createdBy = other.createdBy == null ? null : other.createdBy.copy();
        this.createdDate = other.createdDate == null ? null : other.createdDate.copy();
        this.lastModifiedBy = other.lastModifiedBy == null ? null : other.lastModifiedBy.copy();
        this.lastModifiedDate = other.lastModifiedDate == null ? null : other.lastModifiedDate.copy();
        this.deleted = other.deleted == null ? null : other.deleted.copy();
    }

    @Override
    public UserCriteria copy() {
        return new UserCriteria(this);
    }
}
