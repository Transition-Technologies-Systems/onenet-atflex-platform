package pl.com.tt.flex.server.service.mail.export;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.google.common.collect.Maps;

import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.config.ApplicationProperties;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;

@Slf4j
@Service
public class ExportResultMailService extends MailService {

    private final String FILE_NAME = "fileName";
    private final String TITLE_MESSAGE_KEY = "email.export.title";
    private final String TEMPLATE_PATH = "mail/export/exportResultEmail";


    public ExportResultMailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine,
                                   ApplicationProperties applicationProperties, UserEmailConfigRepository userEmailConfigRepository) {
        super(jHipsterProperties, javaMailSender, messageSource, templateEngine, applicationProperties, userEmailConfigRepository);
    }

    @Async
    public void sendEmailFromTemplate(MailRecipientDTO recipient, String templateName, String titleKey, Object[] titleVariables, Map<String, Object> variables, String baseUrl, FileDTO attachment, EmailType type) {
        if (recipient.getEmail() == null) {
            log.debug("Email field is null in mailRecipientDTO: {}", recipient);
            return;
        }
        Context context = new Context(recipient.getLocale());
        context.setVariables(variables);
        context.setVariable(BASE_URL, baseUrl);
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, titleVariables, recipient.getLocale());
        sendEmail(recipient, subject, content, true, true, attachment, type);
    }

    @Async
    public void informUserAboutExportResult(UserEntity user, FileDTO file, EmailType type) {
        log.debug("Sending email with export result to user {}", user.getLogin());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(FILE_NAME, file.getFileName());
        List<Object> titleVariables = Collections.singletonList(file.getFileName());
        sendEmailFromTemplate(recipient, TEMPLATE_PATH, TITLE_MESSAGE_KEY, titleVariables.toArray(), variables, getBaseUrlForUser(user), file, type);
    }

}
