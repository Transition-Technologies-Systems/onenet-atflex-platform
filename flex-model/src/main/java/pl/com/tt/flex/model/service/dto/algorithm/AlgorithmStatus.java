package pl.com.tt.flex.model.service.dto.algorithm;

import java.io.Serializable;

public enum AlgorithmStatus implements Serializable {
	KDM_MODEL_UPDATING,
	KDM_MODEL_NOT_ACQUIRED,
	EVALUATING,
	COMPLETED,
	FAILURE,
	CANCELLED,
	TECHNICAL_FAILURE
}
