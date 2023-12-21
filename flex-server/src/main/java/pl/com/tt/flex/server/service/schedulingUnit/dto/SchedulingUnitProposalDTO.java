package pl.com.tt.flex.server.service.schedulingUnit.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitProposalEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalStatus;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalType;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

/**
 * A DTO for the {@link SchedulingUnitProposalEntity} entity.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class SchedulingUnitProposalDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;

    @NotNull
    private SchedulingUnitProposalStatus status = SchedulingUnitProposalStatus.NEW;
    //fsp wybieraja w select bspId do ktorego ma dolaczyc ich der, a potem bsp wybiera do ktorego scheduling
    private Long bspId;
    //bsp zaprasza dera bezposrednio do scheduling
    private Long schedulingUnitId;
    @NotNull
    private Long unitId;
    private Long senderId;
    private SchedulingUnitProposalType proposalType;
    //dane do wyswietlenia na front
    private ProposalDetailsDTO details;
    private Instant sentDate;
}
