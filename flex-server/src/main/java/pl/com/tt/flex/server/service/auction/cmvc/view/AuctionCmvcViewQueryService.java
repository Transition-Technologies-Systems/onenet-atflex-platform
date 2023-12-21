package pl.com.tt.flex.server.service.auction.cmvc.view;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcViewEntity;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcViewEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.auction.cmvc.AuctionCmvcViewRepository;
import pl.com.tt.flex.server.service.auction.cmvc.AuctionCmvcOfferQueryService;
import pl.com.tt.flex.server.service.auction.cmvc.AuctionCmvcService;
import pl.com.tt.flex.server.service.auction.cmvc.dto.AuctionCmvcViewCriteria;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcViewMapper;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

/**
 * Service for executing complex queries for {@link AuctionCmvcViewEntity} entities in the database.
 * The main input is a {@link AuctionCmvcViewCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AuctionCmvcDTO} or a {@link Page} of {@link AuctionCmvcDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuctionCmvcViewQueryService extends AbstractQueryServiceImpl<AuctionCmvcViewEntity, AuctionCmvcDTO, Long, AuctionCmvcViewCriteria> {

    private final AuctionCmvcViewRepository auctionCmvcViewRepository;

    private final AuctionCmvcService auctionCmvcService;

    private final AuctionCmvcViewMapper auctionCmvcViewMapper;

    private final AuctionCmvcOfferQueryService auctionCmvcOfferQueryService;

    public AuctionCmvcViewQueryService(final AuctionCmvcViewRepository auctionCmvcViewRepository, final AuctionCmvcService auctionCmvcService,
                                       final AuctionCmvcOfferQueryService auctionCmvcOfferQueryService, final AuctionCmvcViewMapper auctionCmvcViewMapper) {
        this.auctionCmvcViewRepository = auctionCmvcViewRepository;
        this.auctionCmvcService = auctionCmvcService;
        this.auctionCmvcOfferQueryService = auctionCmvcOfferQueryService;
        this.auctionCmvcViewMapper = auctionCmvcViewMapper;
    }


    /**
     * Return a {@link Page} of {@link AuctionCmvcDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AuctionCmvcDTO> findByCriteria(AuctionCmvcViewCriteria criteria, Pageable page) {
        Page<AuctionCmvcDTO> auctionPage = super.findByCriteria(criteria, page);
        auctionPage.getContent().forEach(auctionDTO -> {
            //ustawienie flagi canAddBid (przycisk dodawania aukcji w oknie aukcji CMVC)
            if (auctionCmvcService.canCurrentLoggedUserAddNewBid(auctionDTO, Long.valueOf(auctionDTO.getProductId()))) {
                auctionDTO.setCanAddBid(true);
            }
            //ustawienie listy ofert do wyświetlenia w tooltipie
            auctionDTO.setOffers(auctionCmvcOfferQueryService.findOffersByAuctionIdAndCurrentLoggedUserType(auctionDTO.getId()));
        });

        return auctionPage;
    }

    @Transactional(readOnly = true)
    public List<AuctionCmvcViewEntity> findAllByStatusNewAndProductId(Long productId) {
        return auctionCmvcViewRepository.findAllByStatusAndProductId(AuctionStatus.NEW, productId);
    }

    /**
     * Function to convert {@link AuctionCmvcViewCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AuctionCmvcViewEntity> createSpecification(AuctionCmvcViewCriteria criteria) {
        Specification<AuctionCmvcViewEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), AuctionCmvcViewEntity_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), AuctionCmvcViewEntity_.name));
            }
            if (criteria.getLocalization() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLocalization(), AuctionCmvcViewEntity_.localization));
            }
            if (criteria.getDeliveryDateFrom() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDeliveryDateFrom(), AuctionCmvcViewEntity_.deliveryDateFrom));
            }
            if (criteria.getDeliveryDateTo() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDeliveryDateTo(), AuctionCmvcViewEntity_.deliveryDateTo));
            }
            if (criteria.getGateOpeningTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getGateOpeningTime(), AuctionCmvcViewEntity_.gateOpeningTime));
            }
            if (criteria.getGateClosureTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getGateClosureTime(), AuctionCmvcViewEntity_.gateClosureTime));
            }
            if (criteria.getMinDesiredPower() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMinDesiredPower(), AuctionCmvcViewEntity_.minDesiredPower));
            }
            if (criteria.getMaxDesiredPower() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMaxDesiredPower(), AuctionCmvcViewEntity_.maxDesiredPower));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), AuctionCmvcViewEntity_.status));
            }
            if (criteria.getProductName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getProductName(), AuctionCmvcViewEntity_.productName));
            }
            if (criteria.getCreatedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCreatedDate(), AuctionCmvcViewEntity_.createdDate));
            }
            if (criteria.getCreatedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getCreatedBy(), AuctionCmvcViewEntity_.createdBy));
            }
            if (criteria.getLastModifiedDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getLastModifiedDate(), AuctionCmvcViewEntity_.lastModifiedDate));
            }
            if (criteria.getLastModifiedBy() != null) {
                specification = specification.and(buildStringSpecification(criteria.getLastModifiedBy(), AuctionCmvcViewEntity_.lastModifiedBy));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return AuctionCmvcViewEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<AuctionCmvcViewEntity, Long> getRepository() {
        return this.auctionCmvcViewRepository;
    }

    @Override
    public EntityMapper<AuctionCmvcDTO, AuctionCmvcViewEntity> getMapper() {
        return this.auctionCmvcViewMapper;
    }
}
