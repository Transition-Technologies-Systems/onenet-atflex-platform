package pl.com.tt.flex.model.service.dto.algorithm;

import java.io.Serializable;
import java.time.Instant;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlgorithmEvaluationDTO implements Serializable {
	private Long evaluationId;
	private Long kdmModelId;
	private AlgorithmType typeOfAlgorithm;
	private Instant deliveryDate;
	private Instant creationDate;
	private Instant endDate;
	private AlgorithmStatus status;
}
