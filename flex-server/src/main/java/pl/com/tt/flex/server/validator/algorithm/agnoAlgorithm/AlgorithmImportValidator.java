package pl.com.tt.flex.server.validator.algorithm.agnoAlgorithm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.service.algorithm.AlgorithmEvaluationService;
import pl.com.tt.flex.server.service.importData.algorithm.AlgorithmDanoImportData;
import pl.com.tt.flex.server.service.importData.algorithm.AlgorithmPcbmImportData;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

import com.fasterxml.jackson.core.JsonProcessingException;

@Slf4j
@Component
public class AlgorithmImportValidator {
    private final UnitService unitService;
    private final ProductService productService;
    private final AlgorithmEvaluationService algorithmEvaluationService;

    public AlgorithmImportValidator(UnitService unitService, ProductService productService, AlgorithmEvaluationService algorithmEvaluationService) {
        this.unitService = unitService;
        this.productService = productService;
        this.algorithmEvaluationService = algorithmEvaluationService;
    }

    public void checkPbcmValid(AlgorithmPcbmImportData algorithmPcbmImportData) throws ObjectValidationException {
        validDerName(algorithmPcbmImportData.getDerName());
        validProduct(algorithmPcbmImportData.getType(), algorithmPcbmImportData.getProductType());
    }

    public void checkDanoValid(Long evaluationId, AlgorithmDanoImportData algorithmDanoImportData) throws ObjectValidationException, JsonProcessingException {
        AlgorithmEvaluationEntity algorithmEvaluationEntity = algorithmEvaluationService.findAlgorithmEvaluationEntityById(evaluationId);
        validDerName(algorithmDanoImportData.getDerName());
        validPrice(algorithmEvaluationEntity, algorithmDanoImportData.getPrice());
    }

    public void checkIfFilesExist(List<FileDTO> fileDTOS) throws ObjectValidationException {
        if (fileDTOS.size() == 0) {
            log.error("checkIfFilesExist() Cannot parse results because no files were found");
            throw new ObjectValidationException("Cannot parse results because no files were found.", IMPORT_ALGORITHM_RESULT_COULD_NOT_PARSE_BECAUSE_NO_FILES_FOUND);
        }
    }

    private void validDerName(String derName) throws ObjectValidationException {
        Optional<UnitMinDTO> unitMinOpt = unitService.findUnitByNameIgnoreCase(derName);
        if (unitMinOpt.isEmpty()) {
            log.error("validDerName() Could not find DER with name: {}", derName);
            throw new ObjectValidationException("Could not find DER with name", IMPORT_ALGORITHM_RESULT_COULD_NOT_FIND_DER_MATCHING_NAME);
        }
    }

    private void validProduct(String productName, String direction) throws ObjectValidationException {
        Optional<ProductMinDTO> productMinOpt = productService.findByShortName(productName);
        if (productMinOpt.isEmpty()) {
            log.error("validProduct() Could not find product with name: {}", direction);
            throw new ObjectValidationException("Could not find product with name", IMPORT_ALGORITHM_RESULT_COULD_NOT_FIND_PRODUCT_MATCHING_NAME);
        }
        boolean productDirection = productMinOpt.get().getDirection().name().equals(direction.toUpperCase());
        if (!productDirection) {
            log.error("validProduct() Product direction does not match with product from database: {}", direction);
            throw new ObjectValidationException("Product direction does not match with product from database", IMPORT_ALGORITHM_RESULT_INVALID_PRODUCT_DIRECTION);
        }
    }

    private void validPrice(AlgorithmEvaluationEntity algorithmEvaluationEntity, String price) throws ObjectValidationException {
        BigDecimal priceToCheck = BigDecimal.valueOf(Double.parseDouble(price));
        boolean isPriceExist = false;
        for (AuctionDayAheadOfferEntity auctionDayAheadOfferEntity : algorithmEvaluationEntity.getDaOffers()) {
            isPriceExist = auctionDayAheadOfferEntity.getUnits().stream()
                .flatMap(unit -> unit.getBandData().stream())
                .filter(bandData -> !bandData.getBandNumber().equals("0"))
                .anyMatch(bandData -> bandData.getPrice().compareTo(priceToCheck) == 0);
            if(isPriceExist){
                break;
            }
        }
        if (!isPriceExist) {
            log.error("validPrice() Prices of auction and from file differ");
            throw new ObjectValidationException("Prices of auction and from file differ", IMPORT_ALGORITHM_RESULT_PRICES_BETWEEN_AUCTION_AND_FILE_DIFFER);
        }
    }
}
