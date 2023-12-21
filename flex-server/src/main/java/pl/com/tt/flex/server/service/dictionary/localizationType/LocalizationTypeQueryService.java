package pl.com.tt.flex.server.service.dictionary.localizationType;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.localizationType.LocalizationTypeRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.dictionary.localizationType.dto.LocalizationTypeCriteria;
import pl.com.tt.flex.server.service.dictionary.localizationType.mapper.LocalizationTypeMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

/**
 * Service for executing complex queries for {@link LocalizationTypeEntity} entities in the database.
 * The main input is a {@link LocalizationTypeCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link LocalizationTypeDTO} or a {@link Page} of {@link LocalizationTypeDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class LocalizationTypeQueryService extends AbstractQueryServiceImpl<LocalizationTypeEntity, LocalizationTypeDTO, Long, LocalizationTypeCriteria> {

    private final LocalizationTypeRepository localizationTypeRepository;

    private final LocalizationTypeMapper localizationTypeMapper;

    public LocalizationTypeQueryService(final LocalizationTypeRepository localizationTypeRepository, final LocalizationTypeMapper localizationTypeMapper) {
        this.localizationTypeRepository = localizationTypeRepository;
        this.localizationTypeMapper = localizationTypeMapper;
    }

    /**
     * Function to convert {@link LocalizationTypeCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<LocalizationTypeEntity> createSpecification(LocalizationTypeCriteria criteria) {
        Specification<LocalizationTypeEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), LocalizationTypeEntity_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), LocalizationTypeEntity_.name));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), LocalizationTypeEntity_.type));
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
        return LocalizationTypeEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<LocalizationTypeEntity, Long> getRepository() {
        return this.localizationTypeRepository;
    }

    @Override
    public EntityMapper<LocalizationTypeDTO, LocalizationTypeEntity> getMapper() {
        return this.localizationTypeMapper;
    }
}
