package pl.com.tt.flex.server.service.mail.offer;

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
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewEntity;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Locale.forLanguageTag;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.UNIT_ACTIVATION_REMINDER_CMVC;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.UNIT_ACTIVATION_REMINDER_DAY_AHEAD;

@Service
@Slf4j
public class AuctionOfferMailService extends MailService {
    protected static final String ENERGY_AUCTION_NAME = "energyAuctionName";
    protected static final String DELIVERY_DATE = "deliveryDate";
    protected static final String DELIVERY_INTERVAL = "deliveryInterval";
    protected static final String UNIT_NAME = "unitName";
    protected static final String PRODUCT_NAME = "productName";
    protected static final String ACCEPTED_VOLUME = "acceptedVolume";

    public AuctionOfferMailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine,
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
    public void remindUserAboutActivationOfUnitInDayAheadOffer(UserEntity user, List<UnitMinDTO> auctionOfferDersEntities, SchedulingUnitDTO schedulingUnit, AuctionOfferViewEntity auctionOfferViewEntity) {
        log.debug("Sending reminding email to user {} that unit {} must be activated", user.getLogin(), auctionOfferDersEntities);
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        Locale locale = forLanguageTag(user.getLangKey());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
        String unitIds = auctionOfferDersEntities.stream().map(UnitMinDTO::getName).collect(Collectors.joining(", "));
        String deliveryInterval = timeFormatter.format(auctionOfferViewEntity.getAcceptedDeliveryPeriodFrom()) + " - " +
            timeFormatter.format(auctionOfferViewEntity.getAcceptedDeliveryPeriodTo());
        addVariable(user, USER, variables, locale);
        if (user.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            String schedulingUnitName = schedulingUnit.getName();
            addVariable(schedulingUnitName, UNIT_NAME, variables, locale);
        } else {
            addVariable(unitIds, UNIT_NAME, variables, locale);
        }
        addVariable(auctionOfferViewEntity.getAuctionName(), ENERGY_AUCTION_NAME, variables, locale);
        addVariable(dateFormatter.format(auctionOfferViewEntity.getDeliveryPeriodFrom()), DELIVERY_DATE, variables, locale);
        addVariable(deliveryInterval, DELIVERY_INTERVAL, variables, locale);
        addVariable(auctionOfferViewEntity.getProductName(), PRODUCT_NAME, variables, locale);
        addVariable(auctionOfferViewEntity.getVolume(), ACCEPTED_VOLUME, variables, locale);
        sendEmailFromTemplate(recipient, "mail/auctionActivationReminderEmail", "email.auction.activation.title",
            null, variables, getBaseUrlForUser(user), UNIT_ACTIVATION_REMINDER_DAY_AHEAD);
        log.debug("Email was sent to {} successfully", user.getLogin());
    }

    @Async
    public void remindUserAboutActivationOfUnitInCmvcOffer(UserEntity user, List<UnitMinDTO> unitEntities, AuctionOfferViewEntity auctionOfferViewEntity) {
        log.debug("Sending reminding email to user {} that unit {} must be activated", user.getLogin(), unitEntities);
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        Locale locale = forLanguageTag(user.getLangKey());
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
        String unitIds = unitEntities.stream().map(UnitMinDTO::getName).collect(Collectors.joining(", "));
        String deliveryInterval = timeFormatter.format(auctionOfferViewEntity.getAcceptedDeliveryPeriodFrom()) + " - " +
            timeFormatter.format(auctionOfferViewEntity.getAcceptedDeliveryPeriodTo());
        addVariable(user, USER, variables, locale);
        addVariable(unitIds, UNIT_NAME, variables, locale);
        addVariable(auctionOfferViewEntity.getAuctionName(), ENERGY_AUCTION_NAME, variables, locale);
        addVariable(dateFormatter.format(auctionOfferViewEntity.getDeliveryPeriodFrom()), DELIVERY_DATE, variables, locale);
        addVariable(deliveryInterval, DELIVERY_INTERVAL, variables, locale);
        addVariable(auctionOfferViewEntity.getProductName(), PRODUCT_NAME, variables, locale);
        addVariable(auctionOfferViewEntity.getAcceptedVolume(), ACCEPTED_VOLUME, variables, locale);
        sendEmailFromTemplate(recipient, "mail/auctionActivationReminderEmail", "email.auction.activation.title",
            null, variables, getBaseUrlForUser(user), UNIT_ACTIVATION_REMINDER_CMVC);
        log.debug("Email was sent to {} successfully", user.getLogin());
    }
}
