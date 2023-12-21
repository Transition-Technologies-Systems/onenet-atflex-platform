package pl.com.tt.flex.server.web.rest.dictionary.localizationType;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.model.service.dto.localization.LocalizationType;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.service.dictionary.localizationType.LocalizationTypeQueryService;
import pl.com.tt.flex.server.service.dictionary.localizationType.LocalizationTypeService;
import pl.com.tt.flex.server.service.dictionary.localizationType.dto.LocalizationTypeCriteria;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link LocalizationTypeEntity} for FLEX-ADMIN web module
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class LocalizationTypeResourceAdmin {

    public static final String ENTITY_NAME = "localizationType";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final LocalizationTypeService localizationTypeService;
    private final LocalizationTypeQueryService localizationTypeQueryService;

    public LocalizationTypeResourceAdmin(LocalizationTypeService localizationTypeService, LocalizationTypeQueryService localizationTypeQueryService) {
        this.localizationTypeService = localizationTypeService;
        this.localizationTypeQueryService = localizationTypeQueryService;
    }

    /**
     * {@code POST  /localization-types} : Create a new localizationType.
     *
     * @param localizationTypeDTO the localizationType to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new localizationType, or with status {@code 400 (Bad Request)} if the localizationType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/localization-types")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_LOCALIZATION_TYPE_MANAGE + "\")")
    public ResponseEntity<LocalizationTypeDTO> createLocalizationType(@Valid @RequestBody LocalizationTypeDTO localizationTypeDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to save LocalizationType : {}", localizationTypeDTO);
        if (localizationTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new localizationType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        LocalizationTypeDTO result = localizationTypeService.saveType(localizationTypeDTO);
        localizationTypeService.sendNotificationInformingAboutCreated(result);
        return ResponseEntity.created(new URI("/api/localization-types/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /localization-types} : Updates an existing localizationType.
     *
     * @param localizationTypeDTO the localizationTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated localizationTypeDTO,
     * or with status {@code 400 (Bad Request)} if the localizationTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the localizationTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/localization-types")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_LOCALIZATION_TYPE_MANAGE + "\")")
    public ResponseEntity<LocalizationTypeDTO> updateLocalizationType(@Valid @RequestBody LocalizationTypeDTO localizationTypeDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to update LocalizationType : {}", localizationTypeDTO);
        if (localizationTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        LocalizationTypeDTO result = localizationTypeService.saveType(localizationTypeDTO);
        localizationTypeService.sendNotificationInformingAboutModification(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, localizationTypeDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /localization-types} : get all the localizationTypes.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of localizationTypes in body.
     */
    @GetMapping("/localization-types")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_LOCALIZATION_TYPE_VIEW + "\")")
    public ResponseEntity<List<LocalizationTypeDTO>> getAllLocalizationTypes(LocalizationTypeCriteria criteria, Pageable pageable) {
        log.debug("FLEX-ADMIN - REST request to get LocalizationTypes by criteria: {}", criteria);
        Page<LocalizationTypeDTO> page = localizationTypeQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /localization-types/count} : count all the localizationTypes.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/localization-types/count")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_LOCALIZATION_TYPE_VIEW + "\")")
    public ResponseEntity<Long> countLocalizationTypes(LocalizationTypeCriteria criteria) {
        log.debug("FLEX-ADMIN - REST request to count LocalizationTypes by criteria: {}", criteria);
        return ResponseEntity.ok().body(localizationTypeQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /localization-types/:id} : get the "id" localizationType.
     *
     * @param id the id of the localizationTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the localizationTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/localization-types/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_LOCALIZATION_TYPE_VIEW + "\")")
    public ResponseEntity<LocalizationTypeDTO> getLocalizationType(@PathVariable Long id) {
        log.debug("FLEX-ADMIN - REST request to get LocalizationType : {}", id);
        Optional<LocalizationTypeDTO> localizationTypeDTO = localizationTypeService.findById(id);
        return ResponseUtil.wrapOrNotFound(localizationTypeDTO);
    }


    /**
     * {@code DELETE  /localization-types/:id} : delete the "id" localizationType.
     *
     * @param id the id of the localizationTypeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/localization-types/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_LOCALIZATION_TYPE_DELETE + "\")")
    public ResponseEntity<Void> deleteLocalizationType(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to delete LocalizationType : {}", id);
        localizationTypeService.deleteType(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }


    /**
     * {@code GET  /localization-types/get-by-type} : get all the localization dictionary by type.
     *
     * @param types the types of Localization dictionary. Example : {@link LocalizationType#COUPLING_POINT_ID}
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of localizationTypes in body.
     */
    @GetMapping("/localization-types/get-by-type")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_LOCALIZATION_TYPE_VIEW + "\")")
    public ResponseEntity<List<LocalizationTypeDTO>> getAllByType(@RequestParam List<LocalizationType> types) {
        log.debug("FLEX-ADMIN - REST request to get LocalizationTypes by types: {}", types);
        List<LocalizationTypeDTO> localizationTypeDTOS = localizationTypeService.findAllByTypes(types);
        return ResponseEntity.ok().body(localizationTypeDTOS);
    }

    @GetMapping("/localization-types/get-by-unit-ids")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_LOCALIZATION_TYPE_VIEW + "\")")
    public ResponseEntity<List<LocalizationTypeDTO>> getAllByUnitId(@RequestParam List<Long> unitIds) {
        log.debug("FLEX-ADMIN - REST request to get LocalizationTypes by unitIds");
        List<LocalizationTypeDTO> localizationTypeDTOS = localizationTypeService.findAllByUnitIds(unitIds);
        return ResponseEntity.ok().body(localizationTypeDTOS);
    }
}
