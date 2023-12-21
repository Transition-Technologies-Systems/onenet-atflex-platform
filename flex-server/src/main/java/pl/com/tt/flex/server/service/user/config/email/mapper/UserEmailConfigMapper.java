package pl.com.tt.flex.server.service.user.config.email.mapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.domain.user.config.email.EmailTypesContainer;
import pl.com.tt.flex.server.domain.user.config.email.UserEmailConfigEntity;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;

@Service
@RequiredArgsConstructor
public class UserEmailConfigMapper {

    private final List<EmailTypesContainer> emailTypeContainers;

    public Map<EmailType, Boolean> emailsConfigToMap(Map<EmailType, UserEmailConfigEntity> dbConfigsByType, Role role) {
        Map<EmailType, Boolean> emailConfigMap = new HashMap<>();
        getOptionalEmailTypesForRole(role).forEach(type -> {
            boolean enabled = true;
            if(dbConfigsByType.containsKey(type)) {
                enabled = dbConfigsByType.get(type).isEnabled();
            }
            emailConfigMap.put(type, enabled);
        });
        return emailConfigMap;
    }

    public List<EmailType> getOptionalEmailTypesForRole(Role role) {
        Optional<EmailTypesContainer> maybeEmailTypes = emailTypeContainers.stream().filter(container -> container.supports(role)).findAny();
        if (maybeEmailTypes.isPresent()) {
            return maybeEmailTypes.get().getOptionalEmailTypes();
        }

        throw new IllegalStateException("Cannot find EmailTypesContainer implementation for role: " + role);
    }

}
