package pl.com.tt.flex.onenet.service.offeredservices;


import javax.persistence.criteria.Join;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pl.com.tt.flex.onenet.domain.offeredservices.OfferedServiceEntity;
import pl.com.tt.flex.onenet.domain.offeredservices.OfferedServiceEntity_;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity_;
import pl.com.tt.flex.onenet.repository.AbstractJpaRepository;
import pl.com.tt.flex.onenet.repository.offeredservices.OfferedServicesRepository;
import pl.com.tt.flex.onenet.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.onenet.service.mapper.EntityMapper;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServiceDTO;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServicesCriteria;
import pl.com.tt.flex.onenet.service.offeredservices.mapper.OfferedServicesMapper;
import pl.com.tt.flex.onenet.service.onenetuser.OnenetUserService;

@Service
@Transactional(readOnly = true)
public class OfferedServicesQueryService extends AbstractQueryServiceImpl<OfferedServiceEntity, OfferedServiceDTO, Long, OfferedServicesCriteria> {

	private final OfferedServicesRepository offeredServicesRepository;
	private final OfferedServicesMapper offeredServicesMapper;
	private final OnenetUserService onenetUserService;

	public OfferedServicesQueryService(final OfferedServicesRepository offeredServicesRepository,
									   final OfferedServicesMapper offeredServicesMapper,
									   final OnenetUserService onenetUserService) {
		this.offeredServicesRepository = offeredServicesRepository;
		this.offeredServicesMapper = offeredServicesMapper;
		this.onenetUserService = onenetUserService;
	}

	@Override
	protected Specification<OfferedServiceEntity> createSpecification(OfferedServicesCriteria criteria) {
		Specification<OfferedServiceEntity> specification = Specification.where(null);
		if (criteria != null) {
			if (criteria.getId() != null) {
				specification = specification.and(buildRangeSpecification(criteria.getId(), OfferedServiceEntity_.id));
			}
			if (criteria.getTitle() != null) {
				specification = specification.and(buildStringSpecification(criteria.getTitle(), OfferedServiceEntity_.title));
			}
			if (criteria.getOnenetId() != null) {
				specification = specification.and(buildStringSpecification(criteria.getOnenetId(), OfferedServiceEntity_.onenetId));
			}
			if (criteria.getBusinessObject() != null) {
				specification = specification.and(buildStringSpecification(criteria.getBusinessObject(), OfferedServiceEntity_.businessObject));
			}
			if (criteria.getServiceCode() != null) {
				specification = specification.and(buildStringSpecification(criteria.getServiceCode(), OfferedServiceEntity_.serviceCode));
			}
			if (criteria.getDescription() != null) {
				specification = specification.and(buildStringSpecification(criteria.getDescription(), OfferedServiceEntity_.description));
			}
		}
		return addSpecificationActiveUserAuthorized(specification);
	}

	@Override
	public String getDefaultOrderProperty() {
		return OfferedServiceEntity_.ID;
	}

	@Override
	public AbstractJpaRepository<OfferedServiceEntity, Long> getRepository() {
		return offeredServicesRepository;
	}

	@Override
	public EntityMapper<OfferedServiceDTO, OfferedServiceEntity> getMapper() {
		return offeredServicesMapper;
	}

	private Specification<OfferedServiceEntity> addSpecificationActiveUserAuthorized(Specification<OfferedServiceEntity> specification) {
		return specification.and((root, query, cb) -> {
			Join<OfferedServiceEntity, OnenetUserEntity> authorizedUsersJoin = root.join(OfferedServiceEntity_.authorizedUsers);
			return cb.equal(authorizedUsersJoin.get(OnenetUserEntity_.id), onenetUserService.getCurrentActiveUser().getId());
		});
	}

}
