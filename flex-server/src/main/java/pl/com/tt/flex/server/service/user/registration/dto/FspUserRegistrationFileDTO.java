package pl.com.tt.flex.server.service.user.registration.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.user.registration.FspUserRegistrationFileEntity;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.Serializable;

/**
 * A DTO for the {@link FspUserRegistrationFileEntity} entity.
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FspUserRegistrationFileDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    private FileDTO fileDTO;

    private Long fspUserRegistrationCommentId;

    private Long fspUserRegistrationId;

    public FspUserRegistrationFileDTO(FileDTO fileDTO) {
        this.fileDTO = fileDTO;
    }
}
