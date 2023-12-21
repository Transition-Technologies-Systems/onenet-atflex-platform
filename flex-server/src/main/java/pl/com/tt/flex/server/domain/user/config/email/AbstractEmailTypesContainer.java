package pl.com.tt.flex.server.domain.user.config.email;

import java.util.List;
import java.util.stream.Collectors;

import pl.com.tt.flex.server.domain.email.enumeration.EmailType;

public abstract class AbstractEmailTypesContainer{

    List<EmailType> emailTypes;

    public List<EmailType> getEmailTypes() {
        return emailTypes;
    }

    public List<EmailType> getOptionalEmailTypes() {
        return emailTypes.stream().filter(EmailType::isOptional).collect(Collectors.toList());
    }

}
