package pl.com.tt.flex.server.web.rest.kpi;

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
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiDTO;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.kpi.KpiEntity;
import pl.com.tt.flex.server.domain.kpi.KpiView_;
import pl.com.tt.flex.server.service.kpi.KpiQueryService;
import pl.com.tt.flex.server.service.kpi.KpiService;
import pl.com.tt.flex.server.service.kpi.dto.KpiCriteria;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerateException;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.kpi.KpiValidator;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.service.common.QueryServiceUtil.replaceSortForLang;

/**
 * REST controller for managing {@link KpiEntity}
 */
@Slf4j
@RestController
@RequestMapping("/api/kpis")
public class KpiResource {

    public static final String ENTITY_NAME = "kpi";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final KpiService kpiService;

    private final KpiQueryService kpiQueryService;

    private final KpiValidator kpiValidator;

    private final UserService userService;

    public KpiResource(KpiService kpiService, KpiQueryService kpiQueryService, KpiValidator kpiValidator, UserService userService) {
        this.kpiService = kpiService;
        this.kpiQueryService = kpiQueryService;
        this.kpiValidator = kpiValidator;
        this.userService = userService;
    }

    /**
     * {@code POST  /kpis} : Create a new kpi
     *
     * @param kpiType the kpiType to create.
     * @return the {@link ResponseEntity} with status {@code 200 and with kpi file in body, or with status {@code 400 (Bad Request)} if the kpi has already an ID.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KPI_MANAGE + "\")")
    @PostMapping("/{kpiType}")
    public ResponseEntity<FileDTO> createKpi(@PathVariable KpiType kpiType,
        @RequestParam(required = false) Instant dateFrom,
        @RequestParam(required = false) Instant dateTo) throws ObjectValidationException, KpiGenerateException {

        KpiDTO kpiDTO = KpiDTO.builder()
            .type(kpiType)
            .dateTo(dateTo)
            .dateFrom(dateFrom)
            .build();
        log.debug("FLEX-ADMIN - REST request to save kpi : {}", kpiType);
        kpiValidator.checkCreated(kpiDTO);
        kpiValidator.checkValid(kpiDTO);
        FileDTO result = kpiService.saveAndGenerateFile(kpiDTO);
        return ResponseEntity.ok().body(result);
    }

    /**
     * {@code POST  /kpis} : Create a new kpi
     *
     * @param id the kpi id to regenerate.
     * @return the {@link ResponseEntity} with status {@code 200 and with kpi file in body
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KPI_MANAGE + "\")")
    @PostMapping("/{id}/regenerate")
    public ResponseEntity<FileDTO> regenerate(@PathVariable Long id) throws ObjectValidationException, KpiGenerateException {
        log.debug("FLEX-ADMIN - REST request to regenerate kpi id: {}", id);
        Optional<FileDTO> result = kpiService.regenerate(id);
        return ResponseUtil.wrapOrNotFound(result);
    }

    /**
     * {@code GET  /kpis} : get all the kpis.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of kpis in body.
     */
    @GetMapping()
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KPI_VIEW + "\")")
    public ResponseEntity<List<KpiDTO>> getAllKpis(KpiCriteria criteria, Pageable pageable) {
        log.debug("REST request to get KPIs by criteria");

        pageable = replaceSortForLang(
                KpiView_.TYPE,
                Map.of(
                        "pl", KpiView_.TYPE_ORDER_PL,
                        "en", KpiView_.TYPE_ORDER_EN
                ),
                pageable, userService.getLangKeyForCurrentLoggedUser()
        );

        Page<KpiDTO> page = kpiQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /kpis/types} : get all the kpi types.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of kpi types in body.
     */
    @GetMapping("/types")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KPI_VIEW + "\")")
    public ResponseEntity<List<KpiType.KpiTypeDTO>> getAllTypes() {
        log.debug("REST request to get KPI types");
        return ResponseEntity.ok().body(kpiService.getAllTypes());
    }

    /**
     * {@code GET  /kpis/:id} : get the "id" kpi.
     *
     * @param id the id of the kpiDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fspDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KPI_VIEW + "\")")
    public ResponseEntity<KpiDTO> getKpi(@PathVariable Long id) {
        log.debug("REST request to get Kpi [id: {}]", id);
        Optional<KpiDTO> kpiDTO = kpiService.findById(id);
        return ResponseUtil.wrapOrNotFound(kpiDTO);
    }

    /**
     * {@code DELETE  /kpis/:id} : delete the "id" kpi.
     *
     * @param id the id of the kpiDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}
     * or with status {@code 400 (Bad Request)}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KPI_DELETE + "\")")
    public ResponseEntity<Void> deleteKpi(@PathVariable Long id) throws ObjectValidationException {
        log.debug("REST request to delete Kpi : {}", id);
        kpiValidator.checkDeletable(id);
        kpiService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
