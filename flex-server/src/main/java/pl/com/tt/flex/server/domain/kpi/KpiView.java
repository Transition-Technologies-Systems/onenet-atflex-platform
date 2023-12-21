package pl.com.tt.flex.server.domain.kpi;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;

import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;

@Data
@Entity
@Immutable
@Table(name = "kpi_view")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class KpiView extends AbstractAuditingEntity implements Serializable {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private KpiType type;

    @Column(name = "type_order_pl")
    private Integer typeOrderPl;

    @Column(name = "type_order_en")
    private Integer typeOrderEn;

    @Column(name = "date_from")
    private Instant dateFrom;

    @Column(name = "date_to")
    private Instant dateTo;

}
