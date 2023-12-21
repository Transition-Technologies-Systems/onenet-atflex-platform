package pl.com.tt.flex.server.domain.auction.offer.da;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * A AuctionOfferDersEntity.
 */
@Entity
@Table(name = "auction_da_offer_ders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@GenericGenerator(
    name = "auction_da_offer_ders_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "auction_da_offer_ders_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class AuctionOfferDersEntity extends AbstractAuditingEntity {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auction_da_offer_ders_id_generator")
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offer_id", referencedColumnName = "id")
    private AuctionDayAheadOfferEntity offer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", referencedColumnName = "id")
    private UnitEntity unit;

    /**
     * Oferty skladane na Aukcje DayAhead zawieraja informacje o cenie oraz wolumenie na dana godzine oraz pasmo
     * Pasma:
     * pasmo 0 - cena pobierana z planu pracy DERa oraz uzupelniony wolumen
     * pasma +/- 10 - dodatkowe opcjonalne pasma ktore zawieraja cene oraz wolumen
     * obowiązkowe pasma:
     * * dla aukcji Capacity w zaleznosći od kierunku produktu: UP: 0,1 DOWN: -1,0
     * * dla aukcji Energy: -1,0,1
     * Godzina:
     * mozliwosc uzupelnienie ceny oraz wolumenu na dana godzine z zakresu zdefiniowanego w aukcji:
     * energyAvailabilityFrom - energyAvailabilityTo
     */

    @NotEmpty
    @OneToMany(mappedBy = "offerDer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuctionOfferBandDataEntity> bandData = new ArrayList<>();
}
