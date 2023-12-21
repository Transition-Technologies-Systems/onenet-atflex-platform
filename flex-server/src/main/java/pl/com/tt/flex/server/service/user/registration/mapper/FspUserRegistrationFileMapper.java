package pl.com.tt.flex.server.service.user.registration.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationFileEntity;
import pl.com.tt.flex.server.service.mapper.FileEntityMapper;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationFileDTO;

/**
 * Mapper for the entity {@link FspUserRegistrationFileEntity} and its DTO {@link FspUserRegistrationFileDTO}.
 */
@Mapper(componentModel = "spring", uses = {FspUserRegistrationCommentMapper.class})
public interface FspUserRegistrationFileMapper extends FileEntityMapper<FspUserRegistrationFileDTO, FspUserRegistrationFileEntity> {

    @Mapping(source = "comment.id", target = "fspUserRegistrationCommentId")
    FspUserRegistrationFileDTO toDto(FspUserRegistrationFileEntity fspUserRegistrationFileEntity);

    @Mapping(source = "fspUserRegistrationCommentId", target = "comment")
    @Mapping(source = "fileDTO", target = "fileExtension", qualifiedByName = "fileExtensionDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileName", qualifiedByName = "fileNameDTOToEntity")
    @Mapping(source = "fileDTO", target = "fileZipData", qualifiedByName = "fileBase64DataDTOToEntity")
    FspUserRegistrationFileEntity toEntity(FspUserRegistrationFileDTO fspUserRegistrationFileDTO);

    default FspUserRegistrationFileEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        FspUserRegistrationFileEntity fspUserRegistrationFileEntity = new FspUserRegistrationFileEntity();
        fspUserRegistrationFileEntity.setId(id);
        return fspUserRegistrationFileEntity;
    }
}
