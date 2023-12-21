package pl.com.tt.flex.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.notification.NotificationEntity;

import java.util.List;

/**
 * Spring Data  repository for the NotificationEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long>, JpaSpecificationExecutor<NotificationEntity> {

    @Query("SELECT n FROM NotificationUserEntity n JOIN n.user u WHERE u.login = :login")
    List<NotificationEntity> findAllByUser(@Param("login") String login);

}
