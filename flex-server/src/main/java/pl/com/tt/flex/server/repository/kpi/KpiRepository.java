package pl.com.tt.flex.server.repository.kpi;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.kpi.KpiEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

/**
 * Spring Data  repository for the KpiEntity.
 */
@SuppressWarnings("unused")
@Repository
public interface KpiRepository extends AbstractJpaRepository<KpiEntity, Long> {
}
