package pl.com.tt.flex.server.service.kpi.generator.unit.number.availablebsp;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;

import java.math.BigDecimal;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
class NumberOfDerAvailableForBspDataFactory {

    private final FlexPotentialRepository flexPotentialRepository;

    public NumberOfDerAvailableForBspData create() {
        BigDecimal dersWithBalancingProucts = BigDecimal.valueOf(flexPotentialRepository.countDerJoinedToFlexPotentialWithBalancingProduct());
        return new NumberOfDerAvailableForBspData(dersWithBalancingProucts);
    }
}
