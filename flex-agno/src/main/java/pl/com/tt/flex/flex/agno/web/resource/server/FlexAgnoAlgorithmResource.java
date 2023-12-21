package pl.com.tt.flex.flex.agno.web.resource.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.com.tt.flex.flex.agno.algorithm.AlgorithmService;
import pl.com.tt.flex.model.service.dto.algorithm.AlgEvaluationModuleDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmCancelStatus;


@Slf4j
@RestController
@RequestMapping("/api/flex-agno-algorithm")
public class FlexAgnoAlgorithmResource {

    private final AlgorithmService algorithmService;

    public FlexAgnoAlgorithmResource(AlgorithmService algorithmService) {
        this.algorithmService = algorithmService;
    }

    @PostMapping("/run-algorithm")
    public ResponseEntity<Void> runAgnoAlgorithm(@RequestBody AlgEvaluationModuleDTO algEvaluationDTO) {
        log.info("REST request run algorithm with type: {}, EvaluationID: {}", algEvaluationDTO.getTypeOfAlgorithm(), algEvaluationDTO.getEvaluationId());
        algorithmService.startAlgorithm(algEvaluationDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel-algorithm/{evaluationId}")
    public ResponseEntity<AlgorithmCancelStatus> cancel(@PathVariable long evaluationId) {
        log.info("REST request to cancel algorithm with evaluation id {}", evaluationId);
        return ResponseEntity.ok(algorithmService.cancelAlgorithm(evaluationId));
    }
}