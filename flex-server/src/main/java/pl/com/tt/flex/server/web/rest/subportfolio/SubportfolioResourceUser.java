package pl.com.tt.flex.server.web.rest.subportfolio;

import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioFileEntity;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioQueryService;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioService;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioCriteria;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioMinDTO;
import pl.com.tt.flex.server.service.subportfolio.mapper.SubportfolioMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.subportfolio.SubportfolioValidator;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link SubportfolioEntity} for FLEX-USER web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class SubportfolioResourceUser extends SubportfolioResource {

    private final FspService fspService;


    public SubportfolioResourceUser(SubportfolioService subportfolioService, SubportfolioQueryService subportfolioQueryService,
        SubportfolioMapper subportfolioMapper, SubportfolioValidator subportfolioValidator, UserService userService, FspService fspService) {
        super(subportfolioService, subportfolioQueryService, subportfolioMapper, subportfolioValidator, userService);
        this.fspService = fspService;
    }

    /**
     * {@code POST  /subportfolio} : Create a new subportfolio.
     *
     * @param subportfolioDTO the subportfolioDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new subportfolio, or with status {@code 400 (Bad Request)} if the subportfolio has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/subportfolio/create")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SUBPORTFOLIO_MANAGE + "\")")
    public ResponseEntity<SubportfolioDTO> createSubportfolio(@Valid @RequestPart SubportfolioDTO subportfolioDTO,
        @RequestPart(value = "files", required = false) MultipartFile[] files) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-USER - REST request to save Subportfolio : {}", subportfolioDTO);
        UserDTO currentUser = userService.getCurrentUserDTO().orElseThrow(() -> new SubportfolioResourceException("Current logged-in user not found"));
        if (!currentUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            log.warn("createSubportfolio() User is not authorized to create Subportfolio");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        subportfolioDTO.setFspId(currentUser.getFspId());
        return super.createSubportfolio(subportfolioDTO, files);
    }

    /**
     * {@code POST  /subportfolio-units} : Updates an existing subportfolio.
     *
     * @param subportfolioDTO the subportfolioDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated subportfolioDTO,
     * or with status {@code 400 (Bad Request)} if the subportfolioDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the subportfolioDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/subportfolio/update")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SUBPORTFOLIO_MANAGE + "\")")
    public ResponseEntity<SubportfolioDTO> updateSubportfolio(@Valid @RequestPart SubportfolioDTO subportfolioDTO,
        @RequestPart(value = "files", required = false) MultipartFile[] files) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-USER - REST request to update Subportfolio : {}", subportfolioDTO);
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
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SUBPORTFOLIO_VIEW + "\")")
    public ResponseEntity<List<SubportfolioDTO>> getAllSubportfolio(SubportfolioCriteria criteria, Pageable pageable) {
        log.debug("FLEX-USER - REST request to get Subportfolio by criteria: {}", criteria);
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SubportfolioResourceException("Current logged-in user not found"));
        if (fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            criteria.setFspaId((LongFilter) new LongFilter().setEquals(fspUser.getFspId()));
        }
        return super.getAllSubportfolios(criteria, pageable);
    }

    /**
     * {@code GET  /subportfolio/:id} : get the "id" subportfolio.
     *
     * @param id the id of the subportfolio to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the subportfolio, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/subportfolio/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SUBPORTFOLIO_VIEW + "\")")
    public ResponseEntity<SubportfolioDTO> getSubportfolio(@PathVariable Long id) {
        log.debug("FLEX-USER - REST request to get Subportfolio : {}", id);
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SubportfolioResourceException("Current logged-in user not found"));
        FspEntity fsp = fspService.findFspOfUser(fspUser.getId(), fspUser.getLogin()).orElseThrow(() -> new SubportfolioResourceException("Cannot find Fsp by user id: " + fspUser.getId()));
        if (Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED.equals(fsp.getRole())) {
            Optional<SubportfolioDTO> subportfolioDTO = subportfolioService.findByIdAndFspaId(id, fspUser.getFspId());
            return ResponseUtil.wrapOrNotFound(subportfolioDTO);
        }
        return super.getSubportfolio(id);
    }

    /**
     * {@code DELETE  /subportfolio/:id} : delete the "id" subportfolio.
     *
     * @param id the id of the subportfolio to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/subportfolio/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SUBPORTFOLIO_DELETE + "\")")
    public ResponseEntity<Void> deleteSubportfolio(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-USER - REST request to delete Subportfolio : {}", id);
        return super.deleteSubportfolio(id);
    }

    /**
     * {@code GET  /subportfolio/files/:fileId} : get file from subportfolio
     *
     * @param fileId the id of the file attached to subportfolio.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fileDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/subportfolio/files/{fileId}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SUBPORTFOLIO_VIEW + "\")")
    public ResponseEntity<FileDTO> getSubportfolioFile(@PathVariable Long fileId) {
        log.debug("FLEX-USER - REST request to get Subportfolio [id: {}]", fileId);
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SubportfolioResourceException("Current logged-in user not found"));
        FspEntity fsp = fspService.findFspOfUser(fspUser.getId(), fspUser.getLogin()).orElseThrow(() -> new SubportfolioResourceException("Cannot find Fsp by user id: " + fspUser.getId()));
        if (Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED.equals(fsp.getRole())) {
            Optional<FileDTO> fileDTO = Optional.empty();
            Optional<SubportfolioFileEntity> fileEntity = subportfolioService.getSubportfolioFileByFileIdAndFspaId(fileId, fspUser.getFspId());
            if (fileEntity.isPresent()) {
                fileDTO = Optional.ofNullable(ZipUtil.zipToFiles(fileEntity.get().getFileZipData()).get(0));
            }
            return ResponseUtil.wrapOrNotFound(fileDTO);
        }
        return super.getSubportfolioFile(fileId);
    }

    /**
     * {@code GET  /user/subportfolio/export/all} : export all subportfolio's to file by flex user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SUBPORTFOLIO_VIEW + "\")")
    @GetMapping("/subportfolio/export/all")
    public ResponseEntity<FileDTO> exportAllSubportfolios(SubportfolioCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-USER - REST request to export all subportfolios");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SubportfolioResourceException("Current logged-in user not found"));
        criteria.setFspaId((LongFilter) new LongFilter().setEquals(fspUser.getFspId()));
        return super.exportAllSubportfolios(criteria, pageable, Screen.USER_SUBPORTFOLIO);
    }

    /**
     * {@code GET  /user/subportfolio/export/displayed-data} : export displayed subportfolio's to file by flex user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SUBPORTFOLIO_VIEW + "\")")
    @GetMapping("/subportfolio/export/displayed-data")
    public ResponseEntity<FileDTO> exportDisplayedSubportfolios(SubportfolioCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-USER - REST request to export displayed subportfolios");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SubportfolioResourceException("Current logged-in user not found"));
        criteria.setFspaId((LongFilter) new LongFilter().setEquals(fspUser.getFspId()));
        return super.exportDisplayedSubportfolios(criteria, pageable, Screen.USER_SUBPORTFOLIO);
    }

    @GetMapping("/subportfolioNames")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SUBPORTFOLIO_VIEW + "\")")
    public ResponseEntity<List<String>> getAllSubportfolioNames() {
        log.debug("FLEX-ADMIN - REST request to get all Subportfolio names");
        return super.getAllSubportfolioNames();
    }


    /**
     * {@code GET  /subportfolio/minimal/get-fspa-certified-subs} : get all current logged in FSPA certified Subportfolios.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of subportfolio in body.
     */
    @GetMapping("/subportfolio/minimal/get-fspa-certified-subs")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SUBPORTFOLIO_VIEW + "\")")
    public ResponseEntity<List<SubportfolioMinDTO>> findAllFspaCertifiedSubportfoliosMin() {
        log.debug("FLEX-USER - REST request to get all current logged in FSPA certified Subportfolios");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SubportfolioResourceException("Current logged-in user not found"));
        List<SubportfolioMinDTO> result = subportfolioService.findAllFspaCertifiedSubportfoliosMin(fspUser.getFspId());
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }
}
