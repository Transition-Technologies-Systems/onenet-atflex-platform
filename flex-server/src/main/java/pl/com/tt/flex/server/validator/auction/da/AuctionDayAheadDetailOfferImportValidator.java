package pl.com.tt.flex.server.validator.auction.da;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.der.DerMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferSchedulingUnitDTO;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus.*;
import static pl.com.tt.flex.server.dataexport.exporter.offer.util.OfferExportUtils.getAcceptedPriceForSuSection;
import static pl.com.tt.flex.server.dataexport.exporter.offer.util.OfferExportUtils.getAcceptedVolumeForBandAndTimestamp;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Component
@AllArgsConstructor
@Slf4j
public class AuctionDayAheadDetailOfferImportValidator {

    AuctionDayAheadService auctionDayAheadService;

    public void checkValid(String bidIdToCheck, List<AuctionOfferSchedulingUnitDTO> schedulingUnitVolumes) throws ObjectValidationException, JsonProcessingException {
        long bidId = checkAndGetBidId(bidIdToCheck);
        validBidId(bidId);
        AuctionDayAheadOfferDTO dbOffer = getOfferDTO(bidId);
        validAuctionStatus(dbOffer);
        validBidStatus(dbOffer);
        checkAllOffersUpdated(dbOffer);
        validVolumes(dbOffer, schedulingUnitVolumes);
        validPrices(dbOffer, schedulingUnitVolumes);
    }

    /**
     * Jeśli którakolwiek oferta zawiera dera bez żadnego zaktualizowanego volumenu, zwraca komunikat błędu
     * wraz z danymi problematycznego dera(ów) i ofert(y)
     */
    public void checkAllOffersUpdated(AuctionDayAheadOfferDTO dbOffer) throws JsonProcessingException {
        Map<DerMinDTO, Set<AuctionDayAheadOfferDTO>> notUpdatedOffersByDer = getNotUpdatedOffersByDer(dbOffer);
        if (notUpdatedOffersByDer.size() > 0) {
            notifyNotUpdatedVolume(notUpdatedOffersByDer);
        }
    }

    private void notifyNotUpdatedVolume(Map<DerMinDTO, Set<AuctionDayAheadOfferDTO>> notUpdatedOffersByDer) throws JsonProcessingException {
        Map<String, String> notificationParams = new HashMap<>();
        notificationParams.put("derNames", getDerNamesCommaSeparated(notUpdatedOffersByDer.keySet()));
        notificationParams.put("offerIds", getOfferIdsCommaSeparated(notUpdatedOffersByDer.values()));
        ObjectMapper objectMapper = new ObjectMapper();
        String errorParam = objectMapper.writeValueAsString(notificationParams);
        if (notUpdatedOffersByDer.size() == 1) {
            throw new ObjectValidationException("Der has not updated volumes!", AUCTION_DA_OFFER_NOT_UPDATED_DER_PRESENT, errorParam);
        }
        throw new ObjectValidationException("Ders have not updated volumes!", AUCTION_DA_OFFER_SEVERAL_NOT_UPDATED_DERS_PRESENT, errorParam);
    }

    private Map<DerMinDTO, Set<AuctionDayAheadOfferDTO>> getNotUpdatedOffersByDer(AuctionDayAheadOfferDTO dbOffer) {
        Map<DerMinDTO, Set<AuctionDayAheadOfferDTO>> notUpdatedOffersByDer = new HashMap<>();
        checkVolumesVerified(dbOffer);
        dbOffer.getDers().stream()
            .filter(offerDer -> !isOfferDerUpdated(offerDer))
            .forEach(offerDer -> addNotUpdatedOfferToMap(notUpdatedOffersByDer, dbOffer, offerDer.getDer()));
        return notUpdatedOffersByDer;
    }

    private void checkVolumesVerified(AuctionDayAheadOfferDTO offer) {
        if (!Set.of(VOLUMES_VERIFIED, ACCEPTED).contains(offer.getStatus())) {
            throw new ObjectValidationException("Can only import DA offers with status Volumes Verified", AUCTION_DA_OFFER_CAN_ONLY_IMPORT_WITH_STATUS_VOLUMES_VERIFIED);
        }
    }

    private void addNotUpdatedOfferToMap(Map<DerMinDTO, Set<AuctionDayAheadOfferDTO>> notUpdatedOffersByDer, AuctionDayAheadOfferDTO offer, DerMinDTO der) {
        if (notUpdatedOffersByDer.containsKey(der)) {
            notUpdatedOffersByDer.get(der).add(offer);
        } else {
            Set<AuctionDayAheadOfferDTO> notUpdatedOffersForDer = new HashSet<>();
            notUpdatedOffersForDer.add(offer);
            notUpdatedOffersByDer.put(der, notUpdatedOffersForDer);
        }
    }

    private boolean isOfferDerUpdated(AuctionOfferDersDTO offerDer) {
        return offerDer.getBandData().stream()
            .anyMatch(AuctionOfferBandDataDTO::isEdited);
    }

    private String getDerNamesCommaSeparated(Set<DerMinDTO> problematicDers) {
        return problematicDers.stream()
            .map(DerMinDTO::getName)
            .collect(Collectors.joining(", "));
    }

    private String getOfferIdsCommaSeparated(Collection<Set<AuctionDayAheadOfferDTO>> problematicOffers) {
        return problematicOffers.stream()
            .flatMap(Set::stream)
            .map(AuctionDayAheadOfferDTO::getId)
            .distinct()
            .map(Object::toString)
            .collect(Collectors.joining(", "));
    }

    private void validAuctionStatus(AuctionDayAheadOfferDTO dbOffer) throws ObjectValidationException {
        AuctionStatus auctionStatus = auctionDayAheadService.findAuctionStatusById(dbOffer.getAuctionDayAhead().getId());
        if (AuctionStatus.getOpenAuctionStatuses().contains(auctionStatus)) {
            log.debug("validAuctionStatus() Could not import bid with ID: {}, because auction with id {} is OPEN",
                dbOffer.getId(), dbOffer.getAuctionDayAhead().getId());
            throw new ObjectValidationException("Could not import offer because auction is OPEN", IMPORT_OFFER_COULD_NOT_IMPORT_BECAUSE_AUCTION_IS_OPEN);
        }
    }

    private void validBidStatus(AuctionDayAheadOfferDTO dbOffer) throws ObjectValidationException {
        List<AuctionOfferStatus> blockingModificationStatuses = Arrays.asList(REJECTED, ACCEPTED);
        if (blockingModificationStatuses.contains(dbOffer.getStatus())) {
            log.debug("validBidStatus() Could not import bid={} with status: {}", dbOffer.getId(), dbOffer.getStatus());
            throw new ObjectValidationException("Cannot import bid with status " + dbOffer.getStatus(), IMPORT_CANNOT_IMPORT_OFFER_WITH_STATUS_REJECTED_OR_ACCEPTED);
        }
        List<AuctionOfferStatus> permittedStatuses = Arrays.asList(AuctionOfferStatus.values());
        if (!permittedStatuses.contains(dbOffer.getStatus())) {
            log.debug("validBidStatus() Could not import bid={} with status: {}", dbOffer.getId(), dbOffer.getStatus());
            throw new ObjectValidationException("Could not identify name of status " + dbOffer.getStatus(), IMPORT_COULD_NOT_IDENTIFY_NAME_OF_STATUS);
        }
    }

    private void validPrices(AuctionDayAheadOfferDTO dbOffer, List<AuctionOfferSchedulingUnitDTO> schedulingUnitVolumes) throws ObjectValidationException {
        for (AuctionOfferSchedulingUnitDTO schedulingUnitVolume : schedulingUnitVolumes) {
            if (isChangedPrice(dbOffer, schedulingUnitVolume)) {
                log.debug("validBidId() Could not change price volume: {}", dbOffer.getId());
                throw new ObjectValidationException("Cannot change scheduling unit prices", IMPORT_CANNOT_CHANGE_SCHEDULING_UNIT_PRICE);
            }
        }
    }

    private void validVolumes(AuctionDayAheadOfferDTO dbOffer, List<AuctionOfferSchedulingUnitDTO> schedulingUnitVolumes) throws ObjectValidationException {
        for (AuctionOfferSchedulingUnitDTO schedulingUnitVolume : schedulingUnitVolumes) {
            Optional<BigDecimal> volumeOpt = checkAndGetVolume(schedulingUnitVolume.getVolume());
            if (volumeOpt.isPresent() && !isValidDigitAndScale(volumeOpt.get())) {
                log.debug("validVolumes() Wrong format of scheduling unit volume: bidId={}, volume={}", dbOffer.getId(), volumeOpt.get());
                throw new ObjectValidationException("Invalid volume value", IMPORT_INVALID_SCHEDULING_UNIT_VOLUME);
            }
            if (schedulingUnitVolume.getBandNumber() == 0 && isChangeVolume(dbOffer, schedulingUnitVolume)) {
                log.debug("validVolumes() Could not change zero band volume: {}", dbOffer.getId());
                throw new ObjectValidationException("Cannot change scheduling unit zero band volumes", IMPORT_CANNOT_CHANGE_SU_SELF_SCHEDULE_VOLUMES);
            }
            if (!isImportVolumeLowerThanDbVolume(dbOffer, schedulingUnitVolume)) {
                log.debug("validVolumes() Import acceptedVolume greater than dbAcceptedVolume for offer with id: {}", dbOffer.getId());
                throw new ObjectValidationException("Import acceptedVolume greater than dbAcceptedVolume", IMPORT_SU_VOLUME_CANNOT_BE_GREATER_THAN_INITIAL_SU_VOLUME_OF_OFFER);
            }
        }
    }

    private void validBidId(Long bidId) throws ObjectValidationException {
        Optional<AuctionDayAheadOfferDTO> dbOfferOpt = auctionDayAheadService.findOfferById(bidId);
        if (dbOfferOpt.isEmpty()) {
            log.debug("validBidId() Could not find bid with ID: {}", bidId);
            throw new ObjectValidationException("Could not find matching id", IMPORT_OFFER_COULD_NOT_FIND_MATCHING_ID);
        }
    }

    private Optional<BigDecimal> checkAndGetVolume(String volumeToParse) throws ObjectValidationException {
        try {
            if (Objects.nonNull(volumeToParse)) {
                return Optional.of(new BigDecimal(volumeToParse.replaceAll(",", ".")));
            }
        } catch (Exception e) {
            log.debug("parseVolume() Invalid volume value: {}", volumeToParse);
            throw new ObjectValidationException("Invalid volume value", IMPORT_INVALID_SCHEDULING_UNIT_VOLUME);
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> checkAndGetPrice(String priceToParse) throws ObjectValidationException {
        try {
            if (Objects.nonNull(priceToParse)) {
                return Optional.of(new BigDecimal(priceToParse.replaceAll(",", ".")));
            }
        } catch (Exception e) {
            log.debug("parsePrice() Invalid price value: {}", priceToParse);
            throw new ObjectValidationException("Invalid price value", IMPORT_INVALID_SCHEDULING_UNIT_PRICE);
        }
        return Optional.empty();
    }

    private boolean isValidDigitAndScale(@NotNull BigDecimal value) {
        return value.scale() <= 3 && getIntegerDigits(value) <= 10;
    }

    public long checkAndGetBidId(String idToParse) throws ObjectValidationException {
        try {
            return Long.parseLong(idToParse.replace("up", ""));
        } catch (Exception e) {
            log.debug("getBidId()) Invalid bid id: {}", idToParse);
            throw new ObjectValidationException("Invalid bid id", IMPORT_OFFER_COULD_NOT_FIND_MATCHING_ID);
        }
    }

    private boolean isImportVolumeLowerThanDbVolume(AuctionDayAheadOfferDTO dbOffer, AuctionOfferSchedulingUnitDTO schedulingUnitVolume) throws ObjectValidationException {
        Optional<BigDecimal> acceptedVolume = getAcceptedVolumeForBandAndTimestamp(dbOffer, schedulingUnitVolume.getBandNumber(), schedulingUnitVolume.getTimestamp());
        Optional<BigDecimal> importedVolumeOpt = checkAndGetVolume(schedulingUnitVolume.getVolume());
        boolean importVolumeIsGreaterThanDbVolume = acceptedVolume.isPresent() && importedVolumeOpt.isPresent() && importedVolumeOpt.get().compareTo(acceptedVolume.get()) > 0;
        return importedVolumeOpt.isEmpty() || !importVolumeIsGreaterThanDbVolume;
    }

    private boolean isChangeVolume(AuctionDayAheadOfferDTO dbOffer, AuctionOfferSchedulingUnitDTO schedulingUnitVolume) throws ObjectValidationException {
        Optional<BigDecimal> acceptedVolume = getAcceptedVolumeForBandAndTimestamp(dbOffer, schedulingUnitVolume.getBandNumber(), schedulingUnitVolume.getTimestamp());
        Optional<BigDecimal> importedVolumeOpt = checkAndGetVolume(schedulingUnitVolume.getVolume());
        boolean bothNotPresent = acceptedVolume.isEmpty() && importedVolumeOpt.isEmpty();
        boolean hasTheSameValue = acceptedVolume.isPresent() && importedVolumeOpt.isPresent() && acceptedVolume.get().compareTo(importedVolumeOpt.get()) == 0;
        return !(bothNotPresent || hasTheSameValue);
    }

    private boolean isChangedPrice(AuctionDayAheadOfferDTO dbOffer, AuctionOfferSchedulingUnitDTO schedulingUnitVolume) throws ObjectValidationException {
        Optional<BigDecimal> acceptedPriceOpt = getAcceptedPriceForSuSection(dbOffer, schedulingUnitVolume.getBandNumber(), schedulingUnitVolume.getTimestamp());
        Optional<BigDecimal> importedAcceptedPriceOpt = checkAndGetPrice(schedulingUnitVolume.getPrice());
        boolean bothNotPresent = acceptedPriceOpt.isEmpty() && importedAcceptedPriceOpt.isEmpty();
        boolean hasTheSameValue = acceptedPriceOpt.isPresent() && importedAcceptedPriceOpt.isPresent() && acceptedPriceOpt.get().compareTo(importedAcceptedPriceOpt.get()) == 0;
        return !(bothNotPresent || hasTheSameValue);
    }

    /**
     * Wylicza liczbę miejsc przed przecinkiem
     */
    private long getIntegerDigits(BigDecimal power) {
        BigDecimal n = power.stripTrailingZeros();
        return n.signum() == 0 ? 1 : n.precision() - n.scale();
    }

    private AuctionDayAheadOfferDTO getOfferDTO(Long bidId) {
        return auctionDayAheadService.findOfferById(bidId)
            .orElseThrow(() -> new RuntimeException("AuctionDayAheadOffer not found with id: " + bidId));
    }
}
