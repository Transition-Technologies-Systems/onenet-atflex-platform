package pl.com.tt.flex.server.repository.unit;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.unit.UnitGeoLocationEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;

/**
 * Spring Data  repository for the UnitGeoLocationEntity.
 */
@SuppressWarnings("unused")
@Repository
public interface UnitGeoLocationRepository extends AbstractJpaRepository<UnitGeoLocationEntity, Long> {

    List<UnitGeoLocationEntity> findAllByUnitId(Long unitId);
}
