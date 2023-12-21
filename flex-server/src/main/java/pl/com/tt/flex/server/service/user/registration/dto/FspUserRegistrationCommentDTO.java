package pl.com.tt.flex.server.service.user.registration.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationCommentEntity;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegCommentCreationSource;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.MinimalDTO;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.List;

/**
 * A DTO for the {@link FspUserRegistrationCommentEntity} entity.
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FspUserRegistrationCommentDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    @Size(min = 1, max = 1000)
    private String text;

    @NotNull
    private Long fspUserRegistrationId;

    private Long userId;

    private FspUserRegCommentCreationSource creationSource;

    private List<MinimalDTO<Long, String>> files;

    @Override
    public String toString() {
        return "FspUserRegCommentDTO{" +
            "id=" + id +
            ", fspUserRegistrationId=" + fspUserRegistrationId +
            '}';
    }
}
