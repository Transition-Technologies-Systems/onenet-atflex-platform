package pl.com.tt.flex.server.service.kpi.generator.algorithm.runtime;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStatus;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.repository.algorithm.AlgorithmEvaluationRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.service.kpi.generator.utils.ValidatorUtils.checkValid;

@Component
@AllArgsConstructor
@Transactional(readOnly = true)
class AverageRuntimeAlgorithmAgnoDataFactory {

	private final AlgorithmEvaluationRepository algorithmEvaluationRepository;
	private final MessageSource messageSource;

	public AverageRuntimeAlgorithmAgnoData create() {
		List<AlgorithmEvaluationEntity> algorithmCalculations = algorithmEvaluationRepository.findByAlgorithmStatus(AlgorithmStatus.COMPLETED);
		List<AlgorithmRuntime> algorithmRuntimes = algorithmCalculations
				.stream()
				.map(alg -> new AlgorithmRuntime(alg.getId(), alg.getTypeOfAlgorithm(), getAlgorithmDescription(alg.getTypeOfAlgorithm()), alg.getCreatedDate(), alg.getEndDate()))
				.sorted(Comparator.comparingLong(AlgorithmRuntime::getId))
				.collect(Collectors.toUnmodifiableList());
		checkValid(algorithmRuntimes);
		return new AverageRuntimeAlgorithmAgnoData(algorithmRuntimes);
	}

	/**
	* Zwraca tlumaczenie dla danego typu algorytmu
	*/
	private String getAlgorithmDescription(AlgorithmType type) {
		final String code = "kpi.algorithm.type.";
		return messageSource.getMessage(code + type.name(), null, Locale.getDefault());
	}
}
