package pl.com.tt.flex.server.service.notification.factory.impl;

import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.CONNECTION_TO_ALGORITHM_SERVICE_LOST;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.service.notification.NotificationService;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.emitter.SocketEventEmitter;
import pl.com.tt.flex.server.service.user.UserService;

@Slf4j
@Component
public class ConnectionToAlgorithmServiceLostNotifier extends AbstractNotifier {

    private static final NotificationEvent SUPPORTED_EVENT = CONNECTION_TO_ALGORITHM_SERVICE_LOST;

    public ConnectionToAlgorithmServiceLostNotifier(SocketEventEmitter socketEventEmitter, NotificationService notificationService, UserService userService) {
        super(socketEventEmitter, notificationService, userService);
    }

    @Override
    public void notify(NotificationDTO notificationDTO) {
        saveNotification(notificationDTO);
    }

    @Override
    public boolean support(NotificationEvent event) {
        return SUPPORTED_EVENT.equals(event);
    }
}
