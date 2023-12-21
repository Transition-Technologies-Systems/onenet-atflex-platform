package pl.com.tt.flex.server.service.user;

import pl.com.tt.flex.server.domain.user.UserOnlineEntity;

import java.util.List;
import java.util.Optional;

public interface UserOnlineService {

    void logout(String login);

    Optional<UserOnlineEntity> findByLogin(String login, String ipAddress);

    void saveToken(String login, String token, String ipAddress);

    void invalidateExpiredTokens();

    List<String> findAllLoggedAdminUsersForRefreshViews();

    List<String> findAllLoggedUsersForRefreshCmvcAuctions();

    List<String> findAllLoggedUsersForRefreshDayAheadAuctions();

    List<String> findAllLoggedUsersForRefreshOffersAuctions();
}
