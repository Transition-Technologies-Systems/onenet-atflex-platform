package pl.com.tt.flex.server.domain.auction.offer;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionCategoryAndType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A AuctionOfferViewEntity.
 */
@Data
@Entity
@Immutable
@Table(name = "auction_offer_view")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class AuctionOfferViewEntity extends AbstractAuditingEntity implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "auction_id")
    private Long auctionId;

    @Column(name = "auction_name")
    private String auctionName;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_category_and_type")
    private AuctionCategoryAndType auctionCategoryAndType;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "ders")
    private String ders;

    @Column(name = "coupling_point")
    private String couplingPoint;

    @Column(name = "power_station")
    private String powerStation;

    @Column(name = "poc_with_lv")
    private String pointOfConnectionWithLV;

    // Getter dodany ze względu na konieczność rozróżnienia wartości NULL wyświetlanej
    // jako '-' dla aukcji CM/VC i pustego Stringa wyświetlanego dla aukcji DA
    public String getPointOfConnectionWithLV(){
        return Objects.isNull(pointOfConnectionWithLV) ? "-" : pointOfConnectionWithLV;
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Role role;

    @Column(name = "fsp_id")
    private Long fspId;

    @Enumerated(EnumType.STRING)
    @Column(name = "offer_category")
    private AuctionOfferViewType offerCategory;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AuctionOfferStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "auction_status")
    private AuctionStatus auctionStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AuctionOfferType type;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "volume")
    private String volume;

    @Column(name = "volume_tooltip_visible")
    private Boolean volumeTooltipVisible;

    @Column(name = "volume_divisibility")
    private Boolean volumeDivisibility;

    @Column(name = "accepted_volume")
    private String acceptedVolume = this.volume;

    @Column(name = "accepted_volume_tooltip_visible")
    private Boolean acceptedVolumeTooltipVisible;

    @Column(name = "delivery_period_from")
    private Instant deliveryPeriodFrom;

    @Column(name = "delivery_period_to")
    private Instant deliveryPeriodTo;

    @Column(name = "delivery_period_divisibility")
    private Boolean deliveryPeriodDivisibility;

    @Column(name = "accepted_delivery_period_from")
    private Instant acceptedDeliveryPeriodFrom;

    @Column(name = "accepted_delivery_period_to")
    private Instant acceptedDeliveryPeriodTo;

    @Column(name = "verified_volumes_percent")
    private Integer verifiedVolumesPercent;

    @Column(name = "scheduling_unit_or_potential")
    private String schedulingUnitOrPotential;

    @Column(name = "flex_potential_volume")
    private BigDecimal flexibilityPotentialVolume;

    @Column(name = "flex_potential_volume_unit")
    private String flexibilityPotentialVolumeUnit;

    @ManyToMany(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
    @JoinTable(name = "OFFER_DERS_VIEW",
        joinColumns = @JoinColumn(name = "offer_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "der_id", referencedColumnName = "id"))
    private Set<UnitEntity> schedulingUnitOrPotentialDers = new HashSet<>();
}
