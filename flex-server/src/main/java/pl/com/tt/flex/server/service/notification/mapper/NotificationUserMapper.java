package pl.com.tt.flex.server.service.notification.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.notification.NotificationEntity;
import pl.com.tt.flex.server.domain.notification.NotificationParamEntity;
import pl.com.tt.flex.server.domain.notification.NotificationUserEntity;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for the entity {@link NotificationUserEntity} and its DTO {@link NotificationDTO}.
 */
@Mapper(componentModel = "spring", uses = {})
public interface NotificationUserMapper extends EntityMapper<NotificationDTO, NotificationUserEntity> {

    @Mapping(target = "notification", ignore = true)
    NotificationUserEntity toEntity(NotificationDTO notificationDTO);


    @Mapping(target = "createdDate", source = "notification.createdDate")
    @Mapping(target = "eventType", source = "notification.eventType")
    @Mapping(target = "params", source = "notification.notificationParams", qualifiedByName = "paramsToMap")
    @Mapping(target = "users", ignore = true)
    NotificationDTO toDto(NotificationUserEntity notificationUserEntity);

    @Named("paramsToMap")
    default Map<NotificationParam, NotificationParamValue> paramsToMap(Set<NotificationParamEntity> params) {
        return params.stream()
            .collect(Collectors.toMap(p -> NotificationParam.valueOf(p.getName()),
                p -> NotificationParamValue.ParamValueBuilder.create().addParam(p.getValue()).addObject(p.getObject()).build()));
    }

    default NotificationEntity fromId(Long id) {
        if (id == null) {
            return null;
        }
        NotificationEntity notificationEntity = new NotificationEntity();
        notificationEntity.setId(id);
        return notificationEntity;
    }
}
