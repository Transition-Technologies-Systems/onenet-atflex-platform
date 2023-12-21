package pl.com.tt.flex.server.service.schedulingUnit.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitFileEntity;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.Serializable;

/**
 * A DTO for the {@link SchedulingUnitFileEntity} entity.
 */

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchedulingUnitFileDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    private FileDTO fileDTO;

    private Long schedulingUnitId;

    public SchedulingUnitFileDTO(FileDTO fileDTO) {
        this.fileDTO = fileDTO;
    }
}
