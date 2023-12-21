package pl.com.tt.flex.server.service.subportfolio.mapper;

import org.mapstruct.*;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioFileEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.model.service.dto.file.FileMinDTO;
import pl.com.tt.flex.server.service.dictionary.localizationType.mapper.LocalizationTypeMapper;
import pl.com.tt.flex.server.service.fsp.mapper.FspMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioMinDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link SubportfolioEntity} and its DTO {@link SubportfolioDTO}.
 */
@Mapper(componentModel = "spring", uses = {ProductMapper.class, FspMapper.class, SubportfolioFileMapper.class, UnitMapper.class, LocalizationTypeMapper.class})
public interface SubportfolioMapper extends EntityMapper<SubportfolioDTO, SubportfolioEntity> {

    @Mapping(source = "fspa", target = "fspa")
    @Mapping(source = "fspa.id", target = "fspId")
    @Mapping(source = "units", target = "units")
    @Mapping(source = "units", target = "unitIds", qualifiedByName = "unitsToIds")
    @Mapping(target = "files", ignore = true)
    @Mapping(source = "files", target = "filesMinimal", qualifiedByName = "filesToMinimal")
    SubportfolioDTO toDto(SubportfolioEntity subportfolioEntity);

    @Mapping(source = "fspId", target = "fspa")
    @Mapping(source = "unitIds", target = "units")
    SubportfolioEntity toEntity(SubportfolioDTO subportfolioDTO);

    @Named("filesToMinimal")
    default List<FileMinDTO> filesToMinimal(Set<SubportfolioFileEntity> files) {
        return files.stream().map(fileEntity -> new FileMinDTO(fileEntity.getId(), fileEntity.getFileName())).sorted(Comparator.comparing(FileMinDTO::getFileId))
            .collect(Collectors.toList());
    }

    /**
     * Combined power of ders is the sum of the connection power to the subportfolio
     */
    @AfterMapping
    default void calculateCombinedPower(@MappingTarget SubportfolioDTO subportfolioDTO, SubportfolioEntity subportfolioEntity) {
       BigDecimal combinedPowerOfDers = subportfolioEntity.getUnits().stream().map(UnitEntity::getConnectionPower).reduce(BigDecimal.ZERO, BigDecimal::add);
       subportfolioDTO.setCombinedPowerOfDers(combinedPowerOfDers);
    }

    @AfterMapping
    default void linkFiles(@MappingTarget SubportfolioEntity subportfolioEntity) {
        subportfolioEntity.getFiles().forEach(subportfolioFileEntity -> subportfolioFileEntity.setSubportfolio(subportfolioEntity));
    }

    default SubportfolioEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        SubportfolioEntity subportfolioEntity = new SubportfolioEntity();
        subportfolioEntity.setId(id);
        return subportfolioEntity;
    }

    default SubportfolioMinDTO toMinDto(SubportfolioEntity entity) {
        if (entity == null) {
            return null;
        }
        SubportfolioMinDTO subMinDTO = new SubportfolioMinDTO();
        subMinDTO.setId(entity.getId());
        subMinDTO.setName(entity.getName());
        return subMinDTO;
    }
}
