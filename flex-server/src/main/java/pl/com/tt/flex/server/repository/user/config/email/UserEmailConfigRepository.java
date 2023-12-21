package pl.com.tt.flex.server.repository.user.config.email;

import java.util.List;

import org.springframework.stereotype.Repository;

import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.user.config.email.UserEmailConfigEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

@Repository
public interface UserEmailConfigRepository extends AbstractJpaRepository<UserEmailConfigEntity, Long> {

    List<UserEmailConfigEntity> findAllByUserId(Long userId);

    boolean existsByUserIdAndEmailTypeAndEnabledFalse(Long userId, EmailType emailType);

}
