package pl.com.tt.flex.server.validator.product;

import io.github.jhipster.service.filter.LongFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.service.auction.da.AuctionDayAheadService;
import pl.com.tt.flex.server.service.potential.FlexPotentialQueryService;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialCriteria;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.ObjectValidator;
import pl.com.tt.flex.server.validator.common.CommonValidatorUtil;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;
import static pl.com.tt.flex.server.web.rest.product.ProductResource.ENTITY_NAME;

@Component
@RequiredArgsConstructor
public class ProductValidator implements ObjectValidator<ProductDTO, Long> {

    private final ProductService productService;
    private final FlexPotentialQueryService flexPotentialQueryService;
    private final UserService userService;
    private final AuctionDayAheadService auctionDayAheadService;

    @Override
    public void checkValid(ProductDTO productDTO) throws ObjectValidationException {
        validateBidSize(productDTO);
        validateDates(productDTO);
        validatePsoAndSsoUsers(productDTO);
        validateActive(productDTO);
        validateBalancingCmvcFlags(productDTO);
    }

    private void validateBidSize(ProductDTO productDTO) throws ObjectValidationException {
        if (productDTO.getMinBidSize().compareTo(productDTO.getMaxBidSize()) > 0) {
            throw new ObjectValidationException("MinBidSize is greater than MaxBidSize", MIN_NUMBER_GREATER_THAN_MAX_NUMBER, ENTITY_NAME,
                getActivityEvent(productDTO), productDTO.getId());
        }
    }

    private void validateActive(ProductDTO productDTO) throws ObjectValidationException {
        Instant now = InstantUtil.now();
        if (productDTO.isActive() && (productDTO.getValidFrom().isAfter(now) || productDTO.getValidTo().isBefore(now))) {
            throw new ObjectValidationException("Product cannot be active because present date is not between validFrom and validTo dates",
                PRODUCT_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES, ENTITY_NAME,
                getActivityEvent(productDTO), productDTO.getId());
        }
    }

    private void validateBalancingCmvcFlags(ProductDTO productDTO) throws ObjectValidationException {
        if(!productDTO.isBalancing() && !productDTO.isCmvc()){
            throw new ObjectValidationException("One of balancing and cmvc should be marked",
                ONE_OF_BALANCING_AND_CMVC_SHOULD_BE_MARKED, ENTITY_NAME,
                getActivityEvent(productDTO), productDTO.getId());
        }
    }

    private void validatePsoAndSsoUsers(ProductDTO productDTO) throws ObjectValidationException {
        if (!userService.doesUserHaveOneOfGivenRoles(productDTO.getPsoUserId(), Arrays.asList(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR, Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR))) {
            throw new ObjectValidationException("Selected pso user is not DSO/TSO user", USER_HAS_NO_ROLE_TO_BE_PSO_USER, ENTITY_NAME,
                getActivityEvent(productDTO), productDTO.getId());
        }
        for (Long userId : productDTO.getSsoUserIds()) {
            if (!userService.doesUserHaveOneOfGivenRoles(userId, Arrays.asList(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR, Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR))) {
                throw new ObjectValidationException("Selected sso user is not DSO/TSO user", USER_HAS_NO_ROLE_TO_BE_SSO_USER, ENTITY_NAME,
                    getActivityEvent(productDTO), productDTO.getId());
            }
        }
    }

    @Override
    public void checkModifiable(ProductDTO productDTO) throws ObjectValidationException {
        checkValid(productDTO);
        checkModifiableProductName(productDTO);
    }

    private void checkModifiableProductName(ProductDTO productDTO) throws ObjectValidationException {
        ProductDTO dbProduct = productService.findById(productDTO.getId()).get();
        if(!Objects.equals(dbProduct.getShortName(), productDTO.getShortName()) && auctionDayAheadService.existsOpenAuctionWithProductId(productDTO.getId())) {
            throw new ObjectValidationException("Cannot modify product full name, because is ongoing auction with this product",
                CANNOT_MODIFY_FULL_NAME_BECAUSE_IS_ONGOING_AUCTION_WITH_THIS_PRODUCT, ENTITY_NAME,
                getActivityEvent(productDTO), productDTO.getId());
        }
    }

    @Override
    public void checkDeletable(Long productId) throws ObjectValidationException {
        ProductDTO productDTO = productService.findById(productId).get();
        if (productHasJoinedFlexPotentials(productDTO.getId())) {
            throw new ObjectValidationException("All FlexPotentials provided by the FSP must be removed before removing Product",
                PRODUCT_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_FLEX_POTENTIALS, ENTITY_NAME, ActivityEvent.PRODUCT_DELETED_ERROR, productId);
        }
    }

    private boolean productHasJoinedFlexPotentials(Long productId) {
        FlexPotentialCriteria flexPotentialCriteria = new FlexPotentialCriteria();
        flexPotentialCriteria.setProductId((LongFilter) new LongFilter().setEquals(productId));
        return !flexPotentialQueryService.findByCriteria(flexPotentialCriteria).isEmpty();
    }

    private ActivityEvent getActivityEvent(ProductDTO productDTO) {
        return productDTO.getId() == null ? ActivityEvent.PRODUCT_CREATED_ERROR : ActivityEvent.PRODUCT_UPDATED_ERROR;
    }

    private void validateDates(ProductDTO productDTO) throws ObjectValidationException {
        Instant createdDate;
        if (productDTO.getId() != null) {
            createdDate = productService.findById(productDTO.getId()).get().getCreatedDate();
        } else {
            createdDate = productDTO.getCreatedDate();
        }
        CommonValidatorUtil.checkValidFromToDates(productDTO.getValidFrom(), productDTO.getValidTo(), createdDate,
            ENTITY_NAME, getActivityEvent(productDTO), productDTO.getId());
    }
}
