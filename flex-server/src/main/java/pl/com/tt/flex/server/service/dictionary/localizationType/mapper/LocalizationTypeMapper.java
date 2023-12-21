package pl.com.tt.flex.server.service.dictionary.localizationType.mapper;

import org.mapstruct.Mapper;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

/**
 * Mapper for the entity {@link LocalizationTypeEntity} and its DTO {@link LocalizationTypeDTO}.
 */
@Mapper(componentModel = "spring")
public interface LocalizationTypeMapper extends EntityMapper<LocalizationTypeDTO, LocalizationTypeEntity> {

    default LocalizationTypeEntity localizationTypeFromId(Long id) {
        if (id == null) {
            return null;
        }
        LocalizationTypeEntity localizationTypeEntity = new LocalizationTypeEntity();
        localizationTypeEntity.setId(id);
        return localizationTypeEntity;
    }
}
