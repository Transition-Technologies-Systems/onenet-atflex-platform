package pl.com.tt.flex.server.web.rest.fsp;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.fsp.FspQueryService;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.fsp.dto.FspCriteria;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.fsp.FspValidator;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.domain.screen.enumeration.Screen.ADMIN_BSP;
import static pl.com.tt.flex.server.domain.screen.enumeration.Screen.ADMIN_FSP;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.FSP_NOTHING_TO_EXPORT;

/**
 * REST controller for managing {@link FspEntity} for FLEX-ADMIN web module - Fsp is created with new User as a result of accepted FspUserRegistration
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class FspResourceAdmin {

    public static final String ENTITY_NAME = "fsp";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FspService fspService;

    private final FspQueryService fspQueryService;

    private final UserService userService;

    private final FspValidator fspValidator;

    public FspResourceAdmin(FspService fspService, FspQueryService fspQueryService, UserService userService, FspValidator fspValidator) {
        this.fspService = fspService;
        this.fspQueryService = fspQueryService;
        this.userService = userService;
        this.fspValidator = fspValidator;
    }

    /**
     * {@code PUT  /fsps} : Updates an existing fsp.
     * Operation also handles deactivation of fsp.
     *
     * @param fspDTO the fspDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated fspDTO or with status {@code 404 (Not Found)},
     * or with status {@code 400 (Bad Request)} if the fspDTO is not valid with key:
     *  - fromDateBeforeCreatedDate
     *  - fromDateAfterToDate
     *  - cannotDeactivateBecauseOfActivePotentials if updated dto.active=false and fsp has attached active flexPotentials
     * or with status {@code 500 (Internal Server Error)} if the fspDTO couldn't be updated.
     */
    @PutMapping("/fsps")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_MANAGE + "\")")
    public ResponseEntity<FspDTO> updateFsp(@Valid @RequestBody FspDTO fspDTO) throws ObjectValidationException {
        log.debug("REST request to update Fsp [id: {}]", fspDTO.getId());
        fspDTO = fspValidator.overwriteOnlyAllowedFspDtoFieldsForCurrentUser(fspDTO);
        FspDTO oldFspDTO = fspService.findById(fspDTO.getId()).get();
        fspValidator.validUpdateRequest(fspDTO);
        FspDTO result = fspService.save(fspDTO);
        fspService.registerUpdateNotification(oldFspDTO, result);
        fspService.sendMailInformingAboutModification(oldFspDTO, result);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, fspDTO.getId().toString())).body(result);
    }

    /**
     * {@code GET  /fsps} : get all the fsps.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fsps in body.
     */
    @GetMapping("/fsps")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_VIEW + "\")")
    public ResponseEntity<List<FspDTO>> getAllFsps(FspCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Fsps by criteria");
        criteria.setDeleted((BooleanFilter) new BooleanFilter().setEquals(false));
        Page<FspDTO> page = fspQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /fsps/get-company} : get all the fsps minimal data with company name and role.
     *
     * @param roles roles which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fsps in body.
     */
    @GetMapping("/fsps/get-company")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_VIEW + "\")")
    public ResponseEntity<List<FspCompanyMinDTO>> getAllFspCompanyNameByRoles(@RequestParam("roles") List<Role> roles,
                                                                              @RequestParam(value = "active", defaultValue = "true") boolean active) {
        log.debug("REST request to get Fsps company names by roles: {} and active: {}", roles, active);
        if(!active) {
            return ResponseEntity.ok().body(fspService.getAllFspCompanyNamesByRoles(roles));
        }
        return ResponseEntity.ok().body(fspService.getAllFspCompanyNamesByActiveAndRoles(roles));
    }

    /**
     * {@code GET  /fsps/minimal/get-bsps-with-not-empty-scheduling-units} : get all the fsps minimal data with role BSP and not empty bsp's schedulingUnits collection.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fsps in body.
     */
    @GetMapping("/fsps/minimal/get-bsps-with-not-empty-scheduling-units")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_VIEW + "\")")
    public ResponseEntity<List<FspCompanyMinDTO>> getBspsWithNotEmptySchedulingUnits() {
        log.debug("REST request to get all the fsps minimal data with role BSP and not empty BSP's schedulingUnits collection");
        return ResponseEntity.ok().body(fspService.getBspsWithNotEmptySchedulingUnitsMinimal());
    }

    /**
     * {@code GET  /fsps/get-company-attached-unit} : get all the fsps minimal data with company name and role attached to active and certified Unit.
     *
     * @param roles roles which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fsps in body.
     */
    @GetMapping("/fsps/get-company-attached-unit")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_VIEW + "\")")
    public ResponseEntity<Set<FspCompanyMinDTO>> getAllFspCompanyNameByRolesAttachedToUnit(@RequestParam("roles") List<Role> roles) {
        log.debug("REST request to get Fsps company names by roles attached to active and certified Unit");
        return ResponseEntity.ok().body(fspService.getAllFspCompanyNamesByRolesWhereAttachedUnitIsActiveAndCertified(roles));
    }

    /**
     * {@code GET  /fsps/minimal/get-fsps-with-registered-potentials} : get all the fsps minimal data of FSP/FSPA having registered flex potentials.
     *
     * @param productId the Product id of Flex potential to find
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fsps in body.
     */
    @GetMapping("/fsps/minimal/get-fsps-with-registered-potentials")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_VIEW + "\")")
    public ResponseEntity<List<FspCompanyMinDTO>> getFspsWithRegisteredPotentials(@RequestParam(value = "productId") Long productId) {
        log.debug("REST request to get all the fsps minimal data of FSP/FSPA having registered flex potentials for Product: {}", productId);
        List<FspCompanyMinDTO> result = fspService.findFspsWithRegisteredPotentialsForProduct(productId);
        if (result.isEmpty()) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code GET  /fsps/count} : count all the fsps.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/fsps/count")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_VIEW + "\")")
    public ResponseEntity<Long> countFsps(FspCriteria criteria) {
        log.debug("REST request to count Fsps by criteria");
        return ResponseEntity.ok().body(fspQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /fsps/:id} : get the "id" fsp.
     *
     * @param id the id of the fspDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fspDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/fsps/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_VIEW + "\")")
    public ResponseEntity<FspDTO> getFsp(@PathVariable Long id) {
        log.debug("REST request to get Fsp [id: {}]", id);
        Optional<FspDTO> fspDTO = fspService.findById(id);
        return ResponseUtil.wrapOrNotFound(fspDTO);
    }

    /**
     * {@code DELETE  /fsps/:id} : delete the "id" fsp.
     *
     * @param id the id of the fspDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}
     * or with status {@code 400 (Bad Request)}, if the fspDTO is not valid, with key:
     * - cannotDeleteBecauseOfJoinedPotentials if updated fsp has joined any flexPotentials
     */
    @DeleteMapping("/fsps/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_DELETE + "\")")
    public ResponseEntity<Void> deleteFsp(@PathVariable Long id) throws ObjectValidationException {
        log.debug("REST request to delete Fsp : {}", id);
        fspValidator.checkDeletable(id);
        fspService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    // Pobrane listy obiektow za pomoca metod findByCriteria(Criteria criteria, Sort sort) oraz findByCriteria(Criteria criteria, Pageable pageable)
    // roznia sie w kolejnosci sortowania obiektow. Z tego wzgledu do pobierania listy obiektow do eksportu uzyto metody findByCriteria(Criteria criteria, Pageable pageable)
    // tej samej co do pobrania listy obiektow na front (do widoku).

    /**
     * {@code GET  /fsps/export/all} : export all fsps data to file.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_VIEW + "\")")
    @GetMapping("/fsps/export/all")
    public ResponseEntity<FileDTO> exportAllFsps(FspCriteria criteria, Pageable pageable) throws IOException {
        log.debug("REST request to export fsps");
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        criteria.setDeleted((BooleanFilter) new BooleanFilter().setEquals(false));
        int size = (int) fspQueryService.countByCriteria(criteria);
        PageRequest pageRequest = PageRequest.of(0, size, pageable.getSort());
        Page<FspDTO> fspDTOPage = fspQueryService.findByCriteria(criteria, pageRequest);
        return ResponseEntity.ok().body(fspService.exportFspsToFile(fspDTOPage.getContent(), langKey, false, getScreenNameByRole(criteria.getRole())));
    }

    /**
     * {@code GET  /fsps/export/displayed-data} : export fsps to file.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_VIEW + "\")")
    @GetMapping("/fsps/export/displayed-data")
    public ResponseEntity<FileDTO> exportDisplayedFsps(FspCriteria criteria, Pageable pageable) throws IOException {
        log.debug("REST request to export fsps");
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        criteria.setDeleted((BooleanFilter) new BooleanFilter().setEquals(false));
        int size = (int) fspQueryService.countByCriteria(criteria);
        if (size == 0) {
            throw new ObjectValidationException("Nothing to export", FSP_NOTHING_TO_EXPORT);
        }
        PageRequest pageRequest = PageRequest.of(0, size, pageable.getSort());
        Page<FspDTO> fspDTOPage = fspQueryService.findByCriteria(criteria, pageRequest);
        return ResponseEntity.ok().body(fspService.exportFspsToFile(fspDTOPage.getContent(), langKey, true, getScreenNameByRole(criteria.getRole())));
    }

    private Screen getScreenNameByRole(FspCriteria.RoleFilter roleFilter) {
        if (Objects.isNull(roleFilter) || Objects.isNull(roleFilter.getIn())) {
            throw new FspResourceException("Cannot find screen for role, because role filter (IN) is null");
        }

        if (roleFilter.getIn().containsAll(List.of(Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED))) {
            return ADMIN_FSP;
        } else if (roleFilter.getIn().contains(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            return ADMIN_BSP;
        } else {
            throw new FspResourceException("Cannot find screen for role: " + roleFilter.getIn());
        }
    }

    protected static class FspResourceException extends RuntimeException {
        public FspResourceException(String message) {
            super(message);
        }
    }
}
