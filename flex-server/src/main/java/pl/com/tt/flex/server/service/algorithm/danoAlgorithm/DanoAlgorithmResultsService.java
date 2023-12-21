package pl.com.tt.flex.server.service.algorithm.danoAlgorithm;

import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.dataimport.ImportDataException;

import java.io.IOException;
import java.util.List;

public interface DanoAlgorithmResultsService {
    void parseDanoAlgorithmResults(List<FileDTO> files, Long evaluationId, String langKey) throws IOException, ImportDataException;
}
