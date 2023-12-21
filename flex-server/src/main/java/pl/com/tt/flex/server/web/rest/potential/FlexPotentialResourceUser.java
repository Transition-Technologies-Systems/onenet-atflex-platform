package pl.com.tt.flex.server.web.rest.potential;

import io.github.jhipster.service.filter.LongFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.potential.FlexPotentialMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
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
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.validator.flexPotential.FlexPotentialValidator;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.config.Constants.FLEX_ADMIN_APP_NAME;
import static pl.com.tt.flex.server.config.Constants.FLEX_USER_APP_NAME;

/**
 * REST controller for managing {@link pl.com.tt.flex.server.domain.potential.FlexPotentialEntity} for FLEX-USER web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class FlexPotentialResourceUser extends FlexPotentialResource {

    public FlexPotentialResourceUser(FlexPotentialService flexPotentialService, FlexPotentialQueryService flexPotentialQueryService, FlexPotentialMapper flexPotentialMapper,
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
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FP_MANAGE + "\")")
    public ResponseEntity<FlexPotentialDTO> createFlexPotential(@RequestPart @Valid FlexPotentialDTO flexPotentialDTO,
        @RequestPart(value = "files", required = false) MultipartFile[] files) throws URISyntaxException, ObjectValidationException {
        log.debug("{} - REST request to save FlexPotential", FLEX_USER_APP_NAME);
        flexPotentialDTO.setFsp(new FspDTO(findFspOfCurrentUser().getId()));
        return super.createFlexPotential(flexPotentialDTO, files);
    }

    /**
     * {@code Post  /flex-potentials/update} : Updates an existing flexPotential.
     *
     * @param flexPotentialDTO the flexPotentialDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated flexPotentialDTO,
     * or with status {@code 400 (Bad Request)} if the flexPotentialDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the flexPotentialDTO couldn't be updated,
     * or with status {@code 403(Forbidden)} if current logged in FSP has no access.
     */
    @PostMapping("/flex-potentials/update")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FP_MANAGE + "\")")
    public ResponseEntity<FlexPotentialDTO> updateFlexPotential(@RequestPart @Valid FlexPotentialDTO flexPotentialDTO,
        @RequestPart(value = "files", required = false) MultipartFile[] files) throws ObjectValidationException {
        log.debug("{} - REST request to update FlexPotential : {}", FLEX_USER_APP_NAME, flexPotentialDTO);
        checkIfFsphasRightsToFlexPotential(flexPotentialDTO.getId());
        flexPotentialDTO.setFsp(new FspDTO(findFspOfCurrentUser().getId()));
        return super.updateFlexPotential(flexPotentialDTO, files);
    }

    /**
     * {@code GET  /flex-potentials} : get all the flexPotentials belonging to current logged in user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of flexPotentials in body.
     */
    @GetMapping("/flex-potentials")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FP_VIEW + "\")")
    public ResponseEntity<List<FlexPotentialDTO>> getAllFlexPotentialsByCriteria(FlexPotentialCriteria criteria, Pageable pageable) {
        UserDTO userDTO = userService.getCurrentUserDTO().orElseThrow(() -> new FlexPotentialResourceException("Current logged-in user not found"));
        if (userDTO.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            return ResponseEntity.status(403).build();
        }
        log.debug("{} - REST request to get FlexPotentials by criteria: {}", FLEX_USER_APP_NAME, criteria);
        criteria.setFspId((LongFilter) new LongFilter().setEquals(findFspOfCurrentUser().getId()));
        return super.getAllFlexPotentialsByCriteria(criteria, pageable);
    }

    /**
     * {@code GET  /flex-potentials/get-der-names} : get all the flexPotentials DERs name.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of DERs name joined to Flexibility Potentials in body.
     */
    @GetMapping("/flex-potentials/get-der-names")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FP_VIEW + "\")")
    public ResponseEntity<List<String>> getAllDerNameJoinedToFP() {
        log.debug("{} - REST request to get all DER name joined to FP", FLEX_USER_APP_NAME);
        return ResponseEntity.ok().body(flexPotentialService.getAllDerNameJoinedToFPByFspId(findFspOfCurrentUser().getId()));
    }

    /**
     * {@code GET  /flex-potentials/register/get-der-names} : get all the flexPotentials (FLex Register) DERs name.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of DERs name joined to Flexibility Potentials (Flex Register) in body.
     */
    @GetMapping("/flex-potentials/register/get-der-names")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FP_VIEW + "\")")
    public ResponseEntity<List<String>> getAllDerNameJoinedToFlexRegister() {
        log.debug("{} - REST request to get all DER name joined to Flex Register", FLEX_USER_APP_NAME);
        return ResponseEntity.ok().body(flexPotentialService.getAllDerNameJoinedToFlexRegisterByFspId(findFspOfCurrentUser().getId()));
    }

    /**
     * {@code GET  /flex-potentials/export/all} : export all flex potential to file.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @GetMapping("/flex-potentials/export/all")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FP_VIEW + "\")")
    public ResponseEntity<FileDTO> exportFlexPotentialAll(FlexPotentialCriteria criteria, Pageable pageable) throws IOException {
        log.debug("{} - REST request to export flex all potential", FLEX_USER_APP_NAME);
        criteria.setFspId((LongFilter) new LongFilter().setEquals(findFspOfCurrentUser().getId()));
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
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FP_VIEW + "\")")
    public ResponseEntity<FileDTO> exportFlexPotentialDisplayed(FlexPotentialCriteria criteria, Pageable pageable) throws IOException {
        log.debug("{} - REST request to export displayed flex potential", FLEX_USER_APP_NAME);
        criteria.setFspId((LongFilter) new LongFilter().setEquals(findFspOfCurrentUser().getId()));
        Screen screen = getScreenByCriteria(criteria);
        return super.exportFlexPotential(criteria, pageable, screen, true);
    }

    private Screen getScreenByCriteria(FlexPotentialCriteria criteria) {
        Screen screen;
        if (Objects.nonNull(criteria.getIsRegister()) && Boolean.TRUE.equals(criteria.getIsRegister().getEquals())) {
            screen = Screen.USER_REGISTER_FLEXIBILITY_POTENTIALS;
        } else {
            screen = Screen.USER_FLEXIBILITY_POTENTIALS;
        }
        return screen;
    }

    @GetMapping("/flex-potentials/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FP_VIEW + "\")")
    public ResponseEntity<FlexPotentialDTO> getFlexPotential(@PathVariable Long id) {
        log.debug("{} - REST request to get FlexPotential : {}", FLEX_USER_APP_NAME, id);
        checkIfFsphasRightsToFlexPotential(id);
        return super.getFlexPotential(id);
    }

    @DeleteMapping("/flex-potentials/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FP_DELETE + "\")")
    public ResponseEntity<Void> deleteFlexPotential(@PathVariable Long id) throws ObjectValidationException {
        log.debug("{} - REST request to delete FlexPotential : {}", FLEX_USER_APP_NAME, id);
        checkIfFsphasRightsToFlexPotential(id);
        return super.deleteFlexPotential(id);
    }

    /**
     * {@code GET  /user/flex-potentials/files/:fileId} : get file from flexPotential by flex-user
     *
     * @param fileId the id of the file attached to flexPotential.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fileDTO, or with status {@code 404 (Not Found)}.
     * @throws IOException {@code 500 (Internal Server Error)} if file could not be returned
     */
    @GetMapping("/flex-potentials/files/{fileId}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FP_VIEW + "\")")
    public ResponseEntity<FileDTO> getFlexPotentialFile(@PathVariable Long fileId) throws IOException {
        log.debug("{} - REST request to get FlexPotential file [id: {}]", FLEX_USER_APP_NAME, fileId);
        return super.getFlexPotentialFile(fileId);
    }

    /**
     * {@code GET  /flex-potentials/minimal/get-all-registered-for-fsp-and-product} : get FSP/A registered flex potentials for product.
     *
     * @param productId the Product id of Flex potential to find
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body with the list of flexPotentialMinDTOs}.
     */
    @GetMapping("/flex-potentials/minimal/get-all-registered-for-fsp-and-product")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FP_VIEW + "\")")
    public ResponseEntity<List<FlexPotentialMinDTO>> getAllRegisteredFlexPotentialsForFspAndProduct(@RequestParam(value = "productId") Long productId) {
        FspEntity fsp = findFspOfCurrentUser();
        log.debug("{} - REST request to get all registered FlexPotentials for FSP: {} and Product: {}", FLEX_ADMIN_APP_NAME, fsp.getId(), productId);
        List<FlexPotentialMinDTO> result = flexPotentialService.findAllRegisteredFlexPotentialsForFspAndProduct(fsp.getId(), productId);
        if (result.isEmpty()) {
            ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(result);
    }

    private void checkIfFsphasRightsToFlexPotential(Long fpId) {
        FspEntity currentFsp = findFspOfCurrentUser();
        if (!flexPotentialService.findById(fpId).get().getFsp().getId().equals(currentFsp.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }

    private FspEntity findFspOfCurrentUser() {
        UserEntity maybefspUser = userService.getCurrentUser();
        return fspService.findFspOfUser(maybefspUser.getId(), maybefspUser.getLogin())
            .orElseThrow(() -> new FlexPotentialResourceException("Cannot find Fsp by user id: " + maybefspUser.getId()));
    }
}
