package pl.com.tt.flex.server.service.kpi.generator.factory;

import pl.com.tt.flex.model.service.dto.kpi.KpiType;
import pl.com.tt.flex.server.service.kpi.generator.KpiGenerator;

public interface KpiGeneratorFactory {

    KpiGenerator getGenerator(KpiType kpiType);
}
