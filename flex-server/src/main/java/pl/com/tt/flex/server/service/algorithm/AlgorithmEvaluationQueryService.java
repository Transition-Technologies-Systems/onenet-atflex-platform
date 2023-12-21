package pl.com.tt.flex.server.service.algorithm;

import static pl.com.tt.flex.server.util.CriteriaUtils.setOnlyDayIfEqualsFilter;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.jhipster.service.filter.InstantFilter;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationDTO;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity_;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.repository.algorithm.AlgorithmEvaluationRepository;
import pl.com.tt.flex.server.service.algorithm.dto.AlgorithmEvaluationCriteria;
import pl.com.tt.flex.server.service.algorithm.mapper.AlgorithmEvaluationMapper;
import pl.com.tt.flex.server.service.common.AbstractQueryServiceImpl;
import pl.com.tt.flex.server.service.mapper.EntityMapper;

@Slf4j
@Service
@Transactional(readOnly = true)
public class AlgorithmEvaluationQueryService extends AbstractQueryServiceImpl<AlgorithmEvaluationEntity, AlgorithmEvaluationDTO, Long, AlgorithmEvaluationCriteria> {

    private final AlgorithmEvaluationRepository algorithmEvaluationRepository;

    private final AlgorithmEvaluationMapper algorithmEvaluationMapper;

    public AlgorithmEvaluationQueryService(final AlgorithmEvaluationRepository algorithmEvaluationRepository, final AlgorithmEvaluationMapper algorithmEvaluationMapper) {
        this.algorithmEvaluationRepository = algorithmEvaluationRepository;
        this.algorithmEvaluationMapper = algorithmEvaluationMapper;
    }

    protected Specification<AlgorithmEvaluationEntity> createSpecification(AlgorithmEvaluationCriteria criteria) {
        Specification<AlgorithmEvaluationEntity> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getId(), AlgorithmEvaluationEntity_.id));
            }
            if (criteria.getTypeOfAlgorithm() != null) {
                specification = specification.and(buildSpecification(criteria.getTypeOfAlgorithm(), AlgorithmEvaluationEntity_.typeOfAlgorithm));
            }
            InstantFilter deliveryDayFilter = criteria.getDeliveryDate();
            if (deliveryDayFilter != null) {
                setOnlyDayIfEqualsFilter(deliveryDayFilter);
                specification = specification.and(buildRangeSpecification(deliveryDayFilter, AlgorithmEvaluationEntity_.deliveryDate));
            }
            InstantFilter creationDayFilter = criteria.getCreationDate();
            if (creationDayFilter != null) {
                setOnlyDayIfEqualsFilter(creationDayFilter);
                specification = specification.and(buildRangeSpecification(creationDayFilter, AlgorithmEvaluationEntity_.createdDate));
            }
            InstantFilter endDayFilter = criteria.getEndDate();
            if (endDayFilter != null) {
                setOnlyDayIfEqualsFilter(endDayFilter);
                specification = specification.and(buildRangeSpecification(endDayFilter, AlgorithmEvaluationEntity_.endDate));
            }
            if (criteria.getAlgorithmStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getAlgorithmStatus(), AlgorithmEvaluationEntity_.algorithmStatus));
            }
        }
        return specification;
    }

    @Override
    public String getDefaultOrderProperty() {
        return AlgorithmEvaluationEntity_.ID;
    }

    @Override
    public AbstractJpaRepository<AlgorithmEvaluationEntity, Long> getRepository() {
        return this.algorithmEvaluationRepository;
    }

    @Override
    public EntityMapper<AlgorithmEvaluationDTO, AlgorithmEvaluationEntity> getMapper() {
        return this.algorithmEvaluationMapper;
    }
}
