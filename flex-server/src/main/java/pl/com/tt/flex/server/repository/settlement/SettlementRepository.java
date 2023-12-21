package pl.com.tt.flex.server.repository.settlement;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import pl.com.tt.flex.server.domain.settlement.SettlementEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

@Repository
public interface SettlementRepository extends AbstractJpaRepository<SettlementEntity, Long> {

    @Modifying
    @Query("DELETE FROM SettlementEntity s WHERE s.offerId = :offerId")
    void deleteByOfferId(@Param("offerId") Long offerId);

}
