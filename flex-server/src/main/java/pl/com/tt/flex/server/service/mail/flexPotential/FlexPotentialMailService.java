package pl.com.tt.flex.server.service.mail.flexPotential;

import com.google.common.collect.Maps;
import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import pl.com.tt.flex.server.config.ApplicationProperties;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Locale.forLanguageTag;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.FLEXIBILITY_POTENTIAL_ASSIGNMENT_TO_REGISTER;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.FLEXIBILITY_POTENTIAL_CREATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.FLEXIBILITY_POTENTIAL_EDITION;

@Service
@Slf4j
public class FlexPotentialMailService extends MailService {
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
    protected static final String FLEX_POTENTIAL = "flexPotential";
    protected static final String ID = "fpId";
    protected static final String PRODUCT = "product";
    protected static final String FSP = "fsp";
    protected static final String FSP_NAME = "fspName";
    protected static final String DERS = "ders";
    protected static final String VOLUME = "volume";
    protected static final String VOLUME_UNIT = "volumeUnit";
    protected static final String DIVISIBLE = "divisible";
    protected static final String FULL_ACTIVATION_TIME = "fullActivationTime";
    protected static final String MIN_DELIVERY_DURATION = "minDeliveryDuration";
    protected static final String VALID_FROM = "validFrom";
    protected static final String VALID_TO = "validTo";
    protected static final String ACTIVE = "active";
    protected static final String PRODUCT_PREQ = "productPrequalification";
    protected static final String STATIC_GRID_PREQ = "staticGridPrequalification";

    public FlexPotentialMailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine,
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
    public void informUserAboutNewFlexPotentialCreation(UserEntity user, FlexPotentialDTO flexPotential, FspDTO fsp) {
        log.debug("Sending informing email to creator {} that new Flex Potential with id: {} is created", user.getLogin(), flexPotential.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, user);
        variables.put(FLEX_POTENTIAL, flexPotential);
        variables.put(FSP, fsp);
        variables.put(DERS, CollectionUtils.emptyIfNull(flexPotential.getUnits()).stream().map(UnitMinDTO::getName).collect(Collectors.joining(",")));
        variables.put(VALID_FROM, dateTimeFormatter.format(flexPotential.getValidFrom()));
        variables.put(VALID_TO, dateTimeFormatter.format(flexPotential.getValidTo()));
        List<Object> titleVariables = Collections.singletonList(flexPotential.getId());
        sendEmailFromTemplate(recipient, "mail/flexPotential/flexPotentialCreationEmail",
            "email.flexPotential.creation.title", titleVariables.toArray(), variables, getBaseUrlForUser(user), FLEXIBILITY_POTENTIAL_CREATION);
    }

    @Async
    public void informUserAboutNewFlexPotentialEdition(UserEntity user, FlexPotentialDTO oldFlexPotentialDTO, FlexPotentialDTO flexPotentialDTO) {
        log.debug("Sending informing email to creator {} that Flex Potential with id: {} is modified", user, flexPotentialDTO.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, user);
        variables.put(ID, flexPotentialDTO.getId());
        Locale locale = forLanguageTag(user.getLangKey());
        addVariableIfModified(oldFlexPotentialDTO.getProduct().getShortName(), flexPotentialDTO.getProduct().getShortName(), PRODUCT, variables, locale);
        addVariableIfModified(oldFlexPotentialDTO.getFsp().getCompanyName(), flexPotentialDTO.getFsp().getCompanyName(), FSP_NAME, variables, locale);
        // gdy lista z DERami po edycji nie zawiera tych samych elemetnow co przed modyfikacja, zostaje dodany parametr z DERami
        if (!CollectionUtils.isEqualCollection(oldFlexPotentialDTO.getUnits().stream().map(UnitMinDTO::getId).collect(Collectors.toList()),
            flexPotentialDTO.getUnits().stream().map(UnitMinDTO::getId).collect(Collectors.toList()))) {
            variables.put(DERS, CollectionUtils.emptyIfNull(flexPotentialDTO.getUnits()).stream().map(UnitMinDTO::getName).collect(Collectors.joining(",")));
        }
        addVariableIfModified(oldFlexPotentialDTO.getVolume(), flexPotentialDTO.getVolume(), VOLUME, variables, locale);
        addVariableIfModified(oldFlexPotentialDTO.getVolumeUnit(), flexPotentialDTO.getVolumeUnit(), VOLUME_UNIT, variables, locale);
        addVariableIfModified(oldFlexPotentialDTO.isDivisibility(), flexPotentialDTO.isDivisibility(), DIVISIBLE, variables, locale);
        addVariableIfModified(oldFlexPotentialDTO.getFullActivationTime(), flexPotentialDTO.getFullActivationTime(), FULL_ACTIVATION_TIME, variables, locale);
        addVariableIfModified(oldFlexPotentialDTO.getMinDeliveryDuration(), flexPotentialDTO.getMinDeliveryDuration(), MIN_DELIVERY_DURATION, variables, locale);
        addVariableIfModified(oldFlexPotentialDTO.getValidFrom(), flexPotentialDTO.getValidFrom(), VALID_FROM, variables, locale);
        addVariableIfModified(oldFlexPotentialDTO.getValidTo(), flexPotentialDTO.getValidTo(), VALID_TO, variables, locale);
        addVariableIfModified(oldFlexPotentialDTO.isActive(), flexPotentialDTO.isActive(), ACTIVE, variables, locale);
        addVariableIfModified(oldFlexPotentialDTO.isProductPrequalification(), flexPotentialDTO.isProductPrequalification(), PRODUCT_PREQ, variables, locale);
        addVariableIfModified(oldFlexPotentialDTO.isStaticGridPrequalification(), flexPotentialDTO.isStaticGridPrequalification(), STATIC_GRID_PREQ, variables, locale);
        List<Object> titleVariables = Collections.singletonList(flexPotentialDTO.getId());
        sendEmailFromTemplate(recipient, "mail/flexPotential/flexPotentialEditionEmail",
            "email.flexPotential.edition.title", titleVariables.toArray(), variables, getBaseUrlForUser(user), FLEXIBILITY_POTENTIAL_EDITION);
    }

    @Async
    public void informUserAboutTransferOfFlexPotentialToFlexRegister(UserEntity user, FlexPotentialDTO flexPotential, FspDTO fsp) {
        log.debug("Sending informing email to creator {} that Flex Potential with id: {} is moved to Flex Register", user, flexPotential.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, user);
        variables.put(FLEX_POTENTIAL, flexPotential);
        variables.put(FSP, fsp);
        variables.put(DERS, CollectionUtils.emptyIfNull(flexPotential.getUnits()).stream().map(UnitMinDTO::getName).collect(Collectors.joining(",")));
        variables.put(VALID_FROM, dateTimeFormatter.format(flexPotential.getValidFrom()));
        variables.put(VALID_TO, dateTimeFormatter.format(flexPotential.getValidTo()));
        List<Object> titleVariables = Collections.singletonList(flexPotential.getId());
        sendEmailFromTemplate(recipient, "mail/flexRegister/flexPotentialMovedToFlexRegister",
            "email.flexRegister.flexPotential.title", titleVariables.toArray(), variables, getBaseUrlForUser(user), FLEXIBILITY_POTENTIAL_ASSIGNMENT_TO_REGISTER);
    }
}
