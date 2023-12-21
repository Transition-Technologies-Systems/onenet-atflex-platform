package pl.com.tt.flex.server.service.dictionary.schedulingUnitType.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeDTO;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeMinDTO;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;
import pl.com.tt.flex.server.util.DictionaryUtils;

import java.util.List;

/**
 * Mapper for the entity {@link SchedulingUnitTypeEntity} and its DTO {@link SchedulingUnitTypeDTO}.
 */
@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface SchedulingUnitTypeMapper extends EntityMapper<SchedulingUnitTypeDTO, SchedulingUnitTypeEntity> {

    List<SchedulingUnitTypeMinDTO> toMinDto(List<SchedulingUnitTypeEntity> entityList);

    SchedulingUnitTypeMinDTO toMinDto(SchedulingUnitTypeEntity entity);

    @AfterMapping
    default void setNlsCode(SchedulingUnitTypeEntity schedulingUnitTypeEntity, @MappingTarget SchedulingUnitTypeDTO schedulingUnitTypeDTO) {
        schedulingUnitTypeDTO.setKey(DictionaryUtils.getKey(schedulingUnitTypeEntity));
        schedulingUnitTypeDTO.setNlsCode(DictionaryUtils.getNlsCode(schedulingUnitTypeEntity));
    }

    @AfterMapping
    default void setNlsCode(SchedulingUnitTypeEntity schedulingUnitTypeEntity, @MappingTarget SchedulingUnitTypeMinDTO schedulingUnitTypeMinDTO) {
        schedulingUnitTypeMinDTO.setKey(DictionaryUtils.getKey(schedulingUnitTypeEntity));
        schedulingUnitTypeMinDTO.setNlsCode(DictionaryUtils.getNlsCode(schedulingUnitTypeEntity));
    }

    default SchedulingUnitTypeEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        SchedulingUnitTypeEntity schedulingUnitTypeEntity = new SchedulingUnitTypeEntity();
        schedulingUnitTypeEntity.setId(id);
        return schedulingUnitTypeEntity;
    }
}
