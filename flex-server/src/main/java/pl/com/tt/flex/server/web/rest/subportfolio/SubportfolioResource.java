package pl.com.tt.flex.server.web.rest.subportfolio;

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
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioFileEntity;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.common.dto.FileDTOUtil;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioQueryService;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioService;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioCriteria;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioFileDTO;
import pl.com.tt.flex.server.service.subportfolio.mapper.SubportfolioMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.subportfolio.SubportfolioValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SUBPORTFOLIO_NOTHING_TO_EXPORT;

/**
 * Common REST controller for managing {@link SubportfolioEntity} for all web modules.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class SubportfolioResource {

    public static final String ENTITY_NAME = "subportfolio";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    protected final SubportfolioService subportfolioService;
    protected final SubportfolioQueryService subportfolioQueryService;
    protected final SubportfolioMapper subportfolioMapper;
    protected final SubportfolioValidator subportfolioValidator;
    protected final UserService userService;

    public SubportfolioResource(SubportfolioService subportfolioService, SubportfolioQueryService subportfolioQueryService, SubportfolioMapper subportfolioMapper,
                                SubportfolioValidator subportfolioValidator, UserService userService) {
        this.subportfolioService = subportfolioService;
        this.subportfolioQueryService = subportfolioQueryService;
        this.subportfolioMapper = subportfolioMapper;
        this.subportfolioValidator = subportfolioValidator;
        this.userService = userService;
    }

    public ResponseEntity<SubportfolioDTO> createSubportfolio(SubportfolioDTO subportfolioDTO, MultipartFile[] files) throws URISyntaxException, ObjectValidationException {
        if (subportfolioDTO.getId() != null) {
            throw new BadRequestAlertException("A new Subportfolio cannot already have an ID", ENTITY_NAME, "idexists");
        }
        subportfolioDTO.getFiles().addAll(parseFilesForSubportfolio(files));
        subportfolioValidator.checkValid(subportfolioDTO);
        SubportfolioDTO result = subportfolioService.save(subportfolioDTO, subportfolioDTO.getRemoveFiles());
        subportfolioService.registerNewNotificationForSubportfolioCreation(result);
        subportfolioService.sendMailInformingAboutCreation(result);
        return ResponseEntity.created(new URI("/api/subportfolio/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString())).body(result);
    }

    private List<SubportfolioFileDTO> parseFilesForSubportfolio(MultipartFile[] files) throws ObjectValidationException {
        if (nonNull(files)) {
            subportfolioValidator.checkFileExtensionValid(Arrays.stream(files).collect(Collectors.toList()));
            return Arrays.stream(files).map(file -> new SubportfolioFileDTO(FileDTOUtil.parseMultipartFile(file))).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    public ResponseEntity<SubportfolioDTO> updateSubportfolio(SubportfolioDTO subportfolioDTO, MultipartFile[] files) throws URISyntaxException, ObjectValidationException {
        if (subportfolioDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        SubportfolioDTO oldSubportfolio = subportfolioService.findById(subportfolioDTO.getId()).get();
        subportfolioDTO.getFiles().addAll(parseFilesForSubportfolio(files));
        subportfolioValidator.checkModifiable(subportfolioDTO);
        SubportfolioDTO result = subportfolioService.save(subportfolioDTO, subportfolioDTO.getRemoveFiles());
        subportfolioService.registerNewNotificationForSubportfolioEdition(result, oldSubportfolio);
        subportfolioService.sendMailInformingAboutModification(result, oldSubportfolio);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, subportfolioDTO.getId().toString()))
            .body(result);
    }

    public ResponseEntity<List<SubportfolioDTO>> getAllSubportfolios(SubportfolioCriteria criteria, Pageable pageable) {
        Page<SubportfolioDTO> page = subportfolioQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    public ResponseEntity<SubportfolioDTO> getSubportfolio(Long id) {
        Optional<SubportfolioDTO> subportfolioDTO = subportfolioService.findById(id);
        return ResponseUtil.wrapOrNotFound(subportfolioDTO);
    }

    public ResponseEntity<Void> deleteSubportfolio(Long id) throws ObjectValidationException {
        log.debug("REST request to delete Subportfolio : {}", id);
        subportfolioValidator.checkDeletable(id);
        subportfolioService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    public ResponseEntity<FileDTO> getSubportfolioFile(Long fileId) {
        Optional<FileDTO> fileDTO = Optional.empty();
        Optional<SubportfolioFileEntity> fileEntity = subportfolioService.getSubportfolioFileByFileId(fileId);
        if (fileEntity.isPresent()) {
            fileDTO = Optional.ofNullable(ZipUtil.zipToFiles(fileEntity.get().getFileZipData()).get(0));
        }
        return ResponseUtil.wrapOrNotFound(fileDTO);
    }

    public ResponseEntity<FileDTO> exportAllSubportfolios(SubportfolioCriteria criteria, Pageable pageable, Screen screen) throws IOException {
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        int size = (int) subportfolioQueryService.countByCriteria(criteria);
        PageRequest pageRequest = PageRequest.of(0, size, pageable.getSort());
        Page<SubportfolioDTO> subportfoliosPage = subportfolioQueryService.findByCriteria(criteria, pageRequest);
        return ResponseEntity.ok().body(subportfolioService.exportSubportfoliosToFile(subportfoliosPage.getContent(), langKey, false, screen));
    }

    public ResponseEntity<FileDTO> exportDisplayedSubportfolios(SubportfolioCriteria criteria, Pageable pageable, Screen screen) throws IOException {
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        int size = (int) subportfolioQueryService.countByCriteria(criteria);
        if (size == 0) {
            throw new ObjectValidationException("Nothing to export", SUBPORTFOLIO_NOTHING_TO_EXPORT);
        }
        PageRequest pageRequest = PageRequest.of(0, size, pageable.getSort());
        Page<SubportfolioDTO> subportfoliosPage = subportfolioQueryService.findByCriteria(criteria, pageRequest);
        return ResponseEntity.ok().body(subportfolioService.exportSubportfoliosToFile(subportfoliosPage.getContent(), langKey, true, screen));
    }

    public ResponseEntity<List<String>> getAllSubportfolioNames() {
        List<String> subportfolioNames = subportfolioService.findAllSubportfolioNames();
        return ResponseEntity.ok().body(subportfolioNames);
    }


    protected static class SubportfolioResourceException extends RuntimeException {
        protected SubportfolioResourceException(String message) {
            super(message);
        }
    }
}
