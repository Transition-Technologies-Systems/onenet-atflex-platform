package pl.com.tt.flex.server.service.user.dto;

import lombok.*;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.validator.constraints.UniqueUserEmail;
import pl.com.tt.flex.server.validator.constraints.UniqueUserLogin;

import javax.validation.constraints.*;
import java.time.Instant;
import java.util.Set;

import static java.util.Objects.nonNull;

/**
 * A DTO representing a user, with his roles and authorities.
 */
@Getter
@Setter
@UniqueUserLogin
@UniqueUserEmail
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "authorities")
public class UserDTO {
    private Long id;

    @NotBlank
    @Pattern(regexp = Constants.LOGIN_REGEX)
    @Size(min = 1, max = 50)
    private String login;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    @Email
    @NotNull
    @Size(min = 5, max = 254)
    private String email;

    @Size(max = 50)
    private String phoneNumber;

    @Size(max = 254)
    private String companyName;

    private boolean activated = false;

    @Size(min = 2, max = 10)
    private String langKey;

    private String createdBy;

    private Instant createdDate;

    private String lastModifiedBy;

    private Instant lastModifiedDate;

    private Set<Role> roles;

    private Set<String> authorities;

    private Long fspId;

    private Boolean fspActive;

    private boolean fspOwner;

    private String password;

    private boolean passwordChangeOnFirstLogin;

    private Integer unsuccessfulLoginCount;

    private Instant lastSuccessfulLoginDate;

    private String fspCompany;

    public UserDTO(UserEntity user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.phoneNumber = user.getPhoneNumber();
        this.activated = user.isActivated();
        this.langKey = user.getLangKey();
        this.createdBy = user.getCreatedBy();
        this.createdDate = user.getCreatedDate();
        this.lastModifiedBy = user.getLastModifiedBy();
        this.lastModifiedDate = user.getLastModifiedDate();
        this.companyName = user.getCompanyName();
        this.passwordChangeOnFirstLogin = user.isPasswordChangeOnFirstLogin();
        this.unsuccessfulLoginCount = user.getUnsuccessfulLoginCount();
        this.lastSuccessfulLoginDate = user.getLastSuccessfulLoginDate();
        this.roles = user.getRoles();
        if (nonNull(user.getFsp())) {
            this.fspId = user.getFsp().getId();
            this.fspActive = user.getFsp().isActive();
            this.fspCompany = user.getFsp().getCompanyName();
            if (nonNull(user.getFsp().getOwner())) {
                this.fspOwner = user.getId().equals(user.getFsp().getOwner().getId());
            }
        }
    }

    @Override
    public String toString() {
        return "UserDTO{" +
            ", id='" + id + '\'' +
            ", login='" + login + '\'' +
            '}';
    }

    public boolean hasRole(Role role) {
        return this.roles.contains(role);
    }

    public boolean hasAnyRole(Set<Role> role) {
        return this.roles.stream().anyMatch(role::contains);
    }
}
