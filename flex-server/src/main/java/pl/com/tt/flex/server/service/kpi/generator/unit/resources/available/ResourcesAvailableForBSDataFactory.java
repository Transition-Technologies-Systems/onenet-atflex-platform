package pl.com.tt.flex.server.service.kpi.generator.unit.resources.available;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;
import pl.com.tt.flex.server.repository.unit.UnitRepository;

import java.math.BigDecimal;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
class ResourcesAvailableForBSDataFactory {

    private final UnitRepository unitRepository;
    private final FlexPotentialRepository flexPotentialRepository;

    public ResourcesAvailableForBSData create() {
        BigDecimal certifiedDers = unitRepository.countCertified();
        BigDecimal dersWithPotential = BigDecimal.valueOf(flexPotentialRepository.countDerJoinedToFlexPotentialWithBalancingProduct());

        return new ResourcesAvailableForBSData(dersWithPotential, certifiedDers);
    }
}
