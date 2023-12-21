package pl.com.tt.flex.onenet.service.onenetuser;

import static pl.com.tt.flex.onenet.web.rest.errors.ErrorConstants.UNEXPECTED_ERROR;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.onenet.domain.onenetuser.ActiveOnenetUserEntiy;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity;
import pl.com.tt.flex.onenet.domain.onenetuser.OnenetUserEntity_;
import pl.com.tt.flex.onenet.repository.AbstractJpaRepository;
import pl.com.tt.flex.onenet.repository.onenetuser.ActiveOnenetUserRepository;
import pl.com.tt.flex.onenet.repository.onenetuser.OnenetUserRepository;
import pl.com.tt.flex.onenet.security.SecurityUtils;
import pl.com.tt.flex.onenet.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.onenet.service.mapper.EntityMapper;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetUserCriteria;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetUserDTO;
import pl.com.tt.flex.onenet.service.onenetuser.mapper.OnenetUserMapper;
import pl.com.tt.flex.onenet.web.rest.errors.ObjectValidationException;

@Slf4j
@Service
@Transactional(readOnly = true)
public class OnenetUserQueryService extends AbstractQueryServiceImpl<OnenetUserEntity, OnenetUserDTO, Long, OnenetUserCriteria> {

	private final OnenetUserRepository onenetUserRepository;
	private final ActiveOnenetUserRepository activeOnenetUserRepository;
	private final OnenetUserMapper onenetUserMapper;

	public OnenetUserQueryService(final OnenetUserRepository onenetUserRepository,
								  ActiveOnenetUserRepository activeOnenetUserRepository,
								  final OnenetUserMapper onenetUserMapper) {
		this.onenetUserRepository = onenetUserRepository;
		this.activeOnenetUserRepository = activeOnenetUserRepository;
		this.onenetUserMapper = onenetUserMapper;
	}

	@Override
	public Page<OnenetUserDTO> findByCriteria(OnenetUserCriteria criteria, Pageable page) {
		Page<OnenetUserDTO> onenetUsers = super.findByCriteria(criteria, page);
		String currentFlexUsername = SecurityUtils.getCurrentUserLogin().orElseThrow(() ->
				new ObjectValidationException("Could not find currently logged in user", UNEXPECTED_ERROR));
		Long activeOnenetUserId = activeOnenetUserRepository.findByFlexUsernameEquals(currentFlexUsername)
				.map(ActiveOnenetUserEntiy::getActiveOnenetUser)
				.map(OnenetUserEntity::getId)
				.orElse(null);
		onenetUsers.stream()
				.filter(onenetUser -> onenetUser.getId().equals(activeOnenetUserId))
				.forEach(onenetUser -> onenetUser.setActive(true));
		return onenetUsers;
	}

	@Override
	protected Specification<OnenetUserEntity> createSpecification(OnenetUserCriteria criteria) {
		Specification<OnenetUserEntity> specification = Specification.where(null);
		if (criteria != null) {
			if (criteria.getId() != null) {
				specification = specification.and(buildRangeSpecification(criteria.getId(), OnenetUserEntity_.id));
			}
			if (criteria.getUsername() != null) {
				specification = specification.and(buildStringSpecification(criteria.getUsername(), OnenetUserEntity_.username));
			}
			if (criteria.getOnenetId() != null) {
				specification = specification.and(buildStringSpecification(criteria.getOnenetId(), OnenetUserEntity_.onenetId));
			}
			if (criteria.getEmail() != null) {
				specification = specification.and(buildStringSpecification(criteria.getEmail(), OnenetUserEntity_.email));
			}
		}
		return specification;
	}

	@Override
	public String getDefaultOrderProperty() {
		return OnenetUserEntity_.ID;
	}

	@Override
	public AbstractJpaRepository<OnenetUserEntity, Long> getRepository() {
		return this.onenetUserRepository;
	}

	@Override
	public EntityMapper<OnenetUserDTO, OnenetUserEntity> getMapper() {
		return this.onenetUserMapper;
	}

}
