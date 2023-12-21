package pl.com.tt.flex.server.validator.auction.da;


import static pl.com.tt.flex.model.security.permission.Role.ROLE_ADMIN;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_BALANCING_SERVICE_PROVIDER;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.PENDING;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.VOLUMES_VERIFIED;
import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.deliveryPeriodContainsHour;
import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.getDeliveryPeriodRange;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AUCTION_DA_OFFER_USER_HAS_NO_PRIVILEGES_TO_EDIT_BID;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.data.util.Pair;

import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.AuctionDayAheadDataUtil;

@Slf4j
public class AuctionDayAheadOfferValidatorUtil {

    private AuctionDayAheadOfferValidatorUtil() {
    }

    private static final Set<Role> modifyOpenAuctionRoles = Set.of(
        ROLE_ADMIN,
        ROLE_BALANCING_SERVICE_PROVIDER,
        ROLE_DISTRIBUTION_SYSTEM_OPERATOR,
        ROLE_TRANSMISSION_SYSTEM_OPERATOR
    );

    private static final Set<Role> modifyClosedAuctionRoles = Set.of(
        ROLE_ADMIN,
        ROLE_DISTRIBUTION_SYSTEM_OPERATOR,
        ROLE_TRANSMISSION_SYSTEM_OPERATOR
    );

    //oferty moga edytowac uzytkownicy z rolami BSP, TA(Admin), DSO
    //maja oni w swoich kontenerach uprawnienia: FLEX_USER_AUCTIONS_DAY_AHEAD_OFFER_EDIT / FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_EDIT
    //pola ktore uzytkownik moze edytowac zaleza od roli uzytkownika i statusu aukcji
    //metoda zwraca DTO edytowanej oferty pobranej z bazy danych, nadpisane o te pola z formularza edycji, ktore aktualny uzytkownik moze edytowac
    public static AuctionDayAheadOfferDTO overwriteOnlyAllowedOfferDtoFieldsForCurrentUser(AuctionDayAheadOfferDTO modifiedOffer, AuctionStatus auctionStatus, AuctionDayAheadOfferDTO dbOffer,
                                                                                           UserDTO currentUser, boolean isManualUpdate) throws ObjectValidationException {
        if (isAuctionOpen(dbOffer.getType(), auctionStatus)) {
            if (currentUser.hasAnyRole(modifyOpenAuctionRoles)) {
                return updateOfferWhenAuctionIsOpen(modifiedOffer, dbOffer, currentUser);
            }
        } else if (isAuctionClosed(dbOffer.getType(), auctionStatus) && Set.of(PENDING, VOLUMES_VERIFIED).contains(dbOffer.getStatus())) {
            if (currentUser.hasAnyRole(modifyClosedAuctionRoles)) {
                return updateOfferWhenAuctionIsClosed(modifiedOffer, dbOffer, isManualUpdate, currentUser);
            }
        }
        //uzytkownicy z innymi rolami nie moga edytowac ofert
        throw new ObjectValidationException("User has no privileges to edit bid",
            AUCTION_DA_OFFER_USER_HAS_NO_PRIVILEGES_TO_EDIT_BID);
    }

    // ------------------------------------------- OPEN AUCTION ------------------------------------------------------
    private static AuctionDayAheadOfferDTO updateOfferWhenAuctionIsOpen(AuctionDayAheadOfferDTO modifiedOffer, AuctionDayAheadOfferDTO dbOffer, UserDTO currentUser) {
        //brak mozliwosci edycji pol: acceptedVolume, acceptedPeriod, status, auction, type
        //pola accepted nadpisujemy na taka wartosc jak maja pola bez prefixu 'accepted'
        updateOfferDersInOpenAuction(modifiedOffer, dbOffer, currentUser);
        dbOffer.setVolumeDivisibility(modifiedOffer.getVolumeDivisibility());
        dbOffer.setDeliveryPeriodFrom(modifiedOffer.getDeliveryPeriodFrom());
        dbOffer.setDeliveryPeriodTo(modifiedOffer.getDeliveryPeriodTo());
        dbOffer.setAcceptedDeliveryPeriodFrom(modifiedOffer.getDeliveryPeriodFrom());
        dbOffer.setAcceptedDeliveryPeriodTo(modifiedOffer.getDeliveryPeriodTo());
        dbOffer.setDeliveryPeriodDivisibility(modifiedOffer.getDeliveryPeriodDivisibility());
        return dbOffer;
    }

    private static void updateOfferDersInOpenAuction(AuctionDayAheadOfferDTO modifiedOffer, AuctionDayAheadOfferDTO dbOffer, UserDTO currentUser) {
        Map<Long, AuctionOfferDersDTO> dbOfferDers = groupAuctionOfferByDersId(dbOffer);
        Map<Long, AuctionOfferDersDTO> modifiedOfferDers = groupAuctionOfferByDersId(modifiedOffer);
        addNewDerToOffer(dbOffer, dbOfferDers, modifiedOfferDers);
        updateOfferDerInOpenAuction(dbOffer, dbOfferDers, modifiedOfferDers, currentUser);
    }

    // Metoda ta:
    // * usuwa DERa z oferty,
    // * aktualizucje PASMA przypisane do DERa
    private static void updateOfferDerInOpenAuction(AuctionDayAheadOfferDTO dbOffer, Map<Long, AuctionOfferDersDTO> dbOfferDers, Map<Long, AuctionOfferDersDTO> modifiedOfferDers, UserDTO currentUser) {
        dbOfferDers.forEach((derId, dbAuctionOfferDer) -> {
            if (modifiedOfferDers.containsKey(derId)) {
                AuctionOfferDersDTO modifiedAuctionOfferDer = modifiedOfferDers.get(derId);
                updateBandDataInOpenAuction(dbAuctionOfferDer, modifiedAuctionOfferDer, currentUser);
            } else {
                // usuwanie zlozonych DERow z bazy
                deleteDerFromOfferWithId(derId, dbOffer);
            }
        });
    }

    // Aktualizacja pasm w zlozonych Derze, gdy aukcja jest OTWARTA
    // * Nadpisywanie pol mozliwych do edycji,
    // * Usuwanie pasm z DERa,
    // * Dodawanie nowych pasm
    private static void updateBandDataInOpenAuction(AuctionOfferDersDTO dbAuctionOfferDer, AuctionOfferDersDTO modifiedAuctionOfferDer, UserDTO currentUser) {
        deleteBandFromDer(dbAuctionOfferDer, modifiedAuctionOfferDer);
        addNewBandToDer(dbAuctionOfferDer, modifiedAuctionOfferDer);
        overwritingFieldsInOfferDer(modifiedAuctionOfferDer, dbAuctionOfferDer, currentUser);
    }

    // Gdy aukcja jest OTWARTA, jest możliwość dodania nowego DERa przy modyfikacji oferty modyfikacji.
    private static void addNewBandToDer(AuctionOfferDersDTO dbAuctionOfferDer, AuctionOfferDersDTO modifiedAuctionOfferDer) {
        Set<Integer> dbBandNumbers = dbAuctionOfferDer.getBandData().stream().map(AuctionOfferBandDataDTO::getBandNumber).collect(Collectors.toSet());
        Map<Integer, List<AuctionOfferBandDataDTO>> bandsGroupingByBandNumber = modifiedAuctionOfferDer.getBandData().stream().collect(Collectors.groupingBy(AuctionOfferBandDataDTO::getBandNumber));
        bandsGroupingByBandNumber.forEach((bandNumber, bandsData) -> {
            if (!dbBandNumbers.contains(bandNumber)) {
                bandsData.forEach(b -> {
                    b.setAcceptedVolume(b.getVolume());
                    b.setAcceptedPrice(b.getPrice());
                });
                dbAuctionOfferDer.getBandData().addAll(bandsData);
                log.debug("addNewBandToDer() Add new Band with number: {} to Offer Der with id: {}", bandNumber, dbAuctionOfferDer.getId());
            }
        });
    }

    // Jezeli nie ma danego pasma w bazie, zostaje on dodany
    private static void addNewDerToOffer(AuctionDayAheadOfferDTO dbOffer, Map<Long, AuctionOfferDersDTO> dbOfferDers, Map<Long, AuctionOfferDersDTO> modifiedOfferDers) {
        // gdy zostaje doany nowy der do oferty, zostaje on dodany do bazy
        modifiedOfferDers.forEach((derId, auctionOfferDer) -> {
            if (!dbOfferDers.containsKey(derId)) {
                dbOffer.getDers().add(auctionOfferDer);
                auctionOfferDer.getBandData().forEach(band -> {
                    band.setAcceptedVolume(band.getVolume());
                    band.setAcceptedPrice(band.getPrice());
                });
                log.debug("addNewDerToOffer() Add new Der with id: {} to offer with id: {}", derId, dbOffer.getId());
            }
        });
    }

    // Nadpisywanie mozliwych do modyfikacji pol dla konkretnego pasma i godziny w DERze gdy aukcja jest otwarta
    // Dla Auckji Energy ustawione są dla każdego dera: Dla konkretnej godziny oraz pasama Price, Volume i AcceptedVolume (pole to musi sie rownac Volume)
    private static void overwritingFieldsInOfferDer(AuctionOfferDersDTO modifiedAuctionOfferDer, AuctionOfferDersDTO dbAuctionOfferDer, UserDTO currentUser) {
        // Wartosci dla pasma pogrupowane po numerze pasma i godzinie gieldowej
        Map<Pair<Integer, String>, AuctionOfferBandDataDTO> dbBands = dbAuctionOfferDer.getBandData().stream()
            .map(band -> Pair.of(Pair.of(band.getBandNumber(), band.getHourNumber()), band)).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        Map<Pair<Integer, String>, AuctionOfferBandDataDTO> modifiedBands = modifiedAuctionOfferDer.getBandData().stream()
            .map(band -> Pair.of(Pair.of(band.getBandNumber(), band.getHourNumber()), band)).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
        dbBands.forEach((pairOfBandNrAndHourNr, band) -> {
            if (modifiedBands.containsKey(pairOfBandNrAndHourNr)) {
                if (!currentUser.hasRole(ROLE_DISTRIBUTION_SYSTEM_OPERATOR)) {
                    band.setPrice(modifiedBands.get(pairOfBandNrAndHourNr).getPrice());
                    band.setAcceptedPrice(modifiedBands.get(pairOfBandNrAndHourNr).getPrice());
                }
                band.setVolume(modifiedBands.get(pairOfBandNrAndHourNr).getVolume());
                band.setAcceptedVolume(modifiedBands.get(pairOfBandNrAndHourNr).getVolume());
            } else {
                Integer bandNumber = pairOfBandNrAndHourNr.getFirst();
                String hourNumber = pairOfBandNrAndHourNr.getSecond();
                dbAuctionOfferDer.getBandData()
                    .removeIf(b -> Objects.equals(b.getBandNumber(), bandNumber) && Objects.equals(b.getHourNumber(), hourNumber));
            }
        });
        // Zapis wartości dla nowych godzin przy rozszerzeniu okresu dostawy
        Maps.difference(dbBands, modifiedBands).entriesOnlyOnRight()
            .forEach((pairOfBandNrAndHourNr, band) -> {
                band.setAcceptedPrice(band.getPrice());
                band.setAcceptedVolume(band.getVolume());
                dbAuctionOfferDer.getBandData().add(band);
            });
    }

    protected static boolean isAuctionOpen(AuctionOfferType offerType, AuctionStatus auctionStatus) {
        if (AuctionOfferType.CAPACITY.equals(offerType)) {
            return AuctionStatus.OPEN_CAPACITY.equals(auctionStatus);
        } else if (AuctionOfferType.ENERGY.equals(offerType)) {
            return AuctionStatus.OPEN_ENERGY.equals(auctionStatus);
        }
        throw new IllegalArgumentException("isAuctionOpen() AuctionOfferType not supported: " + offerType);
    }
    // ------------------------------------------- OPEN AUCTION ------------------------------------------------------


    // ------------------------------------------- CLOSED AUCTION ------------------------------------------------------
    protected static boolean isAuctionClosed(AuctionOfferType offerType, AuctionStatus auctionStatus) {
        if (AuctionOfferType.CAPACITY.equals(offerType)) {
            // jeżeli aukcja ma typ CAPACITY_AND_ENERGY
            return AuctionStatus.CLOSED_CAPACITY.equals(auctionStatus);
        } else if (AuctionOfferType.ENERGY.equals(offerType)) {
            return AuctionStatus.CLOSED_ENERGY.equals(auctionStatus);
        }
        throw new IllegalArgumentException("isAuctionClosed() AuctionOfferType not supported: " + offerType);
    }

    private static AuctionDayAheadOfferDTO updateOfferWhenAuctionIsClosed(AuctionDayAheadOfferDTO modifiedOffer, AuctionDayAheadOfferDTO dbOffer, boolean isManualUpdate, UserDTO currentUser) {
        //zaakceptowany wolumen w paśmie zero musi mieć wartość planu pracy dla timestampów w okresie dostawy
        overwriteSelfScheduleAcceptedVolume(modifiedOffer);
        //mozliwosc edycji tylko pol: acceptedVolume, acceptedPeriod oraz usuwania Pasm i Derow z oferty
        if (BooleanUtils.isTrue(dbOffer.getVolumeDivisibility())) {
            deleteDerAndBandInClosedAuction(dbOffer, modifiedOffer);
            Map<Triple<Long, Integer, String>, AuctionOfferBandDataDTO> dbBands = getBandsMap(dbOffer);
            Map<Triple<Long, Integer, String>, AuctionOfferBandDataDTO> modifiedBands = getBandsMap(modifiedOffer);
            updateAcceptedBandInClosedAuction(dbBands, modifiedBands, currentUser);
        }
        if (BooleanUtils.isTrue(dbOffer.getDeliveryPeriodDivisibility())) {
            dbOffer.setAcceptedDeliveryPeriodFrom(modifiedOffer.getAcceptedDeliveryPeriodFrom());
            dbOffer.setAcceptedDeliveryPeriodTo(modifiedOffer.getAcceptedDeliveryPeriodTo());
        }
        if(isManualUpdate){
            dbOffer.getDers().stream()
                .map(AuctionOfferDersDTO::getBandData)
                .flatMap(List::stream)
                .forEach(bandData -> AuctionDayAheadDataUtil.markBandAsEditedUpdateOfferStatus(dbOffer, bandData));
        }
        return dbOffer;
    }

    private static void overwriteSelfScheduleAcceptedVolume(AuctionDayAheadOfferDTO modifiedOffer) {
        var deliveryPeriod = getDeliveryPeriodRange(modifiedOffer.getAcceptedDeliveryPeriodFrom(), modifiedOffer.getAcceptedDeliveryPeriodTo());
        modifiedOffer.getDers().stream()
            .flatMap(der -> der.getBandData().stream())
            .filter(band -> band.getBandNumber() == 0)
            .filter(band -> deliveryPeriodContainsHour(deliveryPeriod, band.getHourNumber()))
            .forEach(band -> band.setAcceptedVolume(band.getVolume()));
    }

    private static void updateAcceptedBandInClosedAuction(Map<Triple<Long, Integer, String>, AuctionOfferBandDataDTO> dbBands,
                                                          Map<Triple<Long, Integer, String>, AuctionOfferBandDataDTO> modifiedBands, UserDTO currentUser) {
        dbBands.entrySet().stream()
            .filter(dbEntry -> modifiedBands.containsKey(dbEntry.getKey()))
            .forEach(dbEntry -> {
                AuctionOfferBandDataDTO dbBand = dbEntry.getValue();
                Triple<Long, Integer, String> key = dbEntry.getKey();
                AuctionOfferBandDataDTO modifiedBand = modifiedBands.get(key);
                dbBand.setAcceptedVolume(modifiedBand.getAcceptedVolume());
                if (!currentUser.hasRole(ROLE_DISTRIBUTION_SYSTEM_OPERATOR)) {
                    dbBand.setAcceptedPrice(modifiedBand.getAcceptedPrice());
                }
            });
    }

    private static Map<Triple<Long, Integer, String>, AuctionOfferBandDataDTO> getBandsMap(AuctionDayAheadOfferDTO offer) {
        Map<Long, AuctionOfferDersDTO> offerDers = groupAuctionOfferByDersId(offer);
        return offerDers.entrySet().stream()
            .map(AuctionDayAheadOfferValidatorUtil::mapDataByDerBandAndHour)
            .flatMap(List::stream)
            .collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private static List<Pair<Triple<Long, Integer, String>, AuctionOfferBandDataDTO>> mapDataByDerBandAndHour(Map.Entry<Long, AuctionOfferDersDTO> der) {
        return der.getValue().getBandData().stream()
            .map(band -> Pair.of(Triple.of(der.getKey(), band.getBandNumber(), band.getHourNumber()), band))
            .collect(Collectors.toList());
    }

    // Gdy auckja jest Zamknieta jest mozliwość:
    // * usuniecia dera z ofery,
    // * usuniecia pasma z dera
    private static void deleteDerAndBandInClosedAuction(AuctionDayAheadOfferDTO dbOffer, AuctionDayAheadOfferDTO modifiedOffer) {
        Map<Long, AuctionOfferDersDTO> dbOfferDers = groupAuctionOfferByDersId(dbOffer);
        Map<Long, AuctionOfferDersDTO> modifiedOfferDers = groupAuctionOfferByDersId(modifiedOffer);
        dbOfferDers.forEach((derId, dbAuctionOfferDer) -> {
            if (modifiedOfferDers.containsKey(derId)) {
                AuctionOfferDersDTO modifiedAuctionOfferDer = modifiedOfferDers.get(derId);
                deleteBandFromDer(dbAuctionOfferDer, modifiedAuctionOfferDer);
            } else {
                deleteDerFromOfferWithId(derId, dbOffer);
            }
        });
    }

    // ------------------------------------------- CLOSED AUCTION ------------------------------------------------------

    // Usuwanie pasma z Dera
    private static void deleteBandFromDer(AuctionOfferDersDTO dbAuctionOfferDer, AuctionOfferDersDTO modifiedAuctionOfferDer) {
        Map<Integer, List<AuctionOfferBandDataDTO>> dbBandsGroupingByBandNumber = dbAuctionOfferDer.getBandData().stream().collect(Collectors.groupingBy(AuctionOfferBandDataDTO::getBandNumber));
        Set<Integer> modifiedOfferDerBandNumbers = modifiedAuctionOfferDer.getBandData().stream().map(AuctionOfferBandDataDTO::getBandNumber).collect(Collectors.toSet());
        dbBandsGroupingByBandNumber.forEach((bandNumber, bandsData) -> {
            if (!modifiedOfferDerBandNumbers.contains(bandNumber)) {
                dbAuctionOfferDer.getBandData().stream()
                    .filter(bandData -> bandData.getBandNumber() == bandNumber)
                    .forEach(band -> {
                        band.setAcceptedVolume(null);
                        band.setAcceptedPrice(null);
                    });
                log.debug("deleteBandFromDer() Delete Band with number: {} from Offer Der with id: {}", bandNumber, dbAuctionOfferDer.getId());
            }
        });
    }

    // Usuwanie Dera z Oferty
    private static void deleteDerFromOfferWithId(Long derId, AuctionDayAheadOfferDTO dbOffer) {
        // usuwanie zlozonych DERow z bazy
        dbOffer.getDers().removeIf(der -> der.getDer().getId().equals(derId));
        log.debug("updateOfferDerInOpenAuction() Delete Offer der with id: {} from Offer with id: {}", derId, dbOffer.getId());
    }

    private static Map<Long, AuctionOfferDersDTO> groupAuctionOfferByDersId(AuctionDayAheadOfferDTO dbOffer) {
        return dbOffer.getDers().stream().map(der -> Pair.of(der.getDer().getId(), der)).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }
}

