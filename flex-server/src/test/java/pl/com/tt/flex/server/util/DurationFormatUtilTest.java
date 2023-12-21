package pl.com.tt.flex.server.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DurationFormatUtilTest {


	@ParameterizedTest
	@CsvSource(value = {
			"PT10S, 10s",
			"PT10M10S, 10m:10s",
			"PT1H15M10S, 1h:15m:10s",
			"PT1H10S, 1h:0m:10s",
			"PT1H, 1h:0m:0s"
	})
	void withHourMinutesAndSeconds(String strDuration, String expected) {
		//given
		Duration duration = Duration.parse(strDuration);
		//when
		String result = DurationFormatUtil.withHourMinutesAndSeconds(duration);
		//then
		assertEquals(expected, result);
	}
}
