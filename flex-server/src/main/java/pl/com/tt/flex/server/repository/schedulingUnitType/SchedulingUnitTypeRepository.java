package pl.com.tt.flex.server.repository.schedulingUnitType;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.Optional;

/**
 * Spring Data  repository for the SchedulingUnitTypeEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SchedulingUnitTypeRepository extends AbstractJpaRepository<SchedulingUnitTypeEntity, Long> {

    Optional<SchedulingUnitTypeEntity> findByDescriptionEn(String descriptionEn);
}
