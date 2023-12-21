package pl.com.tt.flex.server.service.dictionary.schedulingUnitType;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.domain.product.ProductEntity_;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity_;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.schedulingUnitType.SchedulingUnitTypeRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeCriteria;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeDTO;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.mapper.SchedulingUnitTypeMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

/**
 * Service for executing complex queries for {@link SchedulingUnitTypeEntity} entities in the database.
 * The main input is a {@link SchedulingUnitTypeCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link SchedulingUnitTypeDTO} or a {@link Page} of {@link SchedulingUnitTypeDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SchedulingUnitTypeQueryService extends AbstractQueryServiceImpl<SchedulingUnitTypeEntity, SchedulingUnitTypeDTO, Long, SchedulingUnitTypeCriteria> {

    private final SchedulingUnitTypeRepository schedulingUnitTypeRepository;

    private final SchedulingUnitTypeMapper schedulingUnitTypeMapper;

    public SchedulingUnitTypeQueryService(final SchedulingUnitTypeRepository schedulingUnitTypeRepository, final SchedulingUnitTypeMapper schedulingUnitTypeMapper) {
        this.schedulingUnitTypeRepository = schedulingUnitTypeRepository;
        this.schedulingUnitTypeMapper = schedulingUnitTypeMapper;
    }

    /**
     * Function to convert {@link SchedulingUnitTypeCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<SchedulingUnitTypeEntity> createSpecification(SchedulingUnitTypeCriteria criteria) {
        Specification<SchedulingUnitTypeEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), SchedulingUnitTypeEntity_.id));
            }
            if (criteria.getDescriptionEn() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescriptionEn(), SchedulingUnitTypeEntity_.descriptionEn));
            }
            if (criteria.getDescriptionPl() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescriptionPl(), SchedulingUnitTypeEntity_.descriptionPl));
            }
            if (criteria.getProductId() != null) {
                specification = specification.and(buildSpecification(criteria.getProductId(), root -> root.join(SchedulingUnitTypeEntity_.products, JoinType.INNER).get(ProductEntity_.id)));
            }
            if (criteria.getLastModifiedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getLastModifiedDate());
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), LocalizationTypeEntity_.lastModifiedDate));
            }
            if (criteria.getCreatedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getCreatedDate());
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), LocalizationTypeEntity_.createdDate));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return SchedulingUnitTypeEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<SchedulingUnitTypeEntity, Long> getRepository() {
        return this.schedulingUnitTypeRepository;
    }

    @Override
    public EntityMapper<SchedulingUnitTypeDTO, SchedulingUnitTypeEntity> getMapper() {
        return this.schedulingUnitTypeMapper;
    }
}
