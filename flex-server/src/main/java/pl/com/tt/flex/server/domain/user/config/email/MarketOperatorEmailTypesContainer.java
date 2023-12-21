package pl.com.tt.flex.server.domain.user.config.email;

import java.util.ArrayList;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;

@Component
public class MarketOperatorEmailTypesContainer extends AbstractEmailTypesContainer implements EmailTypesContainer {

    @PostConstruct
    public void init() {
        emailTypes = new ArrayList<>();
        emailTypes.add(EmailType.USER_ACCOUNT_CREATION);
        emailTypes.add(EmailType.ACCOUNT_PASSWORD_RESET);
        emailTypes.add(EmailType.FSP_EDITION);
        emailTypes.add(EmailType.NEW_FSP_REGISTRATION_APPLICATION_NOTIFICATION);
        emailTypes.add(EmailType.FSP_REGISTRATION_APPLICATION_CONFIRAMTION_NOTIFICATION);
        emailTypes.add(EmailType.FSP_REGISTRATION_CHANGE_NOTIFICATION);
        emailTypes.add(EmailType.FSP_REGISTRATION_ACCEPTED);
        emailTypes.add(EmailType.FSP_REGISTRATION_REJECTED);
        emailTypes.add(EmailType.FSP_REGISTRATION_APPLICATION_WITHDRAWAL);
    }

    public boolean supports(Role role) {
        return Role.ROLE_MARKET_OPERATOR.equals(role);
    }

}
