package pl.com.tt.flex.server.domain.schedulingUnit;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalStatus;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalType;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A SchedulingUnitProposalEntity - proposals of FSP's Units to BSP's SchedulingUnits.
 */
@Getter
@Setter
@Entity
@Table(name = "scheduling_unit_proposal")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@GenericGenerator(
    name = "scheduling_unit_proposal_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "sched_unit_proposal_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class SchedulingUnitProposalEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheduling_unit_proposal_id_generator")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SchedulingUnitProposalStatus status;

    /**
     * Rekordy z statusem NEW sa domyslnie wyswietlane w oknie jako pierwsze
     * @see SchedulingUnitProposalStatus#NEW
     */
    @Column(name = "status_sort_order", nullable = false)
    private Integer statusSortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    private FspEntity bsp;

    @ManyToOne(fetch = FetchType.LAZY)
    private SchedulingUnitEntity schedulingUnit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private UnitEntity unit;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private UserEntity sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_role", nullable = false)
    private Role senderRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private SchedulingUnitProposalType proposalType;

    /**
     * Sent / resent date
     */
    @Column(name = "sent_date")
    private Instant sentDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SchedulingUnitProposalEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((SchedulingUnitProposalEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
