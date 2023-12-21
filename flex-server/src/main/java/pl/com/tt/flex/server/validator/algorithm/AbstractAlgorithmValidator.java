package pl.com.tt.flex.server.validator.algorithm;

import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationConfigDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelMinimalDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampsMinimalDTO;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.web.rest.algorithm.resource.FlexAgnoAlgorithmResource;

import java.util.*;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

public abstract class AbstractAlgorithmValidator {

    private final FlexAgnoAlgorithmResource flexAgnoAlgorithmResource;

    protected AbstractAlgorithmValidator(FlexAgnoAlgorithmResource flexAgnoAlgorithmResource) {
        this.flexAgnoAlgorithmResource = flexAgnoAlgorithmResource;
    }

    public void checkKdmModel(AlgorithmEvaluationConfigDTO configDTO, Set<AuctionDayAheadOfferDTO> offers) throws ObjectValidationException {
        Optional<KdmModelMinimalDTO> kdmFileMinimal = flexAgnoAlgorithmResource.getKdmFileMinimal(configDTO.getKdmModelId());
        if (kdmFileMinimal.isEmpty()) {
            throw new ObjectValidationException("The algorithm cannot be run because kdmModel is not exist",
                AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_KDM_MODEL_IS_NOT_EXIST);
        }
        KdmModelMinimalDTO kdmModelMinimalDTO = kdmFileMinimal.get();
        checkLackingKdmModelForTimestamp(offers, kdmModelMinimalDTO);
    }

    private void checkLackingKdmModelForTimestamp(Set<AuctionDayAheadOfferDTO> offers, KdmModelMinimalDTO kdmModelMinimalDTO) throws ObjectValidationException {
        List<String> kdmTimestamps = kdmModelMinimalDTO.getTimestamps().stream().map(KdmModelTimestampsMinimalDTO::getTimestamp).collect(Collectors.toList());
        Set<String> offerTimestamps = getOffersTimestamps(offers);
        List<String> lackingKdmModelTimestamps = new ArrayList<>();
        for (String offerTimestamp : offerTimestamps) {
            if(!kdmTimestamps.contains(offerTimestamp)) {
                lackingKdmModelTimestamps.add(offerTimestamp);
            }
        }
        if(!lackingKdmModelTimestamps.isEmpty()) {
            throw new ObjectValidationException("Lacking KDM model for timestamps " + lackingKdmModelTimestamps,
                AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_LACKING_KDM_MODEL_FOR_TIMESTAMP, String.join(", ", lackingKdmModelTimestamps));
        }
    }

    private Set<String> getOffersTimestamps(Set<AuctionDayAheadOfferDTO> offers) {
        return offers.stream()
            .flatMap(offer -> offer.getDers().stream())
            .flatMap(der -> der.getBandData().stream())
            .filter(band -> Objects.nonNull(band.getVolume()) && Objects.nonNull(band.getPrice()))
            .map(AuctionOfferBandDataDTO::getHourNumber).collect(Collectors.toSet());
    }

    /**
     * Błąd rzucamy jedynie w przypadku, gdy DER ma przypisany POC With LV niezawarty w modelu KDM
     * i jednocześnie przypisana jest poprawna stacja zasilająca
     * (gdy obie walidacje nie są spełnione DER po prostu nie jest uwzględniany w algorytmie)
     */
    public boolean isValidDERPowerStationsAndPointsOfConnectionWithLV(UnitDTO der, List<String> kdmPoints, boolean isLvModel) {
        boolean pointsOfConnectionWithLVCorrect = isLvModel && isDerHasCorrectPointsOfConnectionWithLV(der, kdmPoints);
        boolean powerStationsCorrect = isDerHasCorrectPowerStations(der, kdmPoints);
        boolean hasConnectionToLV = der.getPointOfConnectionWithLvTypes().size() > 0;
        return (pointsOfConnectionWithLVCorrect || (powerStationsCorrect && !hasConnectionToLV)) || (powerStationsCorrect && !isLvModel);
    }

    /**
     * Sprawdzenie czy dany DER zawiera point of connection with LV lub nr stacji przyłączeniowej.
     * Wyrzuca wyjątek gdy nie znajduje żadnej z tych rzeczy, mimo że istnieją one w modelu KDM.
     */
    public boolean isDerContainsPOCWithLVOrPowerStation(UnitDTO der) throws ObjectValidationException {
        boolean derHasNoPOCWithLV = der.getPointOfConnectionWithLvTypes().size() == 0;
        boolean derHasNoPowerStation = der.getPowerStationTypes().size() == 0;
        return !(derHasNoPOCWithLV && derHasNoPowerStation);
    }

    /**
     * Sprawdzenie czy punkty przyłączenia w DERze znajdują się w modelu KDM
     */
    public boolean isDerHasCorrectPointsOfConnectionWithLV(UnitDTO der, List<String> kdmPoints) throws ObjectValidationException {
        return der.getPointOfConnectionWithLvTypes().stream()
            .map(LocalizationTypeDTO::getName)
            .allMatch(kdmPoints::contains)
            && (der.getPointOfConnectionWithLvTypes().stream()
            .map(LocalizationTypeDTO::getName).findAny().isPresent());
    }

    /**
     * Sprawdzenie czy nr stacji zasilania w DERze znajdują się w modelu KDM
     */
    public boolean isDerHasCorrectPowerStations(UnitDTO der, List<String> kdmPowerStations) throws ObjectValidationException {
        return der.getPowerStationTypes().stream()
            .map(LocalizationTypeDTO::getName)
            .allMatch(kdmPowerStations::contains)
            && (der.getPowerStationTypes().stream()
            .map(LocalizationTypeDTO::getName).findAny().isPresent());
    }

    public boolean isDerHasCorrectPowerStationButIncorrectPointOfConnectionWithLV(UnitDTO der, List<String> kdmStations) {
        return isDerHasCorrectPowerStations(der, kdmStations) && !isDerHasCorrectPointsOfConnectionWithLV(der, kdmStations);
    }

    /**
     * Sprawdzenie czy są nieprawidłowe DERy podczas procesu walidacji (dla algorytmów PBCM i DANO).
     */
    public void checkInvalidDers(List<String> validUnitNames, List<String> invalidUnitNames, boolean isLvModel) {
        long validDerSize = validUnitNames.stream().distinct().count();
        long invalidDerSize = invalidUnitNames.stream().distinct().count();
        String invalidDerString = invalidUnitNames.stream().distinct().collect(Collectors.joining(", "));
        if ((validDerSize == 0 && invalidDerSize > 1) || (isLvModel && invalidDerSize > 1)) {
            throw new ObjectValidationException("DERs have points of connection with LV, but cannot be found in chosen KDM model.",
                PBCM_DANO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_DERS_CANNOT_BE_FOUND_IN_CHOSEN_KDM_MODEL_DESPITE_HAVING_CONNECTION_WITH_POC_WITH_LV, invalidDerString);
        } else if ((validDerSize == 0 && invalidDerSize == 1) || (isLvModel && invalidDerSize == 1)) {
            throw new ObjectValidationException("DER has point of connection with LV, but cannot be found in chosen KDM model.",
                PBCM_DANO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_DER_CANNOT_BE_FOUND_IN_CHOSEN_KDM_MODEL_DESPITE_HAVING_CONNECTION_WITH_POC_WITH_LV, invalidDerString);
        }
    }

}
