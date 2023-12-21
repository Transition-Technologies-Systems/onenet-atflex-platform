package pl.com.tt.flex.server.service.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.web.dto.AbstractDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SocketEventDTO extends AbstractDTO {

    private NotificationEvent event;
    private NotificationDTO notification;

    @Override
    public String toString() {
        return "SocketEventDTO{" +
            "event=" + event +
            ", notificationId=" + notification.getId() +
            '}';
    }
}
