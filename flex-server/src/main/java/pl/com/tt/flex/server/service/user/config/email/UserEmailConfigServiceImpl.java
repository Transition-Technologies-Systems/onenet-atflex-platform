package pl.com.tt.flex.server.service.user.config.email;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import pl.com.tt.flex.server.domain.user.config.email.UserEmailConfigEntity;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.config.email.mapper.UserEmailConfigMapper;

@Service
@AllArgsConstructor
public class UserEmailConfigServiceImpl implements UserEmailConfigService {

    private final UserService userService;
    private final UserEmailConfigRepository userEmailConfigRepository;
    private final UserEmailConfigMapper userEmailConfigMapper;

    @Override
    @Transactional(readOnly = true)
    public Map<EmailType, Boolean> getConfigForCurrentUser() {
        UserEntity currentUser = userService.getCurrentUser();
        Map<EmailType, UserEmailConfigEntity> emailConfigsByTypeForUser = getEmailConfigsByTypeForUser(currentUser);
        return userEmailConfigMapper.emailsConfigToMap(emailConfigsByTypeForUser, currentUser.getRoles().stream().findAny().get());
    }

    @Override
    @Transactional
    public void saveConfigForCurrentUser(Map<EmailType, Boolean> config) {
        UserEntity currentUser = userService.getCurrentUser();
        Map<EmailType, UserEmailConfigEntity> dbConfigByEmailType = getEmailConfigsByTypeForUser(currentUser);
        config.forEach((emailType, isEnabled) -> {
            List<UserEmailConfigEntity> configEntitiesToSave = new ArrayList<>();
            if (dbConfigByEmailType.containsKey(emailType)) {
                dbConfigByEmailType.get(emailType).setEnabled(isEnabled);
            } else {
                UserEmailConfigEntity configEntityToSave = UserEmailConfigEntity.builder()
                    .user(currentUser)
                    .emailType(emailType)
                    .enabled(isEnabled)
                    .build();
                configEntitiesToSave.add(configEntityToSave);
            }
            if (configEntitiesToSave.size() > 0) {
                userEmailConfigRepository.saveAll(configEntitiesToSave);
            }
        });
    }

    public Map<EmailType, UserEmailConfigEntity> getEmailConfigsByTypeForUser(UserEntity user) {
        return userEmailConfigRepository.findAllByUserId(user.getId()).stream().collect(Collectors.toMap(UserEmailConfigEntity::getEmailType, Function.identity()));
    }

}
