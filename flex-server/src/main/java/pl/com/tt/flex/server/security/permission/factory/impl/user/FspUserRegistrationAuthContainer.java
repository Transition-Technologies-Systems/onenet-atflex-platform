package pl.com.tt.flex.server.security.permission.factory.impl.user;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.security.permission.AuthoritiesContainer;
import pl.com.tt.flex.model.security.permission.Authority;
import pl.com.tt.flex.model.security.permission.Role;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * Authorities needed for FSP user registration process
 * @see pl.com.tt.flex.server.web.rest.user.registration.FspUserRegistrationResource
 */
@Component
public class FspUserRegistrationAuthContainer implements AuthoritiesContainer {

    private List<String> authorities;

    @PostConstruct
    public void init() {
        authorities = Lists.newArrayList();

        // OneNet Flexibility Platform FSP - FLEX USER
        authorities.add(Authority.FLEX_USER_LOGIN);
        authorities.add(Authority.FLEX_USER_CHANGE_PASSWORD);

        // dostep do okna procesu rejestracji
        authorities.add(Authority.FLEX_USER_FSP_REGISTRATION_VIEW);
        authorities.add(Authority.FLEX_USER_FSP_REGISTRATION_MANAGE);

        authorities.add(Authority.FLEX_USER_VIEW_NOTIFICATION);

    }

    @Override
    public List<String> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean supports(Role role) {
        return Role.ROLE_FSP_USER_REGISTRATION.equals(role);
    }
}
