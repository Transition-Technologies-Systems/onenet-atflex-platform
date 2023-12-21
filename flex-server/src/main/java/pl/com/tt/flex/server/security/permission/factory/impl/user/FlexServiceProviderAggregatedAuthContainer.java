package pl.com.tt.flex.server.security.permission.factory.impl.user;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.security.permission.AuthoritiesContainer;
import pl.com.tt.flex.model.security.permission.Authority;
import pl.com.tt.flex.model.security.permission.Role;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class FlexServiceProviderAggregatedAuthContainer implements AuthoritiesContainer {

    private List<String> authorities;

    @PostConstruct
    public void init() {
        authorities = Lists.newArrayList();

        authorities.add(Authority.FLEX_SYS_DICTIONARY_RESOURCE_VIEW);

        // OneNet Flexibility Platform FSP - FLEX USER
        authorities.add(Authority.FLEX_USER_LOGIN);
        authorities.add(Authority.FLEX_USER_VIEW);
        authorities.add(Authority.FLEX_USER_USER_MANAGE);
        authorities.add(Authority.FLEX_USER_CHANGE_PASSWORD);

        authorities.add(Authority.FLEX_USER_PRODUCT_VIEW);

        authorities.add(Authority.FLEX_USER_FSP_REGISTRATION_VIEW);

        authorities.add(Authority.FLEX_USER_UNIT_VIEW);
        authorities.add(Authority.FLEX_USER_UNIT_MANAGE);
        authorities.add(Authority.FLEX_USER_UNIT_DELETE);

        authorities.add(Authority.FLEX_USER_FP_VIEW);
        authorities.add(Authority.FLEX_USER_FP_MANAGE);
        authorities.add(Authority.FLEX_USER_FP_DELETE);

        authorities.add(Authority.FLEX_USER_VIEW_NOTIFICATION);

        authorities.add(Authority.FLEX_SYS_USER_SCREEN_CONFIG);
        authorities.add(Authority.FLEX_SYS_USER_EMAIL_CONFIG);

        authorities.add(Authority.FLEX_SYS_ACTIVITY_MONITOR_VIEW);

        authorities.add(Authority.FLEX_USER_DER_TYPE_VIEW);
        authorities.add(Authority.FLEX_USER_DER_TYPE_MANAGE);
        authorities.add(Authority.FLEX_USER_DER_TYPE_DELETE);

        authorities.add(Authority.FLEX_USER_SCHEDULING_UNIT_VIEW);
        authorities.add(Authority.FLEX_USER_SCHEDULING_UNIT_PROPOSAL_VIEW);
        authorities.add(Authority.FLEX_USER_SCHEDULING_UNIT_PROPOSAL_CREATE);
        authorities.add(Authority.FLEX_USER_SCHEDULING_UNIT_PROPOSAL_MANAGE);

        authorities.add(Authority.FLEX_USER_SCHEDULING_UNIT_TYPE_VIEW);

        authorities.add(Authority.FLEX_USER_SELF_SCHEDULE_VIEW);
        authorities.add(Authority.FLEX_USER_SELF_SCHEDULE_MANAGE);
        authorities.add(Authority.FLEX_USER_SELF_SCHEDULE_DELETE);

        authorities.add(Authority.FLEX_USER_SUBPORTFOLIO_VIEW);
        authorities.add(Authority.FLEX_USER_SUBPORTFOLIO_MANAGE);
        authorities.add(Authority.FLEX_USER_SUBPORTFOLIO_DELETE);

        authorities.add(Authority.FLEX_USER_AUCTIONS_VIEW_PRICES);

        authorities.add(Authority.FLEX_USER_AUCTIONS_CMVC_VIEW);

        authorities.add(Authority.FLEX_USER_AUCTIONS_CMVC_OFFER_VIEW);
        authorities.add(Authority.FLEX_USER_AUCTIONS_CMVC_OFFER_CREATE);
        authorities.add(Authority.FLEX_USER_AUCTIONS_CMVC_OFFER_EDIT);
        authorities.add(Authority.FLEX_USER_AUCTIONS_CMVC_OFFER_DELETE);

        authorities.add(Authority.FLEX_USER_BSP_VIEW);

        authorities.add(Authority.FLEX_USER_LOCALIZATION_TYPE_VIEW);

        authorities.add(Authority.FLEX_USER_SETTLEMENT_VIEW);

        authorities.add(Authority.FLEX_USER_CHAT_VIEW);
        authorities.add(Authority.FLEX_USER_CHAT_MANAGE);
        authorities.add(Authority.FLEX_USER_CHAT_MESSAGE_CREATE);
    }

    @Override
    public List<String> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean supports(Role role) {
        return Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED.equals(role);
    }
}
