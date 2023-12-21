package pl.com.tt.flex.server.service.activityMonitor.dto;

import lombok.*;
import pl.com.tt.flex.server.config.AppModuleName;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;

import java.io.Serializable;
import java.time.Instant;

/**
 * A DTO for the {@link ActivityMonitorDTO} entity.
 */
@Getter
@Setter
@Builder
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class ActivityMonitorDTO implements Serializable {

    private Long id;
    private Instant createdDate;
    private ActivityEvent event;
    private String objectId;
    private String login;
    private String errorCode;
    private String httpRequestUriPath;
    private String httpResponseStatus;
    private AppModuleName appModuleName;
}
