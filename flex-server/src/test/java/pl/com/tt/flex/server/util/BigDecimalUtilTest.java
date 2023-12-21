package pl.com.tt.flex.server.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BigDecimalUtilTest {

    @ParameterizedTest
    @MethodSource("generateData")
    void min(List<BigDecimal> bigDecimals, BigDecimal expected) {
        //when
        BigDecimal result = BigDecimalUtil.min(bigDecimals.toArray(new BigDecimal[0]));
        //then
        assertEquals(expected, result);
    }

    static Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of(List.of(BigDecimal.ONE), BigDecimal.ONE),
                Arguments.of(Arrays.asList(BigDecimal.valueOf(100), BigDecimal.valueOf(20), BigDecimal.valueOf(30)), BigDecimal.valueOf(20)),
                Arguments.of(Arrays.asList(null, null, BigDecimal.valueOf(20), BigDecimal.valueOf(30)), BigDecimal.valueOf(20))
        );
    }
}