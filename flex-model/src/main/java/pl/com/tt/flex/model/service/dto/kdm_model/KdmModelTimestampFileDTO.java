package pl.com.tt.flex.model.service.dto.kdm_model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class KdmModelTimestampFileDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    @NotNull
    private String timestamp;

    private String fileName;

    @JsonIgnore
    private FileDTO fileDTO;

    @NotNull
    private Long kdmModelId;

}
