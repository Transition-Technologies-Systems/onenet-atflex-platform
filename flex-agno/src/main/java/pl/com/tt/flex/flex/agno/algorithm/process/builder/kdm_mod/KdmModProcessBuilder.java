package pl.com.tt.flex.flex.agno.algorithm.process.builder.kdm_mod;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.flex.agno.algorithm.AlgorithmProperties;
import pl.com.tt.flex.flex.agno.algorithm.utils.AlgorithmProcessParam;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static pl.com.tt.flex.flex.agno.algorithm.utils.AlgorithmUtils.*;
import static pl.com.tt.flex.flex.agno.algorithm.utils.Constants.*;

@Slf4j
public class KdmModProcessBuilder {

    public ProcessBuilder build(AlgorithmProperties algorithmProperties, AlgorithmProcessParam algorithmProcessParam) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder()
                .inheritIO()
                .redirectErrorStream(true)
                .redirectOutput(createFile(algorithmProcessParam.getLogFilePath()))
                .directory(new File(algorithmProperties.getPath()));

        String variant = KDM_MOD;
        String kdmPath = algorithmProcessParam.getKdmFilePath();
        String offerPath = algorithmProcessParam.getInputFilePath();
        String configPath = algorithmProperties.getConfigFilePath();
        String workspacePath = algorithmProcessParam.getDirPath();
        String pythonCommand = getPythonCommand();

        processBuilder.command(pythonCommand,
                "-m", "cli",
                "--variant", variant,
                "--kdm", kdmPath,
                "--offers", offerPath,
                "--config", configPath,
                "--workspace", workspacePath,
                "--mod_active");
        return processBuilder;
    }

    public void updateKdmPath(AlgorithmProcessParam algorithmProcessParam) throws KdmModNotAcquiredException {
        Path path = Path.of(algorithmProcessParam.getKdmFilePath());
        String kdmModFilename = KDM_MOD_OUTPUT_PREFIX + path.getFileName();
        String kdmModPath = path.getParent() + SLASH_SYMBOL + kdmModFilename;
        if (isFileExist(kdmModPath)) {
            algorithmProcessParam.setKdmFilePath(kdmModPath);
            return;
        }
        throw new KdmModNotAcquiredException(kdmModPath);
    }
}
