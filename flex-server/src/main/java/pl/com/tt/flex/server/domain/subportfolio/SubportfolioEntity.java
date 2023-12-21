package pl.com.tt.flex.server.domain.subportfolio;

import lombok.*;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A Subportfolio
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "subportfolio")
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@GenericGenerator(
    name = "subportfolio_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "subportfolio_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class SubportfolioEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subportfolio_id_generator")
    private Long id;

    @Size(max = 50)
    @Column(name = "name", length = 50)
    private String name;

    /**
     * Units (DERs)
     */
    @OneToMany(mappedBy = "subportfolio")
    private Set<UnitEntity> units = new HashSet<>();

    /**
     * Quantity of joined Units (DERs)
     */
    @Formula("(select COUNT(*) from UNIT u where u.subportfolio_id = id)")
    private Integer numberOfDers;

    /**
     * Energy transformer coupling point id types
     */
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "subportfolio_cpi_type",
        joinColumns = @JoinColumn(name = "subportfolio_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "localization_type_id", referencedColumnName = "id"))
    private Set<LocalizationTypeEntity> couplingPointIdTypes = new HashSet<>();

    /**
     * Energy transformer mRID
     */
    @Column(name = "mrid")
    private String mrid;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "fspa_id", referencedColumnName = "id")
    private FspEntity fspa;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "active")
    private boolean active;

    @Column(name = "certified")
    private boolean certified;

    @OneToMany(mappedBy = "subportfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SubportfolioFileEntity> files = new HashSet<>();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubportfolioEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((SubportfolioEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
