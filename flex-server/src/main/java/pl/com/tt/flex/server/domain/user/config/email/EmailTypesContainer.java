package pl.com.tt.flex.server.domain.user.config.email;

import java.util.List;

import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;

public interface EmailTypesContainer {

    List<EmailType> getEmailTypes();

    List<EmailType> getOptionalEmailTypes();

    boolean supports(Role role);

}
