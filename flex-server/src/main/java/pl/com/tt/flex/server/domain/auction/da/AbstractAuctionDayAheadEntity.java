package pl.com.tt.flex.server.domain.auction.da;

import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractAuctionDayAheadEntity extends AbstractAuditingEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "auction_type", updatable = false)
    private AuctionDayAheadType type;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id", updatable = false)
    private ProductEntity product;

    @Column(name = "energy_gate_opening_time")
    private Instant energyGateOpeningTime;

    @Column(name = "energy_gate_closure_time")
    private Instant energyGateClosureTime;

    @Column(name = "capacity_gate_opening_time")
    private Instant capacityGateOpeningTime;

    @Column(name = "capacity_gate_closure_time")
    private Instant capacityGateClosureTime;

    @Column(name = "capacity_availability_from")
    private Instant capacityAvailabilityFrom;

    @Column(name = "capacity_availability_to")
    private Instant capacityAvailabilityTo;

    @Column(name = "energy_availability_from")
    private Instant energyAvailabilityFrom;

    @Column(name = "energy_availability_to")
    private Instant energyAvailabilityTo;

    @Column(name = "min_desired_capacity", precision = 21, scale = 2)
    private BigDecimal minDesiredCapacity;

    @Column(name = "max_desired_capacity", precision = 21, scale = 2)
    private BigDecimal maxDesiredCapacity;

    @Column(name = "min_desired_energy", precision = 21, scale = 2)
    private BigDecimal minDesiredEnergy;

    @Column(name = "max_desired_energy", precision = 21, scale = 2)
    private BigDecimal maxDesiredEnergy;
}
