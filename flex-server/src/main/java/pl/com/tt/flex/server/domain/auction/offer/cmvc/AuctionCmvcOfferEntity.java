package pl.com.tt.flex.server.domain.auction.offer.cmvc;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.domain.auction.offer.AbstractAuctionOffer;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.refreshView.listener.AuctionCmvcOfferListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A AuctionCmvcOfferEntity.
 */
@Entity
@Table(name = "auction_cmvc_offer")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuctionCmvcOfferListener.class)
@GenericGenerator(
    name = "auction_offer_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "auction_offer_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class AuctionCmvcOfferEntity extends AbstractAuctionOffer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auction_offer_id_generator")
    private Long id;

    // Oferty na aukcje CM&VC skladaja uzytkownicy FSP/A
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_cmvc_id", referencedColumnName = "id", updatable = false)
    private AuctionCmvcEntity auctionCmvc;

    // FSP/A ma tylko do wyboru zarejestrowane Potencjaly (okno Registered Flexibility Potentials z zakladki Flex Register), które są przypisane na dany produkt z aukcji.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flex_potential_id", referencedColumnName = "id")
    private FlexPotentialEntity flexPotential;

    /**
     * Price - cena, którą zaproponowała organizacja w ofercie.
     * (cena za 1 kW [PLN/kW] jeżeli jest to aukcja na moc lub za 1 kWh [PLN/kWh] jeżeli jest to aukcja na energię)
     */
    @NotNull
    @Column(name = "price", precision = 21, scale = 2)
    private BigDecimal price;

    /**
     * Volume - wolumen oferty zaproponowany przez organizację.
     * (zaproponowana moc przez BSP do danej aukcji [kW] jeżeli jest to aukcja na moc lub z zaproponowaną energię do danej aukcji [kWh] jeżeli jest to aukcja na energię)
     */
    @NotNull
    @Column(name = "volume", precision = 21, scale = 2)
    private BigDecimal volume;

    /**
     * Wolumen, który został ustawiony przez administratora (jeżeli administrator nic nie zmieniał, to ta wartość ma być równa wartości atrybutu "Volume").
     * Pole jest dostępne tylko jeżeli "Volume divisibility" jest ustawione na "Yes".
     * Ponadto należy ustawić weryfikację, aby ta wartość nie była wyższa niż wartość w "Volume".
     */
    @NotNull
    @Column(name = "accepted_volume", precision = 21, scale = 2)
    private BigDecimal acceptedVolume;

    @NotNull
    @Column(name = "scheduled_activation_email")
    private boolean scheduledActivationEmail;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuctionCmvcOfferEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((AuctionCmvcOfferEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
