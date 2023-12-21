package pl.com.tt.flex.server.security.permission.factory;

import com.google.common.collect.Sets;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.security.permission.AuthoritiesContainer;
import pl.com.tt.flex.model.security.permission.Role;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
public class AuthoritiesContainerFactory {

    private final List<AuthoritiesContainer> authorities;

    public AuthoritiesContainerFactory(List<AuthoritiesContainer> authorities) {
        this.authorities = authorities;
    }

    public AuthoritiesContainer getRoleAuthoritiesContainer(Role role) {
        Optional<AuthoritiesContainer> maybeAuthorities = authorities.stream().filter(authority -> authority.supports(role)).findAny();
        if (maybeAuthorities.isPresent()) {
            return maybeAuthorities.get();
        }

        throw new IllegalStateException("Cannot find AuthoritiesContainer implementation for role: " + role);
    }

    public Set<String> getUserAuthorities(Set<Role> roles) {
        Set<String> userAuthorities = Sets.newHashSet();
        roles.forEach(userRole -> userAuthorities.addAll(getRoleAuthoritiesContainer(userRole).getAuthorities()));
        return userAuthorities;
    }
}
