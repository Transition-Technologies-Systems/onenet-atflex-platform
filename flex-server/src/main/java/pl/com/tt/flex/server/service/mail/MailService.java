package pl.com.tt.flex.server.service.mail;

import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.config.ApplicationProperties;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.unit.UnitDirectionOfDeviation;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static pl.com.tt.flex.model.security.permission.Role.*;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.ACCOUNT_PASSWORD_RESET;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.USER_ACCOUNT_ACTIVATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.USER_ACCOUNT_CREATION;

/**
 * Service for sending emails.
 * <p>
 * We use the {@link Async} annotation to send emails asynchronously.
 */
@Slf4j
@Service
public class MailService {

    private static final String LANG_PL = "pl";
    private static final String LANG_EN = "en";

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    protected static final String USER = "user";

    protected static final String BASE_URL = "baseUrl";

    protected final JHipsterProperties jHipsterProperties;

    protected final ApplicationProperties applicationProperties;

    private final JavaMailSender javaMailSender;

    protected final MessageSource messageSource;

    protected final SpringTemplateEngine templateEngine;

    protected final String SUBJECT_FORMAT = "%s - %s";

    private final UserEmailConfigRepository userEmailConfigRepository;

    public MailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource,
                       SpringTemplateEngine templateEngine, ApplicationProperties applicationProperties, UserEmailConfigRepository userEmailConfigRepository) {

        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.applicationProperties = applicationProperties;
        this.userEmailConfigRepository = userEmailConfigRepository;
    }

    @Async
    public void sendEmail(MailRecipientDTO to, String subject, String content, boolean isMultipart, boolean isHtml, FileDTO attachment, EmailType type) {
        if (notificationEnabled(type, to)) {
            log.debug("Send email to {}", to);
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            try {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
                message.setTo(to.getEmail());
                message.setFrom(jHipsterProperties.getMail().getFrom());
                message.setSubject(String.format(SUBJECT_FORMAT, applicationProperties.getMail().getSubjectPrefix(), subject));
                message.setText(content, isHtml);
                if (Objects.nonNull(attachment)) {
                    message.addAttachment(attachment.getFileName(), new ByteArrayResource(attachment.getBytesData()));
                }
                javaMailSender.send(mimeMessage);
                log.debug("Sent email to User '{}'", to);
            } catch (MailException | MessagingException e) {
                log.warn("Email could not be sent to user '{}'", to, e);
            }
        }
    }

    @Async
    public void sendEmailFromTemplate(UserEntity user, String templateName, String titleKey, String langKey, EmailType type) {
        if (user.getEmail() == null) {
            log.debug("Email doesn't exist for user '{}'", user.getLogin());
            return;
        }
        Locale locale;
        // Gdy langKey nie jest puste to wymuś użycie danego języka
        if (langKey != null) {
            locale = Locale.forLanguageTag(langKey);
        } else {
            locale = Locale.forLanguageTag(user.getLangKey());
        }
        Context context = new Context(locale);
        context.setVariable(USER, user);
        setMailBaseUrlByUserRole(user, context);
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        MailRecipientDTO recipient = new MailRecipientDTO(user.getId(), user.getClass().getSimpleName(), user.getEmail(), locale);
        sendEmail(recipient, subject, content, false, true, null, type);
    }

    private boolean notificationEnabled(EmailType type, MailRecipientDTO recipientUser) {
        boolean notificationDisabledByUser = userEmailConfigRepository.existsByUserIdAndEmailTypeAndEnabledFalse(recipientUser.getEntityId(), type);
        return !type.isOptional() || !notificationDisabledByUser;
    }

    private void setMailBaseUrlByUserRole(UserEntity user, Context context) {
        context.setVariable(BASE_URL, getBaseUrlForUser(user));
    }

    @Async
    public void sendActivationEmail(UserEntity user) {
        log.debug("Sending activation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/activationEmail", "email.activation.title", null, USER_ACCOUNT_ACTIVATION);
    }

    @Async
    public void sendCreationEmail(UserEntity user) {
        log.debug("Sending creation email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/creationEmail", "email.activation.title", null, USER_ACCOUNT_CREATION);
    }

    @Async
    public void sendPasswordResetMail(UserEntity user, String langKey) {
        log.debug("Sending password reset email to '{}'", user.getEmail());
        sendEmailFromTemplate(user, "mail/passwordResetEmail", "email.reset.title", langKey, ACCOUNT_PASSWORD_RESET);
    }

    protected String getBaseUrlForUser(UserEntity userEntity) {
        if (CollectionUtils.containsAny(userEntity.getRoles(), FSP_ORGANISATIONS_ROLES) && !userEntity.getRoles().contains(ROLE_ADMIN)) {
            return applicationProperties.getMail().getBaseUrlUser();
        }
        return applicationProperties.getMail().getBaseUrlAdmin();
    }

    protected void addVariable(Object object, String param, Map<String, Object> variables, Locale locale) {
        if (Objects.isNull(object)) {
            variables.put(param, "");
        } else if (object instanceof Instant) {
            String date = dateTimeFormatter.format((Instant) object);
            variables.put(param, date);
        } else if (object instanceof Boolean) {
            String booleanString = messageSource.getMessage("exporter.boolean." + object, null, locale);
            variables.put(param, booleanString);
        } else if (object instanceof DictionaryDTO) {
            String translatedValue = getDictionaryTranslatedValue((DictionaryDTO) object, locale);
            variables.put(param, (translatedValue));
        } else if (object instanceof UnitDirectionOfDeviation) {
            var message = messageSource.getMessage("exporter.unit.directionOfDeviation." + object, null, locale);
            variables.put(param, message);
        } else {
            variables.put(param, object);
        }
    }

    // porownuje stary obiekt z modyfikowany i dodaje obiket do 'variables' z paramaterem 'param', gdy nastapia edycja obiektu
    // obiekty typu Instant zapisuje zgodnie z ustalonym formatem: dd/MM/yyyy HH:mm:ss,
    // obiekt typu Boolean zapisuje wedlug parametru locale,
    // np. oldObject = true, modifiedObject = false, param = TEST, locale = pl --> variables.put(TEST, 'nie')
    protected void addVariableIfModified(Object oldObject, Object modifiedObject, String param, Map<String, Object> variables, Locale locale) {
        if (!Objects.equals(oldObject, modifiedObject)) {
            if (Objects.isNull(modifiedObject)) {
                variables.put(param, "");
            } else if (modifiedObject instanceof Instant) {
                String date = dateTimeFormatter.format((Instant) modifiedObject);
                variables.put(param, date);
            } else if (modifiedObject instanceof Boolean) {
                String booleanString = messageSource.getMessage("exporter.boolean." + modifiedObject, null, locale);
                variables.put(param, booleanString);
            } else if (modifiedObject instanceof DictionaryDTO) {
                String translatedValue = getDictionaryTranslatedValue((DictionaryDTO) modifiedObject, locale);
                variables.put(param, (translatedValue));
            } else if (modifiedObject instanceof UnitDirectionOfDeviation) {
                var message = messageSource.getMessage("exporter.unit.directionOfDeviation." + modifiedObject, null, locale);
                variables.put(param, message);
            } else if (modifiedObject instanceof DerTypeMinDTO) {
                String derType = getDerTypeValue((DerTypeMinDTO) modifiedObject, locale);
                variables.put(param, derType);
            } else {
                variables.put(param, modifiedObject);
            }
        }
    }

    private String getDictionaryTranslatedValue(DictionaryDTO dictionaryDTO, Locale locale) {
        if (Locale.forLanguageTag(LANG_PL).equals(locale)) {
            return dictionaryDTO.getDescriptionPl();
        }
        return dictionaryDTO.getDescriptionEn();
    }

    private String getDerTypeValue(DerTypeMinDTO derTypeMinDTO, Locale locale) {
        if (Locale.forLanguageTag(LANG_PL).equals(locale)) {
            return derTypeMinDTO.getDescriptionPl();
        }
        return derTypeMinDTO.getDescriptionEn();
    }
}
