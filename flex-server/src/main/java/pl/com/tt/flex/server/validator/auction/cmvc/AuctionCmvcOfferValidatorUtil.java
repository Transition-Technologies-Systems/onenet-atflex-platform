package pl.com.tt.flex.server.validator.auction.cmvc;


import org.apache.commons.lang3.BooleanUtils;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.offer.cmvc.AuctionCmvcOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.service.user.dto.UserDTO;

import java.util.Set;

public class AuctionCmvcOfferValidatorUtil {

    public static final Set<Role> modifyOpenAuctionRoles = Set.of(
        Role.ROLE_ADMIN,
        Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR,
        Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR,
        Role.ROLE_FLEX_SERVICE_PROVIDER,
        Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED
    );

    public static final Set<Role> modifyClosedAuctionRoles = Set.of(
        Role.ROLE_ADMIN,
        Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR,
        Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR
    );

    //oferty moga edytowac uzytkownicy z rolami FSP, FSPA, TA(Admin), TSO i DSO
    //maja oni w swoich kontenerach uprawnienia: FLEX_USER_AUCTIONS_CMVC_OFFER_EDIT / FLEX_ADMIN_AUCTIONS_CMVC_OFFER_EDIT
    //pola ktore uzytkownik moze edytowac zaleza od roli uzytkownika i statusu aukcji
    //metoda zwraca DTO edytowanej oferty pobranej z bazy danych, nadpisane o te pola z formularza edycji, ktore aktualny uzytkownik moze edytowac
    public static AuctionCmvcOfferDTO overwriteOnlyAllowedOfferDtoFieldsForCurrentUser(AuctionCmvcOfferDTO modifiedOffer, AuctionStatus auctionStatus, AuctionCmvcOfferDTO dbOffer,
                                                                                       UserDTO currentUser) {
        if (isAuctionOpen(auctionStatus)) {
            if (currentUser.hasAnyRole(modifyOpenAuctionRoles)) {
                //brak mozliwosci edycji pol: acceptedVolume, acceptedPeriod, status, auction, type
                //pola accepted nadpisujemy na taka wartosc jak maja pole bez prefixu 'accepted'
                dbOffer.setPrice(modifiedOffer.getPrice());
                dbOffer.setVolume(modifiedOffer.getVolume());
                dbOffer.setAcceptedVolume(modifiedOffer.getVolume());
                dbOffer.setVolumeDivisibility(modifiedOffer.getVolumeDivisibility());
                dbOffer.setDeliveryPeriodFrom(modifiedOffer.getDeliveryPeriodFrom());
                dbOffer.setDeliveryPeriodTo(modifiedOffer.getDeliveryPeriodTo());
                dbOffer.setAcceptedDeliveryPeriodFrom(modifiedOffer.getDeliveryPeriodFrom());
                dbOffer.setAcceptedDeliveryPeriodTo(modifiedOffer.getDeliveryPeriodTo());
                dbOffer.setDeliveryPeriodDivisibility(modifiedOffer.getDeliveryPeriodDivisibility());
                return dbOffer;
            }
        } else if (isAuctionClosed(auctionStatus)) {
            if (currentUser.hasAnyRole(modifyClosedAuctionRoles)) {
                //mozliwosc edycji tylko pol: acceptedVolume, acceptedPeriod
                if (BooleanUtils.isTrue(dbOffer.getVolumeDivisibility())) {
                    dbOffer.setAcceptedVolume(modifiedOffer.getAcceptedVolume());
                }
                if (BooleanUtils.isTrue(dbOffer.getDeliveryPeriodDivisibility())) {
                    dbOffer.setAcceptedDeliveryPeriodFrom(modifiedOffer.getAcceptedDeliveryPeriodFrom());
                    dbOffer.setAcceptedDeliveryPeriodTo(modifiedOffer.getAcceptedDeliveryPeriodTo());
                }
                //dodatkowo admin może modyfikować pola bez prefixu 'accepted'
                if (currentUser.hasRole(Role.ROLE_ADMIN)) {
                    dbOffer.setPrice(modifiedOffer.getPrice());
                    dbOffer.setVolume(modifiedOffer.getVolume());
                    dbOffer.setVolumeDivisibility(modifiedOffer.getVolumeDivisibility());
                    dbOffer.setDeliveryPeriodFrom(modifiedOffer.getDeliveryPeriodFrom());
                    dbOffer.setDeliveryPeriodTo(modifiedOffer.getDeliveryPeriodTo());
                    dbOffer.setDeliveryPeriodDivisibility(modifiedOffer.getDeliveryPeriodDivisibility());
                    dbOffer.setFlexPotential(modifiedOffer.getFlexPotential());
                }

                return dbOffer;
            }
        }
        //uzytkownicy z innymi rolami nie moga edytowac ofert
        return null;
    }

    protected static boolean isAuctionOpen(AuctionStatus auctionStatus) {
        return AuctionStatus.OPEN.equals(auctionStatus);
    }

    protected static boolean isAuctionClosed(AuctionStatus auctionStatus) {
        return AuctionStatus.CLOSED.equals(auctionStatus);
    }
}

