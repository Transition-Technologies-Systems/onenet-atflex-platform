package pl.com.tt.flex.server.service.potential.mapper;


import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.*;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.potential.FlexPotentialFileEntity;
import pl.com.tt.flex.server.service.fsp.mapper.FspMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.model.service.dto.file.FileMinDTO;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.domain.unit.UnitEntity;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

/**
 * Mapper for the entity {@link FlexPotentialEntity} and its DTO {@link FlexPotentialDTO}.
 */
@Mapper(componentModel = "spring", uses = {ProductMapper.class, UnitMapper.class, FspMapper.class, FlexPotentialFileMapper.class})
public interface FlexPotentialMapper extends EntityMapper<FlexPotentialDTO, FlexPotentialEntity> {

    @Mapping(source = "product", target = "product")
    @Mapping(source = "units", target = "units")
    @Mapping(source = "units", target = "unitIds", qualifiedByName = "unitsToIds")
    @Mapping(source = "fsp", target = "fsp")
    @Mapping(source = "files", target = "filesMinimal", qualifiedByName = "filesToMinimal")
    @Mapping(target = "files", ignore = true)
    FlexPotentialDTO toDto(FlexPotentialEntity flexPotentialEntity);

    @Mapping(source = "product.id", target = "product")
    @Mapping(source = "unitIds", target = "units")
    @Mapping(source = "fsp.id", target = "fsp")
    @Mapping(source = ".", target = "registered", qualifiedByName = "isFPRegistered")
    FlexPotentialEntity toEntity(FlexPotentialDTO flexPotentialDTO);

    @Named("unitsToEntityTemp")
    default Set<UnitEntity> unitsToEntityTemp(FlexPotentialDTO dto) {
        Set<UnitEntity> result = new HashSet<>();
        if (CollectionUtils.isNotEmpty(dto.getUnitIds())) {
            dto.getUnitIds().forEach(unitId -> {
                UnitEntity unitEntity = new UnitEntity();
                unitEntity.setId(unitId);
                result.add(unitEntity);
            });
        } else {
            UnitEntity unitEntity = new UnitEntity();
            unitEntity.setId(dto.getUnit().getId());
            result.add(unitEntity);
        }
        return result;
    }

    @Named("isFPRegistered")
    default boolean isFPRegistered(FlexPotentialDTO dto) {
        return dto.isProductPrequalification() && dto.isStaticGridPrequalification();
    }

    @Named("filesToMinimal")
    default List<FileMinDTO> filesToMinimal(Set<FlexPotentialFileEntity> files) {
        return emptyIfNull(files).stream().map(fileEntity -> new FileMinDTO(fileEntity.getId(), fileEntity.getFileName())).sorted(Comparator.comparing(FileMinDTO::getFileId))
            .collect(Collectors.toList());
    }

    @AfterMapping
    default void linkFiles(@MappingTarget FlexPotentialEntity flexPotentialEntity) {
        flexPotentialEntity.getFiles().forEach(flexPotentialFileEntity -> flexPotentialFileEntity.setFlexPotential(flexPotentialEntity));
    }

    default FlexPotentialEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        FlexPotentialEntity flexPotentialEntity = new FlexPotentialEntity();
        flexPotentialEntity.setId(id);
        return flexPotentialEntity;
    }
}
