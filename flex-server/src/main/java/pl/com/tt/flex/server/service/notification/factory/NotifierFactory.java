package pl.com.tt.flex.server.service.notification.factory;

import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.service.notification.Notifier;

public interface NotifierFactory {

    Notifier getNotifier(NotificationEvent notificationEvent);
}
