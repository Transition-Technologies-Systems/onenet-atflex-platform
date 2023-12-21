package pl.com.tt.flex.server.service.notification.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.service.notification.NotificationService;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.emitter.SocketEventEmitter;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.*;

import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.UNIT_CREATED;
import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.UNIT_UPDATED;

@Component
@Slf4j
public class TsoDsoNotifier extends AbstractNotifier {

    private static final List<NotificationEvent> SUPPORTED_EVENTS = Arrays.asList(
        UNIT_CREATED, UNIT_UPDATED);

    protected TsoDsoNotifier(SocketEventEmitter socketEventEmitter, NotificationService notificationService, UserService userService) {
        super(socketEventEmitter, notificationService, userService);
    }

    @Override
    public void notify(NotificationDTO notificationDTO) {
        saveNotification(notificationDTO);
    }

    @Override
    public boolean support(NotificationEvent event) {
        return SUPPORTED_EVENTS.contains(event);
    }
}
