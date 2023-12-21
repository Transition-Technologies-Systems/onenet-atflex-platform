package pl.com.tt.flex.server.service.kpi.generator.unit.capacity.total.forbsp;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class TotalCapacityDerAvailableBspDataFactory {

    private FlexPotentialRepository flexPotentialRepository;

    public TotalCapacityDerAvailableBspData create() {
        List<FlexPotentialEntity> fp = flexPotentialRepository.findAllByProductBalancingIsTrueAndRegisterIsTrue();
        BigDecimal totalVolume = calculateTotalVolume(fp);
        return new TotalCapacityDerAvailableBspData(totalVolume);
    }

    private static BigDecimal calculateTotalVolume(List<FlexPotentialEntity> fp) {
        return fp.stream()
                 .map(FlexPotentialEntity::getVolume)
                 .filter(Objects::nonNull)
                 .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
