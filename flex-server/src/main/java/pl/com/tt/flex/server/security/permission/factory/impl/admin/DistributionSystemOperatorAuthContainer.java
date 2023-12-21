package pl.com.tt.flex.server.security.permission.factory.impl.admin;

import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.security.permission.AuthoritiesContainer;
import pl.com.tt.flex.model.security.permission.Authority;
import pl.com.tt.flex.model.security.permission.Role;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class DistributionSystemOperatorAuthContainer implements AuthoritiesContainer {

    private List<String> authorities;

    @PostConstruct
    public void init() {
        authorities = Lists.newArrayList();

        // FLEX ADMIN/USER
        authorities.add(Authority.FLEX_SYS_DICTIONARY_RESOURCE_VIEW);

        // OneNet Flexibility Platform Administrator - FLEX ADMIN
        authorities.add(Authority.FLEX_ADMIN_LOGIN);
        authorities.add(Authority.FLEX_ADMIN_USER_CHANGE_PASSWORD);

        authorities.add(Authority.FLEX_ADMIN_FSP_VIEW);

        authorities.add(Authority.FLEX_ADMIN_PRODUCT_VIEW);
        authorities.add(Authority.FLEX_ADMIN_PRODUCT_MANAGE);
        authorities.add(Authority.FLEX_ADMIN_PRODUCT_DELETE);

        authorities.add(Authority.FLEX_ADMIN_UNIT_VIEW);
        authorities.add(Authority.FLEX_ADMIN_UNIT_MANAGE);

        authorities.add(Authority.FLEX_ADMIN_FP_VIEW);
        authorities.add(Authority.FLEX_ADMIN_FP_MANAGE);

        authorities.add(Authority.FLEX_ADMIN_VIEW_NOTIFICATION);

        authorities.add(Authority.FLEX_SYS_USER_SCREEN_CONFIG);
        authorities.add(Authority.FLEX_SYS_USER_EMAIL_CONFIG);

        authorities.add(Authority.FLEX_SYS_ACTIVITY_MONITOR_VIEW);

        authorities.add(Authority.FLEX_ADMIN_DER_TYPE_VIEW);
        authorities.add(Authority.FLEX_ADMIN_DER_TYPE_MANAGE);
        authorities.add(Authority.FLEX_ADMIN_DER_TYPE_DELETE);

        authorities.add(Authority.FLEX_ADMIN_LOCALIZATION_TYPE_VIEW);
        authorities.add(Authority.FLEX_ADMIN_LOCALIZATION_TYPE_MANAGE);
        authorities.add(Authority.FLEX_ADMIN_LOCALIZATION_TYPE_DELETE);

        authorities.add(Authority.FLEX_ADMIN_SCHEDULING_UNIT_VIEW);

        authorities.add(Authority.FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_VIEW);

        authorities.add(Authority.FLEX_ADMIN_SELF_SCHEDULE_VIEW);

        authorities.add(Authority.FLEX_ADMIN_SUBPORTFOLIO_VIEW);
        authorities.add(Authority.FLEX_ADMIN_SUBPORTFOLIO_MANAGE);

        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_CMVC_VIEW);
        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_CMVC_MANAGE);
        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_CMVC_DELETE);

        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_OFFER_VIEW);
        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_OFFER_MANAGE);
        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_OFFER_EXPORT_SETO);
        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_OFFER_IMPORT_SETO);
        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_OFFER_IMPORT_CMVC);

        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_CMVC_OFFER_VIEW);
        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_CMVC_OFFER_EDIT);

        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_SERIES_VIEW);
        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_DAY_AHEAD_VIEW);

        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_VIEW);
        authorities.add(Authority.FLEX_ADMIN_AUCTIONS_DAY_AHEAD_OFFER_EDIT);

        authorities.add(Authority.FLEX_ADMIN_ALGORITHM_EVALUATIONS);
        authorities.add(Authority.FLEX_ADMIN_ALGORITHM_EVALUATION_OFFERS);

        authorities.add(Authority.FLEX_ADMIN_KDM_MODEL_MANAGE);
        authorities.add(Authority.FLEX_ADMIN_KDM_MODEL_VIEW);
        authorities.add(Authority.FLEX_ADMIN_KDM_MODEL_DELETE);

        authorities.add(Authority.FLEX_ADMIN_ONENET_USER_VIEW);
        authorities.add(Authority.FLEX_ADMIN_ONENET_USER_MANAGE);
        authorities.add(Authority.FLEX_ADMIN_ONENET_USER_DELETE);

        authorities.add(Authority.FLEX_ADMIN_OFFERED_SERVICES_VIEW);

        authorities.add(Authority.FLEX_ADMIN_PROVIDE_DATA_VIEW);
        authorities.add(Authority.FLEX_ADMIN_PROVIDE_DATA_SEND);

        authorities.add(Authority.FLEX_ADMIN_CONSUME_DATA_VIEW);

        authorities.add(Authority.FLEX_ADMIN_KPI_VIEW);
        authorities.add(Authority.FLEX_ADMIN_KPI_MANAGE);
        authorities.add(Authority.FLEX_ADMIN_KPI_DELETE);

        authorities.add(Authority.FLEX_ADMIN_SETTLEMENT_VIEW);
        authorities.add(Authority.FLEX_ADMIN_SETTLEMENT_MANAGE);

        authorities.add(Authority.FLEX_ADMIN_CHAT_VIEW);
        authorities.add(Authority.FLEX_ADMIN_CHAT_MANAGE);
        authorities.add(Authority.FLEX_ADMIN_CHAT_MESSAGE_CREATE);
    }

    @Override
    public List<String> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean supports(Role role) {
        return Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR.equals(role);
    }
}
