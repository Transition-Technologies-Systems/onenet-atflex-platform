package pl.com.tt.flex.onenet.service.providedata;

import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.filter.StringFilter;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataViewEntity;
import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataViewEntity_;
import pl.com.tt.flex.onenet.repository.AbstractJpaRepository;
import pl.com.tt.flex.onenet.repository.consumedata.ConsumeDataViewRepository;
import pl.com.tt.flex.onenet.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.onenet.service.consumedata.dto.ConsumeDataViewCriteria;
import pl.com.tt.flex.onenet.service.mapper.EntityMapper;
import pl.com.tt.flex.onenet.service.onenetuser.OnenetUserService;
import pl.com.tt.flex.onenet.service.providedata.dto.ProvideDataViewDTO;
import pl.com.tt.flex.onenet.service.providedata.mapper.ProvideDataViewMapper;

@Slf4j
@Service
public class ProvideDataQueryService extends AbstractQueryServiceImpl<ConsumeDataViewEntity, ProvideDataViewDTO, Long, ConsumeDataViewCriteria> {
	private final ConsumeDataViewRepository consumeDataViewRepository;
	private final OnenetUserService onenetUserService;
	private final ProvideDataViewMapper provideDataViewMapper;

	public ProvideDataQueryService(final ConsumeDataViewRepository consumeDataViewRepository, final OnenetUserService onenetUserService,
								   final ProvideDataViewMapper provideDataViewMapper) {
		this.consumeDataViewRepository = consumeDataViewRepository;
		this.onenetUserService = onenetUserService;
		this.provideDataViewMapper = provideDataViewMapper;
	}

	@Transactional
	public Page<ProvideDataViewDTO> findByCriteria(ConsumeDataViewCriteria criteria, Pageable page) {
		String activeUserName = onenetUserService.getCurrentActiveUser().getUsername();
		if (Objects.nonNull(criteria.getDataSupplier())) {
			criteria.getDataSupplier().setEquals(activeUserName);
		} else {
			StringFilter dataSupplierFilter = new StringFilter();
			dataSupplierFilter.setEquals(activeUserName);
			criteria.setDataSupplier(dataSupplierFilter);
		}
		return super.findByCriteria(criteria, page);
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
		return specification;
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
	public EntityMapper<ProvideDataViewDTO, ConsumeDataViewEntity> getMapper() {
		return this.provideDataViewMapper;
	}

}