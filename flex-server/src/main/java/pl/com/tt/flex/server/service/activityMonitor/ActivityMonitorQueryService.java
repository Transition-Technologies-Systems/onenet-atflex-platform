package pl.com.tt.flex.server.service.activityMonitor;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityMonitorEntity;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityMonitorEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.activityMonitor.ActivityMonitorRepository;
import pl.com.tt.flex.server.service.activityMonitor.dto.ActivityMonitorCriteria;
import pl.com.tt.flex.server.service.activityMonitor.dto.ActivityMonitorDTO;
import pl.com.tt.flex.server.service.activityMonitor.mapper.ActivityMonitorMapper;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

/**
 * Service for executing complex queries for {@link ActivityMonitorEntity} entities in the database.
 * The main input is a {@link ActivityMonitorCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ActivityMonitorDTO} or a {@link Page} of {@link ActivityMonitorDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ActivityMonitorQueryService extends AbstractQueryServiceImpl<ActivityMonitorEntity, ActivityMonitorDTO, Long, ActivityMonitorCriteria> {

    private final ActivityMonitorRepository activityMonitorRepository;

    private final ActivityMonitorMapper activityMonitorMapper;

    public ActivityMonitorQueryService(final ActivityMonitorRepository activityMonitorRepository, final ActivityMonitorMapper activityMonitorMapper) {
        this.activityMonitorRepository = activityMonitorRepository;
        this.activityMonitorMapper = activityMonitorMapper;
    }

    /**
     * Function to convert {@link ActivityMonitorCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ActivityMonitorEntity> createSpecification(ActivityMonitorCriteria criteria) {
        Specification<ActivityMonitorEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), ActivityMonitorEntity_.id));
            }
            if (criteria.getCreatedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getCreatedDate());
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), ActivityMonitorEntity_.createdDate));
            }
            if (criteria.getEvent() != null) {
                specification = specification.and(buildSpecification(criteria.getEvent(), ActivityMonitorEntity_.event));
            }
            if (criteria.getLogin() != null) {
                specification = specification.and(buildSpecification(criteria.getLogin(), ActivityMonitorEntity_.login));
            }
            if (criteria.getHttpRequestUriPath() != null) {
                specification = specification.and(buildSpecification(criteria.getHttpRequestUriPath(), ActivityMonitorEntity_.httpRequestUriPath));
            }
            if (criteria.getHttpResponseStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getHttpRequestUriPath(), ActivityMonitorEntity_.httpResponseStatus));
            }
            if (criteria.getAppModuleName() != null) {
                specification = specification.and(buildSpecification(criteria.getAppModuleName(), ActivityMonitorEntity_.appModuleName));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return ActivityMonitorEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<ActivityMonitorEntity, Long> getRepository() {
        return this.activityMonitorRepository;
    }

    @Override
    public EntityMapper<ActivityMonitorDTO, ActivityMonitorEntity> getMapper() {
        return this.activityMonitorMapper;
    }
}

