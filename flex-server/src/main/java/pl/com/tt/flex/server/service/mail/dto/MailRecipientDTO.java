package pl.com.tt.flex.server.service.mail.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.service.user.dto.UserMinDTO;

import java.util.Locale;

import static java.util.Objects.nonNull;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class MailRecipientDTO {
    private Long entityId;
    private String entityName;
    private String email;
    private Locale locale;

    public MailRecipientDTO(UserEntity user) {
        this.entityId = user.getId();
        this.entityName = user.getClass().getSimpleName();
        this.email = user.getEmail();
        this.locale = Locale.forLanguageTag(user.getLangKey());
    }

    public MailRecipientDTO(UserMinDTO userMinimal) {
        this.entityId = userMinimal.getId();
        this.entityName = userMinimal.getClass().getSimpleName();
        this.email = userMinimal.getEmail();
        this.locale = Locale.forLanguageTag(userMinimal.getLangKey());
    }

    public Locale getLocale() {
        return nonNull(this.locale) ? this.locale : Locale.ENGLISH;
    }

    @Override
    public String toString() {
        return "MailRecipientDTO{" +
            "entityId=" + entityId +
            ", entityName='" + entityName + '\'' +
            '}';
    }


}
