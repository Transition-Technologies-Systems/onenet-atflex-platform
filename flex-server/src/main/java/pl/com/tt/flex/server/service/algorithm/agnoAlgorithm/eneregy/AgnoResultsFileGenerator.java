package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy;

import java.io.IOException;

import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;

public interface AgnoResultsFileGenerator {

    FileDTO getResultsFile(AlgorithmEvaluationEntity algEvaluation) throws IOException;

}
