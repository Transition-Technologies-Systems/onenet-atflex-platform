package pl.com.tt.flex.flex.agno.algorithm.timer_task;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.flex.agno.algorithm.AlgorithmProperties;
import pl.com.tt.flex.flex.agno.algorithm.utils.AlgorithmProcessParam;
import pl.com.tt.flex.flex.agno.algorithm.utils.AlgorithmUtils;
import pl.com.tt.flex.flex.agno.web.resource.admin.FlexAdminResource;
import pl.com.tt.flex.flex.agno.web.resource.server.FlexServerAlgorithmResultResource;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Aktualizacja logow.
 */
@Slf4j
public class AgnoLogScheduledExecutor {

    private final FlexAdminResource flexAdminResource;
    private final FlexServerAlgorithmResultResource flexServerAlgorithmResultResource;
    private final AlgorithmProperties algorithmProperties;
    private final Long evaluationId;
    private final String logFilename;
    private final String logPath;
    private ScheduledExecutorService executor;
    private AgnoLogUpdateTimer agnoLogUpdateTimer;

    public AgnoLogScheduledExecutor(AlgorithmProperties algorithmProperties, Long evaluationId, AlgorithmProcessParam algorithmProcessParam,
                                    FlexAdminResource flexAdminResource, FlexServerAlgorithmResultResource flexServerAlgorithmResultResource) {
        this.flexAdminResource = flexAdminResource;
        this.flexServerAlgorithmResultResource = flexServerAlgorithmResultResource;
        this.algorithmProperties = algorithmProperties;
        this.evaluationId = evaluationId;
        this.logFilename = algorithmProcessParam.getLogFilename();
        this.logPath = algorithmProcessParam.getLogFilePath();
    }

    @SneakyThrows
    public void start() {
        log.info("start() AgnoLogScheduledExecutor is starting");
        this.executor = Executors.newScheduledThreadPool(2);
        this.agnoLogUpdateTimer = setLogUpdateTimer(evaluationId, logFilename, logPath, executor);
    }

    public void execute() {
        if (Objects.nonNull(agnoLogUpdateTimer)) {
            log.info("execute() AgnoLogScheduledExecutor is execute");
            agnoLogUpdateTimer.run();
        }
    }

    public void close() {
        log.info("close() AgnoLogScheduledExecutor is closing");
        executor.execute(agnoLogUpdateTimer);
        executor.shutdown();
    }

    private AgnoLogUpdateTimer setLogUpdateTimer(Long evaluationId, String logFilename, String logPath,
                                                 ScheduledExecutorService executor) throws IOException {
        AlgorithmUtils.createFile(logPath);
        AgnoLogUpdateTimer timer = new AgnoLogUpdateTimer(evaluationId, logFilename, logPath, flexServerAlgorithmResultResource, flexAdminResource);
        executor.scheduleAtFixedRate(timer,
                algorithmProperties.getAgnoLogUpdateInitDelayInSeconds(),
                algorithmProperties.getAgnoLogUpdateInSeconds(),
                TimeUnit.SECONDS);
        return timer;
    }
}
