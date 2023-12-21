package pl.com.tt.flex.server.service.auction.da;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionReminderType;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.auction.da.AuctionDayAheadRepository;
import pl.com.tt.flex.server.repository.auction.da.AuctionDayAheadViewRepository;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadMapper;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadOfferMapper;
import pl.com.tt.flex.server.service.auction.da.mapper.AuctionDayAheadSeriesConverter;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionReminderDTO;
import pl.com.tt.flex.server.service.common.AbstractServiceImpl;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.mapper.EntityMapper;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.AuctionDayAheadDataUtil;
import pl.com.tt.flex.server.util.DateUtil;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.auction.da.AuctionDayAheadOfferValidator;
import pl.com.tt.flex.server.validator.auction.da.AuctionDayAheadOfferValidatorUtil;
import pl.com.tt.flex.server.web.rest.websocket.FlexUserWebsocketResource;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.util.Objects.isNull;
import static pl.com.tt.flex.model.service.dto.auction.offer.AuctionReminderType.DA_OFFER_HAS_BEEN_SUBMITTED_IN_BALANCING_ENERGY;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType.CAPACITY;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType.ENERGY;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus.*;

/**
 * Service Implementation for managing {@link AuctionDayAheadEntity}.
 */
@Service
@Slf4j
@Transactional
public class AuctionDayAheadServiceImpl extends AbstractServiceImpl<AuctionDayAheadEntity, AuctionDayAheadDTO, Long> implements AuctionDayAheadService {

    private final AuctionDayAheadRepository auctionDayAheadRepository;
    private final AuctionDayAheadMapper auctionDayAheadMapper;
    private final AuctionDayAheadViewRepository auctionDayAheadViewRepository;

    private final AuctionDayAheadOfferRepository auctionOfferRepository;
    private final AuctionDayAheadOfferMapper auctionOfferMapper;
    private final AuctionDayAheadOfferValidator offerValidator;

    private final UserService userService;
    private final FspService fspService;
    private final SchedulingUnitService schedulingUnitService;
    private final FlexUserWebsocketResource flexUserWebsocketResource;

    private final AuctionDayAheadSeriesConverter auctionDayAheadSeriesGenerator;

    public AuctionDayAheadServiceImpl(AuctionDayAheadRepository auctionDayAheadRepository, AuctionDayAheadMapper auctionDayAheadMapper,
                                      AuctionDayAheadViewRepository auctionDayAheadViewRepository, AuctionDayAheadOfferRepository auctionOfferRepository,
                                      AuctionDayAheadOfferMapper auctionOfferMapper, AuctionDayAheadOfferValidator offerValidator, UserService userService,
                                      FspService fspService, SchedulingUnitService schedulingUnitService, FlexUserWebsocketResource flexUserWebsocketResource, AuctionDayAheadSeriesConverter auctionDayAheadSeriesGenerator) {
        this.auctionDayAheadRepository = auctionDayAheadRepository;
        this.auctionDayAheadMapper = auctionDayAheadMapper;
        this.auctionDayAheadViewRepository = auctionDayAheadViewRepository;
        this.auctionOfferRepository = auctionOfferRepository;
        this.auctionOfferMapper = auctionOfferMapper;
        this.offerValidator = offerValidator;
        this.userService = userService;
        this.fspService = fspService;
        this.schedulingUnitService = schedulingUnitService;
        this.flexUserWebsocketResource = flexUserWebsocketResource;
        this.auctionDayAheadSeriesGenerator = auctionDayAheadSeriesGenerator;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuctionDayAheadDTO> findById(Long aLong) {
        Optional<AuctionDayAheadDTO> maybyAuctionDayAhead = super.findById(aLong);
        if (maybyAuctionDayAhead.isPresent()) {
            AuctionDayAheadDTO auctionDayAheadDTO = maybyAuctionDayAhead.get();
            auctionDayAheadDTO.setCanAddBid(canCurrentLoggedUserAddNewBid(auctionDayAheadDTO));
            maybyAuctionDayAhead = Optional.of(auctionDayAheadDTO);
        }
        return maybyAuctionDayAhead;
    }

    /**
     * Na podstawie zdefiniowanego obietku AuctionSeries generowane są aukcje na cały okres trwania aukcji (od FirstAuctionDate do LastAuctionDate)
     */
    @Override
    @Transactional
    public void createDayAheadsForSeries(AuctionsSeriesEntity series) {
        log.debug("createDayAheadsForSeries() Series: id[{}], firstDate[{}], lastDate[{}]", series.getId(), series.getFirstAuctionDate(), series.getLastAuctionDate());
        List<AuctionDayAheadEntity> dayAheads = Lists.newArrayList();
        int daNumber = 0;
        for (Instant date = series.getFirstAuctionDate(); !date.isAfter(series.getLastAuctionDate()); date = date.plus(1, DAYS)) {
            AuctionDayAheadEntity dayAhead = auctionDayAheadSeriesGenerator.generateNextDayAhead(series, daNumber++);
            log.debug("createDayAheadsForSeries() DayAhead created for day {}", dayAhead.getDay());
            dayAheads.add(dayAhead);
        }
        auctionDayAheadRepository.saveAll(dayAheads);
    }

    /**
     * Aukcje DA geneorwane są codziennie przez okres od FirstAuctionDate do LastAuctionDate (na podstawie zdefiniowanego obiektu Serii).
     * Przy modyfikacji Serii:
     * -gdy zotanie skrócony okres generowanie się aukcji, to zaplanowane aukcje które nie obejmują tego okresu zostają usuwane.
     * -gdy zostanie wydłużony okres generowania się aukcji, do istniejących aukcji dodawane są nowe aukcje.
     * -gdy któryś z parametórw Serii ulega zmianie, zaplanowane aukcje zostają aktualiozwane (oprócz aukcji zakończonych i rozpoczętych)
     */
    @Override
    public void updateScheduledAuctions(AuctionsSeriesEntity series) {
        log.debug("updateScheduledAuctions - START updated DayAhead auctions created from series [seriesId = {}]", series.getId());
        List<AuctionDayAheadEntity> dbCreatedAuction = auctionDayAheadRepository.findAllByAuctionSeriesId(series.getId());

        // Mapa zawierająca dni w których będą odbywać się aukcje DA
        // Kluczem jest numer auckji, wartością dzień aukcji.
        Map<Integer, Instant> allAuctionsDate = getAllAuctionDayGroupingByAuctionNumber(series);

        // Jeżeli nie istnieje aukcja na dany dzień, to zostaje ona dodana
        allAuctionsDate.forEach((auctionNumber, auctionDate) -> {
            boolean isCreatedAuction = dbCreatedAuction.stream().map(AuctionDayAheadEntity::getDay).collect(Collectors.toList()).contains(auctionDate);
            if (!isCreatedAuction) {
                AuctionDayAheadEntity auctionDayAheadEntity = auctionDayAheadSeriesGenerator.generateNextDayAhead(series, auctionNumber);
                AuctionDayAheadEntity savedAuction = auctionDayAheadRepository.save(auctionDayAheadEntity);
                log.debug("updateScheduledAuctions - created new scheduled auction with id={} and name={}", savedAuction.getId(), savedAuction.getName());
            }
        });

        // Jeżeli istnieje aukcja na dzień, który nie obejmują okresu trwania auckji z Serii, aukcja zostaje usunięta.
        // Jeżeli istnieje auckja na dany dzień, to zostaje ona zaktualizowana
        dbCreatedAuction.forEach(dbAuction -> {
            var auctionTime = dbAuction.getDay();
            //Warunek sprawdza istnienie aukcji z uwzględnieniem zmiany czasu letni/zimowy
            if (Collections.disjoint(allAuctionsDate.values(), Set.of(auctionTime.minus(1, HOURS), auctionTime, auctionTime.plus(1, HOURS)))) {
                auctionDayAheadRepository.delete(dbAuction);
                log.debug("updateScheduledAuctions - deleted existing scheduled auction with id={} and name={}", dbAuction.getId(), dbAuction.getName());
            } else {
                log.debug("updateScheduledAuctions - try to update existing scheduled auction with id={} and name={}", dbAuction.getId(), dbAuction.getName());
                updateNotStartedDaAuctions(series, dbAuction);
            }
        });
        log.debug("updateScheduledAuctions - END updated DayAhead auctions created from series [seriesId = {}]", series.getId());
    }

    private Map<Integer, Instant> getAllAuctionDayGroupingByAuctionNumber(AuctionsSeriesEntity series) {
        Map<Integer, Instant> mapAuctionNumberAndDate = new HashMap<>();
        int daNumber = 0;
        for (Instant date = series.getFirstAuctionDate(); !date.isAfter(series.getLastAuctionDate()); date = date.plus(1, DAYS)) {
            mapAuctionNumberAndDate.put(daNumber++, date);
        }
        return mapAuctionNumberAndDate;
    }

    /**
     * Aktualizowane są aukcje gdy spełnione są następujące warunki:
     * - aktualizowany czas rozpoczecia aukcji jest późniejszy niż aktualna godzina,
     * - aukcja jest jeszcze nie rozpoczęta.
     * <p>
     * W Series zapisane sa daty pierwszej aukcji DayAhead
     */
    private void updateNotStartedDaAuctions(AuctionsSeriesEntity modifySeries, AuctionDayAheadEntity dayAheadToUpdate) {
        boolean isNotStartedAuction = !Arrays.asList(CLOSED_CAPACITY, CLOSED_ENERGY, OPEN_CAPACITY, OPEN_ENERGY).contains(dayAheadToUpdate.getStatus());
        boolean canModify = !isCurrentDayAuction(dayAheadToUpdate, InstantUtil.now()) || isGateOpeningTimeAfterCurrentTime(modifySeries);
        if (canModify && isNotStartedAuction) {
            //kopiujemy same godziny
            auctionDayAheadSeriesGenerator.updateDayAhead(dayAheadToUpdate, modifySeries);
            log.debug("updateNotStartedDaAuctions() Auction with id {} is updated", dayAheadToUpdate.getId());
        } else {
            log.debug("updateNotStartedDaAuctions() Auction with id {} is not updated. NOT UPDATED REASON: canModify={}, isNotStartedAuction={}", dayAheadToUpdate.getId(), canModify, isNotStartedAuction);
        }
    }

    private boolean isCurrentDayAuction(AuctionDayAheadEntity dayAhead, Instant now) {
        return now.isAfter(dayAhead.getDay()) && now.isBefore(dayAhead.getDay().plus(1, DAYS));
    }

    private boolean isGateOpeningTimeAfterCurrentTime(AuctionsSeriesEntity series) {
        if (series.getType().equals(ENERGY)) {
            return DateUtil.isTimeAfter(series.getEnergyGateOpeningTime(), InstantUtil.now());
        } else if (series.getType().equals(AuctionDayAheadType.CAPACITY)) {
            return DateUtil.isTimeAfter(series.getCapacityGateOpeningTime(), InstantUtil.now());
        }
        throw new IllegalArgumentException("Illegal value for AuctionDayAheadType of AuctionsSeriesEntity: " + series.getType());
    }

    @Override
    @Transactional
    public void updateAuctionName(String auctionName, Long id) {
        auctionDayAheadRepository.updateAuctionName(auctionName, id);
    }

    @Override
    @Transactional(readOnly = true)
    public AuctionStatus findAuctionStatusById(Long id) {
        return auctionDayAheadViewRepository.findStatusById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductNameMinDTO> findAllProductsUsedInAuctionDayAheadByType(AuctionDayAheadType auctionDayAheadType) {
        return auctionDayAheadRepository.findAllProductsUsedInAuctionDayAheadByType(auctionDayAheadType);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsOpenAuctionWithProductId(long productId) {
        return auctionDayAheadRepository.existsOpenAuctionsWithProductId(productId);
    }

    @Override
    @Transactional
    public List<AuctionDayAheadDTO> findAllAuctionByTypeAndDeliveryDate(AuctionDayAheadType auctionType, Instant deliveryDate) {
        return getMapper().toDto(auctionDayAheadRepository.findAllByTypeAndDeliveryDate(auctionType, deliveryDate));
    }

    //********************************************************************************** OFFERS ************************************************************************************

    /**
     * @param auctionStatus status aukcji w momencie klikniecia przycisku utworz/edytuj/usun oferte
     */
    @Override
    @Transactional
    public AuctionDayAheadOfferDTO saveOffer(AuctionDayAheadOfferDTO offerDTO, AuctionStatus auctionStatus, boolean isManualUpdate) throws ObjectValidationException {
        removeNullBands(offerDTO);
        offerDTO.setStatus(AuctionOfferStatus.PENDING); //do zmiany statusu jest oddzielny ep
        var isNewOffer = isNull(offerDTO.getId());
        AuctionDayAheadOfferEntity dbOfferEntity = null;
        if (isNewOffer) {
            setAcceptedFieldsInOffer(offerDTO);
            offerValidator.checkValid(offerDTO, auctionStatus);
        } else {
            UserDTO currentUser = userService.getCurrentUserDTO().orElseThrow(() -> new IllegalStateException("Cannot find current logged-in user"));
            Long offerId = offerDTO.getId();
            dbOfferEntity = auctionOfferRepository.findById(offerId).orElseThrow(() -> new IllegalStateException("Cannot find offer with id: " + offerId));
            AuctionDayAheadOfferDTO dbOffer = auctionOfferMapper.toDayAheadDto(dbOfferEntity);
            offerDTO = AuctionDayAheadOfferValidatorUtil.overwriteOnlyAllowedOfferDtoFieldsForCurrentUser(offerDTO, auctionStatus, dbOffer, currentUser, isManualUpdate);
            offerValidator.checkModifiable(offerDTO, auctionStatus);
        }
        AuctionDayAheadOfferEntity auctionOfferEntity = auctionOfferMapper.toEntityFromDayAhead(offerDTO);
        AuctionDayAheadDataUtil.calculateAndSetPrice(auctionOfferEntity);
        AuctionDayAheadDataUtil.calculateAndSetVolume(auctionOfferEntity, auctionStatus, dbOfferEntity);
        AuctionDayAheadDataUtil.calculateAndSetGDFFactors(auctionOfferEntity);
        auctionOfferEntity = auctionOfferRepository.save(auctionOfferEntity);
        sendInformationAboutRemovingReminder(offerDTO);
        return auctionOfferMapper.toDayAheadDto(auctionOfferEntity);
    }

    private void removeNullBands(AuctionDayAheadOfferDTO offerDTO) {
        offerDTO.getDers().stream()
            .map(AuctionOfferDersDTO::getBandData)
            .forEach(list -> list.removeIf(
                band -> Objects.isNull(band.getVolume())));
    }

    private void setAcceptedFieldsInOffer(AuctionDayAheadOfferDTO offerDTO) {
        offerDTO.getDers().forEach(offerDer -> CollectionUtils.emptyIfNull(
            offerDer.getBandData()).forEach(band -> {
                band.setAcceptedVolume(band.getVolume());
                band.setAcceptedPrice(band.getPrice());
        }));
        offerDTO.setAcceptedDeliveryPeriodFrom(offerDTO.getDeliveryPeriodFrom());
        offerDTO.setAcceptedDeliveryPeriodTo(offerDTO.getDeliveryPeriodTo());
    }

    // Uzytkownik BSP po zlozeniu oferty na aukcje Capacity, ma obowiązak zlozyc oferte na aukcje Energy
    // Metoda ta sprawdza czy dany BSP spełnia swoje zobowiązanie, składając nową oferte na aukcje Energy
    // podczas gdy wczesniej zlozyl oferte na aukcjie Capacity
    private boolean checkIfUserFulfillHisObligations(AuctionDayAheadOfferDTO offerDTO, Long bspId) {
        return Objects.isNull(offerDTO.getId()) &&
            offerDTO.getType().equals(AuctionOfferType.ENERGY) &&
            isUserObligatedToTakePartInEnergyAuction(bspId, getDeliveryDateFromNow());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AuctionDayAheadOfferDTO> findOfferById(Long id) {
        UserEntity user = userService.getCurrentUser();
        // usunięcie ceny dla DSO
        if (user.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR)) {
            // metoda peek() bierze obiekt Consumer (bez zwracania wartości), natomiast map() przyjmuje Function (ze zwracaniem)
            // Używana również do zmiany wewnętrznego stanu obiektu
            return auctionOfferRepository.findById(id).stream().peek(auctionDayAheadOfferEntity -> {
                auctionDayAheadOfferEntity.setPrice(null);
                auctionDayAheadOfferEntity.getUnits().forEach(auctionOfferDersEntity ->
                    auctionOfferDersEntity.getBandData().forEach(auctionOfferBandDataEntity -> {
                        auctionOfferBandDataEntity.setPrice(null);
                        auctionOfferBandDataEntity.setAcceptedPrice(null);
                    }));
            }).findFirst().map(auctionOfferMapper::toDayAheadDto);
        }
        return auctionOfferRepository.findById(id).map(auctionOfferMapper::toDayAheadDto);
    }

    /**
     * @param auctionStatus status aukcji w momencie klikniecia przycisku utworz/edytuj/usun oferte
     */
    @Override
    @Transactional
    public void deleteOffer(Long offerId, AuctionStatus auctionStatus) throws ObjectValidationException {
        offerValidator.checkDeletable(offerId, auctionStatus);
        // Usuwane są najpierw powiązania z obliczeniami algorytmu a potem usuwana jest dana oferta
        auctionOfferRepository.deleteDaOffersFromAlgorithmEvaluationByOfferId(offerId);
        auctionOfferRepository.deleteCmvcOffersFromAlgorithmEvaluationByOfferId(offerId);
        auctionOfferRepository.deleteById(offerId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canCurrentLoggedUserAddNewBid(AuctionDayAheadDTO auctionDayAheadDTO) {
        UserEntity user = userService.getCurrentUser();
        //tylko ADMIN i BSP moga skladac oferty
        if (!(user.getRoles().contains(Role.ROLE_ADMIN) || user.getRoles().contains(Role.ROLE_BALANCING_SERVICE_PROVIDER))) {
            return false;
        }
        //jesli uzytkownik nie ma odpowiedniej jednostki grafikowej (jednostki z typem ktory ma podpiety produkt aukcji),
        // wtedy nie moze skladac oferty
        if (!isExistUserSchedulingUnitForOffer(auctionDayAheadDTO, user)) {
            return false;
        }
        return canAddBidToAuctionTypeCapacityOrEnergy(auctionDayAheadDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FspCompanyMinDTO> findBspsWithRegisteredSchedulingUnitsForProductAndAuction(Long productId, Long auctionId) {
        AuctionDayAheadDTO auctionDayAheadDTO = findById(auctionId).orElseThrow(() -> new IllegalStateException("Cannot find auction by id " + auctionId));

//        if (auctionDayAheadDTO.getType().equals(AuctionDayAheadType.CAPACITY_AND_ENERGY) && auctionDayAheadDTO.getStatus().equals(OPEN_ENERGY)) {
//            return auctionDayAheadRepository.findBspsWithSubmittedCapacityOfferToAuctionCE(auctionId);
//        }
        return auctionDayAheadRepository.findBspsWithRegisteredSchedulingUnitsForProduct(productId);
    }

    // Zwracanie informacji o aukcji na Energy, na ktora uzytkownik jest zobowiazany zlozyc oferte
    @Override
    @Transactional(readOnly = true)
    public AuctionReminderDTO getAuctionOfferReminderForBsp(Long bspId) {
        Instant deliveryDate = getDeliveryDateFromNow();
        if (isUserObligatedToTakePartInEnergyAuction(bspId, deliveryDate)) {
            return auctionDayAheadRepository.findAllByTypeAndDeliveryDate(ENERGY, deliveryDate).stream()
                .findFirst()
                .map(auction -> new AuctionReminderDTO(auction.getName(), auction.getId(), auction.getEnergyGateOpeningTime(), auction.getEnergyGateClosureTime()))
                .orElse(new AuctionReminderDTO());
        }
        return new AuctionReminderDTO();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsAlgorithmEvaluationsForGivenOfferId(Long id) {
        return auctionOfferRepository.existsAlgorithmEvaluationsForGivenOfferId(id);
    }

    // jeżeli uzytkownik zlozyl oferte na aukcje Capacity i ta oferta zostala zaakceptowana,
    // powinien zlozyc oferte na aukcjie Energy
    private boolean isUserObligatedToTakePartInEnergyAuction(Long bspId, Instant deliveryDate) {
        boolean existAnyAcceptedOfferForCapacityAuction = auctionOfferRepository.existsAcceptedDaOfferByAuctionTypeDeliveryDateAndBspId(CAPACITY, deliveryDate, bspId);
        boolean existOfferForEnergyAuction = auctionOfferRepository.existsDaOfferByAuctionTypeDeliveryDateAndBspId(ENERGY, deliveryDate, bspId);
        return existAnyAcceptedOfferForCapacityAuction && !existOfferForEnergyAuction;
    }

    // Wyslanie przez WS informacji o spelnieniu zobowiazania przez uzytkownika BSP
    private void sendInformationAboutRemovingReminder(AuctionDayAheadOfferDTO offerDTO) {
        Long bspId = schedulingUnitService.findOwnerBspId(offerDTO.getSchedulingUnit().getId());
        if (checkIfUserFulfillHisObligations(offerDTO, bspId)) {
            List<MinimalDTO<Long, String>> bspUsers = fspService.findFspUsersMin(bspId);
            sendReminderInformationToBspUsers(bspUsers, DA_OFFER_HAS_BEEN_SUBMITTED_IN_BALANCING_ENERGY);
        }
    }

    //Wysylka informacji zwiazanych z przypomnieniem koniecznosci wziecia udziału w aukcji Energy
    @Transactional
    public void sendReminderInformationToBspUsers(List<MinimalDTO<Long, String>> usersOfBsp, AuctionReminderType type) {
        try {
            usersOfBsp.forEach(user -> flexUserWebsocketResource.postAuctionReminder(user.getValue(), type));
        } catch (Exception e) {
            log.debug("sendReminderInformationToBspUsers() Error while post reminder {} to {} app\n{}", type, Constants.FLEX_USER_APP_NAME, e.getMessage());
        }
    }

    @Override
    public Set<AuctionDayAheadOfferDTO> findAllOfferByAuctionTypeAndDeliveryDate(AuctionDayAheadType type, Instant deliveryDate) {
        return auctionOfferRepository.findAllByTypeAndDeliveryDate(type, deliveryDate)
            .stream().map(auctionOfferMapper::toDayAheadDto).collect(Collectors.toSet());
    }

    @Override
    public Set<AuctionDayAheadOfferDTO> findAllOffersById(List<Long> offersId) {
        return auctionOfferRepository.findAllById(offersId)
            .stream().map(auctionOfferMapper::toDayAheadDto).collect(Collectors.toSet());
    }

    @Override
    public boolean existsDaOfferByDeliveryDateAndUnitId(Instant deliveryDate, Long unitId) {
        return auctionOfferRepository.existsDaOfferByDeliveryDateAndUnitId(deliveryDate, unitId);
    }

    @Override
    public List<UnitEntity> getDersByOfferIds(List<Long> offerIds) {
        return auctionOfferRepository.findDersInOffers(offerIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UnitSelfScheduleEntity> getSelfSchedulesForDersInOffers(List<Long> offerIds) {
        return auctionOfferRepository.findSelfSchedulesForDersInOffers(offerIds);
    }

    private boolean isExistUserSchedulingUnitForOffer(AuctionDayAheadDTO auctionDayAheadDTO, UserEntity user) {
        if (!user.getRoles().contains(Role.ROLE_ADMIN)) {
            return schedulingUnitService.existsActiveCertifiedByUserAndProductId(user, auctionDayAheadDTO.getProduct().getId());
        }
        return schedulingUnitService.existsActiveCertifiedByProductId(auctionDayAheadDTO.getProduct().getId());
    }

    private boolean canAddBidToAuctionTypeCapacityOrEnergy(AuctionDayAheadDTO auctionDayAheadDTO) {
        if (auctionDayAheadDTO.getType().equals(AuctionDayAheadType.CAPACITY)) {
            return canAddBidToAuctionTypeCapacity(auctionDayAheadDTO);
        } else if (auctionDayAheadDTO.getType().equals(ENERGY)) {
            return canAddBidToAuctionTypeEnergy(auctionDayAheadDTO);
        }
        throw new IllegalArgumentException("Illegal value for AuctionDayAheadType of AuctionDayAheadDTO: " + auctionDayAheadDTO.getType());
    }

    private boolean canAddBidToAuctionTypeCapacity(AuctionDayAheadDTO auctionDTO) {
        return auctionDTO.getType().equals(AuctionDayAheadType.CAPACITY) && auctionDTO.getStatus().equals(OPEN_CAPACITY);
    }

    private boolean canAddBidToAuctionTypeEnergy(AuctionDayAheadDTO auctionDTO) {
        return auctionDTO.getType().equals(ENERGY) && auctionDTO.getStatus().equals(OPEN_ENERGY);
    }

    private Instant getDeliveryDateFromNow() {
        LocalDate deliveryLocalDate = LocalDate.now().plusDays(1);
        return deliveryLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }
    //********************************************************************************** OFFERS ************************************************************************************

    @Override
    public AbstractJpaRepository<AuctionDayAheadEntity, Long> getRepository() {
        return this.auctionDayAheadRepository;
    }

    @Override
    public EntityMapper<AuctionDayAheadDTO, AuctionDayAheadEntity> getMapper() {
        return this.auctionDayAheadMapper;
    }
}
