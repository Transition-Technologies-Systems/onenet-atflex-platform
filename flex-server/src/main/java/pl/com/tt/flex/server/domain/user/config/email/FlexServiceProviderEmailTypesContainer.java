package pl.com.tt.flex.server.domain.user.config.email;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;

@Component
public class FlexServiceProviderEmailTypesContainer extends AbstractEmailTypesContainer implements EmailTypesContainer {

    @PostConstruct
    public void init() {
        emailTypes = new ArrayList<>();
        emailTypes.add(EmailType.USER_ACCOUNT_CREATION);
        emailTypes.add(EmailType.ACCOUNT_PASSWORD_RESET);
        emailTypes.add(EmailType.FLEXIBILITY_POTENTIAL_CREATION);
        emailTypes.add(EmailType.FLEXIBILITY_POTENTIAL_EDITION);
        emailTypes.add(EmailType.FLEXIBILITY_POTENTIAL_ASSIGNMENT_TO_REGISTER);
        emailTypes.add(EmailType.FSP_EDITION);
        emailTypes.add(EmailType.FSP_REGISTRATION_CONFIRMATION);
        emailTypes.add(EmailType.FSP_USER_ACTIVATION_CONFIRMATION);
        emailTypes.add(EmailType.FSP_REGISTRATION_CHANGE_NOTIFICATION);
        emailTypes.add(EmailType.FSP_REGISTRATION_ACCEPTED);
        emailTypes.add(EmailType.FSP_REGISTRATION_REJECTED);
        emailTypes.add(EmailType.FSP_REGISTRATION_APPLICATION_WITHDRAWAL);
        emailTypes.add(EmailType.UNIT_ACTIVATION_REMINDER_DAY_AHEAD);
        emailTypes.add(EmailType.UNIT_ACTIVATION_REMINDER_CMVC);
        emailTypes.add(EmailType.SUBPORTFOLIO_CREATION);
        emailTypes.add(EmailType.SUBPORTFOLIO_EDITION);
        emailTypes.add(EmailType.UNIT_CREATION);
        emailTypes.add(EmailType.UNIT_EDITION);
        emailTypes.add(EmailType.UNIT_CERTIFICATION);
        emailTypes.add(EmailType.UNIT_CERTIFICATION_LOSS);
    }

    public boolean supports(Role role) {
        return Role.ROLE_FLEX_SERVICE_PROVIDER.equals(role);
    }

}
