package pl.com.tt.flex.server.domain.unit.self_schedule;

import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * A UnitEntity (DER - Distributed Energy Resource)
 */
@Getter
@Setter
@Builder
@Entity
@Table(name = "unit_self_schedule")
@NoArgsConstructor
@AllArgsConstructor
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@GenericGenerator(
    name = "self_schedule_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "unit_self_schedule_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class UnitSelfScheduleEntity extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "self_schedule_id_generator")
    private Long id;

    @Column(name = "self_schedule_date")
    private Instant selfScheduleDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", referencedColumnName = "id")
    private UnitEntity unit;

    @ElementCollection
    @CollectionTable(name = "unit_self_schedule_volume",
        joinColumns = {@JoinColumn(name = "unit_self_schedule_id", referencedColumnName = "id")})
    @MapKeyColumn(name = "hour_number")
    @Column(name = "volume")
    private Map<String, BigDecimal> volumes;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UnitSelfScheduleEntity)) {
            return false;
        }
        return Objects.nonNull(id) && Objects.equals(id, ((UnitSelfScheduleEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
