package pl.com.tt.flex.server.repository.user.config.screen;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.user.config.screen.UserScreenConfigEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.Optional;

@Repository
public interface UserScreenConfigRepository extends AbstractJpaRepository<UserScreenConfigEntity, Long> {

    Optional<UserScreenConfigEntity> findByUserIdAndScreen(@Param("userId") Long userId, @Param("screen") Screen screen);

}
