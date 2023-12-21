package pl.com.tt.flex.server.repository.algorithm;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlgorithmEvaluationRepository extends AbstractJpaRepository<AlgorithmEvaluationEntity, Long> {

    @Query("SELECT o.id FROM AlgorithmEvaluationEntity ae JOIN ae.daOffers o WHERE ae.id = :algorithmEvaluationId")
    List<Long> findDaOfferIdsByAlgorithmEvaluationId(@Param(value = "algorithmEvaluationId") Long algorithmEvaluationId);

    @Query("SELECT o.id FROM AlgorithmEvaluationEntity ae JOIN ae.cmvcOffers o WHERE ae.id = :algorithmEvaluationId")
    List<Long> findCmvcOfferIdsByAlgorithmEvaluationId(@Param(value = "algorithmEvaluationId") Long algorithmEvaluationId);

    @Query("SELECT o FROM AlgorithmEvaluationEntity ae JOIN ae.daOffers o JOIN FETCH o.schedulingUnit su JOIN FETCH su.bsp WHERE ae.id = :algorithmEvaluationId")
    List<AuctionDayAheadOfferEntity> findDaOffersByAlgorithmEvaluationId(@Param(value = "algorithmEvaluationId") Long algorithmEvaluationId);

    @Query("SELECT o FROM AlgorithmEvaluationEntity ae JOIN ae.cmvcOffers o WHERE ae.id = :algorithmEvaluationId")
    List<AuctionCmvcOfferEntity> findCmvcOffersByAlgorithmEvaluationId(@Param(value = "algorithmEvaluationId") Long algorithmEvaluationId);

    @Query("SELECT ae FROM AlgorithmEvaluationEntity ae JOIN FETCH ae.daOffers o JOIN FETCH o.schedulingUnit su WHERE ae.id = :algorithmEvaluationId")
    Optional<AlgorithmEvaluationEntity> findByIdFetchDaOffersFetchSchedulingUnit(@Param(value = "algorithmEvaluationId") Long algorithmEvaluationId);

    @Query("SELECT ae FROM AlgorithmEvaluationEntity ae JOIN ae.daOffers o WHERE o.id = :offerId")
    List<AlgorithmEvaluationEntity> findAlgorithmEvaluationsByDaOfferId(@Param(value = "offerId") Long offerId);

    @Modifying
    @Query("UPDATE AlgorithmEvaluationEntity ae SET ae.processLogsZip = :file WHERE ae.id = :algorithmEvaluationId")
    void updateLogFile(@Param(value = "algorithmEvaluationId") Long algorithmEvaluationId, @Param("file") byte[] file);

    @Modifying
    @Query("UPDATE AlgorithmEvaluationEntity ae SET ae.algorithmStatus = :status WHERE ae.id = :algorithmEvaluationId")
    void updateStatus(@Param(value = "algorithmEvaluationId") Long algorithmEvaluationId, @Param("status") AlgorithmStatus status);

    List<AlgorithmEvaluationEntity> findByAlgorithmStatus(AlgorithmStatus status);
}
