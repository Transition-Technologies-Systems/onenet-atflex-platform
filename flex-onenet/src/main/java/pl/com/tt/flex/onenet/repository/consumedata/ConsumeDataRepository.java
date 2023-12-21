package pl.com.tt.flex.onenet.repository.consumedata;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Repository;

import pl.com.tt.flex.onenet.domain.consumedata.ConsumeDataEntity;
import pl.com.tt.flex.onenet.repository.AbstractJpaRepository;

@Repository
public interface ConsumeDataRepository extends AbstractJpaRepository<ConsumeDataEntity, Long> {

	boolean existsByOnenetIdEquals(String onenetId);

	Optional<ConsumeDataEntity> findByOnenetIdEquals(String onenetId);

	List<ConsumeDataEntity> findAllByOnenetIdIn(Set<String> onenetIds);

}
