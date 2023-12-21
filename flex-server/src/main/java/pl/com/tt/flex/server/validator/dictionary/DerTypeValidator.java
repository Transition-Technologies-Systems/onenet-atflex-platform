package pl.com.tt.flex.server.validator.dictionary;

import io.github.jhipster.service.filter.LongFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.repository.derType.DerTypeRepository;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeDTO;
import pl.com.tt.flex.server.service.unit.UnitQueryService;
import pl.com.tt.flex.server.service.unit.dto.UnitCriteria;
import pl.com.tt.flex.server.validator.ObjectValidator;

import java.util.Optional;

import static pl.com.tt.flex.server.web.rest.dictionary.derType.DerTypeResource.ENTITY_NAME;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Component
@RequiredArgsConstructor
public class DerTypeValidator implements ObjectValidator<DerTypeDTO, Long> {

    private final DerTypeRepository derTypeRepository;
    private final UnitQueryService unitQueryService;

    public void checkCreateRequest(DerTypeDTO derTypeDTO) throws ObjectValidationException {
        if (derTypeRepository.findByDescriptionEn(derTypeDTO.getDescriptionEn()).isPresent()) {
            throw new ObjectValidationException("Cannot create because der type is exist with this description",
                CANNOT_CREATE_BECAUSE_DER_TYPE_WITH_THIS_DESCRIPTION_ALREADY_EXIST, ENTITY_NAME,
                ActivityEvent.DER_TYPE_CREATED_ERROR, derTypeDTO.getId());
        }
    }

    public void checkUpdatableRequest(DerTypeDTO derTypeDTO) throws ObjectValidationException {
        Optional<DerTypeEntity> derTypeEntity = derTypeRepository.findByDescriptionEn(derTypeDTO.getDescriptionEn());
        if (derTypeEntity.isPresent() && !descriptionEnNotChange(derTypeDTO)) {
            throw new ObjectValidationException("Cannot update because der type is exist with this description",
                CANNOT_UPDATE_BECAUSE_DER_TYPE_WITH_THIS_DESCRIPTION_ALREADY_EXIST, ENTITY_NAME,
                ActivityEvent.DER_TYPE_UPDATED_ERROR, derTypeDTO.getId());
        }
    }

    @Override
    public void checkDeletable(Long derTypeId) throws ObjectValidationException {
        if (unitHasJoinedDerType(derTypeId)) {
            throw new ObjectValidationException("Cannot delete because der type is used by Unit",
                CANNOT_DELETE_BECAUSE_DER_TYPE_IS_USED_BY_UNIT, ENTITY_NAME,
                ActivityEvent.DER_TYPE_DELETED_ERROR, derTypeId);
        }
    }

    private boolean unitHasJoinedDerType(Long derTypeId) {
        UnitCriteria unitCriteria = new UnitCriteria();
        unitCriteria.setDerTypeId((LongFilter) new LongFilter().setEquals(derTypeId));
        return !unitQueryService.findByCriteria(unitCriteria).isEmpty();
    }

    private boolean descriptionEnNotChange(DerTypeDTO derTypeDTO) {
        Optional<DerTypeEntity> derTypeById = derTypeRepository.findById(derTypeDTO.getId());
        return derTypeById.get().getDescriptionEn().equals(derTypeDTO.getDescriptionEn());

    }
}
