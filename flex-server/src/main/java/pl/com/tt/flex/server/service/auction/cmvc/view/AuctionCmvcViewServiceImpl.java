package pl.com.tt.flex.server.service.auction.cmvc.view;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcViewEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.auction.cmvc.AuctionCmvcViewRepository;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcViewMapper;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

/**
 * Service Implementation for managing {@link AuctionCmvcViewEntity}.
 */
@Slf4j
@Service
@Transactional
public class AuctionCmvcViewServiceImpl extends AbstractServiceImpl<AuctionCmvcViewEntity, AuctionCmvcDTO, Long> implements AuctionCmvcViewService {

    private final AuctionCmvcViewRepository repository;

    private final AuctionCmvcViewMapper mapper;

    public AuctionCmvcViewServiceImpl(final AuctionCmvcViewRepository repository, final AuctionCmvcViewMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public AbstractJpaRepository<AuctionCmvcViewEntity, Long> getRepository() {
        return repository;
    }

    @Override
    public EntityMapper<AuctionCmvcDTO, AuctionCmvcViewEntity> getMapper() {
        return mapper;
    }
}
