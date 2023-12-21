package pl.com.tt.flex.server.service.dictionary.localizationType;

import pl.com.tt.flex.model.service.dto.localization.LocalizationType;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.service.AbstractService;

import java.util.List;

/**
 * Service Interface for managing {@link LocalizationTypeEntity}.
 */
public interface LocalizationTypeService extends AbstractService<LocalizationTypeEntity, LocalizationTypeDTO, Long> {

    LocalizationTypeDTO saveType(LocalizationTypeDTO localizationTypeDTO) throws ObjectValidationException;

    void deleteType(Long typeId) throws ObjectValidationException;

    List<LocalizationTypeDTO> findAllByTypes(List<LocalizationType> localizationTypes);

    List<LocalizationTypeDTO> findAllByUnitIds(List<Long> unitIds);

    void sendNotificationInformingAboutCreated(LocalizationTypeDTO localizationTypeDTO);

    void sendNotificationInformingAboutModification(LocalizationTypeDTO modifyLocalizationTypeDTO);
}
