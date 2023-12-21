package pl.com.tt.flex.server.service.subportfolio.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioFileEntity;
import pl.com.tt.flex.server.service.mapper.FileEntityMapper;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioFileDTO;

/**
 * Mapper for the entity {@link SubportfolioFileEntity} and its DTO {@link SubportfolioFileDTO}.
 */
@Mapper(componentModel = "spring", uses = {SubportfolioMapper.class})
public interface SubportfolioFileMapper extends FileEntityMapper<SubportfolioFileDTO, SubportfolioFileEntity> {

    @Mapping(source = "subportfolio.id", target = "subportfolioId")
    SubportfolioFileDTO toDto(SubportfolioFileEntity subportfolioFileEntity);

    @Mapping(source = "subportfolioId", target = "subportfolio")
    @Mapping(source = "fileDTO", target = "fileExtension", qualifiedByName = "fileExtensionDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileName", qualifiedByName = "fileNameDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileZipData", qualifiedByName = "fileBase64DataDTOToEntity")
    SubportfolioFileEntity toEntity(SubportfolioFileDTO subportfolioFileDTO);
}
