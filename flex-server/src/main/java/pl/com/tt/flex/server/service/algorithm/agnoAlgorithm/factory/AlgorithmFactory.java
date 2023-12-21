package pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.factory;

import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;

public interface AlgorithmFactory {

    AgnoAlgorithm getAlgorithm(AlgorithmType algorithmType);
}
