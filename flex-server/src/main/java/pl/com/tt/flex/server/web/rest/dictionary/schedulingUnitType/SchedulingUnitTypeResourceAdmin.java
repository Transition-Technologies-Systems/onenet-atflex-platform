package pl.com.tt.flex.server.web.rest.dictionary.schedulingUnitType;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.SchedulingUnitTypeQueryService;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.SchedulingUnitTypeService;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeCriteria;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeDTO;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeMinDTO;
import pl.com.tt.flex.server.validator.dictionary.SchedulingUnitTypeValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.config.Constants.FLEX_ADMIN_APP_NAME;

/**
 * REST controller for managing {@link SchedulingUnitTypeEntity}.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class SchedulingUnitTypeResourceAdmin extends SchedulingUnitTypeResource {

    public static final String ENTITY_NAME = "schedulingUnitType";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SchedulingUnitTypeService schedulingUnitTypeService;
    private final SchedulingUnitTypeQueryService schedulingUnitTypeQueryService;
    private final SchedulingUnitTypeValidator schedulingUnitTypeValidator;

    public SchedulingUnitTypeResourceAdmin(SchedulingUnitTypeService schedulingUnitTypeService, SchedulingUnitTypeQueryService schedulingUnitTypeQueryService,
                                           SchedulingUnitTypeValidator schedulingUnitTypeValidator) {
        super(schedulingUnitTypeQueryService, schedulingUnitTypeService);
        this.schedulingUnitTypeService = schedulingUnitTypeService;
        this.schedulingUnitTypeQueryService = schedulingUnitTypeQueryService;
        this.schedulingUnitTypeValidator = schedulingUnitTypeValidator;
    }

    /**
     * {@code POST  /su-types} : Create a new schedulingUnitType.
     *
     * @param schedulingUnitTypeDTO the schedulingUnitType to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new schedulingUnitType, or with status {@code 400 (Bad Request)} if the schedulingUnitType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/su-types")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_TYPE_MANAGE + "\")")
    public ResponseEntity<SchedulingUnitTypeDTO> createSchedulingUnitType(@Valid @RequestBody SchedulingUnitTypeDTO schedulingUnitTypeDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("{} - REST request to save SchedulingUnitType : {}", FLEX_ADMIN_APP_NAME, schedulingUnitTypeDTO);
        if (schedulingUnitTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new schedulingUnitType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        schedulingUnitTypeValidator.checkValid(schedulingUnitTypeDTO);
        SchedulingUnitTypeDTO result = schedulingUnitTypeService.save(schedulingUnitTypeDTO);
        schedulingUnitTypeService.sendNotificationInformingAboutCreated(result);
        return ResponseEntity.created(new URI("/api/su-types/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /su-types} : Updates an existing schedulingUnitType.
     *
     * @param schedulingUnitTypeDTO the schedulingUnitTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated schedulingUnitTypeDTO,
     * or with status {@code 400 (Bad Request)} if the schedulingUnitTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the schedulingUnitTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/su-types")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_TYPE_MANAGE + "\")")
    public ResponseEntity<SchedulingUnitTypeDTO> updateSchedulingUnitType(@Valid @RequestBody SchedulingUnitTypeDTO schedulingUnitTypeDTO) throws ObjectValidationException {
        log.debug("{} - REST request to update SchedulingUnitType : {}", FLEX_ADMIN_APP_NAME, schedulingUnitTypeDTO);
        if (schedulingUnitTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        schedulingUnitTypeValidator.checkModifiable(schedulingUnitTypeDTO);
        SchedulingUnitTypeDTO result = schedulingUnitTypeService.save(schedulingUnitTypeDTO);
        schedulingUnitTypeService.sendNotificationInformingAboutModification(result);

        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, schedulingUnitTypeDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /su-types} : get all the schedulingUnitTypes.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of schedulingUnitTypes in body.
     */
    @GetMapping("/su-types")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_TYPE_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitTypeDTO>> getAllSchedulingUnitTypes(SchedulingUnitTypeCriteria criteria, Pageable pageable) {
        log.debug("{} - REST request to get SchedulingUnitTypes by criteria: {}", FLEX_ADMIN_APP_NAME, criteria);
        return super.getAllSchedulingUnitTypes(criteria, pageable);
    }

    /**
     * {@code GET  /su-types/minimal} : get all the schedulingUnitTypes minimal
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of schedulingUnitTypes in body.
     */
    @GetMapping("/su-types/minimal")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_TYPE_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitTypeMinDTO>> getSchedulingUnitTypesMinDto() {
        log.debug("{} - REST request to get SchedulingUnitTypes minimal", FLEX_ADMIN_APP_NAME);
        return super.getSchedulingUnitTypesMinDto();
    }

    /**
     * {@code GET  /su-types/count} : count all the schedulingUnitTypes.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/su-types/count")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_TYPE_VIEW + "\")")
    public ResponseEntity<Long> countSchedulingUnitTypes(SchedulingUnitTypeCriteria criteria) {
        log.debug("{} - REST request to count SchedulingUnitTypes by criteria: {}", FLEX_ADMIN_APP_NAME, criteria);
        return ResponseEntity.ok().body(schedulingUnitTypeQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /su-types/:id} : get the "id" schedulingUnitType.
     *
     * @param id the id of the schedulingUnitTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the schedulingUnitTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/su-types/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_TYPE_VIEW + "\")")
    public ResponseEntity<SchedulingUnitTypeDTO> getSchedulingUnitType(@PathVariable Long id) {
        log.debug("{} - REST request to get SchedulingUnitType : {}", FLEX_ADMIN_APP_NAME, id);
        Optional<SchedulingUnitTypeDTO> schedulingUnitTypeDTO = schedulingUnitTypeService.findById(id);
        return ResponseUtil.wrapOrNotFound(schedulingUnitTypeDTO);
    }


    /**
     * {@code DELETE  /su-types/:id} : delete the "id" schedulingUnitType.
     *
     * @param id the id of the schedulingUnitTypeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/su-types/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_TYPE_DELETE + "\")")
    public ResponseEntity<Void> deleteSchedulingUnitType(@PathVariable Long id) throws ObjectValidationException {
        log.debug("{} - REST request to delete SchedulingUnitType : {}", FLEX_ADMIN_APP_NAME, id);
        schedulingUnitTypeValidator.checkDeletable(id);
        schedulingUnitTypeService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
