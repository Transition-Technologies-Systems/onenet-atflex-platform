package pl.com.tt.flex.server.service.activityMonitor;

import pl.com.tt.flex.server.config.AppModuleName;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityMonitorEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.server.service.activityMonitor.dto.ActivityMonitorDTO;

/**
 * Service Interface for managing {@link ActivityMonitorEntity}.
 */
public interface ActivityMonitorService extends AbstractService<ActivityMonitorEntity, ActivityMonitorDTO, Long> {

    void saveEvent(ActivityEvent activityEvent, String objectId);

    void saveErrorEvent(String errorMessage, String errorKey, ActivityEvent activityEvent, String objectId, AppModuleName appName, String requestUriPath, String responseHttpStatus);
}
