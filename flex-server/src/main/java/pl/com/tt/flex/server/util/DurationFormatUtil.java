package pl.com.tt.flex.server.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DurationFormatUtil {

	/**
	 * Format duration to: HH:mm:ss -> 1h:20m:10s
	 */
	public static String withHourMinutesAndSeconds(Duration duration) {
		Duration durationTruncateToSeconds = duration.truncatedTo(ChronoUnit.SECONDS);
		int hoursPart = durationTruncateToSeconds.toHoursPart();
		int minutesPart = durationTruncateToSeconds.toMinutesPart();
		int secondsPart = durationTruncateToSeconds.toSecondsPart();

		StringBuilder stringBuilder = new StringBuilder();
		if (hoursPart != 0) {
			stringBuilder.append(hoursPart).append("h").append(":");
		}
		if (minutesPart != 0 || hoursPart != 0) {
			stringBuilder.append(minutesPart).append("m").append(":");
		}
		stringBuilder.append(secondsPart).append("s");
		return stringBuilder.toString();
	}
}
