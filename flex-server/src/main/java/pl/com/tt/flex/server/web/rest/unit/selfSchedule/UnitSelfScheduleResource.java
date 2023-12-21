package pl.com.tt.flex.server.web.rest.unit.selfSchedule;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.selfSchedule.UnitSelfScheduleQueryService;
import pl.com.tt.flex.server.service.unit.selfSchedule.UnitSelfScheduleService;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleCriteria;
import pl.com.tt.flex.server.service.unit.selfSchedule.dto.UnitSelfScheduleDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.selfSchedule.SelfScheduleFileValidator;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Common REST controller for managing {@link UnitSelfScheduleEntity} for all web modules.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class UnitSelfScheduleResource {

    public static final String ENTITY_NAME = "selfSchedule";

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    protected final UnitService unitService;
    protected final UserService userService;
    protected final SelfScheduleFileValidator selfScheduleFileValidator;
    protected final UnitSelfScheduleService unitSelfScheduleService;
    protected final UnitSelfScheduleQueryService unitSelfScheduleQueryService;


    public UnitSelfScheduleResource(UnitService unitService, UserService userService, SelfScheduleFileValidator selfScheduleFileValidator, UnitSelfScheduleService unitSelfScheduleService, UnitSelfScheduleQueryService unitSelfScheduleQueryService) {
        this.unitService = unitService;
        this.userService = userService;
        this.selfScheduleFileValidator = selfScheduleFileValidator;
        this.unitSelfScheduleService = unitSelfScheduleService;
        this.unitSelfScheduleQueryService = unitSelfScheduleQueryService;
    }

    public void importSelfScheduleDer(@RequestPart(value = "file") MultipartFile[] multipartFile, boolean isAdminTemplate, boolean force)
        throws ObjectValidationException, IOException {
        log.debug("REST request to add self schedule");
        selfScheduleFileValidator.checkValid(multipartFile, isAdminTemplate);
        if (!force) {
            unitSelfScheduleService.throwExceptionIfExistSelfSchedule(multipartFile, isAdminTemplate);
        }
        unitSelfScheduleService.save(multipartFile, isAdminTemplate);
    }

    public ResponseEntity<List<UnitSelfScheduleDTO>> getAllSelfSchedules(UnitSelfScheduleCriteria criteria, Pageable pageable) {
        Page<UnitSelfScheduleDTO> page = unitSelfScheduleQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    protected ResponseEntity<Void> deleteSelfSchedule(Long id) throws ObjectValidationException {
        selfScheduleFileValidator.checkDeletable(id);
        unitSelfScheduleService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    protected ResponseEntity<FileDTO> getTemplate(boolean isAdminTemplate) throws IOException {
        return ResponseEntity.ok().body(unitSelfScheduleService.getTemplate(isAdminTemplate));
    }

    protected ResponseEntity<UnitSelfScheduleDTO> getDetail(Long id) throws IOException {
        return ResponseEntity.ok().body(unitSelfScheduleService.getDetail(id));
    }

    public ResponseEntity<List<MinimalDTO<String, BigDecimal>>> getVolumesForDer(Long derId, Instant date) throws IOException {
        return ResponseEntity.ok(unitSelfScheduleService.findVolumesForDerAndSelfScheduleDate(derId, date));
    }

    public ResponseEntity<Map<Long, List<MinimalDTO<String, BigDecimal>>>> getVolumesForOffer(Long offerId) throws IOException {
        return ResponseEntity.ok(unitSelfScheduleService.findVolumesForOffer(offerId));
    }

    public ResponseEntity<Map<Long, List<MinimalDTO<String, BigDecimal>>>> getVolumesForDers(List<Long> ders, Instant date) throws IOException {
        return ResponseEntity.ok(unitSelfScheduleService.findVolumesForDersAndSelfScheduleDate(ders, date));
    }

    protected static class SelfScheduleResourceException extends RuntimeException {
        protected SelfScheduleResourceException(String message) {
            super(message);
        }
    }
}
