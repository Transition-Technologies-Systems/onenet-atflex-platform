package pl.com.tt.flex.flex.agno.algorithm.process.builder.factory;

import pl.com.tt.flex.flex.agno.algorithm.AlgorithmProperties;
import pl.com.tt.flex.flex.agno.algorithm.utils.AlgorithmProcessParam;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;

import java.io.IOException;

public interface AlgorithmProcessBuilder {

    ProcessBuilder build(AlgorithmProperties algorithmProperties, AlgorithmProcessParam algorithmProcessParam) throws IOException;

    boolean isSupport(AlgorithmType type);

    boolean runKdmMod();

    /**
     * Obsluga pobieranie nowych plikow z algorytmu NCBJ. Sa one generowane na koniec obliczeń dla następujących algorytmów:
     * PBCM
     * DGIA
     * PURE
     * Aby zostały one zwrócone przez algorytm należy na końcu komendy uruchamiania tych algorytmów dodać --extended
     */
    boolean extendedEnabled();
}
