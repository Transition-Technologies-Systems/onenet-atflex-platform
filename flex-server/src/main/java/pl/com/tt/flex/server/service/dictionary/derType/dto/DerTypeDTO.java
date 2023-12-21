package pl.com.tt.flex.server.service.dictionary.derType.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.server.domain.unit.enumeration.DerType;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A DTO for the {@link DerTypeEntity} entity.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DerTypeDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    @NotNull
    private DerType type;

    @NotNull
    @Size(max = 100)
    private String descriptionEn;

    @Size(max = 100)
    private String descriptionPl;

    @Size(max = 150)
    private String key;

    @Size(max = 150)
    private String nlsCode;

    @NotNull
    private BigDecimal sderPoint;
}
