package pl.com.tt.flex.server.validator.dictionary;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.repository.localizationType.LocalizationTypeRepository;
import pl.com.tt.flex.server.validator.ObjectValidator;

import java.util.Optional;

import static pl.com.tt.flex.server.web.rest.dictionary.localizationType.LocalizationTypeResourceAdmin.ENTITY_NAME;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Component
@RequiredArgsConstructor
public class LocalizationTypeValidator implements ObjectValidator<LocalizationTypeDTO, Long> {

    private final LocalizationTypeRepository localizationTypeRepository;

    @Override
    public void checkValid(LocalizationTypeDTO localizationTypeDTO) throws ObjectValidationException {
        if (localizationTypeRepository.existsByNameAndType(localizationTypeDTO.getName(), localizationTypeDTO.getType())) {
            throw new ObjectValidationException("Cannot create because localization type is exist with this name and type",
                CANNOT_CREATE_BECAUSE_LOCALIZATION_TYPE_WITH_THIS_NAME_AND_TYPE_ALREADY_EXIST, ENTITY_NAME,
                ActivityEvent.LOCALIZATION_TYPE_CREATED_ERROR, localizationTypeDTO.getId());
        }
    }

    @Override
    public void checkModifiable(LocalizationTypeDTO localizationTypeDTO) throws ObjectValidationException {
        Optional<LocalizationTypeEntity> entityOptional = localizationTypeRepository.findById(localizationTypeDTO.getId());
        if (entityOptional.isPresent()
            && isChangedUniqueFields(localizationTypeDTO, entityOptional.get())
            && localizationTypeRepository.existsByNameAndType(localizationTypeDTO.getName(), localizationTypeDTO.getType())) {
            throw new ObjectValidationException("Cannot update because localization type is exist with this name and type",
                CANNOT_UPDATE_BECAUSE_LOCALIZATION_TYPE_WITH_THIS_NAME_AND_TYPE_ALREADY_EXIST, ENTITY_NAME,
                ActivityEvent.LOCALIZATION_TYPE_UPDATED_ERROR, localizationTypeDTO.getId());
        }
    }

    @Override
    public void checkDeletable(Long localizationTypeId) throws ObjectValidationException {
        if (isLocalizationTypeIsUsedInUnit(localizationTypeId)) {
            throw new ObjectValidationException("Cannot delete because localization type is used by unit",
                CANNOT_DELETE_BECAUSE_LOCALIZATION_TYPE_IS_USED_BY_UNIT, ENTITY_NAME,
                ActivityEvent.LOCALIZATION_TYPE_DELETED_ERROR, localizationTypeId);
        }
        if (isLocalizationTypeIsUsedInSubportfolio(localizationTypeId)) {
            throw new ObjectValidationException("Cannot delete because localization type is used by subportfolio",
                CANNOT_DELETE_BECAUSE_LOCALIZATION_TYPE_IS_USED_BY_SUBPORTFOLIO, ENTITY_NAME,
                ActivityEvent.LOCALIZATION_TYPE_DELETED_ERROR, localizationTypeId);
        }
    }

    private boolean isLocalizationTypeIsUsedInUnit(Long id) {
        boolean isExistUnitWithCouplingPointTypeId = localizationTypeRepository.existUnitWithCouplingPointTypeId(id);
        boolean isExistUnitWithPowerStationTypeId = localizationTypeRepository.existUnitWithPowerStationTypeId(id);
        return isExistUnitWithPowerStationTypeId || isExistUnitWithCouplingPointTypeId;
    }

    private boolean isLocalizationTypeIsUsedInSubportfolio(Long id) {
        return localizationTypeRepository.existSubportfolioWithCouplingPointTypeId(id);
    }

    private boolean isChangedUniqueFields(LocalizationTypeDTO localizationTypeDTO, LocalizationTypeEntity dbEntity) {
        return nameHasChange(localizationTypeDTO, dbEntity) || typeHasChange(localizationTypeDTO, dbEntity);
    }

    private boolean nameHasChange(LocalizationTypeDTO localizationTypeDTO, LocalizationTypeEntity dbEntity) {
        return !dbEntity.getName().equals(localizationTypeDTO.getName());
    }

    private boolean typeHasChange(LocalizationTypeDTO localizationTypeDTO, LocalizationTypeEntity dbEntity) {
        return !dbEntity.getType().equals(localizationTypeDTO.getType());
    }
}
