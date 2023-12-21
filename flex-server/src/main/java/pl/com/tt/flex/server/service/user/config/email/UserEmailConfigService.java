package pl.com.tt.flex.server.service.user.config.email;

import java.util.Map;

import pl.com.tt.flex.server.domain.email.enumeration.EmailType;

public interface UserEmailConfigService {

    Map<EmailType, Boolean> getConfigForCurrentUser();

    void saveConfigForCurrentUser(Map<EmailType, Boolean> config);

}
