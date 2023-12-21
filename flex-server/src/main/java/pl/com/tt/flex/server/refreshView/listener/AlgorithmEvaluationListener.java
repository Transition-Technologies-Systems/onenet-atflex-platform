package pl.com.tt.flex.server.refreshView.listener;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationViewDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferMinDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelMinimalDTO;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.repository.algorithm.AlgorithmEvaluationRepository;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcOfferMapper;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadOfferMapper;
import pl.com.tt.flex.server.web.rest.algorithm.resource.FlexAgnoAlgorithmResource;
import pl.com.tt.flex.server.web.rest.websocket.FlexAdminRefreshViewWebsocketResource;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;

@Component
@Slf4j
public class AlgorithmEvaluationListener {

    private final FlexAdminRefreshViewWebsocketResource websocket;
    private final FlexAgnoAlgorithmResource flexAgnoAlgorithmResource;
    private final AuctionDayAheadOfferMapper auctionDaOfferMapper;
    private final AuctionCmvcOfferMapper auctionCmvcOfferMapper;
    private final AlgorithmEvaluationRepository algorithmEvaluationRepository;

    public AlgorithmEvaluationListener(@Lazy FlexAdminRefreshViewWebsocketResource adminRefreshViewWebsocketResource, @Lazy FlexAgnoAlgorithmResource flexAgnoAlgorithmResource,
                                       @Lazy AuctionDayAheadOfferMapper auctionDaOfferMapper, @Lazy AuctionCmvcOfferMapper auctionCmvcOfferMapper,
                                       @Lazy AlgorithmEvaluationRepository algorithmEvaluationRepository) {
        this.websocket = adminRefreshViewWebsocketResource;
        this.flexAgnoAlgorithmResource = flexAgnoAlgorithmResource;
        this.auctionDaOfferMapper = auctionDaOfferMapper;
        this.auctionCmvcOfferMapper = auctionCmvcOfferMapper;
        this.algorithmEvaluationRepository = algorithmEvaluationRepository;
    }

    @PostPersist
    @Async
    public void onPostPersist(AlgorithmEvaluationEntity algorithmEvaluationEntity) {
        log.info("onPostPersist() START - Send WebSocket messages with new AlgorithmEvaluation [evaluationID={}]", algorithmEvaluationEntity.getId());
        postAlgEvaluationToAdminApp(algorithmEvaluationEntity);
        log.info("onPostPersist() END - Send WebSocket messages with new AlgorithmEvaluation [evaluationID={}]", algorithmEvaluationEntity.getId());
    }

    @PostUpdate
    @Async
    public void onPostUpdate(AlgorithmEvaluationEntity algorithmEvaluationEntity) {
        log.info("onPostUpdate() START - Send WebSocket messages with updated AlgorithmEvaluation [evaluationID={}]", algorithmEvaluationEntity.getId());
        postAlgEvaluationToAdminApp(algorithmEvaluationEntity);
        log.info("onPostUpdate() END - Send WebSocket messages with updated AlgorithmEvaluation [evaluationID={}]", algorithmEvaluationEntity.getId());
    }

    private void postAlgEvaluationToAdminApp(AlgorithmEvaluationEntity algorithmEvaluationEntity) {
        try {
            AlgorithmEvaluationViewDTO algorithmEvaluationViewDTO = entityToViewDTO(algorithmEvaluationEntity);
            websocket.postModifiedAlgorithmEvaluation(algorithmEvaluationViewDTO);
        } catch (Exception e) {
            log.debug("postAlgEvaluationToAdminApp() Error while post AlgEvaluation id:{} to {} app\n{}", algorithmEvaluationEntity.getId(), Constants.FLEX_ADMIN_APP_NAME, e.getMessage());
        }
    }

    private AlgorithmEvaluationViewDTO entityToViewDTO(AlgorithmEvaluationEntity algorithmEvaluationEntity) {
        AlgorithmEvaluationViewDTO algorithmEvaluationViewDTO = new AlgorithmEvaluationViewDTO();
        algorithmEvaluationViewDTO.setEvaluationId(algorithmEvaluationEntity.getId());
        algorithmEvaluationViewDTO.setCreationDate(algorithmEvaluationEntity.getCreatedDate());
        algorithmEvaluationViewDTO.setTypeOfAlgorithm(algorithmEvaluationEntity.getTypeOfAlgorithm());
        algorithmEvaluationViewDTO.setDeliveryDate(algorithmEvaluationEntity.getDeliveryDate());
        algorithmEvaluationViewDTO.setEndDate(algorithmEvaluationEntity.getEndDate());
        KdmModelMinimalDTO kdmModelMinimalDTO = flexAgnoAlgorithmResource.getKdmFileMinimal(algorithmEvaluationEntity.getKdmModelId())
            .orElseThrow(() -> new IllegalStateException("Cannot find kdmModel with id: " + algorithmEvaluationEntity.getKdmModelId()));
        algorithmEvaluationViewDTO.setKdmModelName(kdmModelMinimalDTO.getAreaName());
        algorithmEvaluationViewDTO.setStatus(algorithmEvaluationEntity.getAlgorithmStatus());
        algorithmEvaluationViewDTO.setOffers(getEvaluationOffers(algorithmEvaluationEntity.getId()));
        return algorithmEvaluationViewDTO;
    }

    private List<AuctionOfferMinDTO> getEvaluationOffers(Long algorithmId) {
        List<AuctionOfferMinDTO> daOffers = algorithmEvaluationRepository.findDaOffersByAlgorithmEvaluationId(algorithmId).stream().map(auctionDaOfferMapper::toOfferMinDto).collect(Collectors.toList());
        List<AuctionOfferMinDTO> cmvcOffers = algorithmEvaluationRepository.findCmvcOffersByAlgorithmEvaluationId(algorithmId).stream().map(auctionCmvcOfferMapper::toOfferMinDto).collect(Collectors.toList());;
        daOffers.addAll(cmvcOffers);
        return daOffers;
    }
}
