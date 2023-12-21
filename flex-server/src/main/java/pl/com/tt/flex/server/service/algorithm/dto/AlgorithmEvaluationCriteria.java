package pl.com.tt.flex.server.service.algorithm.dto;

import java.io.Serializable;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LongFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AlgorithmEvaluationCriteria implements Serializable, Criteria {

    public static class AlgorithmTypeFilter extends Filter<AlgorithmType> {

        public AlgorithmTypeFilter() {
        }

        public AlgorithmTypeFilter(AlgorithmEvaluationCriteria.AlgorithmTypeFilter filter) {
            super(filter);
        }

        @Override
        public AlgorithmEvaluationCriteria.AlgorithmTypeFilter copy() {
            return new AlgorithmEvaluationCriteria.AlgorithmTypeFilter(this);
        }

    }

    public static class AlgoritmStatusFilter extends Filter<AlgorithmStatus> {

        public AlgoritmStatusFilter() {
        }

        public AlgoritmStatusFilter(AlgorithmEvaluationCriteria.AlgoritmStatusFilter filter) {
            super(filter);
        }

        @Override
        public AlgorithmEvaluationCriteria.AlgoritmStatusFilter copy() {
            return new AlgorithmEvaluationCriteria.AlgoritmStatusFilter(this);
        }

    }

    private LongFilter id;

    private AlgorithmTypeFilter typeOfAlgorithm;

    private InstantFilter deliveryDate;

    private InstantFilter creationDate;

    private InstantFilter endDate;

    private AlgoritmStatusFilter algorithmStatus;

    public AlgorithmEvaluationCriteria(AlgorithmEvaluationCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.typeOfAlgorithm = other.typeOfAlgorithm == null ? null : other.typeOfAlgorithm.copy();
        this.deliveryDate = other.deliveryDate == null ? null : other.deliveryDate.copy();
        this.creationDate = other.creationDate == null ? null : other.creationDate.copy();
        this.endDate = other.endDate == null ? null : other.endDate.copy();
        this.algorithmStatus = other.algorithmStatus == null ? null : other.algorithmStatus.copy();
    }

    @Override
    public Criteria copy() {
        return new AlgorithmEvaluationCriteria(this);
    }
}
