package pl.com.tt.flex.server.service.auction.da;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.criteria.JoinType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.filter.LongFilter;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferMinDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity_;
import pl.com.tt.flex.server.domain.auction.offer.AbstractAuctionOffer_;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity_;
import pl.com.tt.flex.server.domain.fsp.FspEntity_;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity_;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.UserEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadOfferMapper;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferCriteria;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.UserService;

/**
 * Service for executing complex queries for {@link AuctionDayAheadOfferEntity} entities in the database.
 * The main input is a {@link AuctionOfferCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AuctionDayAheadOfferDTO} or a {@link Page} of {@link AuctionOfferDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuctionDayAheadOfferQueryService extends AbstractQueryServiceImpl<AuctionDayAheadOfferEntity, AuctionOfferDTO, Long, AuctionOfferCriteria> {

    private final AuctionDayAheadOfferRepository offerRepository;

    private final AuctionDayAheadOfferMapper offerMapper;

    private final UserService userService;

    public AuctionDayAheadOfferQueryService(final AuctionDayAheadOfferRepository offerRepository, final AuctionDayAheadOfferMapper offerMapper, final UserService userService) {
        this.offerRepository = offerRepository;
        this.offerMapper = offerMapper;
        this.userService = userService;
    }

    @Override
    public Page<AuctionOfferDTO> findByCriteria(AuctionOfferCriteria criteria, Pageable page) {
        UserEntity user = userService.getCurrentUser();
        // ukrywamy ceny dla oferty i dla band data w response dla użytkownika DSO
        if (user.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR)) {
            return super.findByCriteria(criteria, page).map(auctionOfferDTO -> {
                auctionOfferDTO.setPrice(null);
                auctionOfferDTO.getDers().forEach(auctionOfferDersDTO ->
                    auctionOfferDersDTO.getBandData().forEach(auctionOfferBandDataDTO -> {
                        auctionOfferBandDataDTO.setPrice(null);
                        auctionOfferBandDataDTO.setAcceptedPrice(null);
                    }));
                return auctionOfferDTO;
            });
        }
        return super.findByCriteria(criteria, page);
    }

    public List<AuctionOfferMinDTO> findOffersByAuctionIdAndCurrentLoggedUserType(Long auctionId) {
        UserEntity user = userService.getCurrentUser();
        var auctionIdFilter = (LongFilter) new LongFilter().setEquals(auctionId);
        var specification = Specification.where(buildSpecification(auctionIdFilter,
            root -> root.join(AuctionDayAheadOfferEntity_.auctionDayAhead, JoinType.LEFT).get(AuctionDayAheadEntity_.id)));
        if (user.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            var userIdFilter = (LongFilter) new LongFilter().setEquals(user.getId());
            specification = specification.and(buildSpecification(userIdFilter,
                root -> root.join(AuctionDayAheadOfferEntity_.schedulingUnit, JoinType.LEFT)
                    .join(SchedulingUnitEntity_.bsp, JoinType.LEFT)
                    .join(FspEntity_.USERS, JoinType.INNER)
                    .get(UserEntity_.ID)));
        }
        var criteria = new AuctionOfferCriteria();
        criteria.setAuctionId(auctionIdFilter);
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
    protected Specification<AuctionDayAheadOfferEntity> createSpecification(AuctionOfferCriteria criteria) {
        Specification<AuctionDayAheadOfferEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), AuctionDayAheadOfferEntity_.id));
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
            if (criteria.getSchedulingUnitCompanyName() != null) {
                specification = specification.and(buildSpecification(criteria.getSchedulingUnitCompanyName(),
                    root -> root.join(AuctionDayAheadOfferEntity_.schedulingUnit, JoinType.LEFT).join(SchedulingUnitEntity_.bsp, JoinType.LEFT).get(FspEntity_.companyName)));
            }
            if (criteria.getType() != null) {
                specification = specification.and(buildSpecification(criteria.getType(), AbstractAuctionOffer_.type));
            }
            if (criteria.getAuctionDayAhead() != null) {
                specification = specification.and(buildSpecification(criteria.getAuctionDayAhead(),
                    root -> root.join(AuctionDayAheadOfferEntity_.auctionDayAhead, JoinType.LEFT).get(AuctionDayAheadEntity_.id)));
            }
            if (criteria.getBspId() != null) {
                specification = specification.and(buildSpecification(criteria.getBspId(),
                    root -> root.join(AuctionDayAheadOfferEntity_.schedulingUnit, JoinType.LEFT).join(SchedulingUnitEntity_.bsp, JoinType.LEFT).get(FspEntity_.id)));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return AuctionDayAheadOfferEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<AuctionDayAheadOfferEntity, Long> getRepository() {
        return this.offerRepository;
    }

    @Override
    public EntityMapper<AuctionOfferDTO, AuctionDayAheadOfferEntity> getMapper() {
        return this.offerMapper;
    }
}
