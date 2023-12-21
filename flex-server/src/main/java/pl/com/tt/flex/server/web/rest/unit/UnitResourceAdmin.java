package pl.com.tt.flex.server.web.rest.unit;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;
import pl.com.tt.flex.server.service.unit.UnitQueryService;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitCriteria;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitGeoLocationMapper;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.unit.UnitValidator;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static java.util.Objects.nonNull;
import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link UnitEntity} for FLEX-ADMIN web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class UnitResourceAdmin extends UnitResource {

    private final FlexPotentialService flexPotentialService;

    public UnitResourceAdmin(UnitService unitService, UnitQueryService unitQueryService, UnitMapper unitMapper, UnitGeoLocationMapper unitGeoLocationMapper,
        UnitValidator unitValidator, UserService userService, FlexPotentialService flexPotentialService) {
        super(unitService, unitQueryService, unitMapper, unitGeoLocationMapper, unitValidator, userService);
        this.flexPotentialService = flexPotentialService;
    }

    /**
     * {@code POST  /units} : FLEX-ADMIN - Create a new unit.
     *
     * @param unitDTO the unitDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new unitDTO, or with status {@code 400 (Bad Request)} if the unit has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/units")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_UNIT_MANAGE + "\")")
    public ResponseEntity<UnitDTO> createUnit(@Valid @RequestBody UnitDTO unitDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to save Unit : {}", unitDTO);
        return super.createUnit(unitDTO);
    }

    /**
     * {@code PUT  /admin/units} : Updates an existing unit.
     *
     * @param unitDTO the unitDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated unitDTO,
     * or with status {@code 400 (Bad Request)} if the unitDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the unitDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/units")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_UNIT_MANAGE + "\")")
    public ResponseEntity<UnitDTO> updateUnit(@Valid @RequestBody UnitDTO unitDTO) throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to update Unit : {}", unitDTO);
        return super.updateUnit(unitDTO);
    }

    /**
     * {@code GET  /admin/units} : FLEX-ADMIN - get all the units.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of units in body.
     */
    @GetMapping("/units")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_UNIT_VIEW + "\")")
    public ResponseEntity<List<UnitDTO>> getAllUnits(UnitCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Units by criteria: {}", criteria);
        Page<UnitDTO> page = unitQueryService.findByCriteria(criteria, pageable);
        page.forEach(this::setFlagBalancedByFlexPotentialProduct);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    private void setFlagBalancedByFlexPotentialProduct(UnitDTO unit) {
        unit.setBalancedByFlexPotentialProduct(flexPotentialService.isDerOfFspBalancedByRegisteredFlexPotentialProduct(unit.getId()));
    }

    @GetMapping("/units/get-all")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_UNIT_VIEW + "\")")
    public ResponseEntity<List<UnitMinDTO>> getAllUnitsMinimal(UnitCriteria unitCriteria) {
        return super.getAllUnitsMinimal(unitCriteria);
    }

    /**
     * {@code GET  /admin/units/:id} : get the "id" unit.
     *
     * @param id the id of the unitDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the unitDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/units/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_UNIT_VIEW + "\")")
    public ResponseEntity<UnitDTO> getUnit(@PathVariable Long id) {
        log.debug("FLEX-ADMIN - REST request to get Unit : {}", id);
        ResponseEntity<UnitDTO> result = super.getUnit(id);
        if (nonNull(result.getBody())) {
            setFlagBalancedByFlexPotentialProduct(result.getBody());
        }
        return result;
    }

    /**
     * {@code DELETE  /units/:id} : delete the "id" unit.
     *
     * @param id the id of the unitDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/units/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_UNIT_DELETE + "\")")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to delete Unit : {}", id);
        return super.deleteUnit(id);
    }

    /**
     * {@code GET  /admin/units/export/all} : export all units's to file by flex admin.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_UNIT_VIEW + "\")")
    @GetMapping("/units/export/all")
    public ResponseEntity<FileDTO> exportUnitsAll(UnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("REST request to export all units by Admin");
        return super.exportUnits(criteria, pageable, Screen.ADMIN_UNITS, false);
    }

    /**
     * {@code GET  /admin/units/export/displayed-data} : export displayed units's to file by flex admin.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_UNIT_VIEW + "\")")
    @GetMapping("/units/export/displayed-data")
    public ResponseEntity<FileDTO> exportUnitsDisplayed(UnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("REST request to export displayed units by Admin");
        return super.exportUnits(criteria, pageable, Screen.ADMIN_UNITS, true);
    }

    @GetMapping("/units/get-all-for-subportfolio-modal-select")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_UNIT_VIEW + "\")")
    public ResponseEntity<List<UnitMinDTO>> getAllForSubportfolioModalSelect(@RequestParam(value = "fspaId") Long fspaId,
        @RequestParam(value = "subportfolioId", required = false) Long subportfolioId) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to get all Units by fspaId and no subportfolio");
        return super.getAllForSubportfolioModalSelect(fspaId, subportfolioId);
    }

    @GetMapping("/units/get-all-for-fsp")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_UNIT_MANAGE + "\")")
    public  ResponseEntity<List<UnitMinDTO>> getAllByFspId(@RequestParam(value = "fspaId") Long fspaId) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to get all Units for fspa id: {}", fspaId);
        return super.getAllByFspId(fspaId);
    }

}
