package pl.com.tt.flex.server.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InstantFormatUtil {

	public static final DateTimeFormatter DD_MM_YYYY_HH_MM_SS = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

	public static String format(Instant instant, DateTimeFormatter formatter) {
		LocalDateTime date = LocalDateTime.ofInstant(instant, TimeZone.getDefault().toZoneId());
		return formatter.format(date);
	}
}
