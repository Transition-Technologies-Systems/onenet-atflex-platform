package pl.com.tt.flex.server.domain.auction.da;

import lombok.*;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.server.refreshView.listener.AuctionDayAheadListener;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A AuctionDayAheadEntity.
 */
@Entity
@Table(name = "auction_day_ahead")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuctionDayAheadListener.class)
@GenericGenerator(
    name = "auction_day_ahead_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "auction_day_ahead_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class AuctionDayAheadEntity extends AbstractAuctionDayAheadEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "auction_day_ahead_id_generator")
    private Long id;

    @NotNull
    @Column(name = "name")
    private String name;

    @Enumerated(EnumType.STRING)
    @Formula("(select v.status from AUCTION_DAY_AHEAD_VIEW v where v.id = id)")
    private AuctionStatus status;

    @NotNull
    @Column(name = "auctions_series_id")
    private Long auctionSeriesId;

    /**
     * Aukcje "Day-ahead" są aukcjami cyklicznymi - z dnia na dzień.
     * Patrz pola firstAuctionDay, lastAuctionDay z AuctionsSeriesEntity.
     */
    @NotNull
    @Column(name = "auction_day")
    private Instant day;

    @NotNull
    @Column(name = "delivery_date")
    private Instant deliveryDate;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuctionDayAheadEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((AuctionDayAheadEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
