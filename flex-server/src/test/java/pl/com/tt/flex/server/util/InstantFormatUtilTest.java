package pl.com.tt.flex.server.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InstantFormatUtilTest {

	@ParameterizedTest
	@CsvSource(value = {
			"2022-11-03T17:40:29.917853Z, 03/11/2022 18:40:29",
			"2022-11-20T23:21:59.917853Z, 21/11/2022 00:21:59",
			"2022-05-11T22:22:30.917853Z, 12/05/2022 00:22:30",
	})
	void format_DD_MM_YYYY_HH_MM_SS(String strInstant, String expected) {
		//given
		Instant instant = Instant.parse(strInstant);
		//when
		String result = InstantFormatUtil.format(instant, InstantFormatUtil.DD_MM_YYYY_HH_MM_SS);
		//then
		assertEquals(expected, result);
	}
}
