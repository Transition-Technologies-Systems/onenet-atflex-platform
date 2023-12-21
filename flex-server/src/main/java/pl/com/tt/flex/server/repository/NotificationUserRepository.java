package pl.com.tt.flex.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.notification.NotificationUserEntity;

import java.util.List;

/**
 * Spring Data  repository for the NotificationUserEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NotificationUserRepository extends JpaRepository<NotificationUserEntity, Long>, JpaSpecificationExecutor<NotificationUserEntity> {

    @Modifying
    @Query("UPDATE NotificationUserEntity n SET n.read = true WHERE n.id IN (SELECT nt.id FROM NotificationUserEntity nt WHERE nt.id IN :ids AND nt.user.login = :login)")
    void updateNotification(@Param("ids") List<Long> notificationIds, @Param("login") String login);

    @Query("SELECT COUNT (n) FROM NotificationUserEntity n WHERE n.read = false AND n.user.login = :login")
    Long countNotRead(@Param("login") String login);

    @Query("SELECT n.read FROM NotificationUserEntity n WHERE n.notification.id = :id AND n.user.login = :login")
    boolean isUserReadNotification(@Param("id") Long notificationId, @Param("login") String login);

    @Modifying
    @Query("UPDATE NotificationUserEntity n SET n.read = true WHERE n.id IN (SELECT nt.id FROM NotificationUserEntity nt WHERE nt.user.login = :login)")
    void updateAllUsersNotifications(@Param("login") String login);
}
