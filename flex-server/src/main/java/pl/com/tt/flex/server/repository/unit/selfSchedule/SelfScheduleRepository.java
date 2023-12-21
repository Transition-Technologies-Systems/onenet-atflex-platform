package pl.com.tt.flex.server.repository.unit.selfSchedule;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.unit.self_schedule.UnitSelfScheduleEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data  repository for the {@link UnitSelfScheduleEntity} entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SelfScheduleRepository extends AbstractJpaRepository<UnitSelfScheduleEntity, Long> {

    Optional<UnitSelfScheduleEntity> findBySelfScheduleDateAndUnitId(Instant date, long unitId);

    List<UnitSelfScheduleEntity> findAllBySelfScheduleDateAndUnitIdIn(Instant date, List<Long> unitIds);

    @Query("SELECT DISTINCT uss FROM UnitSelfScheduleEntity uss JOIN AuctionOfferDersEntity aod ON aod.unit = uss.unit AND aod.offer.id = :offerId " +
        "WHERE uss.selfScheduleDate = aod.offer.auctionDayAhead.deliveryDate")
    List<UnitSelfScheduleEntity> findByOfferId(@Param("offerId") Long offerId);
}
