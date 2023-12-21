package pl.com.tt.flex.server.domain.schedulingUnit;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

/**
 * A SchedulingUnitEntity.
 */
@Getter
@Setter
@Entity
@Table(name = "scheduling_unit")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "scheduling_unit_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "scheduling_unit_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class SchedulingUnitEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "scheduling_unit_id_generator")
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @NotNull
    @Column(name = "active", nullable = false)
    private boolean active;

    /**
     * Units (DERs)
     */
    @OneToMany(mappedBy = "schedulingUnit")
    private Set<UnitEntity> units = new HashSet<>();

    /**
     * Quantity of joined Units (DERs)
     */
    @Formula("(select COUNT(*) from UNIT u where u.scheduling_unit_id = id)")
    private Integer numberOfDers;

    /**
     * Quantity of joined SchedulingUnitProposals
     */
    @Formula("(select COUNT(*) from SCHEDULING_UNIT_PROPOSAL sup where sup.scheduling_unit_id = id AND sup.status = 'NEW')")
    private Integer numberOfDersProposals;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduling_unit_type_id")
    private SchedulingUnitTypeEntity schedulingUnitType;

    /**
     * Owner of SchedulingUnit
     */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "bsp_id", referencedColumnName = "id")
    private FspEntity bsp;

    @OneToMany(mappedBy = "schedulingUnit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SchedulingUnitFileEntity> files = new HashSet<>();

    @Column(name = "ready_for_tests")
    private boolean readyForTests = false;

    @Column(name = "certified")
    private boolean certified = false;

    @Formula("(select MAX(ada.delivery_date) + 1 from AUCTION_DA_OFFER dao join AUCTION_DAY_AHEAD ada on dao.auction_day_ahead_id = ada.id where dao.scheduling_unit_id = id and dao.status != 'REJECTED')")
    private Instant certificationChangeLockedUntil;

    @Transient
    private List<LocalizationTypeEntity> couplingPoints = new ArrayList<>();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "localization_type_id", referencedColumnName = "id")
    private LocalizationTypeEntity primaryCouplingPoint;

    public SchedulingUnitEntity addUnit(UnitEntity unitEntity) {
        this.units.add(unitEntity);
        unitEntity.setSchedulingUnit(this);
        return this;
    }

    // Couling Points zawsze zaciagane są ze wszystkich DERow ktore są podłączone do SU
    public List<LocalizationTypeEntity> getCouplingPoints() {
        return units.stream()
            .flatMap(u -> u.getCouplingPointIdTypes().stream())
            .distinct()
            .sorted(comparing(LocalizationTypeEntity::getName))
            .collect(Collectors.toList());
    }

    // Couling Points zalezne sa od tego jakie lokalizacje ma podpięty do SU DER,
    // nie można ich edytowac z poziomu SchedulingUnit
    private void setCouplingPoints(List<LocalizationTypeEntity> couplingPoints) {
        this.couplingPoints = couplingPoints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SchedulingUnitEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((SchedulingUnitEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
