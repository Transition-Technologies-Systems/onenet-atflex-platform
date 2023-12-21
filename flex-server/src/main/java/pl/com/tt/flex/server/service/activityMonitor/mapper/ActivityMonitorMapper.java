package pl.com.tt.flex.server.service.activityMonitor.mapper;

import org.mapstruct.Mapper;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityMonitorEntity;
import pl.com.tt.flex.server.service.activityMonitor.dto.ActivityMonitorDTO;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

/**
 * Mapper for the entity {@link ActivityMonitorEntity} and its DTO {@link ActivityMonitorDTO}.
 */
@Mapper(componentModel = "spring")
public interface ActivityMonitorMapper extends EntityMapper<ActivityMonitorDTO, ActivityMonitorEntity> {
}
