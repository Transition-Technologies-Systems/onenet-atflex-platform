package pl.com.tt.flex.server.repository.potential;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.potential.FlexPotentialFileEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;

/**
 * Spring Data  repository for the FlexPotentialFile entity.
 */
@SuppressWarnings("unused")
@Repository
public interface FlexPotentialFileRepository extends AbstractJpaRepository<FlexPotentialFileEntity, Long> {

	List<FlexPotentialFileEntity> findAllByFlexPotentialId(Long id);
}
