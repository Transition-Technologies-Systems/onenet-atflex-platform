package pl.com.tt.flex.server.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateFormatter {

    private static String DATE_WITH_UNDERSCORE = "dd_MM_yyyy";
    private static String DATE_WITH_SLASH = "dd/MM/yyyy";
    private static String DATE_WITH_DOT = "dd.MM.yyyy";


    public static String formatWithUnderscore(LocalDate localDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_WITH_UNDERSCORE);
        return localDate.format(dateTimeFormatter);
    }

    public static String formatWithSlash(LocalDate localDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_WITH_SLASH);
        return localDate.format(dateTimeFormatter);
    }

    public static String formatWithDot(LocalDate localDate) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DATE_WITH_DOT);
        return localDate.format(dateTimeFormatter);
    }
}
