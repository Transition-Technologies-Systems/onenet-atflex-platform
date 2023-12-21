package pl.com.tt.flex.server.domain.user.config.email;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;

@Component
public class BalancingServiceProviderEmailTypesContainer extends AbstractEmailTypesContainer implements EmailTypesContainer {

    @PostConstruct
    public void init() {
        emailTypes = new ArrayList<>();
        emailTypes.add(EmailType.FSP_REGISTRATION_CONFIRMATION);
        emailTypes.add(EmailType.USER_ACCOUNT_CREATION);
        emailTypes.add(EmailType.ACCOUNT_PASSWORD_RESET);
        emailTypes.add(EmailType.CAPACITY_OFFER_ACCEPTED);
        emailTypes.add(EmailType.SCHEDULING_UNIT_JOINING_PROPOSAL);
        emailTypes.add(EmailType.SCHEDULING_UNIT_STATUS_CHANGE);
        emailTypes.add(EmailType.SCHEDULING_UNIT_ASSIGNMENT_TO_REGISTER);
        emailTypes.add(EmailType.SCHEDULING_UNIT_CREATION);
        emailTypes.add(EmailType.SCHEDULING_UNIT_EDITION);
        emailTypes.add(EmailType.UNIT_ACTIVATION_REMINDER_DAY_AHEAD);
    }

    public boolean supports(Role role) {
        return Role.ROLE_BALANCING_SERVICE_PROVIDER.equals(role);
    }

}
