package pl.com.tt.flex.server.service.auction.offer;

import io.github.jhipster.service.filter.InstantFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewEntity;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionOfferViewRepository;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferCriteria;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferViewCriteria;
import pl.com.tt.flex.server.service.auction.offer.mapper.AuctionOfferViewMapper;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

import java.util.List;

/**
 * Service for executing complex queries for {@link AuctionOfferViewEntity} entities in the database.
 * The main input is a {@link AuctionOfferCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AuctionOfferViewDTO} or a {@link Page} of {@link AuctionOfferViewDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuctionOfferViewQueryService extends AbstractQueryServiceImpl<AuctionOfferViewEntity, AuctionOfferViewDTO, Long, AuctionOfferViewCriteria> {

    private final AuctionOfferViewRepository offerViewRepository;

    private final AuctionOfferViewMapper offerViewMapper;

    public AuctionOfferViewQueryService(final AuctionOfferViewRepository offerViewRepository, final AuctionOfferViewMapper offerViewMapper) {
        this.offerViewRepository = offerViewRepository;
        this.offerViewMapper = offerViewMapper;
    }

    /**
     * Function to convert {@link AuctionOfferCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AuctionOfferViewEntity> createSpecification(AuctionOfferViewCriteria criteria) {
        Specification<AuctionOfferViewEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), AuctionOfferViewEntity_.id));
            }
            if (criteria.getAuctionId() != null) {
                specification = specification.and(buildSpecification(criteria.getAuctionId(), AuctionOfferViewEntity_.auctionId));
            }
            if (criteria.getVolumeDivisibility() != null) {
                specification = specification.and(buildSpecification(criteria.getVolumeDivisibility(), AuctionOfferViewEntity_.volumeDivisibility));
            }
            if (criteria.getDeliveryPeriod() != null) {
                specification = addSpecificationWithDeliveryPeriod(criteria.getDeliveryPeriod(), specification);
            }
            if (criteria.getDeliveryPeriodDivisibility() != null) {
                specification = specification.and(buildSpecification(criteria.getDeliveryPeriodDivisibility(), AuctionOfferViewEntity_.deliveryPeriodDivisibility));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), AuctionOfferViewEntity_.status));
            }
            if (criteria.getAuctionStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getAuctionStatus(), AuctionOfferViewEntity_.auctionStatus));
            }
            if (criteria.getPrice() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getPrice(), AuctionOfferViewEntity_.price));
            }
            if (criteria.getVolume() != null) {
                specification = specification.and(buildStringSpecification(criteria.getVolume(), AuctionOfferViewEntity_.volume));
            }
            if (criteria.getAcceptedVolume() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAcceptedVolume(), AuctionOfferViewEntity_.acceptedVolume));
            }
            if (criteria.getAuctionName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAuctionName(), AuctionOfferViewEntity_.auctionName));
            }
            if (criteria.getProductId() != null) {
                specification = specification.and(buildStringSpecification(criteria.getProductId(), AuctionOfferViewEntity_.productId));
            }
            if (criteria.getProductName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getProductName(), AuctionOfferViewEntity_.productName));
            }
            if (criteria.getCompanyName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCompanyName(), AuctionOfferViewEntity_.companyName));
            }
            if (criteria.getAcceptedDeliveryPeriod() != null) {
                specification = addSpecificationWithAcceptedDeliveryPeriod(criteria.getAcceptedDeliveryPeriod(), specification);
            }
            if (criteria.getFspId() != null) {
                specification = specification.and(buildSpecification(criteria.getFspId(), AuctionOfferViewEntity_.fspId));
            }
            if (criteria.getOfferCategory() != null) {
                specification = specification.and(buildSpecification(criteria.getOfferCategory(), AuctionOfferViewEntity_.offerCategory));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), AuctionOfferViewEntity_.type));
            }
            if (criteria.getAuctionCategoryAndType() != null) {
                specification = specification.and(buildSpecification(criteria.getAuctionCategoryAndType(), AuctionOfferViewEntity_.auctionCategoryAndType));
            }
            if (criteria.getSchedulingUnitOrPotential() != null) {
                specification = specification.and(buildStringSpecification(criteria.getSchedulingUnitOrPotential(), AuctionOfferViewEntity_.schedulingUnitOrPotential));
            }
        }
        return specification;
    }

    private Specification<AuctionOfferViewEntity> addSpecificationWithAcceptedDeliveryPeriod(InstantFilter acceptedDeliveryPeriodFilter, Specification<AuctionOfferViewEntity> specification) {
        if (acceptedDeliveryPeriodFilter.getGreaterThanOrEqual() != null || acceptedDeliveryPeriodFilter.getGreaterThan() != null || acceptedDeliveryPeriodFilter.getEquals() != null) {
            specification = specification.and(buildRangeSpecification(acceptedDeliveryPeriodFilter, AuctionOfferViewEntity_.acceptedDeliveryPeriodFrom));
        }
        if (acceptedDeliveryPeriodFilter.getLessThanOrEqual() != null || acceptedDeliveryPeriodFilter.getLessThan() != null) {
            specification = specification.and(buildRangeSpecification(acceptedDeliveryPeriodFilter, AuctionOfferViewEntity_.acceptedDeliveryPeriodTo));
        }
        return specification;
    }

    private Specification<AuctionOfferViewEntity> addSpecificationWithDeliveryPeriod(InstantFilter deliveryPeriodFilter, Specification<AuctionOfferViewEntity> specification) {
        if (deliveryPeriodFilter.getGreaterThanOrEqual() != null || deliveryPeriodFilter.getGreaterThan() != null || deliveryPeriodFilter.getEquals() != null) {
            specification = specification.and(buildRangeSpecification(deliveryPeriodFilter, AuctionOfferViewEntity_.deliveryPeriodFrom));
        }
        if (deliveryPeriodFilter.getLessThanOrEqual() != null || deliveryPeriodFilter.getLessThan() != null) {
            specification = specification.and(buildRangeSpecification(deliveryPeriodFilter, AuctionOfferViewEntity_.deliveryPeriodTo));
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return AuctionOfferViewEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<AuctionOfferViewEntity, Long> getRepository() {
        return this.offerViewRepository;
    }

    @Override
    public EntityMapper<AuctionOfferViewDTO, AuctionOfferViewEntity> getMapper() {
        return this.offerViewMapper;
    }
}
