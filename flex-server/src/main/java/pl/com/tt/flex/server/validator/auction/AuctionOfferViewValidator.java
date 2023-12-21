package pl.com.tt.flex.server.validator.auction;

import static pl.com.tt.flex.server.web.rest.auction.da.AuctionDayAheadResource.OFFER_ENTITY_NAME;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AUCTION_BIDS_EVAL_CANNOT_ACCEPT_ALREADY_ACCEPTED_OR_REJECTED_OFFER;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AUCTION_BIDS_EVAL_CANNOT_ACCEPT_NOT_ALL_VOLUMES_ARE_VERIFIED;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AUCTION_BIDS_EVAL_CANNOT_CHANGE_STATUS_AUCTION_NOT_CLOSED;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AUCTION_BIDS_EVAL_CANNOT_EXPORT_BECAUSE_NO_OFFER_FOUND_FOR_GIVEN_DELIVERY_DATE;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.AUCTION_BIDS_EVAL_CANNOT_EXPORT_BECAUSE_NO_TYPE_OF_BIDS_SELECTED;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_IMPORT_FP_BECAUSE_WRONG_FILE_EXTENSION;

import java.util.List;
import java.util.Objects;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.dataimport.factory.DataImportFormat;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferService;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferViewQueryService;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferViewCriteria;

@Slf4j
@Component
public class AuctionOfferViewValidator {

    private final AuctionOfferViewQueryService offerViewQueryService;
    private final AuctionOfferService offerService;

    public AuctionOfferViewValidator(final AuctionOfferViewQueryService offerViewQueryService, final AuctionOfferService offerService) {
        this.offerViewQueryService = offerViewQueryService;
        this.offerService = offerService;
    }

    public void validateOfferExportCriteria(AuctionOfferViewCriteria criteria) {
        verifyCategorySelected(criteria);
        verifySelectedOffersExist(criteria);
    }

    public void validateOfferImportFileExtension(MultipartFile file) throws ImportDataException {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!extension.equalsIgnoreCase(DataImportFormat.XLSX.name())) {
            throw new ImportDataException(CANNOT_IMPORT_FP_BECAUSE_WRONG_FILE_EXTENSION);
        }
    }

    public void validateStatusChange(List<Long> offerIds, AuctionOfferStatus status) {
        checkIfAuctionsClosed(offerIds);
        checkIfAllOffersArePendingOrVerified(offerIds);
        if (status.equals(AuctionOfferStatus.ACCEPTED)) {
            checkIfAllVolumesAreVerified(offerIds);
        }
    }

    private void checkIfAuctionsClosed(List<Long> offerIds) {
        if (!offerService.areAllAuctionsClosed(offerIds)) {
            throw new ObjectValidationException("Cannot change status while auction is open", AUCTION_BIDS_EVAL_CANNOT_CHANGE_STATUS_AUCTION_NOT_CLOSED,
                OFFER_ENTITY_NAME);
        }
    }

    public void checkIfAllOffersArePendingOrVerified(List<Long> ids) throws ObjectValidationException {
        if (!offerService.areAllOffersPendingOrVerified(ids)) {
            throw new ObjectValidationException("Offer must have status pending or volumes verified", AUCTION_BIDS_EVAL_CANNOT_ACCEPT_ALREADY_ACCEPTED_OR_REJECTED_OFFER,
                OFFER_ENTITY_NAME);
        }
    }

    private void checkIfAllVolumesAreVerified(List<Long> offerIds) {
        if (!offerService.areAllDayAheadVolumesVerified(offerIds)) {
            throw new ObjectValidationException("Not all volumes are verified", AUCTION_BIDS_EVAL_CANNOT_ACCEPT_NOT_ALL_VOLUMES_ARE_VERIFIED,
                OFFER_ENTITY_NAME);
        }
    }

    private void verifySelectedOffersExist(AuctionOfferViewCriteria criteria) {
        if ((int) offerViewQueryService.countByCriteria(criteria) == 0) {
            throw new ObjectValidationException("Cannot export because no offers found for given delivery date",
                AUCTION_BIDS_EVAL_CANNOT_EXPORT_BECAUSE_NO_OFFER_FOUND_FOR_GIVEN_DELIVERY_DATE, OFFER_ENTITY_NAME);
        }
    }

    private void verifyCategorySelected(AuctionOfferViewCriteria criteria) {
        if (Objects.isNull(criteria.getAuctionCategoryAndType())) {
            throw new ObjectValidationException("Cannot export because no type of bids selected",
                AUCTION_BIDS_EVAL_CANNOT_EXPORT_BECAUSE_NO_TYPE_OF_BIDS_SELECTED, OFFER_ENTITY_NAME);
        }
    }
}
