package pl.com.tt.flex.server.service.auction.offer;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.Hibernate;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataexport.exporter.DataExporter;
import pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFactory;
import pl.com.tt.flex.server.dataexport.factory.DataExporterFormat;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewEntity;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewType;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.domain.enumeration.NotificationEvent;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionCmvcOfferRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionOfferViewRepository;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.mail.dto.NotificationResultDTO;
import pl.com.tt.flex.server.service.mail.export.ExportResultMailService;
import pl.com.tt.flex.server.service.mail.offer.AuctionOfferMailService;
import pl.com.tt.flex.server.service.mail.offer.da.AuctionDAMailService;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.settlement.SettlementService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.AuctionCmvcDataUtil;

import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.auction.offer.AuctionReminderType.DA_OBLIGED_TO_TAKE_PART_IN_BALANCING_ENERGY;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.ACCEPTED;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType.CAPACITY;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.*;

@Slf4j
@Service
@Transactional
public class AuctionOfferServiceImpl implements AuctionOfferService {

    private final AuctionCmvcOfferRepository cmvcOfferRepository;
    private final AuctionDayAheadOfferRepository daOfferRepository;
    private final AuctionOfferViewRepository offerViewRepository;
    private final AuctionDayAheadService auctionDayAheadService;
    private final DataExporterFactory dataExporterFactory;
    private final AuctionDAMailService mailService;
    private final UserService userService;
    private final FspService fspService;
    private final NotifierFactory notifierFactory;
    private final AuctionOfferMailService offerMailService;
    private final SchedulingUnitMapper schedulingUnitMapper;
    private final FlexPotentialMapper flexPotentialMapper;
    private final ExportResultMailService exportResultMailService;
    private final SettlementService settlementService;

    public AuctionOfferServiceImpl(AuctionCmvcOfferRepository offerRepository, AuctionDayAheadOfferRepository daOfferRepository,
                                   AuctionOfferViewRepository offerViewRepository, AuctionDayAheadService auctionDayAheadService,
                                   DataExporterFactory dataExporterFactory, AuctionDAMailService mailService, UserService userService,
                                   FspService fspService, NotifierFactory notifierFactory, AuctionOfferMailService offerMailService,
                                   SchedulingUnitMapper schedulingUnitMapper, FlexPotentialMapper flexPotentialMapper,
                                   ExportResultMailService exportResultMailService, SettlementService settlementService) {
        this.cmvcOfferRepository = offerRepository;
        this.daOfferRepository = daOfferRepository;
        this.offerViewRepository = offerViewRepository;
        this.auctionDayAheadService = auctionDayAheadService;
        this.dataExporterFactory = dataExporterFactory;
        this.mailService = mailService;
        this.userService = userService;
        this.fspService = fspService;
        this.notifierFactory = notifierFactory;
        this.offerMailService = offerMailService;
        this.schedulingUnitMapper = schedulingUnitMapper;
        this.flexPotentialMapper = flexPotentialMapper;
        this.exportResultMailService = exportResultMailService;
        this.settlementService = settlementService;
    }

    @Override
    @Transactional
    public void updateStatus(AuctionOfferStatus status, List<Long> auctionOfferIds) {
        for (Long offerId : auctionOfferIds) {
            cmvcOfferRepository.updateStatus(status, offerId);
            daOfferRepository.updateStatus(status, offerId);
        }
        if (ACCEPTED.equals(status)) {
            generateSettlementsForOffers(auctionOfferIds);
        }
    }

    @Async
    @Transactional(readOnly = true)
    private void generateSettlementsForOffers(List<Long> auctionOfferIds) {
        cmvcOfferRepository.findAllById(auctionOfferIds).forEach(settlementService::generateSettlementsForOffer);
        daOfferRepository.findAllById(auctionOfferIds).forEach(settlementService::generateSettlementsForOffer);
    }

    @Override
    public FileDTO exportOffersToFile(List<AuctionOfferViewDTO> offers, String langKey, Screen screen, LevelOfDetail detail) throws IOException {
        return getDataExporter(screen).export(offers, Locale.forLanguageTag(langKey), screen, false, detail);
    }

    @Override
    public NotificationResultDTO exportOffersToFileAndSendEmail(List<AuctionOfferViewDTO> offers, String langKey, Screen screen, LevelOfDetail detail, EmailType type) throws IOException {
        FileDTO file = exportOffersToFile(offers, langKey, screen, detail);
        UserEntity currentUser = userService.getCurrentUser();
        exportResultMailService.informUserAboutExportResult(currentUser, file, type);
        return new NotificationResultDTO(currentUser.getEmail());
    }

    @Override
    @Transactional
    public void saveVolumeTransferredToBM(List<Long> offerIds) {
        daOfferRepository.saveVolumeTransferredToBmByOfferIdIn(offerIds);
    }

    @Override
    public void sendInformationAboutAcceptedCapacityOffer(List<Long> ids) {
        List<AuctionDayAheadOfferEntity> acceptedOffers = daOfferRepository.findAllById(ids);
        if (acceptedOffers.size() > 0) {
            AuctionDayAheadDTO auctionEnergy = getEnergyAuctionByOffer(acceptedOffers);
            Map<Pair<Long, Long>, List<AuctionDayAheadOfferEntity>> offersGroupingByBspAndAuctionDa = groupCapacityOfferByBspIdAndAuctionId(acceptedOffers);
            offersGroupingByBspAndAuctionDa.forEach((pairOfBspAndAuction, offers) -> {
                Long bspId = pairOfBspAndAuction.getFirst();
                List<MinimalDTO<Long, String>> usersOfBsp = fspService.findFspUsersMin(bspId);
                Long auctionCapacityId = pairOfBspAndAuction.getSecond();
                AuctionDayAheadDTO capacityAuction = auctionDayAheadService.findById(auctionCapacityId)
                    .orElseThrow(() -> new IllegalStateException("Cannot find capacity auction with id: " + auctionCapacityId));
                sendMailInformingAboutCapacityOffersAccepted(auctionEnergy, usersOfBsp, capacityAuction, offers);
                sendNotificationAboutCapacityOffersAccepted(auctionEnergy, usersOfBsp, capacityAuction, offers);
                sendReminderAboutObligatedToTakePartInEnergyAuction(usersOfBsp);
            });
        }
    }

    @Override
    public void sendReminderAboutActivation(List<Long> ids) {
        List<AuctionOfferViewEntity> offers = offerViewRepository.findAllById(ids);
        offers.forEach(auctionOfferViewEntity -> {
            Set<Long> concernedFspIds = new HashSet<>();
            concernedFspIds.add(auctionOfferViewEntity.getFspId());
            if (auctionOfferViewEntity.getOfferCategory().equals(AuctionOfferViewType.DAY_AHEAD)) {
                AuctionDayAheadOfferEntity auctionDayAheadOffer = daOfferRepository.findById(auctionOfferViewEntity.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find auction id with " + auctionOfferViewEntity.getId()));
                concernedFspIds.addAll(auctionDayAheadOffer.getUnits().stream().map(unit -> unit.getUnit().getFsp().getId()).collect(Collectors.toList()));
                List<MinimalDTO<Long, String>> fspUsers = fspService.findFspUsersMin(concernedFspIds);
                SchedulingUnitDTO schedulingUnit = schedulingUnitMapper.toDto(auctionDayAheadOffer.getSchedulingUnit());
                List<UnitMinDTO> units = schedulingUnit.getUnits();
                sendMailInformingAboutActivationOfUnitFromDayAheadOffer(fspUsers, units, schedulingUnit, auctionOfferViewEntity);
            } else if (auctionOfferViewEntity.getOfferCategory().equals(AuctionOfferViewType.CMVC)) {
                AuctionCmvcOfferEntity auctionCmvcOffer = cmvcOfferRepository.findById(auctionOfferViewEntity.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find auction id with " + auctionOfferViewEntity.getId()));
                FlexPotentialDTO flexPotential = flexPotentialMapper.toDto(auctionCmvcOffer.getFlexPotential());
                List<UnitMinDTO> units = flexPotential.getUnits();
                long daysBetweenTodayAndDayOfDelivery = AuctionCmvcDataUtil.calculateDaysBetweenTodayAndDayOfDelivery(Instant.now(), auctionOfferViewEntity.getDeliveryPeriodFrom());
                if (daysBetweenTodayAndDayOfDelivery >= 2) {
                    auctionCmvcOffer.setScheduledActivationEmail(true);
                } else {
                    concernedFspIds.addAll(auctionCmvcOffer.getFlexPotential().getUnits().stream().map(unit -> unit.getFsp().getId()).collect(Collectors.toList()));
                    List<MinimalDTO<Long, String>> fspUsers = fspService.findFspUsersMin(concernedFspIds);
                    sendMailInformingAboutActivationOfUnitFromCmvcOffer(fspUsers, units, auctionOfferViewEntity);
                }
            }
        });
    }

    private void sendMailInformingAboutActivationOfUnitFromDayAheadOffer(List<MinimalDTO<Long, String>> usersOfFsp, List<UnitMinDTO> units, SchedulingUnitDTO schedulingUnit, AuctionOfferViewEntity auctionOfferViewEntity) {
        initializeLazyLoadedParam(auctionOfferViewEntity);
        usersOfFsp.forEach(user -> offerMailService.remindUserAboutActivationOfUnitInDayAheadOffer(userService.findOne(user.getId()).get(), units, schedulingUnit, auctionOfferViewEntity));
    }

    private void sendMailInformingAboutActivationOfUnitFromCmvcOffer(List<MinimalDTO<Long, String>> usersOfFsp, List<UnitMinDTO> units, AuctionOfferViewEntity auctionOfferViewEntity) {
        initializeLazyLoadedParam(auctionOfferViewEntity);
        usersOfFsp.forEach(user -> offerMailService.remindUserAboutActivationOfUnitInCmvcOffer(userService.findOne(user.getId()).get(), units, auctionOfferViewEntity));
    }

    //Wymagane załadowanie parametru z FetchType.LAZY do pamięci,
    // bez tego metoda z @Asynch w offerMailService wogóle się nie wykona
    private void initializeLazyLoadedParam(AuctionOfferViewEntity auctionOfferViewEntity) {
        Hibernate.initialize(auctionOfferViewEntity.getSchedulingUnitOrPotentialDers());
    }

    private void sendReminderAboutObligatedToTakePartInEnergyAuction(List<MinimalDTO<Long, String>> usersOfBsp) {
        auctionDayAheadService.sendReminderInformationToBspUsers(usersOfBsp, DA_OBLIGED_TO_TAKE_PART_IN_BALANCING_ENERGY);
    }

    private void sendMailInformingAboutCapacityOffersAccepted(AuctionDayAheadDTO auctionEnergy, List<MinimalDTO<Long, String>> usersOfBsp, AuctionDayAheadDTO capacityAuction, List<AuctionDayAheadOfferEntity> offers) {
        usersOfBsp.forEach(u -> mailService.informUserAboutAcceptedCapacityOffer(userService.findOne(u.getId()).get(), offers, capacityAuction, auctionEnergy));
    }

    private void sendNotificationAboutCapacityOffersAccepted(AuctionDayAheadDTO auctionEnergy, List<MinimalDTO<Long, String>> usersOfBsp, AuctionDayAheadDTO capacityAuction, List<AuctionDayAheadOfferEntity> acceptedOffers) {
        String acceptedOfferIDs = acceptedOffers.stream().map(AuctionDayAheadOfferEntity::getId).map(Object::toString).collect(Collectors.joining(", "));
        Map<NotificationParam, NotificationParamValue> notificationParams = NotificationUtils.ParamsMapBuilder.create()
            .addParam(DA_CAPACITY_AUCTION_NAME, capacityAuction.getName())
            .addParam(DA_ACCEPTED_BID_IDS, acceptedOfferIDs)
            .addParam(DA_ENERGY_AUCTION_NAME, auctionEnergy.getName())
            .addParam(DA_ENERGY_AUCTION_ID, auctionEnergy.getId())
            .addParam(DA_AUCTION_DELIVERY_DATE, auctionEnergy.getDeliveryDate())
            .addParam(DA_ENERGY_AUCTION_GATE_OPENING, auctionEnergy.getEnergyGateOpeningTime())
            .addParam(DA_ENERGY_AUCTION_GATE_CLOSURE, auctionEnergy.getEnergyGateClosureTime())
            .build();

        NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, NotificationEvent.AUCTION_DA_ACCEPTED_CAPACITY_OFFER,
            notificationParams, usersOfBsp);
    }


    // Po zlozeniu ofery na pojemność, należy złożyć oferte na aukcje Energii na ten sam dzień dostawy
    private AuctionDayAheadDTO getEnergyAuctionByOffer(List<AuctionDayAheadOfferEntity> acceptedOffers) {
        Instant deliveryDate = acceptedOffers.stream().findFirst().get().getAuctionDayAhead().getDeliveryDate();
        List<AuctionDayAheadDTO> energyAuctions = auctionDayAheadService.findAllAuctionByTypeAndDeliveryDate(AuctionDayAheadType.ENERGY, deliveryDate);
        return energyAuctions.stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("Cannot find auction of type ENERGY and deliveryDate: " + deliveryDate));
    }

    public boolean areAllOffersPendingOrVerified(List<Long> offerIds) {
        return offerViewRepository.areAllOffersPendingOrVerified(offerIds);
    }

    public boolean areAllDayAheadVolumesVerified(List<Long> offerIds) {
        return offerViewRepository.areAllDayAheadVolumesVerified(offerIds);
    }

    public boolean areAllAuctionsClosed(List<Long> offerIds) {
        return offerViewRepository.areAllAuctionsClosed(offerIds);
    }

    // Metoda grupujaca oferty wedlug: id BSP oraz id aukcji (capacity)
    private Map<Pair<Long, Long>, List<AuctionDayAheadOfferEntity>> groupCapacityOfferByBspIdAndAuctionId(List<AuctionDayAheadOfferEntity> acceptedOffers) {
        return acceptedOffers.stream()
            .filter(offer -> offer.getType().equals(CAPACITY))
            .collect(Collectors.groupingBy(o -> Pair.of(o.getSchedulingUnit().getBsp().getId(), o.getAuctionDayAhead().getId())));
    }

    private Map<Pair<Long, Long>, List<AuctionCmvcOfferEntity>> groupCmvcOffersByFspIdAndAuctionId(List<AuctionCmvcOfferEntity> acceptedOffers) {
        return acceptedOffers.stream().collect(Collectors.groupingBy(offer -> Pair.of(offer.getFlexPotential().getFsp().getId(), offer.getAuctionCmvc().getId())));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductNameMinDTO> findAllProductsUsedInOffer() {
        return offerViewRepository.findAllProductsUsedInOffer();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCmvcOffer(Long id) {
        return cmvcOfferRepository.existsById(id);
    }

    public DataExporter getDataExporter(Screen screen) {
        return dataExporterFactory.getDataExporter(DataExporterFormat.XLSX, AuctionOfferViewDTO.class, screen);
    }
}
