package pl.com.tt.flex.server.repository.kpi;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.kpi.KpiView;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

/**
 * Spring Data  repository for the KpiView.
 */
@SuppressWarnings("unused")
@Repository
public interface KpiViewRepository extends AbstractJpaRepository<KpiView, Long> {
}
