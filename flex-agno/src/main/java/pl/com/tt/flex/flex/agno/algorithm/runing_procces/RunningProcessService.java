package pl.com.tt.flex.flex.agno.algorithm.runing_procces;

import pl.com.tt.flex.flex.agno.algorithm.DescendantProcessesFinishMonitor;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmCancelStatus;

public interface RunningProcessService {

    void addRunningProcess(long evaluationId, DescendantProcessesFinishMonitor process);

    AlgorithmCancelStatus destroyProcessForEvaluation(long evaluationId);

    void deleteEvaluationId(long evaluationId);
}
