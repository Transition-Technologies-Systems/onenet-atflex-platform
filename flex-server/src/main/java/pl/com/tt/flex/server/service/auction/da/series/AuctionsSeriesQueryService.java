package pl.com.tt.flex.server.service.auction.da.series;

import static org.apache.commons.collections4.CollectionUtils.emptyIfNull;

import java.util.List;

import javax.persistence.criteria.JoinType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.QueryService;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionsSeriesDTO;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity_;
import pl.com.tt.flex.server.domain.product.ProductEntity_;
import pl.com.tt.flex.server.repository.auction.da.AuctionsSeriesRepository;
import pl.com.tt.flex.server.service.auction.da.series.dto.AuctionsSeriesCriteria;
import pl.com.tt.flex.server.service.auction.da.series.mapper.AuctionsSeriesMapper;

/**
 * Service for executing complex queries for {@link AuctionsSeriesEntity} entities in the database.
 * The main input is a {@link AuctionsSeriesCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AuctionsSeriesDTO} or a {@link Page} of {@link AuctionsSeriesDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuctionsSeriesQueryService extends QueryService<AuctionsSeriesEntity> {

    private final AuctionsSeriesRepository auctionsSeriesRepository;

    private final AuctionsSeriesMapper auctionsSeriesMapper;

    private final AuctionsSeriesService auctionsSeriesService;

    public AuctionsSeriesQueryService(final AuctionsSeriesRepository auctionsSeriesRepository, final AuctionsSeriesMapper auctionsSeriesMapper,
                                      final AuctionsSeriesService auctionsSeriesService) {
        this.auctionsSeriesRepository = auctionsSeriesRepository;
        this.auctionsSeriesMapper = auctionsSeriesMapper;
        this.auctionsSeriesService = auctionsSeriesService;
    }

    /**
     * Return a {@link List} of {@link AuctionsSeriesDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<AuctionsSeriesDTO> findByCriteria(AuctionsSeriesCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<AuctionsSeriesEntity> specification = createSpecification(criteria);
        List<AuctionsSeriesDTO> seriesDTOS = auctionsSeriesMapper.toDto(auctionsSeriesRepository.findAll(specification));
        //Ustawianie flagi 'deletable'
        emptyIfNull(seriesDTOS).forEach(series -> series.setDeletable(auctionsSeriesService.canDeleteAuctionSeries(series.getId())));
        return seriesDTOS;
    }

    /**
     * Return a {@link Page} of {@link AuctionsSeriesDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AuctionsSeriesDTO> findByCriteria(AuctionsSeriesCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<AuctionsSeriesEntity> specification = createSpecification(criteria);
        Page<AuctionsSeriesDTO> seriesPage = auctionsSeriesRepository.findAll(specification, page).map(auctionsSeriesMapper::toDto);
        //Ustawianie flagi 'deletable'
        emptyIfNull(seriesPage.getContent()).forEach(series -> series.setDeletable(auctionsSeriesService.canDeleteAuctionSeries(series.getId())));
        return seriesPage;
    }

    /**
     * Return the number of matching entities in the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(AuctionsSeriesCriteria criteria) {
        log.debug("count by criteria : {}", criteria);
        final Specification<AuctionsSeriesEntity> specification = createSpecification(criteria);
        return auctionsSeriesRepository.count(specification);
    }

    /**
     * Function to convert {@link AuctionsSeriesCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AuctionsSeriesEntity> createSpecification(AuctionsSeriesCriteria criteria) {
        Specification<AuctionsSeriesEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), AuctionsSeriesEntity_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), AuctionsSeriesEntity_.name));
            }
            if (criteria.getProductName() != null) {
                specification = specification.and(buildSpecification(criteria.getProductName(), root -> root.join(AuctionsSeriesEntity_.product, JoinType.INNER).get(ProductEntity_.shortName)));
            }
            if (criteria.getProductId() != null) {
                specification = specification.and(buildSpecification(criteria.getProductId(), root -> root.join(AuctionsSeriesEntity_.product, JoinType.INNER).get(ProductEntity_.id)));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), AuctionsSeriesEntity_.type));
            }
            if (criteria.getEnergyGateOpeningTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEnergyGateOpeningTime(), AuctionsSeriesEntity_.energyGateOpeningTime));
            }
            if (criteria.getEnergyGateClosureTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEnergyGateClosureTime(), AuctionsSeriesEntity_.energyGateClosureTime));
            }
            if (criteria.getCapacityGateOpeningTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCapacityGateOpeningTime(), AuctionsSeriesEntity_.capacityGateOpeningTime));
            }
            if (criteria.getCapacityGateClosureTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCapacityGateClosureTime(), AuctionsSeriesEntity_.capacityGateClosureTime));
            }
            if (criteria.getMinDesiredCapacity() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMinDesiredCapacity(), AuctionsSeriesEntity_.minDesiredCapacity));
            }
            if (criteria.getMaxDesiredCapacity() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMaxDesiredCapacity(), AuctionsSeriesEntity_.maxDesiredCapacity));
            }
            if (criteria.getMinDesiredEnergy() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMinDesiredEnergy(), AuctionsSeriesEntity_.minDesiredEnergy));
            }
            if (criteria.getMaxDesiredEnergy() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMaxDesiredEnergy(), AuctionsSeriesEntity_.maxDesiredEnergy));
            }
            if (criteria.getCapacityAvailabilityFrom() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCapacityAvailabilityFrom(), AuctionsSeriesEntity_.capacityAvailabilityFrom));
            }
            if (criteria.getCapacityAvailabilityTo() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCapacityAvailabilityTo(), AuctionsSeriesEntity_.capacityAvailabilityTo));
            }
            if (criteria.getEnergyAvailabilityFrom() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEnergyAvailabilityFrom(), AuctionsSeriesEntity_.energyAvailabilityFrom));
            }
            if (criteria.getEnergyAvailabilityTo() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEnergyAvailabilityTo(), AuctionsSeriesEntity_.energyAvailabilityTo));
            }
            if (criteria.getFirstAuctionDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getFirstAuctionDate(), AuctionsSeriesEntity_.firstAuctionDate));
            }
            if (criteria.getLastAuctionDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLastAuctionDate(), AuctionsSeriesEntity_.lastAuctionDate));
            }
            if (criteria.getCreatedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), AuctionsSeriesEntity_.createdDate));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), AuctionsSeriesEntity_.createdBy));
            }
            if (criteria.getLastModifiedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), AuctionsSeriesEntity_.lastModifiedDate));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), AuctionsSeriesEntity_.lastModifiedBy));
            }
        }
        return specification;
    }
}
