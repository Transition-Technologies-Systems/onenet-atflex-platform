package pl.com.tt.flex.flex.agno.algorithm.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.flex.agno.algorithm.AlgorithmProperties;
import pl.com.tt.flex.flex.agno.algorithm.AlgorithmServiceImpl;
import pl.com.tt.flex.flex.agno.service.kdm_model.KdmModelTimestampFileService;
import pl.com.tt.flex.flex.agno.util.ZipUtil;
import pl.com.tt.flex.model.service.dto.algorithm.AlgEvaluationModuleDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmPowerArgsDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pl.com.tt.flex.flex.agno.algorithm.utils.AlgorithmUtils.*;
import static pl.com.tt.flex.flex.agno.algorithm.utils.Constants.*;

@Component
@Slf4j
public class AlgorithmFileUtils {

    private final KdmModelTimestampFileService kdmModelTimestampFileService;

    public AlgorithmFileUtils(KdmModelTimestampFileService kdmModelTimestampFileService) {
        this.kdmModelTimestampFileService = kdmModelTimestampFileService;
    }

    /**
     * Metoda wypakowuje wszystkie pliki wsadowe na serverze oraz tworzy liste parametrow
     * uzywanych do wywolania algorytmu
     */
    public List<AlgorithmProcessParam> unpackImportFiles(AlgEvaluationModuleDTO evaluationModuleDTO,
                                                         AlgorithmProperties algorithmProperties,
                                                         boolean extendedEnabled) {
        File saveDir = getProcessDirectory(evaluationModuleDTO, algorithmProperties);
        log.debug("unpackImportFiles() Unpacking input files to {}", saveDir);
        FileDTO importFile = evaluationModuleDTO.getInputFilesZip();
        List<FileDTO> fileDTOS = ZipUtil.zipToFiles(importFile.getBytesData());
        fileDTOS = ignoreFilesWithExtension(fileDTOS, KDM_EXTENSION);
        log.info("unpackImportFiles() Found {} input files.", fileDTOS.size());
        List<AlgorithmProcessParam> algorithmProcessParams = new ArrayList<>();
        try {
            for (FileDTO fileDTO : fileDTOS) {
                AlgorithmProcessParam algorithmProcessFile = saveFileAndPrepareAlgorithmProcessFile(evaluationModuleDTO, saveDir, fileDTO, extendedEnabled);
                algorithmProcessParams.add(algorithmProcessFile);
            }
        } catch (Exception exception) {
            log.info("unpackImportFiles() Problem with unpack input files. Ex msg: {}", exception.getMessage());
            exception.printStackTrace();
            throw new AlgorithmServiceImpl.AlgorithmException("Problem with unpack input files");
        }
        return algorithmProcessParams;
    }

    /**
     * Pobieranie plikow dodatkowych ktore nalezy dodac do plikow wyjsciowych
     *
     * @param additionalFiles Sciezki do plikow ktore nalezy pobrac
     * @param outputFiles Lista do ktorej zostaną dodane pliki
     */
    public void catchAdditionalFiles(List<FileDTO> outputFiles, List<String> additionalFiles) {
        log.debug("catchAdditionalFiles() START - Catch additional files");
        for (String filename : additionalFiles) {
            try {
                catchFile(outputFiles, filename);
            } catch (Exception e) {
                log.error("catchAdditionalFiles() Problem with catch additional file: {}. Exception msg: {}",filename, e.getMessage());
            }
        }
        log.debug("catchAdditionalFiles() END - Catch additional files");
    }

    /**
     * Pobeiranie plikow wyjsciowych z skryptow python'owych
     */
    public void catchFile(List<FileDTO> files, String filePath) {
        try {
            log.info("catchOutputFiles() Try to get file: {}", filePath);
            files.add(getFileFromPath(filePath));
            log.info("catchOutputFiles() Get file: {}", filePath);
        } catch (IOException e) {
            throw new AlgorithmServiceImpl.AlgorithmException(String.format("Problem with catch file for filePath %s.", filePath));
        }
    }

    /**
     * Pobieranie logow algorytmu
     */
    public void catchLogFiles(List<FileDTO> logFiles, AlgorithmProcessParam algorithmParam) {
        try {
            log.info("catchOutputFiles() Try to get log file: {}", algorithmParam.getLogFilePath());
            logFiles.add(getFileFromPath(algorithmParam.getLogFilePath()));
            log.info("catchOutputFiles() Get output file: {}", algorithmParam.getLogFilePath());
        } catch (IOException e) {
            throw new AlgorithmServiceImpl.AlgorithmException(String.format("Problem with catch log files for input file %s.", algorithmParam.getInputFilename()));
        }
    }

    /**
     * Przygotowanie plikow wejsciowych do uruchomienia algorytmu
     */
    private AlgorithmProcessParam saveFileAndPrepareAlgorithmProcessFile(AlgEvaluationModuleDTO evaluationModuleDTO, File saveDir,
                                                                         FileDTO inputFile, boolean extendedEnabled) throws IOException {
        saveAndGetPathToInputFile(saveDir, inputFile);
        String timestamp = extractTimestampFromFileDTO(inputFile);
        String kdmFilePath;
        try {
            kdmFilePath = saveKdmAndGetPath(saveDir, timestamp, evaluationModuleDTO.getKdmModelId());
        } catch (IOException e) {
            log.info("saveFileAndPrepareAlgorithmProcessFile() Encountered problem while saving kdm file. Ex msg: {}", e.getMessage());
            e.printStackTrace();
            throw new AlgorithmServiceImpl.AlgorithmException("Problem with kdm file");
        }
        String activePower = "";
        String pMin = "";
        String pMax = "";
        if (evaluationModuleDTO.getTypeOfAlgorithm().equals(AlgorithmType.DISAGGREGATION)) {
            AlgorithmPowerArgsDTO powerArgs = Optional.ofNullable(evaluationModuleDTO.getPowerArgsByTimestamp())
                    .map(a -> a.get(timestamp))
                    .orElseThrow(() -> new AlgorithmServiceImpl.AlgorithmException("Missing power args for timestamp " + timestamp));
            activePower = String.valueOf(powerArgs.getActivePower());
            log.info("Active power: " + activePower);
            pMin = String.valueOf(powerArgs.getPMin());
            log.info("pMin: " + pMin);
            pMax = String.valueOf(powerArgs.getPMax());
            log.info("pMax: " + pMax);
        }
        List<String> additionalFiles = getAdditionalOutputFiles(evaluationModuleDTO, saveDir, inputFile, extendedEnabled);
        log.info("unpackImportFiles() Saved file in server: {}", inputFile.getFileName());
        return new AlgorithmProcessParam(saveDir.getAbsolutePath(), inputFile.getFileName(), evaluationModuleDTO.getTypeOfAlgorithm(),
                kdmFilePath, activePower, pMin, pMax, additionalFiles);
    }

    /**
     * Zwraca liste dodatkowych plikow ktore nalezy dodac do plikow wyjsciowych. Nalezy podawac pełnąc ścieżkę do plików!
     */
    private List<String> getAdditionalOutputFiles(AlgEvaluationModuleDTO evaluationModuleDTO, File saveDir, FileDTO inputFile, boolean extendedEnabled) {
        ArrayList<String> files = new ArrayList<>();
        if (extendedEnabled) {
            files.addAll(getAlgorithmExtendedFilesName(inputFile.getFileName(), saveDir.getAbsolutePath(), evaluationModuleDTO.getTypeOfAlgorithm()));
        }
        return files;
    }

    /**
     * Zapis pliku wejsciowego do folderu roboczego
     */
    private void saveAndGetPathToInputFile(File saveDir, FileDTO inputFile) throws IOException {
        log.debug("saveAndGetPathToInputFile() Save input file with name={} to dir={}", inputFile.getFileName(), saveDir.getAbsolutePath());
        String filePath = saveDir.getAbsolutePath() + SLASH_SYMBOL + inputFile.getFileName();
        FileUtils.writeByteArrayToFile(new File(filePath), inputFile.getBytesData());
    }

    /**
     * Zapis pliku kdm do folderu roboczego
     */
    private String saveKdmAndGetPath(File saveDir, String timestamp, Long kdmModuleId) throws IOException {
        FileDTO kdmFile = kdmModelTimestampFileService.findKdmTimestampFileByTimestampAndKdmModelId(timestamp, kdmModuleId);
        log.debug("saveAndGetPathToInputFile() Save kdm file with name={} to dir={}", kdmFile.getFileName(), saveDir.getAbsolutePath());
        String filePath = saveDir.getAbsolutePath() + SLASH_SYMBOL + kdmFile.getFileName();
        FileUtils.writeByteArrayToFile(new File(filePath), kdmFile.getBytesData());
        return filePath;
    }

    /**
     * Utworzenie katalogu roboczego dla danej ewaluacji algorytmu
     */
    private File getProcessDirectory(AlgEvaluationModuleDTO evaluationModuleDTO, AlgorithmProperties algorithmProperties) {
        String processDirectoryPath = algorithmProperties.getProcessDirectoryPath();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_WITH_TIME_FORMAT).withZone(ZoneId.systemDefault());
        String date = formatter.format(Instant.now());
        String directory = String.format(PROCESS_DIRECTORY_FORMAT, evaluationModuleDTO.getEvaluationId(), date);
        File file = Paths.get(processDirectoryPath, directory).toFile();
        file.mkdirs();
        return file;
    }

    /**
     * Z listy plikow ignoruje pliki ktore maja rozszerzenie zgodne z zadanym parametrem
     */
    private static List<FileDTO> ignoreFilesWithExtension(List<FileDTO> fileDTOS, String extension) {
        fileDTOS = fileDTOS.stream().filter(f -> !f.getFileName().endsWith(extension)).collect(Collectors.toList());
        return fileDTOS;
    }
}
