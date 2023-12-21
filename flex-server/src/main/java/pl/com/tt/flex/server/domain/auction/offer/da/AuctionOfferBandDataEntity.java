package pl.com.tt.flex.server.domain.auction.offer.da;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A AuctionOfferBandDataEntity.
 */
@Entity
@Table(name = "auction_da_offer_band_data")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@GenericGenerator(
    name = "auction_da_offer_band_data_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "auction_da_offer_band_data_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class AuctionOfferBandDataEntity extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auction_da_offer_band_data_id_generator")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_offer_ders_id", referencedColumnName = "id")
    private AuctionOfferDersEntity offerDer;

    @NotNull
    @Column(name = "hour_number")
    private String hourNumber;

    @NotNull
    @Column(name = "band_number")
    private String bandNumber;

    @NotNull
    @Column(name = "is_edited", nullable = false)
    private boolean isEdited;

    /**
     * Price - cena, którą zaproponowała organizacja w ofercie na dana godzine.
     * (cena za 1 kWh [PLN/kWh])
     * Pasma 0 - nie ma ceny dla danej godziny
     * W przypadku ustawienia flagi CommonPriceOfBid, cena dla kazdego pasma jest wspolna
     * i ustawiona w polu commonPrice encji AuctionDayAheadOfferEntity
     */
    @Column(name = "price", precision = 13, scale = 3)
    private BigDecimal price;

    @Column(name = "accepted_price", precision = 13, scale = 3)
    private BigDecimal acceptedPrice;

    /**
     * Volume - wolumen oferty zaproponowany przez organizację.
     * (zaproponowana moc przez BSP do danej aukcji [kW] jeżeli jest to aukcja na moc lub z zaproponowaną energię do danej aukcji [kWh] jeżeli jest to aukcja na energię)
     */
    @NotNull
    @Column(name = "volume", precision = 22, scale = 3, nullable = false)
    private BigDecimal volume;

    /**
     * Wolumen, który został ustawiony przez administratora (jeżeli administrator nic nie zmieniał, to ta wartość ma być równa wartości atrybutu "Volume").
     * Pole jest dostępne tylko jeżeli "Volume divisibility" jest ustawione na "Yes".
     * Ponadto należy ustawić weryfikację, aby ta wartość nie była wyższa niż wartość w "Volume".
     */
    @Column(name = "accepted_volume", precision = 22, scale = 3)
    private BigDecimal acceptedVolume;

    @Column(name = "volume_transferred_to_bm", precision = 22, scale = 3)
    private BigDecimal volumeTransferredToBM;

    @Column(name = "gdf")
    private String gdf;

    public AuctionOfferBandDataEntity markAsEdited() {
        this.setEdited(true);
        return this;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuctionOfferBandDataEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((AuctionOfferBandDataEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
