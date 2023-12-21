package pl.com.tt.flex.server.web.rest;

import java.time.Instant;

import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

public class InstantTestUtil {

    private InstantTestUtil() {
    }

    public static Instant getFirstHourOfDay(Instant instant) {
        return instant.minus(1, DAYS)
            .atZone(UTC)
            .withHour(23)
            .withMinute(0)
            .withSecond(0)
            .truncatedTo(SECONDS)
            .toInstant();
    }


    public static Instant getInstantWithSpecifiedHourAndMinute(Instant instant, int hour, int minute) {
        return instant
            .atZone(UTC)
            .withHour(hour)
            .withMinute(minute)
            .withSecond(0)
            .truncatedTo(SECONDS)
            .toInstant();
    }
}
