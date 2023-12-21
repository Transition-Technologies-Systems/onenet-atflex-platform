package pl.com.tt.flex.server.web.rest.dictionary.derType;

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
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.service.dictionary.derType.DerTypeQueryService;
import pl.com.tt.flex.server.service.dictionary.derType.DerTypeService;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeCriteria;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeDTO;
import pl.com.tt.flex.server.validator.dictionary.DerTypeValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link DerTypeEntity}.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class DerTypeResource {

    public static final String ENTITY_NAME = "derType";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DerTypeService derTypeService;
    private final DerTypeQueryService derTypeQueryService;
    private final DerTypeValidator derTypeValidator;

    public DerTypeResource(DerTypeService derTypeService, DerTypeQueryService derTypeQueryService, DerTypeValidator derTypeValidator) {
        this.derTypeService = derTypeService;
        this.derTypeQueryService = derTypeQueryService;
        this.derTypeValidator = derTypeValidator;
    }

    /**
     * {@code POST  /der-types} : Create a new derType.
     *
     * @param derTypeDTO the derTypeDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new derTypeDTO, or with status {@code 400 (Bad Request)} if the derType has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/der-types")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_DER_TYPE_MANAGE + "\") or hasAuthority(\"" + FLEX_USER_DER_TYPE_MANAGE + "\")")
    public ResponseEntity<DerTypeDTO> createDerType(@Valid @RequestBody DerTypeDTO derTypeDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("REST request to save DerType : {}", derTypeDTO);
        if (derTypeDTO.getId() != null) {
            throw new BadRequestAlertException("A new derType cannot already have an ID", ENTITY_NAME, "idexists");
        }
        derTypeValidator.checkCreateRequest(derTypeDTO);
        DerTypeDTO result = derTypeService.save(derTypeDTO);
        derTypeService.sendNotificationInformingAboutCreated(result);
        return ResponseEntity.created(new URI("/api/der-types/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * {@code PUT  /der-types} : Updates an existing derType.
     *
     * @param derTypeDTO the derTypeDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated derTypeDTO,
     * or with status {@code 400 (Bad Request)} if the derTypeDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the derTypeDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/der-types")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_DER_TYPE_MANAGE + "\") or hasAuthority(\"" + FLEX_USER_DER_TYPE_MANAGE + "\")")
    public ResponseEntity<DerTypeDTO> updateDerType(@Valid @RequestBody DerTypeDTO derTypeDTO) throws ObjectValidationException {
        log.debug("REST request to update DerType : {}", derTypeDTO);
        if (derTypeDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        derTypeValidator.checkUpdatableRequest(derTypeDTO);
        DerTypeDTO result = derTypeService.save(derTypeDTO);
        derTypeService.sendNotificationInformingAboutModification(result);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, derTypeDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /der-types} : get all the derTypes.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of derTypes in body.
     */
    @GetMapping("/der-types")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_DER_TYPE_VIEW + "\") or hasAuthority(\"" + FLEX_USER_DER_TYPE_VIEW + "\")")
    public ResponseEntity<List<DerTypeDTO>> getAllDerTypes(DerTypeCriteria criteria, Pageable pageable) {
        log.debug("REST request to get DerTypes by criteria: {}", criteria);
        Page<DerTypeDTO> page = derTypeQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /der-types/count} : count all the derTypes.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/der-types/count")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_DER_TYPE_VIEW + "\") or hasAuthority(\"" + FLEX_USER_DER_TYPE_VIEW + "\")")
    public ResponseEntity<Long> countDerTypes(DerTypeCriteria criteria) {
        log.debug("REST request to count DerTypes by criteria: {}", criteria);
        return ResponseEntity.ok().body(derTypeQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /der-types/:id} : get the "id" derType.
     *
     * @param id the id of the derTypeDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the derTypeDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/der-types/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_DER_TYPE_VIEW + "\") or hasAuthority(\"" + FLEX_USER_DER_TYPE_VIEW + "\")")
    public ResponseEntity<DerTypeDTO> getDerType(@PathVariable Long id) {
        log.debug("REST request to get DerType : {}", id);
        Optional<DerTypeDTO> derTypeDTO = derTypeService.findById(id);
        return ResponseUtil.wrapOrNotFound(derTypeDTO);
    }


    /**
     * {@code DELETE  /der-types/:id} : delete the "id" derType.
     *
     * @param id the id of the derTypeDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/der-types/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_DER_TYPE_DELETE + "\") or hasAuthority(\"" + FLEX_USER_DER_TYPE_DELETE + "\")")
    public ResponseEntity<Void> deleteDerType(@PathVariable Long id) throws ObjectValidationException {
        log.debug("REST request to delete DerType : {}", id);
        derTypeValidator.checkDeletable(id);
        derTypeService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
