package pl.com.tt.flex.server.web.rest.potential;

import com.google.common.collect.Lists;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.potential.FlexPotentialFileEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.common.dto.FileDTOUtil;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.importData.flexPotential.FlexPotentialImportService;
import pl.com.tt.flex.server.service.mail.flexPotential.FlexPotentialMailService;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.potential.FlexPotentialQueryService;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialCriteria;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialFileDTO;
import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.flexPotential.FlexPotentialValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.FLEX_POTENTIAL_NOTHING_TO_EXPORT;

/**
 * Common REST controller for managing {@link pl.com.tt.flex.server.domain.potential.FlexPotentialEntity} for all web modules.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class FlexPotentialResource {

    public static final String ENTITY_NAME = "flexPotential";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    protected final FlexPotentialService flexPotentialService;
    protected final FlexPotentialQueryService flexPotentialQueryService;
    protected final FlexPotentialMapper flexPotentialMapper;
    protected final FlexPotentialValidator flexPotentialValidator;
    protected final FspService fspService;
    protected final UserService userService;
    protected final FlexPotentialImportService flexPotentialImportService;
    protected final NotifierFactory notifierFactory;
    protected final ProductService productService;
    protected final UnitService unitService;
    protected final FlexPotentialMailService flexPotentialMailService;

    public FlexPotentialResource(FlexPotentialService flexPotentialService, FlexPotentialQueryService flexPotentialQueryService, FlexPotentialMapper flexPotentialMapper,
                                 FlexPotentialValidator flexPotentialValidator, FspService fspService, UserService userService, FlexPotentialImportService flexPotentialImportService,
                                 NotifierFactory notifierFactory, ProductService productService, UnitService unitService, FlexPotentialMailService flexPotentialMailService) {

        this.flexPotentialService = flexPotentialService;
        this.flexPotentialQueryService = flexPotentialQueryService;
        this.flexPotentialMapper = flexPotentialMapper;
        this.flexPotentialValidator = flexPotentialValidator;
        this.fspService = fspService;
        this.userService = userService;
        this.flexPotentialImportService = flexPotentialImportService;
        this.notifierFactory = notifierFactory;
        this.productService = productService;
        this.unitService = unitService;
        this.flexPotentialMailService = flexPotentialMailService;
    }

    protected ResponseEntity<FlexPotentialDTO> createFlexPotential(FlexPotentialDTO flexPotentialDTO, MultipartFile[] files) throws ObjectValidationException, URISyntaxException {
        if (flexPotentialDTO.getId() != null) {
            throw new BadRequestAlertException("A new flexPotential cannot already have an ID", ENTITY_NAME, "idexists");
        }
        flexPotentialValidator.checkValid(flexPotentialDTO);
        flexPotentialDTO.getFiles().addAll(parseFilesForFlexPotential(files, flexPotentialDTO.getId()));
        FlexPotentialDTO result = flexPotentialService.save(flexPotentialDTO, flexPotentialDTO.getRemoveFiles());
        flexPotentialService.registerCreatedNotification(result);
        flexPotentialService.sendMailInformingAboutCreation(result);
        return ResponseEntity.created(new URI("/api/flex-potentials/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString())).body(result);
    }

    private List<FlexPotentialFileDTO> parseFilesForFlexPotential(MultipartFile[] files, Long flexPotentialId) throws ObjectValidationException {
        if (nonNull(files)) {
            flexPotentialValidator.checkFileExtensionValid(Arrays.stream(files).collect(Collectors.toList()));
            return Arrays.stream(files).map(file -> new FlexPotentialFileDTO(FileDTOUtil.parseMultipartFile(file), flexPotentialId)).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    protected ResponseEntity<FlexPotentialDTO> updateFlexPotential(FlexPotentialDTO flexPotentialDTO, MultipartFile[] files) throws ObjectValidationException {
        if (flexPotentialDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        flexPotentialValidator.checkModifiable(flexPotentialDTO);
        flexPotentialDTO.getFiles().addAll(parseFilesForFlexPotential(files, flexPotentialDTO.getId()));
        FlexPotentialDTO oldFlexPotentialDTO = flexPotentialService.findById(flexPotentialDTO.getId()).get();
        FlexPotentialDTO result = flexPotentialService.save(flexPotentialDTO, flexPotentialDTO.getRemoveFiles());
        flexPotentialService.registerUpdatedNotification(oldFlexPotentialDTO, result);
        flexPotentialService.sendMailInformingAboutModification(oldFlexPotentialDTO, result);
        if (!oldFlexPotentialDTO.isRegistered() && result.isRegistered()) {
            flexPotentialService.sendNotificationInformingAboutRegistered(result);
            flexPotentialService.sendMailInformingAboutRegistered(result);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, flexPotentialDTO.getId().toString())).body(result);
    }

    protected ResponseEntity<List<FlexPotentialDTO>> getAllFlexPotentialsByCriteria(FlexPotentialCriteria criteria, Pageable pageable) {
        Page<FlexPotentialDTO> page = flexPotentialQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    // Pobrane listy obiektow za pomoca metod findByCriteria(Criteria criteria, Sort sort) oraz findByCriteria(Criteria criteria, Pageable pageable)
    // roznia sie w kolejnosci sortowania obiektow. Z tego wzgledu do pobierania listy obiektow do eksportu uzyto metody findByCriteria(Criteria criteria, Pageable pageable)
    // tej samej co do pobrania listy obiektow na front (do widoku).
    public ResponseEntity<FileDTO> exportFlexPotential(FlexPotentialCriteria criteria, Pageable pageable, Screen screen, boolean isOnlyDisplayedData) throws IOException {
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        int size = (int) flexPotentialQueryService.countByCriteria(criteria);
        if (size == 0) {
            throw new ObjectValidationException("Nothing to export", FLEX_POTENTIAL_NOTHING_TO_EXPORT);
        }
        PageRequest pageRequest = PageRequest.of(0, size, pageable.getSort());
        Page<FlexPotentialDTO> flexPotentialsPage = flexPotentialQueryService.findByCriteria(criteria, pageRequest);
        return ResponseEntity.ok().body(flexPotentialService.exportFlexPotentialToFile(flexPotentialsPage.getContent(), langKey, isOnlyDisplayedData, screen));
    }


    // Z importu do pliku xlsx na chwilę obecną zrezygnowano zatem ten endpoint jest nieużywany!

//    /**
//     * {@code POST  /flex-potentials/import/xlsx} : import flex potential to file.
//     *
//     * @param file - file to import.
//     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
//     */
//    @PostMapping("/flex-potentials/import/xlsx")
//    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FP_VIEW + "\") or hasAuthority(\"" + FLEX_USER_FP_VIEW + "\")")
//    public ResponseEntity<Void> importFlexPotential(@RequestPart("file") MultipartFile file) throws IOException, ImportDataException {
//        log.debug("REST request to import flex potential file: {}", file.getName());
//        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
//        if (!extension.equalsIgnoreCase(DataImportFormat.XLSX.name())) {
//            throw new ImportDataException(CANNOT_IMPORT_FP_BECAUSE_WRONG_FILE_EXTENSION);
//        }
//        importDataService.importFlexPotential(file, userService.getLangKeyForCurrentLoggedUser());
//        return ResponseEntity.noContent().build();
//    }

    protected ResponseEntity<FlexPotentialDTO> getFlexPotential(Long id) {
        Optional<FlexPotentialDTO> maybyFlexPotentialDTO = flexPotentialService.findById(id);
        return ResponseUtil.wrapOrNotFound(maybyFlexPotentialDTO);
    }

    protected ResponseEntity<Void> deleteFlexPotential(Long id) throws ObjectValidationException {
        flexPotentialValidator.checkDeletable(id);
        FlexPotentialDTO flexPotentialDTO = flexPotentialService.findById(id).get();
        flexPotentialService.delete(id);
        flexPotentialService.sendNotificationAboutDeleted(flexPotentialDTO);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    protected ResponseEntity<FileDTO> getFlexPotentialFile(Long fileId) throws IOException {
        Optional<FileDTO> fileDTO = Optional.empty();
        Optional<FlexPotentialFileEntity> fileEntity = flexPotentialService.getFlexPotentialFileByFileId(fileId);
        if (fileEntity.isPresent()) {
            fileDTO = Optional.ofNullable(ZipUtil.zipToFiles(fileEntity.get().getFileZipData()).get(0));
        }
        return ResponseUtil.wrapOrNotFound(fileDTO);
    }

    protected static class FlexPotentialResourceException extends RuntimeException {
        public FlexPotentialResourceException(String message) {
            super(message);
        }
    }
}
