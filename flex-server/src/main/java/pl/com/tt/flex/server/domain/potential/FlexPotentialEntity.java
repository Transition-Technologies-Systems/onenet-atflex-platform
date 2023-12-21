package pl.com.tt.flex.server.domain.potential;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A FlexPotentialEntity.
 */
@Getter
@Setter
@Entity
@Builder
@Table(name = "flex_potential")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@NoArgsConstructor
@AllArgsConstructor
@GenericGenerator(
    name = "flex_potential_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "flex_potential_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class FlexPotentialEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "flex_potential_id_generator")
    private Long id;

    /**
     * For each save, the 'version' column is self incremented (starts at 0).
     */
    @Version
    @Column(name = "version")
    private Long version = 0L;

    @Column(name = "volume", precision = 21, scale = 2)
    private BigDecimal volume;

    @Enumerated(EnumType.STRING)
    @Column(name = "volume_unit")
    private ProductBidSizeUnit volumeUnit;

    @Column(name = "valid_from")
    private Instant validFrom;

    @Column(name = "valid_to")
    private Instant validTo;

    @Column(name = "active")
    private boolean active;

    @Column(name = "product_preq")
    private boolean productPrequalification;

    @Column(name = "static_grid_preq")
    private boolean staticGridPrequalification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    @NotNull
    private ProductEntity product;

    /**
     * Only one unit can be attached to FSP FlexPotential.
     * Multiple Units can be attached to FSPA FlexPotential.
     */
    @NotEmpty
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "flex_potential_units",
        joinColumns = @JoinColumn(name = "flex_potential_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "unit_id", referencedColumnName = "id"))
    private Set<UnitEntity> units = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fsp_id")
    @NotNull
    private FspEntity fsp;

    @OneToMany(mappedBy = "flexPotential", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FlexPotentialFileEntity> files = new HashSet<>();

    @Column(name = "created_by_role")
    private String createdByRole;

    @Column(name = "last_modified_by_role")
    private String lastModifiedByRole;

    /**
     * Maximum time for full activation in seconds (related to Product maxFullActivationTime)
     */
    @NotNull
    @Column(name = "full_activation_time", nullable = false, columnDefinition = "smallint")
    private Integer fullActivationTime;

    /**
     * Minimum required duration of delivery in minutes (related to Product minRequiredDeliveryDuration)
     */
    @NotNull
    @Column(name = "min_delivery_duration", nullable = false, columnDefinition = "smallint")
    private Integer minDeliveryDuration;

    /**
     *  Is flex potential aggregated (related with Product aggregationAllowed)
     */
    @Column(name = "aggregated", nullable = false)
    private boolean aggregated;

    /**
     * Is flex potential registered (pre-qualified). ProductPrequalification = true and staticGridPrequalification = true.
     */
    @Column(name = "is_register", nullable = false)
    private boolean registered;

    @Column(name = "divisibility", nullable = false)
    private boolean divisibility;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlexPotentialEntity)) {
            return false;
        }
        return id != null && id.equals(((FlexPotentialEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public String toVersionString() {
        return "FlexPotentialEntity{" +
            "id=" + id +
            ", version=" + version +
            '}';
    }
}
