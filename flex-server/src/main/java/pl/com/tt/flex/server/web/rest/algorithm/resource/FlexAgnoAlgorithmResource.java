package pl.com.tt.flex.server.web.rest.algorithm.resource;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.tt.flex.model.service.dto.algorithm.AlgEvaluationModuleDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmCancelStatus;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelMinimalDTO;
import pl.com.tt.flex.server.config.microservices.MicroservicesProxyConfiguration;

import java.util.Optional;

@FeignClient(value = "flex-agno", configuration = MicroservicesProxyConfiguration.class, primary = false)
public interface FlexAgnoAlgorithmResource {

    @PostMapping(value = "/api/flex-agno-algorithm/run-algorithm", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> runAgnoAlgorithm(@RequestBody AlgEvaluationModuleDTO algEvaluationDTO);

    @PostMapping("/api/flex-agno-algorithm/cancel-algorithm/{evaluationId}")
    AlgorithmCancelStatus cancelAlgorithm(@PathVariable("evaluationId") long evaluationId);

    @GetMapping("/api/flex-agno-kdm/get-kdm-minimal")
    Optional<KdmModelMinimalDTO> getKdmFileMinimal(@RequestParam("kdmModelId") Long kdmModelId);
}
