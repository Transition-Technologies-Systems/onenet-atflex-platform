package pl.com.tt.flex.server.validator.unit;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.localization.LocalizationType;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.unit.enumeration.DerType;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.service.dictionary.derType.DerTypeService;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioService;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.ObjectValidator;
import pl.com.tt.flex.server.validator.common.CommonValidatorUtil;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;
import static pl.com.tt.flex.server.web.rest.unit.UnitResource.ENTITY_NAME;

@Component
@RequiredArgsConstructor
public class UnitValidator implements ObjectValidator<UnitDTO, Long> {

    // constant for validation errors not handled in frontend layer
    public static final String ERR_VALIDATION = "error.validation." + ENTITY_NAME;

    private final UnitService unitService;
    private final FlexPotentialService flexPotentialService;
    private final SchedulingUnitService schedulingUnitService;
    private final SubportfolioService subportfolioService;
    private final UserService userService;
    private final DerTypeService derTypeService;

    @Override
    public void checkValid(UnitDTO unitDTO) throws ObjectValidationException {
        validateDates(unitDTO);
        validateActive(unitDTO);
        validateCouplingPointAndMrid(unitDTO);
        validCouplingPointIdTypes(unitDTO);
        validPowerStationTypes(unitDTO);
        validCertified(unitDTO);
        validateSourcePowerIsSmallerThanConnectionPower(unitDTO);
        validDerTypes(unitDTO);
    }

    @Override
    public void checkModifiable(UnitDTO unitDTO) throws ObjectValidationException {
        checkValid(unitDTO);
        UnitDTO dbUnit = unitService.findById(unitDTO.getId()).orElseThrow(() -> new RuntimeException("Unit to modify not found"));
        UserEntity currentUser = userService.getCurrentUser();
        if (isDeactivateOperation(unitDTO, dbUnit) && unitHasJoinedActiveFlexPotentials(unitDTO)) {
            throw new ObjectValidationException("It is not possible to deactivate an unit related to active potentials",
                CANNOT_DEACTIVATE_BECAUSE_OF_ACTIVE_FLEX_POTENTIALS, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
        }
        if (isDeactivateOperation(unitDTO, dbUnit) && unitHasJoinedSubportfolio(unitDTO)) {
            throw new ObjectValidationException("It is not possible to deactivate an unit related to subportfolio",
                CANNOT_DEACTIVATE_BECAUSE_OF_JOINED_SUBPORTFOLIOS, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
        }
        if (isCertifiedChanged(unitDTO, dbUnit) && !(isDsoOrTsoUser(currentUser) || isTaUser(currentUser))) {
            throw new ObjectValidationException("Only DSO, TSO and TA users are able to modify field 'certified'",
                USER_HAS_NO_AUTHORITY_TO_MODIFY_UNIT_CERTIFIED, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
        }
        if (isCertificationRemovalOperation(unitDTO, dbUnit) && unitHasJoinedSubportfolio(unitDTO)) {
            throw new ObjectValidationException("It is not possible to remove certification an unit related to subportfolio",
                CANNOT_REMOVE_CERTIFICATION_BECAUSE_OF_JOINED_SUBPORTFOLIOS, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
        }
        if (isCertificationRemovalOperation(unitDTO, dbUnit) && unitAssignedToReadyForTestsSchedulingUnit(unitDTO)) {
            throw new ObjectValidationException("It is not possible to remove certification from an unit assigned to ready for tests scheduling unit",
                CANNOT_REMOVE_CERTIFICATION_WHILE_ASSIGNED_TO_READY_FOR_TESTS_SU, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
        }
        if (isFspUser(currentUser)) {
            if (isFspChanged(unitDTO, dbUnit)) {
                throw new ObjectValidationException("It is not possible to modify field 'fsp' by fsp user (ROLE_FLEX_SERVICE_PROVIDER)",
                    CANNOT_MODIFY_UNIT_FSP_BY_FSP_USER, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
            }
        }
        if (!(currentUser.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR) || currentUser.hasRole(Role.ROLE_ADMIN))) {
            if (isCouplingPointChanged(unitDTO, dbUnit) || isMridChanged(unitDTO, dbUnit) || isPowerStationChanged(unitDTO, dbUnit) || isCertifiedChanged(unitDTO, dbUnit) || isBRPCodeChanged(unitDTO, dbUnit) || isPointOfConnectionWithLvChanged(unitDTO, dbUnit)) {
                throw new ObjectValidationException("Fields 'mrid(TSO)', 'mrid(DSO)', 'couplingPointID', 'powerStation', 'certified', 'pointOfConnectionWithLvType' and 'brpCode' can only be edited by DSO and TA users", ERR_VALIDATION, ENTITY_NAME);
            }
        }
        // Przepuszczamy DSO w celu modyfikacji niektórych pól DERów (coupling point, MRID, powerStation i certyfikację)
        if (!(currentUser.hasRole(Role.ROLE_ADMIN) || currentUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER) || currentUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) && !currentUser.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR)) {
            throw new ObjectValidationException("DERs can be modified only by TA, FSP and FSPA users", ERR_VALIDATION, ENTITY_NAME);
        }
        if (isFspUser(currentUser) || isFspaUser(currentUser)) {
            if (!Objects.equals(dbUnit.getPMin(), unitDTO.getPMin()) ||
                !Objects.equals(dbUnit.getQMin(), unitDTO.getQMin()) ||
                !Objects.equals(dbUnit.getQMax(), unitDTO.getQMax())) {
                throw new ObjectValidationException("Fields 'pMin', 'qMin' and 'qMax' cannot be changed by FSP and FSPA users", ERR_VALIDATION, ENTITY_NAME);
            }
        }
    }

    @Override
    public void checkDeletable(Long unitId) throws ObjectValidationException {
        UnitDTO unitDTO = unitService.findById(unitId).get();
        UserEntity currentUser = userService.getCurrentUser();
        if (unitHasJoinedFlexPotentials(unitDTO)) {
            throw new ObjectValidationException("All related FlexPotentials must be removed before removing Unit",
                UNIT_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_FLEX_POTENTIALS, ENTITY_NAME, ActivityEvent.UNIT_DELETED_ERROR, unitId);
        }
        if (unitHasJoinedSchedulingUnit(unitDTO)) {
            throw new ObjectValidationException("All related SchedulingUnit must be removed before removing Unit",
                UNIT_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_SCHEDULING_UNITS, ENTITY_NAME, ActivityEvent.UNIT_DELETED_ERROR, unitId);
        }
        if (unitHasJoinedSubportfolio(unitDTO)) {
            throw new ObjectValidationException("All related Subportfolios must be removed before removing Unit",
                UNIT_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_SUBPORTFOLIOS, ENTITY_NAME, ActivityEvent.UNIT_DELETED_ERROR, unitId);
        }
        if (!(currentUser.hasRole(Role.ROLE_ADMIN) || currentUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER) || currentUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED))) {
            throw new ObjectValidationException("DERs can be deleted only by TA, FSP and FSPA users", ERR_VALIDATION, ENTITY_NAME);
        }
    }

    public void checkIfUserCanCreateUnits() throws ObjectValidationException {
        UserEntity currentUser = userService.getCurrentUser();
        if (!(currentUser.hasRole(Role.ROLE_ADMIN) || currentUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER) || currentUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED))) {
            throw new ObjectValidationException("DERs can be created only by TA, FSP and FSPA users", ERR_VALIDATION, ENTITY_NAME);
        }
    }

    private void validCouplingPointIdTypes(UnitDTO unitDTO) throws ObjectValidationException {
        List<LocalizationTypeDTO> couplingPointIdTypes = unitDTO.getCouplingPointIdTypes();
        // CouplingPoitnIdTypes w Unit jest listą jednoelementowa
        if (!CollectionUtils.isEmpty(couplingPointIdTypes) && couplingPointIdTypes.size() > 1) {
            throw new ObjectValidationException("Field 'couplingPointIdTypes' is a one-item list",
                COUPLING_POINT_ID_IS_ONE_ITEM_LIST, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
        }
        if (!CollectionUtils.isEmpty(couplingPointIdTypes)) {
            boolean isCorrectLocalizationType = couplingPointIdTypes.stream().allMatch(type -> type.getType().equals(LocalizationType.COUPLING_POINT_ID));
            if (!isCorrectLocalizationType) {
                throw new ObjectValidationException("Field 'couplingPointIdTypes' only accepts Localization with type " + LocalizationType.COUPLING_POINT_ID,
                    COUPLING_POINT_ID_HAS_INCORRECT_LOCALIZATION_TYPE, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
            }
        }
    }

    private void validPowerStationTypes(UnitDTO unitDTO) throws ObjectValidationException {
        if (!CollectionUtils.isEmpty(unitDTO.getPowerStationTypes())) {
            boolean isCorrectPowerStationType = unitDTO.getPowerStationTypes().stream().allMatch(type -> type.getType().equals(LocalizationType.POWER_STATION_ML_LV_NUMBER));
            if (!isCorrectPowerStationType) {
                throw new ObjectValidationException("Field 'powerStationTypes' only accepts Localization with type " + LocalizationType.POWER_STATION_ML_LV_NUMBER,
                    POWER_STATION_HAS_INCORRECT_LOCALIZATION_TYPE, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
            }
        }
    }

    private void validateActive(UnitDTO unitDTO) throws ObjectValidationException {
        Instant now = InstantUtil.now();
        if (unitDTO.isActive() && (unitDTO.getValidFrom().isAfter(now) || unitDTO.getValidTo().isBefore(now))) {
            throw new ObjectValidationException("Unit cannot be active because present date is not between validFrom and validTo dates",
                UNIT_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
        }
    }

    private boolean isDeactivateOperation(UnitDTO unitDTO, UnitDTO dbUnitDTO) {
        return !unitDTO.isActive() && dbUnitDTO.isActive();
    }

    private boolean isCertificationRemovalOperation(UnitDTO unitDTO, UnitDTO dbUnitDTO) {
        return !unitDTO.isCertified() && dbUnitDTO.isCertified();
    }

    private boolean unitHasJoinedActiveFlexPotentials(UnitDTO unitDTO) {
        List<Long> flexPotentialDTOS = flexPotentialService.findActiveByUnit(unitDTO);
        return !flexPotentialDTOS.isEmpty();
    }

    private boolean unitHasJoinedFlexPotentials(UnitDTO unitDTO) {
        List<Long> flexPotentialDTOS = flexPotentialService.findByUnit(unitDTO);
        return !flexPotentialDTOS.isEmpty();
    }

    private boolean unitHasJoinedSchedulingUnit(UnitDTO unitDTO) {
        return schedulingUnitService.findByUnit(unitDTO.getId()).isPresent();
    }

    private boolean unitHasJoinedSubportfolio(UnitDTO unitDTO) {
        List<Long> flexPotentialDTOS = subportfolioService.findByUnit(unitDTO);
        return !flexPotentialDTOS.isEmpty();
    }

    private boolean unitAssignedToReadyForTestsSchedulingUnit(UnitDTO unitDTO) {
        return schedulingUnitService.existsByUnitIdAndReadyForTestsTrue(unitDTO.getId());
    }

    private boolean isCertifiedChanged(UnitDTO unitDTO, UnitDTO dbUnitDTO) {
        return !(Boolean.compare(dbUnitDTO.isCertified(), unitDTO.isCertified()) == 0);
    }

    private boolean isFspChanged(UnitDTO unitDTO, UnitDTO dbUnitDTO) {
        return !(dbUnitDTO.getFspId().equals(unitDTO.getFspId()));
    }

    private boolean isFspUser(UserEntity currentUser) {
        return currentUser.getRoles().contains(Role.ROLE_FLEX_SERVICE_PROVIDER) && !currentUser.getRoles().contains(Role.ROLE_ADMIN);
    }

    private boolean isFspaUser(UserEntity currentUser) {
        return currentUser.getRoles().contains(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED) && !currentUser.getRoles().contains(Role.ROLE_ADMIN);
    }

    private boolean isDsoOrTsoUser(UserEntity currentUser) {
        return currentUser.getRoles().stream().anyMatch(role -> role.equals(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR) || role.equals(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR));
    }

    private void validateSourcePowerIsSmallerThanConnectionPower(UnitDTO unitDTO) throws ObjectValidationException {
        if (nonNull(unitDTO.getConnectionPower()) && nonNull(unitDTO.getSourcePower()) && unitDTO.getSourcePower().compareTo(unitDTO.getConnectionPower()) > 0) {
            throw new ObjectValidationException("Unit's source power cannot be bigger than connection power", UNIT_SOURCE_POWER_CANNOT_BE_GREATER_THAN_CONNECTION_POWER);
        }
    }

    private boolean isTaUser(UserEntity currentUser) {
        return currentUser.getRoles().stream().anyMatch(role -> role.equals(Role.ROLE_ADMIN));
    }

    public boolean isUnitBelongsToCurrentFspUser(Long id) {
        UnitDTO unitDTO = unitService.findById(id).orElseThrow(() -> new RuntimeException("Unit not found with id: " + id));
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new RuntimeException("Current logged-in user not found"));
        return unitDTO.getFspId().equals(fspUser.getFspId());
    }

    private ActivityEvent getActivityEvent(UnitDTO unitDTO) {
        return unitDTO.getId() == null ? ActivityEvent.UNIT_CREATED_ERROR : ActivityEvent.UNIT_UPDATED_ERROR;
    }

    private void validateCouplingPointAndMrid(UnitDTO unitDTO) throws ObjectValidationException {
        UserEntity currentUser = userService.getCurrentUser();
        if (isNull(unitDTO.getId()) && !(currentUser.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR) || currentUser.hasRole(Role.ROLE_ADMIN))) {
            if (nonNull(unitDTO.getMridTso())) {
                throw new ObjectValidationException("Field 'mrid(tso)' can only be set by DSO and TA users for new Unit",
                    ERR_VALIDATION, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
            }
            if (nonNull(unitDTO.getMridDso())) {
                throw new ObjectValidationException("Field 'mrid(dso)' can only be set by DSO and TA users for new Unit",
                    ERR_VALIDATION, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
            }
            if (!CollectionUtils.isEmpty(unitDTO.getCouplingPointIdTypes())) {
                throw new ObjectValidationException("Field 'couplingPointID' can only be set by DSO and TA users for new Unit",
                    ERR_VALIDATION, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
            }
            if (!CollectionUtils.isEmpty(unitDTO.getPowerStationTypes())) {
                throw new ObjectValidationException("Field 'powerStation' can only be set by DSO and TA users for new Unit",
                    ERR_VALIDATION, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
            }
            if (nonNull(unitDTO.getCode())) {
                throw new ObjectValidationException("Field 'brpCode' can only be set by DSO and TA users for new Unit",
                    ERR_VALIDATION, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
            }
        }
    }

    private boolean isCouplingPointChanged(UnitDTO unitDTO, UnitDTO dbUnit) {
        if (CollectionUtils.isEmpty(unitDTO.getCouplingPointIdTypes()) || CollectionUtils.isEmpty(dbUnit.getCouplingPointIdTypes())) {
            return unitDTO.getCouplingPointIdTypes() == dbUnit.getCouplingPointIdTypes();
        }
        List<Long> couplingPointIdTypeIds = unitDTO.getCouplingPointIdTypes().stream().map(LocalizationTypeDTO::getId).collect(Collectors.toList());
        List<Long> dbCouplingPointIdTypeIds = dbUnit.getCouplingPointIdTypes().stream().map(LocalizationTypeDTO::getId).collect(Collectors.toList());
        return dbCouplingPointIdTypeIds.stream().anyMatch(id -> !couplingPointIdTypeIds.contains(id));
    }

    private boolean isMridChanged(UnitDTO unitDTO, UnitDTO dbUnit) {
        return !StringUtils.equals(dbUnit.getMridTso(), unitDTO.getMridTso()) || !StringUtils.equals(dbUnit.getMridDso(), unitDTO.getMridDso());
    }

    private boolean isPowerStationChanged(UnitDTO unitDTO, UnitDTO dbUnit) {
        if (CollectionUtils.isEmpty(unitDTO.getPowerStationTypes()) || CollectionUtils.isEmpty(dbUnit.getPowerStationTypes())) {
            return unitDTO.getPowerStationTypes() == dbUnit.getPowerStationTypes();
        }
        List<Long> powerStationTypeIds = unitDTO.getPowerStationTypes().stream().map(LocalizationTypeDTO::getId).collect(Collectors.toList());
        List<Long> dbPowerStationTypeIds = dbUnit.getPowerStationTypes().stream().map(LocalizationTypeDTO::getId).collect(Collectors.toList());
        return dbPowerStationTypeIds.stream().anyMatch(id -> !powerStationTypeIds.contains(id));
    }

    private boolean isPointOfConnectionWithLvChanged(UnitDTO unitDTO, UnitDTO dbUnit) {
        if (CollectionUtils.isEmpty(unitDTO.getPointOfConnectionWithLvTypes()) || CollectionUtils.isEmpty(dbUnit.getPointOfConnectionWithLvTypes())) {
            return unitDTO.getPointOfConnectionWithLvTypes() == dbUnit.getPointOfConnectionWithLvTypes();
        }
        List<Long> pointOfConnectionWithLvTypesIds = unitDTO.getPointOfConnectionWithLvTypes().stream().map(LocalizationTypeDTO::getId).collect(Collectors.toList());
        List<Long> dbPointOfConnectionWithLvTypesIds = dbUnit.getPointOfConnectionWithLvTypes().stream().map(LocalizationTypeDTO::getId).collect(Collectors.toList());
        return dbPointOfConnectionWithLvTypesIds.stream().anyMatch(id -> !pointOfConnectionWithLvTypesIds.contains(id));
    }

    private boolean isBRPCodeChanged(UnitDTO unitDTO, UnitDTO dbUnit) {
        return !StringUtils.equals(dbUnit.getCode(), unitDTO.getCode());
    }

    private void validateDates(UnitDTO unitDTO) throws ObjectValidationException {
        Instant createdDate;
        if (unitDTO.getId() != null) {
            createdDate = unitService.findById(unitDTO.getId()).get().getCreatedDate();
        } else {
            createdDate = unitDTO.getCreatedDate();
        }
        CommonValidatorUtil.checkValidFromToDates(unitDTO.getValidFrom(), unitDTO.getValidTo(), createdDate,
            ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
    }

    private void validCertified(UnitDTO unitDTO) throws ObjectValidationException {
        if (unitDTO.isCertified()) {
            String param = null;
            if (CollectionUtils.isEmpty(unitDTO.getCouplingPointIdTypes())) {
                param = "couplingPointID";
            }
            if (CollectionUtils.isEmpty(unitDTO.getPowerStationTypes())) {
                param = "powerStation";
            }
            if (isNull(unitDTO.getCode())) {
                param = "brpCode";
            }
            if (isNull(unitDTO.getPMin())) {
                param = "pMin";
            }
            if (isNull(unitDTO.getQMin())) {
                param = "qMin";
            }
            if (isNull(unitDTO.getQMax())) {
                param = "qMax";
            }
            if (!isNull(param)) {
                throw new ObjectValidationException("Field '" + param + "' must be set before Unit certification",
                    ERR_VALIDATION, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
            }
        }
    }

    private void validDerTypes(UnitDTO unitDTO) throws ObjectValidationException {
        if (isNull(unitDTO.getDerTypeReception()) && isNull(unitDTO.getDerTypeEnergyStorage()) && isNull(unitDTO.getDerTypeGeneration())) {
            throw new ObjectValidationException("At least one DerType must be selected for DER",
                UNIT_AT_LEAST_ONE_DER_TYPE_MUST_BE_SELECTED_FOR_DER, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
        }
        if (nonNull(unitDTO.getDerTypeReception()) && !derTypeService.existsByIdAndType(unitDTO.getDerTypeReception().getId(), DerType.RECEPTION)) {
            throw new ObjectValidationException("Selected DerTypeReception for DER is not of RECEPTION type",
                UNIT_DER_TYPE_RECEPTION_IS_NOT_OF_RECEPTION_TYPE, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
        }
        if (nonNull(unitDTO.getDerTypeEnergyStorage()) && !derTypeService.existsByIdAndType(unitDTO.getDerTypeEnergyStorage().getId(), DerType.ENERGY_STORAGE)) {
            throw new ObjectValidationException("Selected DerTypeReception for DER is not of ENERGY_STORAGE type",
                UNIT_DER_TYPE_ENERGY_STORAGE_IS_NOT_OF_RECEPTION_TYPE, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
        }
        if (nonNull(unitDTO.getDerTypeGeneration()) && !derTypeService.existsByIdAndType(unitDTO.getDerTypeGeneration().getId(), DerType.GENERATION)) {
            throw new ObjectValidationException("Selected DerTypeReception for DER is not of GENERATION type",
                UNIT_DER_TYPE_GENERATION_IS_NOT_OF_RECEPTION_TYPE, ENTITY_NAME, getActivityEvent(unitDTO), unitDTO.getId());
        }
    }
}
