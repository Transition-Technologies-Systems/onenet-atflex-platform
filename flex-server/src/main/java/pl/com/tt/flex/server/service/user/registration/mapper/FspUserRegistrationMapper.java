package pl.com.tt.flex.server.service.user.registration.mapper;


import com.google.common.collect.Lists;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationCommentEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationDTO;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationFormDTO;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 * Mapper for the entity {@link FspUserRegistrationEntity} and its DTO {@link FspUserRegistrationDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface FspUserRegistrationMapper extends EntityMapper<FspUserRegistrationDTO, FspUserRegistrationEntity> {

    @Mapping(source = "fspUser.id", target = "fspUserId")
    @Mapping(source = "comments", target = "files", qualifiedByName = "filesToMinimal")
    FspUserRegistrationDTO toDto(FspUserRegistrationEntity fspUserRegistrationEntity);

    FspUserRegistrationFormDTO toDtoOnlyWithFormData(FspUserRegistrationDTO fspUserRegistrationDTO);

    @Mapping(target = "comments", ignore = true)
    FspUserRegistrationEntity toEntity(FspUserRegistrationDTO fspUserRegistrationDTO);

    default FspUserRegistrationEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        FspUserRegistrationEntity fspUserRegistrationEntity = new FspUserRegistrationEntity();
        fspUserRegistrationEntity.setId(id);
        return fspUserRegistrationEntity;
    }

    @Named("filesToMinimal")
    default List<MinimalDTO<Long, String>> filesToMinimal(Set<FspUserRegistrationCommentEntity> comments) {
        if (nonNull(comments)) {
            List<MinimalDTO<Long, String>> filesMinimal = Lists.newArrayList();
            comments.forEach(comment -> filesMinimal.addAll(comment.getFiles().stream().map(file -> new MinimalDTO<>(file.getId(), file.getFileName()))
                .sorted(Comparator.comparing(MinimalDTO::getId)).collect(Collectors.toList())));
            return filesMinimal;
        }
        return Lists.newArrayList();
    }
}
