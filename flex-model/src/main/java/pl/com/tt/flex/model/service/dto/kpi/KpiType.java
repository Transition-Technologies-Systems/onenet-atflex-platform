package pl.com.tt.flex.model.service.dto.kpi;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public enum KpiType {
	/**
	 * Aktywne uczestnictwo
	 */
	ACTIVE_PARTICIPATION(false),
	/***
	 * Liczba FSP
	 */
	NUMBER_OF_FSPS(false),
	/***
	 * Liczba transakcji
	 */
	NUMBER_OF_TRANSACTIONS(true),
	/***
	 * Wolumen transakcji
	 */
	VOLUME_OF_TRANSACTIONS(true),
	/***
	 * Wolumen transakcji ofert rozliczonych
	 */
	VOLUME_OF_TRANSACTIONS_CLEARED_BIDS(true),
	/***
	 * Dostępna elastyczność
	 */
	AVAILABLE_FLEXIBILITY(true),
	/***
	 * Procent zasobów prekwalifikowanych z powodzeniem
	 */
	PERCENTAGE_OF_SUCCESSFULLY_PREQUALIFIED_DERS(false),
	/***
	 * Liczba certyfikowanych zasobów posiadających potencjał elastyczności
	 */
	NUMBER_OF_DERS_WITH_AT_LEAST_ONE_FP(false),
	/***
	 * Wolumen prekwalifikowanych zasobów posiadających potencjał elastyczności
	 */
	CAPACITY_OF_CERTIFIED_DERS_WITH_AT_LEAST_ONE_FP(false),
	/***
	 * Wolumen usług bilansujących na rezerwy w górę/Volume of balancing service offers for UP reserves
	 */
	VOLUME_OF_BALANCING_SERVICE_OFFERS_UP_RESERVES(true),
	/***
	 * Wolumen usług bilansujących na rezerwy w górę przekazane do RB/Volume of balancing service offers for UP reserves transferred to BM
	 */
	VOLUME_OF_BALANCING_SERVICE_OFFERS_UP_RESERVES_TRANSFERRED_TO_BM(true),
	/***
	 * Wolumen usług bilansujących na rezerwy w dół/Volume of balancing service offers for DOWN reserves
	 */
	VOLUME_OF_BALANCING_SERVICE_OFFERS_DOWN_RESERVES(true),
	/***
	 * Wolumen usług bilansujących na rezerwy w dół przekazane do RB/Volume of balancing service offers for DOWN reserves transferred to BM
	 */
	VOLUME_OF_BALANCING_SERVICE_OFFERS_DOWN_RESERVES_TRANSFERRED_TO_BM(true),
	/***
	 * Wolumen ofert na energię bilansującą/Volume of balancing energy offers
	 */
	VOLUME_OF_BALANCING_ENERGY_OFFERS(true),
	/***
	 * Wolumen ofert na energię bilansującą przesłaną do RB/Volume of balancing energy offers transferred to BM
	 */
	VOLUME_OF_BALANCING_ENERGY_OFFERS_TRANSFERRED_TO_BM(true),
	/***
	 * Oczekiwana elastyczność (moc)/Requested flexibility (power)
	 */
	REQUEST_FLEXIBILITY_POWER(true),
	/***
	 * Potencjał oferowany przez FSP vs potencjał oczekiwany przez OSD/Flex volume offered by FSP vs Flex request by DSO
	 */
	FLEX_VOLUME_OFFERED_VS_FLEX_REQUESTED_BY_DSO(true),
	/***
	 * Liczba unikniętych ograniczeń technicznych/Number of avoided technical restrictions
	 */
	NUMBER_AVOIDED_TECHNICAL_RESTRICTIONS(false),
	/***
	 * Liczba unikniętych ograniczeń technicznych(przeciążenie napięciowe)/Number of avoided technical restrictions(voltage violations)
	 */
	NUMBER_AVOIDED_TECHNICAL_RESTRICTIONS_VOL_VIOLATIONS(false),
	/***
	 * Liczba zasobów dostępnych dla BSP/Number of DERs available for BSPs
	 */
	NUMBER_OF_DER_AVAILABLE_FOR_BSP(false),
	/***
	 * Procent zasobów dostępnych do usług bilansujących/Percentage of resources available for balancing services
	 */
	PERCENTAGE_RESOURCES_AVAILABLE_FOR_BALANCING_SERVICES(false),
	/***
	 * Zsumowany wolumen zasobów dostępnych dla BSP/Total capacity of DER available for BSP
	 */
	TOTAL_CAPACITY_OF_DERS_AVAILABLE_FOR_BSP(false),
	/***
	 * Średni czas trwania obliczeń algorytmu AGNO/Average runtime of aggregated network offer algorithm
	 */
	AVERAGE_RUNTIME_AGNO_ALGORITHM(false),

	/***
	 * Odchylenie w aktywacji mocy
	 */
	POWER_EXCHANGE_DEVIATION(true),

	/***
	 * Odchylenie w aktywacji energii
	 */
	ENERGY_EXCHANGE_DEVIATION(true);

	private final boolean dateFilter;

	public boolean isDateFilter() {
		return dateFilter;
	}

	KpiType(boolean dateFilter) {
		this.dateFilter = dateFilter;
	}

	public static List<KpiTypeDTO> getTypes() {
		return Arrays.stream(KpiType.values())
						 .map(k -> new KpiTypeDTO(k.name(), k.dateFilter))
						 .sorted(Comparator.comparing(KpiTypeDTO::getName))
						 .collect(Collectors.toUnmodifiableList());
	}


	@Getter
	@Setter
	@AllArgsConstructor
	public static class KpiTypeDTO {
		private final String name;
		private final boolean filterDate;
	}
}
