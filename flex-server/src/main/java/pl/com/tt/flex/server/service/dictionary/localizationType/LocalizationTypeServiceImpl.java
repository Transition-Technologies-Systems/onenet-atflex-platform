package pl.com.tt.flex.server.service.dictionary.localizationType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.localization.LocalizationType;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.localizationType.LocalizationTypeRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.dictionary.localizationType.mapper.LocalizationTypeMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.dictionary.LocalizationTypeValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service Implementation for managing {@link LocalizationTypeEntity}.
 */
@Slf4j
@Service
@Transactional
public class LocalizationTypeServiceImpl extends AbstractServiceImpl<LocalizationTypeEntity, LocalizationTypeDTO, Long> implements LocalizationTypeService {

    private final LocalizationTypeRepository localizationTypeRepository;
    private final LocalizationTypeMapper localizationTypeMapper;
    private final LocalizationTypeValidator localizationTypeValidator;
    private final NotifierFactory notifierFactory;
    private final UserService userService;

    public LocalizationTypeServiceImpl(LocalizationTypeRepository localizationTypeRepository, LocalizationTypeMapper localizationTypeMapper, LocalizationTypeValidator localizationTypeValidator, NotifierFactory notifierFactory, UserService userService) {
        this.localizationTypeRepository = localizationTypeRepository;
        this.localizationTypeMapper = localizationTypeMapper;
        this.localizationTypeValidator = localizationTypeValidator;
        this.notifierFactory = notifierFactory;
        this.userService = userService;
    }

    @Override
    public LocalizationTypeDTO saveType(LocalizationTypeDTO localizationTypeDTO) throws ObjectValidationException {
        if (Objects.isNull(localizationTypeDTO.getId())) {
            localizationTypeValidator.checkValid(localizationTypeDTO);
        } else {
            localizationTypeValidator.checkModifiable(localizationTypeDTO);
        }
        return super.save(localizationTypeDTO);
    }

    @Override
    public void deleteType(Long id) throws ObjectValidationException {
        localizationTypeValidator.checkDeletable(id);
        super.delete(id);
    }

    @Override
    public List<LocalizationTypeDTO> findAllByTypes(List<LocalizationType> localizationTypes) {
        return localizationTypeMapper.toDto(localizationTypeRepository.findAllByTypeInOrderByName(localizationTypes));
    }

    @Override
    public List<LocalizationTypeDTO> findAllByUnitIds(List<Long> unitIds) {
        return localizationTypeMapper.toDto(localizationTypeRepository.findAllByUnitIds(unitIds));
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************

    @Override
    @Transactional
    public void sendNotificationInformingAboutCreated(LocalizationTypeDTO localizationTypeDTO) {
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, localizationTypeDTO.getId())
            .build();
        List<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(localizationTypeDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.LOCALIZATION_TYPE_CREATED, notificationParams, new ArrayList<>(usersToBeNotified));
    }

    @Override
    @Transactional
    public void sendNotificationInformingAboutModification(LocalizationTypeDTO localizationTypeDTO) {
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, localizationTypeDTO.getId())
            .build();
        List<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(localizationTypeDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.LOCALIZATION_TYPE_UPDATED, notificationParams, new ArrayList<>(usersToBeNotified));
    }

    private List<MinimalDTO<Long, String>> getUsersToBeNotified(LocalizationTypeDTO localizationTypeDTO) {
        LocalizationTypeEntity dbType = getRepository().findById(localizationTypeDTO.getId())
            .orElseThrow(() -> new RuntimeException("Cannot find Localization with id: " + localizationTypeDTO.getId()));
        return userService.getUsersByLogin(NotificationUtils.getLoginsOfUsersToBeNotified(userService.getCurrentUser().getLogin(), dbType));
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************


    @Override
    public AbstractJpaRepository<LocalizationTypeEntity, Long> getRepository() {
        return this.localizationTypeRepository;
    }

    @Override
    public EntityMapper<LocalizationTypeDTO, LocalizationTypeEntity> getMapper() {
        return this.localizationTypeMapper;
    }
}
