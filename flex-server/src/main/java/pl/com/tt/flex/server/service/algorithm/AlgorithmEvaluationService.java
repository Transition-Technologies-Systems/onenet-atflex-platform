package pl.com.tt.flex.server.service.algorithm;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.com.tt.flex.model.service.dto.algorithm.AlgEvaluationModuleDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.file.FileContentDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferViewCriteria;
import pl.com.tt.flex.server.service.mail.dto.NotificationResultDTO;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface AlgorithmEvaluationService extends AbstractService<AlgorithmEvaluationEntity, AlgorithmEvaluationDTO, Long> {

    Page<AuctionOfferViewDTO> findOffersUsedInAlgorithmByCriteria(Long algorithmId, AuctionOfferViewCriteria criteria, Pageable pageable);

    AlgorithmEvaluationDTO saveDayAheadAlgorithmEvaluation(Instant deliveryDate, Set<AuctionDayAheadOfferDTO> offers, FileDTO inputFile, AlgorithmType algorithmType, Long kdmModelId);

    void saveAlgorithmResult(AlgEvaluationModuleDTO algEvaluationModuleDTO) throws IOException;

    List<AuctionOfferViewDTO> findOffersUsedInAlgorithmByCriteria(Long algorithmId, AuctionOfferViewCriteria criteria);

    AlgorithmEvaluationEntity findAlgorithmEvaluationEntityById(Long algorithmId);

    FileDTO findInputFilesZip(Long algorithmId) throws ObjectValidationException;

    FileDTO findOutputFilesZip(Long algorithmId) throws ObjectValidationException;

    List<FileContentDTO> findLogFiles(Long algorithmId) throws ObjectValidationException;

    void updateLogFile(Long algorithmId, FileDTO fileDTO);

    void updateStatus(Long evaluationId, AlgorithmStatus status);

    FileDTO generateAgnoResultsFile(Long algEvaluationId) throws IOException, ObjectValidationException;

    AlgorithmEvaluationEntity getAlgorithmEvaluationEntity(Long id);

    void cancelAlgorithm(long evaluationId);

    AlgorithmEvaluationEntity getLatestBmAlgorithmEvaluationEntityForOffer(Long offerId);

	NotificationResultDTO generateAgnoResultsFileAndSendEmail(Long algEvaluationId) throws IOException;
}
