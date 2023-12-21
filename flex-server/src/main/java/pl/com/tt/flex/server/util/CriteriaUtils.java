package pl.com.tt.flex.server.util;

import io.github.jhipster.service.filter.InstantFilter;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class CriteriaUtils {

    public static InstantFilter setOnlyDayIfEqualsFilter(InstantFilter instantFilter) {
        if (instantFilter.getEquals() != null) {
            Instant date = instantFilter.getEquals();
            Instant datePlusDay = date.plusSeconds(60 * 60 * 24);
            instantFilter.setGreaterThanOrEqual(date);
            instantFilter.setLessThan(datePlusDay);
            instantFilter.setEquals(null);
        }
        return instantFilter;
    }

    /**
     * Ustawia data w filtrze LessThanOrEquals godzine 24:00 czasu polskiego dla podanego dnia
     * np z 2023-04-14T21:59:59 na 2023-04-14T22:00:00
     */
    public static InstantFilter setLastHourDayIfLessThanOrEqualsFilter(InstantFilter instantFilter) {
        if (instantFilter.getLessThanOrEqual() != null) {
            Instant date = instantFilter.getLessThanOrEqual();
            ZonedDateTime zonedDateTime = date.atZone(ZoneId.systemDefault());
            zonedDateTime = zonedDateTime.plusDays(1).withHour(0);
            Instant instant = zonedDateTime.toInstant();
            instantFilter.setLessThanOrEqual(instant);
        }
        return instantFilter;
    }
}
