package pl.com.tt.flex.server.repository.settlement;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;

import pl.com.tt.flex.server.domain.settlement.SettlementViewEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

@Repository
public interface SettlementViewRepository extends AbstractJpaRepository<SettlementViewEntity, Long> {

    List<SettlementViewEntity> findAllByOfferIdIn(List<Long> offerIds);

    List<SettlementViewEntity> findAllByIdIn(Collection<Long> ids);

}
