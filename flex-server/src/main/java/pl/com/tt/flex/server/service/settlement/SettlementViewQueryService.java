package pl.com.tt.flex.server.service.settlement;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.server.domain.settlement.SettlementViewEntity;
import pl.com.tt.flex.server.domain.settlement.SettlementViewEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.settlement.SettlementViewRepository;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferCriteria.AuctionOfferStatusFilter;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.settlement.dto.SettlementCriteria;
import pl.com.tt.flex.server.service.settlement.dto.SettlementViewDTO;
import pl.com.tt.flex.server.service.settlement.mapper.SettlementViewMapper;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SettlementViewQueryService extends AbstractQueryServiceImpl<SettlementViewEntity, SettlementViewDTO, Long, SettlementCriteria> {

    private final SettlementViewRepository settlementViewRepository;
    private final SettlementViewMapper settlementViewMapper;

    @Transactional(readOnly = true)
    public List<SettlementViewDTO> findByCriteria(SettlementCriteria criteria, Sort sort) {
        log.debug("find by criteria : {}, sort: {}", criteria, sort);
        final Specification<SettlementViewEntity> specification = createSpecification(criteria);
        return settlementViewMapper.toDto(settlementViewRepository.findAll(specification, sort));
    }

    @Override
    protected Specification<SettlementViewEntity> createSpecification(SettlementCriteria criteria) {
        Specification<SettlementViewEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), SettlementViewEntity_.id));
            }
            if (criteria.getDerName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getDerName(), SettlementViewEntity_.derName));
            }
            if (criteria.getOfferId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getOfferId(), SettlementViewEntity_.offerId));
            }
            if (criteria.getAuctionName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getAuctionName(), SettlementViewEntity_.auctionName));
            }
            if (criteria.getCompanyName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCompanyName(), SettlementViewEntity_.companyName));
            }
            if (criteria.getBspCompanyName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getBspCompanyName(), SettlementViewEntity_.bspCompanyName));
            }
            if (criteria.getAcceptedDeliveryPeriodFrom() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAcceptedDeliveryPeriodFrom(), SettlementViewEntity_.acceptedDeliveryPeriodFrom));
            }
            if (criteria.getAcceptedDeliveryPeriodTo() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getAcceptedDeliveryPeriodTo(), SettlementViewEntity_.acceptedDeliveryPeriodTo));
            }
            if (criteria.getFspId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getFspId(), SettlementViewEntity_.fspId));
            }
            if (criteria.getOfferCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getOfferCreatedBy(), SettlementViewEntity_.offerCreatedBy));
            }
            if (criteria.getOfferCategory() != null) {
                specification = specification.and(buildSpecification(criteria.getOfferCategory(), SettlementViewEntity_.offerCategory));
            }
            specification = applyOfferStatusAcceptedFilter(specification);
        }
        return specification;
    }

    private Specification<SettlementViewEntity> applyOfferStatusAcceptedFilter(Specification<SettlementViewEntity> specification) {
        AuctionOfferStatusFilter offerStatusFilter = new AuctionOfferStatusFilter();
        offerStatusFilter.setEquals(AuctionOfferStatus.ACCEPTED);
        return specification.and(buildSpecification(offerStatusFilter, SettlementViewEntity_.offerStatus));
    }

    @Override
    public String getDefaultOrderProperty() {
        return SettlementViewEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<SettlementViewEntity, Long> getRepository() {
        return this.settlementViewRepository;
    }

    @Override
    public EntityMapper<SettlementViewDTO, SettlementViewEntity> getMapper() {
        return this.settlementViewMapper;
    }

}
