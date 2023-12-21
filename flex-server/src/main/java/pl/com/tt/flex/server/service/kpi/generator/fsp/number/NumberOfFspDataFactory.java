package pl.com.tt.flex.server.service.kpi.generator.fsp.number;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.repository.unit.UnitRepository;

import java.math.BigDecimal;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
public class NumberOfFspDataFactory {

    private final UnitRepository unitRepository;

    public NumberOfFspData create() {
        return new NumberOfFspData(BigDecimal.valueOf(unitRepository.count()));
    }
}
