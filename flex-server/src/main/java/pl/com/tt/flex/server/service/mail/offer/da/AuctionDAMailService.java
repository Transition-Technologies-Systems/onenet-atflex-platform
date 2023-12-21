package pl.com.tt.flex.server.service.mail.offer.da;

import com.google.common.collect.Maps;
import io.github.jhipster.config.JHipsterProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.server.config.ApplicationProperties;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Locale.forLanguageTag;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.CAPACITY_OFFER_ACCEPTED;

@Service
@Slf4j

public class AuctionDAMailService extends MailService {
    protected static final String CAPACITY_AUCTION_NAME = "capacityAuctionName";
    protected static final String OFFER_IDS = "offerIds";
    protected static final String ENERGY_AUCTION_ID = "energyAuctionId";
    protected static final String ENERGY_AUCTION_NAME = "energyAuctionName";
    protected static final String ENERGY_AUCTION_GATE_OPENING_TIME = "energyAuctionGateOpeningTime";
    protected static final String DELIVERY_DATE = "deliveryDate";
    protected static final String PRODUCT_NAME = "productName";

    public AuctionDAMailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender, MessageSource messageSource, SpringTemplateEngine templateEngine,
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
    public void informUserAboutAcceptedCapacityOffer(UserEntity user, List<AuctionDayAheadOfferEntity> acceptedOffers, AuctionDayAheadDTO capacityAuction, AuctionDayAheadDTO energyAuction) {
        log.debug("Sending informing email to user {} that accepted Capacity Offers: {} is created", user.getLogin(), acceptedOffers);
        MailRecipientDTO recipient = new MailRecipientDTO(user);
        Map<String, Object> variables = Maps.newHashMap();
        Locale locale = forLanguageTag(user.getLangKey());
        addVariable(user, USER, variables, locale);
        addVariable(capacityAuction.getName(), CAPACITY_AUCTION_NAME, variables, locale);
        String offersId = acceptedOffers.stream().map(AuctionDayAheadOfferEntity::getId).map(Object::toString).collect(Collectors.joining(", "));
        addVariable(offersId, OFFER_IDS, variables, locale);
        addVariable(energyAuction.getName(), ENERGY_AUCTION_NAME, variables, locale);
        addVariable(energyAuction.getId(), ENERGY_AUCTION_ID, variables, locale);
        addVariable(energyAuction.getDeliveryDate(), DELIVERY_DATE, variables, locale);
        addVariable(energyAuction.getEnergyGateOpeningTime(), ENERGY_AUCTION_GATE_OPENING_TIME, variables, locale);

        List<Object> titleVariables = Collections.singletonList(capacityAuction.getName());
        sendEmailFromTemplate(recipient, "mail/auctionDayAhead/auctionDayAheadAcceptedCapacityOffer",
            "email.dayAhead.offer.capacity.accepted.title", titleVariables.toArray(), variables, getBaseUrlForUser(user), CAPACITY_OFFER_ACCEPTED);
    }
}
