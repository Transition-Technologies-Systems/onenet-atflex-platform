package pl.com.tt.flex.server.service.auction.scheduled;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewEntity;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionCmvcOfferRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionOfferViewRepository;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.server.service.mail.offer.AuctionOfferMailService;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.util.AuctionCmvcDataUtil;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@ConditionalOnProperty("application.mail-sendreminder.enabled")
public class AuctionMailOfferScheduler {

    private final AuctionCmvcOfferRepository auctionCmvcOfferRepository;
    private final AuctionOfferViewRepository auctionOfferViewRepository;
    private final UserRepository userRepository;
    private final AuctionOfferMailService offerMailService;
    private final FlexPotentialMapper flexPotentialMapper;

    public AuctionMailOfferScheduler(AuctionCmvcOfferRepository auctionCmvcOfferRepository, AuctionOfferViewRepository auctionOfferViewRepository,
        UserRepository userRepository, AuctionOfferMailService offerMailService, FlexPotentialMapper flexPotentialMapper) {
        this.auctionCmvcOfferRepository = auctionCmvcOfferRepository;
        this.auctionOfferViewRepository = auctionOfferViewRepository;
        this.userRepository = userRepository;
        this.offerMailService = offerMailService;
        this.flexPotentialMapper = flexPotentialMapper;
    }

    @Scheduled(cron = "${application.mail-sendreminder.cron}")
    @Transactional
    public void execute() {
        log.info("Sending activation reminders for auction offers whose delivery dates are next day");
        List<AuctionCmvcOfferEntity> auctionCmvcOffers = auctionCmvcOfferRepository.findAllByScheduledActivationEmail().stream()
            .filter(auctionCmvcOfferEntity -> isReadyForSendingActivationEmail(Instant.now(), auctionCmvcOfferEntity.getDeliveryPeriodFrom())).collect(Collectors.toList());
        log.info("Messages ready to be sent: " + auctionCmvcOffers.size());
        if (auctionCmvcOffers.size() > 0) {
            auctionCmvcOffers.forEach(auctionCmvcOfferEntity -> {
                AuctionOfferViewEntity auctionOfferViewEntity = auctionOfferViewRepository.findById(auctionCmvcOfferEntity.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find auction with id " + auctionCmvcOfferEntity.getId()));
                FlexPotentialDTO flexPotential = flexPotentialMapper.toDto(auctionCmvcOfferEntity.getFlexPotential());
                List<UnitMinDTO> units = flexPotential.getUnits();
                List<UserEntity> users = userRepository.findAllByFspId(auctionOfferViewEntity.getFspId());
                users.forEach(user -> offerMailService.remindUserAboutActivationOfUnitInCmvcOffer(user, units, auctionOfferViewEntity));
                auctionCmvcOfferEntity.setScheduledActivationEmail(false);
            });
            auctionCmvcOfferRepository.saveAll(auctionCmvcOffers);
        }
        log.info("{} messages sent", auctionCmvcOffers.size());
    }

    public boolean isReadyForSendingActivationEmail(Instant now, Instant acceptedDeliveryDateFrom) {
        // W normalnych okolicznościach mail powinien być wysyłany dzień przed rozpoczęciem dostawy, jednak mogą być przypadki gdy serwer będzie niedostępny.
        // W tym wypadku, po wcześniejszych ustaleniach, wysyłamy maila przy następnym wywołaniu, niezależnie od tego, czy dostawa się rozpoczęła czy nie.
        return AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(now, acceptedDeliveryDateFrom) <= 1;
    }
}
