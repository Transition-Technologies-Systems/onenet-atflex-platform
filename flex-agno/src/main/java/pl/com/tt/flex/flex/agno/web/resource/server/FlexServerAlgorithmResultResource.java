package pl.com.tt.flex.flex.agno.web.resource.server;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import pl.com.tt.flex.flex.agno.config.microservices.MicroservicesProxyConfiguration;
import pl.com.tt.flex.model.service.dto.algorithm.AlgEvaluationModuleDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

@FeignClient(value = "flex-server", configuration = MicroservicesProxyConfiguration.class)
public interface FlexServerAlgorithmResultResource {

    @PostMapping(value = "/api/flex-agno-algorithm/result", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> postResult(@RequestBody AlgEvaluationModuleDTO algEvaluationDTO);

    @PostMapping(value = "/api/flex-agno-algorithm/update-status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateStatus(@RequestParam("evaluationId") Long evaluationId, @RequestParam("status") AlgorithmStatus status);

    @PostMapping(value = "/api/flex-agno-algorithm/update-log", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateLogFile(@RequestParam("evaluationId") Long evaluationId, @RequestBody FileDTO fileDTO);
}
