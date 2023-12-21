package pl.com.tt.flex.server.service.kpi.generator.unit.prequalified.successfully;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.repository.unit.UnitRepository;

import java.math.BigDecimal;

@Component
@Transactional(readOnly = true)
class UnitSuccessfullyPrequalifiedDataFactory {

    private final UnitRepository unitRepository;

    UnitSuccessfullyPrequalifiedDataFactory(UnitRepository unitRepository) {
        this.unitRepository = unitRepository;
    }

    UnitSuccessfullyPrequalifiedData create() {
        BigDecimal certifiedDers = unitRepository.countCertified();
        BigDecimal allDers = BigDecimal.valueOf(unitRepository.count());
        return new UnitSuccessfullyPrequalifiedData(certifiedDers, allDers);
    }
}
