package pl.com.tt.flex.server.service.dictionary.derType.mapper;


import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeDTO;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.util.DictionaryUtils;

/**
 * Mapper for the entity {@link DerTypeEntity} and its DTO {@link DerTypeDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface DerTypeMapper extends EntityMapper<DerTypeDTO, DerTypeEntity> {

    @AfterMapping
    default void setDerTypeMinimal(DerTypeEntity derTypeEntity, @MappingTarget DerTypeDTO derTypeDTO) {
        derTypeDTO.setKey(DictionaryUtils.getKey(derTypeEntity));
        derTypeDTO.setNlsCode(DictionaryUtils.getNlsCode(derTypeEntity));
    }

    @AfterMapping
    default void setDerTypeMinimal(DerTypeEntity derTypeEntity, @MappingTarget DerTypeMinDTO derTypeDTO) {
        derTypeDTO.setNlsCode(DictionaryUtils.getNlsCode(derTypeEntity));
    }

    default DerTypeEntity fromMinDto(DerTypeMinDTO minDto) {
        if (minDto == null) {
            return null;
        }
        return DerTypeEntity.builder()
            .id(minDto.getId())
            .type(minDto.getType())
            .descriptionEn(minDto.getDescriptionEn())
            .descriptionPl(minDto.getDescriptionPl())
            .build();
    }
}
