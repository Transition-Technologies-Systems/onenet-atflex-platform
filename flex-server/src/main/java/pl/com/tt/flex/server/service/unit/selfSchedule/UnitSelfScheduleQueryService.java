package pl.com.tt.flex.server.service.unit.selfSchedule;

import java.time.Instant;
import java.util.List;

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.filter.InstantFilter;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.common.specification.SpecificationCombiner;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity_;
import pl.com.tt.flex.server.domain.fsp.FspEntity_;
import pl.com.tt.flex.server.domain.unit.UnitEntity_;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.unit.selfSchedule.SelfScheduleRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleCriteria;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleDTO;
import pl.com.tt.flex.server.service.unit.selfSchedule.mapper.UnitSelfScheduleMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;


/**
 * Service for executing complex queries for {@link UnitSelfScheduleEntity} entities in the database.
 * The main input is a {@link UnitSelfScheduleCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link UnitSelfScheduleDTO} or a {@link Page} of {@link UnitSelfScheduleDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class UnitSelfScheduleQueryService extends AbstractQueryServiceImpl<UnitSelfScheduleEntity, UnitSelfScheduleDTO, Long, UnitSelfScheduleCriteria> {

    private final SelfScheduleRepository selfScheduleRepository;

    private final UnitSelfScheduleMapper unitSelfScheduleMapper;

    public UnitSelfScheduleQueryService(final SelfScheduleRepository selfScheduleRepository, final UnitSelfScheduleMapper unitSelfScheduleMapper) {
        this.selfScheduleRepository = selfScheduleRepository;
        this.unitSelfScheduleMapper = unitSelfScheduleMapper;
    }

    /**
     * Return a {@link List} of {@link UnitSelfScheduleDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param sort     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<UnitSelfScheduleDTO> findByCriteria(UnitSelfScheduleCriteria criteria, Sort sort) {
        log.debug("find by criteria : {}, sort: {}", criteria, sort);
        final Specification<UnitSelfScheduleEntity> specification = createSpecification(criteria);
        return unitSelfScheduleMapper.toDto(selfScheduleRepository.findAll(specification, sort));
    }

    /**
     * Function to convert {@link UnitSelfScheduleCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<UnitSelfScheduleEntity> createSpecification(UnitSelfScheduleCriteria criteria) {
        return new SpecificationCombiner<UnitSelfScheduleEntity>(criteria)
            .and(criteria::getId, UnitSelfScheduleEntity_.id, this::buildRangeSpecification)

            .and(criteria::getCreatedBy, AbstractAuditingEntity_.createdBy, this::buildStringSpecification)
            .and(criteria::getCreatedDate, AbstractAuditingEntity_.createdDate, this::buildInstantFilterRangeSpecification)
            .and(criteria::getLastModifiedBy, AbstractAuditingEntity_.lastModifiedBy, this::buildStringSpecification)
            .and(criteria::getLastModifiedDate, AbstractAuditingEntity_.lastModifiedDate, this::buildInstantFilterRangeSpecification)
            .and(criteria::getFspId, longFilter -> buildSpecification(criteria.getFspId(),
                root -> root.join(UnitSelfScheduleEntity_.unit, JoinType.LEFT).join(UnitEntity_.fsp, JoinType.LEFT).get(FspEntity_.id)))
            .and(criteria::getUnitId, longFilter -> buildSpecification(criteria.getUnitId(),
                root -> root.join(UnitSelfScheduleEntity_.unit, JoinType.LEFT).get(UnitEntity_.id)))
            .and(criteria::getSelfScheduleDate, UnitSelfScheduleEntity_.selfScheduleDate, this::buildInstantFilterRangeSpecification)
            .combine();
    }

    private Specification<UnitSelfScheduleEntity> buildInstantFilterRangeSpecification(InstantFilter instantFilter,
                                                                                       SingularAttribute<? super UnitSelfScheduleEntity, Instant> instantField) {
        CriteriaUtils.setOnlyDayIfEqualsFilter(instantFilter);
        return buildRangeSpecification(instantFilter, instantField);
    }

    @Override
    public String getDefaultOrderProperty() {
        return UnitSelfScheduleEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<UnitSelfScheduleEntity, Long> getRepository() {
        return this.selfScheduleRepository;
    }

    @Override
    public EntityMapper<UnitSelfScheduleDTO, UnitSelfScheduleEntity> getMapper() {
        return this.unitSelfScheduleMapper;
    }
}
