package pl.com.tt.flex.server.service.notification;

import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;

public interface Notifier {

    void notify(NotificationDTO notificationDTO);

    boolean support(NotificationEvent event);

}
