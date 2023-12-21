package pl.com.tt.flex.server.domain.user.registration.enumeration;

/**
 * The FspUserRegistrationStatus enumeration.
 * FSP - candidate for the user of Flexibility Platform FSP (flex-user)
 * MO - administrator of Flexibility Platform Administrator (flex-admin)
 * @see pl.com.tt.flex.model.security.permission.Role
 */
//
public enum FspUserRegistrationStatus {
    NEW, //sent by FSP
    CONFIRMED_BY_FSP, //confirmed by FSP (confirmation link in the email has been clicked)
    WITHDRAWN_BY_FSP, //withdrawn by FSP
    PRE_CONFIRMED_BY_MO, //pre confirmed by MO (FSP user with limited privileges is created)
    USER_ACCOUNT_ACTIVATED_BY_FSP, //FSP has activated his user account with limited privileges
    ACCEPTED_BY_MO, //accepted by MO
    REJECTED_BY_MO //rejected by MO
}
