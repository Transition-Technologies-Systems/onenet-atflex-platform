package pl.com.tt.flex.server.service.kpi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity_;
import pl.com.tt.flex.server.domain.kpi.KpiView;
import pl.com.tt.flex.server.domain.kpi.KpiView_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.kpi.KpiViewRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.kpi.dto.KpiCriteria;
import pl.com.tt.flex.server.service.kpi.mapper.KpiViewMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

import java.util.List;

/**
 * Service for executing complex queries for {@link KpiView} entities in the database.
 * The main input is a {@link KpiCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link KpiDTO} or a {@link Page} of {@link KpiDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class KpiQueryService extends AbstractQueryServiceImpl<KpiView, KpiDTO, Long, KpiCriteria> {

    private final KpiViewRepository repository;

    private final KpiViewMapper mapper;

    public KpiQueryService(final KpiViewRepository repository, final KpiViewMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Return a {@link List} of {@link KpiDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param sort     The sorting parameters.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<KpiDTO> findByCriteria(KpiCriteria criteria, Sort sort) {
        log.debug("find by criteria : {}, sort: {}", criteria, sort);
        final Specification<KpiView> specification = createSpecification(criteria);
        return mapper.toDto(repository.findAll(specification, sort));
    }

    /**
     * Function to convert {@link KpiCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<KpiView> createSpecification(KpiCriteria criteria) {
        Specification<KpiView> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), KpiView_.id));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), KpiView_.type));
            }
            if (criteria.getDateFrom() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getDateFrom());
                specification = specification.and(buildRangeSpecification(criteria.getDateFrom(), KpiView_.dateFrom));
            }
            if (criteria.getDateTo() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getDateTo());
                CriteriaUtils.setLastHourDayIfLessThanOrEqualsFilter(criteria.getDateTo());
                specification = specification.and(buildRangeSpecification(criteria.getDateTo(), KpiView_.dateTo));
            }
            if (criteria.getCreatedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getCreatedDate());
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), AbstractAuditingEntity_.createdDate));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), AbstractAuditingEntity_.createdBy));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return KpiView_.ID;
    }

    @Override
    public AbstractJpaRepository<KpiView, Long> getRepository() {
        return this.repository;
    }

    @Override
    public EntityMapper<KpiDTO, KpiView> getMapper() {
        return this.mapper;
    }
}
