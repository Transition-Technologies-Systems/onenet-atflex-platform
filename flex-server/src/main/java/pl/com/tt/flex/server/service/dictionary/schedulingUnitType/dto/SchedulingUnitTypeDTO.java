package pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * A DTO for the {@link SchedulingUnitTypeEntity} entity.
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SchedulingUnitTypeDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 100)
    private String descriptionEn;

    @Size(max = 100)
    private String descriptionPl;

    private List<ProductMinDTO> products;

    @Size(max = 150)
    private String key;

    @Size(max = 150)
    private String nlsCode;
}
