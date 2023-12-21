package pl.com.tt.flex.server.service.product.forecastedPrices;

import java.time.Instant;
import java.util.List;

import javax.persistence.criteria.JoinType;
import javax.persistence.metamodel.SingularAttribute;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;
import io.github.jhipster.service.filter.InstantFilter;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.common.specification.SpecificationCombiner;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity_;
import pl.com.tt.flex.server.domain.product.ForecastedPricesEntity;
import pl.com.tt.flex.server.domain.product.ForecastedPricesEntity_;
import pl.com.tt.flex.server.domain.product.ProductEntity_;
import pl.com.tt.flex.server.repository.product.forecastedPrices.ForecastedPricesRepository;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesCriteria;
import pl.com.tt.flex.server.service.product.forecastedPrices.dto.ForecastedPricesDTO;
import pl.com.tt.flex.server.service.product.forecastedPrices.mapper.ForecastedPricesMapper;
import pl.com.tt.flex.server.util.CriteriaUtils;


/**
 * Service for executing complex queries for {@link ForecastedPricesEntity} entities in the database.
 * The main input is a {@link ForecastedPricesCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link ForecastedPricesDTO} or a {@link Page} of {@link ForecastedPricesDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class ForecastedPricesQueryService extends QueryService<ForecastedPricesEntity> {

    private final ForecastedPricesRepository forecastedPricesRepository;

    private final ForecastedPricesMapper forecastedPricesMapper;

    public ForecastedPricesQueryService(final ForecastedPricesRepository forecastedPricesRepository, final ForecastedPricesMapper forecastedPricesMapper) {
        this.forecastedPricesRepository = forecastedPricesRepository;
        this.forecastedPricesMapper = forecastedPricesMapper;
    }

    /**
     * Return a {@link List} of {@link ForecastedPricesDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ForecastedPricesDTO> findByCriteria(ForecastedPricesCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<ForecastedPricesEntity> specification = createSpecification(criteria);
        return forecastedPricesMapper.toDto(forecastedPricesRepository.findAll(specification));
    }


    /**
     * Return a {@link Page} of {@link ForecastedPricesDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ForecastedPricesDTO> findByCriteria(ForecastedPricesCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<ForecastedPricesEntity> specification = createSpecification(criteria);
        return forecastedPricesRepository.findAll(specification, page)
            .map(forecastedPricesMapper::toDto);
    }

    /**
     * Return a {@link List} of {@link ForecastedPricesDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param sort     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<ForecastedPricesDTO> findByCriteria(ForecastedPricesCriteria criteria, Sort sort) {
        log.debug("find by criteria : {}, sort: {}", criteria, sort);
        final Specification<ForecastedPricesEntity> specification = createSpecification(criteria);
        return forecastedPricesMapper.toDto(forecastedPricesRepository.findAll(specification, sort));
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ForecastedPricesCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<ForecastedPricesEntity> specification = createSpecification(criteria);
        return forecastedPricesRepository.count(specification);
    }

    /**
     * Function to convert {@link ForecastedPricesCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<ForecastedPricesEntity> createSpecification(ForecastedPricesCriteria criteria) {
        return new SpecificationCombiner<ForecastedPricesEntity>(criteria)
            .and(criteria::getId, ForecastedPricesEntity_.id, this::buildRangeSpecification)
            .and(criteria::getCreatedBy, AbstractAuditingEntity_.createdBy, this::buildStringSpecification)
            .and(criteria::getCreatedDate, AbstractAuditingEntity_.createdDate, this::buildInstantFilterRangeSpecification)
            .and(criteria::getLastModifiedBy, AbstractAuditingEntity_.lastModifiedBy, this::buildStringSpecification)
            .and(criteria::getLastModifiedDate, AbstractAuditingEntity_.lastModifiedDate, this::buildInstantFilterRangeSpecification)
            .and(criteria::getForecastedPricesDate, ForecastedPricesEntity_.forecastedPricesDate, this::buildInstantFilterRangeSpecification)
            .and(criteria::getProductId, longFilter -> buildSpecification(criteria.getProductId(),
                root -> root.join(ForecastedPricesEntity_.product, JoinType.LEFT).get(ProductEntity_.id)))
            .combine();
    }

    private Specification<ForecastedPricesEntity> buildInstantFilterRangeSpecification(InstantFilter instantFilter,
                                                                                       SingularAttribute<? super ForecastedPricesEntity, Instant> instantField) {
        CriteriaUtils.setOnlyDayIfEqualsFilter(instantFilter);
        return buildRangeSpecification(instantFilter, instantField);
    }


}
