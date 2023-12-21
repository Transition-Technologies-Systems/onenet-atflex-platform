package pl.com.tt.flex.server.service.user.impl;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.UserOnlineEntity;
import pl.com.tt.flex.server.repository.user.UserOnlineRepository;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.server.security.jwt.TokenProvider;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.dto.SocketEventDTO;
import pl.com.tt.flex.server.service.notification.emitter.SocketEventEmitter;
import pl.com.tt.flex.server.service.user.UserOnlineService;
import pl.com.tt.flex.server.util.InstantUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserOnlineServiceImpl implements UserOnlineService {

    private final UserRepository userRepository;
    private final UserOnlineRepository userOnlineRepository;
    private final TokenProvider tokenProvider;
    private final SocketEventEmitter socketEventEmitter;

    @Override
    public void logout(String login) {
        Optional<UserEntity> oneByLogin = userRepository.findOneByLoginAndDeletedIsFalse(login);
        if (oneByLogin.isPresent()) {
            UserEntity user = oneByLogin.get();
            userOnlineRepository.deactivateUserToken(user);
            socketEventEmitter.postEvent(new SocketEventDTO(NotificationEvent.LOGOUT_USER, new NotificationDTO()), login);
        }
    }

    @Override
    public Optional<UserOnlineEntity> findByLogin(String login, String ipAddress) {
        List<UserOnlineEntity> usersOnline = userOnlineRepository.findByUserLoginAndAddressId(login, ipAddress);
        return usersOnline.stream().findFirst();
    }

    @Override
    @Transactional
    public void saveToken(String login, String token, String ipAddress) {
        UserOnlineEntity userOnline = new UserOnlineEntity();
        userOnline.setUser(userRepository.findOneByLoginAndDeletedIsFalse(login)
            .orElseThrow(() -> new IllegalStateException("Cannot find user with given login. ")));
        userOnline.setToken(token);
        userOnline.setAddressId(ipAddress);
        userOnline.setCreatedDate(InstantUtil.now());

        userOnlineRepository.save(userOnline);
    }

    @Override
    @Transactional
    public void invalidateExpiredTokens() {
        List<UserOnlineEntity> usersOnline = userOnlineRepository.findAll();
        Set<String> users = Sets.newHashSet();
        usersOnline.forEach(entity -> {
            boolean isValid = tokenProvider.validateToken(entity.getToken(), entity.getAddressId());
            if (!isValid) {
                log.debug("Invalidate token {} for user {}", entity.getId(), entity.getUser().getLogin());
                users.add(entity.getUser().getLogin());
            }
        });
        users.stream()
            .filter(login -> !CollectionUtils.isEmpty(userOnlineRepository.findByUserLogin(login)))
            .forEach(this::logout);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllLoggedAdminUsersForRefreshViews() {
        return userOnlineRepository.findAllLoggedUsersByRoles(Role.REFRESH_AUCTIONS_AND_OFFERS_ADMIN_ROLES.stream().map(Enum::name).collect(Collectors.toSet()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllLoggedUsersForRefreshCmvcAuctions() {
        return userOnlineRepository.findAllLoggedUsersByRoles(Role.REFRESH_CMVC_AUCTIONS_USER_ROLES.stream().map(Enum::name).collect(Collectors.toSet()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllLoggedUsersForRefreshDayAheadAuctions() {
        return userOnlineRepository.findAllLoggedUsersByRoles(Role.REFRESH_DAY_AHEAD_AUCTIONS_USER_ROLES.stream().map(Enum::name).collect(Collectors.toSet()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findAllLoggedUsersForRefreshOffersAuctions() {
        return userOnlineRepository.findAllLoggedUsersByRoles(Role.REFRESH_OFFERS_USER_ROLES.stream().map(Enum::name).collect(Collectors.toSet()));
    }
}
