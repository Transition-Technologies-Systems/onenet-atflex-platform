package pl.com.tt.flex.server.web.rest.schedulingUnit;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.fsp.FspService;
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
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitProposalValidator;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitValidator;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.config.Constants.FLEX_ADMIN_APP_NAME;

/**
 * REST controller for managing {@link SchedulingUnitEntity} for FLEX-ADMIN web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class SchedulingUnitResourceAdmin extends SchedulingUnitResource {

    public SchedulingUnitResourceAdmin(SchedulingUnitService schedulingUnitService, SchedulingUnitQueryService schedulingUnitQueryService,
        SchedulingUnitMapper schedulingUnitMapper, SchedulingUnitValidator schedulingUnitValidator, UserService userService,
        SchedulingUnitProposalMapper schedulingUnitProposalMapper, SchedulingUnitProposalValidator schedulingUnitProposalValidator,
        FspService fspService, SubportfolioService subportfolioService) {
        super(schedulingUnitService, schedulingUnitQueryService, schedulingUnitMapper, schedulingUnitValidator, userService, schedulingUnitProposalMapper,
            schedulingUnitProposalValidator, fspService, subportfolioService);
    }

    /**
     * {@code POST  /scheduling-units} : Create a new schedulingUnit.
     *
     * @param schedulingUnitDTO the schedulingUnitDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new schedulingUnitDTO, or with status {@code 400 (Bad Request)} if the schedulingUnit has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/scheduling-units/create")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_MANAGE + "\")")
    public ResponseEntity<SchedulingUnitDTO> createSchedulingUnit(@Valid @RequestPart SchedulingUnitDTO schedulingUnitDTO,
        @RequestPart(value = "files", required = false) MultipartFile[] files) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to save SchedulingUnit : {}", schedulingUnitDTO);
        return super.createSchedulingUnit(schedulingUnitDTO, files);
    }

    /**
     * {@code POST  /scheduling-units} : Updates an existing schedulingUnit.
     *
     * @param schedulingUnitDTO the schedulingUnitDTO to update.
     * @param dersToRemove      DERs ids to be removed from updated schedulingUnit.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated schedulingUnitDTO,
     * or with status {@code 400 (Bad Request)} if the schedulingUnitDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the schedulingUnitDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/scheduling-units/update")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_MANAGE + "\")")
    public ResponseEntity<SchedulingUnitDTO> updateSchedulingUnit(@Valid @RequestPart SchedulingUnitDTO schedulingUnitDTO,
        @RequestPart(value = "files", required = false) MultipartFile[] files,
        @RequestParam(value = "dersToRemove", required = false, defaultValue = "") List<Long> dersToRemove)
        throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to update SchedulingUnit : {}", schedulingUnitDTO);
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
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitDTO>> getAllSchedulingUnits(SchedulingUnitCriteria criteria, Pageable pageable) {
        log.debug("FLEX-ADMIN - REST request to get SchedulingUnits by criteria: {}", criteria);
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
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitMinDTO>> getAllSchedulingUnitsMinimal(SchedulingUnitCriteria criteria) {
        log.debug("FLEX-ADMIN - REST request to get SchedulingUnits minimal by criteria: {}", criteria);
        return super.getAllSchedulingUnitsMinimal(criteria);
    }

    /**
     * {@code GET  /scheduling-units/:id} : get the "id" schedulingUnit.
     *
     * @param id the id of the schedulingUnitDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the schedulingUnitDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/scheduling-units/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<SchedulingUnitDTO> getSchedulingUnit(@PathVariable Long id) {
        log.debug("FLEX-ADMIN - REST request to get SchedulingUnit : {}", id);
        return super.getSchedulingUnit(id);
    }

    /**
     * {@code DELETE  /scheduling-units/:id} : delete the "id" schedulingUnit.
     *
     * @param id the id of the schedulingUnitDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/scheduling-units/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_DELETE + "\")")
    public ResponseEntity<Void> deleteSchedulingUnit(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to delete SchedulingUnit : {}", id);
        return super.deleteSchedulingUnit(id);
    }

    /**
     * {@code GET  /fscheduling-units/files/:fileId} : get file from schedulingUnit
     *
     * @param fileId the id of the file attached to schedulingUnit.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fileDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/scheduling-units/files/{fileId}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<FileDTO> getSchedulingUnitFile(@PathVariable Long fileId) {
        log.debug("FLEX-ADMIN - REST request to get SchedulingUnit [id: {}]", fileId);
        return super.getSchedulingUnitFile(fileId);
    }

    // Pobrane listy obiektow za pomoca metod findByCriteria(Criteria criteria, Sort sort) oraz findByCriteria(Criteria criteria, Pageable pageable)
    // roznia sie w kolejnosci sortowania obiektow. Z tego wzgledu do pobierania listy obiektow do eksportu uzyto metody findByCriteria(Criteria criteria, Pageable pageable)
    // tej samej co do pobrania listy obiektow na front (do widoku).

    /**
     * {@code GET  /admin/scheduling-units/export/all} : export all schedulng unit's to file by flex admin.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_VIEW + "\")")
    @GetMapping("/scheduling-units/export/all")
    public ResponseEntity<FileDTO> exportAllSchedulingUnits(SchedulingUnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export all scheduling unit");
        return super.exportSchedulingUnitToFile(criteria, pageable, Screen.ADMIN_SCHEDULING_UNITS, false);
    }

    /**
     * {@code GET  /admin/scheduling-units/export/displayed-data} : export displayed schedulng unit's to file by flex admin.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_VIEW + "\")")
    @GetMapping("/scheduling-units/export/displayed-data")
    public ResponseEntity<FileDTO> exportDisplayedSchedulingUnits(SchedulingUnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export displayed scheduling unit");
        return super.exportSchedulingUnitToFile(criteria, pageable, Screen.ADMIN_SCHEDULING_UNITS, true);
    }

    /**
     * {@code GET  /admin/scheduling-units/register/export/all} : export all register schedulng unit's to file by flex admin.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_VIEW + "\")")
    @GetMapping("/scheduling-units/register/export/all")
    public ResponseEntity<FileDTO> exportAllRegisterSchedulingUnits(SchedulingUnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export all register scheduling unit");
        criteria.setCertified((BooleanFilter) new BooleanFilter().setEquals(true));
        return super.exportSchedulingUnitToFile(criteria, pageable, Screen.ADMIN_REGISTER_SCHEDULING_UNITS, false);
    }

    /**
     * {@code GET  /admin/scheduling-units/register/export/displayed-data} : export displayed register schedulng unit's to file by flex admin.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_VIEW + "\")")
    @GetMapping("/scheduling-units/register/export/displayed-data")
    public ResponseEntity<FileDTO> exportDisplayedRegisterSchedulingUnits(SchedulingUnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export displayed register scheduling unit");
        criteria.setCertified((BooleanFilter) new BooleanFilter().setEquals(true));
        return super.exportSchedulingUnitToFile(criteria, pageable, Screen.ADMIN_REGISTER_SCHEDULING_UNITS, true);
    }

    /**
     * {@code GET  /scheduling-units/{id}/ders : get all Ders joined to SchedulingUnit grouped by Der Fsp company name.
     *
     * @param schedulingUnitId - SchedulingUnit id
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the map of Ders grouped by Der Fsp company name in body.
     */
    @GetMapping("/scheduling-units/{id}/ders")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<Map<String, List<UnitMinDTO>>> getSchedulingUnitDers(@PathVariable("id") Long schedulingUnitId) {
        log.debug("FLEX-ADMIN - Rest request to get SchedulingUnit Ders");
        return super.getSchedulingUnitDers(schedulingUnitId);
    }


    /**
     * {@code GET  /flex-potentials/minimal/get-all-registered-for-fsp-and-product} : get BSP registered scheduling units for product.
     *
     * @param bspId     the id of Bsp the owner of Scheduling units
     * @param productId the Product id of Scheduling unit to find
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body with the list of flexPotentialMinDTOs}.
     */
    @GetMapping("/scheduling-units/minimal/get-all-registered-for-bsp-and-product")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitDropdownSelectDTO>> findAllRegisteredSchedulingUnitsForBspAndProduct(@RequestParam("bspId") Long bspId,
        @RequestParam(value = "productId") Long productId) {
        log.debug("{} - REST request to get all registered SchedulingUnits for FSP: {} and Product: {}", FLEX_ADMIN_APP_NAME, bspId, productId);
        List<SchedulingUnitDropdownSelectDTO> result = schedulingUnitService.findAllRegisteredSchedulingUnitsForBspAndProduct(bspId, productId);
        if (result.isEmpty()) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(result);
    }
}
