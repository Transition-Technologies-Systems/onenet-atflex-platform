package pl.com.tt.flex.server.scheduled;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.service.algorithm.AlgorithmEvaluationServiceImpl;

@Slf4j
@Component
@ConditionalOnProperty("application.check-flex-agno-status.enabled")
public class CheckFlexAgnoStatusTask {

    private boolean agnoServiceUpLastCheck = false;

    private final DiscoveryClient discoveryClient;
    private final AlgorithmEvaluationServiceImpl algorithmEvaluationService;

    public CheckFlexAgnoStatusTask(final DiscoveryClient discoveryClient, final AlgorithmEvaluationServiceImpl algorithmEvaluationService) {
        this.discoveryClient = discoveryClient;
        this.algorithmEvaluationService = algorithmEvaluationService;
    }

    @Scheduled(cron = "${application.check-flex-agno-status.cron}")
    public void execute() {
        List<ServiceInstance> instances = discoveryClient.getInstances("FLEX-AGNO");
        boolean agnoServiceUp = !instances.isEmpty();
        if(agnoServiceUpLastCheck && !agnoServiceUp){
            log.info("Agno service lost connection");
            algorithmEvaluationService.setTechnicalFailureStatusForRunningAlgorithmsAndNotify();
        }
        agnoServiceUpLastCheck = agnoServiceUp;
    }

}
