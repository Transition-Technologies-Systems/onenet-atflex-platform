package pl.com.tt.flex.server.service.notification.dto;

import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.notification.NotificationEntity;
import pl.com.tt.flex.model.service.dto.MinimalDTO;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * A DTO for the {@link NotificationEntity} entity.
 */
@Getter
@Setter
@EqualsAndHashCode
public class NotificationDTO implements Serializable {

    private Long id;

    private NotificationEvent eventType;

    private Instant createdDate;

    private Map<NotificationParam, NotificationParamValue> params = Maps.newHashMap();

    private boolean read;

    private List<MinimalDTO<Long, String>> users;
}
