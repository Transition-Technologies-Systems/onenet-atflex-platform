package pl.com.tt.flex.server.service.auction.da;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionDayAheadDTO;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionsSeriesDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.unit.UnitDirectionOfDeviation;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.enumeration.CreationSource;
import pl.com.tt.flex.server.repository.auction.da.AuctionDayAheadRepository;
import pl.com.tt.flex.server.repository.product.ProductRepository;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.server.service.auction.da.series.AuctionsSeriesService;
import pl.com.tt.flex.server.service.product.mapper.ProductMapper;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Integration tests for {@link AuctionDayAheadService}.
 */
@SpringBootTest(classes = FlexserverApp.class)
@Transactional
class AuctionDayAheadServiceIT {

    private static final Instant now = Instant.parse("2022-03-12T10:00:00Z");

    private static final String USER_LOGIN = "johndoe";
    private static final String USER_LASTNAME = "doe";
    private static final String USER_FIRSTNAME = "john";
    private static final String USER_PASSWORD = "johndoe12";
    private static final String USER_EMAIL = "johndoe@localhost";

    private static final String ADMIN_LOGIN = "johndoe";
    private static final String ADMIN_LASTNAME = "doe";
    private static final String ADMIN_FIRSTNAME = "john";
    private static final String ADMIN_PASSWORD = "johndoe12";
    private static final String ADMIN_EMAIL = "johndoe@localhost";

    private static final String PRODUCT_NAME = "PRODUCT_NAME";
    private static final String PRODUCT_SHORT_NAME = "PRODUCT_SHORT_NAME";
    private static final ProductBidSizeUnit PRODUCT_BID_SIZE_UNIT = ProductBidSizeUnit.KWH;
    private static final BigDecimal PRODUCT_MAX_BID_SIZE = BigDecimal.valueOf(100);
    private static final BigDecimal PRODUCT_MIN_BID_SIZE = BigDecimal.valueOf(1000);
    private static final Instant PRODUCT_VALID_FROM = Instant.parse("2022-03-10T10:00:00Z");
    private static final Instant PRODUCT_VALID_TO = Instant.parse("2022-03-20T10:00:00Z");
    private static final int PRODUCT_MAX_REQUIRED_DELIVERY_DURATION = 100;
    private static final int PRODUCT_MAX_FULL_ACTIVATION_TIME = 100;

    private static final Instant FSP_VALID_FROM = Instant.parse("2022-02-11T23:00:00Z");

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

    @Autowired
    private AuctionsSeriesService auctionsSeriesService;

    @Autowired
    private AuctionDayAheadService auctionDayAheadService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AuctionsSeriesDTO auctionsSeriesDTO;

    @Mock
    private AuctionDayAheadRepository auctionDayAheadRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private AuditingHandler auditingHandler;

    @Mock
    private DateTimeProvider dateTimeProvider;

    @BeforeEach
    public void setup() {
        UserEntity userEntity = new UserEntity();
        userEntity.setLogin(USER_LOGIN);
        userEntity.setLastName(USER_LASTNAME);
        userEntity.setFirstName(USER_FIRSTNAME);
        userEntity.setEmail(USER_EMAIL);
        userEntity.setPassword(passwordEncoder.encode(USER_PASSWORD));
        userEntity.setActivated(true);
        userEntity.setCreationSource(CreationSource.SYSTEM);
        userEntity = userRepository.save(userEntity);

        UserEntity adminUser = new UserEntity();
        adminUser.setLogin(ADMIN_LOGIN);
        adminUser.setLastName(ADMIN_LASTNAME);
        adminUser.setFirstName(ADMIN_FIRSTNAME);
        userEntity.setEmail(ADMIN_EMAIL);
        adminUser.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        adminUser.setActivated(true);
        adminUser.setCreationSource(CreationSource.SYSTEM);
        adminUser = userRepository.save(userEntity);


        FspEntity fspEntity = new FspEntity();
        fspEntity.setOwner(userEntity);
        fspEntity.setValidFrom(FSP_VALID_FROM);
        fspEntity.setRole(Role.ROLE_BALANCING_SERVICE_PROVIDER);

        UnitEntity unitEntity = new UnitEntity();
        unitEntity.setSourcePower(BigDecimal.valueOf(10));
        unitEntity.setConnectionPower(BigDecimal.valueOf(10));
        unitEntity.setDirectionOfDeviation(UnitDirectionOfDeviation.BOTH);


        FlexPotentialEntity flexPotentialEntity = new FlexPotentialEntity();
        flexPotentialEntity.setFsp(fspEntity);
        flexPotentialEntity.setUnits(Collections.singleton(unitEntity));

        ProductEntity productEntity = new ProductEntity();
        productEntity.setActive(true);
        productEntity.setFlexPotentials(Collections.singleton(flexPotentialEntity));
        productEntity.setFullName(PRODUCT_NAME);
        productEntity.setShortName(PRODUCT_SHORT_NAME);
        productEntity.setBidSizeUnit(PRODUCT_BID_SIZE_UNIT);
        productEntity.setMaxBidSize(PRODUCT_MAX_BID_SIZE);
        productEntity.setMinBidSize(PRODUCT_MIN_BID_SIZE);
        productEntity.setValidFrom(PRODUCT_VALID_FROM);
        productEntity.setValidTo(PRODUCT_VALID_TO);
        productEntity.setMinRequiredDeliveryDuration(PRODUCT_MAX_REQUIRED_DELIVERY_DURATION);
        productEntity.setMaxFullActivationTime(PRODUCT_MAX_FULL_ACTIVATION_TIME);
        productEntity.setSsoUsers(Collections.singleton(adminUser));
        productEntity.setPsoUser(adminUser);
        productEntity.setDirection(Direction.UNDEFINED);
        productEntity = productRepository.saveAndFlush(productEntity);


        auctionsSeriesDTO = new AuctionsSeriesDTO();
        auctionsSeriesDTO.setType(AuctionDayAheadType.CAPACITY_AND_ENERGY);
        auctionsSeriesDTO.setFirstAuctionDate(AUCTION_SERIES_FIRST_AUCTION_DAY);
        auctionsSeriesDTO.setLastAuctionDate(AUCTION_SERIES_LAST_AUCTION_DAY);
        auctionsSeriesDTO.setCapacityGateOpeningTime(AUCTION_SERIES_CAPACITY_GATE_OPENING_TIME);
        auctionsSeriesDTO.setCapacityGateClosureTime(AUCTION_SERIES_CAPACITY_GATE_CLOSURE_TIME);
        auctionsSeriesDTO.setEnergyGateOpeningTime(AUCTION_SERIES_ENERGY_GATE_OPENING_TIME);
        auctionsSeriesDTO.setEnergyGateClosureTime(AUCTION_SERIES_ENERGY_GATE_CLOSURE_TIME);
        auctionsSeriesDTO.setCapacityAvailabilityFrom(AUCTION_SERIES_CAPACITY_AVAILABILITY_FROM);
        auctionsSeriesDTO.setCapacityAvailabilityTo(AUCTION_SERIES_CAPACITY_AVAILABILITY_TO);
        auctionsSeriesDTO.setEnergyAvailabilityFrom(AUCTION_SERIES_ENERGY_AVAILABILITY_FROM);
        auctionsSeriesDTO.setEnergyAvailabilityTo(AUCTION_SERIES_ENERGY_AVAILABILITY_TO);
        auctionsSeriesDTO.setProduct(productMapper.toMinDto(productEntity));

        when(dateTimeProvider.getNow()).thenReturn(Optional.of(now));
        auditingHandler.setDateTimeProvider(dateTimeProvider);
    }

    @Test
    void updateScheduledAuctions_shouldDeletedFirstTwoAuctions() {
        AuctionsSeriesDTO savedAuctions = auctionsSeriesService.save(auctionsSeriesDTO);
        List<AuctionDayAheadDTO> auctionsBeforeUpdate = auctionDayAheadService.findAll();
        AuctionsSeriesEntity updatedSeries = auctionsSeriesService.getMapper().toEntity(savedAuctions);
        Instant modifiedFirstAuctionDay = AUCTION_SERIES_FIRST_AUCTION_DAY.plus(2, ChronoUnit.DAYS);
        updatedSeries.setFirstAuctionDate(modifiedFirstAuctionDay);

        auctionDayAheadService.updateScheduledAuctions(updatedSeries);
        List<AuctionDayAheadDTO> auctionsDaAfterUpdate = auctionDayAheadService.findAll();

        assertThat(auctionsBeforeUpdate.size()).isGreaterThan(auctionsDaAfterUpdate.size());

        Duration duration = Duration.between(modifiedFirstAuctionDay, AUCTION_SERIES_LAST_AUCTION_DAY);
        long totalDayOfAuctionsAfterUpdate = duration.toDays() + 1;
        assertThat(auctionsDaAfterUpdate.size()).isEqualTo(totalDayOfAuctionsAfterUpdate);

        auctionsDaAfterUpdate.sort(Comparator.comparing(AuctionDayAheadDTO::getDay));
        assertThat(auctionsBeforeUpdate.get(0).getDay()).isEqualTo(AUCTION_SERIES_FIRST_AUCTION_DAY);
        assertThat(auctionsDaAfterUpdate.get(0).getDay()).isEqualTo(modifiedFirstAuctionDay);
    }

    @Test
    void updateScheduledAuctions_shouldAddFirstTwoAuctions() {
        AuctionsSeriesDTO savedAuctions = auctionsSeriesService.save(auctionsSeriesDTO);
        List<AuctionDayAheadDTO> auctionsBeforeUpdate = auctionDayAheadService.findAll();
        AuctionsSeriesEntity updatedSeries = auctionsSeriesService.getMapper().toEntity(savedAuctions);
        Instant modifiedFirstAuctionDay = AUCTION_SERIES_FIRST_AUCTION_DAY.minus(2, ChronoUnit.DAYS);
        updatedSeries.setFirstAuctionDate(modifiedFirstAuctionDay);

        auctionDayAheadService.updateScheduledAuctions(updatedSeries);
        List<AuctionDayAheadDTO> auctionsDaAfterUpdate = auctionDayAheadService.findAll();

        assertThat(auctionsBeforeUpdate.size()).isLessThan(auctionsDaAfterUpdate.size());

        Duration duration = Duration.between(modifiedFirstAuctionDay, AUCTION_SERIES_LAST_AUCTION_DAY);
        long totalDayOfAuctionsAfterUpdate = duration.toDays() + 1;
        assertThat(auctionsDaAfterUpdate.size()).isEqualTo(totalDayOfAuctionsAfterUpdate);

        auctionsDaAfterUpdate.sort(Comparator.comparing(AuctionDayAheadDTO::getDay));
        assertThat(auctionsBeforeUpdate.get(0).getDay()).isEqualTo(AUCTION_SERIES_FIRST_AUCTION_DAY);
        assertThat(auctionsDaAfterUpdate.get(0).getDay()).isEqualTo(modifiedFirstAuctionDay);
    }

    @Test
    void updateScheduledAuctions_shouldAddLastTwoAuctions() {
        AuctionsSeriesDTO savedAuctions = auctionsSeriesService.save(auctionsSeriesDTO);
        List<AuctionDayAheadDTO> auctionsBeforeUpdate = auctionDayAheadService.findAll();
        AuctionsSeriesEntity updatedSeries = auctionsSeriesService.getMapper().toEntity(savedAuctions);
        Instant modifiedLastAuctionDay = AUCTION_SERIES_LAST_AUCTION_DAY.plus(2, ChronoUnit.DAYS);
        updatedSeries.setLastAuctionDate(modifiedLastAuctionDay);

        auctionDayAheadService.updateScheduledAuctions(updatedSeries);
        List<AuctionDayAheadDTO> auctionsDaAfterUpdate = auctionDayAheadService.findAll();

        int numberOfAuctionsBeforeUpdate = auctionsBeforeUpdate.size();
        int numberOfAuctionsAfterUpdate = auctionsDaAfterUpdate.size();
        assertThat(numberOfAuctionsBeforeUpdate).isLessThan(numberOfAuctionsAfterUpdate);

        Duration duration = Duration.between(AUCTION_SERIES_FIRST_AUCTION_DAY, modifiedLastAuctionDay);
        long totalDayOfAuctionsAfterUpdate = duration.toDays() + 1;
        assertThat(numberOfAuctionsAfterUpdate).isEqualTo(totalDayOfAuctionsAfterUpdate);

        auctionsDaAfterUpdate.sort(Comparator.comparing(AuctionDayAheadDTO::getDay));
        assertThat(auctionsBeforeUpdate.get(numberOfAuctionsBeforeUpdate - 1).getDay()).isEqualTo(AUCTION_SERIES_LAST_AUCTION_DAY);
        assertThat(auctionsDaAfterUpdate.get(numberOfAuctionsAfterUpdate - 1).getDay()).isEqualTo(modifiedLastAuctionDay);
    }
}
