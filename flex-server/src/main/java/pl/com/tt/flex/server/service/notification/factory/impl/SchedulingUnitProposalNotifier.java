package pl.com.tt.flex.server.service.notification.factory.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.service.notification.NotificationService;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.emitter.SocketEventEmitter;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.*;

import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.*;

@Component
@Slf4j
public class SchedulingUnitProposalNotifier extends AbstractNotifier {

    private static final List<NotificationEvent> SUPPORTED_EVENTS = Arrays.asList(SCHEDULING_UNIT_PROPOSAL_TO_BSP, SCHEDULING_UNIT_PROPOSAL_TO_FSP,
        SCHEDULING_UNIT_PROPOSAL_TO_BSP_ACCEPTED, SCHEDULING_UNIT_PROPOSAL_TO_BSP_CANCELLED_BY_FSP, SCHEDULING_UNIT_PROPOSAL_TO_BSP_REJECTED_BY_BSP,
        SCHEDULING_UNIT_PROPOSAL_TO_FSP_ACCEPTED, SCHEDULING_UNIT_PROPOSAL_TO_FSP_CANCELLED_BY_BSP, SCHEDULING_UNIT_PROPOSAL_TO_FSP_REJECTED_BY_FSP);

    protected SchedulingUnitProposalNotifier(SocketEventEmitter socketEventEmitter, NotificationService notificationService, UserService userService) {
        super(socketEventEmitter, notificationService, userService);
    }

    @Override
    public void notify(NotificationDTO notificationDTO) {
        saveNotification(notificationDTO); // user ustawiony w DTO
    }

    @Override
    public boolean support(NotificationEvent event) {
        return SUPPORTED_EVENTS.contains(event);
    }
}
