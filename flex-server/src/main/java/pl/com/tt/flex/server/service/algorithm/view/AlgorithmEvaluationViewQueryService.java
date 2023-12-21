package pl.com.tt.flex.server.service.algorithm.view;

import static pl.com.tt.flex.server.service.common.QueryServiceUtil.setDefaultOrder;
import static pl.com.tt.flex.server.util.CriteriaUtils.setOnlyDayIfEqualsFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.filter.InstantFilter;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationViewDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferMinDTO;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationViewEntity;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationViewEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.algorithm.AlgorithmEvaluationViewRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionCmvcOfferRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.service.algorithm.view.dto.AlgorithmEvaluationViewCriteria;
import pl.com.tt.flex.server.service.algorithm.view.mapper.AlgorithmEvaluationViewMapper;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcOfferMapper;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadOfferMapper;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.user.UserService;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AlgorithmEvaluationViewQueryService extends AbstractQueryServiceImpl<AlgorithmEvaluationViewEntity, AlgorithmEvaluationViewDTO, Long, AlgorithmEvaluationViewCriteria> {

    private final AlgorithmEvaluationViewRepository algorithmEvaluationViewRepository;
    private final AlgorithmEvaluationViewMapper algorithmEvaluationMapper;
    private final AuctionDayAheadOfferMapper auctionDaOfferMapper;
    private final AuctionCmvcOfferMapper auctionCmvcOfferMapper;
    private final AuctionDayAheadOfferRepository auctionDaOfferRepository;
    private final AuctionCmvcOfferRepository auctionCmvcOfferRepository;
    private final UserService userService;
    public AlgorithmEvaluationViewQueryService(final AlgorithmEvaluationViewRepository algorithmEvaluationViewRepository, AuctionDayAheadOfferMapper auctionDaOfferMapper, AuctionCmvcOfferMapper auctionCmvcOfferMapper, final UserService userService,
                                               final AlgorithmEvaluationViewMapper algorithmEvaluationMapper, AuctionDayAheadOfferRepository auctionDaOfferRepository, AuctionCmvcOfferRepository auctionCmvcOfferRepository) {
        this.algorithmEvaluationViewRepository = algorithmEvaluationViewRepository;
        this.auctionDaOfferMapper = auctionDaOfferMapper;
        this.auctionCmvcOfferMapper = auctionCmvcOfferMapper;
        this.algorithmEvaluationMapper = algorithmEvaluationMapper;
        this.userService = userService;
        this.auctionDaOfferRepository = auctionDaOfferRepository;
        this.auctionCmvcOfferRepository = auctionCmvcOfferRepository;
    }

    @Transactional(readOnly = true)
    public Page<AlgorithmEvaluationViewDTO> findByCriteria(AlgorithmEvaluationViewCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<AlgorithmEvaluationViewEntity> specification = createSpecification(criteria);
        Pageable pageForUserLanguage = getPageForUserLanguage(page);
        pageForUserLanguage = setDefaultOrder(pageForUserLanguage, getDefaultOrderProperty());
        Page<AlgorithmEvaluationViewEntity> algorithmEntities = algorithmEvaluationViewRepository.findAll(specification, pageForUserLanguage);
        Page<AlgorithmEvaluationViewDTO> algorithmPage = algorithmEntities.map(algorithmEvaluationMapper::toDto);
        algorithmPage.getContent().forEach(algorithmDTO -> {
            //ustawienie ofert do wyświetlenia w tooltipie
            algorithmDTO.setOffers(getEvaluationOffers(algorithmDTO.getEvaluationId(), algorithmEntities));
        });
        return algorithmPage;
    }

    /**
     * Metoda zwraca listę MinDTO ofert biorących udział w obliczeniach algorytmu o podanym ID.
     */
    private List<AuctionOfferMinDTO> getEvaluationOffers(Long evaluationId, Page<AlgorithmEvaluationViewEntity> algorithmEntities) {
        AlgorithmEvaluationViewEntity algorithm = algorithmEntities.get().filter(alg -> alg.getId().equals(evaluationId)).findFirst().get();
        List<Long> daOfferIds;
        List<Long> cmvcOfferIds;
        List<AuctionOfferMinDTO> daOffers = new ArrayList<>();
        List<AuctionOfferMinDTO> cmvcOffers = new ArrayList<>();
        if(Objects.nonNull(algorithm.getDaOffers())){
            // Identyfikatory ofert przechowywane są w widoku w polu da_offers jako lista oddzielana przecinkiem (pole tekstowe).
            daOfferIds = Stream.of(algorithm.getDaOffers().split(",")).map(id -> Long.valueOf(id)).collect(Collectors.toList());
            daOffers = auctionDaOfferRepository.findAllById(daOfferIds).stream().map(offer -> auctionDaOfferMapper.toOfferMinDto(offer)).collect(Collectors.toList());
        }
        if(Objects.nonNull(algorithm.getCmvcOffers())) {
            // Identyfikatory ofert przechowywane są w widoku w polu cmvc_offers jako lista oddzielana przecinkiem (pole tekstowe).
            cmvcOfferIds = Stream.of(algorithm.getCmvcOffers().split(",")).map(id -> Long.valueOf(id)).collect(Collectors.toList());
            cmvcOffers = auctionCmvcOfferRepository.findAllById(cmvcOfferIds).stream().map(offer -> auctionCmvcOfferMapper.toOfferMinDto(offer)).collect(Collectors.toList());
        }
        daOffers.addAll(cmvcOffers);
        return daOffers;
    }

    protected Specification<AlgorithmEvaluationViewEntity> createSpecification(AlgorithmEvaluationViewCriteria criteria) {
        Specification<AlgorithmEvaluationViewEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), AlgorithmEvaluationViewEntity_.id));
            }
            if (criteria.getKdmModelName() != null) {
                specification = specification.and(buildStringSpecification(criteria.getKdmModelName(), AlgorithmEvaluationViewEntity_.kdmModelName));
            }
            if (criteria.getKdmModelId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getKdmModelId(), AlgorithmEvaluationViewEntity_.kdmModelId));
            }
            if (criteria.getTypeOfAlgorithm() != null) {
                specification = specification.and(buildSpecification(criteria.getTypeOfAlgorithm(), AlgorithmEvaluationViewEntity_.typeOfAlgorithm));
            }
            InstantFilter deliveryDayFilter = criteria.getDeliveryDate();
            if (deliveryDayFilter != null) {
                setOnlyDayIfEqualsFilter(deliveryDayFilter);
                specification = specification.and(buildRangeSpecification(deliveryDayFilter, AlgorithmEvaluationViewEntity_.deliveryDate));
            }
            InstantFilter creationDayFilter = criteria.getCreationDate();
            if (creationDayFilter != null) {
                setOnlyDayIfEqualsFilter(creationDayFilter);
                specification = specification.and(buildRangeSpecification(creationDayFilter, AlgorithmEvaluationViewEntity_.createdDate));
            }
            InstantFilter endDayFilter = criteria.getEndDate();
            if (endDayFilter != null) {
                setOnlyDayIfEqualsFilter(endDayFilter);
                specification = specification.and(buildRangeSpecification(endDayFilter, AlgorithmEvaluationViewEntity_.endDate));
            }
            if (criteria.getAlgorithmStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getAlgorithmStatus(), AlgorithmEvaluationViewEntity_.algorithmStatus));
            }
        }
        return specification;
    }

    private Pageable getPageForUserLanguage(Pageable page) {
        List<Order> orderList = new ArrayList<>();
        page.getSort().stream().forEach(order -> {
            if (order.getProperty().equals(AlgorithmEvaluationViewEntity_.TYPE_OF_ALGORITHM)) {
                Sort.Direction direction = order.getDirection();
                if (userService.getLangKeyForCurrentLoggedUser().equals("pl")) {
                    orderList.add(Order.by(AlgorithmEvaluationViewEntity_.TYPE_ORDER_PL).with(direction));
                } else {
                    orderList.add(Order.by(AlgorithmEvaluationViewEntity_.TYPE_ORDER_EN).with(direction));
                }
            } else {
                orderList.add(order);
            }
        });
        return PageRequest.of(page.getPageNumber(), page.getPageSize(), Sort.by(orderList));
    }

    @Override
    public String getDefaultOrderProperty() {
        return AlgorithmEvaluationViewEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<AlgorithmEvaluationViewEntity, Long> getRepository() {
        return this.algorithmEvaluationViewRepository;
    }

    @Override
    public EntityMapper<AlgorithmEvaluationViewDTO, AlgorithmEvaluationViewEntity> getMapper() {
        return this.algorithmEvaluationMapper;
    }
}
