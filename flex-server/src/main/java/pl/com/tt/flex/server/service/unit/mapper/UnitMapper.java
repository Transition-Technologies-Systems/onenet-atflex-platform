package pl.com.tt.flex.server.service.unit.mapper;


import org.mapstruct.*;
import pl.com.tt.flex.model.service.dto.der.DerMinDTO;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.service.dictionary.derType.mapper.DerTypeMapper;
import pl.com.tt.flex.server.service.dictionary.localizationType.mapper.LocalizationTypeMapper;
import pl.com.tt.flex.server.service.fsp.mapper.FspMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.user.mapper.UserMapper;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * Mapper for the entity {@link UnitEntity} and its DTO {@link UnitDTO}.
 */
@Mapper(componentModel = "spring", uses = {FspMapper.class, UnitGeoLocationMapper.class, UserMapper.class, DerTypeMapper.class, SchedulingUnitMapper.class, LocalizationTypeMapper.class})
public interface UnitMapper extends EntityMapper<UnitDTO, UnitEntity> {

    @Mapping(source = "fsp.id", target = "fspId")
    @Mapping(source = "fsp", target = "fsp")
    @Mapping(target = "geoLocations", ignore = true)
    UnitDTO toDto(UnitEntity unitEntity);

    @Mapping(source = "unitDTO", target = "fsp")
    @Mapping(source = "derTypeReception", target = "derTypeReception")
    @Mapping(source = "derTypeEnergyStorage", target = "derTypeEnergyStorage")
    @Mapping(source = "derTypeGeneration", target = "derTypeGeneration")
    UnitEntity toEntity(UnitDTO unitDTO);

    // https://stackoverflow.com/questions/48967181/spring-data-and-jpa-one-to-many-with-mapstruct
    @AfterMapping
    default void linkGeoLocations(@MappingTarget UnitEntity unitEntity) {
        if (unitEntity.getGeoLocations() != null) {
            unitEntity.getGeoLocations().forEach(geoLocationEntity -> geoLocationEntity.setUnit(unitEntity));
        }
    }

    default UnitEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        UnitEntity unitEntity = new UnitEntity();
        unitEntity.setId(id);
        return unitEntity;
    }

    List<UnitMinDTO> toMinDto(List<UnitEntity> entityList);

    default UnitMinDTO toMinDto(UnitEntity entity) {
        if (entity == null) {
            return null;
        }
        UnitMinDTO unitMinDTO = new UnitMinDTO();
        unitMinDTO.setId(entity.getId());
        unitMinDTO.setName(entity.getName());
        unitMinDTO.setSder(entity.isSder());
        unitMinDTO.setSourcePower(entity.getSourcePower());
        if (nonNull(entity.getFsp())) {
            unitMinDTO.setFspId(entity.getFsp().getId());
            unitMinDTO.setFspCompanyName(entity.getFsp().getCompanyName());
        }
        unitMinDTO.setConnectionPower(entity.getConnectionPower());
        unitMinDTO.setPMin(entity.getPMin());
        unitMinDTO.setUnitDirectionOfDeviation(entity.getDirectionOfDeviation());
        if (Objects.nonNull(entity.getSubportfolio())) {
            unitMinDTO.setSubportfolioName(entity.getSubportfolio().getName());
        }
        if (Objects.nonNull(entity.getSchedulingUnit())) {
            unitMinDTO.setSchedulingUnitId(entity.getSchedulingUnit().getId());
        }
        return unitMinDTO;
    }

    @Named("unitEntityToMinDerDto")
    default DerMinDTO toDerMinDto(UnitEntity entity) {
        if (entity == null) {
            return null;
        }
        DerMinDTO derMinDTO = new DerMinDTO();
        derMinDTO.setId(entity.getId());
        derMinDTO.setName(entity.getName());
        derMinDTO.setSourcePower(entity.getSourcePower());
        derMinDTO.setPMin(entity.getPMin());

        return derMinDTO;
    }

    @Named("unitsToIds")
    default List<Long> unitsToIds(Set<UnitEntity> units) {
        return units.stream().map(UnitEntity::getId).collect(Collectors.toList());
    }
}
