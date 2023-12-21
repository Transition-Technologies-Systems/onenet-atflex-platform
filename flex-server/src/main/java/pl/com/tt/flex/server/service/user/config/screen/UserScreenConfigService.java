package pl.com.tt.flex.server.service.user.config.screen;

import pl.com.tt.flex.server.domain.user.config.screen.ScreenColumnEntity;
import pl.com.tt.flex.server.domain.user.config.screen.UserScreenConfigEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.server.service.user.config.screen.dto.UserScreenConfigDTO;

import java.util.List;
import java.util.Optional;

public interface UserScreenConfigService extends AbstractService<UserScreenConfigEntity, UserScreenConfigDTO, Long> {

    Optional<UserScreenConfigDTO> getForCurrentUserByScreen(Screen screen);

    List<ScreenColumnEntity> getScreenColumnEntities(Screen screen);
}
