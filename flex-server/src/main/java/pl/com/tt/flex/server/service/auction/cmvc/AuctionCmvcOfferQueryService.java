package pl.com.tt.flex.server.service.auction.cmvc;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.JoinType;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.filter.LongFilter;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferMinDTO;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity_;
import pl.com.tt.flex.server.domain.auction.offer.AbstractAuctionOffer_;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity_;
import pl.com.tt.flex.server.domain.fsp.FspEntity_;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity_;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.UserEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionCmvcOfferRepository;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcOfferMapper;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferCriteria;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.UserService;

/**
 * Service for executing complex queries for {@link AuctionCmvcOfferEntity} entities in the database.
 * The main input is a {@link AuctionOfferCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AuctionOfferDTO} or a {@link Page} of {@link AuctionOfferDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuctionCmvcOfferQueryService extends AbstractQueryServiceImpl<AuctionCmvcOfferEntity, AuctionOfferDTO, Long, AuctionOfferCriteria> {

    private final AuctionCmvcOfferRepository offerRepository;

    private final AuctionCmvcOfferMapper offerMapper;

    private final UserService userService;

    public AuctionCmvcOfferQueryService(final AuctionCmvcOfferRepository offerRepository, final AuctionCmvcOfferMapper offerMapper, UserService userService) {
        this.offerRepository = offerRepository;
        this.offerMapper = offerMapper;
        this.userService = userService;
    }

    public List<AuctionOfferMinDTO> findOffersByAuctionIdAndCurrentLoggedUserType(Long auctionId) {
        UserEntity user = userService.getCurrentUser();
        var auctionIdFilter = (LongFilter) new LongFilter().setEquals(auctionId);
        var specification = Specification.where(buildSpecification(auctionIdFilter,
            root -> root.join(AuctionCmvcOfferEntity_.auctionCmvc, JoinType.LEFT)
                .get(AuctionCmvcOfferEntity_.ID)));
        if (user.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER) || user.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            var userIdFilter = (LongFilter) new LongFilter().setEquals(user.getId());
            specification = specification.and(buildSpecification(userIdFilter,
                root -> root.join(AuctionCmvcOfferEntity_.flexPotential, JoinType.LEFT)
                    .join(FlexPotentialEntity_.FSP, JoinType.LEFT)
                    .join(FspEntity_.USERS, JoinType.INNER)
                    .get(UserEntity_.ID)));
        }
        return offerRepository.findAll(specification).stream()
            .map(offerMapper::toOfferMinDto)
            .collect(Collectors.toList());
    }

    /**
     * Function to convert {@link AuctionOfferCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AuctionCmvcOfferEntity> createSpecification(AuctionOfferCriteria criteria) {
        Specification<AuctionCmvcOfferEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), AuctionCmvcOfferEntity_.id));
            }
            if (criteria.getPrice() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getPrice(), AuctionCmvcOfferEntity_.price));
            }
            if (criteria.getVolume() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getVolume(), AuctionCmvcOfferEntity_.volume));
            }
            if (criteria.getVolumeDivisibility() != null) {
                specification = specification.and(buildSpecification(criteria.getVolumeDivisibility(), AbstractAuctionOffer_.volumeDivisibility));
            }
            if (criteria.getDeliveryPeriodDivisibility() != null) {
                specification = specification.and(buildSpecification(criteria.getDeliveryPeriodDivisibility(), AbstractAuctionOffer_.deliveryPeriodDivisibility));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), AbstractAuctionOffer_.status));
            }
            if (criteria.getFlexPotentialCompanyName() != null) {
                specification = specification.and(buildSpecification(criteria.getFlexPotentialCompanyName(),
                    root -> root.join(AuctionCmvcOfferEntity_.flexPotential, JoinType.LEFT).join(FlexPotentialEntity_.fsp, JoinType.LEFT).get(FspEntity_.companyName)));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), AbstractAuctionOffer_.type));
            }
            if (criteria.getAuctionCmvc() != null) {
                specification = specification.and(buildSpecification(criteria.getAuctionCmvc(),
                    root -> root.join(AuctionCmvcOfferEntity_.auctionCmvc, JoinType.LEFT).get(AuctionCmvcEntity_.id)));
            }
            if (criteria.getFspId() != null) {
                specification = specification.and(buildSpecification(criteria.getFspId(),
                    root -> root.join(AuctionCmvcOfferEntity_.flexPotential, JoinType.LEFT).join(FlexPotentialEntity_.fsp, JoinType.LEFT).get(FspEntity_.id)));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return AuctionCmvcOfferEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<AuctionCmvcOfferEntity, Long> getRepository() {
        return this.offerRepository;
    }

    @Override
    public EntityMapper<AuctionOfferDTO, AuctionCmvcOfferEntity> getMapper() {
        return this.offerMapper;
    }
}
