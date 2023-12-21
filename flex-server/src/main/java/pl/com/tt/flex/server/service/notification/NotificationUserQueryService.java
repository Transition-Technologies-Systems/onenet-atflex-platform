package pl.com.tt.flex.server.service.notification;

import io.github.jhipster.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.notification.NotificationEntity_;
import pl.com.tt.flex.server.domain.notification.NotificationUserEntity;
import pl.com.tt.flex.server.domain.notification.NotificationUserEntity_;
import pl.com.tt.flex.server.domain.user.UserEntity_;
import pl.com.tt.flex.server.repository.NotificationUserRepository;
import pl.com.tt.flex.server.service.notification.dto.NotificationCriteria;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.notification.mapper.NotificationUserMapper;

import javax.persistence.criteria.JoinType;
import java.util.List;

import static pl.com.tt.flex.server.service.common.QueryServiceUtil.setDefaultOrder;

/**
 * Service for executing complex queries for {@link NotificationUserEntity} entities in the database.
 * The main input is a {@link NotificationCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link NotificationDTO} or a {@link Page} of {@link NotificationDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class NotificationUserQueryService extends QueryService<NotificationUserEntity> {

    private final NotificationUserRepository notificationRepository;

    private final NotificationUserMapper notificationMapper;

    public NotificationUserQueryService(NotificationUserRepository notificationRepository, NotificationUserMapper notificationMapper) {
        this.notificationRepository = notificationRepository;
        this.notificationMapper = notificationMapper;
    }

    /**
     * Return a {@link List} of {@link NotificationDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<NotificationDTO> findByCriteria(NotificationCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<NotificationUserEntity> specification = createSpecification(criteria);
        return notificationMapper.toDto(notificationRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link NotificationDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<NotificationDTO> findByCriteria(NotificationCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<NotificationUserEntity> specification = createSpecification(criteria);
        page = setDefaultOrder(page, NotificationUserEntity_.ID);
        return notificationRepository.findAll(specification, page)
            .map(notificationMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(NotificationCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<NotificationUserEntity> specification = createSpecification(criteria);
        return notificationRepository.count(specification);
    }

    /**
     * Function to convert {@link NotificationCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<NotificationUserEntity> createSpecification(NotificationCriteria criteria) {
        Specification<NotificationUserEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), NotificationUserEntity_.id));
            }
            if (criteria.getEventType() != null) {
                specification = specification.and(buildSpecification(criteria.getEventType(),
                    root -> root.join(NotificationUserEntity_.notification, JoinType.LEFT).get(NotificationEntity_.eventType)));
            }
            if (criteria.getCreatedDate() != null) {
                specification = specification.and(buildSpecification(criteria.getCreatedDate(),
                    root -> root.join(NotificationUserEntity_.notification, JoinType.LEFT).get(NotificationEntity_.createdDate)));
            }
            if (criteria.getNotificationUserId() != null) {
                specification = specification.and(buildSpecification(criteria.getNotificationUserId(),
                    root -> root.join(NotificationUserEntity_.user, JoinType.LEFT).get(UserEntity_.id)));
            }
        }
        return specification;
    }
}
