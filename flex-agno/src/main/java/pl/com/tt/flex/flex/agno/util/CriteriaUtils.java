package pl.com.tt.flex.flex.agno.util;

import io.github.jhipster.service.filter.InstantFilter;

import java.time.Instant;

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
}
