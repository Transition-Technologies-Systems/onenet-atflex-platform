package pl.com.tt.flex.flex.agno.algorithm.process.builder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.flex.agno.algorithm.AlgorithmProperties;
import pl.com.tt.flex.flex.agno.algorithm.process.builder.factory.AlgorithmProcessBuilder;
import pl.com.tt.flex.flex.agno.algorithm.utils.AlgorithmProcessParam;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;

import java.io.File;
import java.io.IOException;

import static pl.com.tt.flex.flex.agno.algorithm.utils.AlgorithmUtils.getPythonCommand;
import static pl.com.tt.flex.flex.agno.algorithm.utils.Constants.DISAGGREGATION_ALGORITHM_VARIANT;

@Component
@Slf4j
public class DisaggregationAlgorithmProcessBuilder extends AbstractAlgorithmProcessBuilder implements AlgorithmProcessBuilder {

    @Override
    public ProcessBuilder build(AlgorithmProperties algorithmProperties, AlgorithmProcessParam algorithmProcessParam) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder()
                .inheritIO()
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.appendTo(createLogFile(algorithmProcessParam.getLogFilePath())))
                .directory(new File(algorithmProperties.getPath()));

        String variant = DISAGGREGATION_ALGORITHM_VARIANT;
        String kdmPath = algorithmProcessParam.getKdmFilePath();
        String offerPath = algorithmProcessParam.getInputFilePath();
        String configPath = algorithmProperties.getConfigFilePath();
        String workspacePath = algorithmProcessParam.getDirPath();
        String activePower = algorithmProcessParam.getActivePower();
        String pMin = algorithmProcessParam.getPMin();
        String pMax = algorithmProcessParam.getPMax();
        String pythonCommand = getPythonCommand();

        processBuilder.command(pythonCommand,
                "-m", "cli",
                "--variant", variant,
                "--kdm", kdmPath,
                "--offers", offerPath,
                "--config", configPath,
                "--workspace", workspacePath,
                "--out_dir", algorithmProcessParam.getDirPath(),
                "--active_power", activePower, pMin, pMax);
        return processBuilder;
    }

    @Override
    public boolean isSupport(AlgorithmType type) {
        return AlgorithmType.DISAGGREGATION.equals(type);
    }

    @Override
    public boolean extendedEnabled() {
        return false;
    }
}