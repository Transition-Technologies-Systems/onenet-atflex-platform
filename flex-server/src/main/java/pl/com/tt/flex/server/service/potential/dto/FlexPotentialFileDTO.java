package pl.com.tt.flex.server.service.potential.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.potential.FlexPotentialFileEntity;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.Serializable;

/**
 * A DTO for the {@link FlexPotentialFileEntity} entity.
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FlexPotentialFileDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    private FileDTO fileDTO;

    private Long flexPotentialId;

    public FlexPotentialFileDTO(FileDTO fileDTO) {
        this.fileDTO = fileDTO;
    }

    public FlexPotentialFileDTO(FileDTO fileDTO, Long flexPotentialId) {
        this.fileDTO = fileDTO;
        this.flexPotentialId = flexPotentialId;
    }
}
