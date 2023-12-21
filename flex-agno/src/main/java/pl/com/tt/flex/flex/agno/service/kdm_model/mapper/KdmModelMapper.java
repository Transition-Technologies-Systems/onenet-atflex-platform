package pl.com.tt.flex.flex.agno.service.kdm_model.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelEntity;
import pl.com.tt.flex.flex.agno.service.mapper.EntityMapper;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmAreaDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelMinimalDTO;

/**
 * Mapper for the entity {@link KdmModelEntity} and its DTO {@link KdmModelDTO}.
 */
@Mapper(componentModel = "spring", uses = {KdmModelTimestampFileMapper.class})
public interface KdmModelMapper extends EntityMapper<KdmModelDTO, KdmModelEntity> {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "areaName", source = "areaName")
    @Mapping(target = "lvModel", source = "lvModel")
    @Mapping(target = "timestamps", source = "timestampFiles")
    KdmModelMinimalDTO toMinimalDTO(KdmModelEntity kdmModelEntity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "areaName", source = "areaName")
    @Mapping(target = "lvModel", source = "lvModel")
    KdmAreaDTO toAreaDTO(KdmModelEntity kdmModelEntity);

    @AfterMapping
    default void linkFiles(@MappingTarget KdmModelEntity kdmModelEntity) {
        kdmModelEntity.getTimestampFiles().forEach(timestampFileEntity -> timestampFileEntity.setKdmModel(kdmModelEntity));
    }

    default KdmModelEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        KdmModelEntity kdmModelEntity = new KdmModelEntity();
        kdmModelEntity.setId(id);
        return kdmModelEntity;
    }
}

