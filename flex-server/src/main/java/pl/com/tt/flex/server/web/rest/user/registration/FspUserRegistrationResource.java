package pl.com.tt.flex.server.web.rest.user.registration;

import com.google.common.collect.Sets;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationFileEntity;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.common.dto.FileDTOUtil;
import pl.com.tt.flex.server.service.mail.fspUserRegistration.FspUserRegistrationMailService;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.registration.FspUserRegistrationQueryService;
import pl.com.tt.flex.server.service.user.registration.FspUserRegistrationService;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationCommentDTO;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationDTO;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationFileDTO;
import pl.com.tt.flex.server.service.user.registration.mapper.FspUserRegistrationFileMapper;
import pl.com.tt.flex.server.service.user.registration.mapper.FspUserRegistrationMapper;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.fspUserRegistration.FspUserRegistrationValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;
import pl.com.tt.flex.server.web.rest.errors.file.FileParseException;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus.*;
import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * Common REST controller for managing {@link FspUserRegistrationEntity} for all web modules.
 * Controller handles the registration of new FSP user (Flexibility Service Provider)
 * for OneNet Flexibility Platform FSP {@link pl.com.tt.flex.model.security.permission.Role}.
 */
@Slf4j
@RestController
@RequestMapping("/api/fsp-user-registration")
public class FspUserRegistrationResource {

    protected static class FspUserRegistrationResourceException extends RuntimeException {
        protected FspUserRegistrationResourceException(String message) {
            super(message);
        }
    }

    public static final String ENTITY_NAME = "fspUserRegistration";
    public static final String COMMENT_ENTITY_NAME = "fspUserRegistrationComment";

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    protected final FspUserRegistrationService fspUserRegistrationService;

    protected final FspUserRegistrationQueryService fspUserRegistrationQueryService;

    protected final FspUserRegistrationMapper fspUserRegistrationMapper;

    protected final FspUserRegistrationFileMapper fspUserRegistrationFileMapper;

    protected final FspUserRegistrationValidator fspUserRegistrationValidator;

    protected final FspUserRegistrationMailService mailService;

    protected final UserService userService;

    protected final NotifierFactory notifierFactory;

    public FspUserRegistrationResource(FspUserRegistrationService fspUserRegistrationService, FspUserRegistrationQueryService fspUserRegistrationQueryService,
        FspUserRegistrationMapper fspUserRegistrationMapper, FspUserRegistrationFileMapper fspUserRegistrationFileMapper, FspUserRegistrationValidator fspUserRegistrationValidator,
        FspUserRegistrationMailService mailService, UserService userService, NotifierFactory notifierFactory) {

        this.fspUserRegistrationService = fspUserRegistrationService;
        this.fspUserRegistrationQueryService = fspUserRegistrationQueryService;
        this.fspUserRegistrationMapper = fspUserRegistrationMapper;
        this.fspUserRegistrationFileMapper = fspUserRegistrationFileMapper;
        this.fspUserRegistrationValidator = fspUserRegistrationValidator;
        this.mailService = mailService;
        this.userService = userService;
        this.notifierFactory = notifierFactory;
    }

    //************************************************************************************FILES************************************************************************************

    /**
     * {@code GET  /file/:fspUserRegFileId} : get fspUserRegistrationFile by file id
     *
     * @param fspUserRegFileId the id of the file attached to fspUserRegistration.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the fileDTO, or with status {@code 404 (Not Found)}.
     * @throws IOException {@code 500 (Internal Server Error)} if file could not be returned
     */
    @GetMapping("/file/{fspUserRegFileId}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_REGISTRATION_VIEW + "\") or hasAuthority(\"" + FLEX_USER_FSP_REGISTRATION_VIEW + "\")")
    public ResponseEntity<FileDTO> getFspUserRegistrationFile(@PathVariable Long fspUserRegFileId) {
        log.debug("REST request to get fspUserRegistrationFile [id: {}]", fspUserRegFileId);
        Optional<FileDTO> fileDTO = Optional.empty();
        Optional<FspUserRegistrationFileEntity> fileEntity = fspUserRegistrationService.getFspUserRegFileByFileId(fspUserRegFileId);
        if (fileEntity.isPresent()) {
            fileDTO = Optional.of(ZipUtil.zipToFiles(fileEntity.get().getFileZipData()).get(0));
        }
        return ResponseUtil.wrapOrNotFound(fileDTO);
    }

    /**
     * {@code GET  /:id/file} : get all fspUserRegistrationFiles of fspUserRegistration in zip archive
     *
     * @param fspUserRegId the id of fspUserRegistration
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and zip archive with all attached files to fspUserRegistration, or with status {@code 404 (Not Found)}.
     * @throws IOException {@code 500 (Internal Server Error)} if files could not be returned
     */
    @GetMapping("/{id}/file")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_REGISTRATION_VIEW + "\") or hasAuthority(\"" + FLEX_USER_FSP_REGISTRATION_VIEW + "\")")
    public ResponseEntity<FileDTO> getAllFspUserRegistrationFiles(@PathVariable("id") Long fspUserRegId) {
        log.debug("REST request to get all fspUserRegistrationFiles of fspUserRegistration [id: {}]", fspUserRegId);
        fspUserRegistrationService.findOne(fspUserRegId).orElseThrow(() -> new FspUserRegistrationResourceException("No fspUserRegistrationEntity was found for this id: " + fspUserRegId));
        List<FileDTO> fileDTOS = fspUserRegistrationService.getZipWithAllFilesOfFspUserRegistration(fspUserRegId);
        if (fileDTOS.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        byte[] filesZipData = ZipUtil.filesToZip(fileDTOS);
        String fileName = "fsp-registration" + fspUserRegId + "-files.zip";
        return ResponseEntity.ok().body(new FileDTO(fileName, filesZipData));
    }

    /**
     * {@code POST  /file/:fspUserRegId} : Add file to fspUserRegistrationComment
     *
     * @param fspUserRegFileDTO dto with id of fspUserRegistrationComment and fspUserRegistration
     * @param multipartFile         file to add to fspUserRegistration
     * @throws FileParseException {@code 400 (Bad Request)} if problem occurred while parsing attached files
     * @throws RuntimeException   {@code 500 (Internal Server Error)} if fspUserRegistrationComment was not found
     */

    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_REGISTRATION_MANAGE + "\") or hasAuthority(\"" + FLEX_USER_FSP_REGISTRATION_MANAGE + "\")")
    @PostMapping(value = "/file")
    public void addFileToFspUserRegistration(@NotNull @Valid @RequestPart FspUserRegistrationFileDTO fspUserRegFileDTO, @NotNull @RequestPart(value = "file") MultipartFile multipartFile)
        throws ObjectValidationException {
        log.debug("REST request to add file {} to fspUserRegistration [fspUserRegistrationId: {}, fspUserRegistrationCommentId: {}]", multipartFile.getOriginalFilename(),
                fspUserRegFileDTO.getFspUserRegistrationId(), fspUserRegFileDTO.getFspUserRegistrationCommentId());
        FspUserRegistrationDTO fspUserRegDTO = fspUserRegistrationService.findOne(fspUserRegFileDTO.getFspUserRegistrationId()).orElseThrow(() ->
            new FspUserRegistrationResourceException("No fspUserRegistrationEntity was found for this id: " + fspUserRegFileDTO.getFspUserRegistrationId()));
        fspUserRegistrationValidator.validActualStatusOfRegistration(fspUserRegDTO.getStatus(), Sets.newHashSet(NEW, CONFIRMED_BY_FSP, PRE_CONFIRMED_BY_MO,
            USER_ACCOUNT_ACTIVATED_BY_FSP));
        fspUserRegistrationValidator.checkFileExtensionValid(multipartFile);
        fspUserRegFileDTO.setFileDTO(FileDTOUtil.parseMultipartFile(multipartFile));
        fspUserRegistrationService.addFileToFspUserRegistration(fspUserRegFileDTO);
        informMoAndFspAboutChangeInRegistration(fspUserRegDTO);
    }
    //*****************************************************************************************************************************************************************************


    //**********************************************************************************COMMENTS***********************************************************************************

    /**
     * {@code POST  /comments} : Add a new fspUserRegistrationComment to fspUserRegistration.
     *
     * @param commentDTO the comment text and id of fspUserRegistration.
     * @return commentDTO of created comment.
     */
    @PostMapping("/comments")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_REGISTRATION_MANAGE + "\") or hasAuthority(\"" + FLEX_USER_FSP_REGISTRATION_MANAGE + "\")")
    public ResponseEntity<FspUserRegistrationCommentDTO> addCommentToFspUserReg(@Valid @RequestBody FspUserRegistrationCommentDTO commentDTO) {
        log.debug("REST request add FspUserRegistrationComment : {}", commentDTO);
        FspUserRegistrationDTO fspUserRegDTO = fspUserRegistrationService.findOne(commentDTO.getFspUserRegistrationId()).orElseThrow(() ->
            new FspUserRegistrationResourceException("No fspUserRegistrationEntity was found for this id: " + commentDTO.getFspUserRegistrationId()));
        fspUserRegistrationValidator.validActualStatusOfRegistration(fspUserRegDTO.getStatus(),
            Sets.newHashSet(NEW, CONFIRMED_BY_FSP, PRE_CONFIRMED_BY_MO, USER_ACCOUNT_ACTIVATED_BY_FSP));
        if (commentDTO.getId() != null) {
            throw new BadRequestAlertException("A new FspUserRegistrationComment cannot already have an ID", ENTITY_NAME, "idexists");
        }
        FspUserRegistrationCommentDTO result = fspUserRegistrationService.addCommentToFspUserRegistration(commentDTO, userService.getCurrentUser());
        informMoAndFspAboutChangeInRegistration(fspUserRegDTO);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert(applicationName, true, COMMENT_ENTITY_NAME, result.getId().toString())).body(result);
    }

    /**
     * {@code GET  /comments} : get all the fspUserRegistrationComments of fspUserRegistration.
     *
     * @param fspUserRegId id of fspUserRegistration
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fspUserRegistrationComments in body.
     */
    @GetMapping("/{id}/comments")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_REGISTRATION_VIEW + "\") or hasAuthority(\"" + FLEX_USER_FSP_REGISTRATION_VIEW + "\")")
    public ResponseEntity<List<FspUserRegistrationCommentDTO>> getAllFspUserRegComments(@NotNull @PathVariable("id") Long fspUserRegId) {
        log.debug("REST request to get all fspUserRegistrationComments of FspUserRegistration");
        fspUserRegistrationService.findOne(fspUserRegId).orElseThrow(() ->
            new FspUserRegistrationResourceException("No fspUserRegistrationEntity was found for this id: " + fspUserRegId));
        List<FspUserRegistrationCommentDTO> commentDTOS = fspUserRegistrationService.findAllCommentsOfFspUserRegistration(fspUserRegId);
        return ResponseEntity.ok().body(commentDTOS);
    }
    //*****************************************************************************************************************************************************************************

    /**
     * Information for all MOs and FSP user candidate about change in fspUserRegistration.
     */
    protected void informMoAndFspAboutChangeInRegistration(FspUserRegistrationDTO fspUserRegDTO) {
        List<UserEntity> moUsers = userService.findUsersByRole(Role.ROLE_MARKET_OPERATOR);
        moUsers.forEach(moUser -> mailService.informMoAboutChangeInRegistration(moUser, fspUserRegDTO));
        if (nonNull(fspUserRegDTO.getFspUserId())) {
            UserEntity fspUser = userService.findOne(fspUserRegDTO.getFspUserId()).orElseThrow(() ->
                new FspUserRegistrationResourceException("No fsp userEntity was found for this id: " + fspUserRegDTO.getFspUserId()));
            mailService.informFspAboutChangeInRegistration(fspUser, fspUserRegDTO);
        }
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create().addParam(NotificationParam.ID, fspUserRegDTO.getId())
            .addParam(NotificationParam.COMPANY, fspUserRegDTO.getCompanyName())
            .addParam(NotificationParam.ROLE, fspUserRegDTO.getUserTargetRole().getShortName()).build();
        NotificationUtils.registerNewNotification(notifierFactory, NotificationEvent.FSP_USER_REGISTRATION_UPDATED, notificationParams);
    }
}
