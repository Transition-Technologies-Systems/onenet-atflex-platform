package pl.com.tt.flex.flex.agno.algorithm;

import pl.com.tt.flex.model.service.dto.algorithm.AlgEvaluationModuleDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmCancelStatus;

public interface AlgorithmService {

    void startAlgorithm(AlgEvaluationModuleDTO evaluationModuleDTO);

    AlgorithmCancelStatus cancelAlgorithm(long evaluationId);
}
