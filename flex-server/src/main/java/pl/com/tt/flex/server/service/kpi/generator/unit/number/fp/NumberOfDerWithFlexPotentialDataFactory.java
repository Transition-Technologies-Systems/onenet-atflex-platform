package pl.com.tt.flex.server.service.kpi.generator.unit.number.fp;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;

import java.math.BigDecimal;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
class NumberOfDerWithFlexPotentialDataFactory {

    private final FlexPotentialRepository flexPotentialRepository;

    NumberOfDerWithFlexPotentialData create() {
        BigDecimal countDers = BigDecimal.valueOf(flexPotentialRepository.countDerJoinedToFlexRegister());
        return new NumberOfDerWithFlexPotentialData(countDers);
    }
}
