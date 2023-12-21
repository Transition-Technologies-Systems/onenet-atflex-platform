package pl.com.tt.flex.server.domain.auction.offer;

import lombok.Getter;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Getter
@Setter
@MappedSuperclass
public abstract class AbstractAuctionOffer extends AbstractAuditingEntity {

    //------------------ Czesc wspolna CM&VC i Day Ahead -------------
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AuctionOfferStatus status;

    /**
     * Z pola wynika jednostka wolumenu:
     *  - kW jeżeli jest to aukcja na moc
     *  - kWh jeżeli jest to aukcja na energię
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false)
    private AuctionOfferType type;

    /**
     * Pole określające czy w danej ofercie admin może dokonać edycji wolumenu.
     */
    @NotNull
    @Column(name = "volume_divisibility", nullable = false)
    private Boolean volumeDivisibility;

    /**
     * Delivery period - okres dostawy zaproponowany w ofercie (z dokładnością do 15 minut).
     */
    @NotNull
    @Column(name = "delivery_period_from", nullable = false)
    private Instant deliveryPeriodFrom;

    @NotNull
    @Column(name = "delivery_period_to", nullable = false)
    private Instant deliveryPeriodTo;

    /**
     * Pole określające czy w danej ofercie admin może dokonać edycji godziny dostawy.
     */
    @NotNull
    @Column(name = "delivery_period_divisibility", nullable = false)
    private Boolean deliveryPeriodDivisibility;

    /**
     * Okres dostawy, który został ustawiony przez administratora (jeżeli administrator nic nie zmieniał to ta wartość ma być równa wartości atrybutu "Delivery period").
     * Pole jest dostępne tylko jeżeli "Delivery period divisibility" jest ustawione na "Yes".
     * Ponadto należy ustawić weryfikację, aby ten zakres nie wychodził poza zakres określony w "Delivery period".
     */
    @NotNull
    @Column(name = "accepted_delivery_period_from", nullable = false)
    private Instant acceptedDeliveryPeriodFrom;

    @NotNull
    @Column(name = "accepted_delivery_period_to", nullable = false)
    private Instant acceptedDeliveryPeriodTo;
}
