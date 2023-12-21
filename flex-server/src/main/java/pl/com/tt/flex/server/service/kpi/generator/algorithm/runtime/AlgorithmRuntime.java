package pl.com.tt.flex.server.service.kpi.generator.algorithm.runtime;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Getter
class AlgorithmRuntime {

	/**
	 * ID obliczen
	 */
	private final Long id;

	/**
	 * Typ algorytmu
	 */
	private final AlgorithmType algorithmType;

	/**
	 * Tlumaczenie dla danego typu algorytmu
	 */
	private final String algorithmDescription;

	/**
	 * Data uruchomienia
	 */
	private final Instant startDate;

	/**
	 * Delta czasu obliczen algorytmu
	 */
	private final Duration delta;

	AlgorithmRuntime(Long id, AlgorithmType algorithmType, String algorithmDescription, Instant startDate, Instant endDate) {
		Validate.notNull(id, "id cannot be null!");
		Validate.notNull(algorithmType, "algorithmType cannot be null!");
		Validate.notNull(startDate, "startDate cannot be null!");
		Validate.notNull(endDate, "endDate cannot be null!");
		Validate.notNull(algorithmDescription, "algorithmDescription cannot be null!");

		this.id = id;
		this.algorithmType = algorithmType;
		this.algorithmDescription = algorithmDescription;
		this.startDate = startDate;
		this.delta = Duration.between(startDate.truncatedTo(ChronoUnit.SECONDS), endDate.truncatedTo(ChronoUnit.SECONDS));
	}
}
