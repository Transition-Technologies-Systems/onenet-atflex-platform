package pl.com.tt.flex.server.domain.auction.da;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A AuctionDayAheadViewEntity.
 */
@Data
@Entity
@Immutable
@Table(name = "auction_day_ahead_view")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class AuctionDayAheadViewEntity extends AbstractAuditingEntity implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AuctionStatus status;

    @Column(name = "auctions_series_id")
    private Long auctionSeriesId;

    @Column(name = "auction_day")
    private Instant day;

    @Column(name = "delivery_date")
    private Instant deliveryDate;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_type")
    private AuctionDayAheadType type;

    @Column(name = "min_desired_capacity", precision = 21, scale = 2)
    private BigDecimal minDesiredCapacity;

    @Column(name = "max_desired_capacity", precision = 21, scale = 2)
    private BigDecimal maxDesiredCapacity;

    @Column(name = "min_desired_energy", precision = 21, scale = 2)
    private BigDecimal minDesiredEnergy;

    @Column(name = "max_desired_energy", precision = 21, scale = 2)
    private BigDecimal maxDesiredEnergy;

    @Column(name = "status_code")
    private String statusCode;

    @Column(name = "product_min_bid_size", precision = 21, scale = 1)
    private BigDecimal productMinBidSize;

    @Column(name = "product_max_bid_size", precision = 21, scale = 1)
    private BigDecimal productMaxBidSize;
}
