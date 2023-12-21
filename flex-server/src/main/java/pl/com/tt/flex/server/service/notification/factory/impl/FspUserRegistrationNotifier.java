package pl.com.tt.flex.server.service.notification.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.service.notification.NotificationService;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.emitter.SocketEventEmitter;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.*;

@Slf4j
@Component
public class FspUserRegistrationNotifier extends AbstractNotifier {

    private static final List<NotificationEvent> SUPPORTED_EVENTS = Arrays.asList(
        FSP_USER_REGISTRATION_NEW,
        FSP_USER_REGISTRATION_CONFIRMED_BY_FSP,
        FSP_USER_REGISTRATION_WITHDRAWN_BY_FSP,
        FSP_USER_REGISTRATION_ACCEPTED_BY_MO,
        FSP_USER_REGISTRATION_REJECTED_BY_MO,
        FSP_USER_REGISTRATION_UPDATED);

    @Autowired
    public FspUserRegistrationNotifier(SocketEventEmitter socketEventEmitter, NotificationService notificationService,
        UserService userService) {
        super(socketEventEmitter, notificationService, userService);
    }

    @Override
    public void notify(NotificationDTO notificationDTO) {
        saveNotificationByRole(notificationDTO, Set.of(Role.ROLE_MARKET_OPERATOR, Role.ROLE_ADMIN));
    }

    @Override
    public boolean support(NotificationEvent event) {
        return SUPPORTED_EVENTS.contains(event);
    }

}
