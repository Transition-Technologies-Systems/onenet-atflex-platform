package pl.com.tt.flex.server.service.user.registration;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.domain.user.UserEntity_;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.user.registration.FspUserRegistrationRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationCriteria;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationDTO;
import pl.com.tt.flex.server.service.user.registration.mapper.FspUserRegistrationMapper;

/**
 * Service for executing complex queries for {@link FspUserRegistrationEntity} entities in the database.
 * The main input is a {@link FspUserRegistrationCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link FspUserRegistrationDTO} or a {@link Page} of {@link FspUserRegistrationDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class FspUserRegistrationQueryService extends AbstractQueryServiceImpl<FspUserRegistrationEntity, FspUserRegistrationDTO, Long, FspUserRegistrationCriteria> {

    private final FspUserRegistrationRepository fspUserRegistrationRepository;

    private final FspUserRegistrationMapper fspUserRegistrationMapper;

    public FspUserRegistrationQueryService(final FspUserRegistrationRepository fspUserRegistrationRepository, final FspUserRegistrationMapper fspUserRegistrationMapper) {
        this.fspUserRegistrationRepository = fspUserRegistrationRepository;
        this.fspUserRegistrationMapper = fspUserRegistrationMapper;
    }

    /**
     * Function to convert {@link FspUserRegistrationCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<FspUserRegistrationEntity> createSpecification(FspUserRegistrationCriteria criteria) {
        Specification<FspUserRegistrationEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), FspUserRegistrationEntity_.id));
            }
            if (criteria.getFirstName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFirstName(), FspUserRegistrationEntity_.firstName));
            }
            if (criteria.getLastName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastName(), FspUserRegistrationEntity_.lastName));
            }
            if (criteria.getCompanyName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCompanyName(), FspUserRegistrationEntity_.companyName));
            }
            if (criteria.getEmail() != null) {
                specification = specification.and(buildStringSpecification(criteria.getEmail(), FspUserRegistrationEntity_.email));
            }
            if (criteria.getPhoneNumber() != null) {
                specification = specification.and(buildStringSpecification(criteria.getPhoneNumber(), FspUserRegistrationEntity_.phoneNumber));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), FspUserRegistrationEntity_.status));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), FspUserRegistrationEntity_.createdBy));
            }
            if (criteria.getCreatedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), FspUserRegistrationEntity_.createdDate));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), FspUserRegistrationEntity_.lastModifiedBy));
            }
            if (criteria.getLastModifiedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), FspUserRegistrationEntity_.lastModifiedDate));
            }
            if (criteria.getFspUserId() != null) {
                specification = specification.and(buildSpecification(criteria.getFspUserId(),
                    root -> root.join(FspUserRegistrationEntity_.fspUser, JoinType.LEFT).get(UserEntity_.id)));
            }
            if (criteria.getUserTargetRole() != null) {
                specification = specification.and(buildSpecification(criteria.getUserTargetRole(), FspUserRegistrationEntity_.userTargetRole));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return FspUserRegistrationEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<FspUserRegistrationEntity, Long> getRepository() {
        return this.fspUserRegistrationRepository;
    }

    @Override
    public EntityMapper<FspUserRegistrationDTO, FspUserRegistrationEntity> getMapper() {
        return this.fspUserRegistrationMapper;
    }
}
