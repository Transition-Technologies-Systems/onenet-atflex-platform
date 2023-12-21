package pl.com.tt.flex.server.service.subportfolio.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.utils.Lists;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.model.service.dto.file.FileMinDTO;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.validator.constraints.UniqueSubportfolioName;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * A DTO for the {@link SubportfolioEntity} entity.
 */
@Getter
@Setter
@UniqueSubportfolioName
@EqualsAndHashCode(callSuper = false)
public class SubportfolioDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 100)
    private String name;

    private Integer numberOfDers;

    private BigDecimal combinedPowerOfDers;

    private List<LocalizationTypeDTO> couplingPointIdTypes;

    @Size(max = 200)
    private String mrid;

    private FspDTO fspa;
    private Long fspId;

    @NotNull
    private Boolean active;

    private boolean certified;

    private Instant validFrom;

    private Instant validTo;

    private List<Long> unitIds;

    private List<UnitMinDTO> units = Lists.newArrayList();

    private List<FileMinDTO> filesMinimal = Lists.newArrayList();

    private List<SubportfolioFileDTO> files = Lists.newArrayList();

    private List<Long> removeFiles = Lists.newArrayList();
}
