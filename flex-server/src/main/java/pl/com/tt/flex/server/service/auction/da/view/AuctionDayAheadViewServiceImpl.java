package pl.com.tt.flex.server.service.auction.da.view;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadViewEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.auction.da.AuctionDayAheadViewRepository;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadViewMapper;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

/**
 * Service Implementation for managing {@link AuctionDayAheadViewEntity}.
 */
@Slf4j
@Service
@Transactional
public class AuctionDayAheadViewServiceImpl extends AbstractServiceImpl<AuctionDayAheadViewEntity, AuctionDayAheadDTO, Long> implements AuctionDayAheadViewService {

    private final AuctionDayAheadViewRepository repository;

    private final AuctionDayAheadViewMapper mapper;

    public AuctionDayAheadViewServiceImpl(final AuctionDayAheadViewRepository repository, final AuctionDayAheadViewMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AbstractJpaRepository<AuctionDayAheadViewEntity, Long> getRepository() {
        return repository;
    }

    @Override
    public EntityMapper<AuctionDayAheadDTO, AuctionDayAheadViewEntity> getMapper() {
        return mapper;
    }
}
