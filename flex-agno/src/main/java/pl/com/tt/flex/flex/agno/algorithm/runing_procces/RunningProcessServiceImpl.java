package pl.com.tt.flex.flex.agno.algorithm.runing_procces;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.flex.agno.algorithm.DescendantProcessesFinishMonitor;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmCancelStatus;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RunningProcessServiceImpl implements RunningProcessService {

    // Mapa z uruchomionymi procesami dla danego obliczenia algorytmu
    private final Map<Long, List<DescendantProcessesFinishMonitor>> runningEvaluationProcesses = new HashMap<>();


    /**
     * Dodanie do mapy (runningEvaluationProcesses) uruchomionego procesu obliczen algorytmu
     * */
    @Override
    public void addRunningProcess(long evaluationId, DescendantProcessesFinishMonitor process) {
        if (runningEvaluationProcesses.containsKey(evaluationId)) {
            List<DescendantProcessesFinishMonitor> processes = runningEvaluationProcesses.get(evaluationId);
            processes.add(process);
            runningEvaluationProcesses.put(evaluationId, processes);
        } else {
            List<DescendantProcessesFinishMonitor> processes = new ArrayList<>();
            processes.add(process);
            runningEvaluationProcesses.put(evaluationId, processes);
        }
    }

    /**
     * Anulowanie wykonywania się procesów dla obliczen algorytmu o id {@param evaluationId}
     * */
    @Override
    public AlgorithmCancelStatus destroyProcessForEvaluation(long evaluationId) {
        Optional<List<DescendantProcessesFinishMonitor>> finishMonitorsOpt = findProcess(evaluationId);
        if (finishMonitorsOpt.isPresent()) {
            List<DescendantProcessesFinishMonitor> processes = finishMonitorsOpt.get();
            String pids = processes.stream().map(DescendantProcessesFinishMonitor::getProcessPid).map(Object::toString).collect(Collectors.joining(", "));
            log.info("destroyProcessForEvaluation() Start - process to kill {}. PIDS: {}", processes.size(), pids);
            processes.forEach(DescendantProcessesFinishMonitor::cancelProcess);
            log.info("destroyProcessForEvaluation() END - process to kill {}. PIDS: {}", processes.size(), pids);
            deleteEvaluationId(evaluationId);
        } else {
            return AlgorithmCancelStatus.ALGORITHM_IS_NOT_RUNNING;
        }
        return AlgorithmCancelStatus.OK;
    }


    private Optional<List<DescendantProcessesFinishMonitor>> findProcess(Long evaluationId) {
        return runningEvaluationProcesses.entrySet().stream()
                .filter(s -> s.getKey().equals(evaluationId))
                .findFirst()
                .map(Map.Entry::getValue);
    }

    /**
     * Usunięcie zakończonego lub anulowanego obliczania algorytmu
     * */
    @Override
    public void deleteEvaluationId(long evaluationId) {
        runningEvaluationProcesses.remove(evaluationId);
    }
}
