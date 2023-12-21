package pl.com.tt.flex.server.service.kpi.generator.algorithm.runtime;

import lombok.Getter;
import org.apache.commons.lang3.Validate;
import org.springframework.data.util.Pair;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Getter
class AverageRuntimeAlgorithmAgnoData {

	/**
	 * List zawieracjaca obliczenia algorytmów - jego typ, datę zakończenia i deltę czasu(ile zajęło obliczenie).
	 */
	private final List<AlgorithmRuntime> algorithmRuntime;

	/**
	 * Mapa zawirająca średni czas obliczeń dla danego typu
	 */
	private final Map<Pair<AlgorithmType, String>, Duration> algorithmTypeDuration;

	/**
	 * Średni czas obliczeń
	 */
	private final Duration averageDuration;

	public AverageRuntimeAlgorithmAgnoData(List<AlgorithmRuntime> algorithmRuntime) {
		Validate.notNull(algorithmRuntime, "algorithmRuntime cannot be null!");

		this.algorithmRuntime = algorithmRuntime;
		this.algorithmTypeDuration = getDurationGroupingByAlgorithmType(algorithmRuntime);
		this.averageDuration = getAverageDuration(algorithmTypeDuration);
	}

	private Duration getAverageDuration(Map<Pair<AlgorithmType, String>, Duration> algorithmTypeDuration) {
		double millis = algorithmTypeDuration.values()
														 .stream()
														 .mapToLong(Duration::toMillis)
														 .average().orElse(0);
		return Duration.ofMillis((long) millis);
	}

	private Map<Pair<AlgorithmType, String>, Duration> getDurationGroupingByAlgorithmType(List<AlgorithmRuntime> algorithmRuntime) {

		Map<Pair<AlgorithmType, String>, List<AlgorithmRuntime>> algorithmRuntimeGroupingByAlgorithmType = algorithmRuntime
				.stream()
				.collect(Collectors.groupingBy(ar -> Pair.of(ar.getAlgorithmType(), ar.getAlgorithmDescription())));

		Map<Pair<AlgorithmType, String>, Double> durationInMillisGroupingByAlgorithmType = algorithmRuntimeGroupingByAlgorithmType
				.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry
						.getValue()
						.stream()
						.map(AlgorithmRuntime::getDelta)
						.filter(Objects::nonNull)
						.mapToLong(Duration::toMillis)
						.average().orElse(0)
				));
		return durationInMillisGroupingByAlgorithmType
				.entrySet().stream()
				.collect(Collectors.toMap(
						Map.Entry::getKey,
						entry -> Duration.ofMillis(entry.getValue().longValue())
				)).entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing((Pair<AlgorithmType, String> key) -> key.getSecond().toLowerCase(Locale.ROOT))))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}
}
