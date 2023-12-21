package pl.com.tt.flex.server.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BigDecimalUtil {

    public static Collector<Map.Entry<Pair<String, LocalDate>, List<BigDecimal>>, ?, Map<Pair<String, LocalDate>, BigDecimal>> minBigDecimals() {
        return Collectors.toMap(Map.Entry::getKey, entry -> entry
            .getValue()
            .stream()
            .filter(Objects::nonNull)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO));
    }

    public static Collector<Map.Entry<Pair<String, LocalDate>, List<BigDecimal>>, ?, Map<Pair<String, LocalDate>, BigDecimal>> sumBigDecimals() {
        return Collectors.toMap(Map.Entry::getKey, entry -> entry
            .getValue()
            .stream()
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    public static BigDecimal min(@NotNull BigDecimal... values) {
        return Arrays.stream(values).filter(Objects::nonNull).reduce(BigDecimal::min).orElseThrow();
    }
}
