package pl.com.tt.flex.model.service.dto.algorithm;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.com.tt.flex.model.service.dto.file.FileDTO;

import java.io.Serializable;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class AlgEvaluationModuleDTO implements Serializable {
	private Long evaluationId;
	private AlgorithmStatus status;
	private AlgorithmType typeOfAlgorithm;
    private Long kdmModelId;
    private FileDTO inputFilesZip;
	private FileDTO outputFilesZip;
	private FileDTO logFilesZip;
	private String errorMessage;
	private Map<String, AlgorithmPowerArgsDTO> powerArgsByTimestamp;
}
