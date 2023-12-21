package pl.com.tt.flex.server.service.importData.auctionOffer.cmvc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.cmvc.AuctionCmvcOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.service.auction.cmvc.AuctionCmvcService;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferService;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportData;
import pl.com.tt.flex.server.service.importData.auctionOffer.dto.AuctionOfferImportDataResult;
import pl.com.tt.flex.server.validator.auction.cmvc.AuctionCmvcOfferImportValidator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static pl.com.tt.flex.server.util.DateUtil.getAcceptedDeliveryPeriodFrom;
import static pl.com.tt.flex.server.util.DateUtil.getAcceptedDeliveryPeriodTo;
import static pl.com.tt.flex.server.validator.auction.cmvc.AuctionCmvcOfferImportValidator.checkAndGetBidId;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.IMPORT_OTHER;

@Slf4j
@Component
public class AuctionCmvcOfferImportServiceImpl implements AuctionCmvcOfferImportService {

    private final AuctionCmvcService auctionCmvcService;
    private final AuctionOfferService auctionOfferService;
    private final AuctionCmvcOfferImportValidator cmvcOfferImportValidator;

    public AuctionCmvcOfferImportServiceImpl(AuctionCmvcService auctionCmvcService, AuctionOfferService auctionOfferService, AuctionCmvcOfferImportValidator cmvcOfferImportValidator) {
        this.auctionCmvcService = auctionCmvcService;
        this.auctionOfferService = auctionOfferService;
        this.cmvcOfferImportValidator = cmvcOfferImportValidator;
    }

    @Override
    public AuctionOfferImportDataResult importBids(List<AuctionOfferImportData> cmvcBids) {
        log.debug("importBids() Start - import cmvc offers");
        AuctionOfferImportDataResult auctionOfferImportDataResultDTOs = new AuctionOfferImportDataResult();
        cmvcBids.forEach(bid -> {
            try {
                log.debug("importData() Start - import bid with ID={}", bid.getId());
                long offerId = checkAndGetBidId(bid.getId());
                if (auctionOfferService.isCmvcOffer(offerId)) {
                    cmvcOfferImportValidator.checkValid(bid);
                    updateCmvcOffer(bid);
                    auctionOfferImportDataResultDTOs.addImportedBids(offerId);
                    log.debug("importData() End - Successfully import bid with ID={}", bid.getId());
                } else {
                    log.debug("importData() End - Ignore non CMVC bid. BidId={}", bid.getId());
                }
            } catch (ObjectValidationException e) {
                auctionOfferImportDataResultDTOs.addNotImportedBids(new MinimalDTO<>(bid.getId(), e.getMsgKey()));
                log.debug("importData() End - Not import bid={} with error msg: {}", bid.getId(), e.getMessage());
            } catch (Exception e) {
                auctionOfferImportDataResultDTOs.addNotImportedBids(new MinimalDTO<>(bid.getId(), IMPORT_OTHER));
                log.debug("importData() End - Not import bid={} with error msg: {}", bid.getId(), e.getMessage());
                e.printStackTrace();
            }
        });
        log.debug("importData() End - import cmvc offers");
        return auctionOfferImportDataResultDTOs;
    }

    @Transactional
    private void updateCmvcOffer(AuctionOfferImportData bid) throws ObjectValidationException {
        AuctionCmvcOfferDTO offerToUpdate = auctionCmvcService.findOfferById(parseBidId(bid))
            .orElseThrow(() -> new RuntimeException("AuctionCmvcOffer not found with id: " + bid.getId()));
        AuctionCmvcDTO auctionCmvcDTO = auctionCmvcService.findById(offerToUpdate.getAuctionCmvc().getId())
            .orElseThrow(() -> new RuntimeException("AuctionCmvc not found with id: " + offerToUpdate.getAuctionCmvc().getId()));
        offerToUpdate.setAcceptedDeliveryPeriodFrom(getAcceptedDeliveryPeriodFrom(bid.getAcceptedDeliveryPeriod()));
        offerToUpdate.setAcceptedDeliveryPeriodTo(getAcceptedDeliveryPeriodTo(bid.getAcceptedDeliveryPeriod()));
        offerToUpdate.setAcceptedVolume(new BigDecimal(bid.getAcceptedVolume()));
        AuctionOfferStatus updatedStatus = AuctionOfferStatus.findStatusByDescription(bid.getStatus())
            .orElseThrow(() -> new RuntimeException("AuctionOfferStatus not found with description: " + bid.getStatus()));
        auctionCmvcService.saveOffer(offerToUpdate, auctionCmvcDTO.getStatus());
        auctionOfferService.updateStatus(updatedStatus, Collections.singletonList(offerToUpdate.getId()));
    }

    private long parseBidId(AuctionOfferImportData bid) {
        return Long.parseLong(bid.getId());
    }

}

