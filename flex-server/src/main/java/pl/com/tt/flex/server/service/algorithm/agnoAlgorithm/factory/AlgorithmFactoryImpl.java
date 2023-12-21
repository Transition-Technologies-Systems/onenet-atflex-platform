package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;

import java.util.List;

@Component
public class AlgorithmFactoryImpl implements AlgorithmFactory {

    @Autowired
    List<AgnoAlgorithm> algorithmList;

    @Override
    public AgnoAlgorithm getAlgorithm(AlgorithmType algorithmType) {
        return algorithmList.stream().filter(algorithm -> algorithm.isSupport(algorithmType))
            .findFirst().orElseThrow(() -> new IllegalStateException(String.format("Algorithm with type %s is not supported", algorithmType)));
    }
}
