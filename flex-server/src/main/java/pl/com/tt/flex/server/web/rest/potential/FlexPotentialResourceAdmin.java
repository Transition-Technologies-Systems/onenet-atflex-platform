package pl.com.tt.flex.server.web.rest.potential;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.importData.flexPotential.FlexPotentialImportService;
import pl.com.tt.flex.server.service.mail.flexPotential.FlexPotentialMailService;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.potential.FlexPotentialQueryService;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialCriteria;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.flexPotential.FlexPotentialValidator;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.config.Constants.FLEX_ADMIN_APP_NAME;

/**
 * REST controller for managing {@link pl.com.tt.flex.server.domain.potential.FlexPotentialEntity} for FLEX-ADMIN web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class FlexPotentialResourceAdmin extends FlexPotentialResource {

    public FlexPotentialResourceAdmin(FlexPotentialService flexPotentialService, FlexPotentialQueryService flexPotentialQueryService, FlexPotentialMapper flexPotentialMapper,
                                      FlexPotentialValidator flexPotentialValidator, FspService fspService, UserService userService, FlexPotentialImportService flexPotentialImportService, NotifierFactory notifierFactory,
                                      ProductService productService, UnitService unitService, FlexPotentialMailService flexPotentialMailService) {
        super(flexPotentialService, flexPotentialQueryService, flexPotentialMapper, flexPotentialValidator, fspService, userService, flexPotentialImportService, notifierFactory, productService,
            unitService, flexPotentialMailService);
    }

    /**
     * {@code POST  /flex-potentials} : Create a new flexPotential.
     *
     * @param flexPotentialDTO the flexPotentialDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new flexPotentialDTO, or with status {@code 400 (Bad Request)} if the flexPotential has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/flex-potentials")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_MANAGE + "\")")
    public ResponseEntity<FlexPotentialDTO> createFlexPotential(@RequestPart @Valid FlexPotentialDTO flexPotentialDTO,
        @RequestPart(value = "files", required = false) MultipartFile[] files) throws URISyntaxException, ObjectValidationException {
        log.debug("{} - REST request to save FlexPotential", FLEX_ADMIN_APP_NAME);
        if (flexPotentialDTO.getFsp().getId() == null) {
            return ResponseEntity.status(400).build();
        }
        return super.createFlexPotential(flexPotentialDTO, files);
    }

    /**
     * {@code Post  /flex-potentials/update} : Updates an existing flexPotential.
     *
     * @param flexPotentialDTO the flexPotentialDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated flexPotentialDTO,
     * or with status {@code 400 (Bad Request)} if the flexPotentialDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the flexPotentialDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/flex-potentials/update")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_MANAGE + "\")")
    public ResponseEntity<FlexPotentialDTO> updateFlexPotential(@RequestPart @Valid FlexPotentialDTO flexPotentialDTO,
        @RequestPart(value = "files", required = false) MultipartFile[] files) throws ObjectValidationException {
        log.debug("{} - REST request to update FlexPotential : {}", FLEX_ADMIN_APP_NAME, flexPotentialDTO);
        return super.updateFlexPotential(flexPotentialDTO, files);
    }

    /**
     * {@code GET /flex-potentials} : get all the flexPotentials.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of flexPotentials in body.
     */
    @GetMapping("/flex-potentials")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_VIEW + "\")")
    public ResponseEntity<List<FlexPotentialDTO>> getAllFlexPotentialsByCriteria(FlexPotentialCriteria criteria, Pageable pageable) {
        log.debug("{} - REST request to get FlexPotentials by criteria: {}", FLEX_ADMIN_APP_NAME, criteria);
        return super.getAllFlexPotentialsByCriteria(criteria, pageable);
    }

    /**
     * {@code GET  /flex-potentials/get-der-names} : get all the flexPotentials DERs name.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of DERs name joined to Flexibility Potentials in body.
     */
    @GetMapping("/flex-potentials/get-der-names")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_VIEW + "\")")
    public ResponseEntity<List<String>> getDerNameJoinedToFP() {
        log.debug("{} - REST request to get all DER name joined to FP", FLEX_ADMIN_APP_NAME);
        return ResponseEntity.ok().body(flexPotentialService.getAllDerNameJoinedToFP());
    }

    /**
     * {@code GET  /flex-potentials/register/get-der-names} : get all the flexPotentials (FLex Register) DERs name.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of DERs name joined to Flexibility Potentials (Flex Register) in body.
     */
    @GetMapping("/flex-potentials/register/get-der-names")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_VIEW + "\")")
    public ResponseEntity<List<String>> getDerNameJoinedToFlexRegister() {
        log.debug("{} - REST request to get all DER name joined to Flex Register", FLEX_ADMIN_APP_NAME);
        return ResponseEntity.ok().body(flexPotentialService.getAllDerNameJoinedToFlexRegister());
    }

    // Pobrane listy obiektow za pomoca metod findByCriteria(Criteria criteria, Sort sort) oraz findByCriteria(Criteria criteria, Pageable pageable)
    // roznia sie w kolejnosci sortowania obiektow. Z tego wzgledu do pobierania listy obiektow do eksportu uzyto metody findByCriteria(Criteria criteria, Pageable pageable)
    // tej samej co do pobrania listy obiektow na front (do widoku).

    /**
     * {@code GET  /flex-potentials/export/all} : export all flex potential to file.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @GetMapping("/flex-potentials/export/all")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_VIEW + "\")")
    public ResponseEntity<FileDTO> exportFlexPotentialAll(FlexPotentialCriteria criteria, Pageable pageable) throws IOException {
        log.debug("{} - REST request to export all flex potential", FLEX_ADMIN_APP_NAME);
        Screen screen = getScreenByCriteria(criteria);
        return super.exportFlexPotential(criteria, pageable, screen, false);
    }

    /**
     * {@code GET  /flex-potentials/export/displayed-data} : export displayed flex potential to file.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @GetMapping("/flex-potentials/export/displayed-data")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_VIEW + "\")")
    public ResponseEntity<FileDTO> exportFlexPotentialDisplayed(FlexPotentialCriteria criteria, Pageable pageable) throws IOException {
        log.debug("{} - REST request to export displayed flex potential by Admin", FLEX_ADMIN_APP_NAME);
        Screen screen = getScreenByCriteria(criteria);
        return super.exportFlexPotential(criteria, pageable, screen, true);
    }

    private Screen getScreenByCriteria(FlexPotentialCriteria criteria) {
        Screen screen;
        if (Objects.nonNull(criteria.getIsRegister()) && Boolean.TRUE.equals(criteria.getIsRegister().getEquals())) {
            screen = Screen.ADMIN_FLEX_REGISTER;
        } else {
            screen = Screen.ADMIN_FLEXIBILITY_POTENTIALS;
        }
        return screen;
    }

    /**
     * {@code GET  /flex-potentials/:id} : get the "id" flexPotential.
     *
     * @param id the id of the flexPotentialDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the flexPotentialDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/flex-potentials/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_VIEW + "\")")
    public ResponseEntity<FlexPotentialDTO> getFlexPotential(@PathVariable Long id) {
        log.debug("{} - REST request to get FlexPotential : {}", FLEX_ADMIN_APP_NAME, id);
        return super.getFlexPotential(id);
    }

    /**
     * {@code DELETE  /flex-potentials/:id} : delete the "id" flexPotential.
     *
     * @param id the id of the flexPotentialDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/flex-potentials/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_DELETE + "\")")
    public ResponseEntity<Void> deleteFlexPotential(@PathVariable Long id) throws ObjectValidationException {
        log.debug("{} - REST request to delete FlexPotential : {}", FLEX_ADMIN_APP_NAME, id);
        return super.deleteFlexPotential(id);
    }

    /**
     * {@code GET  /flex-potentials/files/:fileId} : get file from flexPotential
     *
     * @param fileId the id of the file attached to flexPotential.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fileDTO, or with status {@code 404 (Not Found)}.
     * @throws IOException {@code 500 (Internal Server Error)} if file could not be returned
     */
    @GetMapping("/flex-potentials/files/{fileId}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_VIEW + "\")")
    public ResponseEntity<FileDTO> getFlexPotentialFile(@PathVariable Long fileId) throws IOException {
        log.debug("{} - REST request to get FlexPotential [id: {}]", FLEX_ADMIN_APP_NAME, fileId);
        return super.getFlexPotentialFile(fileId);
    }

    /**
     * {@code GET  /flex-potentials/minimal/get-all-registered-for-fsp-and-product} : get FSP/A registered flex potentials for product.
     *
     * @param fspId     the id of Fsp the owner of Flex potentials
     * @param productId the Product id of Flex potential to find
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body with the list of flexPotentialMinDTOs}.
     */
    @GetMapping("/flex-potentials/minimal/get-all-registered-for-fsp-and-product")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_VIEW + "\")")
    public ResponseEntity<List<FlexPotentialMinDTO>> getAllRegisteredFlexPotentialsForFspAndProduct(@RequestParam("fspId") Long fspId,
        @RequestParam(value = "productId") Long productId) {
        log.debug("{} - REST request to get all registered FlexPotentials for FSP: {} and Product: {}", FLEX_ADMIN_APP_NAME, fspId, productId);
        List<FlexPotentialMinDTO> result = flexPotentialService.findAllRegisteredFlexPotentialsForFspAndProduct(fspId, productId);
        if (result.isEmpty()) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(result);
    }
}
