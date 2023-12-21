package pl.com.tt.flex.server.service.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.com.tt.flex.server.domain.notification.NotificationEntity;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;

import java.util.List;

/**
 * Service Interface for managing {@link NotificationEntity}.
 */
public interface NotificationService {

    NotificationDTO save(NotificationDTO notificationDTO);

    Page<NotificationDTO> findAll(Pageable pageable);

    void markAsRead(List<Long> notificationIds);

    Long countNotRead();

    void markAllAsRead();

}
