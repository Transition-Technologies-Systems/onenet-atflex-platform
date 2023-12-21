package pl.com.tt.flex.flex.agno.algorithm.timer_task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.flex.agno.web.resource.admin.FlexAdminResource;
import pl.com.tt.flex.flex.agno.web.resource.server.FlexServerAlgorithmResultResource;
import pl.com.tt.flex.model.service.dto.file.FileContentDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.stream.Collectors;

/**
 * Timer służący do wysyłki aktualizacji log'ow do aplikacji flex-server i flex-admin
 */
@Slf4j
public class AgnoLogUpdateTimer extends TimerTask {

    private final Long evaluationId;
    private final String logFilename;
    private final FlexServerAlgorithmResultResource flexServerAlgorithmResultResource;
    private final FlexAdminResource flexAdminResource;
    private final BufferedReader bufferedReader;
    private final List<String> logLines = new ArrayList<>();
    private int lastLogLinesSize = 0;

    public AgnoLogUpdateTimer(Long evaluationId, String logFilename, String logPath,
                              FlexServerAlgorithmResultResource flexServerAlgorithmResultResource,
                              FlexAdminResource flexAdminResource) throws FileNotFoundException {
        this.bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(logPath), StandardCharsets.UTF_8));
        this.logFilename = logFilename;
        this.evaluationId = evaluationId;
        this.flexServerAlgorithmResultResource = flexServerAlgorithmResultResource;
        this.flexAdminResource = flexAdminResource;
    }

    @Override
    public void run() {
        log.info("run() Start update algorithm logs. EvaluationId={}, Filename={}", evaluationId, logFilename);
        readLogs(bufferedReader);
        if (logLines.size() > lastLogLinesSize) {
            log.debug("run() Update algorithm log. EvaluationId={}, Filename={}", evaluationId, logFilename);
            sendLogFile();
            lastLogLinesSize = logLines.size();
        } else {
            log.debug("run() Nothing has changed. EvaluationId={}, Filename={}", evaluationId, logFilename);
        }
        log.info("run() End update algorithm logs. EvaluationId={}, Filename={}", evaluationId, logFilename);
    }

    /**
     * Wysyłka aktualizacji logów do: flex-server i flex-admin(aktualizacja WS).
     * Wysyłka logów do flex-admin tylko wtedy gdy uda się wysłać logi do flex-server.
     */
    private void sendLogFile() {
        try {
            String fullString = String.join("\n", logLines);
            FileDTO logFile = new FileDTO(logFilename, fullString.getBytes(StandardCharsets.UTF_8));
            flexServerAlgorithmResultResource.updateLogFile(evaluationId, logFile);
            log.info("sendLogFile() Send file {} to flex-server. EvaluationId={}", logFilename, evaluationId);
            flexAdminResource.postAlgorithmEvaluationLogs(evaluationId, new FileContentDTO(logFilename, logLines));
            log.info("sendLogFile() Send file {} to flex-admin. EvaluationId={}", logFilename, evaluationId);
        } catch (Exception e) {
            log.info("sendLogFile() Problem with send file {}. EvaluationId={}", logFilename, evaluationId);
            e.printStackTrace();
        }
    }

    private void readLogs(BufferedReader bufferedReader) {
        List<String> lines = bufferedReader.lines().collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(lines)) {
            logLines.addAll(lines);
        }
    }
}
