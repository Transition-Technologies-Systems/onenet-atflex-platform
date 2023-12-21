package pl.com.tt.flex.flex.agno.algorithm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.flex.agno.algorithm.process.builder.factory.AlgorithmProcessBuilder;
import pl.com.tt.flex.flex.agno.algorithm.process.builder.factory.AlgorithmProcessBuilderFactory;
import pl.com.tt.flex.flex.agno.algorithm.process.builder.kdm_mod.KdmModNotAcquiredException;
import pl.com.tt.flex.flex.agno.algorithm.process.builder.kdm_mod.KdmModProcessBuilder;
import pl.com.tt.flex.flex.agno.algorithm.runing_procces.RunningProcessService;
import pl.com.tt.flex.flex.agno.algorithm.timer_task.AgnoLogScheduledExecutor;
import pl.com.tt.flex.flex.agno.algorithm.utils.AlgorithmFileUtils;
import pl.com.tt.flex.flex.agno.algorithm.utils.AlgorithmProcessParam;
import pl.com.tt.flex.flex.agno.util.ZipUtil;
import pl.com.tt.flex.flex.agno.web.resource.admin.FlexAdminResource;
import pl.com.tt.flex.flex.agno.web.resource.server.FlexServerAlgorithmResultResource;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgEvaluationModuleDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmCancelStatus;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static pl.com.tt.flex.flex.agno.algorithm.ProcessUtils.waitForEndingProcess;
import static pl.com.tt.flex.flex.agno.algorithm.utils.Constants.*;
import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus.EVALUATING;
import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus.KDM_MODEL_UPDATING;

@Component
@Slf4j
public class AlgorithmServiceImpl implements AlgorithmService {

    private final AlgorithmProperties algorithmProperties;
    private final AlgorithmProcessBuilderFactory factory;
    private final FlexServerAlgorithmResultResource flexServerAlgorithmResultResource;
    private final FlexAdminResource flexAdminResource;
    private final RunningProcessService runningProcessService;
    private final AlgorithmFileUtils algorithmFileUtils;

    public AlgorithmServiceImpl(AlgorithmProperties algorithmProperties, AlgorithmProcessBuilderFactory factory,
                                RunningProcessService runningProcessService,
                                FlexServerAlgorithmResultResource flexServerAlgorithmResultResource,
                                FlexAdminResource flexAdminResource, AlgorithmFileUtils algorithmFileUtils) {
        this.algorithmProperties = algorithmProperties;
        this.factory = factory;
        this.flexServerAlgorithmResultResource = flexServerAlgorithmResultResource;
        this.flexAdminResource = flexAdminResource;
        this.runningProcessService = runningProcessService;
        this.algorithmFileUtils = algorithmFileUtils;
    }

    @Async
    @Override
    public void startAlgorithm(AlgEvaluationModuleDTO evaluationModuleDTO) {
        log.info("startAlgorithm() Starting algorithm for evaluation id {}. Algorithm type: {}", evaluationModuleDTO.getEvaluationId(),
                evaluationModuleDTO.getTypeOfAlgorithm());
        AlgorithmProcessBuilder algorithmProcessBuilder = factory.getAlgorithmProcessBuilder(evaluationModuleDTO.getTypeOfAlgorithm());
        List<FileDTO> inputFiles = ZipUtil.zipToFiles(evaluationModuleDTO.getInputFilesZip().getBytesData());
        List<FileDTO> outputFiles = new ArrayList<>();
        List<FileDTO> logFiles = new ArrayList<>();
        boolean isCancel = false;
        DescendantProcessesFinishMonitor finishMonitor = null;
        AgnoLogScheduledExecutor agnoLogScheduledExecutor = null;
        try {
            List<AlgorithmProcessParam> algorithmProcessParams = algorithmFileUtils.unpackImportFiles(
                    evaluationModuleDTO, algorithmProperties,
                    algorithmProcessBuilder.extendedEnabled()
            );
            for (AlgorithmProcessParam algorithmParam : algorithmProcessParams) {
                agnoLogScheduledExecutor = new AgnoLogScheduledExecutor(algorithmProperties, evaluationModuleDTO.getEvaluationId(),
                        algorithmParam, flexAdminResource, flexServerAlgorithmResultResource);
                agnoLogScheduledExecutor.start(); // uruchomienie aktualizacji logow
                finishMonitor = runKdmModIfEnabled(evaluationModuleDTO, algorithmProcessBuilder, agnoLogScheduledExecutor, inputFiles, finishMonitor, algorithmParam);
                if (isEvaluationCanceled(finishMonitor)) {
                    finishMonitor = runAlgorithm(algorithmProcessBuilder, algorithmParam, evaluationModuleDTO.getEvaluationId());
                }
                algorithmFileUtils.catchLogFiles(logFiles, algorithmParam);
                if (finishMonitor.isCancel()) {
                    log.info("startAlgorithm() Algorithm is cancel with evaluationId: {}", evaluationModuleDTO.getEvaluationId());
                    isCancel = true;
                    closeIfExistScheduleExecutor(agnoLogScheduledExecutor);
                    break;
                }
                algorithmFileUtils.catchFile(outputFiles, algorithmParam.getOutputFilePath());
                algorithmFileUtils.catchAdditionalFiles(outputFiles, algorithmParam.getAdditionalOutputFiles());
                closeIfExistScheduleExecutor(agnoLogScheduledExecutor);
            }
        } catch (KdmModNotAcquiredException e) {
            log.info("startAlgorithm() Algorithm kdm mod process finished with error. EvaluationId {}, Ex msg: {}",
                    evaluationModuleDTO.getEvaluationId(), e.getMessage());
            postResult(evaluationModuleDTO, inputFiles, outputFiles, logFiles, AlgorithmStatus.KDM_MODEL_NOT_ACQUIRED, e.getMessage());
            closeIfExistScheduleExecutor(agnoLogScheduledExecutor);
            return;
        } catch (Exception e) {
            log.info("startAlgorithm() Algorithm processes finished with error. EvaluationId {}, Ex msg: {}",
                    evaluationModuleDTO.getEvaluationId(), e.getMessage());
            postResult(evaluationModuleDTO, inputFiles, outputFiles, logFiles, AlgorithmStatus.FAILURE, e.getMessage());
            closeIfExistScheduleExecutor(agnoLogScheduledExecutor);
            return;
        }
        AlgorithmStatus status = isCancel ? AlgorithmStatus.CANCELLED : AlgorithmStatus.COMPLETED;
        postResult(evaluationModuleDTO, inputFiles, outputFiles, logFiles, status, null);
        log.info("startAlgorithm() Algorithm processes finished for evaluationId {}", evaluationModuleDTO.getEvaluationId());
    }

    /**
     * Anulowanie wykonywania obliczeń algorytmu
     */
    @Override
    public AlgorithmCancelStatus cancelAlgorithm(long evaluationId) {
        log.info("cancelAlgorithm() START - cancel algorithm with evaluationId={}", evaluationId);
        AlgorithmCancelStatus algorithmCancelStatus = runningProcessService.destroyProcessForEvaluation(evaluationId);
        log.info("cancelAlgorithm() END - cancel algorithm with evaluationId={}", evaluationId);
        return algorithmCancelStatus;
    }

    /**
     * Jezeli dla danego typu algorytm włączona jest opcja modyfikacji pliku kdm, wówczas sktypt modyfikujacy
     * plik kdm jest uruchomiany
     */
    private DescendantProcessesFinishMonitor runKdmModIfEnabled(AlgEvaluationModuleDTO evaluationModuleDTO, AlgorithmProcessBuilder algorithmProcessBuilder,
                                                                AgnoLogScheduledExecutor agnoLogScheduledExecutor, List<FileDTO> inputFiles,
                                                                DescendantProcessesFinishMonitor finishMonitor, AlgorithmProcessParam algorithmParam) throws IOException, KdmModNotAcquiredException {
        if (algorithmProcessBuilder.runKdmMod()) {
            log.info("runKdmModIfEnabled() Kdm mod is enabled for algorithmType: {}", evaluationModuleDTO.getTypeOfAlgorithm());
            postStatusUpdate(evaluationModuleDTO.getEvaluationId(), KDM_MODEL_UPDATING);
            finishMonitor = runKdmModProcess(algorithmParam, evaluationModuleDTO.getEvaluationId());
            if (!finishMonitor.isCancel()) {
                algorithmFileUtils.catchFile(inputFiles, algorithmParam.getKdmFilePath()); // zaktualizowany KDM dodawany jest do plikow wejsciowych
                postStatusUpdate(evaluationModuleDTO.getEvaluationId(), EVALUATING);
                agnoLogScheduledExecutor.execute();
            }
        } else {
            log.info("runKdmModIfEnabled() Kdm mod is disabled for algorithmType: {}!", evaluationModuleDTO.getTypeOfAlgorithm());
        }
        return finishMonitor;
    }

    /**
     * Uruchominie algorytmu modyfikującego pliki kdm.
     * Po zakonczeniu procesu zostaje zaktualizowana lokalizacja pliku kdm
     */
    private DescendantProcessesFinishMonitor runKdmModProcess(AlgorithmProcessParam algorithmParam, long evaluationId) throws IOException, KdmModNotAcquiredException {
        log.info("runKdmModProcess() Run kdm_mod process for kdm_file: {}, evaluation_id: {}", algorithmParam.getKdmFilePath(), evaluationId);
        KdmModProcessBuilder kdmModProcessBuilder = new KdmModProcessBuilder();
        ProcessBuilder processBuilder = kdmModProcessBuilder.build(algorithmProperties, algorithmParam);
        log.info("runKdmModProcess() command: {}", processBuilder.command());
        Process process = processBuilder.start();
        DescendantProcessesFinishMonitor processesFinishMonitor = new DescendantProcessesFinishMonitor(process);
        runningProcessService.addRunningProcess(evaluationId, processesFinishMonitor);
        log.info("runKdmModProcess() Processed file: {}. Process pid: {}", algorithmParam.getKdmFilePath(), process.pid());
        waitForEndingProcess(processesFinishMonitor);
        if (!processesFinishMonitor.isCancel()) kdmModProcessBuilder.updateKdmPath(algorithmParam);
        return processesFinishMonitor;
    }

    /**
     * Uruchominie algorytmu.
     */
    private DescendantProcessesFinishMonitor runAlgorithm(AlgorithmProcessBuilder algorithmProcessBuilder,
                                                          AlgorithmProcessParam algorithmProcessParam, long evaluationId) throws IOException {
        log.info("runAlgorithm() Run algorithm with params: {}", algorithmProcessParam);
        ProcessBuilder processBuilder = algorithmProcessBuilder.build(algorithmProperties, algorithmProcessParam);
        log.info("runAlgorithm() command: {}", processBuilder.command());
        Process process = processBuilder.start();
        DescendantProcessesFinishMonitor processesFinishMonitor = new DescendantProcessesFinishMonitor(process);
        runningProcessService.addRunningProcess(evaluationId, processesFinishMonitor);
        log.info("runAlgorithm() Processed file: {}. Process pid: {}", algorithmProcessParam.getInputFilename(), process.pid());
        waitForEndingProcess(processesFinishMonitor);
        return processesFinishMonitor;
    }

    private void closeIfExistScheduleExecutor(AgnoLogScheduledExecutor agnoLogScheduledExecutor) {
        if (Objects.nonNull(agnoLogScheduledExecutor)) {
            agnoLogScheduledExecutor.close();
        }
    }

    private void postResult(AlgEvaluationModuleDTO evaluationModuleDTO, List<FileDTO> inputFiles, List<FileDTO> outputFiles,
                            List<FileDTO> logFiles, AlgorithmStatus status, String errorMsg) {
        evaluationModuleDTO.setStatus(status);
        evaluationModuleDTO.setErrorMessage(errorMsg);
        evaluationModuleDTO.setInputFilesZip(new FileDTO(ALGORITHM_INPUT_ZIP_FILE_NAME, ZipUtil.filesToZip(inputFiles)));
        evaluationModuleDTO.setOutputFilesZip(new FileDTO(ALGORITHM_OUTPUT_ZIP_FILE_NAME, ZipUtil.filesToZip(outputFiles)));
        evaluationModuleDTO.setLogFilesZip(new FileDTO(ALGORITHM_OUTPUT_LOG_ZIP_FILE_NAME, ZipUtil.filesToZip(logFiles)));
        flexServerAlgorithmResultResource.postResult(evaluationModuleDTO);
        runningProcessService.deleteEvaluationId(evaluationModuleDTO.getEvaluationId());
    }

    private void postStatusUpdate(Long evaluationId, AlgorithmStatus status) {
        log.debug("postStatusUpdate() Update status. EvaluationID: {}, Status: {}", evaluationId, status);
        flexServerAlgorithmResultResource.updateStatus(evaluationId, status);
        flexAdminResource.updateStatus(new MinimalDTO<>(evaluationId, status));
    }

    private boolean isEvaluationCanceled(DescendantProcessesFinishMonitor finishMonitor) {
        return Objects.isNull(finishMonitor) || !finishMonitor.isCancel();
    }

    public static class AlgorithmException extends RuntimeException {

        public AlgorithmException(String message) {
            super(message);
        }

        public AlgorithmException(String message, Throwable cause) {
            super(message, cause);
        }

    }
}
