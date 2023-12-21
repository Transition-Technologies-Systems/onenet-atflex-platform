package pl.com.tt.flex.server.web.rest.user.registration;

import com.google.common.collect.Sets;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.common.errors.mail.EmailAlreadyUsedException;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.service.common.dto.FileDTOUtil;
import pl.com.tt.flex.server.service.mail.fspUserRegistration.FspUserRegistrationMailService;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.registration.FspUserRegistrationQueryService;
import pl.com.tt.flex.server.service.user.registration.FspUserRegistrationService;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationDTO;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationFileDTO;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationFormDTO;
import pl.com.tt.flex.server.service.user.registration.mapper.FspUserRegistrationFileMapper;
import pl.com.tt.flex.server.service.user.registration.mapper.FspUserRegistrationMapper;
import pl.com.tt.flex.server.validator.fspUserRegistration.FspUserRegistrationValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;
import pl.com.tt.flex.server.web.rest.errors.common.WrongActualStatusException;
import pl.com.tt.flex.server.web.rest.errors.file.FileParseException;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus.*;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_FSP_REGISTRATION_MANAGE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_FSP_REGISTRATION_VIEW;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SECURITY_KEY_IS_INVALID_OR_EXPIRED;

/**
 * REST controller for managing {@link FspUserRegistrationEntity} for FLEX-USER web module.
 * Controller handles the registration of new FSP user (Flexibility Service Provider)
 * for OneNet Flexibility Platform FSP {@link Role}.
 */
@Slf4j
@RestController
@RequestMapping("/api/fsp-user-registration/user")
public class FspUserRegistrationResourceUser extends FspUserRegistrationResource {

    public FspUserRegistrationResourceUser(FspUserRegistrationService fspUserRegistrationService, FspUserRegistrationQueryService fspUserRegistrationQueryService,
        FspUserRegistrationMapper fspUserRegistrationMapper, FspUserRegistrationFileMapper fspUserRegistrationFileMapper,
        FspUserRegistrationValidator fspUserRegistrationValidator, FspUserRegistrationMailService mailService, UserService userService, NotifierFactory notifierFactory) {
        super(fspUserRegistrationService, fspUserRegistrationQueryService, fspUserRegistrationMapper, fspUserRegistrationFileMapper, fspUserRegistrationValidator,
            mailService, userService, notifierFactory);
    }

    /**
     * {@code POST  /fsp/create} : Create a new fspUserRegistration - registration request sent by candidate for FSP platform user.
     * FspUserRegistration is created with status NEW.
     * Attached files (FspUserRegistrationFile) are joined to initial comment (FspUserRegistrationComment).
     * Candidate should receive email message within he can confirm or withdraw his registration request:
     * - if confirmed then candidate waits for another email with activation link, which will be sent after pre confirmation of registration request by MO (administrator)
     * - if canceled then candidate is able to lock or release his email for further registration requests
     *
     * @param fspUserRegistrationDTO the managed fspUserRegistration Model.
     * @param files                  files attached (at least one is required).
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email address is already used.
     * @throws BadRequestAlertException  {@code 400 (Bad Request)} if the fspUserRegistration has already an ID.
     * @throws FileParseException        {@code 400 (Bad Request)} if problem occurred while parsing attached files
     * @see FspUserRegistrationStatus
     */
    @PostMapping(value = "/fsp/create")
    public ResponseEntity<FspUserRegistrationDTO> createFspUserRegistration(
        @Valid @RequestPart("fspUserRegistrationDTO") FspUserRegistrationDTO fspUserRegistrationDTO,
        @RequestPart("files") MultipartFile[] files)
        throws URISyntaxException, ObjectValidationException {

        log.debug("REST request to create new FspUserRegistration");
        if (fspUserRegistrationDTO.getId() != null) {
            throw new BadRequestAlertException("A new fspUserRegistration cannot already have an ID", ENTITY_NAME, "idexists");
        }
        fspUserRegistrationDTO.setStatus(NEW);
        fspUserRegistrationValidator.checkFileExtensionValid(Arrays.stream(files).collect(Collectors.toList()));
        fspUserRegistrationValidator.checkRole(fspUserRegistrationDTO);
        fspUserRegistrationValidator.checkRulesField(fspUserRegistrationDTO);
        List<FspUserRegistrationFileDTO> fspFileDTOS = Arrays.stream(files).map(file -> new FspUserRegistrationFileDTO(FileDTOUtil.parseMultipartFile(file))).collect(Collectors.toList());
        FspUserRegistrationEntity fspUserRegistrationEntity = fspUserRegistrationService.createRegistrationRequest(fspUserRegistrationDTO, fspFileDTOS);
        FspUserRegistrationDTO result = fspUserRegistrationMapper.toDto(fspUserRegistrationEntity);
        mailService.sendRequestConfirmationLinkToFsp(fspUserRegistrationEntity);
        userService.findUsersByRole(Role.ROLE_MARKET_OPERATOR).forEach(moUser -> mailService.informMoAboutNewRegistration(moUser, result));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(NotificationParam.ROLE, result.getUserTargetRole().getShortName()).build();
        NotificationUtils.registerNewNotification(notifierFactory, NotificationEvent.FSP_USER_REGISTRATION_NEW, notificationParams);
        return ResponseEntity.created(new URI("/api/fsp-user-registration/" + result.getId())).headers(HeaderUtil.createEntityCreationAlert(
            applicationName, true, ENTITY_NAME, result.getId().toString())).body(result);
    }

    /**
     * {@code GET  /fsp/confirm-by-key} : confirm fspUserRegistration request by FSP candidate
     * Status before operation: NEW.
     * Status after operation: CONFIRMED_BY_FSP.
     * MO administrators receive emails with information about new registration request in system.
     *
     * @param key security key used to authenticate candidate
     * @throws WrongActualStatusException {@code 400 (Bad Request)} when actual status of registration should be different to perform this operation
     * @throws RuntimeException           {@code 500 (Internal Server Error)} if the user's registration request couldn't be confirmed.
     * @see FspUserRegistrationStatus
     */
    @GetMapping("/fsp/confirm-by-key")
    public ResponseEntity<FspUserRegistrationDTO> confirmRegistrationRequestByFspUsingKey(@RequestParam(value = "key") String key) throws ObjectValidationException {
        log.debug("Rest request to confirm fspUserRegistration request by FSP using key by candidate");
        FspUserRegistrationDTO fspUserReg = fspUserRegistrationService.findOneBySecurityKey(key)
            .orElseThrow(() -> new ObjectValidationException("No fspUserRegistrationEntity was found for this key: " + key, SECURITY_KEY_IS_INVALID_OR_EXPIRED));
        fspUserRegistrationValidator.validActualStatusOfRegistration(fspUserReg.getStatus(), Sets.newHashSet(NEW));
        FspUserRegistrationDTO result = fspUserRegistrationService.confirmNewRegistrationRequestByFsp(fspUserReg.getId());
        userService.findUsersByRole(Role.ROLE_MARKET_OPERATOR).forEach(moUser -> mailService.informMoAboutConfirmRegistrationByFsp(moUser, result));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create().addParam(NotificationParam.ID, result.getId()).
            addParam(NotificationParam.COMPANY, result.getCompanyName()).build();
        NotificationUtils.registerNewNotification(notifierFactory, NotificationEvent.FSP_USER_REGISTRATION_CONFIRMED_BY_FSP, notificationParams);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString())).body(result);
    }

    /**
     * {@code GET  /fsp/withdraw-by-key} : withdraw fspUserRegistration request by FSP candidate using link from email message
     * Status before operation: NEW.
     * Status after operation: WITHDRAWN_BY_FSP.
     *
     * @param key           security key used to authenticate candidate
     * @param removeDbEntry - If true then remove registration request entry from database.
     *                      - If false then FspUserRegistration is updated with WITHDRAWN FspUserRegistrationStatus.
     *                      Email address from this registration is locked for further registration requests
     * @throws WrongActualStatusException {@code 400 (Bad Request)} when actual status of registration should be different to perform this operation
     * @throws RuntimeException           {@code 500 (Internal Server Error)} if the user's registration request couldn't be withdrawn.
     * @see FspUserRegistrationStatus
     */
    @GetMapping("/fsp/withdraw-by-key")
    public void withdrawRegistrationRequestByFspUsingKey(@RequestParam(value = "key") String key, @RequestParam(value = "removeDbEntry") boolean removeDbEntry)
        throws ObjectValidationException {
        log.debug("Rest request to withdrawn fspUserRegistration by FSP using key (removing db entry: {}) by candidate", removeDbEntry);
        FspUserRegistrationDTO fspUserReg = fspUserRegistrationService.findOneBySecurityKey(key)
            .orElseThrow(() -> new ObjectValidationException("No fspUserRegistrationEntity was found for this key: " + key, SECURITY_KEY_IS_INVALID_OR_EXPIRED));
        fspUserRegistrationValidator.validActualStatusOfRegistration(fspUserReg.getStatus(), Sets.newHashSet(NEW));
        fspUserRegistrationService.withdrawNewRegistrationRequestByFsp(fspUserReg.getId(), removeDbEntry);
    }

    /**
     * {@code GET  /fsp/by-user-key} : get fspUserRegistration by user activation key. Returns only data from form sent by candidate.
     * Required status of registration: PRE_CONFIRMED_BY_MO.
     *
     * @param key security key used to authenticate candidate
     * @throws WrongActualStatusException {@code 400 (Bad Request)} when actual status of registration should be different to perform this operation
     * @throws RuntimeException           {@code 500 (Internal Server Error)} if couldn't get the user's registration data.
     * @see FspUserRegistrationStatus
     */
    @GetMapping("/fsp/by-user-key")
    public ResponseEntity<FspUserRegistrationFormDTO> getFspRegistrationByUserActivationKey(@RequestParam(value = "key") String key) throws ObjectValidationException {
        log.debug("Rest request to get fspUserRegistration data by FSP user activation key");
        FspUserRegistrationDTO fspUserReg = fspUserRegistrationService.findOneByUserActivationKey(key)
            .orElseThrow(() -> new ObjectValidationException("No fspUserRegistrationEntity was found for this key: " + key, SECURITY_KEY_IS_INVALID_OR_EXPIRED));
        fspUserRegistrationValidator.validActualStatusOfRegistration(fspUserReg.getStatus(), Sets.newHashSet(PRE_CONFIRMED_BY_MO));
        return ResponseEntity.ok(fspUserRegistrationMapper.toDtoOnlyWithFormData(fspUserReg));
    }

    /**
     * {@code GET  /:id/fsp/withdraw} : withdraw fspUserRegistration request by logged-in FSP candidate
     * Status before operation: PRE_CONFIRMED_BY_MO / USER_ACCOUNT_ACTIVATED_BY_FSP.
     * Status after operation: WITHDRAWN_BY_FSP.
     * Candidate's user account is deactivated.
     * MO administrators and candidate receive email messages with information that registration request has been withdrawn.
     *
     * @param fspUserRegId the id of fspUserRegistration
     * @throws WrongActualStatusException {@code 400 (Bad Request)} when actual status of registration should be different to perform this operation
     * @throws RuntimeException           {@code 500 (Internal Server Error)} if the user's registration request couldn't be withdrawn.
     * @see FspUserRegistrationStatus
     */
    @GetMapping("/{id}/fsp/withdraw")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FSP_REGISTRATION_MANAGE + "\")")
    public void withdrawRegistrationRequestByFsp(@PathVariable("id") Long fspUserRegId) {
        log.debug("Rest request to withdraw fspUserRegistration with id: {} by candidate", fspUserRegId);
        FspUserRegistrationDTO fspUserRegDTO = fspUserRegistrationService.findOne(fspUserRegId)
            .orElseThrow(() -> new FspUserRegistrationResourceException("No fspUserRegistrationEntity was found with this id: " + fspUserRegId));
        fspUserRegistrationValidator.validActualStatusOfRegistration(fspUserRegDTO.getStatus(), Sets.newHashSet(PRE_CONFIRMED_BY_MO, USER_ACCOUNT_ACTIVATED_BY_FSP));
        FspUserRegistrationEntity fspUserRegEntity = fspUserRegistrationService.withdrawPreConfirmedRegistrationRequestByFsp(fspUserRegId);
        mailService.informFspAboutRegistrationWithdrawn(fspUserRegEntity);
        userService.findUsersByRole(Role.ROLE_MARKET_OPERATOR).forEach(moUser -> mailService.informMoAboutRegistrationWithdrawn(moUser, fspUserRegEntity));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create().addParam(NotificationParam.ID, fspUserRegEntity.getId()).
            addParam(NotificationParam.COMPANY, fspUserRegEntity.getCompanyName()).build();
        NotificationUtils.registerNewNotification(notifierFactory, NotificationEvent.FSP_USER_REGISTRATION_WITHDRAWN_BY_FSP, notificationParams);
    }

    /**
     * {@code GET  /fsp} : get fspUserRegistration of a logged-in candidate.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the FspUserRegistrationDTO, or with status {@code 404 (Not Found)}.
     * @throws RuntimeException {@code 500 (Internal Server Error)} if couldn't get login of current logged-in candidate.
     */
    @GetMapping("/fsp")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_FSP_REGISTRATION_VIEW + "\")")
    public ResponseEntity<FspUserRegistrationDTO> getFspUserRegistrationOfLoggedInCandidate() {
        log.debug("REST request to get FspUserRegistration of logged-in candidate");
        return ResponseUtil.wrapOrNotFound(fspUserRegistrationService.findOneByFspUserId(userService.getCurrentUser().getId()));
    }
}
