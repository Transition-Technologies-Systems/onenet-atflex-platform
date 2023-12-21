package pl.com.tt.flex.server.web.rest.unit.selfSchedule;

import io.github.jhipster.service.filter.LongFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
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
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.validator.selfSchedule.SelfScheduleFileValidator;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link UnitSelfScheduleEntity} for FLEX-USER web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UnitSelfScheduleResourceUser extends UnitSelfScheduleResource {

    private final SelfScheduleFileValidator validator;

    public UnitSelfScheduleResourceUser(UnitService unitService, UserService userService, SelfScheduleFileValidator selfScheduleFileValidator,
        UnitSelfScheduleService unitSelfScheduleService, UnitSelfScheduleQueryService unitSelfScheduleQueryService) {
        super(unitService, userService, selfScheduleFileValidator, unitSelfScheduleService, unitSelfScheduleQueryService);
        this.validator = selfScheduleFileValidator;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SELF_SCHEDULE_MANAGE + "\")")
    @PostMapping(value = "/self-schedule")
    public void importSelfScheduleDer(@RequestPart(value = "file") MultipartFile[] multipartFile, @RequestParam boolean force)
        throws ObjectValidationException, IOException {
        log.debug("FLEX-USER - REST request to import Self Schedules file");
        super.importSelfScheduleDer(multipartFile, false, force);
    }

    @GetMapping("/self-schedule")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SELF_SCHEDULE_VIEW + "\")")
    public ResponseEntity<List<UnitSelfScheduleDTO>> getAllSelfSchedules(UnitSelfScheduleCriteria criteria, Pageable pageable) {
        log.debug("FLEX-USER - REST request to get Self Schedules by criteria: {}", criteria);
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SelfScheduleResourceException("Current logged-in user not found"));
        criteria.setFspId((LongFilter) new LongFilter().setEquals(fspUser.getFspId()));
        return super.getAllSelfSchedules(criteria, pageable);
    }

    @GetMapping("/self-schedule/detail/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SELF_SCHEDULE_VIEW + "\")")
    public ResponseEntity<UnitSelfScheduleDTO> getDetail(@PathVariable Long id) throws IOException {
        log.debug("FLEX_USER - REST request to get detail of Self Schedule: {}", id);
        if (!validator.isSelfScheduleBelongsToFspUser(id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return super.getDetail(id);
    }

    @GetMapping("/self-schedule/get-volumes-for-der")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SELF_SCHEDULE_OFFER_VIEW + "\")")
    public ResponseEntity<List<MinimalDTO<String, BigDecimal>>> getVolumesForDer(@RequestParam Long derId, @RequestParam Instant selfScheduleDate) throws IOException {
        log.debug("FLEX_USER - REST request to get volumes for Der with ID: {} and self schedule date: {}", derId, selfScheduleDate);
        return super.getVolumesForDer(derId, selfScheduleDate);
    }

    @GetMapping("/self-schedule/get-volumes-for-offer")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SELF_SCHEDULE_OFFER_VIEW + "\")")
    public ResponseEntity<Map<Long, List<MinimalDTO<String, BigDecimal>>>> getVolumesForOffer(@RequestParam Long offerId) throws IOException {
        log.debug("FLEX_USER - REST request to get der volumes for offer with ID: {}", offerId);
        return super.getVolumesForOffer(offerId);
    }

    @GetMapping("/self-schedule/get-volumes-for-ders")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SELF_SCHEDULE_OFFER_VIEW + "\")")
    public ResponseEntity<Map<Long, List<MinimalDTO<String, BigDecimal>>>> getVolumesForDers(@RequestParam List<Long> ders, @RequestParam Instant selfScheduleDate) throws IOException {
        log.debug("FLEX_USER - REST request to get der volumes for ders with ID: {}", ders);
        return super.getVolumesForDers(ders, selfScheduleDate);
    }

    @DeleteMapping("/self-schedule/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SELF_SCHEDULE_DELETE + "\")")
    public ResponseEntity<Void> deleteSelfSchedule(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-USER - REST request to delete Self Schedule : {}", id);
        if (!validator.isSelfScheduleBelongsToFspUser(id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return super.deleteSelfSchedule(id);
    }

    @GetMapping("/self-schedule/template")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SELF_SCHEDULE_VIEW + "\")")
    public ResponseEntity<FileDTO> getTemplate() throws IOException {
        log.debug("FLEX_USER- REST request to get Self Schedule Template");
        return super.getTemplate(false);
    }
}
