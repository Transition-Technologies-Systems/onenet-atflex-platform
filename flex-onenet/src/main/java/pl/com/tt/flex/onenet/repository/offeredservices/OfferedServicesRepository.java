package pl.com.tt.flex.onenet.repository.offeredservices;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import pl.com.tt.flex.onenet.domain.offeredservices.OfferedServiceEntity;
import pl.com.tt.flex.onenet.repository.AbstractJpaRepository;

@Repository
public interface OfferedServicesRepository extends AbstractJpaRepository<OfferedServiceEntity, Long> {

	boolean existsByOnenetIdEquals(String onenetId);

	Optional<OfferedServiceEntity> findByOnenetIdEquals(String onenetId);

}
