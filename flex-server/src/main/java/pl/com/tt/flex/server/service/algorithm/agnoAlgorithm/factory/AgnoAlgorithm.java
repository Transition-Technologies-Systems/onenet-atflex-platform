package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.factory;

import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationConfigDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;

import java.io.IOException;
import java.util.Set;

public interface AgnoAlgorithm {

    FileDTO getAlgorithmInputFiles(AlgorithmEvaluationConfigDTO evaluationConfigDTO, Set<AuctionDayAheadOfferDTO> offers) throws IOException, ObjectValidationException;

    void startAlgorithm(AlgorithmEvaluationConfigDTO evaluationConfigDTO) throws IOException, ObjectValidationException;

    boolean isSupport(AlgorithmType algorithmType);
}
