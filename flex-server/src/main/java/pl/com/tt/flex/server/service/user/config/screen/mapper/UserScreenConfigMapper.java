package pl.com.tt.flex.server.service.user.config.screen.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import pl.com.tt.flex.server.domain.user.config.screen.ScreenColumnEntity;
import pl.com.tt.flex.server.domain.user.config.screen.UserScreenConfigEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.config.screen.dto.ScreenColumnDTO;
import pl.com.tt.flex.server.service.user.config.screen.dto.UserScreenConfigDTO;

@Mapper(componentModel = "spring")
public interface UserScreenConfigMapper extends EntityMapper<UserScreenConfigDTO, UserScreenConfigEntity> {

    @Override
    @Mapping(target = "user.id", source = "userId")
    UserScreenConfigEntity toEntity(UserScreenConfigDTO dto);

    @Override
    @Mapping(target = "userId", source = "user.id")
    UserScreenConfigDTO toDto(UserScreenConfigEntity entity);

    ScreenColumnEntity dtoToEntity(ScreenColumnDTO dto);

    ScreenColumnDTO entityToDTO(ScreenColumnEntity entity);

    @AfterMapping
    default void fillBackwardRefs(@MappingTarget UserScreenConfigEntity userScreenConfig) {
        for (ScreenColumnEntity screenColumn : userScreenConfig.getScreenColumns()) {
            screenColumn.setUserScreenConfig(userScreenConfig);
        }
    }
}
