package pl.com.tt.flex.server.service.auction.da.series;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionsSeriesDTO;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.auction.da.AuctionsSeriesRepository;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.auction.da.series.mapper.AuctionsSeriesMapper;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;
import pl.com.tt.flex.server.util.AuctionDayAheadDataUtil;
import pl.com.tt.flex.server.util.InstantUtil;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Service Implementation for managing {@link AuctionsSeriesEntity}.
 */
@Service
@Slf4j
@Transactional
public class AuctionsSeriesServiceImpl extends AbstractServiceImpl<AuctionsSeriesEntity, AuctionsSeriesDTO, Long> implements AuctionsSeriesService {

    private final AuctionsSeriesRepository auctionsSeriesRepository;

    private final AuctionsSeriesMapper auctionsSeriesMapper;

    private final ProductService productService;

    private final AuctionDayAheadService auctionDayAheadService;

    public AuctionsSeriesServiceImpl(AuctionsSeriesRepository auctionsSeriesRepository, AuctionsSeriesMapper auctionsSeriesMapper, @Lazy ProductService productService,
                                     @Lazy AuctionDayAheadService auctionDayAheadService) {
        this.auctionsSeriesRepository = auctionsSeriesRepository;
        this.auctionsSeriesMapper = auctionsSeriesMapper;
        this.productService = productService;
        this.auctionDayAheadService = auctionDayAheadService;
    }

    @Override
    @Transactional
    public AuctionsSeriesDTO save(AuctionsSeriesDTO auctionsSeriesDTO) {
        log.debug("Request to save AuctionsSeries : {}", auctionsSeriesDTO);
        auctionsSeriesDTO.setName(generateAuctionName(auctionsSeriesDTO));
        AuctionsSeriesEntity auctionsSeriesEntity = auctionsSeriesMapper.toEntity(auctionsSeriesDTO);
        auctionsSeriesEntity = auctionsSeriesRepository.save(auctionsSeriesEntity);
        log.debug("AuctionsSeries saved with id : {}", auctionsSeriesEntity.getId());
        if (auctionsSeriesDTO.getId() == null) {
            auctionDayAheadService.createDayAheadsForSeries(auctionsSeriesEntity);
        }
        if (auctionsSeriesDTO.getId() != null) {
            auctionDayAheadService.updateScheduledAuctions(auctionsSeriesEntity);
        }
        return auctionsSeriesMapper.toDto(auctionsSeriesEntity);
    }

    private String generateAuctionName(AuctionsSeriesDTO auctionsSeriesDTO) {
        if (Objects.nonNull(auctionsSeriesDTO.getName())) {
            return auctionsSeriesDTO.getName();
        }
        ProductNameMinDTO productNameMinDTO = productService.getProductShortName(auctionsSeriesDTO.getProduct().getId());
        return AuctionDayAheadDataUtil.generateAuctionSeriesName(productNameMinDTO.getName(), InstantUtil.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuctionsSeriesEntity> findAuctionsToCreate() {
        return auctionsSeriesRepository.findAuctionsToCreate(InstantUtil.now());
    }

    @Override
    @Transactional
    //Calkowicie Seria jest usuwana gdy nie rozpoczely sie jeszcze zadne aukcje.
    //Gdy sa aukcje juz rozpoczete/zakonczone zmieniana jest data ostatniej aukcji na zgodna z data ostatniej aukcji, a Seria nie jest usuwana.
    public void delete(Long id) {
        //Usuwanie aukcji DA z statusem NEW_CAPACITY, NEW_ENERGY lub SCHEDULED
        auctionsSeriesRepository.deleteNotStartedAuctionBySeriesId(id);
        AuctionsSeriesEntity auctionSeries = auctionsSeriesRepository.findById(id).get();
        //jeżeli istnieją otwarte lub zakonczone aukcje powstałe z Serii, to zmieniana jest data ostatniej auckji w Serii.
        if (!auctionsSeriesRepository.existAuctionOpenOrClosureFromSeriesId(id)) {
            super.delete(id);
            log.debug("delete() Delete series with id {} and all Series Auctions", id);
        } else {
            stopGeneratingNewAuction(auctionSeries);
            log.debug("delete() Delete not started auctions from series with id {}", id);
        }
    }

    private void stopGeneratingNewAuction(AuctionsSeriesEntity auctionsSeries) {
        Instant lastAuctionDate = auctionsSeriesRepository.findLastAuctionDateBySeriesId(auctionsSeries.getId());
        auctionsSeries.setLastAuctionDate(lastAuctionDate);
        log.debug("stopGeneratingNewAuction() Changed LastAuctionDate to [{}] in Series with id {}", lastAuctionDate, auctionsSeries.getId());
    }

    /**
     * Jeśli istnieje otwarta lub zamknięta aukcja DayAhead wygenerowana z serii,
     * wtedy nie mozna usunąć serii.
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canDeleteAuctionSeries(Long seriesId) {
        return !auctionsSeriesRepository.existAuctionOpenOrClosureFromSeriesId(seriesId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuctionsSeriesEntity> findAllByProductId(Long productId) {
        return auctionsSeriesRepository.findAllByProductId(productId);
    }

    @Override
    @Transactional
    public void updateAuctionName(String auctionName, Long id) {
        auctionsSeriesRepository.updateAuctionName(auctionName, id);
    }

    @Override
    public AbstractJpaRepository<AuctionsSeriesEntity, Long> getRepository() {
        return this.auctionsSeriesRepository;
    }

    @Override
    public EntityMapper<AuctionsSeriesDTO, AuctionsSeriesEntity> getMapper() {
        return this.auctionsSeriesMapper;
    }
}
