package pl.com.tt.flex.flex.agno.web.resource.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.com.tt.flex.flex.agno.service.kdm_model.KdmModelService;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelMinimalDTO;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/flex-agno-kdm")
public class FlexAgnoKdmModelResource {

    private final KdmModelService kdmModelService;

    public FlexAgnoKdmModelResource(KdmModelService kdmModelService) {
        this.kdmModelService = kdmModelService;
    }

    @GetMapping("/get-kdm-minimal")
    public ResponseEntity<Optional<KdmModelMinimalDTO>> getKdmFileMinimal(@RequestParam Long kdmModelId) {
        log.info("REST request to get kdm model with id: {}", kdmModelId);
        return ResponseEntity.ok(kdmModelService.getKdmModelMinimal(kdmModelId));
    }
}