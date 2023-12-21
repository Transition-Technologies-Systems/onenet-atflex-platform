package pl.com.tt.flex.server.service.fsp;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity_;
import pl.com.tt.flex.server.domain.user.UserEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.fsp.FspRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.fsp.dto.FspCriteria;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.fsp.mapper.FspMapper;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

/**
 * Service for executing complex queries for {@link FspEntity} entities in the database.
 * The main input is a {@link FspCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link FspDTO} or a {@link Page} of {@link FspDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class FspQueryService extends AbstractQueryServiceImpl<FspEntity, FspDTO, Long, FspCriteria> {

    private final FspRepository fspRepository;

    private final FspMapper fspMapper;

    public FspQueryService(final FspRepository fspRepository, final FspMapper fspMapper) {
        this.fspRepository = fspRepository;
        this.fspMapper = fspMapper;
    }

    /**
     * Return a {@link List} of {@link FspDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param sort     The sorting parameters.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<FspDTO> findByCriteria(FspCriteria criteria, Sort sort) {
        log.debug("find by criteria : {}, sort: {}", criteria, sort);
        final Specification<FspEntity> specification = createSpecification(criteria);
        return fspMapper.toDto(fspRepository.findAll(specification, sort));
    }

    /**
     * Function to convert {@link FspCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<FspEntity> createSpecification(FspCriteria criteria) {
        Specification<FspEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), FspEntity_.id));
            }
            if (criteria.getValidFrom() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getValidFrom());
                specification = specification.and(buildRangeSpecification(criteria.getValidFrom(), FspEntity_.validFrom));
            }
            if (criteria.getValidTo() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getValidTo());
                specification = specification.and(buildRangeSpecification(criteria.getValidTo(), FspEntity_.validTo));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), FspEntity_.active));
            }
            if (criteria.getOwnerId() != null) {
                specification = specification.and(buildSpecification(criteria.getOwnerId(),
                    root -> root.join(FspEntity_.owner, JoinType.LEFT).get(UserEntity_.id)));
            }
            if (criteria.getRepresentativeCompanyName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getRepresentativeCompanyName(), FspEntity_.companyName));
            }
            if (criteria.getRepresentativeFirstName() != null) {
                specification = specification.and(buildSpecification(criteria.getRepresentativeFirstName(),
                    root -> root.join(FspEntity_.owner, JoinType.LEFT).get(UserEntity_.firstName)));
            }
            if (criteria.getRepresentativeLastName() != null) {
                specification = specification.and(buildSpecification(criteria.getRepresentativeLastName(),
                    root -> root.join(FspEntity_.owner, JoinType.LEFT).get(UserEntity_.lastName)));
            }
            if (criteria.getRepresentativeEmail() != null) {
                specification = specification.and(buildSpecification(criteria.getRepresentativeEmail(),
                    root -> root.join(FspEntity_.owner, JoinType.LEFT).get(UserEntity_.email)));
            }
            if (criteria.getRepresentativePhoneNumber() != null) {
                specification = specification.and(buildSpecification(criteria.getRepresentativePhoneNumber(),
                    root -> root.join(FspEntity_.owner, JoinType.LEFT).get(UserEntity_.phoneNumber)));
            }
            if (criteria.getCreatedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getCreatedDate());
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), FspEntity_.createdDate));
            }
            if (criteria.getLastModifiedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getLastModifiedDate());
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), FspEntity_.lastModifiedDate));
            }
            if (criteria.getDeleted() != null) {
                specification = specification.and(buildSpecification(criteria.getDeleted(), FspEntity_.deleted));
            }
            if (criteria.getRole() != null) {
                specification = specification.and(buildSpecification(criteria.getRole(), FspEntity_.role));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return FspEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<FspEntity, Long> getRepository() {
        return this.fspRepository;
    }

    @Override
    public EntityMapper<FspDTO, FspEntity> getMapper() {
        return this.fspMapper;
    }
}
