package pl.com.tt.flex.server.service.notification.factory.impl;

import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.DISAGGREGATION_COMPLETED;
import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.DISAGGREGATION_FAILED;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.service.notification.NotificationService;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.emitter.SocketEventEmitter;
import pl.com.tt.flex.server.service.user.UserService;

@Slf4j
@Component
public class DisaggregationResultNotifier extends AbstractNotifier {

    private static final List<NotificationEvent> SUPPORTED_EVENTS = List.of(DISAGGREGATION_COMPLETED, DISAGGREGATION_FAILED);

    public DisaggregationResultNotifier(SocketEventEmitter socketEventEmitter, NotificationService notificationService, UserService userService) {
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
