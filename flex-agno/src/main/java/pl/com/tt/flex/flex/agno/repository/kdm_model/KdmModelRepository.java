package pl.com.tt.flex.flex.agno.repository.kdm_model;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelEntity;
import pl.com.tt.flex.flex.agno.repository.AbstractJpaRepository;

/**
 * Spring Data  repository for the KdmModelEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface KdmModelRepository extends AbstractJpaRepository<KdmModelEntity, Long> {

	boolean existsByAreaName(String areaName);

	@Query(value = "SELECT CASE WHEN count(*)> 0 THEN 'true' ELSE 'false' END FROM algorithm_evaluation WHERE algorithm_evaluation.kdm_model_id = :id",
			nativeQuery = true)
	boolean isUsedInAlgorithmEvaluation(@Param("id") Long id);

}
