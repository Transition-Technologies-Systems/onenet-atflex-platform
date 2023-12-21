package pl.com.tt.flex.flex.agno.service.kdm_model;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelEntity;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelEntity_;
import pl.com.tt.flex.flex.agno.repository.AbstractJpaRepository;
import pl.com.tt.flex.flex.agno.repository.kdm_model.KdmModelRepository;
import pl.com.tt.flex.flex.agno.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.flex.agno.service.kdm_model.dto.KdmModelCriteria;
import pl.com.tt.flex.flex.agno.service.kdm_model.mapper.KdmModelMapper;
import pl.com.tt.flex.flex.agno.service.mapper.EntityMapper;
import pl.com.tt.flex.flex.agno.util.CriteriaUtils;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelDTO;

/**
 * Service for executing complex queries for {@link KdmModelEntity} entities in the database.
 * The main input is a {@link KdmModelCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link KdmModelDTO} or a {@link Page} of {@link KdmModelDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class KdmModelQueryService extends AbstractQueryServiceImpl<KdmModelEntity, KdmModelDTO, Long, KdmModelCriteria> {

	private final KdmModelRepository kdmModelRepository;

	private final KdmModelMapper kdmModelMapper;

	public KdmModelQueryService(final KdmModelRepository kdmModelRepository, final KdmModelMapper kdmModelMapper) {
		this.kdmModelRepository = kdmModelRepository;
		this.kdmModelMapper = kdmModelMapper;
	}

	/**
	 * Function to convert {@link KdmModelCriteria} to a {@link Specification}
	 *
	 * @param criteria The object which holds all the filters, which the entities should match.
	 * @return the matching {@link Specification} of the entity.
	 */
	protected Specification<KdmModelEntity> createSpecification(KdmModelCriteria criteria) {
		Specification<KdmModelEntity> specification = Specification.where(null);
		if (criteria != null) {
			if (criteria.getId() != null) {
				specification = specification.and(buildRangeSpecification(criteria.getId(), KdmModelEntity_.id));
			}
			if (criteria.getAreaName() != null) {
				specification = specification.and(buildStringSpecification(criteria.getAreaName(), KdmModelEntity_.areaName));
			}
			if (criteria.getLvModel() != null) {
				specification = specification.and(buildSpecification(criteria.getLvModel(), KdmModelEntity_.lvModel));
			}
			if (criteria.getLastModifiedDate() != null) {
				CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getLastModifiedDate());
				specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), KdmModelEntity_.lastModifiedDate));
			}
			if (criteria.getCreatedDate() != null) {
				CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getCreatedDate());
				specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), KdmModelEntity_.createdDate));
			}
		}
		return specification;
	}

	@Override
	public String getDefaultOrderProperty() {
		return KdmModelEntity_.ID;
	}

	@Override
	public AbstractJpaRepository<KdmModelEntity, Long> getRepository() {
		return this.kdmModelRepository;
	}

	@Override
	public EntityMapper<KdmModelDTO, KdmModelEntity> getMapper() {
		return this.kdmModelMapper;
	}
}
