package pl.com.tt.flex.server.service.auction.da.mapper;


import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.repository.product.ProductRepository;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;

import java.time.*;
import java.time.temporal.ChronoUnit;

import static java.time.ZoneOffset.UTC;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.util.AuctionDayAheadDataUtil.generateAuctionDayAheadName;

@Component
public class AuctionDayAheadSeriesConverter {

    private final ProductRepository productRepository;

    // Na podstawie tej strefy czasowej, uwzgledniana jest zmiana czasu.
    private final ZoneId zoneId = ZoneId.of("Europe/Paris");

    public AuctionDayAheadSeriesConverter(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Aukcje DayAhead sa generowane codziennie na podstawie aukcji Series
     * (w zakresie jej dat OpeningTime i ClosureTime).
     * <p>
     * DayAhead:
     * OpeningTime: n
     * ClosureTime: n
     * AvailabilityFrom: n + 1
     * AvailabilityTo: n + 1
     * n - dzien generowania aukcji DayAhead
     * <p>
     * Przykladowa konwersja (n = 22/01/29)
     * <p>
     * Auction series utworzona 22/01/28 z czasem dostawy 12h (Availability)
     * FirstAuctionDate:        22/01/28 23:00:00
     * LastAuctionDate:         22/02/07 23:00:00
     * OpeningTime:             22/01/29 00:15:00
     * ClosureTime:             22/01/29 22:45:00
     * AvailabilityFrom:        22/01/30 05:00:00
     * AvailabilityTo:          22/01/30 17:00:00
     * <p>
     * Generated DA auction
     * AuctionDate:             22/01/28 23:00:00
     * OpeningTime:             22/01/29 00:15:00
     * ClosureTime:             22/01/29 22:45:00
     * DeliveryDate:            22/01/29 23:00:00
     * AvailabilityFrom:        22/01/30 05:00:00
     * AvailabilityTo:          22/01/30 17:00:00
     * <p>
     * <p>
     * Auction series z czasem dostawy 24h (Availability)
     * FirstAuctionDate:        22/01/28 23:00:00
     * LastAuctionDate:         22/02/07 23:00:00
     * OpeningTime:             22/01/29 00:15:00
     * ClosureTime:             22/01/29 22:45:00
     * AvailabilityFrom:        22/01/28 23:00:00
     * AvailabilityTo:          22/01/29 23:00:00
     * <p>
     * Generated DA auction
     * AuctionDate:             22/01/28 23:00:00
     * OpeningTime:             22/01/29 00:15:00
     * ClosureTime:             22/01/29 22:15:00
     * AvailabilityFrom:        22/01/29 23:00:00
     * AvailabilityTo:          22/01/30 23:00:00
     */

    public AuctionDayAheadEntity generateNextDayAhead(AuctionsSeriesEntity series, int numOfCreatedSeriesDaAuctions) {
        AuctionDayAheadEntity dayAhead = new AuctionDayAheadEntity();
        dayAhead.setAuctionSeriesId(series.getId());
        dayAhead.setType(series.getType());
        dayAhead.setProduct(series.getProduct());
        dayAhead.setMinDesiredCapacity(series.getMinDesiredCapacity());
        dayAhead.setMaxDesiredCapacity(series.getMaxDesiredCapacity());
        dayAhead.setMinDesiredEnergy(series.getMinDesiredEnergy());
        dayAhead.setMaxDesiredEnergy(series.getMaxDesiredEnergy());

        dayAhead.setDay(specifyAuctionDay(series, numOfCreatedSeriesDaAuctions));
        dayAhead.setDeliveryDate(specifyDeliveryDate(series, numOfCreatedSeriesDaAuctions));
        dayAhead.setName(specifyAuctionName(series, numOfCreatedSeriesDaAuctions));


        dayAhead.setEnergyGateOpeningTime(nonNull(series.getEnergyGateOpeningTime()) ?
            specifyDateBasedOnTimeZone(series.getEnergyGateOpeningTime(), numOfCreatedSeriesDaAuctions) : null);
        dayAhead.setEnergyGateClosureTime(nonNull(series.getEnergyGateClosureTime()) ?
            specifyDateBasedOnTimeZone(series.getEnergyGateClosureTime(), numOfCreatedSeriesDaAuctions) : null);
        dayAhead.setCapacityGateOpeningTime(nonNull(series.getCapacityGateOpeningTime()) ?
            specifyDateBasedOnTimeZone(series.getCapacityGateOpeningTime(), numOfCreatedSeriesDaAuctions) : null);
        dayAhead.setCapacityGateClosureTime(nonNull(series.getCapacityGateClosureTime()) ?
            specifyDateBasedOnTimeZone(series.getCapacityGateClosureTime(), numOfCreatedSeriesDaAuctions) : null);
        dayAhead.setEnergyAvailabilityFrom(nonNull(series.getEnergyAvailabilityFrom()) ?
            specifyDateBasedOnTimeZone(series.getEnergyAvailabilityFrom(), numOfCreatedSeriesDaAuctions) : null);
        dayAhead.setEnergyAvailabilityTo(nonNull(series.getEnergyAvailabilityTo()) ?
            specifyDateBasedOnTimeZone(series.getEnergyAvailabilityTo(), numOfCreatedSeriesDaAuctions) : null);
        dayAhead.setCapacityAvailabilityFrom(nonNull(series.getCapacityAvailabilityFrom()) ?
            specifyDateBasedOnTimeZone(series.getCapacityAvailabilityFrom(), numOfCreatedSeriesDaAuctions) : null);
        dayAhead.setCapacityAvailabilityTo(nonNull(series.getCapacityAvailabilityTo()) ?
            specifyDateBasedOnTimeZone(series.getCapacityAvailabilityTo(), numOfCreatedSeriesDaAuctions) : null);
        return dayAhead;
    }

    private Instant specifyDateBasedOnTimeZone(Instant seriesDate, int numOfCreatedSeriesDaAuctions) {
        ZonedDateTime seriesDateAtZone = seriesDate.atZone(zoneId);
        ZonedDateTime afterPlusDays = seriesDateAtZone.plus(numOfCreatedSeriesDaAuctions, ChronoUnit.DAYS);
        return afterPlusDays.toInstant().atZone(UTC).toInstant();
    }

    private Instant specifyAuctionDay(AuctionsSeriesEntity series, int numOfCreatedSeriesDaAuctions) {
        return specifyDateBasedOnTimeZone(series.getFirstAuctionDate(), numOfCreatedSeriesDaAuctions);
    }

    private Instant specifyDeliveryDate(AuctionsSeriesEntity series, int numOfCreatedSeriesDaAuctions) {
        return specifyDateBasedOnTimeZone(series.getFirstAuctionDate(), numOfCreatedSeriesDaAuctions + 1);
    }

    private String specifyAuctionName(AuctionsSeriesEntity series, int numOfCreatedSeriesDaAuctions) {
        ProductNameMinDTO productShortName = productRepository.getProductShortName(series.getProduct().getId());
        return generateAuctionDayAheadName(productShortName.getName(), specifyDeliveryDate(series, numOfCreatedSeriesDaAuctions));
    }

    public void updateDayAhead(AuctionDayAheadEntity dayAhead, AuctionsSeriesEntity series) {
        int numberOfAuction = (int) ChronoUnit.DAYS.between(series.getFirstAuctionDate(), dayAhead.getDay());
        dayAhead.setMinDesiredEnergy(series.getMinDesiredEnergy());
        dayAhead.setMaxDesiredEnergy(series.getMaxDesiredEnergy());
        dayAhead.setMinDesiredCapacity(series.getMinDesiredCapacity());
        dayAhead.setMaxDesiredCapacity(series.getMaxDesiredCapacity());
        dayAhead.setEnergyGateOpeningTime(parseDayAheadDateWithSeriesTime(dayAhead.getEnergyGateOpeningTime(), series.getEnergyGateOpeningTime(), numberOfAuction));
        dayAhead.setEnergyGateClosureTime(parseDayAheadDateWithSeriesTime(dayAhead.getEnergyGateClosureTime(), series.getEnergyGateClosureTime(), numberOfAuction));
        dayAhead.setEnergyAvailabilityFrom(parseDayAheadDateWithSeriesTime(dayAhead.getEnergyAvailabilityFrom(), series.getEnergyAvailabilityFrom(), numberOfAuction));
        dayAhead.setEnergyAvailabilityTo(parseDayAheadDateWithSeriesTime(dayAhead.getEnergyAvailabilityTo(), series.getEnergyAvailabilityTo(), numberOfAuction));
        dayAhead.setCapacityGateOpeningTime(parseDayAheadDateWithSeriesTime(dayAhead.getCapacityGateOpeningTime(), series.getCapacityGateOpeningTime(), numberOfAuction));
        dayAhead.setCapacityGateClosureTime(parseDayAheadDateWithSeriesTime(dayAhead.getCapacityGateClosureTime(), series.getCapacityGateClosureTime(), numberOfAuction));
        dayAhead.setCapacityAvailabilityFrom(parseDayAheadDateWithSeriesTime(dayAhead.getCapacityAvailabilityFrom(), series.getCapacityAvailabilityFrom(), numberOfAuction));
        dayAhead.setCapacityAvailabilityTo(parseDayAheadDateWithSeriesTime(dayAhead.getCapacityAvailabilityTo(), series.getCapacityAvailabilityTo(), numberOfAuction));
    }

    private Instant parseDayAheadDateWithSeriesTime(Instant dayAheadDate, Instant seriesTime, int numberOfAuction) {
        if (isNull(dayAheadDate) || isNull(seriesTime))
            return null;
        return specifyDateBasedOnTimeZone(seriesTime, numberOfAuction);
    }
}
