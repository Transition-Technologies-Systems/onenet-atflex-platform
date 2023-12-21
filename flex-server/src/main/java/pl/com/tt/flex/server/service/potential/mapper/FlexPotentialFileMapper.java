package pl.com.tt.flex.server.service.potential.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.com.tt.flex.server.domain.potential.FlexPotentialFileEntity;
import pl.com.tt.flex.server.service.mapper.FileEntityMapper;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialFileDTO;

/**
 * Mapper for the entity {@link FlexPotentialFileEntity} and its DTO {@link FlexPotentialFileDTO}.
 */
@Mapper(componentModel = "spring", uses = {FlexPotentialMapper.class})
public interface FlexPotentialFileMapper extends FileEntityMapper<FlexPotentialFileDTO, FlexPotentialFileEntity> {

    @Mapping(source = "flexPotential.id", target = "flexPotentialId")
    FlexPotentialFileDTO toDto(FlexPotentialFileEntity flexPotentialFileEntity);

    @Mapping(source = "flexPotentialId", target = "flexPotential")
    @Mapping(source = "fileDTO", target = "fileExtension", qualifiedByName = "fileExtensionDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileName", qualifiedByName = "fileNameDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileZipData", qualifiedByName = "fileBase64DataDTOToEntity")
    FlexPotentialFileEntity toEntity(FlexPotentialFileDTO flexPotentialFileDTO);
}
