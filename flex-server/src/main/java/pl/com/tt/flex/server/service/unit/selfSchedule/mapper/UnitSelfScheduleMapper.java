package pl.com.tt.flex.server.service.unit.selfSchedule.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.service.mapper.FileEntityMapper;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleDTO;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.util.DateUtil.sortedHourNumbers;

/**
 * Mapper for the entity {@link UnitSelfScheduleEntity} and its DTO {@link UnitSelfScheduleDTO}.
 */
@Mapper(componentModel = "spring", uses = {UnitMapper.class})
public interface UnitSelfScheduleMapper extends FileEntityMapper<UnitSelfScheduleDTO, UnitSelfScheduleEntity> {

    @Mapping(source = "unit.fsp", target = "fsp")
    @Mapping(source = "volumes", target = "volumes", qualifiedByName = "toVolumeEntity")
    UnitSelfScheduleDTO toDto(UnitSelfScheduleEntity selfScheduleDerEntity);

    @Mapping(source = "unit.id", target = "unit")
    @Mapping(source = "volumes", target = "volumes", qualifiedByName = "toVolumeDto")
    UnitSelfScheduleEntity toEntity(UnitSelfScheduleDTO unitSelfScheduleDTO);

    default UnitSelfScheduleEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        UnitSelfScheduleEntity unitSelfScheduleEntity = new UnitSelfScheduleEntity();
        unitSelfScheduleEntity.setId(id);
        return unitSelfScheduleEntity;
    }

    @Named("toVolumeEntity")
    default Map<String, BigDecimal> toVolumeEntity(List<MinimalDTO<String, BigDecimal>> volumes) {
        return volumes.stream().collect(Collectors.toMap(MinimalDTO::getId, MinimalDTO::getValue));
    }

    @Named("toVolumeDto")
    default List<MinimalDTO<String, BigDecimal>> toVolumeDto(Map<String, BigDecimal> volumes) {
        return volumes.entrySet().stream().map(volume -> new MinimalDTO<>(volume.getKey(), volume.getValue()))
            .sorted(Comparator.comparingInt(c -> sortedHourNumbers.indexOf(c.getId())))
            .collect(Collectors.toList());
    }
}
