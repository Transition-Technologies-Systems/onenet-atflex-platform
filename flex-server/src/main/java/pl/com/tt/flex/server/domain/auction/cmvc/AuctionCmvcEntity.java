package pl.com.tt.flex.server.domain.auction.cmvc;

import lombok.*;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionType;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.refreshView.listener.AuctionCmvcListener;

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
 * A AuctionCmvcEntity.
 */
@Entity
@Table(name = "auction_cmvc")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuctionCmvcListener.class)
@GenericGenerator(
    name = "auction_cmvc_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "auction_cmvc_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class AuctionCmvcEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auction_cmvc_id_generator")
    private Long id;

    @NotNull
    @Size(max = 100)
    @Column(name = "name", length = 100, nullable = false, unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    @Formula("(select v.status from AUCTION_CMVC_VIEW v where v.id = id)")
    private AuctionStatus status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", referencedColumnName = "id", updatable = false)
    private ProductEntity product;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_type", updatable = false)
    private AuctionType auctionType = AuctionType.CAPACITY;

    @ManyToMany
    @org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "localization_auction_cmvc",
        joinColumns = @JoinColumn(name = "auction_cmvc_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "localization_type_id", referencedColumnName = "id"))
    private Set<LocalizationTypeEntity> localization = new HashSet<>();

    @NotNull
    @Column(name = "delivery_date_from", nullable = false)
    private Instant deliveryDateFrom;

    @NotNull
    @Column(name = "delivery_date_to", nullable = false)
    private Instant deliveryDateTo;

    @NotNull
    @Column(name = "gate_opening_time", nullable = false)
    private Instant gateOpeningTime;

    @NotNull
    @Column(name = "gate_closure_time", nullable = false)
    private Instant gateClosureTime;

    @Column(name = "min_desired_power", precision = 21, scale = 2)
    private BigDecimal minDesiredPower;

    @Column(name = "max_desired_power", precision = 21, scale = 2)
    private BigDecimal maxDesiredPower;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuctionCmvcEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((AuctionCmvcEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
