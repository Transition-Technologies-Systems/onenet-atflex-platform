package pl.com.tt.flex.server.domain.product;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A ProductEntity.
 */

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "product_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "product_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class ProductEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_id_generator")
    private Long id;

    /**
     * For each save, the 'version' column is self incremented (starts at 0).
     */
    @Version
    @Column(name = "version", columnDefinition = "integer")
    private Long version = 0L;

    @NotNull
    @Size(max = 255)
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @NotNull
    @Size(max = 50)
    @Column(name = "short_name", length = 50, nullable = false)
    private String shortName;

    @NotNull
    @Column(name = "locational", nullable = false)
    private boolean locational;

    /**
     * wartosc dziesietna z 1 miejscem po przecinku
     */
    @NotNull
    @Column(name = "min_bid_size", precision = 21, scale = 1, nullable = false)
    private BigDecimal minBidSize;

    /**
     * wartosc dziesietna z 1 miejscem po przecinku
     */
    @NotNull
    @Column(name = "max_bid_size", precision = 21, scale = 1, nullable = false)
    private BigDecimal maxBidSize;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "bid_size_unit", nullable = false)
    private ProductBidSizeUnit bidSizeUnit;

    /**
     * Maximum time for full activation of product in seconds
     */
    @NotNull
    @Column(name = "max_full_activation_time", nullable = false, columnDefinition = "smallint")
    private Integer maxFullActivationTime;

    /**
     * Minimum required duration of product delivery in minutes
     */
    @NotNull
    @Column(name = "min_delivery_duration", nullable = false, columnDefinition = "smallint")
    private Integer minRequiredDeliveryDuration;

    @NotNull
    @Column(name = "active", nullable = false)
    private boolean active;

    @NotNull
    @Column(name = "valid_from", nullable = false)
    private Instant validFrom;

    @NotNull
    @Column(name = "valid_to", nullable = false)
    private Instant validTo;

    @OneToMany(mappedBy = "product", fetch = FetchType.LAZY)
    private Set<FlexPotentialEntity> flexPotentials;

    /**
     * Primary System Operator (pSO) - user with role TSO/DSO
     */
    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private UserEntity psoUser;

    @NotNull
    @Column(name = "balancing", nullable = false)
    private boolean balancing;

    @NotNull
    @Column(name = "cmvc", nullable = false)
    private boolean cmvc;

    /**
     * Secondary System Operators (sSO) - users with role TSO/DSO
     */
    @NotEmpty
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "product_sso_user",
        joinColumns = @JoinColumn(name = "product_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"))
    private Set<UserEntity> ssoUsers = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProductFileEntity> files = new HashSet<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false)
    private Direction direction;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((ProductEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public String toVersionString() {
        return "ProductEntity{" +
            "id=" + id +
            ", version=" + version +
            '}';
    }
}
