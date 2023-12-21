package pl.com.tt.flex.server.web.rest.schedulingUnit;

import com.google.common.collect.Lists;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity_;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitFileEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.common.dto.FileDTOUtil;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitQueryService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitCriteria;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitFileDTO;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitProposalMapper;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitProposalValidator;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SCHEDULING_UNIT_NOTHING_TO_EXPORT;

/**
 * Common REST controller for managing {@link SchedulingUnitEntity} for all web modules.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class SchedulingUnitResource {

    public static final String SCHEDULING_UNIT_ENTITY_NAME = "schedulingUnit";

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    protected final SchedulingUnitService schedulingUnitService;
    protected final SchedulingUnitQueryService schedulingUnitQueryService;
    protected final SchedulingUnitMapper schedulingUnitMapper;
    protected final SchedulingUnitValidator schedulingUnitValidator;
    protected final UserService userService;
    protected final SchedulingUnitProposalMapper schedulingUnitProposalMapper;
    protected final SchedulingUnitProposalValidator schedulingUnitProposalValidator;
    protected final FspService fspService;
    protected final SubportfolioService subportfolioService;

    public SchedulingUnitResource(SchedulingUnitService schedulingUnitService, SchedulingUnitQueryService schedulingUnitQueryService, SchedulingUnitMapper schedulingUnitMapper,
        SchedulingUnitValidator schedulingUnitValidator, UserService userService, SchedulingUnitProposalMapper schedulingUnitProposalMapper,
        SchedulingUnitProposalValidator schedulingUnitProposalValidator, FspService fspService, SubportfolioService subportfolioService) {
        this.schedulingUnitService = schedulingUnitService;
        this.schedulingUnitQueryService = schedulingUnitQueryService;
        this.schedulingUnitMapper = schedulingUnitMapper;
        this.schedulingUnitValidator = schedulingUnitValidator;
        this.userService = userService;
        this.schedulingUnitProposalMapper = schedulingUnitProposalMapper;
        this.schedulingUnitProposalValidator = schedulingUnitProposalValidator;
        this.fspService = fspService;
        this.subportfolioService = subportfolioService;
    }

    public ResponseEntity<SchedulingUnitDTO> createSchedulingUnit(SchedulingUnitDTO schedulingUnitDTO, MultipartFile[] files) throws URISyntaxException, ObjectValidationException {
        if (schedulingUnitDTO.getId() != null) {
            throw new BadRequestAlertException("A new schedulingUnit cannot already have an ID", SCHEDULING_UNIT_ENTITY_NAME, "idexists");
        }
        schedulingUnitDTO.getFiles().addAll(parseFilesForScheduleUnit(files));
        schedulingUnitValidator.checkValid(schedulingUnitDTO);
        SchedulingUnitDTO result = schedulingUnitService.save(schedulingUnitDTO);
        schedulingUnitService.registerNewNotificationForSchedulingUnitCreation(result);
        schedulingUnitService.sendMailInformingAboutSchedulingUnitCreation(result);
        return ResponseEntity.created(new URI("/api/scheduling-units/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, SCHEDULING_UNIT_ENTITY_NAME, result.getId().toString())).body(result);
    }

    private List<SchedulingUnitFileDTO> parseFilesForScheduleUnit(MultipartFile[] files) throws ObjectValidationException {
        if (nonNull(files)) {
            schedulingUnitValidator.checkFileExtensionValid(Arrays.stream(files).collect(Collectors.toList()));
            return Arrays.stream(files).map(file -> new SchedulingUnitFileDTO(FileDTOUtil.parseMultipartFile(file))).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public ResponseEntity<SchedulingUnitDTO> updateSchedulingUnit(SchedulingUnitDTO schedulingUnitDTO, MultipartFile[] files, List<Long> dersToRemove)
        throws URISyntaxException, ObjectValidationException {
        if (schedulingUnitDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", SCHEDULING_UNIT_ENTITY_NAME, "idnull");
        }
        SchedulingUnitDTO schedulingUnitEntityFromDb = schedulingUnitService.findById(schedulingUnitDTO.getId()).orElseThrow(() -> new BadRequestAlertException("Cannot find scheduling unit", SCHEDULING_UNIT_ENTITY_NAME, "idnull"));
        schedulingUnitDTO.getFiles().addAll(parseFilesForScheduleUnit(files));
        schedulingUnitValidator.checkModifiable(schedulingUnitDTO);
        SchedulingUnitDTO result = schedulingUnitService.update(schedulingUnitDTO, dersToRemove, schedulingUnitDTO.getRemoveFiles());
        schedulingUnitService.registerNewNotificationForSchedulingUnitEdition(result, schedulingUnitEntityFromDb);
        schedulingUnitService.sendMailInformingAboutSchedulingUnitModification(schedulingUnitEntityFromDb, result);
        if (hasBeenMarkedReadyForTests(schedulingUnitEntityFromDb, result)) {
            schedulingUnitDTO.setBsp(result.getBsp());
            schedulingUnitService.notifyUsersThatSchedulingUnitIsReadyForTests(schedulingUnitDTO, getRecipients());
        }
        if (!schedulingUnitEntityFromDb.isCertified() && result.isCertified()) {
            schedulingUnitService.sendNotificationInformingAboutRegistered(result);
            schedulingUnitService.sendMailInformingAboutRegistered(result);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, SCHEDULING_UNIT_ENTITY_NAME, schedulingUnitDTO.getId().toString()))
            .body(result);
    }

    private boolean hasBeenMarkedReadyForTests(SchedulingUnitDTO schedulingUnitEntityFromDb, SchedulingUnitDTO result) {
        return !schedulingUnitEntityFromDb.isReadyForTests() && result.isReadyForTests();
    }

    private List<UserEntity> getRecipients() {
        return userService.findUsersByRole(Set.of(Role.ROLE_ADMIN, Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR)).stream().filter(user ->
            !user.getLogin().equals(userService.getCurrentUser().getLogin())).collect(Collectors.toList());
    }

    protected ResponseEntity<List<SchedulingUnitMinDTO>> getAllSchedulingUnitsMinimal(SchedulingUnitCriteria criteria) {
        criteria.setActive((BooleanFilter) new BooleanFilter().setEquals(true));
        return ResponseEntity.ok(schedulingUnitQueryService.findMinByCriteria(criteria, Sort.by(SchedulingUnitEntity_.NAME)));
    }

    public ResponseEntity<SchedulingUnitDTO> getSchedulingUnit(Long id) {
        Optional<SchedulingUnitDTO> schedulingUnitDTO = schedulingUnitService.findById(id);
        return ResponseUtil.wrapOrNotFound(schedulingUnitDTO);
    }

    public ResponseEntity<Void> deleteSchedulingUnit(Long id) throws ObjectValidationException {
        log.debug("REST request to delete SchedulingUnit : {}", id);
        schedulingUnitValidator.checkDeletable(id);
        schedulingUnitService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, SCHEDULING_UNIT_ENTITY_NAME, id.toString())).build();
    }

    public ResponseEntity<FileDTO> getSchedulingUnitFile(Long fileId) {
        Optional<FileDTO> fileDTO = Optional.empty();
        Optional<SchedulingUnitFileEntity> fileEntity = schedulingUnitService.getSchedulingUnitFileByFileId(fileId);
        if (fileEntity.isPresent()) {
            fileDTO = Optional.ofNullable(ZipUtil.zipToFiles(fileEntity.get().getFileZipData()).get(0));
        }
        return ResponseUtil.wrapOrNotFound(fileDTO);
    }

    // Pobrane listy obiektow za pomoca metod findByCriteria(Criteria criteria, Sort sort) oraz findByCriteria(Criteria criteria, Pageable pageable)
    // roznia sie w kolejnosci sortowania obiektow. Z tego wzgledu do pobierania listy obiektow do eksportu uzyto metody findByCriteria(Criteria criteria, Pageable pageable)
    // tej samej co do pobrania listy obiektow na front (do widoku).
    protected ResponseEntity<FileDTO> exportSchedulingUnitToFile(SchedulingUnitCriteria criteria, Pageable pageable, Screen screen, boolean isOnlyDisplayedData) throws IOException {
        int size = (int) schedulingUnitQueryService.countByCriteria(criteria);
        if (size == 0) {
            throw new ObjectValidationException("Nothing to export", SCHEDULING_UNIT_NOTHING_TO_EXPORT);
        }
        PageRequest pageRequest = PageRequest.of(0, size, pageable.getSort());
        Page<SchedulingUnitDTO> schedulingUnitsPage = schedulingUnitQueryService.findByCriteria(criteria, pageRequest);
        return ResponseEntity.ok().body(schedulingUnitService.exportSchedulingUnitToFile(schedulingUnitsPage.getContent(), isOnlyDisplayedData, screen));
    }

    protected ResponseEntity<Map<String, List<UnitMinDTO>>> getSchedulingUnitDers(Long schedulingUnitId) {
        List<UnitMinDTO> schedulingUnitDers = schedulingUnitService.getSchedulingUnitDers(schedulingUnitId);
        Map<String, List<UnitMinDTO>> result = schedulingUnitDers.stream().collect(Collectors.groupingBy(UnitMinDTO::getFspCompanyName));
        return ResponseEntity.ok(result);
    }

    protected static class SchedulingUnitResourceException extends RuntimeException {
        protected SchedulingUnitResourceException(String message) {
            super(message);
        }
    }
}
