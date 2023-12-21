package pl.com.tt.flex.flex.agno.algorithm;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessUtils {

    public static void waitForEndingProcess(DescendantProcessesFinishMonitor processesFinishMonitor) {
        log.info("waitForEndingProcess() Waiting for end process with pid: {}", processesFinishMonitor.getProcessPid());
        while (!Thread.interrupted()) {
            //will be timeouted after desired property value
            if (processesFinishMonitor.isAllDead()) {
                log.info("waitForEndingProcess() Finish processed pid: {}", processesFinishMonitor.getProcessPid());
                break;
            }
        }
        cleanup(processesFinishMonitor);
    }

    public static void cleanup(DescendantProcessesFinishMonitor descendantProcessesFinishMonitor) {
        log.info("cleanup() Cleanup of processes - start");
        Set<ProcessHandle> aliveProcesses = descendantProcessesFinishMonitor.getAliveProcesses();
        log.debug("cleanup() Killing all spawned processes: {}", aliveProcesses.stream().map(ProcessHandleWrapper::new).collect(Collectors.toList()));
        aliveProcesses.forEach(processHandle -> {
            log.debug("cleanup() Killing process with pid {}", processHandle.pid());
            processHandle.destroy();
            if (processHandle.isAlive()) {
                log.error("cleanup() Process with pid {} is still alive!", processHandle.pid());
            }
        });
        log.info("cleanup() Cleanup of processes - end");
    }
}
