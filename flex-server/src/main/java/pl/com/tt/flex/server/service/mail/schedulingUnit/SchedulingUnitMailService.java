package pl.com.tt.flex.server.service.mail.schedulingUnit;

import com.google.common.collect.Maps;
import io.github.jhipster.config.JHipsterProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
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
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalStatus;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalType;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalDTO;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Locale.forLanguageTag;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.SCHEDULING_UNIT_ASSIGNMENT_TO_REGISTER;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.SCHEDULING_UNIT_CREATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.SCHEDULING_UNIT_EDITION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.SCHEDULING_UNIT_JOINING_INVIATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.SCHEDULING_UNIT_JOINING_PROPOSAL;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.SCHEDULING_UNIT_READY_FOR_TESTS_NOTIFICATION;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.SCHEDULING_UNIT_STATUS_CHANGE;

@Service
@Slf4j
public class SchedulingUnitMailService extends MailService {

    protected static final String PROPOSAL = "proposal";
    protected static final String SCHEDULING_UNIT = "schedulingUnit";
    protected static final String SCHEDULING_UNIT_NAME = "schedulingUnitName";
    protected static final String SCHEDULING_UNIT_ID = "schedulingUnitId";
    protected static final String BSP = "bsp";
    protected static final String BSP_NAME = "bspName";
    protected static final String TSO_AND_TA_NAME = "tsoAndTaName";
    protected static final String TYPE = "type";
    protected static final String COUPLING_POINTS_ID = "couplingPointsId";
    protected static final String PRIMARY_CP = "primaryCP";
    protected static final String NUMBER_OF_DERS = "numberOfDers";
    protected static final String ACTIVE = "active";
    protected static final String READY_FOR_TESTS = "readyForTests";
    protected static final String CERTIFIED = "certified";

    public SchedulingUnitMailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine,
                                     ApplicationProperties applicationProperties, UserEmailConfigRepository userEmailConfigRepository) {
        super(jHipsterProperties, javaMailSender, messageSource, templateEngine, applicationProperties, userEmailConfigRepository);
    }

    @Async
    public void sendEmailFromTemplate(MailRecipientDTO recipient, TemplateNameAndTitleKey templateNameAndTitleKey, String baseUrl, EmailType type) {
        if (recipient.getEmail() == null) {
            log.debug("Email field is null in mailRecipientDTO: {}", recipient);
            return;
        }
        Context context = new Context(recipient.getLocale());
        context.setVariables(templateNameAndTitleKey.getTemplateVariables());
        context.setVariable(BASE_URL, baseUrl);
        String content = templateEngine.process(templateNameAndTitleKey.getTemplateName(), context);
        String subject = messageSource.getMessage(templateNameAndTitleKey.getTitleKey(), templateNameAndTitleKey.getTitleVariables(), recipient.getLocale());
        sendEmail(recipient, subject, content, false, true, null, type);
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

    /**
     * Link for BSP User owner with a proposal to attach a FSP/FSPA User Unit to BSP Scheduling Unit.
     */
    @Async
    public void sendSchedulingUnitProposalLinkForBsp(UserEntity ownerOfSchedulingUnit, SchedulingUnitProposalDTO proposal) {
        log.debug("Sending sendSchedulingUnitProposalLink to BSP");
        MailRecipientDTO recipient = new MailRecipientDTO(ownerOfSchedulingUnit);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(PROPOSAL, proposal);
        String title = proposal.getProposalType().equals(SchedulingUnitProposalType.INVITATION) ?
            "email.schedulingUnit.proposal.toFsp.new.title" : "email.schedulingUnit.proposal.toBsp.new.title";
        TemplateNameAndTitleKey templateNameAndTitleKey = new TemplateNameAndTitleKey("mail/schedulingUnit/toBsp/unitProposalLink",
            title, variables, null);
        sendEmailFromTemplate(recipient, templateNameAndTitleKey, applicationProperties.getMail().getBaseUrlUser(), SCHEDULING_UNIT_JOINING_PROPOSAL);
    }

    /**
     * Link for FSP/FSPA User owner with a invitation to attach a FSP/FSPA User Unit to BSP Scheduling Unit.
     */
    @Async
    public void sendSchedulingUnitProposalLinkForFsp(UserEntity ownerOfUnit, SchedulingUnitProposalDTO proposal) {
        log.debug("Sending sendSchedulingUnitProposalLink to FSP");
        MailRecipientDTO recipient = new MailRecipientDTO(ownerOfUnit);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(PROPOSAL, proposal);
        TemplateNameAndTitleKey templateNameAndTitleKey = new TemplateNameAndTitleKey("mail/schedulingUnit/toFsp/unitProposalLink",
            "email.schedulingUnit.proposal.toFsp.new.title", variables, null);
        sendEmailFromTemplate(recipient, templateNameAndTitleKey, applicationProperties.getMail().getBaseUrlUser(), SCHEDULING_UNIT_JOINING_INVIATION);
    }

    @Async
    public void sendSchedulingUnitReadyForTestsLink(UserEntity user, SchedulingUnitDTO schedulingUnitDTO) {
        log.debug("Sending sendSchedulingUnitReadyForTestsLink to all TAs and all TSOs");
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(SCHEDULING_UNIT_ID, schedulingUnitDTO.getId());
        variables.put(SCHEDULING_UNIT_NAME, schedulingUnitDTO.getName());
        variables.put(BSP_NAME, schedulingUnitDTO.getBsp().getCompanyName());
        variables.put(TSO_AND_TA_NAME, user.getFirstName() + " " + user.getLastName());
        TemplateNameAndTitleKey templateNameAndTitleKey = new TemplateNameAndTitleKey("mail/schedulingUnit/tests/unitTestsLink",
            "email.schedulingUnit.readyForTests.title", variables, null);
        sendEmailFromTemplate(recipient, templateNameAndTitleKey, applicationProperties.getMail().getBaseUrlAdmin(), SCHEDULING_UNIT_READY_FOR_TESTS_NOTIFICATION);
    }

    @Async
    public void notifyUserThatProposalStatusIsChanged(SchedulingUnitProposalDTO proposalDTO, UserEntity userToNotify) {
        log.debug("Sending mail that proposal status is changed [proposalId - {}, status - {}, user - {}", proposalDTO.getId(), proposalDTO.getStatus(), userToNotify.getLogin());
        MailRecipientDTO recipient = new MailRecipientDTO(userToNotify);
        TemplateNameAndTitleKey templateNameAndTitleKey = fillTemplateNameAndTitleKey(proposalDTO);
        sendEmailFromTemplate(recipient, templateNameAndTitleKey, applicationProperties.getMail().getBaseUrlUser(), SCHEDULING_UNIT_STATUS_CHANGE);
    }

    private TemplateNameAndTitleKey fillTemplateNameAndTitleKey(SchedulingUnitProposalDTO proposalDTO) {
        Map<String, Object> templateVariables = Maps.newHashMap();
        templateVariables.put(PROPOSAL, proposalDTO);
        if (proposalDTO.getProposalType().equals(SchedulingUnitProposalType.REQUEST)) {
            if (proposalDTO.getStatus().equals(SchedulingUnitProposalStatus.ACCEPTED)) {
                List<Object> titleVariables = Arrays.asList(proposalDTO.getDetails().getBspName(), proposalDTO.getId());
                return new TemplateNameAndTitleKey("mail/schedulingUnit/toFsp/unitProposalAccepted", "email.schedulingUnit.proposal.toFsp.accepted.title",
                    templateVariables, titleVariables.toArray());
            }
            if (proposalDTO.getStatus().equals(SchedulingUnitProposalStatus.CANCELLED)) {
                List<Object> titleVariables = Arrays.asList(proposalDTO.getDetails().getFspName(), proposalDTO.getId());
                return new TemplateNameAndTitleKey("mail/schedulingUnit/toBsp/unitProposalCancelled", "email.schedulingUnit.proposal.toBsp.cancelled.title",
                    templateVariables, titleVariables.toArray());
            }
            if (proposalDTO.getStatus().equals(SchedulingUnitProposalStatus.REJECTED)) {
                List<Object> titleVariables = Arrays.asList(proposalDTO.getDetails().getBspName(), proposalDTO.getId());
                return new TemplateNameAndTitleKey("mail/schedulingUnit/toFsp/unitProposalRejected", "email.schedulingUnit.proposal.toFsp.rejected.title",
                    templateVariables, titleVariables.toArray());
            }
        }
        if (proposalDTO.getProposalType().equals(SchedulingUnitProposalType.INVITATION)) {
            if (proposalDTO.getStatus().equals(SchedulingUnitProposalStatus.ACCEPTED)) {
                List<Object> titleVariables = Arrays.asList(proposalDTO.getDetails().getFspName(), proposalDTO.getId());
                return new TemplateNameAndTitleKey("mail/schedulingUnit/toBsp/unitProposalAccepted", "email.schedulingUnit.proposal.toBsp.accepted.title",
                    templateVariables, titleVariables.toArray());
            }
            if (proposalDTO.getStatus().equals(SchedulingUnitProposalStatus.CANCELLED)) {
                List<Object> titleVariables = Arrays.asList(proposalDTO.getDetails().getBspName(), proposalDTO.getId());
                return new TemplateNameAndTitleKey("mail/schedulingUnit/toFsp/unitProposalCancelled", "email.schedulingUnit.proposal.toFsp.cancelled.title",
                    templateVariables, titleVariables.toArray());
            }
            if (proposalDTO.getStatus().equals(SchedulingUnitProposalStatus.REJECTED)) {
                List<Object> titleVariables = Arrays.asList(proposalDTO.getDetails().getFspName(), proposalDTO.getId());
                return new TemplateNameAndTitleKey("mail/schedulingUnit/toBsp/unitProposalRejected", "email.schedulingUnit.proposal.toBsp.rejected.title",
                    templateVariables, titleVariables.toArray());
            }
        }
        throw new IllegalArgumentException("Cannot find templateName and titleKey for SchedulingUnitProposal " +
            "[type - " + proposalDTO.getStatus() + " , status - " + proposalDTO.getStatus() + "]");
    }

    @Async
    public void informUserAboutTransferOfSuToFlexRegister(UserEntity user, SchedulingUnitDTO schedulingUnitDTO, FspDTO bsp) {
        log.debug("Sending informing email to creator {} that Scheduling Unit with id: {} is moved to Flex Register", user, schedulingUnitDTO.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        Locale locale = forLanguageTag(user.getLangKey());
        addVariable(schedulingUnitDTO.getId(), SCHEDULING_UNIT_ID, variables, locale);
        addVariable(user, USER, variables, locale);
        addVariable(bsp.getCompanyName(), BSP_NAME, variables, locale);
        addVariable(schedulingUnitDTO.getSchedulingUnitType(), TYPE, variables, locale);
        String couplingPointsId = CollectionUtils.emptyIfNull(schedulingUnitDTO.getCouplingPoints()).stream()
            .map(LocalizationTypeDTO::getName).collect(Collectors.joining(", "));
        addVariable(couplingPointsId, COUPLING_POINTS_ID, variables, locale);
        addVariable(schedulingUnitDTO.getNumberOfDers(), NUMBER_OF_DERS, variables, locale);
        addVariable(schedulingUnitDTO.getActive(), ACTIVE, variables, locale);
        if (Objects.nonNull(schedulingUnitDTO.getPrimaryCouplingPoint())) {
            variables.put(PRIMARY_CP, schedulingUnitDTO.getPrimaryCouplingPoint().getName());
        }
        List<Object> titleVariables = Collections.singletonList(schedulingUnitDTO.getId());
        sendEmailFromTemplate(recipient, "mail/flexRegister/schedulingUnitMovedToFlexRegister",
            "email.flexRegister.schedulingUnit.title", titleVariables.toArray(), variables, getBaseUrlForUser(user), SCHEDULING_UNIT_ASSIGNMENT_TO_REGISTER);
    }

    @Async
    public void informUserAboutNewSchedulingUnitCreation(UserEntity user, SchedulingUnitDTO schedulingUnit, FspDTO bsp) {
        log.debug("Sending informing email to {} that new SchedulingUnit with id: {} is created", user.getLogin(), schedulingUnit.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Locale locale = forLanguageTag(user.getLangKey());
        Map<String, Object> variables = Maps.newHashMap();
        addVariable(user, USER, variables, locale);
        addVariable(schedulingUnit.getId(), SCHEDULING_UNIT_ID, variables, locale);
        addVariable(schedulingUnit.getName(), SCHEDULING_UNIT_NAME, variables, locale);
        addVariable(bsp.getCompanyName(), BSP, variables, locale);
        addVariable(schedulingUnit.getSchedulingUnitType(), TYPE, variables, locale);
        addVariable(schedulingUnit.getActive(), ACTIVE, variables, locale);
        addVariable(schedulingUnit.isReadyForTests(), READY_FOR_TESTS, variables, locale);
        addVariable(schedulingUnit.isCertified(), CERTIFIED, variables, locale);
        List<Object> titleVariables = Collections.singletonList(schedulingUnit.getId());
        sendEmailFromTemplate(recipient, "mail/schedulingUnit/schedulingUnitCreationEmail",
            "email.schedulingUnit.creation.title", titleVariables.toArray(), variables, getBaseUrlForUser(user), SCHEDULING_UNIT_CREATION);
    }

    @Async
    public void informUserAboutSchedulingUnitEdition(UserEntity user, SchedulingUnitDTO oldSchedulingUnit, SchedulingUnitDTO modifySchedulingUnit) {
        log.debug("Sending informing that Scheduling unit with id: {} is modified", modifySchedulingUnit.getId());
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        variables.put(USER, user);
        variables.put(SCHEDULING_UNIT_ID, modifySchedulingUnit.getId());
        variables.put(SCHEDULING_UNIT_NAME, modifySchedulingUnit.getName());
        Locale locale = forLanguageTag(user.getLangKey());
        addVariableIfModified(oldSchedulingUnit.getBsp().getCompanyName(), modifySchedulingUnit.getBsp().getCompanyName(), BSP, variables, locale);
        addVariableIfModified(oldSchedulingUnit.getSchedulingUnitType(), modifySchedulingUnit.getSchedulingUnitType(), TYPE, variables, locale);
        addVariableIfModified(oldSchedulingUnit.getActive(), modifySchedulingUnit.getActive(), ACTIVE, variables, locale);
        addVariableIfModified(oldSchedulingUnit.isReadyForTests(), modifySchedulingUnit.isReadyForTests(), READY_FOR_TESTS, variables, locale);
        addVariableIfModified(oldSchedulingUnit.isCertified(), modifySchedulingUnit.isCertified(), CERTIFIED, variables, locale);
        List<Object> titleVariables = Collections.singletonList(modifySchedulingUnit.getName());
        sendEmailFromTemplate(recipient, "mail/schedulingUnit/schedulingUnitEditionEmail",
            "email.schedulingUnit.edition.title", titleVariables.toArray(), variables, getBaseUrlForUser(user), SCHEDULING_UNIT_EDITION);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private static class TemplateNameAndTitleKey {
        private String templateName;
        private String titleKey;
        Map<String, Object> templateVariables;
        private Object[] titleVariables;
    }
}
