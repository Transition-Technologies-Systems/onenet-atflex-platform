package pl.com.tt.flex.server.domain.auction.offer.da;

import java.math.BigDecimal;
import java.util.*;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotEmpty;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.domain.auction.offer.AbstractAuctionOffer;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.refreshView.listener.AuctionDayAheadOfferListener;

/**
 * A AuctionDayAheadOfferEntity.
 */
@Entity
@Table(name = "auction_da_offer")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuctionDayAheadOfferListener.class)
@GenericGenerator(
    name = "auction_offer_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "auction_offer_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class AuctionDayAheadOfferEntity extends AbstractAuctionOffer {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auction_offer_id_generator")
    private Long id;

    // Oferty na aukcje DayAhead skladaja uzytkownicy BSP
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_day_ahead_id", referencedColumnName = "id", updatable = false)
    private AuctionDayAheadEntity auctionDayAhead;

    // BSP ma tylko do wyboru zarejestrowane Jednostki grafikowe (okno Registered Scheduling Units z zakladki Flex Register),
    // które są przypisane na dany produkt z aukcji.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduling_unit_id", referencedColumnName = "id")
    private SchedulingUnitEntity schedulingUnit;

    @NotEmpty
    @OneToMany(mappedBy = "offer", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuctionOfferDersEntity> units = new ArrayList<>();

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "volume_from")
    private BigDecimal volumeFrom;

    @Column(name = "volume_to")
    private BigDecimal volumeTo;

    @Column(name = "accepted_volume_from")
    private BigDecimal acceptedVolumeFrom;

    @Column(name = "accepted_volume_to")
    private BigDecimal acceptedVolumeTo;

    @Column(name = "verified_volumes_percent")
    private int verifiedVolumesPercent;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuctionDayAheadOfferEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((AuctionDayAheadOfferEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
