package pl.com.tt.flex.server.service.user.registration.mapper;


import com.google.common.collect.Lists;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationCommentEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationFileEntity;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.mapper.UserMapper;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationCommentDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * Mapper for the entity {@link FspUserRegistrationCommentEntity} and its DTO {@link FspUserRegistrationCommentDTO}.
 */
@Mapper(componentModel = "spring", uses = {FspUserRegistrationMapper.class, UserMapper.class})
public interface FspUserRegistrationCommentMapper extends EntityMapper<FspUserRegistrationCommentDTO, FspUserRegistrationCommentEntity> {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "fspUserRegistration.id", target = "fspUserRegistrationId")
    @Mapping(source = "files", target = "files", qualifiedByName = "filesToMinimal")
    FspUserRegistrationCommentDTO toDto(FspUserRegistrationCommentEntity fspUserRegistrationCommentEntity);

    @Mapping(source = "userId", target = "user")
    @Mapping(source = "fspUserRegistrationId", target = "fspUserRegistration")
    FspUserRegistrationCommentEntity toEntity(FspUserRegistrationCommentDTO fspUserRegistrationCommentDTO);

    default FspUserRegistrationCommentEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        FspUserRegistrationCommentEntity fspUserRegistrationCommentEntity = new FspUserRegistrationCommentEntity();
        fspUserRegistrationCommentEntity.setId(id);
        return fspUserRegistrationCommentEntity;
    }

    @Named("filesToMinimal")
    default List<MinimalDTO<Long, String>> filesToMinimal(Set<FspUserRegistrationFileEntity> files) {
        if (nonNull(files)) {
            return files.stream().map(fileEntity -> new MinimalDTO<>(fileEntity.getId(), fileEntity.getFileName())).sorted(Comparator.comparing(MinimalDTO::getId))
                .collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }
}
