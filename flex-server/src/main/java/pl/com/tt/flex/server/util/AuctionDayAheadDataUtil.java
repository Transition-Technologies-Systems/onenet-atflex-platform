package pl.com.tt.flex.server.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.tuple.Pair;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionDayAheadOfferDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferBandDataDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.da.AuctionOfferDersDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionOfferType;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionDayAheadOfferEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferBandDataEntity;
import pl.com.tt.flex.server.domain.auction.offer.da.AuctionOfferDersEntity;

@Slf4j
public class AuctionDayAheadDataUtil {

    private AuctionDayAheadDataUtil() {
    }

    public static String generateAuctionDayAheadName(String productName, Instant auctionDay) {
        String auctionNameFormat = "DA_%s_%s";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault());
        Instant day = auctionDay.truncatedTo(ChronoUnit.DAYS);
        return String.format(auctionNameFormat, productName, dateTimeFormatter.format(day.plus(1, ChronoUnit.DAYS)));
    }

    public static String generateAuctionSeriesName(String productName, Instant dateOfCreation) {
        String auctionNameFormat = "DA_%s_%s";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault());
        Instant dayOfCreation = dateOfCreation.truncatedTo(ChronoUnit.DAYS);
        return String.format(auctionNameFormat, productName, dateTimeFormatter.format(dayOfCreation));
    }

    public static void calculateAndSetPrice(AuctionDayAheadOfferEntity auctionOfferEntity) {
        List<BigDecimal> prices = auctionOfferEntity.getUnits().stream()
            .map(AuctionOfferDersEntity::getBandData)
            .flatMap(List::stream)
            .filter(band -> Objects.nonNull(band.getAcceptedVolume()))
            .filter(band -> band.getAcceptedVolume().compareTo(BigDecimal.ZERO) > 0)
            .map(AuctionOfferBandDataEntity::getPrice)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        if(CollectionUtils.isEmpty(prices)) {
            auctionOfferEntity.setPrice(BigDecimal.ZERO);
            return;
        }
        MathContext mc = new MathContext(5);
        BigDecimal averagePrice = prices.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(prices.size()), mc);
        auctionOfferEntity.setPrice(averagePrice);
    }

    public static void calculateAndSetVolume(AuctionDayAheadOfferEntity auctionOfferEntity, AuctionStatus auctionStatus, AuctionDayAheadOfferEntity dbOffer) {

        if (auctionOfferEntity.getType().equals(AuctionOfferType.ENERGY)) {
            if (auctionStatus.equals(AuctionStatus.OPEN_ENERGY)) {
                auctionOfferEntity.setVolumeFrom(calculateEnergyVolumeFrom(auctionOfferEntity));
                auctionOfferEntity.setVolumeTo(calculateEnergyVolumeTo(auctionOfferEntity));
            } else {
                calculateVolumeForClosedAuctionOfferEntity(auctionOfferEntity, dbOffer);
            }
            auctionOfferEntity.setAcceptedVolumeFrom(calculateEnergyAcceptedVolumeFrom(auctionOfferEntity));
            auctionOfferEntity.setAcceptedVolumeTo(calculateEnergyAcceptedVolumeTo(auctionOfferEntity));
        }
        if (auctionOfferEntity.getType().equals(AuctionOfferType.CAPACITY)) {
            if (auctionStatus.equals(AuctionStatus.OPEN_CAPACITY)) {
                auctionOfferEntity.setVolumeFrom(calculateCapacityVolumeFrom(auctionOfferEntity));
                auctionOfferEntity.setVolumeTo(calculateCapacityVolumeTo(auctionOfferEntity));
            } else {
                calculateVolumeForClosedAuctionOfferEntity(auctionOfferEntity, dbOffer);
            }
            auctionOfferEntity.setAcceptedVolumeFrom(calculateCapacityAcceptedVolumeFrom(auctionOfferEntity));
            auctionOfferEntity.setAcceptedVolumeTo(calculateCapacityAcceptedVolumeTo(auctionOfferEntity));
        }
    }

    public static Range<Integer> getDeliveryPeriodRange(Instant deliveryPeriodFrom, Instant deliveryPeriodTo) {
        ZoneOffset offset = ZoneOffset.ofHours(2);
        OffsetDateTime offsetDateTimeFrom = deliveryPeriodFrom.atOffset(offset);
        int acceptedHourFrom = offsetDateTimeFrom.getHour();
        OffsetDateTime offsetDateTimeTo = deliveryPeriodTo.atOffset(offset);
        int acceptedHourTo = (int) (acceptedHourFrom + ChronoUnit.HOURS.between(offsetDateTimeFrom, offsetDateTimeTo));
        return Range.between(acceptedHourFrom, acceptedHourTo);
    }

    public static boolean deliveryPeriodContainsHour(Range<Integer> deliveryPeriod, String hour) {
        int parsedHour;
        if (hour.equals("2a")) {
            parsedHour = 2;
        } else {
            parsedHour = Integer.parseInt(hour);
        }
        return deliveryPeriod.contains(parsedHour);
    }

    public static void calculateAndSetGDFFactors(AuctionDayAheadOfferEntity auctionOfferEntity) {
        Map<Pair<String, String>, BigDecimal> bandSumAtTimestamp = new HashMap<>();
        for (var der : auctionOfferEntity.getUnits()) {
            for (var band : der.getBandData()) {
                var key = Pair.of(band.getBandNumber(), band.getHourNumber());
                BigDecimal amountToAdd = Optional.ofNullable(band.getAcceptedVolume()).orElse(BigDecimal.ZERO);
                if (bandSumAtTimestamp.containsKey(key)) {
                    var newSum = bandSumAtTimestamp.get(key).add(amountToAdd);
                    bandSumAtTimestamp.replace(key, newSum);
                } else {
                    bandSumAtTimestamp.put(key, amountToAdd);
                }
            }
        }
        for (var der : auctionOfferEntity.getUnits()) {
            for (var band : der.getBandData()) {
                var key = Pair.of(band.getBandNumber(), band.getHourNumber());
                BigDecimal amount = Optional.ofNullable(band.getAcceptedVolume()).orElse(BigDecimal.ZERO);
                band.setGdf(amount.toString().concat("/").concat(bandSumAtTimestamp.get(key).toString()));
            }
        }
    }

    public static void markBandAsEditedUpdateOfferStatus(AuctionOfferBandDataEntity band) {
        Set<AuctionStatus> closedDaStatusSet = Set.of(AuctionStatus.CLOSED_CAPACITY, AuctionStatus.CLOSED_ENERGY);
        band.markAsEdited();
        AuctionDayAheadOfferEntity offer = band.getOfferDer().getOffer();
        AuctionStatus auctionStatus = offer.getAuctionDayAhead().getStatus();
        if (closedDaStatusSet.contains(auctionStatus)) {
            offer.setStatus(AuctionOfferStatus.VOLUMES_VERIFIED);
            offer.setVerifiedVolumesPercent(calculateVerifiedVolumesPart(offer.getUnits()));
        }
    }

    public static void markBandAsEditedUpdateOfferStatus(AuctionDayAheadOfferDTO offer, AuctionOfferBandDataDTO band) {
        band.setEdited(true);
        offer.setStatus(AuctionOfferStatus.VOLUMES_VERIFIED);
        offer.setVerifiedVolumesPercent(calculateVerifiedVolumesPartFromDTO(offer.getDers()));
    }

    private static int calculateVerifiedVolumesPart(List<AuctionOfferDersEntity> offerDers) {
        int totalPairNumber = 0;
        int verifiedNumber = 0;
        for(AuctionOfferDersEntity offerDer : offerDers) {
            for(AuctionOfferBandDataEntity band : offerDer.getBandData()) {
                if(!band.getBandNumber().equals("0")) {
                    totalPairNumber++;
                    if (band.isEdited()) {
                        verifiedNumber++;
                    }
                }
            }
        }
        return 100 * verifiedNumber / totalPairNumber;
    }

    private static int calculateVerifiedVolumesPartFromDTO(List<AuctionOfferDersDTO> offerDers) {
        int totalPairNumber = 0;
        int verifiedNumber = 0;
        for(AuctionOfferDersDTO offerDer : offerDers) {
            for(AuctionOfferBandDataDTO band : offerDer.getBandData()) {
                if(band.getBandNumber() != 0) {
                    totalPairNumber++;
                    if (band.isEdited()) {
                        verifiedNumber++;
                    }
                }
            }
        }
        return 100 * verifiedNumber / totalPairNumber;
    }

    private static void calculateVolumeForClosedAuctionOfferEntity(AuctionDayAheadOfferEntity auctionOfferEntity, AuctionDayAheadOfferEntity dbOffer) {
        if (Objects.nonNull(dbOffer)) {
            auctionOfferEntity.setVolumeFrom(dbOffer.getVolumeFrom());
            auctionOfferEntity.setVolumeTo(dbOffer.getVolumeTo());
        } else {
            if (auctionOfferEntity.getType().equals(AuctionOfferType.CAPACITY)) {
                auctionOfferEntity.setVolumeFrom(calculateCapacityVolumeFrom(auctionOfferEntity));
                auctionOfferEntity.setVolumeTo(calculateCapacityVolumeTo(auctionOfferEntity));
            } else if (auctionOfferEntity.getType().equals(AuctionOfferType.ENERGY)) {
                auctionOfferEntity.setVolumeFrom(calculateEnergyVolumeFrom(auctionOfferEntity));
                auctionOfferEntity.setVolumeTo(calculateEnergyVolumeTo(auctionOfferEntity));
            }
        }
    }

    private static BigDecimal calculateEnergyVolumeFrom(AuctionDayAheadOfferEntity auctionOfferEntity) {
        Supplier<Stream<AuctionOfferBandDataEntity>> bandData = () -> auctionOfferEntity.getUnits().stream()
            .map(AuctionOfferDersEntity::getBandData)
            .flatMap(List::stream);
        var negativeBandSum = (Double) bandData.get()
            .filter(data -> data.getBandNumber().charAt(0) == '-').mapToDouble(band -> getValueOrZero(band.getVolume())).sum();
        //Dla pasm ujemnych zwracamy sumę z minusem
        return BigDecimal.valueOf(negativeBandSum * (-1));
    }

    private static BigDecimal calculateEnergyVolumeTo(AuctionDayAheadOfferEntity auctionOfferEntity) {
        Supplier<Stream<AuctionOfferBandDataEntity>> bandData = () -> auctionOfferEntity.getUnits().stream()
            .map(AuctionOfferDersEntity::getBandData)
            .flatMap(List::stream);
        var positiveBandSum = bandData.get()
            .filter(data -> !Set.of('-', '0').contains(data.getBandNumber().charAt(0))).mapToDouble(band -> getValueOrZero(band.getVolume())).sum();
        return BigDecimal.valueOf(positiveBandSum);
    }

    private static BigDecimal calculateEnergyAcceptedVolumeFrom(AuctionDayAheadOfferEntity auctionOfferEntity) {
        Supplier<Stream<AuctionOfferBandDataEntity>> bandData = () -> auctionOfferEntity.getUnits().stream()
            .map(AuctionOfferDersEntity::getBandData)
            .flatMap(List::stream);
        var negativeBandSum = bandData.get()
            .filter(data -> data.getBandNumber().charAt(0) == '-')
            .filter(data -> Objects.nonNull(data.getAcceptedVolume())).mapToDouble(band -> getValueOrZero(band.getAcceptedVolume())).sum();
        //Dla pasm ujemnych zwracamy sumę z minusem
        return BigDecimal.valueOf(negativeBandSum * (-1));
    }

    private static BigDecimal calculateEnergyAcceptedVolumeTo(AuctionDayAheadOfferEntity auctionOfferEntity) {
        Supplier<Stream<AuctionOfferBandDataEntity>> bandData = () -> auctionOfferEntity.getUnits().stream()
            .map(AuctionOfferDersEntity::getBandData)
            .flatMap(List::stream);
        var positiveBandSum = bandData.get()
            .filter(data -> !Set.of('-', '0').contains(data.getBandNumber().charAt(0)))
            .filter(data -> Objects.nonNull(data.getAcceptedVolume())).mapToDouble(band -> getValueOrZero(band.getAcceptedVolume())).sum();
        return BigDecimal.valueOf(positiveBandSum);
    }

    private static BigDecimal calculateCapacityVolumeFrom(AuctionDayAheadOfferEntity auctionOfferEntity) {
        var direction = auctionOfferEntity.getAuctionDayAhead().getProduct().getDirection();
        Supplier<Stream<AuctionOfferBandDataEntity>> bandData = () -> auctionOfferEntity.getUnits().stream()
            .map(AuctionOfferDersEntity::getBandData)
            .flatMap(List::stream);
        if (direction.equals(Direction.UP)) {
            return BigDecimal.ZERO;
        }
        if (direction.equals(Direction.DOWN)) {
            var negativeBandSum = bandData.get()
                .filter(data -> data.getBandNumber().charAt(0) == '-').mapToDouble(band -> getValueOrZero(band.getVolume())).sum();
            //Dla pasm ujemnych zwracamy sumę z minusem
            return BigDecimal.valueOf(negativeBandSum * (-1));
        }
        return null;
    }

    private static BigDecimal calculateCapacityVolumeTo(AuctionDayAheadOfferEntity auctionOfferEntity) {
        var direction = auctionOfferEntity.getAuctionDayAhead().getProduct().getDirection();
        Supplier<Stream<AuctionOfferBandDataEntity>> bandData = () -> auctionOfferEntity.getUnits().stream()
            .map(AuctionOfferDersEntity::getBandData)
            .flatMap(List::stream);
        if (direction.equals(Direction.UP)) {
            var positiveBandSum = bandData.get()
                .filter(data -> !Set.of('-', '0').contains(data.getBandNumber().charAt(0))).mapToDouble(band -> getValueOrZero(band.getVolume())).sum();
            return BigDecimal.valueOf(positiveBandSum);
        }
        if (direction.equals(Direction.DOWN)) {
            return BigDecimal.ZERO;
        }
        return null;
    }

    private static BigDecimal calculateCapacityAcceptedVolumeFrom(AuctionDayAheadOfferEntity auctionOfferEntity) {
        var direction = auctionOfferEntity.getAuctionDayAhead().getProduct().getDirection();
        Supplier<Stream<AuctionOfferBandDataEntity>> bandData = () -> auctionOfferEntity.getUnits().stream()
            .map(AuctionOfferDersEntity::getBandData)
            .flatMap(List::stream);
        if (direction.equals(Direction.UP)) {
            return BigDecimal.ZERO;
        }
        if (direction.equals(Direction.DOWN)) {
            var negativeBandSum = bandData.get()
                .filter(data -> data.getBandNumber().charAt(0) == '-').mapToDouble(band -> getValueOrZero(band.getAcceptedVolume())).sum();
            //Dla pasm ujemnych zwracamy sumę z minusem
            return BigDecimal.valueOf(negativeBandSum * (-1));
        }
        return null;
    }

    private static BigDecimal calculateCapacityAcceptedVolumeTo(AuctionDayAheadOfferEntity auctionOfferEntity) {
        var direction = auctionOfferEntity.getAuctionDayAhead().getProduct().getDirection();
        Supplier<Stream<AuctionOfferBandDataEntity>> bandData = () -> auctionOfferEntity.getUnits().stream()
            .map(AuctionOfferDersEntity::getBandData)
            .flatMap(List::stream);
        if (direction.equals(Direction.UP)) {
            var positiveBandSum = bandData.get()
                .filter(data -> !Set.of('-', '0').contains(data.getBandNumber().charAt(0))).mapToDouble(band -> getValueOrZero(band.getAcceptedVolume())).sum();
            return BigDecimal.valueOf(positiveBandSum);
        }
        if (direction.equals(Direction.DOWN)) {
            return BigDecimal.ZERO;
        }
        return null;
    }

    private static Double getValueOrZero(BigDecimal volume) {
        return Objects.isNull(volume) ? 0.0 : volume.doubleValue();
    }

    // Sprawdza czy dla danego pasma w ofercie istnieje timestamp
    public static boolean existTimestampForBand(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp) {
        return offerDTO.getDers().stream()
            .flatMap(d -> d.getBandData().stream())
            .anyMatch(b -> b.getBandNumber() == bandNumber && b.getHourNumber()
                .equals(timestamp));
    }

    // Oblicza sume wolumenow oferty dla danego pasma i timestamp'a
    public static BigDecimal sumBandVolumeInTimestamp(AuctionDayAheadOfferDTO auctionDayAheadOfferDTO, int bandNumber, String timestamp) {
        return auctionDayAheadOfferDTO.getDers().stream()
            .flatMap(d -> d.getBandData().stream())
            .filter(d -> d.getBandNumber() == bandNumber)
            .filter(d -> d.getHourNumber().equals(timestamp))
            .map(AuctionOfferBandDataDTO::getVolume)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Oblicza sume zaakceptowanych wolumenow oferty dla danego pasma i timestamp'a
    public static BigDecimal sumBandAcceptedVolumeInTimestamp(AuctionDayAheadOfferDTO auctionDayAheadOfferDTO, int bandNumber, String timestamp) {
        return auctionDayAheadOfferDTO.getDers().stream()
            .flatMap(d -> d.getBandData().stream())
            .filter(d -> d.getBandNumber() == bandNumber)
            .filter(d -> d.getHourNumber().equals(timestamp))
            .map(AuctionOfferBandDataDTO::getAcceptedVolume)
            .filter(Objects::nonNull)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Oblicza średnią cene oferty dla danego pasma i timestamp'a
    public static BigDecimal averagePriceInTimestampAndBand(AuctionDayAheadOfferDTO offerDTO, int bandNumber, String timestamp) {
        List<BigDecimal> prices = offerDTO.getDers().stream()
            .flatMap(d -> d.getBandData().stream())
            .filter(b -> b.getHourNumber().equals(timestamp))
            .filter(b -> b.getBandNumber() == bandNumber)
            .map(AuctionOfferBandDataDTO::getPrice)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        MathContext mc = new MathContext(5);
        if (CollectionUtils.isEmpty(prices)) {
            return BigDecimal.ZERO;
        }
        return prices.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(prices.size()), mc);
    }

    // Oblicza średnią zaakceptowaną cene oferty dla danego pasma i timestamp'a
    public static BigDecimal averageAcceptedPriceInTimestampAndBand(AuctionDayAheadOfferDTO offerDTO, int bandNumber, String timestamp) {
        List<BigDecimal> prices = offerDTO.getDers().stream()
            .flatMap(d -> d.getBandData().stream())
            .filter(b -> b.getHourNumber().equals(timestamp))
            .filter(b -> b.getBandNumber() == bandNumber)
            .map(AuctionOfferBandDataDTO::getAcceptedPrice)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        MathContext mc = new MathContext(5);
        if (CollectionUtils.isEmpty(prices)) {
            return BigDecimal.ZERO;
        }
        return prices.stream().filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(prices.size()), mc);
    }

    /**
     * BandData dla danegj oferty w danym timestampie i pasmie
     */
    public static Stream<AuctionOfferBandDataDTO> getBandDataStreamForBandNumberAndTimestamp(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp) {
        return offerDTO.getDers().stream().flatMap(d -> d.getBandData().stream()).
            filter(b -> b.getBandNumber() == bandNumber)
            .filter(b -> b.getHourNumber().equals(timestamp));
    }

    /**
     * Metoda sprawdzająca czy w danym timestamie i pasmie wszystkie ceny są takie same
     */
    public static boolean areAllPricesTheSameInBandAndTimestamp(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp) {
        List<BigDecimal> pricesInTimestampAndBand = getBandDataStreamForBandNumberAndTimestamp(offerDTO, bandNumber, timestamp)
            .map(AuctionOfferBandDataDTO::getPrice)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return !CollectionUtils.isEmpty(pricesInTimestampAndBand) && pricesInTimestampAndBand.stream().allMatch(pricesInTimestampAndBand.get(0)::equals);
    }

    /**
     * Metoda sprawdzająca czy w danym timestamie i pasmie wszystkie zaakceptowane ceny są takie same
     */
    public static boolean areAllAcceptedPricesTheSameInBandAndTimestamp(AuctionDayAheadOfferDTO offerDTO, Integer bandNumber, String timestamp) {
        List<BigDecimal> pricesInTimestampAndBand = getBandDataStreamForBandNumberAndTimestamp(offerDTO, bandNumber, timestamp)
            .map(AuctionOfferBandDataDTO::getAcceptedPrice)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return !CollectionUtils.isEmpty(pricesInTimestampAndBand) && pricesInTimestampAndBand.stream().allMatch(pricesInTimestampAndBand.get(0)::equals);
    }
}
