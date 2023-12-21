package pl.com.tt.flex.server.domain.algorithm;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.server.domain.auction.offer.cmvc.AuctionCmvcOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.audit.AbstractAuditingEntity;
import pl.com.tt.flex.server.refreshView.listener.AlgorithmEvaluationListener;

@Entity
@Table(name = "algorithm_evaluation")
@EntityListeners(AlgorithmEvaluationListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@GenericGenerator(
    name = "algorithm_evaluation_id_generator",
    strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
    parameters = {
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.SEQUENCE_PARAM, value = "algorithm_evaluation_seq"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INITIAL_PARAM, value = "1"),
        @org.hibernate.annotations.Parameter(name = SequenceStyleGenerator.INCREMENT_PARAM, value = "1")
    }
)
public class AlgorithmEvaluationEntity extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "algorithm_evaluation_id_generator")
    private Long id;

    @NotNull
    @Column(name = "kdm_model_id")
    private Long kdmModelId;

    @NotNull
    @Column(name = "type_of_algorithm")
    @Enumerated(EnumType.STRING)
    private AlgorithmType typeOfAlgorithm;

    @NotNull
    @Column(name = "delivery_date")
    private Instant deliveryDate;

    @Column(name = "end_date")
    private Instant endDate;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @NotNull
    @Column(name = "input_files_zip", nullable = false)
    private byte[] inputFilesZip;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "output_files_zip")
    private byte[] outputFilesZip;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "process_logs_zip")
    private byte[] processLogsZip;

    @NotNull
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AlgorithmStatus algorithmStatus;

    @ManyToMany(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "alg_evaluation_da_offers",
        joinColumns = @JoinColumn(name = "algorithm_evaluation_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "da_offer_id", referencedColumnName = "id"))
    private Set<AuctionDayAheadOfferEntity> daOffers = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(name = "alg_evaluation_cmvc_offers",
        joinColumns = @JoinColumn(name = "algorithm_evaluation_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "cmvc_offer_id", referencedColumnName = "id"))
    private Set<AuctionCmvcOfferEntity> cmvcOffers = new HashSet<>();
}
