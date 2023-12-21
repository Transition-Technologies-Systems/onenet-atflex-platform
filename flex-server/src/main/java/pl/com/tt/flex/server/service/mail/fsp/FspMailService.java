package pl.com.tt.flex.server.service.mail.fsp;

import com.google.common.collect.Maps;
import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.config.ApplicationProperties;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Locale.forLanguageTag;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.FSP_EDITION;

@Service
@Slf4j
public class FspMailService extends MailService {

    protected static final String FSP_ID = "fsp_id";
    protected static final String BSP_ID = "bsp_id";
    protected static final String BSP_COMPANY = "bspCompany";
    protected static final String FSP_COMPANY = "fspCompany";
    protected static final String COMPANY_NAME = "companyName";
    protected static final String FIRST_NAME = "firstName";
    protected static final String LAST_NAME = "lastName";
    protected static final String PHONE_NUMBER = "phoneNumber";
    protected static final String EMAIL = "email";
    protected static final String VALID_FROM = "validFrom";
    protected static final String VALID_TO = "validTo";
    protected static final String ACTIVE = "active";
    protected static final String AGREEMENT_WITH_TSO = "agreementWithTso";


    public FspMailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine,
                          ApplicationProperties applicationProperties, UserEmailConfigRepository userEmailConfigRepository) {
        super(jHipsterProperties, javaMailSender, messageSource, templateEngine, applicationProperties, userEmailConfigRepository);
    }

    @Async
    public void sendEmailFromTemplate(MailRecipientDTO recipient, String templateName, String titleKey, Object[] titleVariables, Map<String, Object> variables, String baseUrl, EmailType type) {
        if (recipient.getEmail() == null) {
            log.debug("Email field is null in mailRecipientDTO: {}", recipient);
            return;
        }
        Context context = new Context(recipient.getLocale());
        context.setVariables(variables);
        context.setVariable(BASE_URL, baseUrl);
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, titleVariables, recipient.getLocale());
        sendEmail(recipient, subject, content, false, true, null, type);
    }


    @Async
    public void informUserAboutFspEdition(UserEntity user, FspDTO oldFspDTO, FspDTO modifiedFspDTO, UserEntity modifiedRepresentative) {
        log.debug("Sending informing email to user {} that product with id: {} is updated", user, modifiedFspDTO.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, user);
        Locale locale = forLanguageTag(user.getLangKey());
        addVariableIfModified(oldFspDTO.getCompanyName(), modifiedFspDTO.getCompanyName(), COMPANY_NAME, variables, locale);
        addVariableIfModified(oldFspDTO.getRepresentative().getFirstName(), modifiedRepresentative.getFirstName(), FIRST_NAME, variables, locale);
        addVariableIfModified(oldFspDTO.getRepresentative().getLastName(), modifiedRepresentative.getLastName(), LAST_NAME, variables, locale);
        addVariableIfModified(oldFspDTO.getRepresentative().getEmail(), modifiedRepresentative.getEmail(), EMAIL, variables, locale);
        addVariableIfModified(oldFspDTO.getRepresentative().getPhoneNumber(), modifiedRepresentative.getPhoneNumber(), PHONE_NUMBER, variables, locale);
        addVariableIfModified(oldFspDTO.getValidFrom(), modifiedFspDTO.getValidFrom(), VALID_FROM, variables, locale);
        addVariableIfModified(oldFspDTO.getValidTo(), modifiedFspDTO.getValidTo(), VALID_TO, variables, locale);
        addVariableIfModified(oldFspDTO.isActive(), modifiedFspDTO.isActive(), ACTIVE, variables, locale);

        String titleKey;
        if (oldFspDTO.getRole().equals(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            variables.put(BSP_COMPANY, modifiedFspDTO.getCompanyName());
            variables.put(BSP_ID, modifiedFspDTO.getId());
            addVariableIfModified(oldFspDTO.isAgreementWithTso(), modifiedFspDTO.isAgreementWithTso(), AGREEMENT_WITH_TSO, variables, locale);
            titleKey = "email.fsp.edition.bsp.title";
        } else {
            variables.put(FSP_COMPANY, modifiedFspDTO.getCompanyName());
            variables.put(FSP_ID, modifiedFspDTO.getId());
            titleKey = "email.fsp.edition.title";
        }
        List<Object> titleVariables = Collections.singletonList(modifiedFspDTO.getCompanyName());
        sendEmailFromTemplate(recipient, "mail/fsp/fspEdition", titleKey, titleVariables.toArray(),
            variables, getBaseUrlForUser(user), FSP_EDITION);
    }
}
