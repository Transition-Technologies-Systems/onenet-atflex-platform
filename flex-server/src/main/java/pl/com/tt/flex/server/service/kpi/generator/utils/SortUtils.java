package pl.com.tt.flex.server.service.kpi.generator.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SortUtils {

    public static <V> LinkedHashMap<Pair<String, LocalDate>, V> sortMapWitStringLocalDatePair(Map<Pair<String, LocalDate>, V> map) {
        return map.entrySet()
                  .stream()
                  .sorted(Map.Entry.comparingByKey(Comparator.comparing((Pair<String, LocalDate> p) -> p.getLeft().toLowerCase(Locale.ROOT)).thenComparing(Pair::getRight)))
                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static <V> LinkedHashMap<String, V> sortMapWithString(Map<String, V> map) {
        return map.entrySet()
                  .stream()
                  .sorted(Map.Entry.comparingByKey(Comparator.comparing((String key) -> key.toLowerCase(Locale.ROOT))))
                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    public static <V> LinkedHashMap<LocalDate, V> sortMapWithLocalDate(Map<LocalDate, V> map) {
        return map.entrySet()
                  .stream()
                  .sorted(Map.Entry.comparingByKey())
                  .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }
}
