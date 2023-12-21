package pl.com.tt.flex.server.service.dictionary.schedulingUnitType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitProposalRepository;
import pl.com.tt.flex.server.repository.schedulingUnitType.SchedulingUnitTypeRepository;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeDTO;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeMinDTO;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.mapper.SchedulingUnitTypeMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link SchedulingUnitTypeEntity}.
 */
@Slf4j
@Service
@Transactional
public class SchedulingUnitTypeServiceImpl extends AbstractServiceImpl<SchedulingUnitTypeEntity, SchedulingUnitTypeDTO, Long> implements SchedulingUnitTypeService {

    private final SchedulingUnitTypeRepository schedulingUnitTypeRepository;
    private final SchedulingUnitProposalRepository schedulingUnitProposalRepository;
    private final SchedulingUnitTypeMapper schedulingUnitTypeMapper;
    private final NotifierFactory notifierFactory;
    private final UserService userService;

    public SchedulingUnitTypeServiceImpl(SchedulingUnitTypeRepository schedulingUnitTypeRepository, SchedulingUnitProposalRepository schedulingUnitProposalRepository, SchedulingUnitTypeMapper schedulingUnitTypeMapper, NotifierFactory notifierFactory, UserService userService) {
        this.schedulingUnitTypeRepository = schedulingUnitTypeRepository;
        this.schedulingUnitProposalRepository = schedulingUnitProposalRepository;
        this.schedulingUnitTypeMapper = schedulingUnitTypeMapper;
        this.notifierFactory = notifierFactory;
        this.userService = userService;
    }

    @Override
    public List<SchedulingUnitTypeMinDTO> getAllSchedulingUnitTypesMinimal() {
        return schedulingUnitTypeMapper.toMinDto(schedulingUnitTypeRepository.findAll());
    }

    @Override
    public List<SchedulingUnitTypeMinDTO> getSchedulingUnitTypesMinimalByUserRole(UserDTO user) {
        List<SchedulingUnitTypeMinDTO> typesList = getAllSchedulingUnitTypesMinimal();
        if (user.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER) || user.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            List<Long> listOfSchedulingUnitTypesWithJoinedDersOfFsp = findAllUnitTypesByFspId(user.getFspId());
            typesList = typesList.stream().filter(type -> listOfSchedulingUnitTypesWithJoinedDersOfFsp.contains(type.getId())).collect(Collectors.toList());
        }
        return typesList;
    }

    @Override
    public List<Long> findAllUnitTypesByFspId(Long fspId) {
        return schedulingUnitProposalRepository.findAllUnitTypesWithJoinedDersOfFsp(fspId);
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************

    @Override
    @Transactional
    public void sendNotificationInformingAboutCreated(SchedulingUnitTypeDTO schedulingUnitTypeDTO) {
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, schedulingUnitTypeDTO.getId())
            .build();
        List<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(schedulingUnitTypeDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SU_TYPE_CREATED, notificationParams, new ArrayList<>(usersToBeNotified));
    }

    @Override
    @Transactional
    public void sendNotificationInformingAboutModification(SchedulingUnitTypeDTO schedulingUnitTypeDTO) {
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ID, schedulingUnitTypeDTO.getId())
            .build();
        List<MinimalDTO<Long, String>> usersToBeNotified = getUsersToBeNotified(schedulingUnitTypeDTO);
        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.SU_TYPE_UPDATED, notificationParams, new ArrayList<>(usersToBeNotified));
    }

    private List<MinimalDTO<Long, String>> getUsersToBeNotified(SchedulingUnitTypeDTO schedulingUnitTypeDTO) {
        SchedulingUnitTypeEntity dbType = getRepository().findById(schedulingUnitTypeDTO.getId())
            .orElseThrow(() -> new RuntimeException("Cannot find SchedulingUnitType with id: " + schedulingUnitTypeDTO.getId()));
        return userService.getUsersByLogin(NotificationUtils.getLoginsOfUsersToBeNotified(userService.getCurrentUser().getLogin(), dbType));
    }

    //********************************************************************************** NOTIFICATION ************************************************************************************


    @Override
    public AbstractJpaRepository<SchedulingUnitTypeEntity, Long> getRepository() {
        return this.schedulingUnitTypeRepository;
    }

    @Override
    public EntityMapper<SchedulingUnitTypeDTO, SchedulingUnitTypeEntity> getMapper() {
        return this.schedulingUnitTypeMapper;
    }
}
