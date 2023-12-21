package pl.com.tt.flex.server.service.product;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity_;
import pl.com.tt.flex.server.domain.user.UserEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.product.ProductRepository;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.product.dto.ProductCriteria;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;

import javax.persistence.criteria.JoinType;
import java.util.List;

/**
 * Service for executing complex queries for {@link ProductEntity} entities in the database.
 * The main input is a {@link ProductCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ProductDTO} or a {@link Page} of {@link ProductDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ProductQueryService extends AbstractQueryServiceImpl<ProductEntity, ProductDTO, Long, ProductCriteria> {

    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    public ProductQueryService(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    public List<ProductMinDTO> findMinByCriteria(ProductCriteria criteria) {
        log.debug("find min by criteria : {}", criteria);
        final Specification<ProductEntity> specification = createSpecification(criteria);
        return productMapper.toMinDto(productRepository.findAll(specification));
    }

    /**
     * Return a {@link List} of {@link ProductDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param sort     The sorting parameters.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> findByCriteria(ProductCriteria criteria, Sort sort) {
        log.debug("find by criteria : {}, sort: {}", criteria, sort);
        final Specification<ProductEntity> specification = createSpecification(criteria);
        return productMapper.toDto(productRepository.findAll(specification, sort));
    }

    /**
     * Function to convert {@link ProductCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ProductEntity> createSpecification(ProductCriteria criteria) {
        Specification<ProductEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), ProductEntity_.id));
            }
            if (criteria.getFullName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getFullName(), ProductEntity_.fullName));
            }
            if (criteria.getShortName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getShortName(), ProductEntity_.shortName));
            }
            if (criteria.getLocational() != null) {
                specification = specification.and(buildSpecification(criteria.getLocational(), ProductEntity_.locational));
            }
            if (criteria.getMinBidSize() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMinBidSize(), ProductEntity_.minBidSize));
            }
            if (criteria.getMaxBidSize() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMaxBidSize(), ProductEntity_.maxBidSize));
            }
            if (criteria.getBidSizeUnit() != null) {
                specification = specification.and(buildSpecification(criteria.getBidSizeUnit(), ProductEntity_.bidSizeUnit));
            }
            if (criteria.getMaxFullActivationTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMaxFullActivationTime(), ProductEntity_.maxFullActivationTime));
            }
            if (criteria.getMinRequiredDeliveryDuration() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMinRequiredDeliveryDuration(), ProductEntity_.minRequiredDeliveryDuration));
            }
            if (criteria.getActive() != null) {
                specification = specification.and(buildSpecification(criteria.getActive(), ProductEntity_.active));
            }
            if (criteria.getValidFrom() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getValidFrom());
                specification = specification.and(buildRangeSpecification(criteria.getValidFrom(), ProductEntity_.validFrom));
            }
            if (criteria.getValidTo() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getValidTo());
                specification = specification.and(buildRangeSpecification(criteria.getValidTo(), ProductEntity_.validTo));
            }
            if (criteria.getVersion() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getVersion(), ProductEntity_.version));
            }
            if (criteria.getPsoUserId() != null) {
                specification = specification.and(buildSpecification(criteria.getPsoUserId(), root -> root.join(ProductEntity_.psoUser, JoinType.LEFT).get(UserEntity_.id)));
            }
            if (criteria.getSsoUserId() != null) {
                specification = specification.and(buildSpecification(criteria.getSsoUserId(), root -> root.join(ProductEntity_.ssoUsers, JoinType.LEFT).get(UserEntity_.id)));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), ProductEntity_.createdBy));
            }
            if (criteria.getCreatedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getCreatedDate());
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), ProductEntity_.createdDate));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), ProductEntity_.lastModifiedBy));
            }
            if (criteria.getLastModifiedDate() != null) {
                CriteriaUtils.setOnlyDayIfEqualsFilter(criteria.getLastModifiedDate());
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), ProductEntity_.lastModifiedDate));
            }
            if (criteria.getBalancing() != null) {
                specification = specification.and(buildSpecification(criteria.getBalancing(), ProductEntity_.balancing));
            }
            if (criteria.getCmvc() != null) {
                specification = specification.and(buildSpecification(criteria.getCmvc(), ProductEntity_.cmvc));
            }
            if (criteria.getDirection() != null) {
                specification = specification.and(buildSpecification(criteria.getDirection(), ProductEntity_.direction));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return ProductEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<ProductEntity, Long> getRepository() {
        return this.productRepository;
    }

    @Override
    public EntityMapper<ProductDTO, ProductEntity> getMapper() {
        return this.productMapper;
    }
}
