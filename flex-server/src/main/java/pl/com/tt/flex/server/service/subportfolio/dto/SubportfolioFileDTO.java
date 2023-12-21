package pl.com.tt.flex.server.service.subportfolio.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioFileEntity;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.Serializable;

/**
 * A DTO for the {@link SubportfolioFileEntity} entity.
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubportfolioFileDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    private FileDTO fileDTO;

    private Long subportfolioId;

    public SubportfolioFileDTO(FileDTO fileDTO) {
        this.fileDTO = fileDTO;
    }
}
