package pl.com.tt.flex.server.service.mail.unit;

import static java.util.Locale.forLanguageTag;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.UNIT_CERTIFICATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.UNIT_CERTIFICATION_LOSS;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.UNIT_CREATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.UNIT_EDITION;

import com.google.common.collect.Maps;
import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.slf4j.Slf4j;
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
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UnitMailService extends MailService {

    protected static final String UNIT = "unit";
    protected static final String DER_TYPE_RECEPTION = "derTypeReception";
    protected static final String DER_TYPE_ENERGY_STORAGE = "derTypeEnergyStorage";
    protected static final String DER_TYPE_GENERATION = "derTypeGeneration";
    protected static final String VALID_FROM = "validFrom";
    protected static final String COUPLING_POINT_IDS = "couplingPointIds";
    protected static final String POWER_STATION_TYPES = "powerStationTypes";
    protected static final String VALID_TO = "validTo";
    protected static final String DIRECTION_OF_DEVIATION = "directionOfDeviation";
    protected static final String ACTIVE = "active";
    protected static final String AGGREGATED = "aggregated";
    protected static final String CERTIFIED = "certified";
    protected static final String COMPANY_NAME = "companyName";
    protected static final String CODE = "code";
    protected static final String SOURCE_POWER = "sourcePower";
    protected static final String CONNECTION_POWER = "connectionPower";
    protected static final String PPE = "ppe";
    protected static final String MRID_TSO = "mridTso";
    protected static final String MRID_DSO = "mridDso";
    protected static final String P_MIN = "pMin";
    protected static final String Q_MIN = "qMin";
    protected static final String Q_MAX = "qMax";
    protected static final String POINT_OF_CONNECTION_WITH_LV = "pointOfConnectionWithLvTypes";

    public UnitMailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine,
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

    /**
     * Information for all FSP that new Unit is created by DSO/TSO user.
     */
    @Async
    public void informFspAboutNewUnitCreation(UserEntity user, UnitDTO unitDTO) {
        log.debug("Sending informing email to FSP {} that new unit with id: {} is created", user, unitDTO.getId());
        Locale locale = forLanguageTag(user.getLangKey());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        addVariable(user, USER, variables, locale);
        addVariable(unitDTO, UNIT, variables, locale);
        addVariable(unitDTO.getValidFrom(), VALID_FROM, variables, locale);
        addVariable(unitDTO.getValidTo(), VALID_TO, variables, locale);
        addVariable(getCouplingPontIdStringList(unitDTO), COUPLING_POINT_IDS, variables, locale);
        addVariable(getPowerStationTypesStringList(unitDTO), POWER_STATION_TYPES, variables, locale);
        addVariable(unitDTO.getDirectionOfDeviation(), DIRECTION_OF_DEVIATION, variables, locale);
        addVariable(unitDTO.isActive(), ACTIVE, variables, locale);
        addVariable(unitDTO.isAggregated(), AGGREGATED, variables, locale);
        addVariable(unitDTO.isCertified(), CERTIFIED, variables, locale);
        addVariable(getPointOfConnectionWithLvTypes(unitDTO), POINT_OF_CONNECTION_WITH_LV, variables, locale);
        setDerTypesDescriptionByUserLangKey(user, unitDTO, variables);
        List<Object> titleVars = Collections.singletonList(unitDTO.getName());
        sendEmailFromTemplate(recipient, "mail/unit/toFsp/unitCreationEmail", "email.unit.creation.title", titleVars.toArray(), variables,
            applicationProperties.getMail().getBaseUrlAdmin(), UNIT_CREATION);
    }

    /**
     * Information for all FSP that unit was updated by admin.
     */
    @Async
    public void informFspAboutUnitModification(UserEntity user, UnitDTO oldUnitDTO, UnitDTO unitDTO) {
        log.debug("Sending informing email to FSP {} that new unit with id: {} is updated", user, unitDTO.getId());
        Locale locale = forLanguageTag(user.getLangKey());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        addVariable(user, USER, variables, locale);
        addVariable(unitDTO, UNIT, variables, locale);
        addVariableIfModified(getCompanyNameOrNull(oldUnitDTO), getCompanyNameOrNull(unitDTO), COMPANY_NAME, variables, locale);
        addVariableIfModified(oldUnitDTO.getCode(), unitDTO.getCode(), CODE, variables, locale);
        addVariableIfModified(oldUnitDTO.getSourcePower(), unitDTO.getSourcePower(), SOURCE_POWER, variables, locale);
        addVariableIfModified(oldUnitDTO.getConnectionPower(), unitDTO.getConnectionPower(), CONNECTION_POWER, variables, locale);
        addVariableIfModified(oldUnitDTO.getDirectionOfDeviation(), unitDTO.getDirectionOfDeviation(), DIRECTION_OF_DEVIATION, variables, locale);
        addVariableIfModified(oldUnitDTO.getDerTypeReception(), unitDTO.getDerTypeReception(), DER_TYPE_RECEPTION, variables, locale);
        addVariableIfModified(oldUnitDTO.getDerTypeGeneration(), unitDTO.getDerTypeGeneration(), DER_TYPE_GENERATION, variables, locale);
        addVariableIfModified(oldUnitDTO.getDerTypeEnergyStorage(), unitDTO.getDerTypeEnergyStorage(), DER_TYPE_ENERGY_STORAGE, variables, locale);
        addVariableIfModified(oldUnitDTO.getPpe(), unitDTO.getPpe(), PPE, variables, locale);
        addVariableIfModified(oldUnitDTO.getMridTso(), unitDTO.getMridTso(), MRID_TSO, variables, locale);
        addVariableIfModified(oldUnitDTO.getMridDso(), unitDTO.getMridDso(), MRID_DSO, variables, locale);
        addVariableIfModified(oldUnitDTO.getPMin(), unitDTO.getPMin(), P_MIN, variables, locale);
        addVariableIfModified(oldUnitDTO.getQMin(), unitDTO.getQMin(), Q_MIN, variables, locale);
        addVariableIfModified(oldUnitDTO.getQMax(), unitDTO.getQMax(), Q_MAX, variables, locale);
        addVariableIfModified(oldUnitDTO.getValidFrom(), unitDTO.getValidFrom(), VALID_FROM, variables, locale);
        addVariableIfModified(oldUnitDTO.getValidTo(), unitDTO.getValidTo(), VALID_TO, variables, locale);
        addVariableIfModified(getCouplingPontIdStringList(oldUnitDTO), getCouplingPontIdStringList(unitDTO), COUPLING_POINT_IDS, variables, locale);
        addVariableIfModified(getPowerStationTypesStringList(oldUnitDTO), getPowerStationTypesStringList(unitDTO), POWER_STATION_TYPES, variables, locale);
        addVariableIfModified(oldUnitDTO.isActive(), unitDTO.isActive(), ACTIVE, variables, locale);
        addVariableIfModified(oldUnitDTO.isAggregated(), unitDTO.isAggregated(), AGGREGATED, variables, locale);
        addVariableIfModified(oldUnitDTO.isCertified(), unitDTO.isCertified(), CERTIFIED, variables, locale);
        addVariableIfModified(getPointOfConnectionWithLvTypes(oldUnitDTO), getPointOfConnectionWithLvTypes(unitDTO), POINT_OF_CONNECTION_WITH_LV, variables, locale);
        List<Object> titleVars = Collections.singletonList(unitDTO.getName());
        sendEmailFromTemplate(recipient, "mail/unit/toFsp/unitEditionEmail", "email.unit.edition.title", titleVars.toArray(), variables,
            applicationProperties.getMail().getBaseUrlUser(), UNIT_EDITION);
    }

    /**
     * Information for all FSP that Unit is certified.
     */
    @Async
    public void informFspAboutUnitCertified(UserEntity user, UnitDTO unitDTO) {
        log.debug("Sending informing email to FSP {} that unit with id: {} is certified", user, unitDTO.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, user);
        variables.put(UNIT, unitDTO);
        setDerTypesDescriptionByUserLangKey(user, unitDTO, variables);
        sendEmailFromTemplate(recipient, "mail/unit/toFsp/unitHasBeenCertifiedEmail", "email.unit.fsp.hasBeenCertified.title", null, variables,
            applicationProperties.getMail().getBaseUrlAdmin(), UNIT_CERTIFICATION);
    }

    /**
     * Information for all FSP that Unit lost certification.
     */
    @Async
    public void informFspAboutUnitLostCertification(UserEntity user, UnitDTO unitDTO) {
        log.debug("Sending informing email to FSP {} that unit with id: {} lost certification", user, unitDTO.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, user);
        variables.put(UNIT, unitDTO);
        setDerTypesDescriptionByUserLangKey(user, unitDTO, variables);
        sendEmailFromTemplate(recipient, "mail/unit/toFsp/unitLostCertificationEmail", "email.unit.fsp.lostCertification.title", null, variables,
            applicationProperties.getMail().getBaseUrlAdmin(), UNIT_CERTIFICATION_LOSS);
    }

    private String getCompanyNameOrNull(UnitDTO unit) {
        return Optional.of(unit)
            .map(UnitDTO::getFsp)
            .map(FspDTO::getCompanyName)
            .orElse(null);
    }

    private void setDerTypesDescriptionByUserLangKey(UserEntity user, UnitDTO unitDTO, Map<String, Object> variables) {
        setDerTypesDescription(user, unitDTO.getDerTypeReception(), DER_TYPE_RECEPTION, variables);
        setDerTypesDescription(user, unitDTO.getDerTypeEnergyStorage(), DER_TYPE_ENERGY_STORAGE, variables);
        setDerTypesDescription(user, unitDTO.getDerTypeGeneration(), DER_TYPE_GENERATION, variables);
    }

    private void setDerTypesDescription(UserEntity user, DerTypeMinDTO derType, String derTypeVariableConstant, Map<String, Object> variables) {
        if (Objects.nonNull(derType)) {
            if (user.getLangKey().equals("pl") && Objects.nonNull(derType.getDescriptionPl())) {
                variables.put(derTypeVariableConstant, derType.getDescriptionPl());
            } else {
                variables.put(derTypeVariableConstant, derType.getDescriptionEn());
            }
        } else {
            variables.put(derTypeVariableConstant, "");
        }
    }

    private String getCouplingPontIdStringList(UnitDTO unitDTO) {
        return unitDTO.getCouplingPointIdTypes().stream().map(LocalizationTypeDTO::getName).collect(Collectors.joining(", "));
    }

    private String getPowerStationTypesStringList(UnitDTO unitDTO) {
        return unitDTO.getPowerStationTypes().stream().map(LocalizationTypeDTO::getName).collect(Collectors.joining(", "));
    }

    private String getPointOfConnectionWithLvTypes(UnitDTO unitDTO) {
        return unitDTO.getPointOfConnectionWithLvTypes().stream().map(LocalizationTypeDTO::getName).collect(Collectors.joining(", "));
    }
}
