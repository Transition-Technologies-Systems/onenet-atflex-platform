package pl.com.tt.flex.server.domain.user.config.email;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;

@Component
public class TransmissionSystemOperatorEmailTypesContainer extends AbstractEmailTypesContainer implements EmailTypesContainer {

    @PostConstruct
    public void init() {
        emailTypes = new ArrayList<>();
        emailTypes.add(EmailType.USER_ACCOUNT_CREATION);
        emailTypes.add(EmailType.ACCOUNT_PASSWORD_RESET);
        emailTypes.add(EmailType.OFFERS_USED_IN_ALGORITHM_EXPORT);
        emailTypes.add(EmailType.ALGORITHM_RESULT_EXPORT);
        emailTypes.add(EmailType.OFFER_EXPORT_TSO);
        emailTypes.add(EmailType.FLEXIBILITY_POTENTIAL_EDITION);
        emailTypes.add(EmailType.FSP_EDITION);
        emailTypes.add(EmailType.PRODUCT_CREATION);
        emailTypes.add(EmailType.PRODUCT_EDITION);
        emailTypes.add(EmailType.SCHEDULING_UNIT_READY_FOR_TESTS_NOTIFICATION);
        emailTypes.add(EmailType.SCHEDULING_UNIT_EDITION);
        emailTypes.add(EmailType.SUBPORTFOLIO_CREATION);
        emailTypes.add(EmailType.SUBPORTFOLIO_EDITION);
    }

    public boolean supports(Role role) {
        return Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR.equals(role);
    }

}
