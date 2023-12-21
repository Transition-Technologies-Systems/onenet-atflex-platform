package pl.com.tt.flex.server.web.rest.subportfolio;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioQueryService;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioService;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioCriteria;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioMinDTO;
import pl.com.tt.flex.server.service.subportfolio.mapper.SubportfolioMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.subportfolio.SubportfolioValidator;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link SubportfolioEntity} for FLEX-ADMIN web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class SubportfolioResourceAdmin extends SubportfolioResource {

    public SubportfolioResourceAdmin(SubportfolioService subportfolioService, SubportfolioQueryService subportfolioQueryService,
        SubportfolioMapper subportfolioMapper, SubportfolioValidator subportfolioValidator, UserService userService) {
        super(subportfolioService, subportfolioQueryService, subportfolioMapper, subportfolioValidator, userService);
    }

    /**
     * {@code POST  /subportfolio} : Create a new subportfolio.
     *
     * @param subportfolioDTO the subportfolioDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new subportfolioDTO, or with status {@code 400 (Bad Request)} if the subportfolio has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/subportfolio/create")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SUBPORTFOLIO_MANAGE + "\")")
    public ResponseEntity<SubportfolioDTO> createSubportfolio(@Valid @RequestPart SubportfolioDTO subportfolioDTO,
        @RequestPart(value = "files", required = false) MultipartFile[] files) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to save Subportfolio : {}", subportfolioDTO);
        return super.createSubportfolio(subportfolioDTO, files);
    }

    /**
     * {@code POST  /subportfolio} : Updates an existing subportfolio.
     *
     * @param subportfolioDTO the subportfolioDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated subportfolioDTO,
     * or with status {@code 400 (Bad Request)} if the subportfolioDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the subportfolioDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/subportfolio/update")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SUBPORTFOLIO_MANAGE + "\")")
    public ResponseEntity<SubportfolioDTO> updateSubportfolio(@Valid @RequestPart SubportfolioDTO subportfolioDTO,
        @RequestPart(value = "files", required = false) MultipartFile[] files) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to update Subportfolio : {}", subportfolioDTO);
        return super.updateSubportfolio(subportfolioDTO, files);
    }

    /**
     * {@code GET  /subportfolio} : get all the subportfolio.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of subportfolio in body.
     */
    @GetMapping("/subportfolio")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SUBPORTFOLIO_VIEW + "\")")
    public ResponseEntity<List<SubportfolioDTO>> getAllSubportfolio(SubportfolioCriteria criteria, Pageable pageable) {
        log.debug("FLEX-ADMIN - REST request to get Subportfolio by criteria: {}", criteria);
        return super.getAllSubportfolios(criteria, pageable);
    }

    /**
     * {@code GET  /subportfolio/:id} : get the "id" subportfolio.
     *
     * @param id the id of the subportfolioDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the subportfolioDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/subportfolio/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SUBPORTFOLIO_VIEW + "\")")
    public ResponseEntity<SubportfolioDTO> getSubportfolio(@PathVariable Long id) {
        log.debug("FLEX-ADMIN - REST request to get Subportfolio : {}", id);
        return super.getSubportfolio(id);
    }

    /**
     * {@code DELETE  /subportfolio/:id} : delete the "id" subportfolio.
     *
     * @param id the id of the subportfolioDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/subportfolio/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SUBPORTFOLIO_DELETE + "\")")
    public ResponseEntity<Void> deleteSubportfolio(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to delete Subportfolio : {}", id);
        return super.deleteSubportfolio(id);
    }

    /**
     * {@code GET  /subportfolio/files/:fileId} : get file from subportfolio
     *
     * @param fileId the id of the file attached to subportfolio.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fileDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/subportfolio/files/{fileId}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SUBPORTFOLIO_VIEW + "\")")
    public ResponseEntity<FileDTO> getSubportfolioFile(@PathVariable Long fileId) {
        log.debug("FLEX-ADMIN - REST request to get Subportfolio [id: {}]", fileId);
        return super.getSubportfolioFile(fileId);
    }

    /**
     * {@code GET  /admin/subportfolio/export/all} : export all subportfolio's to file by flex admin.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SUBPORTFOLIO_VIEW + "\")")
    @GetMapping("/subportfolio/export/all")
    public ResponseEntity<FileDTO> exportAllSubportfolios(SubportfolioCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export all subportfolios");
        return super.exportAllSubportfolios(criteria, pageable, Screen.ADMIN_SUBPORTFOLIO);
    }

    /**
     * {@code GET  /admin/subportfolio/export/displayed-data} : export displayed subportfolio's to file by flex admin.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SUBPORTFOLIO_VIEW + "\")")
    @GetMapping("/subportfolio/export/displayed-data")
    public ResponseEntity<FileDTO> exportDisplayedSubportfolios(SubportfolioCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export displayed subportfolios");
        return super.exportDisplayedSubportfolios(criteria, pageable, Screen.ADMIN_SUBPORTFOLIO);
    }


    @GetMapping("/subportfolioNames")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SUBPORTFOLIO_VIEW + "\")")
    public ResponseEntity<List<String>> getAllSubportfolioNames() {
        log.debug("FLEX-ADMIN - REST request to get all Subportfolio names");
        return super.getAllSubportfolioNames();
    }

    /**
     * {@code GET  /subportfolio/minimal/get-fspa-certified-subs} : get all FSPA certified Subportfolios.
     *
     * @param fspaId Fsp owner of Subportfolios
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of subportfolio in body.
     */
    @GetMapping("/subportfolio/minimal/get-fspa-certified-subs")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SUBPORTFOLIO_VIEW + "\")")
    public ResponseEntity<List<SubportfolioMinDTO>> findAllFspaCertifiedSubportfoliosMin(@RequestParam(value = "fspaId") Long fspaId) {
        log.debug("FLEX-ADMIN - REST request to get all FSPA certified subportfolios");
        List<SubportfolioMinDTO> result = subportfolioService.findAllFspaCertifiedSubportfoliosMin(fspaId);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

}
