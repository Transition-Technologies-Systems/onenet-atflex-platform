package pl.com.tt.flex.server.service.activityMonitor;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.config.AppModuleName;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityMonitorEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.activityMonitor.ActivityMonitorRepository;
import pl.com.tt.flex.server.security.SecurityUtils;
import pl.com.tt.flex.server.service.activityMonitor.dto.ActivityMonitorDTO;
import pl.com.tt.flex.server.service.activityMonitor.mapper.ActivityMonitorMapper;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

/**
 * Service Implementation for managing {@link ActivityMonitorEntity}.
 */
@Slf4j
@Service
@Transactional
@AllArgsConstructor
public class ActivityMonitorServiceImpl extends AbstractServiceImpl<ActivityMonitorEntity, ActivityMonitorDTO, Long> implements ActivityMonitorService {

    private final ActivityMonitorRepository activityMonitorRepository;
    private final ActivityMonitorMapper activityMonitorMapper;

    @Override
    @Transactional
    public void saveEvent(ActivityEvent activityEvent, String objectId) {
        ActivityMonitorEntity activityMonitorEntity = new ActivityMonitorEntity();
        activityMonitorEntity.setLogin(SecurityUtils.getCurrentUserLogin().get());
        activityMonitorEntity.setEvent(activityEvent);
        activityMonitorEntity.setObjectId(objectId);
        activityMonitorRepository.save(activityMonitorEntity);
    }

    @Override
    @Transactional
    public void saveErrorEvent(String errorMessage, String errorKey, ActivityEvent activityEvent, String objectId, AppModuleName appName,
        String requestUriPath, String responseHttpStatus) {
        ActivityMonitorEntity activityMonitorEntity = new ActivityMonitorEntity();
        activityMonitorEntity.setLogin(SecurityUtils.getCurrentUserLogin().get());
        activityMonitorEntity.setEvent(activityEvent);
        activityMonitorEntity.setObjectId(objectId);
        activityMonitorEntity.setErrorMessage(errorMessage);
        activityMonitorEntity.setErrorCode(errorKey);
        activityMonitorEntity.setHttpRequestUriPath(requestUriPath);
        activityMonitorEntity.setHttpResponseStatus(responseHttpStatus);
        activityMonitorEntity.setAppModuleName(appName);
        activityMonitorRepository.save(activityMonitorEntity);
    }

    @Override
    public AbstractJpaRepository<ActivityMonitorEntity, Long> getRepository() {
        return activityMonitorRepository;
    }

    @Override
    public EntityMapper<ActivityMonitorDTO, ActivityMonitorEntity> getMapper() {
        return activityMonitorMapper;
    }
}
