package pl.com.tt.flex.server.domain.enumeration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The NotificationEvent enumeration.
 */
@Getter
@RequiredArgsConstructor
public enum NotificationEvent {

    //USER
    LOGOUT_USER(true, true),
    USER_CREATED(true, true),
    USER_UPDATED(true, true),

    //FSP Registration
    FSP_USER_REGISTRATION_NEW(true, false),
    FSP_USER_REGISTRATION_CONFIRMED_BY_FSP(true, false),
    FSP_USER_REGISTRATION_WITHDRAWN_BY_FSP(true, false),
    FSP_USER_REGISTRATION_ACCEPTED_BY_MO(true, false),
    FSP_USER_REGISTRATION_REJECTED_BY_MO(true, false),
    FSP_USER_REGISTRATION_UPDATED(true, false),

    //FSP/BSP
    FSP_UPDATED(true, true),
    BSP_UPDATED(true, true),

    //PRODUCT
    PRODUCT_CREATED(true, true),
    PRODUCT_UPDATED(true, true),

    //FP
    FP_UPDATED(true, true),
    FP_DELETED(true, true),
    FP_CREATED(true, true),
    FP_MOVED_TO_FLEX_REGISTER(true, true),

    //UNIT
    UNIT_CREATED(true, true),
    UNIT_UPDATED(true, true),
    UNIT_CREATED_TO_FSP(false, true),
    UNIT_HAS_BEEN_CERTIFIED(false, true),
    UNIT_LOST_CERTIFICATION(false, true),

    //SCHEDULING UNIT
    SCHEDULING_UNIT_CREATED(true, true),
    SCHEDULING_UNIT_UPDATED(true, true),

    SCHEDULING_UNIT_PROPOSAL_TO_BSP(false, true),
    SCHEDULING_UNIT_PROPOSAL_TO_BSP_ACCEPTED(false, true),
    SCHEDULING_UNIT_PROPOSAL_TO_BSP_CANCELLED_BY_FSP(false, true),
    SCHEDULING_UNIT_PROPOSAL_TO_BSP_REJECTED_BY_BSP(false, true),

    SCHEDULING_UNIT_PROPOSAL_TO_FSP(false, true),
    SCHEDULING_UNIT_PROPOSAL_TO_FSP_ACCEPTED(false, true),
    SCHEDULING_UNIT_PROPOSAL_TO_FSP_CANCELLED_BY_BSP(false, true),
    SCHEDULING_UNIT_PROPOSAL_TO_FSP_REJECTED_BY_FSP(false, true),

    SCHEDULING_UNIT_READY_FOR_TESTS(true, true),
    SCHEDULING_UNIT_YOUR_DER_REMOVED_FROM_SU(false, true),
    SCHEDULING_UNIT_DER_REMOVED_FROM_YOUR_SU(false, true),
    SCHEDULING_UNIT_MOVED_TO_FLEX_REGISTER(true, true),

    //DICTIONARY TYPES
    DER_TYPE_CREATED(true, true),
    DER_TYPE_UPDATED(true, true),
    SU_TYPE_CREATED(true, true),
    SU_TYPE_UPDATED(true, true),
    LOCALIZATION_TYPE_CREATED(true, true),
    LOCALIZATION_TYPE_UPDATED(true, true),

    //IMPORT BID
    BID_IMPORT(true, false),

    //SUBPORTFOLIO
    SUBPORTFOLIO_CREATED(true, true),
    SUBPORTFOLIO_UPDATED(true, true),

    //AUCTION DAY AHEAD
    AUCTION_DA_ACCEPTED_CAPACITY_OFFER(false, true),

    //ALGORITHMS
    DISAGGREGATION_COMPLETED(true, false),
    DISAGGREGATION_FAILED(true, false),
    CONNECTION_TO_ALGORITHM_SERVICE_LOST(true, false);


    private final boolean notifyAdmin;
    private final boolean notifyUser;
}
