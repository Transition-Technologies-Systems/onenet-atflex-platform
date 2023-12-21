package pl.com.tt.flex.server.service.schedulingUnit;

import io.github.jhipster.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.fsp.FspEntity_;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity_;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitProposalEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitProposalEntity_;
import pl.com.tt.flex.server.domain.unit.UnitEntity_;
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitProposalRepository;
import pl.com.tt.flex.server.service.schedulingUnit.dto.*;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitProposalMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

import javax.persistence.criteria.JoinType;
import java.util.List;

import static pl.com.tt.flex.server.service.common.QueryServiceUtil.setDefaultOrder;

/**
 * Service for executing complex queries for {@link SchedulingUnitProposalEntity} entities in the database.
 * The main input is a {@link SchedulingUnitProposalCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link SchedulingUnitProposalDTO} or a {@link Page} of {@link SchedulingUnitProposalDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SchedulingUnitQueryProposalService extends QueryService<SchedulingUnitProposalEntity> {

    private final SchedulingUnitProposalRepository schedulingUnitProposalRepository;

    private final SchedulingUnitProposalMapper schedulingUnitProposalMapper;

    public SchedulingUnitQueryProposalService(SchedulingUnitProposalRepository schedulingUnitProposalRepository, SchedulingUnitProposalMapper schedulingUnitProposalMapper) {
        this.schedulingUnitProposalRepository = schedulingUnitProposalRepository;
        this.schedulingUnitProposalMapper = schedulingUnitProposalMapper;
    }

    /**
     * Return a {@link List} of {@link SchedulingUnitDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<SchedulingUnitProposalMinDTO> findByCriteria(SchedulingUnitProposalCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<SchedulingUnitProposalEntity> specification = createSpecification(criteria);
        return schedulingUnitProposalMapper.toMinDto(schedulingUnitProposalRepository.findAll(specification));
    }

    /**
     * Return a {@link Page} of {@link SchedulingUnitDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<SchedulingUnitProposalMinDTO> findByCriteria(SchedulingUnitProposalCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<SchedulingUnitProposalEntity> specification = createSpecification(criteria);
        page = setDefaultOrder(page, SchedulingUnitProposalEntity_.ID);
        return schedulingUnitProposalRepository.findAll(specification, page)
            .map(schedulingUnitProposalMapper::toMinDto);
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(SchedulingUnitProposalCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<SchedulingUnitProposalEntity> specification = createSpecification(criteria);
        return schedulingUnitProposalRepository.count(specification);
    }

    /**
     * Function to convert {@link SchedulingUnitCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<SchedulingUnitProposalEntity> createSpecification(SchedulingUnitProposalCriteria criteria) {
        Specification<SchedulingUnitProposalEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), SchedulingUnitProposalEntity_.id));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), SchedulingUnitProposalEntity_.status));
            }
            if (criteria.getBspId() != null) {
                specification = specification.and(buildSpecification(criteria.getBspId(),
                    root -> root.join(SchedulingUnitProposalEntity_.bsp, JoinType.LEFT).get(FspEntity_.id)));
            }
            if (criteria.getBspCompanyName() != null) {
                specification = specification.and(buildSpecification(criteria.getBspCompanyName(),
                    root -> root.join(SchedulingUnitProposalEntity_.bsp, JoinType.LEFT).get(FspEntity_.companyName)));
            }
            if (criteria.getSchedulingUnitId() != null) {
                specification = specification.and(buildSpecification(criteria.getSchedulingUnitId(),
                    root -> root.join(SchedulingUnitProposalEntity_.schedulingUnit, JoinType.LEFT).get(SchedulingUnitEntity_.id)));
            }
            if (criteria.getUnitId() != null) {
                specification = specification.and(buildSpecification(criteria.getUnitId(),
                    root -> root.join(SchedulingUnitProposalEntity_.unit, JoinType.LEFT).get(UnitEntity_.id)));
            }
            if (criteria.getUnitName() != null) {
                specification = specification.and(buildSpecification(criteria.getUnitName(),
                    root -> root.join(SchedulingUnitProposalEntity_.unit, JoinType.LEFT).get(UnitEntity_.name)));
            }
            if (criteria.getFspId() != null) {
                specification = specification.and(buildSpecification(criteria.getFspId(),
                    root -> root.join(SchedulingUnitProposalEntity_.unit, JoinType.LEFT).join(UnitEntity_.fsp, JoinType.LEFT).get(FspEntity_.id)));
            }
            if (criteria.getFspCompanyName() != null) {
                specification = specification.and(buildSpecification(criteria.getFspCompanyName(),
                    root -> root.join(SchedulingUnitProposalEntity_.unit, JoinType.LEFT).join(UnitEntity_.fsp, JoinType.LEFT).get(FspEntity_.companyName)));
            }
            if (criteria.getProposalType() != null) {
                specification = specification.and(buildSpecification(criteria.getProposalType(), SchedulingUnitProposalEntity_.proposalType));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), SchedulingUnitProposalEntity_.createdBy));
            }
            if (criteria.getCreatedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getCreatedDate());
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), SchedulingUnitProposalEntity_.createdDate));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), SchedulingUnitProposalEntity_.lastModifiedBy));
            }
            if (criteria.getLastModifiedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getLastModifiedDate());
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), SchedulingUnitProposalEntity_.lastModifiedDate));
            }
            if (criteria.getSentDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getSentDate());
                specification = specification.and(buildRangeSpecification(criteria.getSentDate(), SchedulingUnitProposalEntity_.sentDate));
            }
        }
        return specification;
    }
}
