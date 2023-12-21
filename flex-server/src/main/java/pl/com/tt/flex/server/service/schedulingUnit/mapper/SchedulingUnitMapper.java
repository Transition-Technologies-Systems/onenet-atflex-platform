package pl.com.tt.flex.server.service.schedulingUnit.mapper;

import org.mapstruct.*;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitFileEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.model.service.dto.file.FileMinDTO;
import pl.com.tt.flex.server.service.dictionary.localizationType.mapper.LocalizationTypeMapper;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.mapper.SchedulingUnitTypeMapper;
import pl.com.tt.flex.server.service.fsp.mapper.FspMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDropdownSelectDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.util.DictionaryUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link SchedulingUnitEntity} and its DTO {@link SchedulingUnitDTO}.
 */
@Mapper(componentModel = "spring", uses = {ProductMapper.class, FspMapper.class, SchedulingUnitFileMapper.class, SchedulingUnitTypeMapper.class, UnitMapper.class, LocalizationTypeMapper.class})
public interface SchedulingUnitMapper extends EntityMapper<SchedulingUnitDTO, SchedulingUnitEntity> {

    @Mapping(source = "bsp", target = "bsp")
    @Mapping(target = "files", ignore = true)
    @Mapping(source = "files", target = "filesMinimal", qualifiedByName = "filesToMinimal")
    @Mapping(source = "schedulingUnitType", target = "schedulingUnitType", qualifiedByName = "prepareSchedulingUnitType")
    @Mapping(source = "units", target = "units")
    SchedulingUnitDTO toDto(SchedulingUnitEntity schedulingUnitEntity);

    @Mapping(target = "units", ignore = true)
    @Mapping(source = "bsp.id", target = "bsp")
    @Mapping(source = "schedulingUnitType.id", target = "schedulingUnitType")
    SchedulingUnitEntity toEntity(SchedulingUnitDTO schedulingUnitDTO);

    List<SchedulingUnitMinDTO> toMinDto(List<SchedulingUnitEntity> entityList);

    @Mapping(source = "schedulingUnitType", target = "schedulingUnitType", qualifiedByName = "prepareSchedulingUnitType")
    @Mapping(source = "bsp", target = "bsp")
    @Mapping(source = "units", target = "ders", qualifiedByName = "unitEntityToMinDerDto")
    SchedulingUnitMinDTO toMinDto(SchedulingUnitEntity entity);

    List<SchedulingUnitDropdownSelectDTO> toDropdownSelectDto(List<SchedulingUnitEntity> entityList);

    @Mapping(source = "schedulingUnitType", target = "schedulingUnitType", qualifiedByName = "prepareSchedulingUnitType")
    @Mapping(source = "units", target = "ders", qualifiedByName = "unitEntityToMinDerDto")
    SchedulingUnitDropdownSelectDTO toDropdownSelectDto(SchedulingUnitEntity entity);

    @Named("couplingPointsToIds")
    default List<Long> couplingPointsToIds(Set<LocalizationTypeEntity> couplingPoints) {
        return couplingPoints.stream().map(LocalizationTypeEntity::getId).collect(Collectors.toList());
    }

    @Named("filesToMinimal")
    default List<FileMinDTO> filesToMinimal(Set<SchedulingUnitFileEntity> files) {
        return files.stream().map(fileEntity -> new FileMinDTO(fileEntity.getId(), fileEntity.getFileName())).sorted(Comparator.comparing(FileMinDTO::getFileId))
            .collect(Collectors.toList());
    }

    @Named("prepareSchedulingUnitType")
    default DictionaryDTO toDictionary(SchedulingUnitTypeEntity schedulingUnitTypeEntity) {
        if (Objects.isNull(schedulingUnitTypeEntity)) {
            return null;
        }
        return DictionaryUtils.getDictionaryDTO(schedulingUnitTypeEntity);
    }

    @AfterMapping
    default void linkFiles(@MappingTarget SchedulingUnitEntity schedulingUnitEntity) {
        schedulingUnitEntity.getFiles().forEach(schedulingUnitFileEntity -> schedulingUnitFileEntity.setSchedulingUnit(schedulingUnitEntity));
    }

    default SchedulingUnitEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        SchedulingUnitEntity schedulingUnitEntity = new SchedulingUnitEntity();
        schedulingUnitEntity.setId(id);
        return schedulingUnitEntity;
    }
}
