package pl.com.tt.flex.server.domain.auction.cmvc;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionType;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A AuctionCmvcViewEntity.
 */
@Data
@Entity
@Immutable
@Table(name = "auction_cmvc_view")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class AuctionCmvcViewEntity extends AbstractAuditingEntity implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AuctionStatus status;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_type")
    private AuctionType auctionType;

    @Column(name = "localization")
    private String localization;

    @Column(name = "delivery_date_from")
    private Instant deliveryDateFrom;

    @Column(name = "delivery_date_to")
    private Instant deliveryDateTo;

    @Column(name = "gate_opening_time")
    private Instant gateOpeningTime;

    @Column(name = "gate_closure_time")
    private Instant gateClosureTime;

    @Column(name = "min_desired_power")
    private BigDecimal minDesiredPower;

    @Column(name = "max_desired_power")
    private BigDecimal maxDesiredPower;

    @Column(name = "statusCode")
    private String statusCode;
}
