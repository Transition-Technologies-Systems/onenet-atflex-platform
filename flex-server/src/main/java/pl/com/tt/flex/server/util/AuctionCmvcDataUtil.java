package pl.com.tt.flex.server.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class AuctionCmvcDataUtil {

    public static String generateAuctionCmvcName(String productName, Instant auctionDeliveryDate, Long auctionsCount) {
        String auctionNameFormat = "CM/VC_%s_%s_%s";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault());
        String deliveryDate = dateTimeFormatter.format(auctionDeliveryDate);
        String suffix = generateSuffix(auctionsCount);
        return String.format(auctionNameFormat, productName, deliveryDate, suffix);
    }

    public static long calculateDaysBetweenTodayAndDayOfDelivery(Instant now, Instant acceptedDeliveryDateFrom) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());
        String nowDateStr = formatter.format(now);
        String acceptedDeliveryDateFromStr = formatter.format(acceptedDeliveryDateFrom);
        LocalDate nowDate = LocalDate.parse(nowDateStr, formatter);
        LocalDate deliveryBeginning = LocalDate.parse(acceptedDeliveryDateFromStr, formatter);
        return ChronoUnit.DAYS.between(nowDate, deliveryBeginning);
    }

    /**
     * Suffix to numer porządkowy aukcji cmvc, jest możliwość, że na daną date dostawy i produkt będzie więcej niż jedna aukcja,
     * dlateg0 nazwę generujemy z suffixem "_01, _02, _03, ..."
     * @param auctionsCount
     * @return
     */
    private static String generateSuffix(Long auctionsCount) {
        return String.format("%02d", auctionsCount + 1);
    }

}
