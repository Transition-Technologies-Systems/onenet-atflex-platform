package pl.com.tt.flex.server.service.schedulingUnit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.domain.fsp.FspEntity_;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity_;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity_;
import pl.com.tt.flex.server.domain.unit.UnitEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitCriteria;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link SchedulingUnitEntity} entities in the database.
 * The main input is a {@link SchedulingUnitCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link SchedulingUnitDTO} or a {@link Page} of {@link SchedulingUnitDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SchedulingUnitQueryService extends AbstractQueryServiceImpl<SchedulingUnitEntity, SchedulingUnitDTO, Long, SchedulingUnitCriteria> {

    private final SchedulingUnitRepository schedulingUnitRepository;

    private final SchedulingUnitMapper schedulingUnitMapper;

    public SchedulingUnitQueryService(SchedulingUnitRepository schedulingUnitRepository, SchedulingUnitMapper schedulingUnitMapper) {
        this.schedulingUnitRepository = schedulingUnitRepository;
        this.schedulingUnitMapper = schedulingUnitMapper;
    }

    public List<SchedulingUnitMinDTO> findMinByCriteria(SchedulingUnitCriteria criteria, Sort sort) {
        log.debug("find min by criteria : {}", criteria);
        final Specification<SchedulingUnitEntity> specification = createSpecification(criteria);
        return schedulingUnitMapper.toMinDto(schedulingUnitRepository.findAll(specification, sort));
    }

    /**
     * Function to convert {@link SchedulingUnitCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<SchedulingUnitEntity> createSpecification(SchedulingUnitCriteria criteria) {
        Specification<SchedulingUnitEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), SchedulingUnitEntity_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), SchedulingUnitEntity_.name));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), SchedulingUnitEntity_.active));
            }
            if (criteria.getNumberOfDers() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getNumberOfDers(), SchedulingUnitEntity_.numberOfDers));
            }
            if (criteria.getSchedulingUnitTypeId() != null) {
                specification = specification.and(buildSpecification(criteria.getSchedulingUnitTypeId(),
                    root -> root.join(SchedulingUnitEntity_.schedulingUnitType, JoinType.LEFT).get(SchedulingUnitTypeEntity_.id)));
            }
            if (criteria.getUnitId() != null) {
                specification = specification.and(buildSpecification(criteria.getUnitId(),
                    root -> root.join(SchedulingUnitEntity_.units, JoinType.LEFT).get(UnitEntity_.id)));
            }
            if (criteria.getBspId() != null) {
                specification = specification.and(buildSpecification(criteria.getBspId(),
                    root -> root.join(SchedulingUnitEntity_.bsp, JoinType.LEFT).get(FspEntity_.id)));
            }
            if (criteria.getBspRepresentativeCompanyName() != null) {
                specification = specification.and(buildSpecification(criteria.getBspRepresentativeCompanyName(),
                    root -> root.join(SchedulingUnitEntity_.bsp, JoinType.LEFT).get(FspEntity_.companyName)));
            }
            if (criteria.getReadyForTests() != null) {
                specification = specification.and(buildSpecification(criteria.getReadyForTests(), SchedulingUnitEntity_.readyForTests));
            }
            if (criteria.getCertified() != null) {
                specification = specification.and(buildSpecification(criteria.getCertified(), SchedulingUnitEntity_.certified));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), SchedulingUnitEntity_.createdBy));
            }
            if (criteria.getCreatedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getCreatedDate());
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), SchedulingUnitEntity_.createdDate));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), SchedulingUnitEntity_.lastModifiedBy));
            }
            if (criteria.getLastModifiedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getLastModifiedDate());
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), SchedulingUnitEntity_.lastModifiedDate));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return SchedulingUnitEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<SchedulingUnitEntity, Long> getRepository() {
        return this.schedulingUnitRepository;
    }

    @Override
    public EntityMapper<SchedulingUnitDTO, SchedulingUnitEntity> getMapper() {
        return this.schedulingUnitMapper;
    }
}
