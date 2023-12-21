package pl.com.tt.flex.server.web.rest.user.registration;

import com.google.common.collect.Sets;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.service.mail.fspUserRegistration.FspUserRegistrationMailService;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.registration.FspUserRegistrationQueryService;
import pl.com.tt.flex.server.service.user.registration.FspUserRegistrationService;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationCriteria;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationDTO;
import pl.com.tt.flex.server.service.user.registration.mapper.FspUserRegistrationFileMapper;
import pl.com.tt.flex.server.service.user.registration.mapper.FspUserRegistrationMapper;
import pl.com.tt.flex.server.validator.fspUserRegistration.FspUserRegistrationValidator;
import pl.com.tt.flex.server.web.rest.errors.common.WrongActualStatusException;

import java.util.List;
import java.util.Map;

import static pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus.*;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_FSP_REGISTRATION_MANAGE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_FSP_REGISTRATION_VIEW;

/**
 * REST controller for managing {@link FspUserRegistrationEntity} for FLEX-ADMIN web module.
 * Controller handles the registration of new FSP user (Flexibility Service Provider)
 * for OneNet Flexibility Platform FSP {@link Role}.
 */
@Slf4j
@RestController
@RequestMapping("/api/fsp-user-registration/admin")
public class FspUserRegistrationResourceAdmin extends FspUserRegistrationResource {

    public FspUserRegistrationResourceAdmin(FspUserRegistrationService fspUserRegistrationService, FspUserRegistrationQueryService fspUserRegistrationQueryService,
        FspUserRegistrationMapper fspUserRegistrationMapper, FspUserRegistrationFileMapper fspUserRegistrationFileMapper,
        FspUserRegistrationValidator fspUserRegistrationValidator, FspUserRegistrationMailService mailService, UserService userService, NotifierFactory notifierFactory) {
        super(fspUserRegistrationService, fspUserRegistrationQueryService, fspUserRegistrationMapper, fspUserRegistrationFileMapper, fspUserRegistrationValidator,
            mailService, userService, notifierFactory);
    }

    /**
     * {@code GET /:id/mo/pre-confirm} : pre confirm fspUserRegistration request by MO administrator
     * Status before operation: CONFIRMED_BY_FSP.
     * Status after operation: PRE_CONFIRMED_BY_MO.
     * A user with limited privileges is created for FSP candidate.
     * Candidate receives email message with login and url link allowing to set password for created user.
     * Candidate is able now to participate in his registration topic (e.g. adding comments and files).
     *
     * @param fspUserRegId the id of fspUserRegistration
     * @throws WrongActualStatusException {@code 400 (Bad Request)} when actual status of registration should be different to perform this operation
     * @throws RuntimeException           {@code 500 (Internal Server Error)} if the user's registration request couldn't be pre-confirmed.
     * @see FspUserRegistrationStatus
     */
    @GetMapping("/{id}/mo/pre-confirm")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_REGISTRATION_MANAGE + "\")")
    public void preConfirmFspRegistrationRequestByMo(@PathVariable("id") Long fspUserRegId) {
        log.debug("Rest request to pre-confirm fspUserRegistration with id: {} by MO administrator", fspUserRegId);
        FspUserRegistrationDTO fspUserRegDTO = fspUserRegistrationService.findOne(fspUserRegId)
            .orElseThrow(() -> new FspUserRegistrationResourceException("No fspUserRegistrationEntity was found with this id: " + fspUserRegId));
        fspUserRegistrationValidator.validActualStatusOfRegistration(fspUserRegDTO.getStatus(), Sets.newHashSet(CONFIRMED_BY_FSP));
        FspUserRegistrationEntity fspUserRegEntity = fspUserRegistrationService.preConfirmRegistrationRequestByMo(fspUserRegId);
        mailService.sendAccountActivationLinkToFsp(fspUserRegEntity);
    }

    /**
     * {@code GET  /:id/mo/accept} : accept fspUserRegistration request by MO administrator
     * Status before operation: USER_ACCOUNT_ACTIVATED_BY_FSP.
     * Status after operation: ACCEPTED_BY_MO.
     * FSP user with limited privileges is now able to fully use Flexibility Platform FSP.
     * Fsp object {@link FspEntity} is created for user.
     * FSP user and MO users receive email messages with information that registration request has been accepted.
     *
     * @param fspUserRegId the id of fspUserRegistration
     * @throws WrongActualStatusException {@code 400 (Bad Request)} when actual status of registration should be different to perform this operation
     * @throws RuntimeException           {@code 500 (Internal Server Error)} if the user's registration request couldn't be accepted.
     * @see FspUserRegistrationStatus
     */
    @GetMapping("/{id}/mo/accept")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_REGISTRATION_MANAGE + "\")")
    public void acceptFspRegistrationRequestByMo(@PathVariable("id") Long fspUserRegId) {
        log.debug("Rest request to accept fspUserRegistration with id: {} by MO administrator", fspUserRegId);
        FspUserRegistrationDTO fspUserRegDTO = fspUserRegistrationService.findOne(fspUserRegId)
            .orElseThrow(() -> new FspUserRegistrationResourceException("No fspUserRegistrationEntity was found with this id: " + fspUserRegId));
        fspUserRegistrationValidator.validActualStatusOfRegistration(fspUserRegDTO.getStatus(), Sets.newHashSet(USER_ACCOUNT_ACTIVATED_BY_FSP));
        FspUserRegistrationEntity fspUserRegEntity = fspUserRegistrationService.acceptRegistrationRequestByMo(fspUserRegId);
        mailService.informFspAboutRegistrationAcceptation(fspUserRegEntity);
        userService.findUsersByRole(Role.ROLE_MARKET_OPERATOR).forEach(moUser -> mailService.informMoAboutRegistrationAcceptation(moUser, fspUserRegEntity));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create().addParam(NotificationParam.ID, fspUserRegEntity.getId()).
            addParam(NotificationParam.COMPANY, fspUserRegEntity.getCompanyName()).build();
        NotificationUtils.registerNewNotification(notifierFactory, NotificationEvent.FSP_USER_REGISTRATION_ACCEPTED_BY_MO, notificationParams);
    }

    /**
     * {@code GET  /:id/mo/reject} : reject fspUserRegistration request by MO administrator
     * Status before operation: NEW / CONFIRMED_BY_FSP / PRE_CONFIRMED_BY_MO / USER_ACCOUNT_ACTIVATED_BY_FSP.
     * Status after operation: REJECTED_BY_MO.
     * FSP user with limited privileges is deactivated.
     * FSP user and MO users receive email messages with information that registration request has been rejected.
     *
     * @param fspUserRegId the id of fspUserRegistration
     * @throws WrongActualStatusException {@code 400 (Bad Request)} when actual status of registration should be different to perform this operation
     * @throws RuntimeException           {@code 500 (Internal Server Error)} if the user's registration request couldn't be accepted.
     * @see FspUserRegistrationStatus
     */
    @GetMapping("/{id}/mo/reject")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_REGISTRATION_MANAGE + "\")")
    public void rejectFspRegistrationRequestByMo(@PathVariable("id") Long fspUserRegId, @RequestParam(value = "langKey", required = false) String langKey) {
        log.debug("Rest request to reject fspUserRegistration with id: {} by MO administrator", fspUserRegId);
        FspUserRegistrationDTO fspUserRegDTO = fspUserRegistrationService.findOne(fspUserRegId)
            .orElseThrow(() -> new FspUserRegistrationResourceException("No fspUserRegistrationEntity was found with this id: " + fspUserRegId));
        fspUserRegistrationValidator.validActualStatusOfRegistration(fspUserRegDTO.getStatus(), Sets.newHashSet(NEW, CONFIRMED_BY_FSP, PRE_CONFIRMED_BY_MO,
            USER_ACCOUNT_ACTIVATED_BY_FSP));
        FspUserRegistrationEntity fspUserRegEntity = fspUserRegistrationService.rejectRegistrationRequestByMo(fspUserRegId);
        mailService.informFspAboutRegistrationRejection(fspUserRegEntity);
        userService.findUsersByRole(Role.ROLE_MARKET_OPERATOR).forEach(moUser -> mailService.informMoAboutRegistrationRejection(moUser, fspUserRegEntity));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create().addParam(NotificationParam.ID, fspUserRegEntity.getId()).
            addParam(NotificationParam.COMPANY, fspUserRegEntity.getCompanyName()).build();
        NotificationUtils.registerNewNotification(notifierFactory, NotificationEvent.FSP_USER_REGISTRATION_REJECTED_BY_MO, notificationParams);
    }

    /**
     * {@code GET} : get all the fspUserRegistrations.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of fspUserRegistrations in body.
     */
    @GetMapping
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_REGISTRATION_VIEW + "\")")
    public ResponseEntity<List<FspUserRegistrationDTO>> getAllFspUserRegistrations(FspUserRegistrationCriteria criteria, Pageable pageable) {
        log.debug("REST request to get FspUserRegistrations by criteria: {}", criteria);
        Page<FspUserRegistrationDTO> page = fspUserRegistrationQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /:id} : get the "id" fspUserRegistration.
     *
     * @param fspUserRegId the id of the FspUserRegistrationDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the FspUserRegistrationDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_REGISTRATION_VIEW + "\")")
    public ResponseEntity<FspUserRegistrationDTO> getFspUserRegistration(@PathVariable("id") Long fspUserRegId) {
        log.debug("REST request to get FspUserRegistration : {}", fspUserRegId);
        return ResponseUtil.wrapOrNotFound(fspUserRegistrationService.findOne(fspUserRegId));
    }

    /**
     * {@code GET /:id/mo/mark-as-read} : mark fspUserRegistration as read by administrator
     *
     * @param fspUserRegId the id of fspUserRegistration
     * @throws RuntimeException {@code 500 (Internal Server Error)} if the user's registration request couldn't be marked as read.
     */
    @GetMapping("/{id}/mo/mark-as-read")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_FSP_REGISTRATION_MANAGE + "\")")
    public void markFspRegistrationAsReadByMo(@PathVariable("id") Long fspUserRegId) {
        log.debug("Rest request to mark fspUserRegistration with id: {} as read by MO administrator", fspUserRegId);
        FspUserRegistrationDTO fspUserRegDTO = fspUserRegistrationService.findOne(fspUserRegId).orElseThrow(() ->
            new FspUserRegistrationResourceException("No fspUserRegistrationEntity was found with this id: " + fspUserRegId));
        fspUserRegistrationService.markFspUserRegistrationAsReadByAdmin(fspUserRegId, userService.getCurrentUser());
        informMoAndFspAboutChangeInRegistration(fspUserRegDTO);
    }
}
