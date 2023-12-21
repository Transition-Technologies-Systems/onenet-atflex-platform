package pl.com.tt.flex.server.service.user.dto;

import lombok.*;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.model.security.permission.Role;

import java.util.Set;

/**
 * A minimal DTO representing a user.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserMinDTO {

    private Long id;
    private String login;
    private String firstName;
    private String lastName;
    private String email;
    private String companyName;
    private String phoneNumber;
    private String langKey;
    private Set<Role> roles;

    public UserMinDTO(UserEntity user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.email = user.getEmail();
        this.companyName = user.getCompanyName();
        this.phoneNumber = user.getPhoneNumber();
        this.langKey = user.getLangKey();
        this.roles = user.getRoles();
    }

    @Override
    public String toString() {
        return "UserMinDTO{" +
            ", id='" + id + '\'' +
            ", login='" + login + '\'' +
            '}';
    }
}
