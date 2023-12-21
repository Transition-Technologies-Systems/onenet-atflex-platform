package pl.com.tt.flex.server.service.notification.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.notification.NotificationService;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.emitter.SocketEventEmitter;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.*;

import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.*;

@Component
@Slf4j
public class FlexPotentialNotifier extends AbstractNotifier {

    private final UserService userService;

    private static final List<NotificationEvent> SUPPORTED_EVENTS = Arrays.asList(
        FP_UPDATED,
        FP_DELETED,
        FP_CREATED,
        FP_MOVED_TO_FLEX_REGISTER);

    protected FlexPotentialNotifier(SocketEventEmitter socketEventEmitter, NotificationService notificationService, UserService userService) {
        super(socketEventEmitter, notificationService, userService);
        this.userService = userService;
    }

    @Override
    public void notify(NotificationDTO notificationDTO) {
        if(CollectionUtils.isEmpty(notificationDTO.getUsers())) {
            String companyName = notificationDTO.getParams().get(NotificationParam.COMPANY).getValue();
            saveNotificationByFsp(notificationDTO, companyName);
        } else  {
            saveNotification(notificationDTO);
        }
    }

    @Override
    public boolean support(NotificationEvent event) {
        return SUPPORTED_EVENTS.contains(event);
    }

    protected void saveNotificationByFsp(NotificationDTO notificationDTO, String companyName) {
        Set<MinimalDTO<Long, String>> users = new HashSet<>();
        users.addAll(userService.getUsersByRolesMinimal(Set.of(Role.ROLE_MARKET_OPERATOR, Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR, Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR, Role.ROLE_ADMIN)));
        users.addAll(userService.getUsersByCompanyNameMinimal(companyName));
        notificationDTO.setUsers(new ArrayList<>(users));
        saveNotification(notificationDTO);
    }
}
