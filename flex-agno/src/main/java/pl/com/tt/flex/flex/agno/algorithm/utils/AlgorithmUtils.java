package pl.com.tt.flex.flex.agno.algorithm.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.SystemUtils;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static pl.com.tt.flex.flex.agno.algorithm.utils.Constants.*;

@Slf4j
public class AlgorithmUtils {

    private AlgorithmUtils() {
    }

    public static String getAlgorithmOutputFilename(String inputFilename, AlgorithmType algorithmType, String activePower) {
        if (algorithmType == AlgorithmType.BM) {
            return BM_ALGORITHM_OUTPUT_FILE_PREFIX + inputFilename;
        } else if (algorithmType == AlgorithmType.PBCM) {
            return PBCM_ALGORITHM_OUTPUT_FILE_PREFIX + inputFilename;
        } else if (algorithmType == AlgorithmType.DANO) {
            return DANO_ALGORITHM_OUTPUT_FILE_PREFIX + inputFilename;
        } else if (algorithmType == AlgorithmType.DISAGGREGATION) {
            return DISAGGREGATION_ALGORITHM_OUTPUT_FILE_PREFIX + activePower + "_" + inputFilename;
        } else {
            throw new IllegalStateException("Unrecognized type of algorithm");
        }
    }

    /**
     * Metoda zwraca liste plików ktore sa dodatowo generowane przez algorytm NCBJ, gdy na koncu komendy uruchomieniowe dodany jest parametr --extended
     * <p/>
     * Generwane sa dwa pliki:
     * {process_dir}/{typ_algorytmu}Q_down_{nazwa_pliku_wejscowego}
     * {process_dir}/{typ_algorytmu}Q_up_{nazwa_pliku_wejscowego}
     * <p>
     * Przyklad dla PBCM:
     * {process_dir}/pbcmQ_down_{nazwa_inputa}
     * {process_dir}/pbcmQ_up_{nazwa_inputa}
     */
    public static List<String> getAlgorithmExtendedFilesName(String inputFilename, String directory, AlgorithmType algorithmType) {
        if (algorithmType == AlgorithmType.BM) {
            return getExtendedFilenames(BM_ALGORITHM_VARIANT, inputFilename, directory);
        } else if (algorithmType == AlgorithmType.PBCM) {
            return getExtendedFilenames(PBCM_ALGORITHM_VARIANT, inputFilename, directory);
        } else if (algorithmType == AlgorithmType.DANO) {
            return getExtendedFilenames(DANO_ALGORITHM_VARIANT, inputFilename, directory);
        } else if (algorithmType == AlgorithmType.DISAGGREGATION) {
            return getExtendedFilenames(DISAGGREGATION_ALGORITHM_VARIANT, inputFilename, directory);
        } else {
            throw new IllegalStateException("Unrecognized type of algorithm");
        }
    }

    private static List<String> getExtendedFilenames(String prefix, String inputFilename, String directory) {
        List<String> filenames = new ArrayList<>();
        filenames.add(directory + SLASH_SYMBOL + prefix.toLowerCase() + "Q_up_" + inputFilename);
        filenames.add(directory + SLASH_SYMBOL + prefix.toLowerCase() + "Q_down_" + inputFilename);
        return filenames;
    }

    public static String getPythonCommand() {
        String pythonCommand;
        if (SystemUtils.IS_OS_LINUX) {
            pythonCommand = LINUX_PYTHON3;
        } else if (SystemUtils.IS_OS_WINDOWS) {
            pythonCommand = WIN_PYTHON_3;
        } else {
            throw new RuntimeException("Unknown OS: " + SystemUtils.OS_NAME);
        }
        return pythonCommand;
    }

    public static FileDTO getFileFromPath(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] sourceBytes = Files.readAllBytes(file.getAbsoluteFile().toPath());
        return new FileDTO(file.getName(), sourceBytes);
    }

    public static String extractTimestampFromFileDTO(FileDTO fileDTO) {
        String filename = fileDTO.getFileName();
        if (isTimestampDoubleDigit(filename)) {
            return filename.substring(filename.length() - 7, filename.length() - 5);
        } else {
            return filename.substring(filename.length() - 6, filename.length() - 5);
        }
    }

    public static File createFile(String path) throws IOException {
        Path filePath = Path.of(path);
        File file = filePath.toFile();
        if (!isFileExist(path)) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        return file;
    }

    public static boolean isFileExist(String path) {
        Path filePath = Path.of(path);
        File file = filePath.toFile();
        return file.exists();
    }

    public static boolean isTimestampDoubleDigit(String agnoOutputFilename) {
        return agnoOutputFilename.charAt(agnoOutputFilename.length() - 7) != '_';
    }
}
