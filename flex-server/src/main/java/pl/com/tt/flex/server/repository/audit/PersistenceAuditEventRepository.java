package pl.com.tt.flex.server.repository.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pl.com.tt.flex.server.domain.audit.PersistentAuditEventEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for the {@link PersistentAuditEventEntity} entity.
 */
public interface PersistenceAuditEventRepository extends AbstractJpaRepository<PersistentAuditEventEntity, Long> {

    List<PersistentAuditEventEntity> findByPrincipal(String principal);

    List<PersistentAuditEventEntity> findByPrincipalAndAuditEventDateAfterAndAuditEventType(String principal, Instant after, String type);

    Page<PersistentAuditEventEntity> findAllByAuditEventDateBetween(Instant fromDate, Instant toDate, Pageable pageable);

    List<PersistentAuditEventEntity> findByAuditEventDateBefore(Instant before);
}
