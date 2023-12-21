package pl.com.tt.flex.server.service.notification.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.notification.NotificationService;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.emitter.SocketEventEmitter;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.Arrays;
import java.util.List;

import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.*;

@Component
@Slf4j
public class FspNotifier extends AbstractNotifier {

    private final UserService userService;

    private static final List<NotificationEvent> SUPPORTED_EVENTS = Arrays.asList(
        UNIT_HAS_BEEN_CERTIFIED,
        UNIT_LOST_CERTIFICATION,
        FSP_UPDATED,
        BSP_UPDATED);

    protected FspNotifier(SocketEventEmitter socketEventEmitter, NotificationService notificationService, UserService userService) {
        super(socketEventEmitter, notificationService, userService);
        this.userService = userService;
    }

    @Override
    public void notify(NotificationDTO notificationDTO) {
        if(CollectionUtils.isEmpty(notificationDTO.getUsers())) {
            saveNotificationForFspUser(notificationDTO);
        } else {
            saveNotification(notificationDTO);
        }
    }

    @Override
    public boolean support(NotificationEvent event) {
        return SUPPORTED_EVENTS.contains(event);
    }

    private void saveNotificationForFspUser(NotificationDTO notificationDTO) {
        String companyName = notificationDTO.getParams().get(NotificationParam.COMPANY).getValue();
        List<MinimalDTO<Long, String>> usersToSendNotification = userService.getUsersByCompanyNameMinimal(companyName);
        notificationDTO.setUsers(usersToSendNotification);
        saveNotification(notificationDTO);
    }
}
