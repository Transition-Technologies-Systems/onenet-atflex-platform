package pl.com.tt.flex.server.service.dictionary.schedulingUnitType;

import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeDTO;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeMinDTO;
import pl.com.tt.flex.server.service.user.dto.UserDTO;

import java.util.List;

/**
 * Service Interface for managing {@link SchedulingUnitTypeEntity}.
 */
public interface SchedulingUnitTypeService extends AbstractService<SchedulingUnitTypeEntity, SchedulingUnitTypeDTO, Long> {
    List<SchedulingUnitTypeMinDTO> getAllSchedulingUnitTypesMinimal();
    List<SchedulingUnitTypeMinDTO> getSchedulingUnitTypesMinimalByUserRole(UserDTO user);

    List<Long> findAllUnitTypesByFspId(Long fspId);

    void sendNotificationInformingAboutCreated(SchedulingUnitTypeDTO schedulingUnitTypeDTO);

    void sendNotificationInformingAboutModification(SchedulingUnitTypeDTO modifySchedulingUnitTypeDTO);
}
