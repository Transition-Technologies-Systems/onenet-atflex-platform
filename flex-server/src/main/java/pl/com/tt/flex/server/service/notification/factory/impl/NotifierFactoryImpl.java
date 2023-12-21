package pl.com.tt.flex.server.service.notification.factory.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.service.notification.Notifier;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotifierFactoryImpl implements NotifierFactory {

    private final List<Notifier> notifiers;

    @Override
    public Notifier getNotifier(NotificationEvent event) {
        Optional<Notifier> maybeNotifier = notifiers.stream().filter(notifier -> notifier.support(event)).findAny();
        return maybeNotifier.orElseThrow(() -> new IllegalStateException("Cannot find Notifier by event type  " + event));
    }
}
