package pl.com.tt.flex.server.web.rest.dictionary.schedulingUnitType;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.SchedulingUnitTypeQueryService;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.SchedulingUnitTypeService;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeCriteria;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeDTO;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeMinDTO;

import java.util.List;

/**
 * REST controller for managing {@link SchedulingUnitTypeEntity}.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class SchedulingUnitTypeResource {

    public static final String ENTITY_NAME = "schedulingUnitType";

    private final SchedulingUnitTypeQueryService schedulingUnitTypeQueryService;
    protected final SchedulingUnitTypeService schedulingUnitTypeService;

    public SchedulingUnitTypeResource(SchedulingUnitTypeQueryService schedulingUnitTypeQueryService, SchedulingUnitTypeService schedulingUnitTypeService) {
        this.schedulingUnitTypeQueryService = schedulingUnitTypeQueryService;

        this.schedulingUnitTypeService = schedulingUnitTypeService;
    }

    protected ResponseEntity<List<SchedulingUnitTypeDTO>> getAllSchedulingUnitTypes(SchedulingUnitTypeCriteria criteria, Pageable pageable) {
        Page<SchedulingUnitTypeDTO> page = schedulingUnitTypeQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    protected ResponseEntity<List<SchedulingUnitTypeMinDTO>> getSchedulingUnitTypesMinDto() {
        return ResponseEntity.ok(schedulingUnitTypeService.getAllSchedulingUnitTypesMinimal());
    }
}
