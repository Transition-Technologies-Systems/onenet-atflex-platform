package pl.com.tt.flex.server.service.importData.auctionOffer;

import static pl.com.tt.flex.server.domain.enumeration.NotificationEvent.BID_IMPORT;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.IMPORTED_BIDS;
import static pl.com.tt.flex.server.domain.enumeration.NotificationParam.NOT_IMPORTED_BIDS;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.IMPORT_OFFERS_TEMPLATE_INCORRECT;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.dataimport.factory.DataImportFactory;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.domain.enumeration.NotificationParam;
import pl.com.tt.flex.server.service.importData.auctionOffer.cmvc.AuctionCmvcOfferImportService;
import pl.com.tt.flex.server.service.importData.auctionOffer.da.AuctionDaOfferImportService;
import pl.com.tt.flex.server.service.importData.auctionOffer.da.AuctionDaSetoOfferImportService;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportData;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferSchedulingUnitDTO;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferSetoImportData;
import pl.com.tt.flex.server.service.notification.dto.NotificationParamValue;
import pl.com.tt.flex.server.service.notification.factory.NotifierFactory;
import pl.com.tt.flex.server.service.notification.util.NotificationUtils;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuctionOfferImportServiceImpl implements AuctionOfferImportService {

    private final DataImportFactory dataImportFactory;
    private final AuctionCmvcOfferImportService cmvcOfferImportService;
    private final AuctionDaOfferImportService daOfferImportService;
    private final AuctionDaSetoOfferImportService setoOfferImportService;
    private final UserService userService;
    private final NotifierFactory notifierFactory;

    public AuctionOfferImportDataResult importCmvcData(MultipartFile file) throws ObjectValidationException {
        log.debug("importCmvcData() Start - import auctions offer");
        try {
            List<AuctionOfferImportData> cmvcBids = getBids(AuctionOfferImportData.class, file);
            AuctionOfferImportDataResult importResult = cmvcOfferImportService.importBids(cmvcBids);
            log.debug("importCmvcData() End - import auctions offer");
            return importResult;
        } catch (Exception e) {
            log.debug("importCmvcData() Problem with import. Ex msg: {}", e.getMessage());
            throw new ObjectValidationException("Invalid template", IMPORT_OFFERS_TEMPLATE_INCORRECT);
        }
    }

    public AuctionOfferImportDataResult importPbcmDanoData(MultipartFile file) throws ObjectValidationException {
        log.debug("importPbcmDanoData() Start - import auctions offer");
        try {
            List<AuctionOfferSchedulingUnitDTO> daBids = getBids(AuctionOfferSchedulingUnitDTO.class, file);
            AuctionOfferImportDataResult importResult = daOfferImportService.importBids(daBids);
            log.debug("importPbcmDanoData() End - import auctions offer");
            return importResult;
        } catch (IOException | ImportDataException e) {
            log.debug("importPbcmDanoData() Problem with import. Ex msg: {}", e.getMessage());
            throw new ObjectValidationException("Invalid template", IMPORT_OFFERS_TEMPLATE_INCORRECT);
        }
    }

    public AuctionOfferImportDataResult importSetoData(MultipartFile file) {
        log.debug("importSetoData() Start - import auctions offer");
        try {
            List<AuctionOfferSetoImportData> daBids = getBids(AuctionOfferSetoImportData.class, file);
            AuctionOfferImportDataResult importResult = setoOfferImportService.importBids(daBids);
            log.debug("importSetoData() End - import auctions offer");
            return importResult;
        } catch (IOException | ImportDataException e) {
            log.debug("importSetoData() Problem with import. Ex msg: {}", e.getMessage());
            throw new ObjectValidationException("Invalid template", IMPORT_OFFERS_TEMPLATE_INCORRECT);
        }
    }

    @Override
    @Transactional
    public void sendNotificationAboutImportOffer(AuctionOfferImportDataResult result) {
        try {
            UserDTO userDTO = userService.getCurrentUserDTO().orElseThrow(() -> new RuntimeException("Current logged-in user not found"));
            Map<NotificationParam, NotificationParamValue> notificationParams = getNotificationParams(result);
            // Informacje o imporcie dostaje tylko uzytkownik, ktory importuje dane
            List<MinimalDTO<Long, String>> usersToBeNotified = userService.getUsersByLogin(Set.of(userDTO.getLogin()));
            NotificationUtils.registerNewNotificationForSpecifiedUsers(notifierFactory, BID_IMPORT, notificationParams, usersToBeNotified);
        } catch (Exception e) {
            log.debug("sendNotificationAboutImportOffer() Cannot send notification about imported offers, resultDTO: {}", result);
            e.printStackTrace();
        }
    }

    /**
     * Dla pomyslnie zaimportowanych ofert ustawiany jest parametr, ktory jako value trzyma
     * id'ki zaimportowanych ofert
     * <p>
     * Oferty ktore nie zostaly pomyslnie zaimportowane, zostaja zapisywane w json'ie
     * Informacje o blednym imporcie trzymana jest w nastepujacy sposob: "id_ofert" : "kod bledu"
     * np. [{"1":"error.auction.offer.import.other"}, {"2":"error.auction.offer.import.other"}]
     */
    public static Map<NotificationParam, NotificationParamValue> getNotificationParams(AuctionOfferImportDataResult result) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String importedBidsStr = CollectionUtils.isEmpty(result.getImportedBids()) ?
            null : result.getImportedBids().stream().map(Object::toString).collect(Collectors.joining(", "));
        String notImportedBidsStr = CollectionUtils.isEmpty(result.getNotImportedBids()) ?
            null : objectMapper.writeValueAsString(result.getNotImportedBids());
        return NotificationUtils.ParamsMapBuilder.create()
            .addParam(IMPORTED_BIDS, importedBidsStr)
            .addObjectsParam(NOT_IMPORTED_BIDS, notImportedBidsStr)
            .build();
    }

    private List getBids(Class dataClass, MultipartFile file) throws IOException, ImportDataException {
        Locale locale = Locale.forLanguageTag(userService.getLangKeyForCurrentLoggedUser());
        return dataImportFactory.getDataImport(dataClass, DataImportFormat.XLSX)
            .doImport(file, locale);
    }

}
