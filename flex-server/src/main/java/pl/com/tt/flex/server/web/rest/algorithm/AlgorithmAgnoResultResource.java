package pl.com.tt.flex.server.web.rest.algorithm;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.algorithm.AlgEvaluationModuleDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.algorithm.AlgorithmEvaluationService;

@Slf4j
@RestController
@RequestMapping("/api/flex-agno-algorithm")
public class AlgorithmAgnoResultResource {

    private final AlgorithmEvaluationService algorithmEvaluationService;

    public AlgorithmAgnoResultResource(final AlgorithmEvaluationService algorithmEvaluationService) {
        this.algorithmEvaluationService = algorithmEvaluationService;
    }

    @PostMapping(value = "/result", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> getResult(@RequestBody AlgEvaluationModuleDTO algEvaluationModuleDTO) throws IOException {
        log.info("Rest request to save AlgEvaluation result: evaluationId: {}", algEvaluationModuleDTO.getEvaluationId());
        algorithmEvaluationService.saveAlgorithmResult(algEvaluationModuleDTO);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/update-status", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateStatus(@RequestParam Long evaluationId, @RequestParam AlgorithmStatus status) {
        log.info("Rest request to update status in evaluation: {}. Status: {}", evaluationId, status);
        algorithmEvaluationService.updateStatus(evaluationId, status);
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/update-log", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> updateLogFile(@RequestParam Long evaluationId, @RequestBody FileDTO fileDTO) {
        log.info("Rest request to update log file with name: {} in evaluation: {}", fileDTO.getFileName(), evaluationId);
        algorithmEvaluationService.updateLogFile(evaluationId, fileDTO);
        return ResponseEntity.ok().build();
    }
}
