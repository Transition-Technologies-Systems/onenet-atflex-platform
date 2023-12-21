package pl.com.tt.flex.server.domain.product;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * A ForecastedPricesFileEntity
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "forecasted_prices")
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "forecasted_prices_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "forecasted_prices_product_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class ForecastedPricesEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "forecasted_prices_id_generator")
    private Long id;

    @Column(name = "forecasted_prices_date")
    private Instant forecastedPricesDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", referencedColumnName = "id")
    private ProductEntity product;

    @ElementCollection
    @CollectionTable(name = "forecasted_prices_price",
        joinColumns = {@JoinColumn(name = "forecasted_price_product_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "hour_number")
    @Column(name = "price")
    private Map<String, BigDecimal> prices;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ForecastedPricesEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((ForecastedPricesEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
