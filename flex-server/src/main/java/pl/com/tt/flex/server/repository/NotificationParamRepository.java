package pl.com.tt.flex.server.repository;

import pl.com.tt.flex.server.domain.notification.NotificationParamEntity;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data  repository for the NotificationParamEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NotificationParamRepository extends JpaRepository<NotificationParamEntity, Long> {
}
