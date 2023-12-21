package pl.com.tt.flex.server.service.mail.subportfolio;


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
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.config.ApplicationProperties;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Locale.forLanguageTag;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.SUBPORTFOLIO_CREATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.SUBPORTFOLIO_EDITION;

@Service
@Slf4j
public class SubportfolioMailService extends MailService {
    protected static final String SUBPORTFOLIO_NAME = "subportfolioName";
    protected static final String FSPA = "fspa";
    protected static final String DERS = "ders";
    protected static final String COUPLING_POINT_ID = "couplingPointId";
    protected static final String MRID = "mrid";
    protected static final String VALID_FROM = "validFrom";
    protected static final String VALID_TO = "validTo";
    protected static final String ACTIVE = "active";
    protected static final String CERTIFIED = "certified";
    protected static final String SUBPORTFOLIO_ID = "subportfolioId";

    public SubportfolioMailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine,
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
    public void informUserAboutSubportfolioCreation(UserEntity user, SubportfolioDTO subportfolioDTO) {
        log.debug("Sending informing email to creator {} that new Subportfolio with id: {} is created", user.getLogin(), subportfolioDTO.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        Locale locale = forLanguageTag(user.getLangKey());
        addVariable(user, USER, variables, locale);
        addVariable(subportfolioDTO.getId(), SUBPORTFOLIO_ID, variables, locale);
        addVariable(subportfolioDTO.getName(), SUBPORTFOLIO_NAME, variables, locale);
        addVariable(subportfolioDTO.getFspa().getCompanyName(), FSPA, variables, locale);
        String ders = fillDerVariable(subportfolioDTO, user.getLangKey());
        addVariable(ders, DERS, variables, locale);
        String couplingPointsId = fillCouplingPointVariable(subportfolioDTO, user.getLangKey());
        addVariable(couplingPointsId, COUPLING_POINT_ID, variables, locale);
        addVariable(subportfolioDTO.getMrid(), MRID, variables, locale);
        addVariable(subportfolioDTO.getValidFrom(), VALID_FROM, variables, locale);
        addVariable(subportfolioDTO.getValidTo(), VALID_TO, variables, locale);
        addVariable(subportfolioDTO.getActive(), ACTIVE, variables, locale);
        addVariable(subportfolioDTO.isCertified(), CERTIFIED, variables, locale);
        List<Object> titleVariables = Collections.singletonList(subportfolioDTO.getName());
        sendEmailFromTemplate(recipient, "mail/subportfolio/subportfolioCreationEmail",
            "email.subportfolio.creation.title", titleVariables.toArray(), variables, getBaseUrlForUser(user), SUBPORTFOLIO_CREATION);
    }

    @Async
    public void informUserAboutSubportfolioEdition(UserEntity user, SubportfolioDTO modifySubportfolio, SubportfolioDTO oldSubportfolio) {
        log.debug("Sending informing email to creator {} that Subportfolio with id: {} is modified", user, modifySubportfolio.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, user);
        variables.put(SUBPORTFOLIO_ID, modifySubportfolio.getId());
        variables.put(SUBPORTFOLIO_NAME, modifySubportfolio.getName());
        Locale locale = forLanguageTag(user.getLangKey());
        addVariableIfModified(oldSubportfolio.getFspa().getCompanyName(), modifySubportfolio.getFspa().getCompanyName(), FSPA, variables, locale);
        // gdy lista z DERami po edycji nie zawiera tych samych elemetnow co przed modyfikacja, zostaje dodany parametr z DERami
        if (!CollectionUtils.isEqualCollection(oldSubportfolio.getUnits().stream().map(UnitMinDTO::getId).collect(Collectors.toList()),
            modifySubportfolio.getUnits().stream().map(UnitMinDTO::getId).collect(Collectors.toList()))) {
            variables.put(DERS, fillDerVariable(modifySubportfolio, user.getLangKey()));
        }
        // gdy lista z CouplingPointIDs po edycji nie zawiera tych samych elemetnow co przed modyfikacja, zostaje dodany parametr z CouplingPointIDs
        if (!CollectionUtils.isEqualCollection(oldSubportfolio.getCouplingPointIdTypes().stream().map(LocalizationTypeDTO::getId).collect(Collectors.toList()),
            modifySubportfolio.getCouplingPointIdTypes().stream().map(LocalizationTypeDTO::getId).collect(Collectors.toList()))) {
            variables.put(COUPLING_POINT_ID, fillCouplingPointVariable(modifySubportfolio, user.getLangKey()));
        }
        addVariableIfModified(oldSubportfolio.getMrid(), modifySubportfolio.getMrid(), MRID, variables, locale);
        addVariableIfModified(oldSubportfolio.getValidFrom(), modifySubportfolio.getValidFrom(), VALID_FROM, variables, locale);
        addVariableIfModified(oldSubportfolio.getValidTo(), modifySubportfolio.getValidTo(), VALID_TO, variables, locale);
        addVariableIfModified(oldSubportfolio.getActive(), modifySubportfolio.getActive(), ACTIVE, variables, locale);
        addVariableIfModified(oldSubportfolio.isCertified(), modifySubportfolio.isCertified(), CERTIFIED, variables, locale);
        List<Object> titleVariables = Collections.singletonList(modifySubportfolio.getName());
        sendEmailFromTemplate(recipient, "mail/subportfolio/subportfolioEditionEmail",
            "email.subportfolio.edition.title", titleVariables.toArray(), variables, getBaseUrlForUser(user), SUBPORTFOLIO_EDITION);
    }

    private String fillDerVariable(SubportfolioDTO subportfolioDTO, String langKey) {
        if (langKey.equals("pl") && subportfolioDTO.getUnits().size() == 0) {
            return "Brak";
        } else if (langKey.equals("en") && subportfolioDTO.getUnits().size() == 0) {
            return "None";
        }
        return subportfolioDTO.getUnits().stream().map(UnitMinDTO::getName).collect(Collectors.joining(","));
    }

    private String fillCouplingPointVariable(SubportfolioDTO subportfolioDTO, String langKey) {
        if (langKey.equals("pl") && subportfolioDTO.getCouplingPointIdTypes().size() == 0) {
            return "Brak";
        } else if (langKey.equals("en") && subportfolioDTO.getCouplingPointIdTypes().size() == 0) {
            return "None";
        }
        return subportfolioDTO.getCouplingPointIdTypes().stream().map(LocalizationTypeDTO::getName).collect(Collectors.joining(", "));
    }
}
