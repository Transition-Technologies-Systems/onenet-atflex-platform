package pl.com.tt.flex.server.service.auction.da.mapper;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.server.domain.auction.da.AuctionDayAheadEntity;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.repository.product.ProductRepository;
import pl.com.tt.flex.server.service.product.dto.ProductNameMinDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AuctionDayAheadSeriesConverterTest {

    private static final Instant now = Instant.parse("2022-03-12T10:00:00Z");

    private static final Long PRODUCT_ID = 1L;
    private static final String PRODUCT_NAME = "PRODUCT_NAME";
    private static final String PRODUCT_SHORT_NAME = "PRODUCT_SHORT_NAME";

    private static final Long AUCTION_SERIES_ID = 1L;
    private static final Instant AUCTION_SERIES_FIRST_AUCTION_DAY = Instant.parse("2022-03-11T23:00:00Z");
    private static final Instant AUCTION_SERIES_LAST_AUCTION_DAY = Instant.parse("2022-03-15T23:00:00Z");
    private static final Instant AUCTION_SERIES_CAPACITY_GATE_OPENING_TIME = Instant.parse("2022-03-12T10:00:00Z");
    private static final Instant AUCTION_SERIES_CAPACITY_GATE_CLOSURE_TIME = Instant.parse("2022-03-12T14:00:00Z");
    private static final Instant AUCTION_SERIES_ENERGY_GATE_OPENING_TIME = Instant.parse("2022-03-12T16:00:00Z");
    private static final Instant AUCTION_SERIES_ENERGY_GATE_CLOSURE_TIME = Instant.parse("2022-03-12T18:00:00Z");
    private static final Instant AUCTION_SERIES_CAPACITY_AVAILABILITY_FROM = Instant.parse("2022-03-13T00:00:00Z");
    private static final Instant AUCTION_SERIES_CAPACITY_AVAILABILITY_TO = Instant.parse("2022-03-13T23:00:00Z");
    private static final Instant AUCTION_SERIES_ENERGY_AVAILABILITY_FROM = Instant.parse("2022-03-13T00:00:00Z");
    private static final Instant AUCTION_SERIES_ENERGY_AVAILABILITY_TO = Instant.parse("2022-03-13T23:00:00Z");
    private static final BigDecimal AUCTION_SERIES_MIN_DESIRED_CAPACITY = BigDecimal.valueOf(50);
    private static final BigDecimal AUCTION_SERIES_MAX_DESIRED_CAPACITY = BigDecimal.valueOf(100);
    private static final BigDecimal AUCTION_SERIES_MIN_DESIRED_ENERGY = BigDecimal.valueOf(20);
    private static final BigDecimal AUCTION_SERIES_MAX_DESIRED_ENERGY = BigDecimal.valueOf(70);

    private static final Instant AUCTION_DA_CAPACITY_GATE_OPENING_TIME = Instant.parse("2022-03-12T08:00:00Z");
    private static final Instant AUCTION_DA_CAPACITY_GATE_CLOSURE_TIME = Instant.parse("2022-03-12T11:00:00Z");
    private static final Instant AUCTION_DA_ENERGY_GATE_OPENING_TIME = Instant.parse("2022-03-12T12:00:00Z");
    private static final Instant AUCTION_DA_ENERGY_GATE_CLOSURE_TIME = Instant.parse("2022-03-12T13:00:00Z");
    private static final Instant AUCTION_DA_CAPACITY_AVAILABILITY_FROM = Instant.parse("2022-03-13T04:00:00Z");
    private static final Instant AUCTION_DA_CAPACITY_AVAILABILITY_TO = Instant.parse("2022-03-13T20:00:00Z");
    private static final Instant AUCTION_DA_ENERGY_AVAILABILITY_FROM = Instant.parse("2022-03-13T07:00:00Z");
    private static final Instant AUCTION_DA_ENERGY_AVAILABILITY_TO = Instant.parse("2022-03-13T20:00:00Z");
    private static final BigDecimal AUCTION_DA_MIN_DESIRED_CAPACITY = BigDecimal.valueOf(25);
    private static final BigDecimal AUCTION_DA_MAX_DESIRED_CAPACITY = BigDecimal.valueOf(70);
    private static final BigDecimal AUCTION_DA_MIN_DESIRED_ENERGY = BigDecimal.valueOf(30);
    private static final BigDecimal AUCTION_DA_MAX_DESIRED_ENERGY = BigDecimal.valueOf(40);

    private final ZoneId zoneId = ZoneId.of("Europe/Paris");
    private static final Instant AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_FIRST_AUCTION_DAY = Instant.parse("2022-03-25T23:00:00Z");
    private static final Instant AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_LAST_AUCTION_DAY = Instant.parse("2022-11-01T23:00:00Z");
    private static final Instant AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_CAPACITY_GATE_OPENING_TIME = Instant.parse("2022-03-25T02:00:00Z");
    private static final Instant AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_CAPACITY_GATE_CLOSURE_TIME = Instant.parse("2022-03-25T06:00:00Z");
    private static final Instant AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_ENERGY_GATE_OPENING_TIME = Instant.parse("2022-03-25T08:00:00Z");
    private static final Instant AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_ENERGY_GATE_CLOSURE_TIME = Instant.parse("2022-03-25T12:00:00Z");
    private static final Instant AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_ENERGY_AVAILABILITY_FROM = Instant.parse("2022-03-25T00:00:00Z");
    private static final Instant AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_ENERGY_AVAILABILITY_TO = Instant.parse("2022-03-26T00:00:00Z");
    private static final Instant AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_CAPACITY_AVAILABILITY_FROM = Instant.parse("2022-03-25T00:00:00Z");
    private static final Instant AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_CAPACITY_AVAILABILITY_TO = Instant.parse("2022-03-26T00:00:00Z");
    private static final BigDecimal AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_MIN_DESIRED_CAPACITY = BigDecimal.valueOf(30);
    private static final BigDecimal AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_MAX_DESIRED_CAPACITY = BigDecimal.valueOf(40);
    private static final BigDecimal AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_MIN_DESIRED_ENERGY = BigDecimal.valueOf(30);
    private static final BigDecimal AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_MAX_DESIRED_ENERGY = BigDecimal.valueOf(40);

    @Mock
    private ProductRepository productRepository;

    private ProductEntity productEntity;
    private AuctionsSeriesEntity capacityAndEnergySeries;
    private AuctionsSeriesEntity energySeries;
    private AuctionsSeriesEntity seriesWithDaylightChanging;

    @BeforeEach
    public void setup() {
        productEntity = new ProductEntity();
        productEntity.setId(PRODUCT_ID);
        productEntity.setFullName(PRODUCT_NAME);
        productEntity.setShortName(PRODUCT_SHORT_NAME);

        capacityAndEnergySeries = new AuctionsSeriesEntity();
        capacityAndEnergySeries.setId(AUCTION_SERIES_ID);
        capacityAndEnergySeries.setType(AuctionDayAheadType.CAPACITY_AND_ENERGY);
        capacityAndEnergySeries.setFirstAuctionDate(AUCTION_SERIES_FIRST_AUCTION_DAY);
        capacityAndEnergySeries.setLastAuctionDate(AUCTION_SERIES_LAST_AUCTION_DAY);
        capacityAndEnergySeries.setCapacityGateOpeningTime(AUCTION_SERIES_CAPACITY_GATE_OPENING_TIME);
        capacityAndEnergySeries.setCapacityGateClosureTime(AUCTION_SERIES_CAPACITY_GATE_CLOSURE_TIME);
        capacityAndEnergySeries.setEnergyGateOpeningTime(AUCTION_SERIES_ENERGY_GATE_OPENING_TIME);
        capacityAndEnergySeries.setEnergyGateClosureTime(AUCTION_SERIES_ENERGY_GATE_CLOSURE_TIME);
        capacityAndEnergySeries.setCapacityAvailabilityFrom(AUCTION_SERIES_CAPACITY_AVAILABILITY_FROM);
        capacityAndEnergySeries.setCapacityAvailabilityTo(AUCTION_SERIES_CAPACITY_AVAILABILITY_TO);
        capacityAndEnergySeries.setEnergyAvailabilityFrom(AUCTION_SERIES_ENERGY_AVAILABILITY_FROM);
        capacityAndEnergySeries.setEnergyAvailabilityTo(AUCTION_SERIES_ENERGY_AVAILABILITY_TO);
        capacityAndEnergySeries.setMinDesiredCapacity(AUCTION_SERIES_MIN_DESIRED_CAPACITY);
        capacityAndEnergySeries.setMaxDesiredCapacity(AUCTION_SERIES_MAX_DESIRED_CAPACITY);
        capacityAndEnergySeries.setMinDesiredEnergy(AUCTION_SERIES_MIN_DESIRED_ENERGY);
        capacityAndEnergySeries.setMaxDesiredEnergy(AUCTION_SERIES_MAX_DESIRED_ENERGY);
        capacityAndEnergySeries.setProduct(productEntity);

        energySeries = new AuctionsSeriesEntity();
        energySeries.setId(AUCTION_SERIES_ID);
        energySeries.setType(AuctionDayAheadType.ENERGY);
        energySeries.setFirstAuctionDate(AUCTION_SERIES_FIRST_AUCTION_DAY);
        energySeries.setLastAuctionDate(AUCTION_SERIES_LAST_AUCTION_DAY);
        energySeries.setEnergyGateOpeningTime(AUCTION_SERIES_ENERGY_GATE_OPENING_TIME);
        energySeries.setEnergyGateClosureTime(AUCTION_SERIES_ENERGY_GATE_CLOSURE_TIME);
        energySeries.setEnergyAvailabilityFrom(AUCTION_SERIES_ENERGY_AVAILABILITY_FROM);
        energySeries.setEnergyAvailabilityTo(AUCTION_SERIES_ENERGY_AVAILABILITY_TO);
        energySeries.setMinDesiredEnergy(AUCTION_SERIES_MIN_DESIRED_ENERGY);
        energySeries.setMaxDesiredEnergy(AUCTION_SERIES_MAX_DESIRED_ENERGY);
        energySeries.setProduct(productEntity);

        seriesWithDaylightChanging = new AuctionsSeriesEntity();
        seriesWithDaylightChanging.setId(AUCTION_SERIES_ID);
        seriesWithDaylightChanging.setType(AuctionDayAheadType.CAPACITY_AND_ENERGY);
        seriesWithDaylightChanging.setFirstAuctionDate(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_FIRST_AUCTION_DAY);
        seriesWithDaylightChanging.setLastAuctionDate(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_LAST_AUCTION_DAY);
        seriesWithDaylightChanging.setCapacityGateOpeningTime(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_CAPACITY_GATE_OPENING_TIME);
        seriesWithDaylightChanging.setCapacityGateClosureTime(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_CAPACITY_GATE_CLOSURE_TIME);
        seriesWithDaylightChanging.setEnergyGateOpeningTime(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_ENERGY_GATE_OPENING_TIME);
        seriesWithDaylightChanging.setEnergyGateClosureTime(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_ENERGY_GATE_CLOSURE_TIME);
        seriesWithDaylightChanging.setCapacityAvailabilityFrom(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_CAPACITY_AVAILABILITY_FROM);
        seriesWithDaylightChanging.setCapacityAvailabilityTo(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_CAPACITY_AVAILABILITY_TO);
        seriesWithDaylightChanging.setEnergyAvailabilityFrom(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_ENERGY_AVAILABILITY_FROM);
        seriesWithDaylightChanging.setEnergyAvailabilityTo(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_ENERGY_AVAILABILITY_TO);
        seriesWithDaylightChanging.setMinDesiredEnergy(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_MIN_DESIRED_ENERGY);
        seriesWithDaylightChanging.setMaxDesiredEnergy(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_MAX_DESIRED_ENERGY);
        seriesWithDaylightChanging.setMinDesiredCapacity(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_MIN_DESIRED_CAPACITY);
        seriesWithDaylightChanging.setMaxDesiredCapacity(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_MAX_DESIRED_CAPACITY);
        seriesWithDaylightChanging.setProduct(productEntity);

        MockitoAnnotations.initMocks(this);
        when(productRepository.getProductShortName(PRODUCT_ID)).thenReturn(new ProductNameMinDTO(PRODUCT_ID, PRODUCT_SHORT_NAME));
    }

    @Test
    void shouldGenerateEnergyDAAuctionsFromSeries() {
        AuctionDayAheadSeriesConverter auctionDayAheadSeriesConverter = new AuctionDayAheadSeriesConverter(productRepository);
        int auctionNumber = 0;
        for (Instant date = energySeries.getFirstAuctionDate(); !date.isAfter(energySeries.getLastAuctionDate()); date = date.plus(1, ChronoUnit.DAYS)) {
            AuctionDayAheadEntity dayAhead = auctionDayAheadSeriesConverter.generateNextDayAhead(energySeries, auctionNumber);

            //Aukcja jest tylko na ENERGY, pola zwiazane z CAPACITY powinny miec null
            assertThat(dayAhead.getCapacityAvailabilityFrom()).isNull();
            assertThat(dayAhead.getCapacityAvailabilityTo()).isNull();
            assertThat(dayAhead.getCapacityGateOpeningTime()).isNull();
            assertThat(dayAhead.getCapacityGateClosureTime()).isNull();
            assertThat(dayAhead.getMaxDesiredCapacity()).isNull();
            assertThat(dayAhead.getMinDesiredCapacity()).isNull();

            assertThat(dayAhead.getAuctionSeriesId()).isEqualTo(AUCTION_SERIES_ID);
            assertThat(dayAhead.getMaxDesiredEnergy()).isEqualTo(AUCTION_SERIES_MAX_DESIRED_ENERGY);
            assertThat(dayAhead.getMinDesiredEnergy()).isEqualTo(AUCTION_SERIES_MIN_DESIRED_ENERGY);

            Instant auctionDate = energySeries.getFirstAuctionDate().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getDay()).isEqualTo(auctionDate);

            Instant auctionDeliveryDate = energySeries.getFirstAuctionDate().plus(auctionNumber + 1, ChronoUnit.DAYS);
            assertThat(dayAhead.getDeliveryDate()).isEqualTo(auctionDeliveryDate);

            Instant energyAvailabilityFrom = energySeries.getEnergyAvailabilityFrom().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getEnergyAvailabilityFrom()).isEqualTo(energyAvailabilityFrom);

            Instant energyAvailabilityTo = energySeries.getEnergyAvailabilityTo().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getEnergyAvailabilityTo()).isEqualTo(energyAvailabilityTo);

            Instant energyGateOpeningTime = energySeries.getEnergyGateOpeningTime().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getEnergyGateOpeningTime()).isEqualTo(energyGateOpeningTime);

            Instant energyGateClosureTime = energySeries.getEnergyGateClosureTime().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getEnergyGateClosureTime()).isEqualTo(energyGateClosureTime);

            auctionNumber = auctionNumber + 1;
        }
    }

    @Test
    void shouldGenerateCapacityAndEnergyDAAuctionsFromSeries() {
        AuctionDayAheadSeriesConverter auctionDayAheadSeriesConverter = new AuctionDayAheadSeriesConverter(productRepository);
        int auctionNumber = 0;
        for (Instant date = capacityAndEnergySeries.getFirstAuctionDate(); !date.isAfter(capacityAndEnergySeries.getLastAuctionDate()); date = date.plus(1, ChronoUnit.DAYS)) {
            AuctionDayAheadEntity dayAhead = auctionDayAheadSeriesConverter.generateNextDayAhead(capacityAndEnergySeries, auctionNumber);

            assertThat(dayAhead.getAuctionSeriesId()).isEqualTo(AUCTION_SERIES_ID);
            assertThat(dayAhead.getMaxDesiredCapacity()).isEqualTo(AUCTION_SERIES_MAX_DESIRED_CAPACITY);
            assertThat(dayAhead.getMinDesiredCapacity()).isEqualTo(AUCTION_SERIES_MIN_DESIRED_CAPACITY);
            assertThat(dayAhead.getMaxDesiredEnergy()).isEqualTo(AUCTION_SERIES_MAX_DESIRED_ENERGY);
            assertThat(dayAhead.getMinDesiredEnergy()).isEqualTo(AUCTION_SERIES_MIN_DESIRED_ENERGY);

            Instant auctionDate = capacityAndEnergySeries.getFirstAuctionDate().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getDay()).isEqualTo(auctionDate);

            Instant auctionDeliveryDate = capacityAndEnergySeries.getFirstAuctionDate().plus(auctionNumber + 1, ChronoUnit.DAYS);
            assertThat(dayAhead.getDeliveryDate()).isEqualTo(auctionDeliveryDate);

            Instant capacityAvailabilityFrom = capacityAndEnergySeries.getCapacityAvailabilityFrom().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getCapacityAvailabilityFrom()).isEqualTo(capacityAvailabilityFrom);

            Instant capacityAvailabilityTo = capacityAndEnergySeries.getCapacityAvailabilityTo().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getCapacityAvailabilityTo()).isEqualTo(capacityAvailabilityTo);

            Instant capacityGateOpeningTime = capacityAndEnergySeries.getCapacityGateOpeningTime().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getCapacityGateOpeningTime()).isEqualTo(capacityGateOpeningTime);

            Instant capacityGateClosureTime = capacityAndEnergySeries.getCapacityGateClosureTime().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getCapacityGateClosureTime()).isEqualTo(capacityGateClosureTime);

            Instant energyAvailabilityFrom = capacityAndEnergySeries.getEnergyAvailabilityFrom().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getEnergyAvailabilityFrom()).isEqualTo(energyAvailabilityFrom);

            Instant energyAvailabilityTo = capacityAndEnergySeries.getEnergyAvailabilityTo().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getEnergyAvailabilityTo()).isEqualTo(energyAvailabilityTo);

            Instant energyGateOpeningTime = capacityAndEnergySeries.getEnergyGateOpeningTime().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getEnergyGateOpeningTime()).isEqualTo(energyGateOpeningTime);

            Instant energyGateClosureTime = capacityAndEnergySeries.getEnergyGateClosureTime().plus(auctionNumber, ChronoUnit.DAYS);
            assertThat(dayAhead.getEnergyGateClosureTime()).isEqualTo(energyGateClosureTime);

            auctionNumber = auctionNumber + 1;
        }
    }

    @Test
    void shouldUpdatedCapacityAndEnergyDayAheadAuction() {
        AuctionDayAheadSeriesConverter auctionDayAheadSeriesConverter = new AuctionDayAheadSeriesConverter(productRepository);
        AuctionDayAheadEntity auctionDayAheadEntity = createAuctionDayAhead();
        auctionDayAheadSeriesConverter.updateDayAhead(auctionDayAheadEntity, capacityAndEnergySeries);
        int auctionNumber = 0;
        assertThat(auctionDayAheadEntity.getAuctionSeriesId()).isEqualTo(AUCTION_SERIES_ID);
        assertThat(auctionDayAheadEntity.getMaxDesiredCapacity()).isEqualTo(AUCTION_SERIES_MAX_DESIRED_CAPACITY);
        assertThat(auctionDayAheadEntity.getMinDesiredCapacity()).isEqualTo(AUCTION_SERIES_MIN_DESIRED_CAPACITY);
        assertThat(auctionDayAheadEntity.getMaxDesiredEnergy()).isEqualTo(AUCTION_SERIES_MAX_DESIRED_ENERGY);
        assertThat(auctionDayAheadEntity.getMinDesiredEnergy()).isEqualTo(AUCTION_SERIES_MIN_DESIRED_ENERGY);

        Instant auctionDate = capacityAndEnergySeries.getFirstAuctionDate().plus(auctionNumber, ChronoUnit.DAYS);
        assertThat(auctionDayAheadEntity.getDay()).isEqualTo(auctionDate);

        Instant auctionDeliveryDate = capacityAndEnergySeries.getFirstAuctionDate().plus(auctionNumber + 1, ChronoUnit.DAYS);
        assertThat(auctionDayAheadEntity.getDeliveryDate()).isEqualTo(auctionDeliveryDate);

        Instant capacityAvailabilityFrom = capacityAndEnergySeries.getCapacityAvailabilityFrom().plus(auctionNumber, ChronoUnit.DAYS);
        assertThat(auctionDayAheadEntity.getCapacityAvailabilityFrom()).isEqualTo(capacityAvailabilityFrom);

        Instant capacityAvailabilityTo = capacityAndEnergySeries.getCapacityAvailabilityTo().plus(auctionNumber, ChronoUnit.DAYS);
        assertThat(auctionDayAheadEntity.getCapacityAvailabilityTo()).isEqualTo(capacityAvailabilityTo);

        Instant capacityGateOpeningTime = capacityAndEnergySeries.getCapacityGateOpeningTime().plus(auctionNumber, ChronoUnit.DAYS);
        assertThat(auctionDayAheadEntity.getCapacityGateOpeningTime()).isEqualTo(capacityGateOpeningTime);

        Instant capacityGateClosureTime = capacityAndEnergySeries.getCapacityGateClosureTime().plus(auctionNumber, ChronoUnit.DAYS);
        assertThat(auctionDayAheadEntity.getCapacityGateClosureTime()).isEqualTo(capacityGateClosureTime);

        Instant energyAvailabilityFrom = capacityAndEnergySeries.getEnergyAvailabilityFrom().plus(auctionNumber, ChronoUnit.DAYS);
        assertThat(auctionDayAheadEntity.getEnergyAvailabilityFrom()).isEqualTo(energyAvailabilityFrom);

        Instant energyAvailabilityTo = capacityAndEnergySeries.getEnergyAvailabilityTo().plus(auctionNumber, ChronoUnit.DAYS);
        assertThat(auctionDayAheadEntity.getEnergyAvailabilityTo()).isEqualTo(energyAvailabilityTo);

        Instant energyGateOpeningTime = capacityAndEnergySeries.getEnergyGateOpeningTime().plus(auctionNumber, ChronoUnit.DAYS);
        assertThat(auctionDayAheadEntity.getEnergyGateOpeningTime()).isEqualTo(energyGateOpeningTime);

        Instant energyGateClosureTime = capacityAndEnergySeries.getEnergyGateClosureTime().plus(auctionNumber, ChronoUnit.DAYS);
        assertThat(auctionDayAheadEntity.getEnergyGateClosureTime()).isEqualTo(energyGateClosureTime);
    }

    @Test
    void shouldGenerateEnergyDAAuctionsWithDaylightChangingFromSeries() {
        AuctionDayAheadSeriesConverter auctionDayAheadSeriesConverter = new AuctionDayAheadSeriesConverter(productRepository);
        int auctionNumber = 0;
        for (Instant date = seriesWithDaylightChanging.getFirstAuctionDate(); !date.isAfter(seriesWithDaylightChanging.getLastAuctionDate()); date = date.plus(1, ChronoUnit.DAYS)) {
            AuctionDayAheadEntity dayAhead = auctionDayAheadSeriesConverter.generateNextDayAhead(seriesWithDaylightChanging, auctionNumber);

            assertThat(dayAhead.getAuctionSeriesId()).isEqualTo(AUCTION_SERIES_ID);
            assertThat(dayAhead.getMaxDesiredEnergy()).isEqualTo(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_MAX_DESIRED_ENERGY);
            assertThat(dayAhead.getMinDesiredEnergy()).isEqualTo(AUCTION_SERIES_WITH_DAYLIGHT_CHANGING_MIN_DESIRED_ENERGY);

            Instant auctionDate = getNextDateAtZone(auctionNumber, seriesWithDaylightChanging.getFirstAuctionDate());
            assertThat(dayAhead.getDay()).isEqualTo(auctionDate);

            Instant auctionDeliveryDate = getNextDateAtZone(auctionNumber + 1, seriesWithDaylightChanging.getFirstAuctionDate());
            assertThat(dayAhead.getDeliveryDate()).isEqualTo(auctionDeliveryDate);

            Instant capacityAvailabilityFrom = getNextDateAtZone(auctionNumber, seriesWithDaylightChanging.getCapacityAvailabilityFrom());
            assertThat(dayAhead.getCapacityAvailabilityFrom()).isEqualTo(capacityAvailabilityFrom);

            Instant capacityAvailabilityTo = getNextDateAtZone(auctionNumber, seriesWithDaylightChanging.getCapacityAvailabilityTo());
            assertThat(dayAhead.getCapacityAvailabilityTo()).isEqualTo(capacityAvailabilityTo);

            Instant capacityGateOpeningTime = getNextDateAtZone(auctionNumber, seriesWithDaylightChanging.getCapacityGateOpeningTime());
            assertThat(dayAhead.getCapacityGateOpeningTime()).isEqualTo(capacityGateOpeningTime);

            Instant capacityGateClosureTime = getNextDateAtZone(auctionNumber, seriesWithDaylightChanging.getCapacityGateClosureTime());
            assertThat(dayAhead.getCapacityGateClosureTime()).isEqualTo(capacityGateClosureTime);

            Instant energyAvailabilityFrom = getNextDateAtZone(auctionNumber, seriesWithDaylightChanging.getEnergyAvailabilityFrom());
            assertThat(dayAhead.getEnergyAvailabilityFrom()).isEqualTo(energyAvailabilityFrom);

            Instant energyAvailabilityTo = getNextDateAtZone(auctionNumber, seriesWithDaylightChanging.getEnergyAvailabilityTo());
            assertThat(dayAhead.getEnergyAvailabilityTo()).isEqualTo(energyAvailabilityTo);

            Instant energyGateOpeningTime = getNextDateAtZone(auctionNumber, seriesWithDaylightChanging.getEnergyGateOpeningTime());
            assertThat(dayAhead.getEnergyGateOpeningTime()).isEqualTo(energyGateOpeningTime);

            Instant energyGateClosureTime = getNextDateAtZone(auctionNumber, seriesWithDaylightChanging.getEnergyGateClosureTime());
            assertThat(dayAhead.getEnergyGateClosureTime()).isEqualTo(energyGateClosureTime);

            auctionNumber = auctionNumber + 1;
        }
    }

    private Instant getNextDateAtZone(int auctionNumber, Instant date) {
        ZonedDateTime zonedDateTime = date.atZone(zoneId);
        ZonedDateTime returnedDate = zonedDateTime.plus(auctionNumber, ChronoUnit.DAYS);
        return returnedDate.toInstant();
    }

    @NotNull
    private AuctionDayAheadEntity createAuctionDayAhead() {
        AuctionDayAheadEntity auctionDayAheadEntity = new AuctionDayAheadEntity();
        auctionDayAheadEntity.setId(1L);
        auctionDayAheadEntity.setDay(AUCTION_SERIES_FIRST_AUCTION_DAY);
        auctionDayAheadEntity.setDeliveryDate(AUCTION_SERIES_FIRST_AUCTION_DAY.plus(1, ChronoUnit.DAYS));
        auctionDayAheadEntity.setAuctionSeriesId(AUCTION_SERIES_ID);
        auctionDayAheadEntity.setType(AuctionDayAheadType.CAPACITY_AND_ENERGY);
        auctionDayAheadEntity.setCapacityGateOpeningTime(AUCTION_DA_CAPACITY_GATE_OPENING_TIME);
        auctionDayAheadEntity.setCapacityGateClosureTime(AUCTION_DA_CAPACITY_GATE_CLOSURE_TIME);
        auctionDayAheadEntity.setEnergyGateOpeningTime(AUCTION_DA_ENERGY_GATE_OPENING_TIME);
        auctionDayAheadEntity.setEnergyGateClosureTime(AUCTION_DA_ENERGY_GATE_CLOSURE_TIME);
        auctionDayAheadEntity.setCapacityAvailabilityFrom(AUCTION_DA_CAPACITY_AVAILABILITY_FROM);
        auctionDayAheadEntity.setCapacityAvailabilityTo(AUCTION_DA_CAPACITY_AVAILABILITY_TO);
        auctionDayAheadEntity.setEnergyAvailabilityFrom(AUCTION_DA_ENERGY_AVAILABILITY_FROM);
        auctionDayAheadEntity.setEnergyAvailabilityTo(AUCTION_DA_ENERGY_AVAILABILITY_TO);
        auctionDayAheadEntity.setMinDesiredCapacity(AUCTION_DA_MIN_DESIRED_CAPACITY);
        auctionDayAheadEntity.setMaxDesiredCapacity(AUCTION_DA_MAX_DESIRED_CAPACITY);
        auctionDayAheadEntity.setMinDesiredEnergy(AUCTION_DA_MIN_DESIRED_ENERGY);
        auctionDayAheadEntity.setMaxDesiredEnergy(AUCTION_DA_MAX_DESIRED_ENERGY);
        auctionDayAheadEntity.setProduct(productEntity);
        return auctionDayAheadEntity;
    }
}
