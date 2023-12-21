package pl.com.tt.flex.server.service.schedulingUnit.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.utils.Lists;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.model.service.dto.file.FileMinDTO;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.validator.constraints.UniqueScheduligUnitName;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * A DTO for the {@link SchedulingUnitEntity} entity.
 */
@Getter
@Setter
@UniqueScheduligUnitName
@EqualsAndHashCode(callSuper = false)
public class SchedulingUnitDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 100)
    private String name;

    @NotNull
    private Boolean active;

    private Integer numberOfDers;

    private Integer numberOfDersProposals;

    private DictionaryDTO schedulingUnitType;

    private FspDTO bsp;

    private List<FileMinDTO> filesMinimal = Lists.newArrayList();

    private List<SchedulingUnitFileDTO> files = Lists.newArrayList();

    private List<Long> removeFiles = Lists.newArrayList();

    /**
     * If SchedulingUnit is ready for tests then its DERs cannot be modified.
     */
    private boolean readyForTests = false;

    /**
     * If SchedulingUnit is certified then it is also shown in Flex Register tab.
     */
    private boolean certified = false;

    private Instant certificationChangeLockedUntil;

    private List<UnitMinDTO> units;

    private List<LocalizationTypeDTO> couplingPoints = Lists.newArrayList();

    private LocalizationTypeDTO primaryCouplingPoint;

}
