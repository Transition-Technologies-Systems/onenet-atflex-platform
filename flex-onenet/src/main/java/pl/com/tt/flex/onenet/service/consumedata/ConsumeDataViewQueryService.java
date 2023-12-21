package pl.com.tt.flex.onenet.service.consumedata;

import javax.persistence.criteria.Join;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataViewEntity;
import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataViewEntity_;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity_;
import pl.com.tt.flex.onenet.repository.AbstractJpaRepository;
import pl.com.tt.flex.onenet.repository.consumedata.ConsumeDataViewRepository;
import pl.com.tt.flex.onenet.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.onenet.service.consumedata.dto.ConsumeDataViewCriteria;
import pl.com.tt.flex.onenet.service.consumedata.dto.ConsumeDataViewDTO;
import pl.com.tt.flex.onenet.service.consumedata.mapper.ConsumeDataViewMapper;
import pl.com.tt.flex.onenet.service.mapper.EntityMapper;
import pl.com.tt.flex.onenet.service.onenetuser.OnenetUserService;

/**
 * Serwis do obsługi zaawansowanych zapytań dla encji {@link ConsumeDataViewEntity} w bazie danych.
 * Klasa {@link ConsumeDataViewCriteria} zawiera filtry, za pomocą których można filtrować oraz sortować dane.
 * Zwraca listę {@link ConsumeDataViewDTO} które spełniają dane kryteria.
 */
@Slf4j
@Service
public class ConsumeDataViewQueryService extends AbstractQueryServiceImpl<ConsumeDataViewEntity, ConsumeDataViewDTO, Long, ConsumeDataViewCriteria> {
	private final ConsumeDataViewMapper consumeDataViewMapper;
	private final ConsumeDataViewRepository consumeDataViewRepository;
	private final OnenetUserService onenetUserService;

	public ConsumeDataViewQueryService(final ConsumeDataViewMapper consumeDataMapper, final ConsumeDataViewRepository consumeDataViewRepository, final OnenetUserService onenetUserService) {
		this.consumeDataViewMapper = consumeDataMapper;
		this.consumeDataViewRepository = consumeDataViewRepository;
		this.onenetUserService = onenetUserService;
	}

	@Override
	protected Specification<ConsumeDataViewEntity> createSpecification(ConsumeDataViewCriteria criteria) {
		Specification<ConsumeDataViewEntity> specification = Specification.where(null);
		if (criteria != null) {
			if (criteria.getId() != null) {
				specification = specification.and(buildRangeSpecification(criteria.getId(), ConsumeDataViewEntity_.id));
			}
			if (criteria.getTitle() != null) {
				specification = specification.and(buildStringSpecification(criteria.getTitle(), ConsumeDataViewEntity_.title));
			}
			if (criteria.getOnenetId() != null) {
				specification = specification.and(buildStringSpecification(criteria.getOnenetId(), ConsumeDataViewEntity_.onenetId));
			}
			if (criteria.getBusinessObject() != null) {
				specification = specification.and(buildStringSpecification(criteria.getBusinessObject(), ConsumeDataViewEntity_.businessObject));
			}
			if (criteria.getDataSupplier() != null) {
				specification = specification.and(buildStringSpecification(criteria.getDataSupplier(), ConsumeDataViewEntity_.dataSupplier));
			}
		}
		return addSpecificationActiveUserAuthorized(specification);
	}

	private Specification<ConsumeDataViewEntity> addSpecificationActiveUserAuthorized(Specification<ConsumeDataViewEntity> specification) {
		return specification.and((root, query, cb) -> {
			Join<ConsumeDataViewEntity, OnenetUserEntity> authorizedUsersJoin = root.join(ConsumeDataViewEntity_.authorizedUsers);
			return cb.equal(authorizedUsersJoin.get(OnenetUserEntity_.id), onenetUserService.getCurrentActiveUser().getId());
		});
	}

	@Override
	public String getDefaultOrderProperty() {
		return ConsumeDataViewEntity_.ID;
	}

	@Override
	public AbstractJpaRepository<ConsumeDataViewEntity, Long> getRepository() {
		return this.consumeDataViewRepository;
	}

	@Override
	public EntityMapper<ConsumeDataViewDTO, ConsumeDataViewEntity> getMapper() {
		return this.consumeDataViewMapper;
	}

}
