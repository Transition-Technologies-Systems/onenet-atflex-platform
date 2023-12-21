package pl.com.tt.flex.server.security.permission;

import pl.com.tt.flex.model.security.permission.Role;

import java.util.List;

public interface AuthoritiesContainer {

    List<String> getAuthorities();

    boolean supports(Role role);

}
