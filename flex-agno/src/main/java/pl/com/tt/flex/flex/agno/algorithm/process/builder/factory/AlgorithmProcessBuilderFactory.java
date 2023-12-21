package pl.com.tt.flex.flex.agno.algorithm.process.builder.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;

import java.util.List;

@Component
public class AlgorithmProcessBuilderFactory {

    @Autowired
    List<AlgorithmProcessBuilder> algorithmProcessBuilders;

    public AlgorithmProcessBuilder getAlgorithmProcessBuilder(AlgorithmType type) {
        return algorithmProcessBuilders.stream().filter(a -> a.isSupport(type))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Not found algorithm process builder for: " + type));
    }
}
