package pl.com.tt.flex.server.service.dictionary.derType;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity_;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.derType.DerTypeRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeCriteria;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeDTO;
import pl.com.tt.flex.server.service.dictionary.derType.mapper.DerTypeMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

/**
 * Service for executing complex queries for {@link DerTypeEntity} entities in the database.
 * The main input is a {@link DerTypeCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link DerTypeDTO} or a {@link Page} of {@link DerTypeDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class DerTypeQueryService extends AbstractQueryServiceImpl<DerTypeEntity, DerTypeDTO, Long, DerTypeCriteria> {

    private final DerTypeRepository derTypeRepository;

    private final DerTypeMapper derTypeMapper;

    public DerTypeQueryService(final DerTypeRepository derTypeRepository, final DerTypeMapper derTypeMapper) {
        this.derTypeRepository = derTypeRepository;
        this.derTypeMapper = derTypeMapper;
    }

    /**
     * Function to convert {@link DerTypeCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<DerTypeEntity> createSpecification(DerTypeCriteria criteria) {
        Specification<DerTypeEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), DerTypeEntity_.id));
            }
            if (criteria.getDescriptionEn() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescriptionEn(), DerTypeEntity_.descriptionEn));
            }
            if (criteria.getDescriptionPl() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDescriptionPl(), DerTypeEntity_.descriptionPl));
            }
            if (criteria.getLastModifiedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getLastModifiedDate());
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), LocalizationTypeEntity_.lastModifiedDate));
            }
            if (criteria.getCreatedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getCreatedDate());
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), LocalizationTypeEntity_.createdDate));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), DerTypeEntity_.type));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return DerTypeEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<DerTypeEntity, Long> getRepository() {
        return this.derTypeRepository;
    }

    @Override
    public EntityMapper<DerTypeDTO, DerTypeEntity> getMapper() {
        return this.derTypeMapper;
    }
}
