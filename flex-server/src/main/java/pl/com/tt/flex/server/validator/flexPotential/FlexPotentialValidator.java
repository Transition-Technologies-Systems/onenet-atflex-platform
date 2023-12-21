package pl.com.tt.flex.server.validator.flexPotential;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.product.ProductService;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.DateUtil;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.AbstractFileValidator;
import pl.com.tt.flex.server.validator.ObjectValidator;
import pl.com.tt.flex.server.validator.common.CommonValidatorUtil;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;
import pl.com.tt.flex.server.web.rest.potential.FlexPotentialResource;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;
import static pl.com.tt.flex.server.web.rest.potential.FlexPotentialResource.ENTITY_NAME;

@Component
@RequiredArgsConstructor
public class FlexPotentialValidator extends AbstractFileValidator implements ObjectValidator<FlexPotentialDTO, Long> {

    private static final Set<FileExtension> SUPPORTED_FILE_EXTENSIONS = Sets.newHashSet(DOC, DOCX, PDF, TXT, XLS, XLSX);

    private final FlexPotentialService flexPotentialService;
    private final ProductService productService;
    private final UnitService unitService;
    private final UserService userService;

    @Override
    public void checkValid(FlexPotentialDTO flexPotentialDTO) throws ObjectValidationException {
        validDates(flexPotentialDTO);
        validActive(flexPotentialDTO);
        validateByProduct(flexPotentialDTO);
        validateByUnit(flexPotentialDTO);
        checkIfFlexPotentialContainsDers(flexPotentialDTO);
        checkIfFlexPotentialCanBeMovedToFlexRegister(flexPotentialDTO);
        checkIfUserCanCreateFlexPotential(flexPotentialDTO);
    }

    @Override
    public void checkDeletable(Long flexPotentialId) throws ObjectValidationException {
        UserEntity currentUser = userService.getCurrentUser();
        if (currentUser.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR) || currentUser.hasRole(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR)) {
            throw new ObjectValidationException("Flex potential can't be deleted by TSO and DSO users", FLEX_POTENTIAL_CANNOT_BE_DELETED_BY_TSO_AND_DSO,  FlexPotentialResource.ENTITY_NAME,
                ActivityEvent.FP_DELETED_ERROR, flexPotentialId);
        }

        FlexPotentialDTO flexPotentialDTO = flexPotentialService.findById(flexPotentialId).get();
        if (flexPotentialDTO.isActive()) {
            throw new ObjectValidationException("Cannot remove active FlexPotential",
                CANNOT_DELETE_FP_BECAUSE_IT_IS_ACTIVE, FlexPotentialResource.ENTITY_NAME,
                ActivityEvent.FP_DELETED_ERROR, flexPotentialId);
        }
    }

    public void checkIfUserCanCreateFlexPotential(FlexPotentialDTO flexPotentialDTO) throws ObjectValidationException {
        if (!Objects.nonNull(flexPotentialDTO.getId())) {
            UserEntity currentUser = userService.getCurrentUser();
            if (currentUser.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR) || currentUser.hasRole(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR)) {
                throw new ObjectValidationException("Flex Potential can't be created by TSO and DSO users", FLEX_POTENTIAL_CANNOT_BE_CREATED_BY_TSO_AND_DSO, ENTITY_NAME, ActivityEvent.FP_CREATED_ERROR, flexPotentialDTO.getId());
            }
        }
    }

    /**
     * Jeżeli flex potential został oznaczony jako zarejestrowany (jest zagregowany, przeprowadzono wstępną prekwalifikację produktu i statyczną sieci),
     * lecz nie zawiera DERów, to wyrzucić wyjątek.
     */
    private void checkIfFlexPotentialCanBeMovedToFlexRegister(FlexPotentialDTO result) throws ObjectValidationException {
        if (result.isRegistered() && result.getUnitIds().size() == 0) {
            throw new ObjectValidationException("Cannot move Flex Potential with no DERs to Flex Register", CANNOT_MOVE_EMPTY_FLEX_POTENTIAL_TO_FLEX_REGISTER,
                ENTITY_NAME, ActivityEvent.FP_UPDATED_ERROR, result.getId());
        }
    }

    /**
     * Sprawdzenie czy utworzony lub aktualizowany flex potential zawiera DERy
     * (co prawda nie da się utworzyć pustego DERa przez aplikację, brakowało walidacji na backendzie)
     */
    private void checkIfFlexPotentialContainsDers(FlexPotentialDTO flexPotentialDTO) throws ObjectValidationException {
        if (flexPotentialDTO.getUnitIds() != null) {
            if (flexPotentialDTO.getUnitIds().size() == 0) {
                throw new ObjectValidationException("Cannot create flex potential with no DERs", CANNOT_CREATE_FLEX_POTENTIAL_WITH_NO_DERS,
                    ENTITY_NAME, getActivityEvent(flexPotentialDTO), flexPotentialDTO.getId());
            }
        } else {
            throw new BadRequestAlertException("List of unit ids is null", ENTITY_NAME, "idnull");
        }
    }

    private void validActive(FlexPotentialDTO flexPotentialDTO) throws ObjectValidationException {
        Instant now = InstantUtil.now();
        if (flexPotentialDTO.isActive() && (flexPotentialDTO.getValidFrom().isAfter(now) || flexPotentialDTO.getValidTo().isBefore(now))) {
            throw new ObjectValidationException("FlexPotential cannot be active because present date is not between validFrom and validTo dates",
                FP_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES, ENTITY_NAME,
                getActivityEvent(flexPotentialDTO), flexPotentialDTO.getId());
        }
    }

    private void validateByUnit(FlexPotentialDTO flexPotentialDTO) throws ObjectValidationException {
        if (flexPotentialDTO.getUnitIds() != null) {
            for (Long unitId : flexPotentialDTO.getUnitIds()) {
                UnitDTO unitDTO = unitService.findById(unitId).get();
                if (!unitDTO.isActive()) {
                    throw new ObjectValidationException("FlexPotential Unit is not active", FP_UNIT_IS_NOT_ACTIVE, ENTITY_NAME,
                        getActivityEvent(flexPotentialDTO), flexPotentialDTO.getId());
                }
                if (flexPotentialDTO.getValidTo().isAfter(unitDTO.getValidTo())) {
                    throw new ObjectValidationException("FlexPotential exceeds the expiry date of the unit", FP_EXCEEDS_THE_EXPIRY_DATE_OF_THE_UNIT, ENTITY_NAME,
                        getActivityEvent(flexPotentialDTO), flexPotentialDTO.getId());
                }
            }
        } else {
            throw new BadRequestAlertException("List of unit ids is null", ENTITY_NAME, "idnull");
        }
    }

    private void validateByProduct(FlexPotentialDTO flexPotentialDTO) throws ObjectValidationException {
        ProductDTO productDTO = productService.findById(flexPotentialDTO.getProduct().getId()).get();
        if (flexPotentialDTO.getVolume().compareTo(productDTO.getMaxBidSize()) > 0 ||
            flexPotentialDTO.getVolume().compareTo(productDTO.getMinBidSize()) < 0) {
            throw new ObjectValidationException("FlexPotential volume is not between product max/min bidSize",
                FP_VOLUME_IS_NOT_BETWEEN_MIN_MAX_PRODUCT_BID_SIZE, ENTITY_NAME,
                getActivityEvent(flexPotentialDTO), flexPotentialDTO.getId());
        }
        if (!DateUtil.isInstantBetween(flexPotentialDTO.getValidFrom(), productDTO.getValidFrom(), productDTO.getValidTo(), true) &&
            !DateUtil.isInstantBetween(flexPotentialDTO.getValidTo(), productDTO.getValidFrom(), productDTO.getValidTo(), true)) {
            throw new ObjectValidationException("FlexPotential exceeds the expiry date of the product",
                FP_EXCEEDS_THE_EXPIRY_DATE_OF_THE_PRODUCT, ENTITY_NAME,
                getActivityEvent(flexPotentialDTO), flexPotentialDTO.getId());
        }
        if (!productDTO.isActive()) {
            throw new ObjectValidationException("FlexPotential Product is not active",
                FP_PRODUCT_IS_NOT_ACTIVE, ENTITY_NAME,
                getActivityEvent(flexPotentialDTO), flexPotentialDTO.getId());
        }
        if (flexPotentialDTO.getFullActivationTime().compareTo(productDTO.getMaxFullActivationTime()) > 0) {
            throw new ObjectValidationException("FlexPotential fullActivationTime cannot be higher than Product maxFullActivationTime",
                FP_FULL_ACTIVATION_TIME_CANNOT_BE_HIGHER_THAN_PRODUCT_MAX_FULL_ACTIVATION_TIME, ENTITY_NAME,
                getActivityEvent(flexPotentialDTO), flexPotentialDTO.getId());
        }
        if (flexPotentialDTO.getMinDeliveryDuration().compareTo(productDTO.getMinRequiredDeliveryDuration()) < 0) {
            throw new ObjectValidationException("FlexPotential minDeliveryDuration cannot be less than Product minRequiredDeliveryDuration",
                FP_MIN_DELIVERY_DURATION_CANNOT_BE_LESS_THAN_PRODUCT_MIN_REQUIRED_DELIVERY_DURATION, ENTITY_NAME,
                getActivityEvent(flexPotentialDTO), flexPotentialDTO.getId());
        }
    }

    private ActivityEvent getActivityEvent(FlexPotentialDTO flexPotentialDTO) {
        return flexPotentialDTO.getId() == null ? ActivityEvent.FP_CREATED_ERROR : ActivityEvent.FP_UPDATED_ERROR;
    }

    private void validDates(FlexPotentialDTO flexPotentialDTO) throws ObjectValidationException {
        Instant createdDate;
        if (flexPotentialDTO.getId() != null) {
            createdDate = flexPotentialService.findById(flexPotentialDTO.getId()).get().getCreatedDate();
        } else {
            createdDate = flexPotentialDTO.getCreatedDate();
        }
        CommonValidatorUtil.checkValidFromToDates(flexPotentialDTO.getValidFrom(), flexPotentialDTO.getValidTo(), createdDate,
            ENTITY_NAME, getActivityEvent(flexPotentialDTO), flexPotentialDTO.getId());
    }

    @Override
    protected Set<FileExtension> getSupportedFileExtensions() {
        return SUPPORTED_FILE_EXTENSIONS;
    }

    @Override
    protected String getEntityName() {
        return ENTITY_NAME;
    }
}
