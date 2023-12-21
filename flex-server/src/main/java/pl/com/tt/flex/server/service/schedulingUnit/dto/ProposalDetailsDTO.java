package pl.com.tt.flex.server.service.schedulingUnit.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitProposalEntity;
import pl.com.tt.flex.model.service.dto.dictionary.DictionaryDTO;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * A DTO for the {@link SchedulingUnitProposalEntity} entity.
 */
@Getter
@Setter
@EqualsAndHashCode
public class ProposalDetailsDTO implements Serializable {

    private String fspName;
    private String bspName;
    private String schedulingUnitName;
    private DictionaryDTO schedulingUnitType;
    private String derName;
    private BigDecimal derSourcePower;
    private BigDecimal derConnectionPower;
    private DerTypeMinDTO derTypeReception;
    private DerTypeMinDTO derTypeEnergyStorage;
    private DerTypeMinDTO derTypeGeneration;
}
