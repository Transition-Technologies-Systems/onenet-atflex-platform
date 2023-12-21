package pl.com.tt.flex.server.domain.settlement;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import lombok.Data;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.server.domain.auction.offer.AuctionOfferViewType;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;

@Data
@Entity
@Immutable
@Table(name = "settlement_view")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class SettlementViewEntity extends AbstractAuditingEntity implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "der_name")
    private String derName;

    @Column(name = "fsp_id")
    private Long fspId;

    @Column(name = "offer_id")
    private Long offerId;

    @Column(name = "auction_name")
    private String auctionName;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "bsp_company_name")
    private String bspCompanyName;

    @Column(name = "accepted_delivery_period_from")
    private Instant acceptedDeliveryPeriodFrom;

    @Column(name = "accepted_delivery_period_to")
    private Instant acceptedDeliveryPeriodTo;

    @Column(name = "accepted_volume")
    private String acceptedVolume;

    @Column(name = "activated_volume")
    private BigDecimal activatedVolume;

    @Column(name = "settlement_amount")
    private BigDecimal settlementAmount;

    @Column(name = "unit")
    private String unit;

    @Column(name = "offer_created_by")
    private String offerCreatedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "offer_status")
    private AuctionOfferStatus offerStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "offer_category")
    private AuctionOfferViewType offerCategory;



}
