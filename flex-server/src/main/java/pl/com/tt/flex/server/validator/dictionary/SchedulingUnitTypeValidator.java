package pl.com.tt.flex.server.validator.dictionary;

import io.github.jhipster.service.filter.LongFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.repository.schedulingUnitType.SchedulingUnitTypeRepository;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeDTO;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitQueryService;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitCriteria;
import pl.com.tt.flex.server.validator.ObjectValidator;

import java.util.Optional;

import static pl.com.tt.flex.server.web.rest.dictionary.schedulingUnitType.SchedulingUnitTypeResource.ENTITY_NAME;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Component
@RequiredArgsConstructor
public class SchedulingUnitTypeValidator implements ObjectValidator<SchedulingUnitTypeDTO, Long> {

    private final SchedulingUnitTypeRepository schedulingUnitTypeRepository;
    private final SchedulingUnitQueryService schedulingUnitQueryService;

    @Override
    public void checkValid(SchedulingUnitTypeDTO schedulingUnitTypeDTO) throws ObjectValidationException {
        if (schedulingUnitTypeRepository.findByDescriptionEn(schedulingUnitTypeDTO.getDescriptionEn()).isPresent()) {
            throw new ObjectValidationException("Cannot create because scheduling unit type is exist with this description",
                CANNOT_CREATE_BECAUSE_SCHEDULING_UNIT_TYPE_WITH_THIS_DESCRIPTION_ALREADY_EXIST, ENTITY_NAME,
                ActivityEvent.SCHEDULING_UNIT_TYPE_CREATED_ERROR, schedulingUnitTypeDTO.getId());
        }
    }

    @Override
    public void checkModifiable(SchedulingUnitTypeDTO schedulingUnitTypeDTO) throws ObjectValidationException {
        Optional<SchedulingUnitTypeEntity> schedulingUnitTypeEntity = schedulingUnitTypeRepository.findByDescriptionEn(schedulingUnitTypeDTO.getDescriptionEn());
        if (schedulingUnitTypeEntity.isPresent() && !descriptionEnNotChange(schedulingUnitTypeDTO)) {
            throw new ObjectValidationException("Cannot update because scheduling unit type is exist with this description",
                CANNOT_UPDATE_BECAUSE_SCHEDULING_UNIT_TYPE_WITH_THIS_DESCRIPTION_ALREADY_EXIST, ENTITY_NAME,
                ActivityEvent.SCHEDULING_UNIT_TYPE_UPDATED_ERROR, schedulingUnitTypeDTO.getId());
        }
    }

    @Override
    public void checkDeletable(Long suTypeId) throws ObjectValidationException {
        if (suHasJoinedSuType(suTypeId)) {
            throw new ObjectValidationException("Cannot delete because scheduling unit type is used by SchedulingUnit",
                CANNOT_DELETE_BECAUSE_SCHEDULING_UNIT_TYPE_IS_USED_BY_SCHEDULING_UNIT, ENTITY_NAME,
                ActivityEvent.SCHEDULING_UNIT_TYPE_DELETED_ERROR, suTypeId);
        }
    }

    private boolean descriptionEnNotChange(SchedulingUnitTypeDTO schedulingUnitTypeDTO) {
        Optional<SchedulingUnitTypeEntity> schedulingUnitTypeEntity = schedulingUnitTypeRepository.findById(schedulingUnitTypeDTO.getId());
        return schedulingUnitTypeEntity.get().getDescriptionEn().equals(schedulingUnitTypeDTO.getDescriptionEn());
    }

    private boolean suHasJoinedSuType(Long suTypeId) {
        SchedulingUnitCriteria schedulingUnitCriteria = new SchedulingUnitCriteria();
        schedulingUnitCriteria.setSchedulingUnitTypeId((LongFilter) new LongFilter().setEquals(suTypeId));
        return !schedulingUnitQueryService.findByCriteria(schedulingUnitCriteria).isEmpty();
    }
}
