package pl.com.tt.flex.server.repository.product.forecastedPrices;

import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.product.ForecastedPricesEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.time.Instant;
import java.util.Optional;

/**
 * Spring Data  repository for the {@link ForecastedPricesEntity} entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ForecastedPricesRepository extends AbstractJpaRepository<ForecastedPricesEntity, Long> {

    Optional<ForecastedPricesEntity> findByForecastedPricesDateAndProductId(Instant date, long productId);
}
