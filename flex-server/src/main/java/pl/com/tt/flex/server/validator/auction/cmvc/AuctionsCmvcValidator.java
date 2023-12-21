package pl.com.tt.flex.server.validator.auction.cmvc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.auction.cmvc.AuctionCmvcDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.repository.auction.cmvc.AuctionCmvcViewRepository;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.ObjectValidator;

import java.util.Objects;

import static pl.com.tt.flex.server.web.rest.auction.cmvc.AuctionCmvcResource.ENTITY_NAME;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Component
@RequiredArgsConstructor
public class AuctionsCmvcValidator implements ObjectValidator<AuctionCmvcDTO, Long> {

    private final AuctionCmvcViewRepository auctionCmvcViewRepository;
    private final ProductService productService;

    @Override
    public void checkValid(AuctionCmvcDTO auctionCmvcDTO) throws ObjectValidationException {
        validAuctionDate(auctionCmvcDTO);
        validDesiredPower(auctionCmvcDTO);
        validProduct(auctionCmvcDTO);
    }

    private void validAuctionDate(AuctionCmvcDTO auctionCmvcDTO) throws ObjectValidationException {
        if (auctionCmvcDTO.getDeliveryDateTo().isBefore(auctionCmvcDTO.getDeliveryDateFrom())) {
            throw new ObjectValidationException("DeliveryDateTo is before DeliveryDateFrom.",
                DELIVERY_DATE_TO_BEFORE_DELIVERY_DATE_FROM, ENTITY_NAME,
                getActivityEvent(auctionCmvcDTO), auctionCmvcDTO.getId());
        }
        if (auctionCmvcDTO.getGateClosureTime().isBefore(auctionCmvcDTO.getGateOpeningTime())) {
            throw new ObjectValidationException("GateClosureTime is before GateOpeningTime",
                GATE_CLOSURE_TIME_BEFORE_GATE_OPENING_TIME, ENTITY_NAME,
                getActivityEvent(auctionCmvcDTO), auctionCmvcDTO.getId());
        }
        if (auctionCmvcDTO.getGateClosureTime().isAfter(auctionCmvcDTO.getDeliveryDateFrom())) {
            throw new ObjectValidationException("GateClosureTime is after DeliveryDateFrom",
                GATE_CLOSURE_TIME_IS_AFTER_DELIVERY_DATE_FROM, ENTITY_NAME,
                getActivityEvent(auctionCmvcDTO), auctionCmvcDTO.getId());
        }
    }

    private void validDesiredPower(AuctionCmvcDTO auctionCmvcDTO) throws ObjectValidationException {
        if (Objects.nonNull(auctionCmvcDTO.getMinDesiredPower()) && Objects.nonNull(auctionCmvcDTO.getMaxDesiredPower())) {
            if (auctionCmvcDTO.getMinDesiredPower().compareTo(auctionCmvcDTO.getMaxDesiredPower()) > 0) {
                throw new ObjectValidationException("MinDesiredPower is bigger than MaxDesiredPower",
                    MAX_DESIRED_POWER_BIGGER_THAN_MIN_DESIRED_POWER, ENTITY_NAME,
                    getActivityEvent(auctionCmvcDTO), auctionCmvcDTO.getId());
            }
        }
    }

    private void validProduct(AuctionCmvcDTO auctionCmvcDTO) throws ObjectValidationException {
        ProductDTO productDTO = productService.findById(auctionCmvcDTO.getProduct().getId()).get();
        if (!(productDTO.isActive()) || productDTO.getValidTo().isBefore(InstantUtil.now())) {
            throw new ObjectValidationException("Product is inactive or date validTo expired",
                PRODUCT_IS_INACTIVE_OR_DATE_VALID_TO_EXPIRED, ENTITY_NAME,
                getActivityEvent(auctionCmvcDTO), auctionCmvcDTO.getId());
        }
        if (auctionCmvcDTO.getGateClosureTime().isAfter(productDTO.getValidTo())) {
            throw new ObjectValidationException("GateClosureTime is after Product validTo date",
                GATE_CLOSURE_TIME_IS_AFTER_PRODUCT_VALID_TO, ENTITY_NAME,
                getActivityEvent(auctionCmvcDTO), auctionCmvcDTO.getId());
        }
    }

    @Override
    public void checkDeletable(Long id) throws ObjectValidationException {
        AuctionStatus status = auctionCmvcViewRepository.findStatusById(id);
        if (AuctionStatus.OPEN.equals(status)) {
            throw new ObjectValidationException("Cannot delete open auction",
                CANNOT_DELETE_OPEN_AUCTION, ENTITY_NAME);
        }
        if (AuctionStatus.CLOSED.equals(status)) {
            throw new ObjectValidationException("Cannot delete closed auction",
                CANNOT_DELETE_CLOSED_AUCTION, ENTITY_NAME);
        }
    }

    private ActivityEvent getActivityEvent(AuctionCmvcDTO auctionCmvcDTO) {
        return auctionCmvcDTO.getId() == null ? ActivityEvent.AUCTIONS_CMVC_CREATED_ERROR : ActivityEvent.AUCTIONS_CMVC_DELETED_ERROR;
    }
}

