package pl.com.tt.flex.server.validator.algorithm;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.util.ZipUtil;

import java.util.List;
import java.util.Objects;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@Component
public class AlgorithmEvaluationValidator {
    public void checkLogFiles(AlgorithmEvaluationEntity algorithmEvaluationEntity) throws ObjectValidationException {
        byte[] logFiles = algorithmEvaluationEntity.getProcessLogsZip();
        if (!checkIfExistFile(logFiles)) {
            throw new ObjectValidationException("Cannot find log files for evaluationID: " + algorithmEvaluationEntity.getId(),
                AGNO_ALGORITHM_CANNOT_FOUND_LOG_FILES);
        }
    }

    public void checkInputFiles(AlgorithmEvaluationEntity algorithmEvaluationEntity) throws ObjectValidationException {
        byte[] inputFiles = algorithmEvaluationEntity.getInputFilesZip();
        if (!checkIfExistFile(inputFiles)) {
            throw new ObjectValidationException("Cannot find input files for evaluationID: " + algorithmEvaluationEntity.getId(),
                AGNO_ALGORITHM_CANNOT_FOUND_INPUT_FILES);
        }

    }

    public void checkOutputFiles(AlgorithmEvaluationEntity algorithmEvaluationEntity) throws ObjectValidationException {
        byte[] outputFiles = algorithmEvaluationEntity.getOutputFilesZip();
        if (!checkIfExistFile(outputFiles)) {
            throw new ObjectValidationException("Cannot find output files for evaluationID: " + algorithmEvaluationEntity.getId(),
                AGNO_ALGORITHM_CANNOT_FOUND_OUTPUT_FILES);
        }
    }

    private boolean checkIfExistFile(byte[] file) {
        if (Objects.isNull(file)) {
            return false;
        }
        List<FileDTO> fileDTOS = ZipUtil.zipToFiles(file);
        return !CollectionUtils.isEmpty(fileDTOS);
    }
}
