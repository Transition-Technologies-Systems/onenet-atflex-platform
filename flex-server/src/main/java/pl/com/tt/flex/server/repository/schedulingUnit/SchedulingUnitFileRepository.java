package pl.com.tt.flex.server.repository.schedulingUnit;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitFileEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data  repository for the FlexPotentialFile entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SchedulingUnitFileRepository extends AbstractJpaRepository<SchedulingUnitFileEntity, Long> {

	List<SchedulingUnitFileEntity> findAllBySchedulingUnitId(Long id);

	Optional<SchedulingUnitFileEntity> findByIdAndSchedulingUnitBspId(Long fileId, Long fspId);
}
