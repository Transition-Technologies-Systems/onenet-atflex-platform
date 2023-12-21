package pl.com.tt.flex.server.validator.algorithm.agnoAlgorithm;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationConfigDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.algorithm.AlgorithmEvaluationService;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.product.forecastedPrices.ForecastedPricesService;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.algorithm.AbstractAlgorithmValidator;
import pl.com.tt.flex.server.web.rest.algorithm.resource.FlexAgnoAlgorithmResource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType.BM;
import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType.PBCM;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Component
public class AgnoAlgorithmValidator extends AbstractAlgorithmValidator {

    private final ForecastedPricesService forecastedPricesService;
    private final AuctionDayAheadService auctionDayAheadService;
    private final AlgorithmEvaluationService algorithmEvaluationService;

    public AgnoAlgorithmValidator(ForecastedPricesService forecastedPricesService, AuctionDayAheadService auctionDayAheadService, AlgorithmEvaluationService algorithmEvaluationService, FlexAgnoAlgorithmResource flexAgnoAlgorithmResource) {
        super(flexAgnoAlgorithmResource);
        this.forecastedPricesService = forecastedPricesService;
        this.auctionDayAheadService = auctionDayAheadService;
        this.algorithmEvaluationService = algorithmEvaluationService;
    }

    public void checkCancel(long evaluationId) throws ObjectValidationException {
        AlgorithmEvaluationDTO algorithmEvaluationDTO = algorithmEvaluationService.findById(evaluationId)
            .orElseThrow(() -> new IllegalStateException("Cannot found algorithm evaluation with id: " + evaluationId));
        List<AlgorithmStatus> statusesToCancel = List.of(AlgorithmStatus.EVALUATING, AlgorithmStatus.KDM_MODEL_UPDATING);
        if (!statusesToCancel.contains(algorithmEvaluationDTO.getStatus())) {
            throw new ObjectValidationException("Cannot cancel not running algorithm",
                AGNO_ALGORITHM_CANNOT_CANCEL_NOT_RUNNING_ALGORITHM);
        }
    }

    public void checkValid(Set<AuctionDayAheadOfferDTO> offers, AlgorithmEvaluationConfigDTO configDTO) throws ObjectValidationException {
        checkAlgorithmEvaluationConfig(configDTO);
        checkOffers(offers);
        checkKdmModel(configDTO, offers);
        if (configDTO.getAlgorithmType().equals(PBCM)) {
            checkForecastedPrices(offers, configDTO.getDeliveryDate());
        }
    }

    public void checkInputFile(FileDTO fileDTO) throws ObjectValidationException {
        List<FileDTO> files = ZipUtil.zipToFiles(fileDTO.getBytesData());
        if (CollectionUtils.isEmpty(files)) {
            throw new ObjectValidationException("The AGNO algorithm cannot be run because no bids have been submitted",
                AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NO_BIDS_HAVE_BEEN_SUBMITTED);
        }
    }

    /**
     * Sprawdzenie czy wszystkie DERy mają swoje punkty przyłączenia do sieci niskiego napięcia lub
     * numery ID stacji zasilającej i są zgodne ze swoimi modelami KDM.
     * @throws ObjectValidationException gdy nie wszystkie DERy mają punkty przyłączenia do nn lub nr stacji zasilającej
     */
    public void checkIfAllDersHavePOCsWithLvOrPowerStationIds(List<String> units) throws ObjectValidationException {
        List<String> invalidUnits = units.stream().distinct().collect(Collectors.toList());
        String invalidDerString = invalidUnits.stream().distinct().collect(Collectors.joining(", "));
        if (invalidUnits.size() > 1) {
            throw new ObjectValidationException("DERs cannot be found in chosen KDM model",
                AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_DERS_WERE_NOT_FOUND_IN_CHOSEN_KDM_MODEL, invalidDerString);
        } else if (invalidUnits.size() == 1) {
            throw new ObjectValidationException("DER cannot be found in chosen KDM model",
                AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_DER_WAS_NOT_FOUND_IN_CHOSEN_KDM_MODEL, invalidDerString);
        }
    }

    public boolean isAgnoValidDERPowerStationsAndPointsOfConnectionWithLV(UnitDTO der, List<String> kdmPoints, boolean isLvModel) {
        if (isDerContainsPOCWithLVOrPowerStation(der)) {
            boolean pointsOfConnectionWithLVCorrect = isLvModel && isDerHasCorrectPointsOfConnectionWithLV(der, kdmPoints);
            boolean powerStationsCorrect = isDerHasCorrectPowerStations(der, kdmPoints);
            return powerStationsCorrect || pointsOfConnectionWithLVCorrect;
        } else {
            return false;
        }
    }

    private void checkOffers(Set<AuctionDayAheadOfferDTO> offers) throws ObjectValidationException {
        if (CollectionUtils.isEmpty(offers)) {
            throw new ObjectValidationException("The AGNO algorithm cannot be run because no bids have been submitted",
                AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NO_BIDS_HAVE_BEEN_SUBMITTED);
        }
    }

    // Sprawdzenie czy dla kazdego uzytego w aukcji produktu zostaly dodane prognozowane ceny
    private void checkForecastedPrices(Set<AuctionDayAheadOfferDTO> offers, Instant deliveryDate) throws ObjectValidationException {
        Set<ProductMinDTO> productIdsUsedInOffer = offers.stream().map(offer -> offer.getAuctionDayAhead().getProduct()).collect(Collectors.toSet());
        List<String> productsWithoutForecastedPrices = getProductNamesWithoutForecastedPrices(deliveryDate, productIdsUsedInOffer);
        if (!CollectionUtils.isEmpty(productsWithoutForecastedPrices)) {
            String message = String.format("Cannot find forecasted price for: products: %s and forecastedPriceDate: %s",
                String.join(",", productsWithoutForecastedPrices), deliveryDate);
            throw new ObjectValidationException(message, CANNOT_FIND_FORECASTED_PRICES);
        }
    }

    private List<String> getProductNamesWithoutForecastedPrices(Instant deliveryDate, Set<ProductMinDTO> productIdsUsedInOffer) {
        List<String> productsWithoutForecastedPrices = new ArrayList<>();
        for (ProductMinDTO product : productIdsUsedInOffer) {
            if (!forecastedPricesService.existForecastedPricesForProductAndDeliveryDate(product.getId(), deliveryDate)) {
                productsWithoutForecastedPrices.add(product.getShortName());
            }
        }
        return productsWithoutForecastedPrices;
    }

    private void checkAlgorithmEvaluationConfig(AlgorithmEvaluationConfigDTO configDTO) throws ObjectValidationException {
        if (!CollectionUtils.isEmpty(configDTO.getOffers())) {
            Set<AuctionDayAheadOfferDTO> offers = auctionDayAheadService.findAllOffersById(configDTO.getOffers());
            AlgorithmType algorithmType = configDTO.getAlgorithmType();
            checkAlgEvaluationSelectedOffers(offers, algorithmType);
        }
    }

    private void checkAlgEvaluationSelectedOffers(Set<AuctionDayAheadOfferDTO> offers, AlgorithmType algorithmType) throws ObjectValidationException {
        // jeżeli algorytmType -> BM to do uruchomienia algorytmu mozna użyc tylko ofert skladanych na aukcje DA ENERGY
        if (algorithmType.equals(BM) && !offers.stream().allMatch(o -> o.getType().equals(algorithmType.getOfferType()))) {
            throw new ObjectValidationException("Selected offers is not compatible with algorithm with type: " + algorithmType,
                AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NOT_CHOOSE_ONLY_ENERGY_RELATED_BIDS);
        }
        // jeżeli algorytmType -> PBCM to do uruchomienia algorytmu mozna użyc tylko ofert skladanych na aukcje DA CAPACITY
        if (algorithmType.equals(PBCM) && !offers.stream().allMatch(o -> o.getType().equals(algorithmType.getOfferType()))) {
            throw new ObjectValidationException("Selected offers is not compatible with algorithm with type: " + algorithmType,
                AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NOT_CHOOSE_ONLY_CAPACITY_RELATED_BIDS);
        }
    }
}
