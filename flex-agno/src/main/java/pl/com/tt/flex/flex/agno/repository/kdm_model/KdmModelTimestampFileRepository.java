package pl.com.tt.flex.flex.agno.repository.kdm_model;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelTimestampFileEntity;
import pl.com.tt.flex.flex.agno.repository.AbstractJpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data  repository for the KdmModelTimestampFileEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface KdmModelTimestampFileRepository extends AbstractJpaRepository<KdmModelTimestampFileEntity, Long> {

    boolean existsByTimestampAndKdmModelId(String timestamp, Long kdmModelId);

    List<KdmModelTimestampFileEntity> findAllByKdmModelId(Long kdmModelId);

    Optional<KdmModelTimestampFileEntity> findByTimestampAndKdmModelId(String timestamp, Long kdmModelId);
}
