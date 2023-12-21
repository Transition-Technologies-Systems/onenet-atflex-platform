package pl.com.tt.flex.server.service.notification.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.service.notification.NotificationService;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.emitter.SocketEventEmitter;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.Arrays;
import java.util.List;

import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.*;

@Component
@Slf4j
public class SchedulingUnitNotifier extends AbstractNotifier {

    private static final List<NotificationEvent> SUPPORTED_EVENTS = Arrays.asList(SCHEDULING_UNIT_READY_FOR_TESTS, SCHEDULING_UNIT_YOUR_DER_REMOVED_FROM_SU,
        SCHEDULING_UNIT_DER_REMOVED_FROM_YOUR_SU, SCHEDULING_UNIT_CREATED, SCHEDULING_UNIT_UPDATED, SCHEDULING_UNIT_MOVED_TO_FLEX_REGISTER);

    protected SchedulingUnitNotifier(SocketEventEmitter socketEventEmitter, NotificationService notificationService, UserService userService) {
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
