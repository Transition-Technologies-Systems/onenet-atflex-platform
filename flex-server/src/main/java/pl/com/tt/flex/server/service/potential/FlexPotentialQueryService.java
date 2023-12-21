package pl.com.tt.flex.server.service.potential;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.domain.fsp.FspEntity_;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity_;
import pl.com.tt.flex.server.domain.product.ProductEntity_;
import pl.com.tt.flex.server.domain.unit.UnitEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialCriteria;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

/**
 * Service for executing complex queries for {@link FlexPotentialEntity} entities in the database.
 * The main input is a {@link FlexPotentialCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link FlexPotentialDTO} or a {@link Page} of {@link FlexPotentialDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class FlexPotentialQueryService extends AbstractQueryServiceImpl<FlexPotentialEntity, FlexPotentialDTO, Long, FlexPotentialCriteria> {

    private final FlexPotentialRepository flexPotentialRepository;

    private final FlexPotentialMapper flexPotentialMapper;

    public FlexPotentialQueryService(final FlexPotentialRepository flexPotentialRepository, final FlexPotentialMapper flexPotentialMapper) {
        this.flexPotentialRepository = flexPotentialRepository;
        this.flexPotentialMapper = flexPotentialMapper;
    }

    /**
     * Return a {@link List} of {@link FlexPotentialDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param sort     The sorting parameters.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<FlexPotentialDTO> findByCriteria(FlexPotentialCriteria criteria, Sort sort) {
        log.debug("find by criteria : {}, sort: {}", criteria, sort);
        final Specification<FlexPotentialEntity> specification = createSpecification(criteria);
        return flexPotentialMapper.toDto(flexPotentialRepository.findAll(specification, sort));
    }


    /**
     * Function to convert {@link FlexPotentialCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<FlexPotentialEntity> createSpecification(FlexPotentialCriteria criteria) {
        Specification<FlexPotentialEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), FlexPotentialEntity_.id));
            }
            if (criteria.getVolume() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getVolume(), FlexPotentialEntity_.volume));
            }
            if (criteria.getVolumeUnit() != null) {
                specification = specification.and(buildSpecification(criteria.getVolumeUnit(), FlexPotentialEntity_.volumeUnit));
            }
            if (criteria.getValidFrom() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getValidFrom());
                specification = specification.and(buildRangeSpecification(criteria.getValidFrom(), FlexPotentialEntity_.validFrom));
            }
            if (criteria.getValidTo() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getValidTo());
                specification = specification.and(buildRangeSpecification(criteria.getValidTo(), FlexPotentialEntity_.validTo));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), FlexPotentialEntity_.active));
            }
            if (criteria.getProductPrequalification() != null) {
                specification = specification.and(buildSpecification(criteria.getProductPrequalification(), FlexPotentialEntity_.productPrequalification));
            }
            if (criteria.getStaticGridPrequalification() != null) {
                specification = specification.and(buildSpecification(criteria.getStaticGridPrequalification(), FlexPotentialEntity_.staticGridPrequalification));
            }
            if (criteria.getVersion() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getVersion(), FlexPotentialEntity_.version));
            }
            if (criteria.getFspId() != null) {
                specification = specification.and(buildSpecification(criteria.getFspId(), root -> root.join(FlexPotentialEntity_.fsp, JoinType.INNER).get(FspEntity_.id)));
            }

            if (criteria.getProductId() != null) {
                specification = specification.and(buildSpecification(criteria.getProductId(), root -> root.join(FlexPotentialEntity_.product, JoinType.INNER)
                    .get(ProductEntity_.id)));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), FlexPotentialEntity_.createdBy));
            }
            if (criteria.getCreatedByRole() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedByRole(), FlexPotentialEntity_.createdByRole));
            }
            if (criteria.getCreatedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getCreatedDate());
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), FlexPotentialEntity_.createdDate));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), FlexPotentialEntity_.lastModifiedBy));
            }
            if (criteria.getLastModifiedByRole() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedByRole(), FlexPotentialEntity_.lastModifiedByRole));
            }
            if (criteria.getLastModifiedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getLastModifiedDate());
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), FlexPotentialEntity_.lastModifiedDate));
            }
            if (criteria.getProductShortName() != null) {
                specification = specification.and(buildSpecification(criteria.getProductShortName(), root ->
                    root.join(FlexPotentialEntity_.product, JoinType.INNER).get(ProductEntity_.shortName)));
            }
            if (criteria.getFspRepresentativeCompanyName() != null) {
                specification = specification.and(buildSpecification(criteria.getFspRepresentativeCompanyName(), root ->
                    root.join(FlexPotentialEntity_.fsp, JoinType.INNER).get(FspEntity_.companyName)));
            }
            if (criteria.getFullActivationTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getFullActivationTime(), FlexPotentialEntity_.fullActivationTime));
            }
            if (criteria.getMinDeliveryDuration() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMinDeliveryDuration(), FlexPotentialEntity_.minDeliveryDuration));
            }
            if (criteria.getAggregated() != null) {
                specification = specification.and(buildSpecification(criteria.getAggregated(), FlexPotentialEntity_.aggregated));
            }
            if (criteria.getIsRegister() != null) {
                specification = specification.and(buildSpecification(criteria.getIsRegister(), FlexPotentialEntity_.registered));
            }
            if (criteria.getDivisibility() != null) {
                specification = specification.and(buildSpecification(criteria.getDivisibility(), FlexPotentialEntity_.divisibility));
            }
            if (criteria.getUnitName() != null) {
                specification = specification.and(buildSpecification(criteria.getUnitName(), root ->
                    root.join(FlexPotentialEntity_.units, JoinType.INNER).get(UnitEntity_.name)));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return FlexPotentialEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<FlexPotentialEntity, Long> getRepository() {
        return this.flexPotentialRepository;
    }

    @Override
    public EntityMapper<FlexPotentialDTO, FlexPotentialEntity> getMapper() {
        return this.flexPotentialMapper;
    }
}
