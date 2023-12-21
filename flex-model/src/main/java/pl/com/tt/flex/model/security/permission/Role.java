package pl.com.tt.flex.model.security.permission;

import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum Role {

    // Technical Administrator (TA)
    ROLE_ADMIN("ADMIN"),

    // OneNet Flexibility Platform Administrator
    // Transmission System Operator (TSO)
    ROLE_TRANSMISSION_SYSTEM_OPERATOR("TSO"),
    // Distribution System Operator (DSO)
    ROLE_DISTRIBUTION_SYSTEM_OPERATOR("DSO"),
    // Market Operator (MO)
    ROLE_MARKET_OPERATOR("MO"),

    // OneNet Flexibility Platform FSP
    // Flexibility Service Provider (FSP)
    ROLE_FLEX_SERVICE_PROVIDER("FSP"),
    // Balancing Service Provider (BSP)
    ROLE_BALANCING_SERVICE_PROVIDER("BSP"),
    // Role needed for FSP platform user candidate in registration process
    ROLE_FSP_USER_REGISTRATION("FSP_REG"),
    //Flexibility Service Provider Aggregated (FSPA)
    ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED("FSPA"),

    ROLE_ANONYMOUS("ANONYMOUS");

    // OneNet Flexibility Platform FSP organisation roles
    public static Set<Role> FSP_ORGANISATIONS_ROLES = Sets.newHashSet(ROLE_FLEX_SERVICE_PROVIDER, ROLE_BALANCING_SERVICE_PROVIDER, ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED);
    private final String shortName;

    // Users roles for refreshing auctions
    public static Set<Role> REFRESH_CMVC_AUCTIONS_USER_ROLES = Sets.newHashSet(Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED);
    public static Set<Role> REFRESH_DAY_AHEAD_AUCTIONS_USER_ROLES = Sets.newHashSet(Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED, Role.ROLE_BALANCING_SERVICE_PROVIDER);
    public static Set<Role> REFRESH_OFFERS_USER_ROLES = Sets.newHashSet(Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED);
    public static Set<Role> REFRESH_AUCTIONS_AND_OFFERS_ADMIN_ROLES = Sets.newHashSet(Role.ROLE_ADMIN);
}
