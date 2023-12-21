package pl.com.tt.flex.server.service.notification;

import static java.util.Objects.nonNull;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.notification.NotificationEntity;
import pl.com.tt.flex.server.domain.notification.NotificationParamEntity;
import pl.com.tt.flex.server.domain.notification.NotificationUserEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.NotificationRepository;
import pl.com.tt.flex.server.repository.NotificationUserRepository;
import pl.com.tt.flex.server.security.SecurityUtils;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.mapper.NotificationMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.InstantUtil;

/**
 * Service Implementation for managing {@link NotificationEntity}.
 */
@Slf4j
@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    private final NotificationUserRepository notificationUserRepository;

    private final NotificationMapper notificationMapper;

    private final UserService userService;

    public NotificationServiceImpl(final NotificationRepository notificationRepository, final NotificationUserRepository notificationUserRepository,
                                   final NotificationMapper notificationMapper, final UserService userService) {
        this.notificationRepository = notificationRepository;
        this.notificationUserRepository = notificationUserRepository;
        this.notificationMapper = notificationMapper;
        this.userService = userService;
    }

    @Override
    @Transactional
    public NotificationDTO save(NotificationDTO notificationDTO) {
        log.debug("Request to save Notification : {}", notificationDTO);
        NotificationEntity notificationEntity = notificationMapper.toEntity(notificationDTO);
        notificationEntity.setCreatedDate(InstantUtil.now());
        notificationEntity.setNotificationUsers(getUsers(notificationDTO.getUsers(), notificationEntity));
        notificationEntity.setNotificationParams(getParams(notificationDTO.getParams(), notificationEntity));
        notificationEntity = notificationRepository.save(notificationEntity);
        return notificationMapper.toDto(notificationEntity);
    }

    private Set<NotificationUserEntity> getUsers(List<MinimalDTO<Long, String>> users, NotificationEntity notificationEntity) {
        Set<NotificationUserEntity> notificationUserEntities = Sets.newHashSet();
        users.forEach(longStringMinimalDTO -> {
            Optional<UserEntity> maybeUser = userService.findOne(longStringMinimalDTO.getId());
            if (maybeUser.isPresent()) {
                NotificationUserEntity notificationUserEntity = new NotificationUserEntity();
                notificationUserEntity.setUser(maybeUser.get());
                notificationUserEntity.setRead(false);
                notificationUserEntity.setNotification(notificationEntity);
                notificationUserEntities.add(notificationUserEntity);
            }
        });
        return notificationUserEntities;
    }

    private Set<NotificationParamEntity> getParams(Map<NotificationParam, NotificationParamValue> params, NotificationEntity notificationEntity) {
        Set<NotificationParamEntity> result = Sets.newHashSet();
        if (nonNull(params)) {
            params.keySet().forEach(key -> {
                NotificationParamEntity notificationParamEntity = new NotificationParamEntity();
                notificationParamEntity.setName(key.name());
                notificationParamEntity.setValue(params.get(key).getValue());
                notificationParamEntity.setObject(getObjectBytesFromParams(params, key));
                notificationParamEntity.setNotification(notificationEntity);
                if (Objects.isNull(notificationParamEntity.getValue()) && ArrayUtils.isEmpty(notificationParamEntity.getObject())) {
                    log.debug("getParams(): Delete params {} because field value and objects is null", notificationParamEntity.getName());
                } else {
                    result.add(notificationParamEntity);
                }
            });
        }
        return result;
    }

    private byte[] getObjectBytesFromParams(Map<NotificationParam, NotificationParamValue> params, NotificationParam key) {
        if (Objects.nonNull(params.get(key).getObject())) {
            return params.get(key).getObject().getBytes(StandardCharsets.UTF_8);
        }
        return new byte[0];
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Notifications");
        return notificationRepository.findAll(pageable)
            .map(notificationMapper::toDto);
    }

    @Override
    public void markAsRead(List<Long> notificationIds) {
        notificationUserRepository.updateNotification(notificationIds, SecurityUtils.getCurrentUserLogin().get());
    }

    @Override
    public Long countNotRead() {
        return notificationUserRepository.countNotRead(SecurityUtils.getCurrentUserLogin().get());
    }

    @Override
    public void markAllAsRead() {
        notificationUserRepository.updateAllUsersNotifications(SecurityUtils.getCurrentUserLogin().get());
    }
}
