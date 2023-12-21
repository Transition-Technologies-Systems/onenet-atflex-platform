package pl.com.tt.flex.server.service.user.config.screen;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.user.config.screen.ScreenColumnEntity;
import pl.com.tt.flex.server.domain.user.config.screen.UserScreenConfigEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.user.config.screen.ScreenColumnRepository;
import pl.com.tt.flex.server.repository.user.config.screen.UserScreenConfigRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.config.screen.dto.UserScreenConfigDTO;
import pl.com.tt.flex.server.service.user.config.screen.mapper.UserScreenConfigMapper;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserScreenConfigServiceImpl extends AbstractServiceImpl<UserScreenConfigEntity, UserScreenConfigDTO, Long> implements UserScreenConfigService {

    private final UserScreenConfigRepository userScreenConfigRepository;
    private final ScreenColumnRepository screenColumnRepository;
    private final UserScreenConfigMapper userScreenConfigMapper;
    private final UserService userService;

    @Override
    public UserScreenConfigDTO save(UserScreenConfigDTO userScreenConfigDTO) {
        if (userScreenConfigDTO.getUserId() == null) {
            userScreenConfigDTO.setUserId(getCurrentLoggedUserId());
        }
        return super.save(userScreenConfigDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserScreenConfigDTO> getForCurrentUserByScreen(Screen screen) {
        return userScreenConfigRepository.findByUserIdAndScreen(getCurrentLoggedUserId(), screen).map(userScreenConfigMapper::toDto);
    }

    public List<ScreenColumnEntity> getScreenColumnEntities(Screen screen) {
        return screenColumnRepository.findAllByUserAndScreen(getCurrentLoggedUserId(), screen);
    }

    private long getCurrentLoggedUserId() {
        return userService.getCurrentUser().getId();
    }

    @Override
    public AbstractJpaRepository<UserScreenConfigEntity, Long> getRepository() {
        return userScreenConfigRepository;
    }

    @Override
    public EntityMapper<UserScreenConfigDTO, UserScreenConfigEntity> getMapper() {
        return userScreenConfigMapper;
    }
}
