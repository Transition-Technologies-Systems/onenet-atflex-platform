package pl.com.tt.flex.server.service.schedulingUnit.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitFileEntity;
import pl.com.tt.flex.server.service.mapper.FileEntityMapper;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitFileDTO;

/**
 * Mapper for the entity {@link SchedulingUnitFileEntity} and its DTO {@link SchedulingUnitFileDTO}.
 */
@Mapper(componentModel = "spring", uses = {SchedulingUnitMapper.class})
public interface SchedulingUnitFileMapper extends FileEntityMapper<SchedulingUnitFileDTO, SchedulingUnitFileEntity> {

    @Mapping(source = "schedulingUnit.id", target = "schedulingUnitId")
    SchedulingUnitFileDTO toDto(SchedulingUnitFileEntity schedulingUnitFileEntity);

    @Mapping(source = "schedulingUnitId", target = "schedulingUnit")
    @Mapping(source = "fileDTO", target = "fileExtension", qualifiedByName = "fileExtensionDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileName", qualifiedByName = "fileNameDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileZipData", qualifiedByName = "fileBase64DataDTOToEntity")
    SchedulingUnitFileEntity toEntity(SchedulingUnitFileDTO schedulingUnitFileDTO);
}
