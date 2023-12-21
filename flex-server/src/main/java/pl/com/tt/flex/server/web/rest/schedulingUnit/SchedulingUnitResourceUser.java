package pl.com.tt.flex.server.web.rest.schedulingUnit;

import com.google.common.collect.Maps;
import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitFileEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitQueryService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitCriteria;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDropdownSelectDTO;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitProposalMapper;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitProposalValidator;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitValidator;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.config.Constants.FLEX_ADMIN_APP_NAME;

/**
 * REST controller for managing {@link SchedulingUnitEntity} for FLEX-USER web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class SchedulingUnitResourceUser extends SchedulingUnitResource {

    public SchedulingUnitResourceUser(SchedulingUnitService schedulingUnitService, SchedulingUnitQueryService schedulingUnitQueryService,
                                      SchedulingUnitMapper schedulingUnitMapper, SchedulingUnitValidator schedulingUnitValidator, UserService userService,
                                      FspService fspService, SchedulingUnitProposalMapper schedulingUnitProposalMapper,
                                      SchedulingUnitProposalValidator schedulingUnitProposalValidator, SubportfolioService subportfolioService) {
        super(schedulingUnitService, schedulingUnitQueryService, schedulingUnitMapper, schedulingUnitValidator, userService, schedulingUnitProposalMapper,
            schedulingUnitProposalValidator, fspService, subportfolioService);
    }

    /**
     * {@code POST  /scheduling-units} : Create a new schedulingUnit by BSP User (ROLE_BALANCING_SERVICE_PROVIDER)
     *
     * @param schedulingUnitDTO the schedulingUnitDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new schedulingUnitDTO, or with status {@code 400 (Bad Request)} if the schedulingUnit has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/scheduling-units/create")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_MANAGE + "\")")
    public ResponseEntity<SchedulingUnitDTO> createSchedulingUnit(@Valid @RequestPart SchedulingUnitDTO schedulingUnitDTO,
                                                                  @RequestPart(value = "files", required = false) MultipartFile[] files) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-USER - REST request to save SchedulingUnit : {}", schedulingUnitDTO);
        UserDTO currentUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitResourceException("Current logged-in user not found"));
        if (!currentUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        schedulingUnitDTO.setBsp(new FspDTO(currentUser.getFspId()));
        return super.createSchedulingUnit(schedulingUnitDTO, files);
    }

    /**
     * {@code POST  /scheduling-units} : Updates an existing schedulingUnit by BSP User (ROLE_BALANCING_SERVICE_PROVIDER)
     *
     * @param schedulingUnitDTO the schedulingUnitDTO to update.
     * @param dersToRemove      DERs ids to be removed from updated schedulingUnit.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated schedulingUnitDTO,
     * or with status {@code 400 (Bad Request)} if the schedulingUnitDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the schedulingUnitDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/scheduling-units/update")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_MANAGE + "\")")
    public ResponseEntity<SchedulingUnitDTO> updateSchedulingUnit(@Valid @RequestPart SchedulingUnitDTO schedulingUnitDTO,
                                                                  @RequestPart(value = "files", required = false) MultipartFile[] files,
                                                                  @RequestParam(value = "dersToRemove", required = false, defaultValue = "") List<Long> dersToRemove)
        throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-USER - REST request to update SchedulingUnit : {}", schedulingUnitDTO);
        UserDTO currentUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitResourceException("Current logged-in user not found"));
        if (!currentUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER) &&
            !schedulingUnitService.existsBySchedulingUnitIdAndBspId(schedulingUnitDTO.getId(), currentUser.getFspId())) {
            log.warn("updateSchedulingUnit() Current user [id: {}] has no access to schedulingUnit [id: {}]", currentUser.getId(), schedulingUnitDTO.getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return super.updateSchedulingUnit(schedulingUnitDTO, files, dersToRemove);
    }

    /**
     * {@code GET  /scheduling-units} : get all the schedulingUnits.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of schedulingUnits in body.
     */
    @GetMapping("/scheduling-units")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitDTO>> getAllSchedulingUnits(SchedulingUnitCriteria criteria, Pageable pageable) {
        log.debug("FLEX-USER - REST request to get SchedulingUnits by criteria: {}", criteria);
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitResourceException("Current logged-in user not found"));
        setCriteriaByRole(criteria, fspUser);
        Page<SchedulingUnitDTO> page = schedulingUnitQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /scheduling-units/minimal} : get all the schedulingUnits minimal dto.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of schedulingUnits in body.
     */
    @GetMapping("/scheduling-units/minimal")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitMinDTO>> getAllSchedulingUnitsMinimal(SchedulingUnitCriteria criteria) {
        log.debug("FLEX-USER - REST request to get SchedulingUnits minimal by criteria: {}", criteria);
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitResourceException("Current logged-in user not found"));
        setCriteriaByRole(criteria, fspUser);
        return super.getAllSchedulingUnitsMinimal(criteria);
    }

    /**
     * {@code GET  /scheduling-units/:id} : get the "id" schedulingUnit.
     *
     * @param id the id of the schedulingUnitDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the schedulingUnitDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/scheduling-units/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<SchedulingUnitDTO> getSchedulingUnit(@PathVariable Long id) {
        log.debug("FLEX-USER - REST request to get SchedulingUnit : {}", id);
        FspEntity fsp = findFspOfCurrentUser();
        if (Role.ROLE_BALANCING_SERVICE_PROVIDER.equals(fsp.getRole())) {
            Optional<SchedulingUnitDTO> schedulingUnitDTO = schedulingUnitService.findByIdAndBspId(id, fsp.getId());
            return ResponseUtil.wrapOrNotFound(schedulingUnitDTO);
        }
        return super.getSchedulingUnit(id);
    }

    /**
     * {@code DELETE  /scheduling-units/:id} : delete the "id" schedulingUnit by BSP User (ROLE_BALANCING_SERVICE_PROVIDER)
     *
     * @param id the id of the schedulingUnitDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/scheduling-units/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_DELETE + "\")")
    public ResponseEntity<Void> deleteSchedulingUnit(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-USER - REST request to delete SchedulingUnit : {}", id);
        return super.deleteSchedulingUnit(id);
    }

    /**
     * {@code GET  /scheduling-units/files/:fileId} : get file from schedulingUnit
     *
     * @param fileId the id of the file attached to schedulingUnit.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fileDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/scheduling-units/files/{fileId}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<FileDTO> getSchedulingUnitFile(@PathVariable Long fileId) {
        log.debug("FLEX-USER - REST request to get SchedulingUnit [id: {}]", fileId);
        FspEntity fsp = findFspOfCurrentUser();
        if (Role.ROLE_BALANCING_SERVICE_PROVIDER.equals(fsp.getRole())) {
            Optional<FileDTO> fileDTO = Optional.empty();
            Optional<SchedulingUnitFileEntity> fileEntity = schedulingUnitService.getSchedulingUnitFileByFileIdAndSchedulingUnitBspId(fileId, fsp.getId());
            if (fileEntity.isPresent()) {
                fileDTO = Optional.ofNullable(ZipUtil.zipToFiles(fileEntity.get().getFileZipData()).get(0));
            }
            return ResponseUtil.wrapOrNotFound(fileDTO);
        }
        return super.getSchedulingUnitFile(fileId);
    }

    // Pobrane listy obiektow za pomoca metod findByCriteria(Criteria criteria, Sort sort) oraz findByCriteria(Criteria criteria, Pageable pageable)
    // roznia sie w kolejnosci sortowania obiektow. Z tego wzgledu do pobierania listy obiektow do eksportu uzyto metody findByCriteria(Criteria criteria, Pageable pageable)
    // tej samej co do pobrania listy obiektow na front (do widoku).

    /**
     * {@code GET  /user/scheduling-units/export/all} : export all schedulng unit's to file by flex user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_VIEW + "\")")
    @GetMapping("/scheduling-units/export/all")
    public ResponseEntity<FileDTO> exportAllSchedulingUnits(SchedulingUnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-USER - REST request to export all scheduling unit");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitResourceException("Current logged-in user not found"));
        setCriteriaByRole(criteria, fspUser);
        return super.exportSchedulingUnitToFile(criteria, pageable, Screen.USER_SCHEDULING_UNITS, false);
    }

    /**
     * {@code GET  /user/scheduling-units/export/displayed-data} : export displayed schedulng unit's to file by flex user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_VIEW + "\")")
    @GetMapping("/scheduling-units/export/displayed-data")
    public ResponseEntity<FileDTO> exportDisplayedSchedulingUnit(SchedulingUnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-USER - REST request to export displayed scheduling unit");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitResourceException("Current logged-in user not found"));
        setCriteriaByRole(criteria, fspUser);
        return super.exportSchedulingUnitToFile(criteria, pageable, Screen.USER_SCHEDULING_UNITS, true);
    }

    /**
     * {@code GET  /user/scheduling-units/register/export/all} : export all registered schedulng unit's to file by flex user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_VIEW + "\")")
    @GetMapping("/scheduling-units/register/export/all")
    public ResponseEntity<FileDTO> exportAllRegisteredSchedulingUnit(SchedulingUnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-USER - REST request to export all registered scheduling unit");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitResourceException("Current logged-in user not found"));
        setCriteriaByRole(criteria, fspUser);
        criteria.setCertified((BooleanFilter) new BooleanFilter().setEquals(true));
        return super.exportSchedulingUnitToFile(criteria, pageable, Screen.USER_REGISTER_SCHEDULING_UNITS, false);
    }

    /**
     * {@code GET  /user/scheduling-units/register/export/displayed-data} : export displayed registered schedulng unit's to file by flex user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_VIEW + "\")")
    @GetMapping("/scheduling-units/register/export/displayed-data")
    public ResponseEntity<FileDTO> exportDisplayedRegisteredSchedulingUnit(SchedulingUnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-USER - REST request to export displayed registered scheduling unit");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitResourceException("Current logged-in user not found"));
        setCriteriaByRole(criteria, fspUser);
        criteria.setCertified((BooleanFilter) new BooleanFilter().setEquals(true));
        return super.exportSchedulingUnitToFile(criteria, pageable, Screen.USER_REGISTER_SCHEDULING_UNITS, true);
    }

    /**
     * {@code GET  /scheduling-units/{id}/ders : get all Ders joined to SchedulingUnit grouped by Der Fsp company name.
     *
     * @param schedulingUnitId - SchedulingUnit id
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the map of Ders grouped by Der Fsp company name in body.
     */
    @GetMapping("/scheduling-units/{id}/ders")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<Map<String, List<UnitMinDTO>>> getSchedulingUnitDers(@PathVariable("id") Long schedulingUnitId) {
        log.debug("FLEX-USER - Rest request to get SchedulingUnit Ders");
        UserEntity currentUser = userService.getCurrentUser();
        if (currentUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER) || currentUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            // access only to Ders belonging to current user
            Map<String, List<UnitMinDTO>> result = Maps.newHashMap();
            List<UnitMinDTO> schedulingUnitDers = schedulingUnitService.getSchedulingUnitDersForFsp(schedulingUnitId, currentUser.getFsp().getId());
//            result.put(currentUser.getCompanyName(), schedulingUnitDers);
            FspEntity currentUserFsp = findFspOfCurrentUser();
            result.put(currentUserFsp.getCompanyName(), schedulingUnitDers);
            return ResponseEntity.ok(result);
        }
        return super.getSchedulingUnitDers(schedulingUnitId);
    }

    /**
     * {@code GET  /flex-potentials/minimal/get-all-registered-for-fsp-and-product} : get BSP registered scheduling units for product.
     *
     * @param productId the Product id of Scheduling unit to find
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body with the list of flexPotentialMinDTOs}.
     */
    @GetMapping("/scheduling-units/minimal/get-all-registered-for-bsp-and-product")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitDropdownSelectDTO>> findAllRegisteredSchedulingUnitsForBspAndProduct(@RequestParam(value = "productId") Long productId) {
        FspEntity currentUserBsp = findFspOfCurrentUser();
        log.debug("{} - REST request to get all registered SchedulingUnits for FSP: {} and Product: {}", FLEX_ADMIN_APP_NAME, currentUserBsp.getId(), productId);
        List<SchedulingUnitDropdownSelectDTO> result = schedulingUnitService.findAllRegisteredSchedulingUnitsForBspAndProduct(currentUserBsp.getId(), productId);
        if (result.isEmpty()) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(result);
    }

    private FspEntity findFspOfCurrentUser() {
        UserEntity maybefspUser = userService.getCurrentUser();
        return fspService.findFspOfUser(maybefspUser.getId(), maybefspUser.getLogin())
            .orElseThrow(() -> new SchedulingUnitResourceException("Cannot find Fsp by user id: " + maybefspUser.getId()));
    }

    private void setCriteriaByRole(SchedulingUnitCriteria criteria, UserDTO fspUser) {
        if (fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER) || fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            setCriteriaForFspFspaUsers(criteria, fspUser);
        }
        if (fspUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            setCriteriaForBspUser(criteria, fspUser);
        }
    }

    private void setCriteriaForFspFspaUsers(SchedulingUnitCriteria criteria, UserDTO fspUser) {
        List<Long> listOfSchedulingUnitsWithJoinedDersOfFsp = schedulingUnitService.findAllWithJoinedDersOfFsp(fspUser.getFspId());
        criteria.setId((LongFilter) new LongFilter().setIn(listOfSchedulingUnitsWithJoinedDersOfFsp));
    }

    private void setCriteriaForBspUser(SchedulingUnitCriteria criteria, UserDTO fspUser) {
        criteria.setBspId((LongFilter) new LongFilter().setEquals(fspUser.getFspId()));
    }
}
