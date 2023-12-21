package pl.com.tt.flex.server.service.dictionary.derType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.domain.unit.enumeration.DerType;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.derType.DerTypeRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeDTO;
import pl.com.tt.flex.server.service.dictionary.derType.mapper.DerTypeMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Service Implementation for managing {@link DerTypeEntity}.
 */
@Slf4j
@Service
@Transactional
public class DerTypeServiceImpl extends AbstractServiceImpl<DerTypeEntity, DerTypeDTO, Long> implements DerTypeService {

    private final DerTypeRepository derTypeRepository;
    private final DerTypeMapper derTypeMapper;
    private final NotifierFactory notifierFactory;
    private final UserService userService;

    public DerTypeServiceImpl(DerTypeRepository derTypeRepository, DerTypeMapper derTypeMapper, NotifierFactory notifierFactory, UserService userService) {
        this.derTypeRepository = derTypeRepository;
        this.derTypeMapper = derTypeMapper;
        this.notifierFactory = notifierFactory;
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdAndType(Long derId, DerType derType) {
        return derTypeRepository.existsByIdAndType(derId, derType);
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************

    @Override
    @Transactional
    public void sendNotificationInformingAboutCreated(DerTypeDTO derTypeDTO) {
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, derTypeDTO.getId())
            .build();
        List<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(derTypeDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.DER_TYPE_CREATED, notificationParams, new ArrayList<>(usersToBeNotified));
    }

    @Override
    @Transactional
    public void sendNotificationInformingAboutModification(DerTypeDTO modifyDerType) {
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, modifyDerType.getId())
            .build();
        List<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(modifyDerType);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.DER_TYPE_UPDATED, notificationParams, new ArrayList<>(usersToBeNotified));
    }

    private List<MinimalDTO<Long, String>> getUsersToBeNotified(DerTypeDTO derTypeDTO) {
        DerTypeEntity dbType = getRepository().findById(derTypeDTO.getId())
            .orElseThrow(() -> new RuntimeException("Cannot find DerType with id: " + derTypeDTO.getId()));
        return userService.getUsersByLogin(NotificationUtils.getLoginsOfUsersToBeNotified(userService.getCurrentUser().getLogin(), dbType));
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************

    @Override
    public AbstractJpaRepository<DerTypeEntity, Long> getRepository() {
        return this.derTypeRepository;
    }

    @Override
    public EntityMapper<DerTypeDTO, DerTypeEntity> getMapper() {
        return this.derTypeMapper;
    }
}
