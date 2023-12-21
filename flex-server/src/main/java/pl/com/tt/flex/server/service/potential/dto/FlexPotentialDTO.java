package pl.com.tt.flex.server.service.potential.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.utils.Lists;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.model.service.dto.file.FileMinDTO;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * A DTO for the {@link FlexPotentialEntity} entity.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class FlexPotentialDTO extends AbstractAuditingDTO{

    private Long id;

    private Long version = 0L;

    @NotNull
    private ProductMinDTO product;

    private FspDTO fsp;

    private List<Long> unitIds;
    private UnitMinDTO unit;

    private List<UnitMinDTO> units;

    @ApiModelProperty(value = "Value needs to be between selected Product min and max bid size")
    private BigDecimal volume;

    private ProductBidSizeUnit volumeUnit;

    private Instant validFrom;

    private Instant validTo;

    private boolean active;

    private boolean productPreqNeeded;

    private boolean productPrequalification;

    private boolean staticGridPreqNeeded;

    private boolean staticGridPrequalification;

    private String createdByRole;

    private String lastModifiedByRole;

    private SchedulingUnitMinDTO schedulingUnit;

    private List<FileMinDTO> filesMinimal = Lists.newArrayList();

    private List<FlexPotentialFileDTO> files = Lists.newArrayList();

    private List<Long> removeFiles = Lists.newArrayList();

    private Integer fullActivationTime;

    private Integer minDeliveryDuration;

    private boolean aggregated;

    private boolean divisibility;

    private boolean registered;
}
