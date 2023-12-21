package pl.com.tt.flex.server.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.zone.ZoneOffsetTransition;
import java.time.zone.ZoneRules;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.ZoneOffset.UTC;

public class DateUtil {

    private static final ZoneId ZONE_ID_DEFAULT = ZoneId.systemDefault();
    // Pattern matching: 12/05/2022 07:00 - 14:00
    public static final Pattern periodPattern = Pattern.compile("(\\d\\d.\\d\\d.\\d\\d\\d\\d) (\\d\\d:\\d\\d) - (\\d\\d:\\d\\d)");
    // Pattern matching date in format: 12/05/2022
    public static final Pattern datePattern = Pattern.compile("^(0?[1-9]|[12][0-9]|3[01])[\\/\\-](0?[1-9]|1[012])[\\/\\-]\\d{4}$");
    // Pattern matching hour in format (24H) 12:00
    public static final Pattern hourPattern = Pattern.compile("^([0-1]?[0-9]|2[0-4]):[0-5][0-9]$");
    // Sorted
    public static final List<String> sortedHourNumbers = Arrays.asList(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "2a");

    public static final String EXTRA_HOUR_CONSTANT = "2a";

    public static LocalDate toLocalDate(Instant instant) {
        return LocalDate.ofInstant(instant, ZONE_ID_DEFAULT);
    }

    public static LocalDate toLocalDate(Instant instant, boolean isToDate) {
        if (isToDate) {
            return LocalDate.ofInstant(instant, ZONE_ID_DEFAULT).minus(1, ChronoUnit.DAYS);
        }
        return LocalDate.ofInstant(instant, ZONE_ID_DEFAULT);
    }

    public static boolean isInstantBetween(Instant testDate, Instant startDate, Instant endDate, boolean checkInclusiveRange) {
        if (checkInclusiveRange) {
            return !(testDate.isBefore(startDate) || testDate.isAfter(endDate)); // testDate >= fromDate && testDate <= endDate
        }
        return testDate.isAfter(startDate) && testDate.isBefore(endDate); // testDate > fromDate && testDate < endDate
    }

    /**
     * W celu porownania tylko czas ustawiamy w obu datach ten sam dzien
     */
    public static boolean isTimeAfter(Instant testDate, Instant date) {
        testDate = testDate.atZone(UTC)
                           .withYear(date.atZone(UTC).getYear())
                           .withDayOfMonth(date.atZone(UTC).getDayOfMonth())
                           .withMonth(date.atZone(UTC).getMonthValue()).toInstant();
        return testDate.isAfter(date);
    }

    /**
     * W celu porownania tylko dnia (bez godziny) ustawiamy w obu datach tą samą godzine
     */
    public static boolean isDayAfter(Instant testDate, Instant date) {
        testDate = testDate.atZone(UTC).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant();
        date = date.atZone(UTC).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant();
        return testDate.isAfter(date);
    }

    /**
     * @param day LocalDate to get number of hours for
     * @return 24, except for day when there is summer/winter time change
     */
    public static int calculate(LocalDate day) {
        ZoneRules rules = ZONE_ID_DEFAULT.getRules();
        int year = day.getYear();

        if (getWinterToSummerTimeTransitionDay(rules, year).equals(day)) return 23;
        if (getSummerToWinterTimeTransitionDay(rules, year).equals(day)) return 25;
        return 24;
    }

    public static LocalDate getWinterToSummerTimeTransitionDay(ZoneRules rules, int year) {
        ZoneOffsetTransition nextTransition = rules.nextTransition(toInstant(LocalDate.of(year, 1, 1)));
        return nextTransition.getInstant().atZone(ZONE_ID_DEFAULT).toLocalDate();
    }

    public static LocalDate getSummerToWinterTimeTransitionDay(ZoneRules rules, int year) {
        ZoneOffsetTransition nextTransition = rules.nextTransition(toInstant(LocalDate.of(year, 7, 1)));
        return nextTransition.getInstant().atZone(ZONE_ID_DEFAULT).toLocalDate();
    }

    public static Instant toInstant(LocalDate tradingDay) {
        return tradingDay.atStartOfDay(ZONE_ID_DEFAULT).toInstant();
    }

    public static boolean isValidPeriodDate(String datePeriod) {
        Matcher matcher = periodPattern.matcher(datePeriod);
        if (matcher.matches()) {
            String date = matcher.group(1);
            String hourFrom = matcher.group(2);
            String hourTo = matcher.group(3);
            return datePattern.matcher(date).matches() && hourPattern.matcher(hourFrom).matches() && hourPattern.matcher(hourTo).matches();
        }
        return false;
    }

    //20.12.2021 08:00 - 16:00  --> 20.12.2021 08:00
    public static Instant getAcceptedDeliveryPeriodFrom(String datePeriod) {
        Matcher matcher = periodPattern.matcher(datePeriod);
        matcher.matches();
        String localDataStr = matcher.group(1);
        String localTimeStr = matcher.group(2);
        ZoneId zoneId = ZoneId.of("Europe/Paris");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(localDataStr, dateFormatter);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime localTime = LocalTime.parse(localTimeStr, timeFormatter);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        return ZonedDateTime.of(localDateTime, zoneId).toInstant();
    }

    //20.12.2021 08:00 - 16:00  --> 20.12.2021 16:00
    public static Instant getAcceptedDeliveryPeriodTo(String datePeriod) {

        Matcher matcher = periodPattern.matcher(datePeriod);
        matcher.matches();
        String localDataStr = matcher.group(1);
        String localTimeStr = matcher.group(3);
        ZoneId zoneId = ZoneId.of("Europe/Paris");

        // W eksportowanym pliku godziny zapisujemy w formacie 24H.
        // Gdy okres ma być do pólnocy, to ustawiamy godzine 24:00
        //np. okres od 2022.05.10 00:00 do 2022.05.11 00:00 to okres ustawiany w nastepujacy sposob -> 00:00 - 24:00
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate localDate = LocalDate.parse(localDataStr, dateFormatter);

        if (localTimeStr.equals("24:00")) {
            localDate = localDate.plusDays(1);
            localTimeStr = "00:00";
        }
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime localTime = LocalTime.parse(localTimeStr, timeFormatter);
        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        return ZonedDateTime.of(localDateTime, zoneId).toInstant();
    }

    public static Date trunc(Date date, ChronoUnit chronoUnit) {
        Instant instant = date.toInstant();
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        ZonedDateTime truncatedZonedDateTime = zonedDateTime.truncatedTo(chronoUnit);
        Instant truncatedInstant = truncatedZonedDateTime.toInstant();
        return Date.from(truncatedInstant);
    }

    public static List<String> getHourNumberList(Instant dateFrom, Instant dateTo) {
        ZonedDateTime dateTimeFrom = dateFrom.atZone(ZoneId.systemDefault());
        ZonedDateTime dateTimeTo = dateTo.atZone(ZoneId.systemDefault());

        List<Integer> hourList = Stream.iterate(dateTimeFrom, d -> d.plusHours(1))
                                       .limit(ChronoUnit.HOURS.between(dateTimeFrom, dateTimeTo)).map(ZonedDateTime::getHour).collect(Collectors.toList());
        List<String> strHoursList = new ArrayList<>();
        for (int i = 0; i < hourList.size(); i++) {
            if (hourList.size() > i + 1 && hourList.get(i).equals(2) && hourList.get(i + 1).equals(2)) {
                strHoursList.add(EXTRA_HOUR_CONSTANT);
            } else {
                int hourNumber = hourList.get(i) + 1;
                strHoursList.add(String.valueOf(hourNumber));
            }
        }
        return strHoursList;
    }

    public static Instant getDeliveryDateFromNow() {
        LocalDate deliveryLocalDate = LocalDate.now().plusDays(1);
        return deliveryLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
    }

    public static Date getDateFromString(String dateString) throws ParseException {
        String formatterStr = "dd" + dateString.charAt(2) + "MM" + dateString.charAt(5) + "yyyy";
        SimpleDateFormat dateFormat = new SimpleDateFormat(formatterStr);
        dateFormat.setLenient(false);
        return dateFormat.parse(dateString);
    }

    // 2023-05-09T22:00:00Z, 2023-05-10T22:00:00Z --> 10/05/2023 00:00 - 24:00
    public static String getSameDayDateRangeString(Instant from, Instant to) {
        String dateFromString = getDateStringOfPattern(from, "dd/MM/yyyy HH:mm");
        String dateToString = getDateStringOfPattern(to, "HH:mm").replace("00:00", "24:00");;
        return dateFromString.concat(" - ").concat(dateToString);
    }

    private static String getDateStringOfPattern(Instant date, String pattern) {
        return LocalDateTime.ofInstant(date, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern));
    }

    // "10/05/2023 00:00 - 24:00" --> 2023-05-09T22:00:00Z
    public static Instant getFromDate(String dateRange) {
        String[] parts = dateRange.split(" - ");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        LocalDateTime startDateTime = LocalDateTime.parse(parts[0], formatter);
        return startDateTime.toInstant(ZoneOffset.UTC).minus(Duration.ofHours(2));
    }

    // "10/05/2023 00:00 - 24:00" --> 2023-05-10T22:00:00Z
    public static Instant getToDate(String dateRange) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String endDateString = dateRange.substring(0, 10) + dateRange.substring(18);
        LocalDateTime endDateTime = LocalDateTime.parse(endDateString, formatter);
        if (dateRange.split(" - ")[1].equals("24:00")) {
            endDateTime = endDateTime.plusDays(1);
        }
        return endDateTime.toInstant(ZoneOffset.UTC).minus(Duration.ofHours(2));
    }

}
