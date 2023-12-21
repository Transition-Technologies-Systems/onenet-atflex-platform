package pl.com.tt.flex.server.service.auction.da;

import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadViewEntity;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadViewEntity_;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.auction.da.AuctionDayAheadViewRepository;
import pl.com.tt.flex.server.service.auction.da.dto.AuctionDayAheadCriteria;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadViewMapper;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.UserService;

/**
 * Service for executing complex queries for {@link AuctionDayAheadEntity} entities in the database.
 * The main input is a {@link AuctionDayAheadCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link AuctionDayAheadDTO} or a {@link Page} of {@link AuctionDayAheadDTO} which fulfills the criteria.
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class AuctionDayAheadQueryService extends AbstractQueryServiceImpl<AuctionDayAheadViewEntity, AuctionDayAheadDTO, Long, AuctionDayAheadCriteria> {

    private final AuctionDayAheadViewRepository auctionDayAheadViewRepository;

    private final AuctionDayAheadService auctionDayAheadService;

    private final AuctionDayAheadViewMapper auctionDayAheadViewMapper;

    private final AuctionDayAheadOfferQueryService auctionDayAheadOfferQueryService;

    private final UserService userService;

    public AuctionDayAheadQueryService(final AuctionDayAheadViewRepository auctionDayAheadViewRepository, final AuctionDayAheadService auctionDayAheadService,
                                       final AuctionDayAheadViewMapper auctionDayAheadViewMapper, final AuctionDayAheadOfferQueryService auctionDayAheadOfferQueryService,
                                       final UserService userService) {
        this.auctionDayAheadViewRepository = auctionDayAheadViewRepository;
        this.auctionDayAheadService = auctionDayAheadService;
        this.auctionDayAheadViewMapper = auctionDayAheadViewMapper;
        this.auctionDayAheadOfferQueryService = auctionDayAheadOfferQueryService;
        this.userService = userService;
    }

    /**
     * Return a {@link Page} of {@link AuctionDayAheadDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<AuctionDayAheadDTO> findByCriteria(AuctionDayAheadCriteria criteria, Pageable page) {
        Page<AuctionDayAheadDTO> auctionPage = super.findByCriteria(criteria, page);
        UserEntity user = userService.getCurrentUser();
        auctionPage.getContent().forEach(auctionDTO -> {
            //ustawienie flagi canAddBid (przycisk dodawania aukcji w oknie aukcji DA)
            if (auctionDayAheadService.canCurrentLoggedUserAddNewBid(auctionDTO)) {
                auctionDTO.setCanAddBid(true);
            }
            //ustawienie ofert do wyświetlenia w tooltipie
            auctionDTO.setOffers(auctionDayAheadOfferQueryService.findOffersByAuctionIdAndCurrentLoggedUserType(auctionDTO.getId()));

            // usunięcie ceny dla DSO
            if (user.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR)) {
                auctionDTO.getOffers().forEach(auctionOfferMinDTO -> auctionOfferMinDTO.setPrice(null));
            }
        });
        return auctionPage;
    }

    @Transactional(readOnly = true)
    public List<AuctionDayAheadViewEntity> findAllByStatusNewAndProductId(Long productId) {
        return auctionDayAheadViewRepository.findAllByStatusInAndProductId(Arrays.asList(AuctionStatus.NEW_CAPACITY, AuctionStatus.NEW_ENERGY, AuctionStatus.SCHEDULED), productId);
    }

    /**
     * Function to convert {@link AuctionDayAheadCriteria} to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<AuctionDayAheadViewEntity> createSpecification(AuctionDayAheadCriteria criteria) {
        Specification<AuctionDayAheadViewEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), AuctionDayAheadViewEntity_.id));
            }
            if (criteria.getName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getName(), AuctionDayAheadViewEntity_.name));
            }
            if (criteria.getAuctionType() != null) {
                specification = specification.and(buildSpecification(criteria.getAuctionType(), AuctionDayAheadViewEntity_.type));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), AuctionDayAheadViewEntity_.status));
            }
            if (criteria.getDay() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDay(), AuctionDayAheadViewEntity_.day));
            }
            if (criteria.getDeliveryDate() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getDeliveryDate(), AuctionDayAheadViewEntity_.deliveryDate));
            }
            if (criteria.getEnergyGateOpeningTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEnergyGateOpeningTime(), AuctionDayAheadViewEntity_.energyGateOpeningTime));
            }
            if (criteria.getEnergyGateClosureTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEnergyGateClosureTime(), AuctionDayAheadViewEntity_.energyGateClosureTime));
            }
            if (criteria.getCapacityGateOpeningTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCapacityGateOpeningTime(), AuctionDayAheadViewEntity_.capacityGateOpeningTime));
            }
            if (criteria.getCapacityGateClosureTime() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCapacityGateClosureTime(), AuctionDayAheadViewEntity_.capacityGateClosureTime));
            }
            if (criteria.getGateDate() != null) {
                specification = addSpecificationWithGateDate(criteria, specification);
            }
            if (criteria.getMinDesiredCapacity() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMinDesiredCapacity(), AuctionDayAheadViewEntity_.minDesiredCapacity));
            }
            if (criteria.getMaxDesiredCapacity() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMaxDesiredCapacity(), AuctionDayAheadViewEntity_.maxDesiredCapacity));
            }
            if (criteria.getMinDesiredEnergy() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMinDesiredEnergy(), AuctionDayAheadViewEntity_.minDesiredEnergy));
            }
            if (criteria.getMaxDesiredEnergy() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getMaxDesiredEnergy(), AuctionDayAheadViewEntity_.maxDesiredEnergy));
            }
            if (criteria.getCapacityAvailabilityFrom() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCapacityAvailabilityFrom(), AuctionDayAheadViewEntity_.capacityAvailabilityFrom));
            }
            if (criteria.getCapacityAvailabilityTo() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getCapacityAvailabilityTo(), AuctionDayAheadViewEntity_.capacityAvailabilityTo));
            }
            if (criteria.getEnergyAvailabilityFrom() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEnergyAvailabilityFrom(), AuctionDayAheadViewEntity_.energyAvailabilityFrom));
            }
            if (criteria.getEnergyAvailabilityTo() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getEnergyAvailabilityTo(), AuctionDayAheadViewEntity_.energyAvailabilityTo));
            }
            if (criteria.getProductName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getProductName(), AuctionDayAheadViewEntity_.productName));
            }
            if (criteria.getProductId() != null) {
                specification = specification.and(buildSpecification(criteria.getProductId(), AuctionDayAheadViewEntity_.productId));
            }
        }
        return specification;
    }

    private Specification<AuctionDayAheadViewEntity> addSpecificationWithGateDate(AuctionDayAheadCriteria criteria, Specification<AuctionDayAheadViewEntity> specification) {
        if (criteria.getGateDate().getGreaterThanOrEqual() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getGateDate(), AuctionDayAheadViewEntity_.energyGateOpeningTime)
                .or(buildRangeSpecification(criteria.getGateDate(), AuctionDayAheadViewEntity_.capacityGateOpeningTime)));
        }
        if (criteria.getGateDate().getLessThanOrEqual() != null) {
            specification = specification.and(buildRangeSpecification(criteria.getGateDate(), AuctionDayAheadViewEntity_.energyGateClosureTime)
                .or(buildRangeSpecification(criteria.getGateDate(), AuctionDayAheadViewEntity_.capacityGateClosureTime)));
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return AuctionDayAheadViewEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<AuctionDayAheadViewEntity, Long> getRepository() {
        return this.auctionDayAheadViewRepository;
    }

    @Override
    public EntityMapper<AuctionDayAheadDTO, AuctionDayAheadViewEntity> getMapper() {
        return this.auctionDayAheadViewMapper;
    }
}
