package pl.com.tt.flex.server.repository.algorithm;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationViewEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

@Repository
public interface AlgorithmEvaluationViewRepository extends AbstractJpaRepository<AlgorithmEvaluationViewEntity, Long> {
}
