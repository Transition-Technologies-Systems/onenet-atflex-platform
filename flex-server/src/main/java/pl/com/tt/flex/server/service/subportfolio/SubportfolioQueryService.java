package pl.com.tt.flex.server.service.subportfolio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.fsp.FspEntity_;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity_;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity_;
import pl.com.tt.flex.server.domain.unit.UnitEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.subportfolio.SubportfolioRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioCriteria;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;
import pl.com.tt.flex.server.service.subportfolio.mapper.SubportfolioMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link SubportfolioEntity} entities in the database.
 * The main input is a {@link SubportfolioCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link SubportfolioDTO} or a {@link Page} of {@link SubportfolioDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class SubportfolioQueryService extends AbstractQueryServiceImpl<SubportfolioEntity, SubportfolioDTO, Long, SubportfolioCriteria> {

    private final SubportfolioRepository subportfolioRepository;

    private final SubportfolioMapper subportfolioMapper;

    public SubportfolioQueryService(SubportfolioRepository subportfolioRepository, SubportfolioMapper subportfolioMapper) {
        this.subportfolioRepository = subportfolioRepository;
        this.subportfolioMapper = subportfolioMapper;
    }

    /**
     * Function to convert {@link SubportfolioCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<SubportfolioEntity> createSpecification(SubportfolioCriteria criteria) {
        Specification<SubportfolioEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), SubportfolioEntity_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), SubportfolioEntity_.name));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), SubportfolioEntity_.active));
            }
            if (criteria.getCertified() != null) {
                specification = specification.and(buildSpecification(criteria.getCertified(), SubportfolioEntity_.certified));
            }
            if (criteria.getNumberOfDers() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getNumberOfDers(), SubportfolioEntity_.numberOfDers));
            }
            if (criteria.getMrid() != null) {
                specification = specification.and(buildStringSpecification(criteria.getMrid(), SubportfolioEntity_.mrid));
            }
            if (criteria.getCouplingPointIdTypes() != null) {
                specification = specification.and(buildSpecification(criteria.getCouplingPointIdTypes(),
                    root -> root.join(SubportfolioEntity_.couplingPointIdTypes, JoinType.LEFT).get(LocalizationTypeEntity_.ID)));
            }
            if (criteria.getUnitId() != null) {
                specification = specification.and(buildSpecification(criteria.getUnitId(),
                    root -> root.join(SubportfolioEntity_.units, JoinType.LEFT).get(UnitEntity_.id)));
            }
            if (criteria.getFspaId() != null) {
                specification = specification.and(buildSpecification(criteria.getFspaId(),
                    root -> root.join(SubportfolioEntity_.fspa, JoinType.LEFT).get(FspEntity_.id)));
            }
            if (criteria.getFspaRepresentativeCompanyName() != null) {
                specification = specification.and(buildSpecification(criteria.getFspaRepresentativeCompanyName(),
                    root -> root.join(SubportfolioEntity_.fspa, JoinType.LEFT).get(FspEntity_.companyName)));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), SubportfolioEntity_.createdBy));
            }
            if (criteria.getCreatedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getCreatedDate());
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), SubportfolioEntity_.createdDate));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), SubportfolioEntity_.lastModifiedBy));
            }
            if (criteria.getLastModifiedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getLastModifiedDate());
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), SubportfolioEntity_.lastModifiedDate));
            }
            if (criteria.getValidFrom() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getValidFrom());
                specification = specification.and(buildRangeSpecification(criteria.getValidFrom(), SubportfolioEntity_.validFrom));
            }
            if (criteria.getValidTo() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getValidTo());
                specification = specification.and(buildRangeSpecification(criteria.getValidTo(), SubportfolioEntity_.validTo));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return SubportfolioEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<SubportfolioEntity, Long> getRepository() {
        return this.subportfolioRepository;
    }

    @Override
    public EntityMapper<SubportfolioDTO, SubportfolioEntity> getMapper() {
        return this.subportfolioMapper;
    }
}
