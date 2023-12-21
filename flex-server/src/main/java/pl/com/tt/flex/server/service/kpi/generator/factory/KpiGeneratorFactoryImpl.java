package pl.com.tt.flex.server.service.kpi.generator.factory;

import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;

import java.util.List;

@Component
public class KpiGeneratorFactoryImpl implements KpiGeneratorFactory {

    private final List<KpiGenerator> generators;

    public KpiGeneratorFactoryImpl(List<KpiGenerator> generators) {
        this.generators = generators;
    }


    @Override
    public KpiGenerator getGenerator(KpiType kpiType) {
        return generators.stream()
            .filter(g -> g.isSupported(kpiType))
            .findFirst().orElseThrow(() -> new IllegalStateException("Cannot found kpi generator for type: " + kpiType));
    }
}
