package pl.com.tt.flex.server.web.rest.errors;

import java.net.URI;

public final class ErrorConstants {

    public static final String ERR_OBJECT_MODIFIED_BY_ANOTHER_USER = "error.objectModifiedByAnotherUser";
    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "https://www.flex-platform/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
    public static final String UNEXPECTED_ERROR = "error.unexpectedError";

    public static final String USER_IS_NOT_ACTIVATED = "error.userIsNotActivated";
    public static final String USER_EMAIL_NOT_FOUND = "error.userEmailNotFound";
    public static final String USER_LOGIN_NOT_FOUND = "error.userLoginNotFound";

    public static final String EMAIL_ALREADY_USED_TYPE = "error.emailExists";
    public static final String LOGIN_ALREADY_USED_TYPE = "error.user.userExists";
    public static final String LOGIN_CONTAINS_INVALID_CHARACTERS = "error.user.loginContainsInvalidCharacters";
    public static final String NO_PERMISSION_TO_LOGIN = "error.noPermissionToLogin";
    public static final String FILE_PARSE_ERROR = "error.fileParseError";
    public static final String WRONG_ACTUAL_STATUS = "error.wrongActualStatus";
    public static final String FROM_DATE_BEFORE_CREATED_DATE = "error.date.fromDateBeforeCreatedDate";
    public static final String FROM_DATE_AFTER_TO_DATE = "error.date.fromDateAfterToDate";
    public static final String MIN_NUMBER_GREATER_THAN_MAX_NUMBER = "error.number.minNumberGreaterThanMaxNumber";
    public static final String FILE_EXTENSION_NOT_SUPPORTED = "error.file.fileExtensionNotSupported";
    public static final String SECURITY_KEY_IS_INVALID_OR_EXPIRED = "error.securityKeyIsInvalidOrExpired";

    //FLEX POTENTIAL
    public static final String CANNOT_DEACTIVATE_BECAUSE_OF_ACTIVE_FLEX_POTENTIALS = "error.flexPotential.cannotDeactivateBecauseOfActiveFlexPotentials";
    public static final String CANNOT_DELETE_FP_BECAUSE_IT_IS_ACTIVE = "error.flexPotential.cannotDeleteActive";
    public static final String FP_VOLUME_IS_NOT_BETWEEN_MIN_MAX_PRODUCT_BID_SIZE = "error.flexPotential.fpVolumeIsNotBetweenMinMaxProductBidSize";
    public static final String CANNOT_IMPORT_FP_BECAUSE_WRONG_FILE_EXTENSION = "error.flexPotential.cannotImportWrongFileExtension";
    public static final String CANNOT_IMPORT_FP_BECAUSE_WRONG_HEADERS = "error.flexPotential.cannotImportInvalidHeaders";
    public static final String CANNOT_IMPORT_FP_BECAUSE_INCORRECT_DATA_TO_IMPORT = "error.flexPotential.cannotImportIncorrectDataToImport";
    public static final String CANNOT_IMPORT_FP_BECAUSE_NO_PERMISSION_TO_FSP = "error.flexPotential.cannotImportNoPermissionToFSP";
    public static final String CANNOT_IMPORT_FP_BECAUSE_NO_PERMISSION_TO_FP = "error.flexPotential.cannotImportNoPermissionToFP";
    public static final String CANNOT_IMPORT_FP_BECAUSE_NOT_FIND_PRODUCT = "error.flexPotential.cannotImportNotFindProduct";
    public static final String CANNOT_IMPORT_FP_BECAUSE_NOT_FIND_UNIT = "error.flexPotential.cannotImportNotFindUnit";
    public static final String CANNOT_IMPORT_FP_BECAUSE_NOT_FIND_FSP = "error.flexPotential.cannotImportNotFindFSP";
    public static final String CANNOT_IMPORT_FP_BECAUSE_NOT_FIND_FP = "error.flexPotential.cannotImportNotFindFlexPotential";
    public static final String FP_PRODUCT_IS_NOT_ACTIVE = "error.flexPotential.productIsNotActive";
    public static final String FP_EXCEEDS_THE_EXPIRY_DATE_OF_THE_PRODUCT = "error.flexPotential.fpExceedsTheExpiryDateOfTheProduct";
    public static final String FP_UNIT_IS_NOT_ACTIVE = "error.flexPotential.unitIsNotActive";
    public static final String FP_EXCEEDS_THE_EXPIRY_DATE_OF_THE_UNIT = "error.flexPotential.fpExceedsTheExpiryDateOfTheUnit";
    public static final String FP_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES = "error.flexPotential.cannotBeActiveBecauseDateNowIsNotBetweenValidFromToDates";
    public static final String FP_FULL_ACTIVATION_TIME_CANNOT_BE_HIGHER_THAN_PRODUCT_MAX_FULL_ACTIVATION_TIME = "error.flexPotential.fullActivationTimeCannotBeHigherThanProductMaxFullActivationTime";
    public static final String FP_MIN_DELIVERY_DURATION_CANNOT_BE_LESS_THAN_PRODUCT_MIN_REQUIRED_DELIVERY_DURATION = "error.flexPotential.minDeliveryDurationCannotBeLessThanProductMinRequiredDeliveryDuration";
    public static final String FP_NEW_FP_HAS_BEEN_ADDED_BEFORE_SAVING_THIS_ONE = "error.flexPotential.newFlexibilityPotentialHasBeenAddedBeforeSavingThisOne";
    public static final String CANNOT_MOVE_EMPTY_FLEX_POTENTIAL_TO_FLEX_REGISTER = "error.flexPotential.cannotMoveEmptyFlexPotentialToFlexRegister";
    public static final String CANNOT_CREATE_FLEX_POTENTIAL_WITH_NO_DERS = "error.flexPotential.cannotCreateFlexPotentialWithNoDers";
    public static final String FLEX_POTENTIAL_CANNOT_BE_CREATED_BY_TSO_AND_DSO = "error.flexPotential.flexPotentialCannotBeCreatedByTsoAndDso";
    public static final String FLEX_POTENTIAL_CANNOT_BE_DELETED_BY_TSO_AND_DSO = "error.flexPotential.flexPotentialCannotBeDeletedByTsoAndDso";
    public static final String FLEX_POTENTIAL_NOTHING_TO_EXPORT = "error.flexPotential.nothingToExport";


    //PRODUCT
    public static final String USER_HAS_NO_ROLE_TO_BE_PSO_USER = "error.product.userHasNoRoleToBePsoUser";
    public static final String USER_HAS_NO_ROLE_TO_BE_SSO_USER = "error.product.userHasNoRoleToBeSsoUser";
    public static final String PRODUCT_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES = "error.product.cannotBeActiveBecauseDateNowIsNotBetweenValidFromToDates";
    public static final String NEW_PRODUCT_HAS_BEEN_ADDED_BEFORE_SAVING_THIS_ONE = "error.product.newProductHasBeenAddedBeforeSavingThisOne";
    public static final String PRODUCT_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_FLEX_POTENTIALS = "error.product.cannotDeleteBecauseOfJoinedFlexPotentials";
    public static final String CANNOT_MODIFY_FULL_NAME_BECAUSE_IS_ONGOING_AUCTION_WITH_THIS_PRODUCT = "error.product.cannotModifyFullNameBecauseIsOngoingAuctionWithThisProduct";
    public static final String ONE_OF_BALANCING_AND_CMVC_SHOULD_BE_MARKED = "error.product.oneOfBalancingAndCmvcShouldBeMarked";
    public static final String PRODUCT_NOTHING_TO_EXPORT = "error.product.nothingToExport";

    //FORECASTED PRICES
    public static final String FORECASTED_PRICES_TEMPLATE_INCORRECT = "error.forecastedPrices.templateIncorrect";
    public static final String CANNOT_DELETE_BECAUSE_FORECASTED_DAY_HAS_ALREADY_STARTED_OR_ENDED = "error.forecastedPrices.cannotDeleteForecastedPricesBecauseForecastDayHasAlreadyStartedOrEnded";
    public static final String NOT_ALLOWED_TO_IMPORT_FORECASTED_PRICES_WITH_PAST_OR_CURRENT_DATE = "error.forecastedPrices.notAllowedToImportForecastedPricesWithPastOrCurrentDate";
    public static final String SAVING_THE_SAME_FORECASTED_PRICES = "SAVING_THE_SAME_FORECASTED_PRICES";

    //USER
    public static final String CANNOT_DELETE_BECAUSE_USER_IS_FSP_OWNER = "error.user.cannotDeleteBecauseUserIsFspOwner";
    public static final String CANNOT_DELETE_BECAUSE_USER_HAS_ACTIVE_PRODUCT = "error.user.cannotDeleteBecauseUserHasActiveProduct";
    public static final String CANNOT_CREATE_BECAUSE_NEW_USER_CANNOT_ALREADY_HAVE_ID = "error.user.cannotCreateBecauseNewUserAlreadyHaveId";
    public static final String CANNOT_CREATE_BECAUSE_REQUEST_CONTAINS_NOT_ALLOWED_PROPERTIES = "error.user.cannotCreateBecauseRequestContainsNotAllowedProperties";
    public static final String CANNOT_CHANGE_USER_FSP_BECAUSE_USER_IS_FSP_OWNER = "error.user.cannotChangeUserFspBecauseUserIsFspOwner";
    public static final String CANNOT_CHANGE_ROLE_FOR_FSP_USER_CANDIDATE = "error.user.cannotChangeRoleForFspUserCandidate";
    public static final String CANNOT_CHANGE_ROLE_FOR_FSP_USER_OWNER = "error.user.cannotChangeRoleForFspUserOwner";
    public static final String CANNOT_CHANGE_EMAIL_FOR_EXISTING_USER = "error.user.cannotChangeEmailForExistingUser";
    public static final String CANNOT_CHANGE_LOGIN_FOR_EXISTING_USER = "error.user.cannotChangeLoginForExistingUser";
    public static final String CANNOT_ACTIVATE_FSP_USER_WHICH_FSP_IS_NOT_ACTIVATED = "error.user.cannotActivateFspUserWhichFspIsNotActivated";
    public static final String CANNOT_SET_PASSWORD_DUE_TO_INCORRECT_LENGTH = "error.user.cannotSetPasswordDueToIncorrectLength";
    public static final String ERR_LOGIN_ALREADY_EXISTS = "error.user.loginAlreadyExists";
    public static final String ERR_USER_WITH_NO_COMPANY = "error.user.noCompany";

    //UNIT (DER)
    public static final String USER_HAS_NO_AUTHORITY_TO_MODIFY_UNIT_CERTIFIED = "error.unit.userHasNoAuthorityToModifyUnitCertified";
    public static final String CANNOT_MODIFY_UNIT_FSP_BY_FSP_USER = "error.unit.cannotModifyFspByFspUser";
    public static final String UNIQUE_UNIT_NAME = "error.unit.uniqueUnitName";
    public static final String UNIT_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES = "error.unit.cannotBeActiveBecauseDateNowIsNotBetweenValidFromToDates";
    public static final String ONLY_TA_AND_FSP_CAN_DELETE_UNIT = "error.unit.onlyTaAndFspCanDeleteUnit";
    public static final String ONLY_TA_AND_FSP_CAN_CREATE_UNIT = "error.unit.onlyTaAndFspCanCreateUnit";
    public static final String ONLY_TA_AND_FSP_CAN_MODIFY_UNIT = "error.unit.onlyTaAndFspCanModifyUnit";
    public static final String ONLY_DSO_USERS_CAN_FILL_COUPLING_POINT_ID_FOR_NEW_UNIT = "error.unit.onlyDsoUsersCanFillCouplingPointIdForNewUnit";
    public static final String ONLY_DSO_USERS_CAN_FILL_MRID_FOR_NEW_UNIT = "error.unit.onlyDsoUsersCanFillMridForNewUnit";
    public static final String ONLY_DSO_USERS_CAN_MODIFY_UNIT_COUPLING_POINT_ID = "error.unit.onlyDsoUsersCanModifyUnitCouplingPointId";
    public static final String ONLY_DSO_USERS_CAN_MODIFY_UNIT_MRID = "error.unit.onlyDsoUsersCanModifyUnitMrid";
    public static final String UNIT_NEW_UNIT_HAS_BEEN_ADDED_BEFORE_SAVING_THIS_ONE = "error.unit.newUnitHasBeenAddedBeforeSavingThisOne";
    public static final String BEFORE_CERTIFICATION_OF_DER_COUPLING_POINT_ID_MUST_BE_SET = "error.unit.beforeCertificationOfDerCouplingPointIdMustBeSet";
    public static final String UNIT_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_FLEX_POTENTIALS = "error.unit.cannotDeleteBecauseOfJoinedFlexPotentials";
    public static final String UNIT_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_SCHEDULING_UNITS = "error.unit.cannotDeleteBecauseOfJoinedSchedulingUnits";
    public static final String UNIT_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_SUBPORTFOLIOS = "error.unit.cannotDeleteBecauseOfJoinedSubportfolios";
    public static final String UNIT_SOURCE_POWER_CANNOT_BE_GREATER_THAN_CONNECTION_POWER = "error.unit.sourcePowerCannotBeGreaterThanConnectionPower";
    public static final String CANNOT_DEACTIVATE_BECAUSE_OF_JOINED_SUBPORTFOLIOS = "error.unit.cannotDeactivateBecauseOfJoinedSubportfolios";
    public static final String CANNOT_REMOVE_CERTIFICATION_BECAUSE_OF_JOINED_SUBPORTFOLIOS = "error.unit.cannotRemoveCertificationBecauseOfJoinedSubportfolios";
    public static final String POWER_STATION_HAS_INCORRECT_LOCALIZATION_TYPE = "error.unit.fieldPowerStationHasIncorrectLocalizationType";
    public static final String COUPLING_POINT_ID_HAS_INCORRECT_LOCALIZATION_TYPE = "error.unit.fieldCouplingPointIdHasIncorrectLocalizationType";
    public static final String COUPLING_POINT_ID_IS_ONE_ITEM_LIST = "error.unit.fieldCouplingPointIdIsOneItemList";
    public static final String UNIT_AT_LEAST_ONE_DER_TYPE_MUST_BE_SELECTED_FOR_DER = "error.unit.atLeastOneDerTypeMustBeSelectedForDer";
    public static final String UNIT_DER_TYPE_RECEPTION_IS_NOT_OF_RECEPTION_TYPE = "error.unit.selectedDerTypeReceptionIsNotReceptionType";
    public static final String UNIT_DER_TYPE_ENERGY_STORAGE_IS_NOT_OF_RECEPTION_TYPE = "error.unit.selectedDerTypeEnergyStorageIsNotEnergyStorageType";
    public static final String UNIT_DER_TYPE_GENERATION_IS_NOT_OF_RECEPTION_TYPE = "error.unit.selectedDerTypeGenerationIsNotGenerationType";
    public static final String CANNOT_REMOVE_CERTIFICATION_WHILE_ASSIGNED_TO_READY_FOR_TESTS_SU = "error.unit.cannotRemoveCertificationWhileAssignedToReadyForTestsSU";
    public static final String UNIT_NOTHING_TO_EXPORT = "error.unit.nothingToExport";

    //SELF SCHEDULE
    public static final String SELF_SCHEDULE_TEMPLATE_INCORRECT = "error.selfSchedule.templateIncorrect";
    public static final String SELF_SCHEDULE_IMPORT_UNCERTIFIED_DER = "error.selfSchedule.importUncertifiedDer";
    public static final String SELF_SCHEDULE_PRECISION_TOO_HIGH = "error.selfSchedule.precisionTooHigh";
    public static final String SELF_SCHEDULE_DER_EXCEEDS_TECHNICAL_LIMITS = "error.selfSchedule.derExceedsTechnicalLimits";
    public static final String SELF_SCHEDULE_DERS_EXCEED_TECHNICAL_LIMITS = "error.selfSchedule.dersExceedTechnicalLimits";
    public static final String NOT_ALLOWED_TO_IMPORT_SELF_SCHEDULE_WITH_PAST_OR_CURRENT_DATE = "error.selfSchedule.notAllowedToImportSelfScheduleWithPastOrCurrentDate";
    public static final String CANNOT_DELETE_BECAUSE_SELF_SCHEDULED_DAY_HAS_ALREADY_STARTED_OR_ENDED = "error.selfSchedule.cannotDeleteBecauseSelfScheduledDayHasAlreadyStartedOrEnded";
    public static final String CANNOT_DELETE_BECAUSE_SELF_SCHEDULE_USED_IN_OFFER = "error.selfSchedule.cannotDeleteBecauseSelfScheduleUsedInOffer";
    public static final String CANNOT_IMPORT_IF_ADMIN_USED_FSPS_TEMPLATE = "error.selfSchedule.cannotImportIfAdminUsedFspsTemplate";
    public static final String CANNOT_IMPORT_IF_FSPS_USED_ADMIN_TEMPLATE = "error.selfSchedule.cannotImportIfFspUsedAdminsTemplate";
    public static final String DUPLICATE_SELF_SCHEDULE = "DUPLICATE_SELF_SCHEDULE";
    public static final String SAVING_THE_SAME_SELF_SCHEDULES = "SAVING_THE_SAME_SELF_SCHEDULES";

    //FSP
    public static final String FSP_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES = "error.fsp.cannotBeActiveBecauseDateNowIsNotBetweenValidFromToDates";
    public static final String FSP_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_FLEX_POTENTIALS = "error.fsp.cannotDeleteBecauseOfJoinedFlexPotentials";
    public static final String FSP_NOTHING_TO_EXPORT = "error.fsp.nothingToExport";

    //DER TYPE
    public static final String CANNOT_CREATE_BECAUSE_DER_TYPE_WITH_THIS_DESCRIPTION_ALREADY_EXIST = "error.derType.cannotCreateBecauseDerTypeWithThisDescriptionAlreadyExist";
    public static final String CANNOT_UPDATE_BECAUSE_DER_TYPE_WITH_THIS_DESCRIPTION_ALREADY_EXIST = "error.derType.cannotUpdateBecauseDerTypeWithThisDescriptionAlreadyExist";
    public static final String CANNOT_DELETE_BECAUSE_DER_TYPE_IS_USED_BY_UNIT = "error.derType.cannotDeleteBecauseDerTypeIsUsedByUnit";

    //SCHEDULING_UNIT
    public static final String CANNOT_DELETE_SCHEDULING_UNIT_BECAUSE_IT_IS_ACTIVE = "error.schedulingUnit.cannotDeleteActive";
    public static final String CANNOT_CHANGE_SCHEDULING_UNIT_TYPE_BECAUSE_IT_IS_MARKED_AS_READY_FOR_TESTS =
        "error.schedulingUnit.cannotChangeSchedulingUnitTypeBecauseItIsMarkedAsReadyForTests";
    public static final String CANNOT_CHANGE_SCHEDULING_UNIT_TYPE_BECAUSE_DER_IS_ALREADY_JOINED_TO_IT =
        "error.schedulingUnit.cannotChangeSchedulingUnitTypeBecauseSomeDERIsAlreadyJoinedToIt";
    public static final String USER_HAS_NO_AUTHORITY_TO_MODIFY_SCHEDULING_UNIT_CERTIFIED = "error.schedulingUnit.userHasNoAuthorityToModifySchedulingUnitCertified";
    public static final String SCHEDULING_UNIT_CANNOT_BE_CERTIFIED_BECAUSE_IT_IS_NOT_MARKED_AS_READY_FOR_TESTS =
        "error.schedulingUnit.schedulingUnitCannotBeCertifiedIfItIsNotMarkedAsReadyForTests";
    public static final String SCHEDULING_UNIT_CANNOT_BE_MARKED_AS_READY_FOR_TESTS_WITH_NO_DERS_ASSIGNED =
        "error.schedulingUnit.schedulingUnitCannotBeMarkedAsReadyForTestsWithNoDersAssigned";
    public static final String CANNOT_MOVE_EMPTY_SCHEDULING_UNIT_TO_FLEX_REGISTER = "error.schedulingUnit.cannotMoveEmptySchedulingUnitToFlexRegister";
    public static final String CANNOT_DELETE_SCHEDULING_UNIT_BECAUSE_IT_HAS_JOINED_DERS = "error.schedulingUnit.cannotRemoveBecauseSchedulingUnitHasJoinedDERs";
    public static final String CANNOT_DELETE_SCHEDULING_UNIT_BECAUSE_IT_HAS_PROPOSALS_DERS = "error.schedulingUnit.cannotRemoveBecauseSchedulingUnitHasProposalsDERs";
    public static final String SCHEDULING_UNIT_CANNOT_REMOVE_DERS_FROM_SU_MARKED_AS_READY_FOR_TESTS = "error.schedulingUnit.cannotRemoveDersFromSuMarkedAsReadyForTests";
    public static final String SCHEDULING_UNIT_CANNOT_REMOVE_DERS_FROM_SU_MARKED_AS_CERTIFIED = "error.schedulingUnit.cannotRemoveDersFromSuMarkedAsCertified";
    public static final String CANNOT_REMOVE_CERTIFICATION_WHILE_SU_HAS_OPEN_AUCTION_OFFER = "error.schedulingUnit.cannotRemoveCertificationFromSuWithOpenAuctionOffer";
    public static final String CANNOT_REMOVE_CERTIFICATION_FROM_SU_WITH_ONGOING_DELIVERY = "error.schedulingUnit.cannotRemoveCertificationFromSuWithOngoingDelivery";
    public static final String SCHEDULING_UNIT_NOTHING_TO_EXPORT = "error.schedulingUnit.nothingToExport";

    //SCHEDULING_UNIT PROPOSAL
    public static final String UNIT_IS_ALREADY_JOINED_TO_SCHEDULING_UNIT = "error.schedulingUnitProposal.unitIsAlreadyJoinedToSchedulingUnit";
    public static final String FSP_OWNER_OF_PROPOSED_UNIT_HAS_ALREADY_JOINED_OTHER_UNITS_TO_ANOTHER_BSP =
        "error.schedulingUnitProposal.fspOwnerOfProposedUnitHasAlreadyJoinedOtherUnitsToAnotherBsp";
    public static final String FSPA_UNIT_IS_NOT_JOINED_TO_ANY_SUBPORTFOLIO = "error.schedulingUnitProposal.fspaUnitIsNotJoinedToAnySubportfolio";
    public static final String SOME_UNIT_FROM_SUBPORTFOLIO_OF_PROPOSED_UNIT_IS_ALREADY_JOINED_TO_ANOTHER_BSP =
        "error.schedulingUnitProposal.someUnitFromSubportfolioOfProposedUnitIsAlreadyJoinedToAnotherBSP";
    public static final String SOME_UNIT_FROM_SUBPORTFOLIO_OF_PROPOSED_UNIT_IS_ALREADY_JOINED_TO_ANOTHER_SCHEDULING_UNIT_BELONGING_TO_PROPOSED_BSP =
        "error.schedulingUnitProposal.someUnitFromSubportfolioOfProposedUnitIsAlreadyJoinedToAnotherSchedulingUnitOfProposedBSP";
    public static final String SCHEDULING_UNIT_PROPOSAL_ALREADY_EXISTS = "error.schedulingUnitProposal.proposalAlreadyExists";
    public static final String CANNOT_CREATE_OR_ACCEPT_SCHEDULING_UNIT_PROPOSAL_BECAUSE_SCHEDULING_UNIT_IS_MARKED_AS_READY_FOR_TEST =
        "error.schedulingUnitProposal.CannotCreateOrAcceptSchedulingUnitProposalBecauseSchedulingUnitIsMarkedAsReadyForTests";
    public static final String SCHEDULING_UNIT_PROPOSAL_CANNOT_RESEND_BECAUSE_OF_WRONG_CURRENT_STATUS =
        "error.schedulingUnitProposal.cannotResendBecauseOfWrongCurrentStatus";
    public static final String SCHEDULING_UNIT_PROPOSAL_CANNOT_CREATE_PROPOSITION_BECAUSE_ALREADY_EXITSTS_FROM_OPPOSITE_SIDE =
        "error.schedulingUnitProposal.cannotCreatePropositionBecauseAlreadyExistsFromOppositeSide";
    public static final String SCHEDULING_UNIT_PROPOSAL_CANNOT_CREATE_INVITATION_BECAUSE_ALREADY_EXITSTS_FROM_OPPOSITE_SIDE =
        "error.schedulingUnitProposal.cannotCreateInvitationBecauseAlreadyExistsFromOppositeSide";
    public static final String SCHEDULING_UNIT_PROPOSAL_CANNOT_JOIN_DER_WITH_SU_BECAUSE_DER_IS_NOT_LINKED_WITH_ALL_PRODUCTS_THAT_ARE_IN_SU_TYPE =
        "error.schedulingUnitProposal.cannotJoinDerToSuBecauseDerIsNotLinkedWithAllProductThatAreInSuType";


    //SCHEDULING_UNIT_TYPE
    public static final String CANNOT_CREATE_BECAUSE_SCHEDULING_UNIT_TYPE_WITH_THIS_DESCRIPTION_ALREADY_EXIST = "error.schedulingUnitType.cannotCreateBecauseSchedulingUnitTypeWithThisDescriptionAlreadyExist";
    public static final String CANNOT_UPDATE_BECAUSE_SCHEDULING_UNIT_TYPE_WITH_THIS_DESCRIPTION_ALREADY_EXIST = "error.schedulingUnitType.cannotUpdateBecauseSchedulingUnitTypeWithThisDescriptionAlreadyExist";
    public static final String CANNOT_DELETE_BECAUSE_SCHEDULING_UNIT_TYPE_IS_USED_BY_SCHEDULING_UNIT = "error.schedulingUnitType.cannotDeleteBecauseSchedulingUnitTypeIsUsedBySchedulingUnit";

    //LOCALIZATION_TYPE
    public static final String CANNOT_CREATE_BECAUSE_LOCALIZATION_TYPE_WITH_THIS_NAME_AND_TYPE_ALREADY_EXIST = "error.localizationType.cannotCreateBecauseLocalizationTypeWithThisNameAndTypeAlreadyExist";
    public static final String CANNOT_UPDATE_BECAUSE_LOCALIZATION_TYPE_WITH_THIS_NAME_AND_TYPE_ALREADY_EXIST = "error.localizationType.cannotUpdateBecauseLocalizationTypeWithThisNameAndTypeExist";
    public static final String CANNOT_DELETE_BECAUSE_LOCALIZATION_TYPE_IS_USED_BY_UNIT = "error.localizationType.cannotDeleteBecauseLocalizationTypeIsUsedByUnit";
    public static final String CANNOT_DELETE_BECAUSE_LOCALIZATION_TYPE_IS_USED_BY_SUBPORTFOLIO = "error.localizationType.cannotDeleteBecauseLocalizationTypeIsUsedBySubportfolio";

    //SUBPORTFOLIO
    public static final String CANNOT_DELETE_ACTIVE_SUBPORTFOLIO = "error.subportfolio.cannotDeleteActive";
    public static final String CANNOT_DELETE_SUBPORTFOLIO_CONTAINING_DERS = "error.subportfolio.cannotDeleteSubportfolioContainingDers";
    public static final String CANNOT_CERTIFY = "error.subportfolio.cannotCertify";
    public static final String CANNOT_MODIFY_FSPA = "error.subportfolio.cannotModifyFspa";
    public static final String CANNOT_ADD_DERS_NOT_BELONGING_TO_ANOTHER_FSPA = "error.subportfolio.cannotAddDersNotBelongingToAnotherFspa";
    public static final String CANNOT_ADD_DERS_IN_SCHEDULING_UNIT = "error.subportfolio.cannotAddDersInSchedulingUnit";
    public static final String CANNOT_CERTIFY_SUBPORTFOLIO_WITHOUT_DERS = "error.subportfolio.cannotCertifySubportfolioWithoutDers";
    public static final String SUBPORTFOLIO_CANNOT_BE_CREATED_BY_TSO_AND_DSO = "error.subportfolio.subportfolioCannotBeCreatedByTsoAndDso";
    public static final String SUBPORTFOLIO_CANNOT_BE_DELETED_BY_TSO_AND_DSO = "error.subportfolio.subportfolioCannotBeDeletedByTsoAndDso";
    public static final String SUBPORTFOLIO_NOTHING_TO_EXPORT = "error.subportfolio.nothingToExport";


    //AUCTIONS SERIRES
    public static final String CAPACITY_GATE_CLOSURE_TIME_IS_BEFORE_CAPACITY_GATE_OPENING_TIME = "error.auctionSeries.capacityGateClosureTimeIsBeforeCapacityGateOpeningTime";
    public static final String CAPACITY_MIN_DESIRED_IS_GREATER_THAN_CAPACITY_MAX_DESIRED = "error.auctionSeries.capacityMinDesiredIsGreaterThanCapacityMaxDesired";
    public static final String CAPACITY_MIN_DESIRED_IS_LESS_THAN_PRODUCT_MIN_BID_SIZE = "error.auctionSeries.capacityMinDesiredIsLessThanProductMinBidSize";
    public static final String CAPACITY_MAX_DESIRED_IS_GREATER_THAN_PRODUCT_MAX_BID_SIZE = "error.auctionSeries.capacityMaxDesiredIsGreaterThanProductMaxBidSize";
    public static final String ENERGY_GATE_CLOSURE_TIME_IS_BEFORE_ENERGY_GATE_OPENING_TIME = "error.auctionSeries.energyGateClosureTimeIsBeforeEnergyGateOpeningTime";
    public static final String ENERGY_MIN_DESIRED_IS_GREATER_THAN_ENERGY_MAX_DESIRED = "error.auctionSeries.energyMinDesiredIsGreaterThanEnergyMaxDesired";
    public static final String ENERGY_MIN_DESIRED_IS_LESS_THAN_PRODUCT_MIN_BID_SIZE = "error.auctionSeries.energyMinDesiredIsLessThanProductMinBidSize";
    public static final String ENERGY_MAX_DESIRED_IS_GREATER_THAN_PRODUCT_MAX_BID_SIZE = "error.auctionSeries.energyMaxDesiredIsGreaterThanProductMaxBidSize";
    public static final String LAST_AUCTION_DATE_IS_BEFORE_FIRST_AUCTION_DATE = "error.auctionSeries.lastAuctionDateIsBeforeFirstAuctionDate";
    public static final String FIRST_AUCTION_DATE_IS_BEFORE_PRODUCT_VALID_FROM = "error.auctionSeries.firstAuctionDateIsBeforeProductValidFrom";
    public static final String LAST_AUCTION_DATE_IS_AFTER_PRODUCT_VALID_TO = "error.auctionSeries.lastAuctionDateIsAfterProductValidTo";
    public static final String FIRST_AUCTION_DATE_MAY_START_TOMORROW_AT_THE_EARLIEST = "error.auctionSeries.firstAuctionDayMayStartTomorrowAtTheEarliest";
    public static final String ENERGY_AVAILABILITY_TO_IS_BEFORE_ENERGY_AVAILABILITY_FROM = "error.auctionSeries.energyAvailabilityToIsBeforeEnergyAvailabilityFrom";
    public static final String CAPACITY_AVAILABILITY_TO_IS_BEFORE_CAPACITY_AVAILABILITY_FROM = "error.auctionSeries.capacityAvailabilityToIsBeforeCapacityAvailabilityFrom";
    public static final String REQUIRED_FIELDS_ARE_NOT_COMPLETED = "error.auctionSeries.requiredFieldsAreNotCompleted";
    public static final String NON_REQUIRED_FIELDS_ARE_COMPLETED = "error.auctionSeries.nonRequiredFieldsAreCompleted";
    public static final String CAPACITY_GATE_TIME_AND_ENERGY_GATE_TIME_IS_OVERLAPPED = "error.auctionSeries.capacityGateTimeAndEnergyGateTimeIsOverlapped";
    public static final String CANNOT_MODIFY_PRODUCT_IN_AUCTION_SERIES = "error.auctionSeries.cannotModifyProductInAuctionSeries";
    public static final String CANNOT_MODIFY_AUCTION_TYPE_IN_AUCTION_SERIES = "error.auctionSeries.cannotModifyAuctionTypeInAuctionSeries";
    public static final String CANNOT_MODIFY_FIRST_AUCTION_DATE_BECAUSE_DAY_AHEAD_AUCTION_HAVE_ALREADY_STARTED =
        "error.auctionSeries.cannotModifyFirstAuctionDateBecauseDayAheadAuctionHaveAlreadyStarted";
    public static final String CANNOT_MODIFY_LAST_AUCTION_DATE_BECAUSE_EXISTS_AUCTIONS_AFTER_GIVEN_DATE =
        "error.auctionSeries.cannotModifyLastAuctionDateBecauseExistsAuctionsAfterGivenDate";
    public static final String CANNOT_MODIFY_LAST_AUCTION_DATE_BECAUSE_AUCTION_SERIES_IS_EXPIRED =
        "error.auctionSeries.cannotModifyLastAuctionDateBecauseAuctionSeriesIsExpired";
    public static final String GATE_OPENING_TIME_MUST_BE_LATER = "error.auctionSeries.gateOpeningTimeMustBeLater";
    public static final String PRODUCT_DOES_NOT_HAVE_BALANCING_FLAG = "error.auctionSeries.productDoesNotHaveBalancingType";
    public static final String CAPACITY_SERIES_HAS_PRODUCT_WITH_WRONG_DIRECTION = "error.auctionSeries.capacitySeriesHasProductWithWrongDirection";
    public static final String ENERGY_SERIES_HAS_PRODUCT_WITH_WRONG_DIRECTION = "error.auctionSeries.energySeriesHasProductWithWrongDirection";

    //AUCTION CM&VC
    public static final String DELIVERY_DATE_TO_BEFORE_DELIVERY_DATE_FROM = "error.auctionCmvc.deliveryDateToBeforeDeliveryDateFrom";
    public static final String GATE_CLOSURE_TIME_BEFORE_GATE_OPENING_TIME = "error.auctionCmvc.gateClosureTimeBeforeGateOpeningTime";
    public static final String MAX_DESIRED_POWER_BIGGER_THAN_MIN_DESIRED_POWER = "error.auctionCmvc.maxDesiredPowerIsBiggerThanMinDesiredPower";
    public static final String GATE_CLOSURE_TIME_IS_AFTER_PRODUCT_VALID_TO = "error.auctionCmvc.gateClosureTimeIsAfterProductValidTo";
    public static final String GATE_CLOSURE_TIME_IS_AFTER_DELIVERY_DATE_FROM = "error.auctionCmvc.gateClosureTimeIsAfterDeliveryDateFrom";
    public static final String PRODUCT_IS_INACTIVE_OR_DATE_VALID_TO_EXPIRED = "error.auctionCmvc.productIsInactiveOrDateValidToExpired";
    public static final String CANNOT_DELETE_OPEN_AUCTION = "error.auctionCmvc.cannotDeleteOpenAuction";
    public static final String CANNOT_DELETE_CLOSED_AUCTION = "error.auctionCmvc.cannotDeleteClosedAuction";
    public static final String AUCTION_NAME_NOT_UNIQUE = "error.auctionCmvc.auctionNameNotUnique";
    //AUCTION CM&VC OFFERS
    public static final String AUCTION_CMVC_OFFER_CANNOT_CREATE_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN = "error.auctionCmvc.offer.cannotCreateOfferBecauseAuctionIsNotOpen";
    public static final String AUCTION_CMVC_OFFER_CANNOT_MODIFY_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN = "error.auctionCmvc.offer.cannotModifyOfferBecauseAuctionIsNotOpen";
    public static final String AUCTION_CMVC_OFFER_WRONG_OFFER_STATUS_FOR_NEW_OFFER = "error.auctionCmvc.offer.illegalOfferStatusForNewOffer";
    public static final String AUCTION_CMVC_OFFER_ACCEPTED_FIELDS_WHILE_CREATING_NEW_OFFER_ARE_NOT_EQUAL_TO_ITS_SUBSTITUTES =
        "error.auctionCmvc.offer.acceptedFieldsWhileCreatingNewOfferAreNotEqualToItsSubstitutes";
    public static final String AUCTION_CMVC_OFFER_VOLUME_OF_OFFER_CANNOT_EXCEED_THE_RANGE =
        "error.auctionCmvc.offer.volumeOfOfferCannotExceedTheRange";
    public static final String AUCTION_CMVC_OFFER_ACCEPTED_VOLUME_OF_OFFER_CANNOT_EXCEED_THE_RANGE =
        "error.auctionCmvc.offer.acceptedVolumeOfOfferCannotExceedTheRange";
    public static final String AUCTION_CMVC_OFFER_DELIVERY_PERIOD_FROM_TO_ARE_OVERLAPPED = "error.auctionCmvc.offer.deliverPeriodFromToAreOverlapped";
    public static final String AUCTION_CMVC_OFFER_ACCEPTED_DELIVERY_PERIOD_FROM_TO_ARE_OVERLAPPED = "error.auctionCmvc.offer.acceptedDeliverPeriodFromToAreOverlapped";
    public static final String AUCTION_CMVC_OFFER_PRODUCT_OF_ACUTION_AND_POTENTIAL_MUST_BE_THE_SAME = "error.auctionCmvc.offer.productOfAuctionAndPotentialMustBeTheSame";
    public static final String AUCTION_CMVC_OFFER_MODIFICATION_OF_OFFER_IS_ONLY_ALLOWED_IF_OFFER_STATUS_IS_PENDING =
        "error.auctionCmvc.offer.modificationOfOfferIsOnlyAllowedIfOfferStatusIsPending";
    public static final String AUCTION_CMVC_OFFER_CANNOT_CHANGE_OFFER_STATUS = "error.auctionCmvc.offer.cannotChangeOfferStatus";
    public static final String AUCTION_CMVC_OFFER_CANNOT_DELETE_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN = "error.auctionCmvc.offer.cannotDeleteOfferBecauseAuctionIsNotOpen";
    public static final String AUCTION_CMVC_OFFER_ILLEGAL_TYPE = "error.auctionCmvc.offer.illegalType";
    public static final String AUCTION_CMVC_OFFER_CANNOT_MODIFY_ACCPTED_VOLUME_BECAUSE_VOLUME_DIVISIBILITY_IS_FALSE_OR_AUCTION_IS_NOT_CLOSED = "error.auctionCmvc.offer.cannotModifyAcceptedVolumeBecauseVolumeDivisibilityIsFalseOrAuctionIsNotClosed";
    public static final String AUCTION_CMVC_OFFER_ACCEPTED_VOLUME_CANNOT_BE_GREATER_THAN_VOLUME_OF_OFFER =
        "error.auctionCmvc.offer.acceptedVolumeCannotBeGraterThanVolumeOfOffer";
    public static final String AUCTION_CMVC_OFFER_CANNOT_MODIFY_ACCPTED_DELIVERY_PERIOD_BECAUSE_DELIVERY_PERIOD_DIVISIBILITY_IS_FALSE_OR_AUCTION_IS_NOT_CLOSED = "error.auctionCmvc.offer.cannotModifyAcceptedDeliveryPeriodBecauseVolumeDivisibilityIsFalseOrAuctionIsNotClosed";
    public static final String AUCTION_CMVC_OFFER_ACCEPTED_DELIVERY_PERIOD_CANNOT_EXCEED_THE_DELIVERY_PERIOD_RANGE =
        "error.auctionCmvc.offer.acceptedDeliveryPeriodCannotExceedTheDeliveryPeriodRange";
    public static final String AUCTION_CMVC_OFFER_USER_HAS_NO_ACCESS_TO_FLEX_POTENTIAL = "error.auctionCmvc.offer.userHasNoAccessToFlexPotential";
    public static final String AUCTION_CMVC_OFFER_CANNOT_CHANGE_FLEX_POTENTIAL_IN_SUBMITTED_OFFER = "error.auctionCmvc.offer.cannotChangeFlexPotentialInSubmittedOffer";
    public static final String AUCTION_CMVC_OFFER_CANNOT_CHANGE_FSP_NAME_IN_SUBMITTED_OFFER = "error.auctionCmvc.offer.cannotChangeFspNameInSubmittedOffer";
    public static final String AUCTION_CMVC_OFFER_DELIVERY_PERIOD_HAS_INVALID_MINUTE = "error.auctionCmvc.offer.deliveryPeriodHasInvalidMinute";
    public static final String AUCTION_CMVC_OFFER_ACCEPTED_DELIVERY_PERIOD_HAS_INVALID_MINUTE = "error.auctionCmvc.offer.acceptedDeliveryPeriodHasInvalidMinute";

    //AUCTION OFFERS IMPORT
    public static final String AUCTION_DA_IMPORT_TEMPLATE_INCORRECT = "error.dayAhead.offer.import.wrong.file";
    public static final String IMPORT_OFFERS_TEMPLATE_INCORRECT = "error.auction.offer.import.templateIncorrect";
//    public static final String IMPORT_NO_OFFERS_TO_IMPORT = "error.auction.offer.import.noOffersToImport";
    public static final String IMPORT_OFFER_COULD_NOT_FIND_MATCHING_ID = "error.auction.offer.import.couldNotFindMatchingId";
    public static final String IMPORT_COULD_NOT_IDENTIFY_NAME_OF_STATUS = "error.auction.offer.import.couldNotIdentifyNameOfStatus";
    public static final String IMPORT_NOT_ALLOWED_TO_CHANGE_ACCEPTED_VOLUME = "error.auction.offer.import.notAllowedToChangeAcceptedVolume";
    public static final String IMPORT_NOT_ALLOWED_TO_CHANGE_ACCEPTED_DELIVERY_PERIOD = "error.auction.offer.import.notAllowedToChangeAcceptedDeliveryPeriod";
    public static final String IMPORT_WRONG_FORMAT_OF_ACCEPTED_VOLUME = "error.auction.offer.import.wrongFormatOfAcceptedVolume";
    public static final String IMPORT_WRONG_FORMAT_OF_ACCEPTED_DELIVERY_PERIOD = "error.auction.offer.import.wrongFormatOfAcceptedDeliveryPeriod";
    public static final String IMPORT_ACCEPTED_DELIVERY_PERIOD_CANNOT_EXCEED_THE_DELIVERY_PERIOD_RANGE = "error.auction.offer.import.acceptedDeliveryPeriodCannotExceedTheDeliveryPeriodRange";
    public static final String IMPORT_ACCEPTED_DELIVERY_PERIOD_CANNOT_HAVE_ZERO_LENGTH = "error.auction.offer.import.acceptedDeliveryPeriodCannotHaveZeroLength";
    public static final String IMPORT_ACCEPTED_VOLUME_CANNOT_BE_GREATER_THAN_VOLUME_OF_OFFER = "error.auction.offer.import.acceptedVolumeCannotBeGreaterThanVolumeOfOffer";
    public static final String IMPORT_ACCEPTED_VOLUME_CANNOT_BE_LOWER_THAN_ZERO = "error.auction.offer.import.acceptedVolumeCannotBeLowerThanZero";
    public static final String IMPORT_VOLUME_IN_BAND_CANNOT_BE_MODIFIED_IF_HIGHER_BAND_EXISTS = "error.auction.offer.import.volumeInBandCannotBeModifiedIfHigherBandExists";
    public static final String IMPORT_SU_VOLUME_CANNOT_BE_GREATER_THAN_INITIAL_SU_VOLUME_OF_OFFER = "error.auction.offer.import.importSuVolumeCannotBeGreaterThanInitialSuVolumeOfOffer";
    public static final String IMPORT_OFFER_COULD_NOT_IMPORT_BECAUSE_AUCTION_IS_OPEN = "error.auction.offer.import.couldNotImportBecauseAuctionIsOpen";
    public static final String IMPORT_OFFER_COULD_NOT_IMPORT_BECAUSE_NOT_ALLOWED_STATUS_CHANGE = "error.auction.offer.import.couldNotImportBecauseNotAllowedStatusChange";
    public static final String AUCTION_DA_OFFER_IMPORT_NO_VALID_TIMESTAMPS = "error.auction.offer.import.noValidTimestamps";
    public static final String AUCTION_DA_OFFER_IMPORT_UNSUPPORTED_AUCTION_TYPE = "error.auction.offer.import.unsupportedAuctionType";
    public static final String AUCTION_DA_OFFER_IMPORT_UNSUPPORTED_PRODUCT_DIRECTION = "error.auction.offer.import.unsupportedProductDirection";
    public static final String AUCTION_DA_OFFER_IMPORT_INCOHERENT_SELF_SCHEDULE = "error.auction.offer.import.incoherentSelfSchedule";
    public static final String AUCTION_DA_OFFER_IMPORT_DELIVERY_PERIOD_NOT_SET = "error.auction.offer.import.deliveryPeriodNotSet";
    public static final String IMPORT_CANNOT_IMPORT_OFFER_WITH_STATUS_REJECTED_OR_ACCEPTED = "error.auction.offer.import.cannotImportBidWithStatusAcceptedOrRejected";
    public static final String IMPORT_OTHER = "error.auction.offer.import.other";
    public static final String IMPORT_NOTHING_CHANGED = "error.auction.offer.import.nothingChanged";
    public static final String IMPORT_ACTIVE_POWER_OUT_OF_RANGE = "error.auction.offer.import.activePowerOutOfRange";
    public static final String AUCTION_DA_OFFER_CAN_ONLY_IMPORT_WITH_STATUS_VOLUMES_VERIFIED = "error.auction.offer.import.canOnlyImportDaOfferWithStatusVolumesVerified";
    //DA CAPACITY
    public static final String IMPORT_INVALID_SCHEDULING_UNIT_VOLUME = "error.auction.offer.import.cannotImportDaBecauseFindInvalidSchedulingUnitVolume";
    public static final String IMPORT_INVALID_SCHEDULING_UNIT_PRICE = "error.auction.offer.import.cannotImportDaBecauseFindInvalidSchedulingUnitPrice";
    public static final String IMPORT_CANNOT_CHANGE_SCHEDULING_UNIT_PRICE = "error.auction.offer.import.cannotChangeSchedulingUnitPrice";
    public static final String IMPORT_CANNOT_CHANGE_SU_SELF_SCHEDULE_VOLUMES = "error.auction.offer.import.cannotChangeSelfScheduleVolumes";

    // ALGORITHM RESULTS IMPORT
    public static final String IMPORT_ALGORITHM_RESULT_COULD_NOT_FIND_DER_MATCHING_NAME = "error.algorithm.import.couldNotFindDerMatchingName";
    public static final String IMPORT_ALGORITHM_RESULT_COULD_NOT_FIND_PRODUCT_MATCHING_NAME = "error.algorithm.import.couldNotFindProductMatchingName";
    public static final String IMPORT_ALGORITHM_RESULT_PRICES_BETWEEN_AUCTION_AND_FILE_DIFFER = "error.algorithm.import.pricesBetweenAuctionAndFileDiffer";
    public static final String IMPORT_ALGORITHM_RESULT_INVALID_PRODUCT_DIRECTION = "error.algorithm.import.invalidProductDirection";
    public static final String IMPORT_ALGORITHM_RESULT_COULD_NOT_PARSE_BECAUSE_NO_FILES_FOUND = "error.algorithm.import.couldNotParseBecauseNoFilesFound";
    public static final String IMPORT_ALGORITHM_RESULT_ERROR_PARSING_PBCM_RESULTS = "error.algorithm.import.errorParsingPbcmResults";

    //AUCTION DA OFFERS
    public static final String AUCTION_DA_OFFER_USER_CANNOT_ADD_BID = "error.dayAhead.offer.cannotAddBid";
    public static final String AUCTION_DA_OFFER_USER_HAS_NO_PRIVILEGES_TO_EDIT_BID = "error.dayAhead.offer.userHasNoPrivilegesToEditBid";
    public static final String AUCTION_DA_OFFER_WRONG_OFFER_STATUS_FOR_NEW_OFFER = "error.dayAhead.offer.illegalOfferStatusForNewOffer";
    public static final String AUCTION_DA_OFFER_CANNOT_CREATE_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN = "error.dayAhead.offer.cannotCreateOfferBecauseAuctionIsNotOpen";
    public static final String AUCTION_DA_OFFER_CANNOT_MODIFY_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN = "error.dayAhead.offer.cannotModifyOfferBecauseAuctionIsNotOpen";
    public static final String AUCTION_DA_OFFER_CANNOT_DELETE_OFFER_BECAUSE_AUCTION_IS_NOT_OPEN = "error.dayAhead.offer.cannotDeleteOfferBecauseAuctionIsNotOpen";
    public static final String AUCTION_DA_OFFER_CANNOT_DELETE_OFFER_BECAUSE_ITS_USED_IN_CALCULATIONS = "error.dayAhead.offer.cannotDeleteOfferBecauseItsUsedInCalculations";
    public static final String AUCTION_DA_OFFER_ACCEPTED_FIELDS_WHILE_CREATING_NEW_OFFER_ARE_NOT_EQUAL_TO_ITS_SUBSTITUTES =
        "error.dayAhead.offer.acceptedFieldsWhileCreatingNewOfferAreNotEqualToItsSubstitutes";
    public static final String AUCTION_DA_OFFER_DELIVERY_PERIOD_FROM_TO_ARE_OVERLAPPED = "error.dayAhead.offer.deliverPeriodFromToAreOverlapped";
    public static final String AUCTION_DA_OFFER_PRODUCT_OF_ACUTION_AND_SCHEDULING_MUST_BE_THE_SAME = "error.dayAhead.offer.productOfAuctionAndSchedulingMustBeTheSame";
    public static final String AUCTION_DA_OFFER_MODIFICATION_OF_OFFER_IS_ONLY_ALLOWED_IF_OFFER_STATUS_IS_PENDING_OR_VOLUMES_VERIFIED =
        "error.dayAhead.offer.modificationOfOfferIsOnlyAllowedIfOfferStatusIsPendingOrVolumesVerified";
    public static final String AUCTION_DA_OFFER_ACCEPTED_VOLUME_CANNOT_EXCEED_THE_RANGE =
        "error.dayAhead.offer.acceptedVolumeCannotExceedTheRange";
    public static final String AUCTION_DA_OFFER_VOLUME_CANNOT_EXCEED_THE_RANGE =
        "error.dayAhead.offer.acceptedVolumeCannotExceedTheRange";
    public static final String AUCTION_DA_OFFER_CANNOT_MODIFY_ACCEPTED_VOLUME_BECAUSE_VOLUME_DIVISIBILITY_IS_FALSE_OR_AUCTION_IS_NOT_CLOSED =
        "error.dayAhead.offer.cannotModifyAcceptedVolumeBecauseVolumeDivisibilityIsFalseOrAuctionIsNotClosed";
    public static final String AUCTION_DA_OFFER_CANNOT_MODIFY_ACCPTED_DELIVERY_PERIOD_BECAUSE_DELIVERY_PERIOD_DIVISIBILITY_IS_FALSE_OR_AUCTION_IS_NOT_CLOSED =
        "error.dayAhead.offer.cannotModifyAcceptedDeliveryPeriodBecauseVolumeDivisibilityIsFalseOrAuctionIsNotClosed";
    public static final String AUCTION_DA_OFFER_ACCEPTED_DELIVERY_PERIOD_CANNOT_EXCEED_THE_DELIVERY_PERIOD_RANGE =
        "error.dayAhead.offer.acceptedDeliveryPeriodCannotExceedTheDeliveryPeriodRange";
    public static final String AUCTION_DA_OFFER_USER_HAS_NO_ACCESS_TO_SCHEDULING_UNIT = "error.dayAhead.offer.userHasNoAccessToSchedulingUnit";
    public static final String AUCTION_DA_OFFER_CANNOT_CHANGE_SCHEDULING_UNIT_IN_SUBMITTED_OFFER = "error.dayAhead.offer.cannotChangeSchedulingUnitInSubmittedOffer";
    public static final String AUCTION_DA_OFFER_CANNOT_CHANGE_BSP_NAME_NOR_SCHEDULING_UNIT_IN_SUBMITTED_OFFER =
        "error.dayAhead.offer.cannotChangeBspNameNorSchedulingUnitInSubmittedOffer";
    public static final String AUCTION_DA_OFFER_SELECTED_DER_IS_NOT_JOINED_TO_SCHEDULING_UNIT =
        "error.dayAhead.offer.selectedDerIsNotJoinedToSchedulingUnit";
    public static final String AUCTION_DA_ENERGY_OFFER_INVALID_BAND_NUMBER_VALUE = "error.dayAhead.offer.illegalBandNumberValueInEnergyOffer";
    public static final String AUCTION_DA_CAPACITY_OFFER_INVALID_BAND_NUMBER_VALUE = "error.dayAhead.offer.illegalBandNumberValueInCapacityOffer";
    public static final String AUCTION_DA_ENERGY_OFFER_NOT_SET_REQUIRED_BANDS = "error.dayAhead.offer.notSetRequiredBandsInEnergyOffer";
    public static final String AUCTION_DA_OFFER_NOT_ALL_HOURS_HAVE_BEEN_TYPED = "error.dayAhead.offer.notAllRequiredHoursHaveBeenTyped";
    public static final String AUCTION_DA_OFFER_NOT_SET_PRICE_IN_ALL_NON_ZERO_BANDS = "error.dayAhead.offer.notSetPriceInAllNonZeroBands";
    public static final String AUCTION_DA_OFFER_BAND_ZERO_CANNOT_HAVE_PRICE = "error.dayAhead.offer.bandZeroCannotHavePrice";
    public static final String AUCTION_DA_OFFER_NOT_SET_VOLUME_IN_ALL_BANDS = "error.dayAhead.offer.notSetVolumeInAllBands";
    public static final String AUCTION_DA_OFFER_CANNOT_FIND_SELF_SCHEDULE_FOR_DER = "error.dayAhead.offer.cannotFindSelfScheduleForDer";
    public static final String AUCTION_DA_OFFER_BAND_ZERO_MUST_HAVE_VOLUME_FROM_SELF_SCHEDULE = "error.dayAhead.offer.bandZeroMustHaveVolumeFromSelfSchedule";
    public static final String AUCTION_DA_OFFER_ACCEPTED_VOLUME_HIGHER_THAN_VOLUME = "error.dayAhead.offer.acceptedVolumeHigherThanVolume";
    public static final String AUCTION_DA_OFFER_DER_LIMIT_EXCEEDED_IN_AT_LEAST_ONE_TIMESTAMP = "error.dayAhead.offer.derLimitExceededInAtLeastOneTimestamp";
    public static final String AUCTION_DA_OFFER_NOT_UPDATED_DER_PRESENT = "error.auction.offer.import.notUpdatedDerPresent";
    public static final String AUCTION_DA_OFFER_SEVERAL_NOT_UPDATED_DERS_PRESENT = "error.auction.offer.import.severalNotUpdatedDersPresent";

    //AUCTION BIDS EVALUATION
    public static final String AUCTION_BIDS_EVAL_CANNOT_EXPORT_BECAUSE_NO_OFFER_FOUND_FOR_GIVEN_DELIVERY_DATE =
        "error.bidsEvaluation.cannotExportBecauseOffersNotFoundForGivenDeliveryDate";
    public static final String AUCTION_BIDS_EVAL_CANNOT_EXPORT_BECAUSE_NO_TYPE_OF_BIDS_SELECTED =
        "error.bidsEvaluation.cannotExportBecauseNoTypeOfBidsSelected";
    public static final String AUCTION_BIDS_EVAL_CANNOT_ACCEPT_ALREADY_ACCEPTED_OR_REJECTED_OFFER = "error.bidsEvaluation.cannotAcceptAlreadyAcceptedOrRejectedOffer";
    public static final String AUCTION_BIDS_EVAL_CANNOT_ACCEPT_NOT_ALL_VOLUMES_ARE_VERIFIED = "error.bidsEvaluation.cannotAcceptNotAllVolumesAreVerified";
    public static final String AUCTION_BIDS_EVAL_CANNOT_CHANGE_STATUS_AUCTION_NOT_CLOSED = "error.bidsEvaluation.cannotChangeStatusAuctionNotClosed";

    //AGNO ALGORITHM
    public static final String CANNOT_FIND_FORECASTED_PRICES = "error.agnoAlgorithm.cannotFindForecastedPrices";
    public static final String AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NO_BIDS_HAVE_BEEN_SUBMITTED = "error.agnoAlgorithm.agnoAlgorithmCannotBeRunBecauseNoBidsHaveBeenSubmitted";
    public static final String AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_DER_WAS_NOT_FOUND_IN_CHOSEN_KDM_MODEL = "error.agnoAlgorithm.agnoAlgorithmCannotBeRunBecauseDerWasNotFoundInChosenKdmModel";
    public static final String AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_DERS_WERE_NOT_FOUND_IN_CHOSEN_KDM_MODEL = "error.agnoAlgorithm.agnoAlgorithmCannotBeRunBecauseDersWereNotFoundInChosenKdmModel";
    public static final String AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NOT_CHOOSE_ONLY_CAPACITY_RELATED_BIDS = "error.agnoAlgorithm.chooseOnlyCapacityRelatedBids";
    public static final String AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_NOT_CHOOSE_ONLY_ENERGY_RELATED_BIDS = "error.agnoAlgorithm.chooseOnlyEnergyRelatedBids";
    public static final String AGNO_ALGORITHM_CANNOT_CANCEL_NOT_RUNNING_ALGORITHM = "error.agnoAlgorithm.cannotCancelNotRunningAlgorithm";
    public static final String AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_KDM_MODEL_IS_NOT_EXIST = "error.agnoAlgorithm.agnoAlgorithmCannotBeRunBecauseKdmModelIsNotExist";
    public static final String AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_LACKING_KDM_MODEL_FOR_TIMESTAMP = "error.agnoAlgorithm.agnoAlgorithmCannotBeRunBecauseLackingKdmModelForTimestamp";
    public static final String AGNO_ALGORITHM_CANNOT_FOUND_LOG_FILES = "error.agnoAlgorithm.cannotFoundLogFiles";
    public static final String AGNO_ALGORITHM_CANNOT_FOUND_INPUT_FILES = "error.agnoAlgorithm.cannotFoundInputFiles";
    public static final String AGNO_ALGORITHM_CANNOT_FOUND_OUTPUT_FILES = "error.agnoAlgorithm.cannotFoundOutputFiles";
    public static final String AGNO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_SELECTED_OFFERS_IS_NOT_COMPATIBLE_WITH_SELECTED_ALGORITHM_TYPE = "error.agnoAlgorithm.agnoAlgorithmCannotBeRunSelectedOffersIsNotCompatibleWithSelectedAlgorithmType";
    public static final String AGNO_ALGORITHM_COMPARISON_ONE_OFFER_PER_COUPLING_POINT_ALLOWED = "error.agnoAlgorithm.comparison.oneOfferPerCouplingPointAllowed";

    // DANO/PBCM ALGORITHM
    public static final String PBCM_DANO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_DER_CANNOT_BE_FOUND_IN_CHOSEN_KDM_MODEL_DESPITE_HAVING_CONNECTION_WITH_POC_WITH_LV = "error.agnoAlgorithm.pbcmDanoAlgorithmCannotBeRunBecauseDerCannotBeFoundInChosenKdmModelDespiteHavingConnectionWithPocWithLv";
    public static final String PBCM_DANO_ALGORITHM_CANNOT_BE_RUN_BECAUSE_DERS_CANNOT_BE_FOUND_IN_CHOSEN_KDM_MODEL_DESPITE_HAVING_CONNECTION_WITH_POC_WITH_LV = "error.agnoAlgorithm.pbcmDanoAlgorithmCannotBeRunBecauseDersCannotBeFoundInChosenKdmModelDespiteHavingConnectionWithPocWithLv";


    // KPI
    public static final String KPI_EMPTY_DATA_FOR_GIVEN_PARAMETERS = "error.kpi.emptyDataForGivenParameters";
    public static final String KPI_REQUIRED_TO_SET_DATE_FILTER = "error.kpi.requiredToSetDateFilters";

    // ACTIVATIONS/SETTLEMENTS
    public static final String SETTLEMENTS_NOTHING_TO_EXPORT = "error.activationSettlements.nothingToExport";
    public static final String SETTLEMENTS_TEMPLATE_INCORRECT = "error.activationSettlements.templateIncorrect";
    public static final String SETTLEMENTS_NOTHING_CHANGED = "error.activationSettlements.nothingChanged";
    public static final String SAVING_THE_SAME_ACTIVATION_SETTLEMENTS = "SAVING_THE_SAME_ACTIVATION_SETTLEMENTS";
    public static final String DUPLICATE_ACTIVATION_SETTLEMENTS = "DUPLICATE_ACTIVATION_SETTLEMENTS";

    // CHAT
    public static final String CHAT_ACCESS_FORBIDDEN = "error.chat.accessForbidden";
    public static final String CHAT_FILE_DOES_NOT_EXIST = "error.chat.fileDoesNotExist";
    public static final String CHAT_CANNOT_CREATE_EMPTY_MESSAGE = "error.chat.cannotCreateEmptyMessage";
    public static final String CHAT_ALREADY_EXISTS = "error.chat.alreadyExists";

    private ErrorConstants() {
    }
}
