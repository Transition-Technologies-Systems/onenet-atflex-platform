package pl.com.tt.flex.server.service.algorithm.view.dto;

import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class AlgorithmEvaluationViewCriteria implements Serializable, Criteria {

    public static class AlgorithmTypeFilter extends Filter<AlgorithmType> {

        public AlgorithmTypeFilter() {
        }

        public AlgorithmTypeFilter(AlgorithmTypeFilter filter) {
            super(filter);
        }

        @Override
        public AlgorithmTypeFilter copy() {
            return new AlgorithmTypeFilter(this);
        }

    }

    public static class AlgoritmStatusFilter extends Filter<AlgorithmStatus> {

        public AlgoritmStatusFilter() {
        }

        public AlgoritmStatusFilter(AlgoritmStatusFilter filter) {
            super(filter);
        }

        @Override
        public AlgoritmStatusFilter copy() {
            return new AlgoritmStatusFilter(this);
        }

    }

    private LongFilter id;

    private AlgorithmTypeFilter typeOfAlgorithm;

    private StringFilter kdmModelName;

    private LongFilter kdmModelId;

    private InstantFilter deliveryDate;

    private InstantFilter creationDate;

    private InstantFilter endDate;

    private AlgoritmStatusFilter algorithmStatus;

    public AlgorithmEvaluationViewCriteria(AlgorithmEvaluationViewCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.kdmModelName = other.kdmModelName == null ? null : other.kdmModelName.copy();
        this.kdmModelId = other.kdmModelId == null ? null : other.kdmModelId.copy();
        this.typeOfAlgorithm = other.typeOfAlgorithm == null ? null : other.typeOfAlgorithm.copy();
        this.deliveryDate = other.deliveryDate == null ? null : other.deliveryDate.copy();
        this.creationDate = other.creationDate == null ? null : other.creationDate.copy();
        this.endDate = other.endDate == null ? null : other.endDate.copy();
        this.algorithmStatus = other.algorithmStatus == null ? null : other.algorithmStatus.copy();
    }

    @Override
    public Criteria copy() {
        return new AlgorithmEvaluationViewCriteria(this);
    }
}
