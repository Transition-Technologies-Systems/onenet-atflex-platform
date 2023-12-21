package pl.com.tt.flex.server.domain.unit;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.refreshView.listener.UnitListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A UnitEntity (DER - Distributed Energy Resource)
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "unit")
@EntityListeners(UnitListener.class)
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "unit_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "unit_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class UnitEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "unit_id_generator")
    private Long id;

    /**
     * For each save, the 'version' column is self incremented (starts at 0).
     */
    @Version
    @Column(name = "version")
    private Long version = 0L;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    @Size(max = 50)
    @Column(name = "brp_code", length = 50)
    private String code;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "der_type_reception_id")
    private DerTypeEntity derTypeReception;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "der_type_energy_storage_id")
    private DerTypeEntity derTypeEnergyStorage;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "der_type_generation_id")
    private DerTypeEntity derTypeGeneration;

    @Column(name = "aggregated")
    private boolean aggregated;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "active")
    private boolean active;

    @Column(name = "certified")
    private boolean certified;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fsp_id")
    private FspEntity fsp;

    @OneToMany(mappedBy = "unit", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private Set<UnitGeoLocationEntity> geoLocations = new HashSet<>();

    @NotNull
    @Column(precision = 13, scale = 2)
    private BigDecimal sourcePower;

    @NotNull
    @Column(precision = 13, scale = 2)
    private BigDecimal connectionPower;

    @NotNull
    @Enumerated(EnumType.STRING)
    private UnitDirectionOfDeviation directionOfDeviation;

    /**
     * Energy consumption point (in Polish - Punkt Poboru Energii)
     */
    @Column(name = "ppe", nullable = false)
    private String ppe;

    /**
     * Energy transformer coupling point id types
     */
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "unit_coup_point_id_type",
        joinColumns = @JoinColumn(name = "unit_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "localization_type_id", referencedColumnName = "id"))
    private Set<LocalizationTypeEntity> couplingPointIdTypes = new HashSet<>();

    /**
     * Energy transformer mRID(DSO)
     */
    @Column(name = "mrid_dso")
    private String mridDso;

    /**
     * Energy transformer mRID(TSO)
     */
    @Column(name = "mrid_tso")
    private String mridTso;

    @Column(name = "p_min")
    private BigDecimal pMin;

    @Column(name = "q_min")
    private BigDecimal qMin;

    @Column(name = "q_max")
    private BigDecimal qMax;


    /**
     * Power station MV/LV number
     */
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "unit_power_station_type",
        joinColumns = @JoinColumn(name = "unit_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "localization_type_id", referencedColumnName = "id"))
    private Set<LocalizationTypeEntity> powerStationTypes = new HashSet<>();

    /**
     * Point of connection with LV/Punkt przyłączenia z siecią nn (niskiego napięcia)
     */
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "unit_point_of_connection_type",
        joinColumns = @JoinColumn(name = "unit_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "localization_type_id", referencedColumnName = "id"))
    private Set<LocalizationTypeEntity> pointOfConnectionWithLvTypes = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduling_unit_id", referencedColumnName = "id")
    private SchedulingUnitEntity schedulingUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subportfolio_id", referencedColumnName = "id")
    private SubportfolioEntity subportfolio;

    /**
     * If connectionPower of DER is bigger than derTypeGeneration sderPoint value, then DER is SDER (Significant Distributed Energy Resource).
     * Determined SDER value is not stored in the database.
     */
    // tymczasowo okreslamy czy DER jest SDER'em na podstawie derTypeGeneration
    @Formula("(CASE WHEN connection_power >= (SELECT dt.sder_point FROM der_type dt WHERE dt.id = der_type_generation_id) THEN true ELSE false END)")
    private boolean sder;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UnitEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((UnitEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public String toVersionString() {
        return "UnitEntity{" +
            "id=" + id +
            ", version=" + version +
            '}';
    }
}
