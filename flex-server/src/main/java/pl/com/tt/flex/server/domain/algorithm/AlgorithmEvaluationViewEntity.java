package pl.com.tt.flex.server.domain.algorithm;

import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@Entity
@Immutable
@Table(name = "algorithm_evaluation_view")
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class AlgorithmEvaluationViewEntity extends AbstractAuditingEntity {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "kdm_model_id")
    private Long kdmModelId;

    @Column(name = "kdm_model_name")
    private String kdmModelName;

    @Column(name = "type_of_algorithm")
    @Enumerated(EnumType.STRING)
    private AlgorithmType typeOfAlgorithm;

    @Column(name = "type_order_pl")
    private Integer typeOrderPl;

    @Column(name = "type_order_en")
    private Integer typeOrderEn;

    @NotNull
    @Column(name = "delivery_date")
    private Instant deliveryDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AlgorithmStatus algorithmStatus;

    @Column(name = "da_offers")
    private String daOffers;

    @Column(name = "cmvc_offers")
    private String cmvcOffers;
}
