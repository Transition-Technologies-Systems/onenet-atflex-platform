package pl.com.tt.flex.flex.agno.algorithm.process.builder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import pl.com.tt.flex.flex.agno.algorithm.process.builder.factory.AlgorithmProcessBuilder;

import java.io.File;
import java.io.IOException;

import static pl.com.tt.flex.flex.agno.algorithm.utils.AlgorithmUtils.createFile;

@Slf4j
public abstract class AbstractAlgorithmProcessBuilder implements AlgorithmProcessBuilder {

    @Value("${application.algorithm.run-kdm-mod}")
    private boolean runKdmMod;

    @Override
    public boolean runKdmMod() {
        return this.runKdmMod;
    }

    protected File createLogFile(String logsPath) throws IOException {
        log.info("createLogFile() Std out for algorithm will be logged to '{}'", logsPath);
        return createFile(logsPath);
    }
}
