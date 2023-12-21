package pl.com.tt.flex.flex.agno.web.resource.admin;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import pl.com.tt.flex.flex.agno.config.microservices.MicroservicesProxyConfiguration;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.file.FileContentDTO;

@FeignClient(value = "flex-admin", configuration = MicroservicesProxyConfiguration.class)
public interface FlexAdminResource {

    @PostMapping(value = "/api/broadcast/refresh-view/auctions/algorithm-evaluations/logs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> postAlgorithmEvaluationLogs(@RequestParam("evaluationID") Long evaluationId, @RequestBody FileContentDTO fileContentDTO);

    @PostMapping(value = "/api/broadcast/refresh-view/auctions/algorithm-evaluations/status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> updateStatus(@RequestBody MinimalDTO<Long, AlgorithmStatus> evaluationStatus);
}
