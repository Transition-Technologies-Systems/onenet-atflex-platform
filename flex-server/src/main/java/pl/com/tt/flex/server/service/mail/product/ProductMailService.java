package pl.com.tt.flex.server.service.mail.product;

import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.PRODUCT_CREATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.PRODUCT_EDITION;

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
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.product.dto.ProductMailDTO;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ProductMailService extends MailService {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
    protected static final String OLD_PRODUCT = "oldProduct";
    protected static final String PRODUCT = "product";
    protected static final String PSO_USER = "psoUser";
    protected static final String SSO_USERS = "ssoUsers";
    protected static final String VALID_FROM = "validFrom";
    protected static final String VALID_TO = "validTo";
    protected static final String EDITED_FIELDS = "editedFields";

    public ProductMailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine,
                              ApplicationProperties applicationProperties, UserEmailConfigRepository userEmailConfigRepository) {
        super(jHipsterProperties, javaMailSender, messageSource, templateEngine, applicationProperties, userEmailConfigRepository);
    }

    @Async
    public void sendEmailFromTemplate(MailRecipientDTO recipient, String templateName, String titleKey, Object[] titleVars, Map<String, Object> variables, String baseUrl, EmailType type) {
        if (recipient.getEmail() == null) {
            log.debug("Email field is null in mailRecipientDTO: {}", recipient);
            return;
        }
        Context context = new Context(recipient.getLocale());
        context.setVariables(variables);
        context.setVariable(BASE_URL, baseUrl);
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, titleVars, recipient.getLocale());
        sendEmail(recipient, subject, content, false, true, null, type);
    }

    @Async
    public void informUserAboutNewProductCreation(UserEntity user, ProductDTO productDTO, String psoUser, String ssoUsers) {
        log.debug("Sending informing email to creator {} that new unit with id: {} is created", user, productDTO.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, user);
        variables.put(PRODUCT, productDTO);
        variables.put(PSO_USER, psoUser);
        variables.put(SSO_USERS, ssoUsers);
        variables.put(VALID_FROM, dateTimeFormatter.format(productDTO.getValidFrom()));
        variables.put(VALID_TO, dateTimeFormatter.format(productDTO.getValidTo()));
        List<Object> titleVars = Collections.singletonList(productDTO.getFullName());
        sendEmailFromTemplate(recipient, "mail/product/productCreation", "email.product.creation.title", titleVars.toArray(), variables,
            applicationProperties.getMail().getBaseUrlAdmin(), PRODUCT_CREATION);
    }

    @Async
    public void informUserAboutProductEdition(UserEntity user, ProductMailDTO oldProductMailDTO, ProductMailDTO productMailDTO, Map<NotificationParam, NotificationParamValue> notificationParams) {
        log.debug("Sending informing email to creator {} that product with id: {} is updated", user, productMailDTO.getProductDTO().getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, user);
        variables.put(OLD_PRODUCT, oldProductMailDTO);
        variables.put(PRODUCT, productMailDTO);
        variables.put(VALID_FROM, dateTimeFormatter.format(productMailDTO.getProductDTO().getValidFrom()));
        variables.put(VALID_TO, dateTimeFormatter.format(productMailDTO.getProductDTO().getValidTo()));
        variables.put(EDITED_FIELDS, notificationParams);
        List<Object> titleVars = Collections.singletonList(productMailDTO.getProductDTO().getFullName());
        sendEmailFromTemplate(recipient, "mail/product/productEdition", "email.product.edition.title", titleVars.toArray(), variables,
            applicationProperties.getMail().getBaseUrlAdmin(), PRODUCT_EDITION);
    }
}
