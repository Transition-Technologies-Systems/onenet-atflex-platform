package pl.com.tt.flex.server.service.schedulingUnit.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.audit.AbstractAuditingDTO;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitProposalEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalStatus;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalType;

import java.io.Serializable;
import java.time.Instant;

/**
 * A DTO for the {@link SchedulingUnitProposalEntity} entity.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class SchedulingUnitProposalMinDTO extends AbstractAuditingDTO implements Serializable {

    private Long id;
    private SchedulingUnitProposalStatus status;
    private SchedulingUnitProposalType proposalType;
    private Long fspId;
    private String fspName;
    private Long bspId;
    private String bspName;
    private Long schedulingUnitId;
    private String schedulingUnitName;
    private Long derId;
    private String derName;
    private Instant sentDate;
}
