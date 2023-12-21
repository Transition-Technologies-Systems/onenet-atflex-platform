package pl.com.tt.flex.server.service.mail.fspUserRegistration;

import com.google.common.collect.Maps;
import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import pl.com.tt.flex.server.config.ApplicationProperties;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationEntity;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationDTO;

import java.util.Locale;
import java.util.Map;

import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.FSP_REGISTRATION_ACCEPTED;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.FSP_REGISTRATION_APPLICATION_CONFIRAMTION_NOTIFICATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.FSP_REGISTRATION_APPLICATION_WITHDRAWAL;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.FSP_REGISTRATION_CHANGE_NOTIFICATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.FSP_REGISTRATION_CONFIRMATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.FSP_REGISTRATION_REJECTED;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.FSP_USER_ACTIVATION_CONFIRMATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.NEW_FSP_REGISTRATION_APPLICATION_NOTIFICATION;

/**
 * Service for sending emails for Fsp User Registration process.
 */
@Slf4j
@Service
public class FspUserRegistrationMailService extends MailService {

    protected static final String FSP_USER_REG = "fspUserReg";
    protected static final String ROLE = "regRole";

    public FspUserRegistrationMailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine,
                                          ApplicationProperties applicationProperties, UserEmailConfigRepository userEmailConfigRepository) {
        super(jHipsterProperties, javaMailSender, messageSource, templateEngine, applicationProperties, userEmailConfigRepository);
    }

    @Async
    public void sendEmailFromTemplate(MailRecipientDTO recipient, String templateName, String titleKey, Map<String, Object> variables, String baseUrl, EmailType type) {
        if (recipient.getEmail() == null) {
            log.debug("Email field is null in mailRecipientDTO: {}", recipient);
            return;
        }
        Context context = new Context(recipient.getLocale());
        context.setVariables(variables);
        context.setVariable(BASE_URL, baseUrl);
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, recipient.getLocale());
        sendEmail(recipient, subject, content, false, true, null, type);
    }

    /**
     * Mail with link to confirm the willingness to register by the candidate for FSP user
     */
    @Async
    public void sendRequestConfirmationLinkToFsp(FspUserRegistrationEntity fspUserReg) {
        log.debug("Sending email to fsp user candidate [fspUserRegistrationId: {}] with link to confirm his registration request", fspUserReg.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(fspUserReg.getId(), fspUserReg.getClass().getSimpleName(), fspUserReg.getEmail(), Locale.forLanguageTag(fspUserReg.getLangKey()));
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(FSP_USER_REG, fspUserReg);
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toFsp/requestConfirmationLinkEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlUser(), FSP_REGISTRATION_CONFIRMATION);
    }

    /**
     * Activation link for newly crated FSP user candidate with limited privileges.
     */
    @Async
    public void sendAccountActivationLinkToFsp(FspUserRegistrationEntity fspUserReg) {
        log.debug("Sending account activation email to newly crated FSP user [id: {}]", fspUserReg.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(fspUserReg.getId(), fspUserReg.getClass().getSimpleName(), fspUserReg.getEmail(), Locale.forLanguageTag(fspUserReg.getFspUser().getLangKey()));
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(FSP_USER_REG, fspUserReg);
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toFsp/accountActivationLinkEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlUser(), FSP_USER_ACTIVATION_CONFIRMATION);
    }

    /**
     * Information for all MOs that new FSP user candidate sent registration request.
     */
    @Async
    public void informMoAboutNewRegistration(UserEntity mo, FspUserRegistrationDTO fspUserReg) {
        log.debug("Sending informing email to MO {} that new FSP user candidate {} sent registration request", mo, fspUserReg);
        MailRecipientDTO recipient = new MailRecipientDTO(mo);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, mo);
        variables.put(ROLE, fspUserReg.getUserTargetRole().getShortName());
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toMo/newRegistrationEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlAdmin(), NEW_FSP_REGISTRATION_APPLICATION_NOTIFICATION);
    }

    /**
     * Information for all MOs that new FSP user candidate confirmed registration request.
     */
    @Async
    public void informMoAboutConfirmRegistrationByFsp(UserEntity mo, FspUserRegistrationDTO fspUserReg) {
        log.debug("Sending informing email to MO {} that new FSP user candidate {} confirmed registration request", mo, fspUserReg);
        MailRecipientDTO recipient = new MailRecipientDTO(mo);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, mo);
        variables.put(FSP_USER_REG, fspUserReg);
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toMo/registrationConfirmedByFspEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlAdmin(), FSP_REGISTRATION_APPLICATION_CONFIRAMTION_NOTIFICATION);
    }

    /**
     * Information for all MOs about change in fspUserRegistration.
     */
    @Async
    public void informMoAboutChangeInRegistration(UserEntity mo, FspUserRegistrationDTO fspUserRegDTO) {
        log.debug("Sending informing email to MO {} about change in fspUserRegistration {}", mo, fspUserRegDTO);
        MailRecipientDTO recipient = new MailRecipientDTO(mo);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, mo);
        variables.put(FSP_USER_REG, fspUserRegDTO);
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toMo/changeInRegistrationEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlAdmin(), FSP_REGISTRATION_CHANGE_NOTIFICATION);
    }

    /**
     * Information for FSP user candidate about change in his fspRegistration.
     */
    @Async
    public void informFspAboutChangeInRegistration(UserEntity fsp, FspUserRegistrationDTO fspUserRegDTO) {
        log.debug("Sending informing email to FSP {} about change in his fspUserRegistration {}", fsp, fspUserRegDTO);
        MailRecipientDTO recipient = new MailRecipientDTO(fsp);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, fsp);
        variables.put(FSP_USER_REG, fspUserRegDTO);
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toFsp/changeInRegistrationEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlUser(), FSP_REGISTRATION_CHANGE_NOTIFICATION);
    }

    /**
     * Information for FSP user candidate that his fspUserRegistration has been accepted.
     */
    @Async
    public void informFspAboutRegistrationAcceptation(FspUserRegistrationEntity fspUserRegEntity) {
        log.debug("Sending informing email to FSP that his fspUserRegistration {} has been accepted", fspUserRegEntity);
        MailRecipientDTO recipient = new MailRecipientDTO(fspUserRegEntity.getFspUser());
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(FSP_USER_REG, fspUserRegEntity);
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toFsp/registrationAcceptedEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlUser(), FSP_REGISTRATION_ACCEPTED);
    }

    /**
     * Information for all MO users that fspUserRegistration has been accepted.
     */
    @Async
    public void informMoAboutRegistrationAcceptation(UserEntity mo, FspUserRegistrationEntity fspUserRegEntity) {
        log.debug("Sending informing email to MO {} that his fspUserRegistration {} has been accepted", mo, fspUserRegEntity);
        MailRecipientDTO recipient = new MailRecipientDTO(mo);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, mo);
        variables.put(FSP_USER_REG, fspUserRegEntity);
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toMo/registrationAcceptedEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlAdmin(), FSP_REGISTRATION_ACCEPTED);
    }

    /**
     * Information for FSP user candidate that his fspUserRegistration has been rejected.
     */
    @Async
    public void informFspAboutRegistrationRejection(FspUserRegistrationEntity fspUserRegEntity) {
        log.debug("Sending informing email to FSP that his fspUserRegistration {} has been rejected", fspUserRegEntity);
        MailRecipientDTO recipient = nonNull(fspUserRegEntity.getFspUser()) ? new MailRecipientDTO(fspUserRegEntity.getFspUser()) :
            new MailRecipientDTO(fspUserRegEntity.getId(), fspUserRegEntity.getClass().getSimpleName(), fspUserRegEntity.getEmail(), Locale.forLanguageTag(fspUserRegEntity.getFspUser().getLangKey()));
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(FSP_USER_REG, fspUserRegEntity);
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toFsp/registrationRejectedEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlUser(), FSP_REGISTRATION_REJECTED);
    }

    /**
     * Information for all MO users that fspUserRegistration has been rejected.
     */
    @Async
    public void informMoAboutRegistrationRejection(UserEntity mo, FspUserRegistrationEntity fspUserRegEntity) {
        log.debug("Sending informing email to MO {} that his fspUserRegistration {} has been rejected", mo, fspUserRegEntity);
        MailRecipientDTO recipient = new MailRecipientDTO(mo);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, mo);
        variables.put(FSP_USER_REG, fspUserRegEntity);
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toMo/registrationRejectedEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlAdmin(), FSP_REGISTRATION_REJECTED);
    }

    /**
     * Information for FSP user candidate that his fspUserRegistration has been withdrawn by himself.
     */
    @Async
    public void informFspAboutRegistrationWithdrawn(FspUserRegistrationEntity fspUserRegEntity) {
        log.debug("Sending informing email to FSP that his fspUserRegistration {} has been withdrawn by himself", fspUserRegEntity);
        MailRecipientDTO recipient = nonNull(fspUserRegEntity.getFspUser()) ? new MailRecipientDTO(fspUserRegEntity.getFspUser()) :
            new MailRecipientDTO(fspUserRegEntity.getId(), fspUserRegEntity.getClass().getSimpleName(), fspUserRegEntity.getEmail(), Locale.forLanguageTag(fspUserRegEntity.getFspUser().getLangKey()));
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(FSP_USER_REG, fspUserRegEntity);
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toFsp/registrationWithdrawnEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlUser(), FSP_REGISTRATION_APPLICATION_WITHDRAWAL);
    }

    /**
     * Information for all MO users that fspUserRegistration has been withdrawn by candidate.
     */
    @Async
    public void informMoAboutRegistrationWithdrawn(UserEntity mo, FspUserRegistrationEntity fspUserRegEntity) {
        log.debug("Sending informing email to MO {} that his fspUserRegistration {} has been rejected", mo, fspUserRegEntity);
        MailRecipientDTO recipient = new MailRecipientDTO(mo);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, mo);
        variables.put(FSP_USER_REG, fspUserRegEntity);
        sendEmailFromTemplate(recipient, "mail/fspUserRegistration/toMo/registrationWithdrawnEmail", "email.fspUserRegistration.title", variables,
            applicationProperties.getMail().getBaseUrlAdmin(), FSP_REGISTRATION_APPLICATION_WITHDRAWAL);
    }
}
