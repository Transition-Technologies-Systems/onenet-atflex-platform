package pl.com.tt.flex.server.service.mail;

import io.github.jhipster.config.JHipsterProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskHolder;
import org.thymeleaf.spring5.SpringTemplateEngine;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.config.ApplicationProperties;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionCmvcOfferRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionOfferViewRepository;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.server.repository.user.config.email.UserEmailConfigRepository;
import pl.com.tt.flex.server.service.auction.scheduled.AuctionMailOfferScheduler;
import pl.com.tt.flex.server.service.mail.dto.MailRecipientDTO;
import pl.com.tt.flex.server.service.mail.offer.AuctionOfferMailService;
import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.util.AuctionCmvcDataUtil;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = FlexserverApp.class)
public class AuctionMailOfferSchedulerIT {
    @Autowired
    private JHipsterProperties jHipsterProperties;

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Spy
    private JavaMailSenderImpl javaMailSender;

    @Captor
    private ArgumentCaptor<MimeMessage> messageCaptor;

    private AuctionOfferMailService auctionOfferMailService;

    private AuctionMailOfferScheduler auctionMailOfferScheduler;

    @Autowired
    private AuctionCmvcOfferRepository auctionCmvcOfferRepository;
    @Autowired
    private AuctionOfferViewRepository auctionOfferViewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuctionOfferMailService offerMailService;
    @Autowired
    private FlexPotentialMapper flexPotentialMapper;
    @Autowired
    private UserEmailConfigRepository userEmailConfigRepository;

    @Autowired
    private ScheduledTaskHolder scheduledTaskHolder;

    private MailRecipientDTO recipient;

    private String subject;
    private String subjectWithPrefix;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        auctionOfferMailService = new AuctionOfferMailService(jHipsterProperties, javaMailSender, messageSource, templateEngine, applicationProperties, userEmailConfigRepository);
        auctionMailOfferScheduler = new AuctionMailOfferScheduler(auctionCmvcOfferRepository, auctionOfferViewRepository, userRepository, offerMailService, flexPotentialMapper);
        this.recipient = new MailRecipientDTO(1l, "UserEntity", "john.doe@example.com", Locale.ENGLISH);
        subject = "Activation reminder";
        subjectWithPrefix = applicationProperties.getMail().getSubjectPrefix() + " - " + subject;
    }

    @Test
    public void testSendActivationReminderEmailForDayAheadAuction() throws MessagingException, IOException {
        UserEntity user = new UserEntity();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        UnitMinDTO unit = new UnitMinDTO();
        unit.setName("testUnit");
        unit.setId(1L);
        List<UnitMinDTO> unitEntitySet = List.of(unit);
        AuctionOfferViewEntity auctionOfferViewEntity = new AuctionOfferViewEntity();
        auctionOfferViewEntity.setAuctionName("Test_DA");
        auctionOfferViewEntity.setProductName("testproduct");
        auctionOfferViewEntity.setDeliveryPeriodFrom(Instant.now().plus(1, ChronoUnit.DAYS));
        auctionOfferViewEntity.setDeliveryPeriodTo(Instant.now().plus(2, ChronoUnit.DAYS));
        auctionOfferViewEntity.setAcceptedDeliveryPeriodFrom(Instant.now().plus(1, ChronoUnit.DAYS));
        auctionOfferViewEntity.setAcceptedDeliveryPeriodTo(Instant.now().plus(2, ChronoUnit.DAYS));
        auctionOfferViewEntity.setAcceptedVolume("123");
        SchedulingUnitDTO schedulingUnit = new SchedulingUnitDTO();
        schedulingUnit.setName("testSu");
        auctionOfferMailService.remindUserAboutActivationOfUnitInDayAheadOffer(user, unitEntitySet, schedulingUnit, auctionOfferViewEntity);
        verify(javaMailSender).send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(user.getEmail());
        assertThat(message.getFrom()[0].toString()).isEqualTo(jHipsterProperties.getMail().getFrom());
        assertThat(message.getContent().toString()).isNotEmpty();
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    @Test
    public void testSendActivationReminderEmailForCmvcAuction() throws MessagingException, IOException {
        UserEntity user = new UserEntity();
        user.setLangKey(Constants.DEFAULT_LANGUAGE);
        user.setLogin("john");
        user.setEmail("john.doe@example.com");
        UnitMinDTO unit = new UnitMinDTO();
        unit.setName("testUnit");
        unit.setId(1L);
        List<UnitMinDTO> unitEntitySet = List.of(unit);
        AuctionOfferViewEntity auctionOfferViewEntity = new AuctionOfferViewEntity();
        auctionOfferViewEntity.setAuctionName("Test_CMVC");
        auctionOfferViewEntity.setProductName("testproduct");
        auctionOfferViewEntity.setDeliveryPeriodFrom(Instant.now().plus(1, ChronoUnit.DAYS));
        auctionOfferViewEntity.setDeliveryPeriodTo(Instant.now().plus(2, ChronoUnit.DAYS));
        auctionOfferViewEntity.setAcceptedDeliveryPeriodFrom(Instant.now().plus(1, ChronoUnit.DAYS));
        auctionOfferViewEntity.setAcceptedDeliveryPeriodTo(Instant.now().plus(2, ChronoUnit.DAYS));
        auctionOfferViewEntity.setAcceptedVolume("123");
        auctionOfferMailService.remindUserAboutActivationOfUnitInCmvcOffer(user, unitEntitySet, auctionOfferViewEntity);
        verify(javaMailSender).send(messageCaptor.capture());
        MimeMessage message = messageCaptor.getValue();
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo(user.getEmail());
        assertThat(message.getFrom()[0].toString()).isEqualTo(jHipsterProperties.getMail().getFrom());
        assertThat(message.getContent().toString()).isNotEmpty();
        assertThat(message.getDataHandler().getContentType()).isEqualTo("text/html;charset=UTF-8");
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenDifferenceBetweenTodayAndStartOfDeliveryIsOneDay() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, time.plus(1, ChronoUnit.DAYS));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenDifferenceBetweenTodayAndStartOfDeliveryIsSeventeenHours() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, time.plus(17, ChronoUnit.HOURS));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenDifferenceBetweenTodayAndStartOfDeliveryIsFifteenHours() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, time.plus(15, ChronoUnit.HOURS));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenDifferenceBetweenTodayAndStartOfDeliveryIsFortyFiveHours() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, time.plus(45, ChronoUnit.HOURS));
        assertFalse(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenItsCloseToMidnightAndDeliveryStartsAtMidnight() {
        Instant time = Instant.parse("2022-07-28T21:30:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, time.plus(30, ChronoUnit.MINUTES));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenItsCloseToMidnightAndDeliveryStartsInFewDaysAtMidnight() {
        Instant time = Instant.parse("2022-07-28T21:30:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, Instant.parse("2022-07-30T22:00:00Z"));
        assertFalse(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenSwitchingFromDSTToStandardTime() {
        Instant time = Instant.parse("2022-10-29T21:30:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, Instant.parse("2022-10-30T00:00:00Z"));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenSwitchingFromDSTToStandardTimeButWhenItsTheSameDay() {
        Instant time = Instant.parse("2022-10-29T22:00:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, Instant.parse("2022-10-30T00:00:00Z"));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenSwitchingFromStandardTimeToDST() {
        Instant time = Instant.parse("2022-03-26T22:30:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, Instant.parse("2022-03-27T00:00:00Z"));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenSwitchingFromStandardTimeToDSTButWhenItsTheSameDay() {
        Instant time = Instant.parse("2022-03-26T23:00:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, Instant.parse("2022-03-27T02:00:00Z"));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenDifferenceBetweenTodayAndStartOfDeliveryIsThreeDays() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, time.plus(3, ChronoUnit.DAYS));
        assertFalse(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenStartOfDeliveryIsToday() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, time.plus(1, ChronoUnit.HOURS));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenDeliveryStartedOneHourPrior() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, time.minus(1, ChronoUnit.HOURS));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenStartOfDeliveryWasYesterday() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, time.minus(1, ChronoUnit.DAYS));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfActivationMailIsReadyToSendWhenStartOfDeliveryWasSeveralDaysAgo() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        boolean isReadyForSendingEmail = auctionMailOfferScheduler.isReadyForSendingActivationEmail(time, time.minus(3, ChronoUnit.DAYS));
        assertTrue(isReadyForSendingEmail);
    }

    @Test
    public void testCheckIfNumberOfDaysIsThreeDays() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, time.plus(3, ChronoUnit.DAYS));
        assertEquals(3, numberOfDays);
    }

    @Test
    public void testCheckIfNumberOfDaysIsOneDay() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, time.plus(1, ChronoUnit.DAYS));
        assertEquals(1, numberOfDays);
    }

    @Test
    public void testCheckIfNumberOfDaysIsZero() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, time.plus(1, ChronoUnit.HOURS));
        assertEquals(0, numberOfDays);
    }

    @Test
    public void testCheckNumberOfDaysWhenDeliveryIsInSeventeenHours() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, time.plus(17, ChronoUnit.HOURS));
        assertEquals(1, numberOfDays);
    }

    @Test
    public void testCheckNumberOfDaysWhenDeliveryIsInFifteenHours() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, time.plus(15, ChronoUnit.HOURS));
        assertEquals(0, numberOfDays);
    }

    @Test
    public void testCheckNumberOfDaysWhenDeliveryStartedFiftyHoursAgo() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, time.minus(50, ChronoUnit.HOURS));
        assertEquals(-2, numberOfDays);
    }

    @Test
    public void testCheckNumberOfDaysWhenItsCloseToMidnightAndDeliveryStartsAtMidnight() {
        Instant time = Instant.parse("2022-07-28T21:30:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, time.plus(30, ChronoUnit.MINUTES));
        assertEquals(1, numberOfDays);
    }

    @Test
    public void testCheckNumberOfDaysWhenItsCloseToMidnightAndDeliveryStartsInFewDaysAtMidnight() {
        Instant time = Instant.parse("2022-07-28T21:30:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, Instant.parse("2022-07-30T22:00:00Z"));
        assertEquals(3, numberOfDays);
    }

    @Test
    public void testCheckNumberOfDaysWhenSwitchingFromDSTToStandardTime() {
        Instant time = Instant.parse("2022-10-29T21:30:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, Instant.parse("2022-10-30T00:00:00Z"));
        assertEquals(1, numberOfDays);
    }

    @Test
    public void testCheckNumberOfDaysWhenSwitchingFromDSTToStandardTimeButWhenItsTheSameDay() {
        Instant time = Instant.parse("2022-10-29T22:00:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, Instant.parse("2022-10-30T00:00:00Z"));
        assertEquals(0, numberOfDays);
    }

    @Test
    public void testCheckNumberOfDaysWhenSwitchingFromStandardTimeToDST() {
        Instant time = Instant.parse("2022-03-26T22:30:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, Instant.parse("2022-03-27T00:00:00Z"));
        assertEquals(1, numberOfDays);
    }

    @Test
    public void testCheckNumberOfDaysWhenSwitchingFromStandardTimeToDSTButWhenItsTheSameDay() {
        Instant time = Instant.parse("2022-03-26T23:00:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, Instant.parse("2022-03-27T02:00:00Z"));
        assertEquals(0, numberOfDays);
    }

    @Test
    public void testCheckIfNumberOfDaysIsNegative() {
        Instant time = Instant.parse("2022-07-28T06:00:00Z");
        long numberOfDays = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(time, time.minus(3, ChronoUnit.DAYS));
        assertEquals(-3, numberOfDays);
    }
}
