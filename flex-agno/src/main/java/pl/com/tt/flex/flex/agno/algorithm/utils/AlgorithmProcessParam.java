package pl.com.tt.flex.flex.agno.algorithm.utils;

import lombok.*;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.util.ArrayList;
import java.util.List;

import static pl.com.tt.flex.flex.agno.algorithm.utils.Constants.*;

/**
 * Klasa odpowiedzalna za trzymania parametrow sluzacych do uruchomienia algorytmow.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AlgorithmProcessParam {
    private String inputFilename;
    private String logFilename;
    private String dirPath;
    private String inputFilePath;
    private String logFilePath;
    private String outputFilePath;
    private List<String> additionalOutputFiles = new ArrayList<>();
    private String kdmFilePath;
    private FileDTO logFile;
    private FileDTO outputFile;
    private String activePower;
    private String pMin;
    private String pMax;

    public AlgorithmProcessParam(String dirPath, String inputFilename, AlgorithmType type,
                                 String kdmFilePath, String activePower, String pMin, String pMax,
                                 List<String> additionalOutputFiles) {
        this.inputFilename = inputFilename;
        this.logFilename = inputFilename.replace(XLSX_EXTENSION, LOG_EXTENSION);
        this.dirPath = dirPath;
        this.inputFilePath = dirPath + SLASH_SYMBOL + inputFilename;
        this.outputFilePath = dirPath + SLASH_SYMBOL + AlgorithmUtils.getAlgorithmOutputFilename(inputFilename, type, activePower);
        this.additionalOutputFiles = additionalOutputFiles;
        this.logFilePath = dirPath + SLASH_SYMBOL + inputFilename.replace(XLSX_EXTENSION, LOG_EXTENSION);
        this.kdmFilePath = kdmFilePath;
        this.activePower = activePower;
        this.pMin = pMin;
        this.pMax = pMax;
    }
}
