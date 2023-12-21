package pl.com.tt.flex.server.repository.user.config.screen;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.com.tt.flex.server.domain.user.config.screen.ScreenColumnEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;

import java.util.List;

public interface ScreenColumnRepository extends JpaRepository<ScreenColumnEntity, Long> {

    @Query("SELECT s FROM ScreenColumnEntity s WHERE s.userScreenConfig.user.id = :userId AND s.userScreenConfig.screen = :screen " +
        "ORDER BY s.orderNr")
    List<ScreenColumnEntity>  findAllByUserAndScreen(@Param("userId") Long userId, @Param("screen") Screen screen);
}
