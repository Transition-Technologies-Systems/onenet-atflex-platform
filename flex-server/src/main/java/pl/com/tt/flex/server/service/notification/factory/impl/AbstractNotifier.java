package pl.com.tt.flex.server.service.notification.factory.impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.notification.NotificationService;
import pl.com.tt.flex.server.service.notification.Notifier;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.dto.SocketEventDTO;
import pl.com.tt.flex.server.service.notification.emitter.SocketEventEmitter;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.List;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractNotifier implements Notifier {

    private final SocketEventEmitter socketEventEmitter;
    private final NotificationService notificationService;
    private final UserService userService;

    protected void saveNotification(NotificationDTO notificationDTO) {
        if (!CollectionUtils.isEmpty(notificationDTO.getUsers())) {
            try {
                notificationService.save(notificationDTO);
            } catch (Exception e) {
                log.error("Error while saving notification! {}\n{}", notificationDTO, e.getMessage());
            }
            notificationDTO.getUsers().forEach(user -> socketEventEmitter.postEvent(new SocketEventDTO(notificationDTO.getEventType(), notificationDTO), user.getValue()));
        }
    }

    protected void saveNotificationByRole(NotificationDTO notificationDTO, Set<Role> roles) {
        List<MinimalDTO<Long, String>> users = userService.getUsersByRolesMinimal(roles);
        notificationDTO.setUsers(users);
        saveNotification(notificationDTO);
    }

}
