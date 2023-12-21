package pl.com.tt.flex.server.service.kpi.generator.exchange.deviation;

import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import pl.com.tt.flex.server.service.kpi.generator.utils.SortUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class ExchangeDeviationData {
    private final Map<Pair<String, LocalDate>, List<DeviationData>> deviationByProductNameAndDeliveryDate;
    private final Map<String, List<DeviationData>> deviationGroupingByProductName;

    public ExchangeDeviationData(List<DeviationData> data) {

        this.deviationByProductNameAndDeliveryDate = getDeviationByProductNameAndDeliveryDate(data);
        this.deviationGroupingByProductName = getDeviationGroupingByProductName(data);
    }

    private Map<String, List<DeviationData>> getDeviationGroupingByProductName(List<DeviationData> deviationData) {
        return SortUtils.sortMapWithString(deviationData.stream().collect(Collectors.groupingBy(DeviationData::getProduct)));
    }

    private Map<Pair<String, LocalDate>, List<DeviationData>> getDeviationByProductNameAndDeliveryDate(List<DeviationData> deviationData) {
        Map<Pair<String, LocalDate>, List<DeviationData>> deviationByProductAndDate =
            deviationData.stream().collect(Collectors.groupingBy(data -> Pair.of(data.getProduct(), data.getDeliveryDate())));
        return SortUtils.sortMapWitStringLocalDatePair(deviationByProductAndDate);
    }
}
