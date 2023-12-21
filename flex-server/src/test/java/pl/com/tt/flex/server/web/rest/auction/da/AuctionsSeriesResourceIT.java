package pl.com.tt.flex.server.web.rest.auction.da;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.shaded.org.apache.commons.lang.RandomStringUtils;
import pl.com.tt.flex.model.service.dto.auction.da.AuctionsSeriesDTO;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.auction.da.AuctionsSeriesEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.repository.auction.da.AuctionsSeriesRepository;
import pl.com.tt.flex.server.service.auction.da.series.AuctionsSeriesQueryService;
import pl.com.tt.flex.server.service.auction.da.series.AuctionsSeriesService;
import pl.com.tt.flex.server.service.auction.da.series.mapper.AuctionsSeriesMapper;
import pl.com.tt.flex.server.util.AuctionDayAheadDataUtil;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.errors.ErrorConstants;
import pl.com.tt.flex.server.web.rest.product.ProductResourceIT;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType.CAPACITY;
import static pl.com.tt.flex.model.service.dto.auction.type.AuctionDayAheadType.ENERGY;
import static pl.com.tt.flex.server.web.rest.InstantTestUtil.getFirstHourOfDay;
import static pl.com.tt.flex.server.web.rest.InstantTestUtil.getInstantWithSpecifiedHourAndMinute;

/**
 * Integration tests for the {@link AuctionsSeriesResource} REST controller.
 */
@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = {FLEX_ADMIN_AUCTIONS_SERIES_VIEW, FLEX_ADMIN_AUCTIONS_SERIES_MANAGE, FLEX_ADMIN_AUCTIONS_SERIES_DELETE})
public class AuctionsSeriesResourceIT {

    private static final AuctionDayAheadType AUCTION_CAPACITY_TYPE = CAPACITY;
    private static final AuctionDayAheadType AUCTION_ENERGY_TYPE = ENERGY;

    private static final Instant DEFAULT_FIRST_AUCTION_DATE = getFirstHourOfDay(Instant.now().plus(1, DAYS));
    private static final Instant INCREASED_FIRST_AUCTION_DATE = getFirstHourOfDay(Instant.now().plus(2, DAYS));

    private static final Instant DEFAULT_LAST_AUCTION_DATE = getFirstHourOfDay(Instant.now().plus(10, DAYS));
    private static final Instant SHORTEN_LAST_AUCTION_DATE = getFirstHourOfDay(Instant.now().plus(12, DAYS));
    private static final Instant INCREASED_LAST_AUCTION_DATE = getFirstHourOfDay(Instant.now().plus(8, DAYS));

    private static final Instant DEFAULT_CAPACITY_GATE_OPENING_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(1, DAYS), 10, 0);
    private static final Instant UPDATED_CAPACITY_GATE_OPENING_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(1, DAYS), 12, 0);

    private static final Instant DEFAULT_CAPACITY_GATE_CLOSURE_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(1, DAYS), 14, 0);
    private static final Instant UPDATED_CAPACITY_GATE_CLOSURE_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(1, DAYS), 15, 0);

    private static final Instant DEFAULT_ENERGY_GATE_OPENING_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(1, DAYS), 16, 0);
    private static final Instant UPDATED_ENERGY_GATE_OPENING_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(1, DAYS), 17, 0);

    private static final Instant DEFAULT_ENERGY_GATE_CLOSURE_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(1, DAYS), 20, 0);
    private static final Instant UPDATED_ENERGY_GATE_CLOSURE_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(1, DAYS), 21, 0);

    private static final BigDecimal DEFAULT_MIN_DESIRED_CAPACITY = new BigDecimal(60);
    private static final BigDecimal UPDATED_MIN_DESIRED_CAPACITY = new BigDecimal(70);

    private static final BigDecimal DEFAULT_MAX_DESIRED_CAPACITY = new BigDecimal(90);
    private static final BigDecimal UPDATED_MAX_DESIRED_CAPACITY = new BigDecimal(80);

    private static final BigDecimal DEFAULT_MIN_DESIRED_ENERGY = new BigDecimal(50);
    private static final BigDecimal UPDATED_MIN_DESIRED_ENERGY = new BigDecimal(60);

    private static final BigDecimal DEFAULT_MAX_DESIRED_ENERGY = new BigDecimal(100);
    private static final BigDecimal UPDATED_MAX_DESIRED_ENERGY = new BigDecimal(90);

    private static final Instant DEFAULT_CAPACITY_AVAILABILITY_FROM = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(2, DAYS), 8, 0);
    private static final Instant UPDATED_CAPACITY_AVAILABILITY_FROM = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_CAPACITY_AVAILABILITY_TO = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(2, DAYS), 15, 0);
    private static final Instant UPDATED_CAPACITY_AVAILABILITY_TO = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_ENERGY_AVAILABILITY_FROM = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(2, DAYS), 8, 0);
    private static final Instant UPDATED_ENERGY_AVAILABILITY_FROM = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Instant DEFAULT_ENERGY_AVAILABILITY_TO = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(2, DAYS), 15, 0);
    private static final Instant UPDATED_ENERGY_AVAILABILITY_TO = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    @Autowired
    private AuctionsSeriesRepository auctionsSeriesRepository;

    @Autowired
    private AuctionsSeriesMapper auctionsSeriesMapper;

    @Autowired
    private AuctionsSeriesService auctionsSeriesService;

    @Autowired
    private AuctionsSeriesQueryService auctionsSeriesQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAuctionsSeriesMockMvc;

    private AuctionsSeriesEntity seriesCapacity;
    private AuctionsSeriesEntity seriesEnergy;
    private ProductEntity productWithUpDirectionEntity;
    private ProductEntity productWithUndefinedDirectionEntity;

    public static AuctionsSeriesEntity createSeriesCapacityAndEnergy(EntityManager em, ProductEntity productEntity) {
        AuctionsSeriesEntity seriesCapacity = new AuctionsSeriesEntity();
        seriesCapacity.setProduct(productEntity);
        seriesCapacity.setType(AUCTION_CAPACITY_TYPE);
        seriesCapacity.setName(AuctionDayAheadDataUtil.generateAuctionSeriesName(productEntity.getShortName(), Instant.now()));
        seriesCapacity.setCapacityGateOpeningTime(DEFAULT_CAPACITY_GATE_OPENING_TIME);
        seriesCapacity.setCapacityGateClosureTime(DEFAULT_CAPACITY_GATE_CLOSURE_TIME);
        seriesCapacity.setMinDesiredCapacity(DEFAULT_MIN_DESIRED_CAPACITY);
        seriesCapacity.setMaxDesiredCapacity(DEFAULT_MAX_DESIRED_CAPACITY);
        seriesCapacity.setCapacityAvailabilityFrom(DEFAULT_CAPACITY_AVAILABILITY_FROM);
        seriesCapacity.setCapacityAvailabilityTo(DEFAULT_CAPACITY_AVAILABILITY_TO);
        seriesCapacity.setFirstAuctionDate(DEFAULT_FIRST_AUCTION_DATE);
        seriesCapacity.setLastAuctionDate(DEFAULT_LAST_AUCTION_DATE);
        return seriesCapacity;
    }

    public static AuctionsSeriesEntity createSeriesEnergy(EntityManager em, ProductEntity productEntity) {
        AuctionsSeriesEntity auctionsSeriesEntity = new AuctionsSeriesEntity();
        auctionsSeriesEntity.setProduct(productEntity);
        auctionsSeriesEntity.setType(AUCTION_ENERGY_TYPE);
        auctionsSeriesEntity.setName(AuctionDayAheadDataUtil.generateAuctionSeriesName(productEntity.getShortName(), Instant.now()));
        auctionsSeriesEntity.setEnergyGateOpeningTime(DEFAULT_ENERGY_GATE_OPENING_TIME);
        auctionsSeriesEntity.setEnergyGateClosureTime(DEFAULT_ENERGY_GATE_CLOSURE_TIME);
        auctionsSeriesEntity.setMinDesiredEnergy(DEFAULT_MIN_DESIRED_ENERGY);
        auctionsSeriesEntity.setMaxDesiredEnergy(DEFAULT_MAX_DESIRED_ENERGY);
        auctionsSeriesEntity.setEnergyAvailabilityFrom(DEFAULT_ENERGY_AVAILABILITY_FROM);
        auctionsSeriesEntity.setEnergyAvailabilityTo(DEFAULT_ENERGY_AVAILABILITY_TO);
        auctionsSeriesEntity.setFirstAuctionDate(DEFAULT_FIRST_AUCTION_DATE);
        auctionsSeriesEntity.setLastAuctionDate(DEFAULT_LAST_AUCTION_DATE);
        return auctionsSeriesEntity;
    }

    private static ProductEntity createProductEntity(EntityManager em, Direction direction) {
        ProductEntity productEntity;
        List<ProductEntity> products = TestUtil.findAll(em, ProductEntity.class);
        if (products.isEmpty() || products.stream().noneMatch(p -> p.getDirection().equals(direction))) {
            productEntity = ProductResourceIT.createEntity(em);
            productEntity.setFullName("product-" + direction.name());
            productEntity.setShortName("product-" + direction.name());
            productEntity.setActive(true);
            productEntity.setValidFrom(Instant.now().minus(2, ChronoUnit.DAYS).truncatedTo(SECONDS));
            productEntity.setBalancing(true);
            productEntity.setDirection(direction);
            em.persist(productEntity);
            em.flush();
            em.detach(productEntity);
        } else {
            productEntity = products.stream().filter(p -> p.getDirection().equals(direction)).findFirst().get();
        }
        return productEntity;
    }

    @BeforeEach
    public void initTest() {
        productWithUpDirectionEntity = createProductEntity(em, Direction.UP);
        productWithUndefinedDirectionEntity = createProductEntity(em, Direction.UNDEFINED);
        seriesCapacity = createSeriesCapacityAndEnergy(em, productWithUpDirectionEntity);
        seriesEnergy = createSeriesEnergy(em, productWithUndefinedDirectionEntity);
    }

    @Test
    @Transactional
    public void createAuctionsSeriesEnergy() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesEnergy);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(status().isCreated());

        // Validate the AuctionsSeries in the database
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate + 1);
        AuctionsSeriesEntity testAuctionsSeries = auctionsSeriesList.get(auctionsSeriesList.size() - 1);
        assertThat(testAuctionsSeries.getProduct().getId()).isEqualTo(productWithUndefinedDirectionEntity.getId());
        assertThat(testAuctionsSeries.getType()).isEqualTo(AUCTION_ENERGY_TYPE);
        assertThat(testAuctionsSeries.getName()).isNotNull();
        assertThat(testAuctionsSeries.getEnergyGateOpeningTime()).isEqualTo(DEFAULT_ENERGY_GATE_OPENING_TIME);
        assertThat(testAuctionsSeries.getEnergyGateClosureTime()).isEqualTo(DEFAULT_ENERGY_GATE_CLOSURE_TIME);
        assertThat(testAuctionsSeries.getMinDesiredEnergy()).isEqualTo(DEFAULT_MIN_DESIRED_ENERGY);
        assertThat(testAuctionsSeries.getMaxDesiredEnergy()).isEqualTo(DEFAULT_MAX_DESIRED_ENERGY);
        assertThat(testAuctionsSeries.getEnergyAvailabilityFrom()).isEqualTo(DEFAULT_ENERGY_AVAILABILITY_FROM);
        assertThat(testAuctionsSeries.getEnergyAvailabilityTo()).isEqualTo(DEFAULT_ENERGY_AVAILABILITY_TO);
        assertThat(testAuctionsSeries.getFirstAuctionDate()).isEqualTo(DEFAULT_FIRST_AUCTION_DATE);
        assertThat(testAuctionsSeries.getLastAuctionDate()).isEqualTo(DEFAULT_LAST_AUCTION_DATE);
        assertThat(testAuctionsSeries.getCapacityGateOpeningTime()).isNull();
        assertThat(testAuctionsSeries.getCapacityGateClosureTime()).isNull();
        assertThat(testAuctionsSeries.getMinDesiredCapacity()).isNull();
        assertThat(testAuctionsSeries.getMaxDesiredCapacity()).isNull();
        assertThat(testAuctionsSeries.getCapacityAvailabilityFrom()).isNull();
        assertThat(testAuctionsSeries.getCapacityAvailabilityTo()).isNull();
    }

    @Test
    @Transactional
    public void createAuctionsSeriesCapacity() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesCapacity);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(status().isCreated());

        // Validate the AuctionsSeries in the database
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate + 1);
        AuctionsSeriesEntity testAuctionsSeries = auctionsSeriesList.get(auctionsSeriesList.size() - 1);
        assertThat(testAuctionsSeries.getProduct().getId()).isEqualTo(productWithUpDirectionEntity.getId());
        assertThat(testAuctionsSeries.getType()).isEqualTo(AUCTION_CAPACITY_TYPE);
        assertThat(testAuctionsSeries.getName()).isNotNull();
        assertThat(testAuctionsSeries.getCapacityGateOpeningTime()).isEqualTo(DEFAULT_CAPACITY_GATE_OPENING_TIME);
        assertThat(testAuctionsSeries.getCapacityGateClosureTime()).isEqualTo(DEFAULT_CAPACITY_GATE_CLOSURE_TIME);
        assertThat(testAuctionsSeries.getMinDesiredCapacity()).isEqualTo(DEFAULT_MIN_DESIRED_CAPACITY);
        assertThat(testAuctionsSeries.getMaxDesiredCapacity()).isEqualTo(DEFAULT_MAX_DESIRED_CAPACITY);
        assertThat(testAuctionsSeries.getCapacityAvailabilityFrom()).isEqualTo(DEFAULT_CAPACITY_AVAILABILITY_FROM);
        assertThat(testAuctionsSeries.getCapacityAvailabilityTo()).isEqualTo(DEFAULT_CAPACITY_AVAILABILITY_TO);
        assertThat(testAuctionsSeries.getFirstAuctionDate()).isEqualTo(DEFAULT_FIRST_AUCTION_DATE);
        assertThat(testAuctionsSeries.getLastAuctionDate()).isEqualTo(DEFAULT_LAST_AUCTION_DATE);
    }

    @Test
    @Transactional
    public void createAuctionsSeriesWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();

        // Create the AuctionsSeries with an existing ID
        seriesEnergy.setId(1L);
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesEnergy);

        // An entity with an existing ID cannot be created, so this API call must fail
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries in the database
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseModifyProduct() throws Exception {
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesEnergy);

        // Modify Product in AuctionSeries
        ProductMinDTO product = new ProductMinDTO();
        product.setId(Long.MAX_VALUE);
        auctionsSeriesDTO.setProduct(product);

        restAuctionsSeriesMockMvc.perform(put("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.CANNOT_MODIFY_PRODUCT_IN_AUCTION_SERIES))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseModifyAuctionType() throws Exception {
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesEnergy);

        // Modify Auction Type in AuctionSeries
        auctionsSeriesDTO.setType(AUCTION_CAPACITY_TYPE);

        restAuctionsSeriesMockMvc.perform(put("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.CANNOT_MODIFY_AUCTION_TYPE_IN_AUCTION_SERIES))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseWrongFirstAuctionDate() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesEnergy);
        auctionsSeriesDTO.setFirstAuctionDate(getFirstHourOfDay(Instant.now().minus(1, DAYS)));
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.FIRST_AUCTION_DATE_MAY_START_TOMORROW_AT_THE_EARLIEST))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseLastAuctionDateIsBeforeFirstAuctionDate() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesEnergy);
        auctionsSeriesDTO.setFirstAuctionDate(getFirstHourOfDay(Instant.now().plus(3, DAYS)));
        auctionsSeriesDTO.setLastAuctionDate(getFirstHourOfDay(Instant.now().plus(2, DAYS)));
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.LAST_AUCTION_DATE_IS_BEFORE_FIRST_AUCTION_DATE))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseEnergyMaxDesiredIsLowerThanEnergyMinDesired() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesEnergy);
        auctionsSeriesDTO.setMaxDesiredEnergy(productWithUndefinedDirectionEntity.getMinBidSize());
        auctionsSeriesDTO.setMinDesiredEnergy(productWithUndefinedDirectionEntity.getMaxBidSize());
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.ENERGY_MIN_DESIRED_IS_GREATER_THAN_ENERGY_MAX_DESIRED))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseCapacityMaxDesiredIsLowerThanCapacityMinDesired() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesCapacity);
        auctionsSeriesDTO.setMaxDesiredCapacity(productWithUpDirectionEntity.getMinBidSize());
        auctionsSeriesDTO.setMinDesiredCapacity(productWithUpDirectionEntity.getMaxBidSize());
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.CAPACITY_MIN_DESIRED_IS_GREATER_THAN_CAPACITY_MAX_DESIRED))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseWrongEnergyMaxDesired() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesEnergy);
        auctionsSeriesDTO.setMaxDesiredEnergy(productWithUndefinedDirectionEntity.getMaxBidSize().add(BigDecimal.valueOf(10)));
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.ENERGY_MAX_DESIRED_IS_GREATER_THAN_PRODUCT_MAX_BID_SIZE))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseWrongEnergyMinDesired() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesEnergy);
        auctionsSeriesDTO.setMinDesiredEnergy(productWithUndefinedDirectionEntity.getMinBidSize().add(BigDecimal.valueOf(-10)));
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.ENERGY_MIN_DESIRED_IS_LESS_THAN_PRODUCT_MIN_BID_SIZE))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseWrongCapacityMaxDesired() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesCapacity);
        auctionsSeriesDTO.setMaxDesiredCapacity(productWithUpDirectionEntity.getMaxBidSize().add(BigDecimal.valueOf(10)));
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.CAPACITY_MAX_DESIRED_IS_GREATER_THAN_PRODUCT_MAX_BID_SIZE))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseWrongCapacityMinDesired() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesCapacity);
        auctionsSeriesDTO.setMinDesiredCapacity(productWithUpDirectionEntity.getMinBidSize().add(BigDecimal.valueOf(-10)));
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.CAPACITY_MIN_DESIRED_IS_LESS_THAN_PRODUCT_MIN_BID_SIZE))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeriesEnergy_shouldNotCreatedBecauseRequiredFiledIsNotCompleted() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesEnergy);

        auctionsSeriesDTO.setEnergyGateClosureTime(null);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.REQUIRED_FIELDS_ARE_NOT_COMPLETED))
            .andExpect(status().isBadRequest());

        auctionsSeriesDTO.setEnergyGateClosureTime(null);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.REQUIRED_FIELDS_ARE_NOT_COMPLETED))
            .andExpect(status().isBadRequest());

        auctionsSeriesDTO.setEnergyAvailabilityFrom(null);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.REQUIRED_FIELDS_ARE_NOT_COMPLETED))
            .andExpect(status().isBadRequest());

        auctionsSeriesDTO.setEnergyAvailabilityTo(null);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.REQUIRED_FIELDS_ARE_NOT_COMPLETED))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeriesCapacity_shouldNotCreatedBecauseRequiredFiledIsNotCompleted() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesCapacity);

        auctionsSeriesDTO.setCapacityGateOpeningTime(null);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.REQUIRED_FIELDS_ARE_NOT_COMPLETED))
            .andExpect(status().isBadRequest());

        auctionsSeriesDTO.setCapacityGateClosureTime(null);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.REQUIRED_FIELDS_ARE_NOT_COMPLETED))
            .andExpect(status().isBadRequest());

        auctionsSeriesDTO.setCapacityAvailabilityFrom(null);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.REQUIRED_FIELDS_ARE_NOT_COMPLETED))
            .andExpect(status().isBadRequest());

        auctionsSeriesDTO.setCapacityAvailabilityTo(null);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.REQUIRED_FIELDS_ARE_NOT_COMPLETED))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseCapacityAvailabilityToIsBeforeCapacityAvailabilityFrom() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesCapacity);

        auctionsSeriesDTO.setCapacityAvailabilityFrom(DEFAULT_CAPACITY_AVAILABILITY_TO);
        auctionsSeriesDTO.setCapacityAvailabilityTo(DEFAULT_CAPACITY_AVAILABILITY_FROM);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.CAPACITY_AVAILABILITY_TO_IS_BEFORE_CAPACITY_AVAILABILITY_FROM))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createAuctionsSeries_shouldNotCreatedBecauseEnergyAvailabilityToIsBeforeEnergyAvailabilityFrom() throws Exception {
        int databaseSizeBeforeCreate = auctionsSeriesRepository.findAll().size();
        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesEnergy);

        auctionsSeriesDTO.setEnergyAvailabilityTo(DEFAULT_ENERGY_AVAILABILITY_FROM);
        auctionsSeriesDTO.setEnergyAvailabilityFrom(DEFAULT_ENERGY_AVAILABILITY_TO);
        restAuctionsSeriesMockMvc.perform(post("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(jsonPath("$.errorKey").value(ErrorConstants.ENERGY_AVAILABILITY_TO_IS_BEFORE_ENERGY_AVAILABILITY_FROM))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries is not created
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeries() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList
        restAuctionsSeriesMockMvc.perform(get("/api/admin/auctions-series?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(seriesCapacity.getId().intValue())))
            .andExpect(jsonPath("$.[*].product.id").value(hasItem(productWithUpDirectionEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].product.id").value(hasItem(productWithUndefinedDirectionEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(AUCTION_CAPACITY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(seriesCapacity.getName())))
            .andExpect(jsonPath("$.[*].energyGateOpeningTime").value(hasItem(DEFAULT_ENERGY_GATE_OPENING_TIME.toString())))
            .andExpect(jsonPath("$.[*].energyGateClosureTime").value(hasItem(DEFAULT_ENERGY_GATE_CLOSURE_TIME.toString())))
            .andExpect(jsonPath("$.[*].capacityGateOpeningTime").value(hasItem(DEFAULT_CAPACITY_GATE_OPENING_TIME.toString())))
            .andExpect(jsonPath("$.[*].capacityGateClosureTime").value(hasItem(DEFAULT_CAPACITY_GATE_CLOSURE_TIME.toString())))
            .andExpect(jsonPath("$.[*].minDesiredCapacity").value(hasItem(DEFAULT_MIN_DESIRED_CAPACITY.intValue())))
            .andExpect(jsonPath("$.[*].maxDesiredCapacity").value(hasItem(DEFAULT_MAX_DESIRED_CAPACITY.intValue())))
            .andExpect(jsonPath("$.[*].minDesiredEnergy").value(hasItem(DEFAULT_MIN_DESIRED_ENERGY.intValue())))
            .andExpect(jsonPath("$.[*].maxDesiredEnergy").value(hasItem(DEFAULT_MAX_DESIRED_ENERGY.intValue())))
            .andExpect(jsonPath("$.[*].capacityAvailabilityFrom").value(hasItem(DEFAULT_CAPACITY_AVAILABILITY_FROM.toString())))
            .andExpect(jsonPath("$.[*].capacityAvailabilityTo").value(hasItem(DEFAULT_CAPACITY_AVAILABILITY_TO.toString())))
            .andExpect(jsonPath("$.[*].energyAvailabilityFrom").value(hasItem(DEFAULT_ENERGY_AVAILABILITY_FROM.toString())))
            .andExpect(jsonPath("$.[*].energyAvailabilityTo").value(hasItem(DEFAULT_ENERGY_AVAILABILITY_TO.toString())))
            .andExpect(jsonPath("$.[*].firstAuctionDate").value(hasItem(DEFAULT_FIRST_AUCTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastAuctionDate").value(hasItem(DEFAULT_LAST_AUCTION_DATE.toString())));
    }

    @Test
    @Transactional
    public void getAuctionsSeriesCapacity() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get the auctionsSeries
        restAuctionsSeriesMockMvc.perform(get("/api/admin/auctions-series/{id}", seriesCapacity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(seriesCapacity.getId().intValue()))
            .andExpect(jsonPath("$.product.id").value(productWithUpDirectionEntity.getId().intValue()))
            .andExpect(jsonPath("$.type").value(AUCTION_CAPACITY_TYPE.toString()))
            .andExpect(jsonPath("$.name").value(seriesCapacity.getName()))
            .andExpect(jsonPath("$.capacityGateOpeningTime").value(DEFAULT_CAPACITY_GATE_OPENING_TIME.toString()))
            .andExpect(jsonPath("$.capacityGateClosureTime").value(DEFAULT_CAPACITY_GATE_CLOSURE_TIME.toString()))
            .andExpect(jsonPath("$.minDesiredCapacity").value(DEFAULT_MIN_DESIRED_CAPACITY.intValue()))
            .andExpect(jsonPath("$.maxDesiredCapacity").value(DEFAULT_MAX_DESIRED_CAPACITY.intValue()))
            .andExpect(jsonPath("$.capacityAvailabilityFrom").value(DEFAULT_CAPACITY_AVAILABILITY_FROM.toString()))
            .andExpect(jsonPath("$.capacityAvailabilityTo").value(DEFAULT_CAPACITY_AVAILABILITY_TO.toString()))
            .andExpect(jsonPath("$.firstAuctionDate").value(DEFAULT_FIRST_AUCTION_DATE.toString()))
            .andExpect(jsonPath("$.lastAuctionDate").value(DEFAULT_LAST_AUCTION_DATE.toString()));
    }

    @Test
    @Transactional
    public void getAuctionsSeriesEnergy() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get the auctionsSeries
        restAuctionsSeriesMockMvc.perform(get("/api/admin/auctions-series/{id}", seriesEnergy.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(seriesEnergy.getId().intValue()))
            .andExpect(jsonPath("$.product.id").value(productWithUndefinedDirectionEntity.getId().intValue()))
            .andExpect(jsonPath("$.type").value(AUCTION_ENERGY_TYPE.toString()))
            .andExpect(jsonPath("$.name").value(seriesEnergy.getName()))
            .andExpect(jsonPath("$.energyGateOpeningTime").value(DEFAULT_ENERGY_GATE_OPENING_TIME.toString()))
            .andExpect(jsonPath("$.energyGateClosureTime").value(DEFAULT_ENERGY_GATE_CLOSURE_TIME.toString()))
            .andExpect(jsonPath("$.minDesiredEnergy").value(DEFAULT_MIN_DESIRED_ENERGY.intValue()))
            .andExpect(jsonPath("$.maxDesiredEnergy").value(DEFAULT_MAX_DESIRED_ENERGY.intValue()))
            .andExpect(jsonPath("$.energyAvailabilityFrom").value(DEFAULT_ENERGY_AVAILABILITY_FROM.toString()))
            .andExpect(jsonPath("$.energyAvailabilityTo").value(DEFAULT_ENERGY_AVAILABILITY_TO.toString()))
            .andExpect(jsonPath("$.firstAuctionDate").value(DEFAULT_FIRST_AUCTION_DATE.toString()))
            .andExpect(jsonPath("$.lastAuctionDate").value(DEFAULT_LAST_AUCTION_DATE.toString()));
    }

    @Test
    @Transactional
    public void getAuctionsSeriesByIdFiltering() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        Long id = seriesCapacity.getId();

        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("id.equals=" + id);
        defaultAuctionsSeriesShouldNotBeFound("id.notEquals=" + id);

        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultAuctionsSeriesShouldNotBeFound("id.greaterThan=" + id);

        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("id.lessThanOrEqual=" + id);
        defaultAuctionsSeriesShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllAuctionsSeriesByProductIdIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where productId equals to DEFAULT_PRODUCT_ID
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("productId.equals=" + productWithUpDirectionEntity.getId());

        // Get all the auctionsSeriesList where productId equals to UPDATED_PRODUCT_ID
        defaultAuctionsSeriesShouldNotBeFound("productId.equals=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByProductIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where productId not equals to DEFAULT_PRODUCT_ID
        defaultAuctionsSeriesShouldNotBeFound("productId.notEquals=" + productWithUpDirectionEntity.getId());

        // Get all the auctionsSeriesList where productId not equals to UPDATED_PRODUCT_ID
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("productId.notEquals=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByProductIdIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where productId in DEFAULT_PRODUCT_ID or UPDATED_PRODUCT_ID
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("productId.in=" + productWithUpDirectionEntity.getId() + "," + Long.MAX_VALUE);

        // Get all the auctionsSeriesList where productId equals to UPDATED_PRODUCT_ID
        defaultAuctionsSeriesShouldNotBeFound("productId.in=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByProductIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where productId is not null
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("productId.specified=true");

        // Get all the auctionsSeriesList where productId is null
        defaultAuctionsSeriesShouldNotBeFound("productId.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByProductIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where productId is greater than or equal to DEFAULT_PRODUCT_ID
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("productId.greaterThanOrEqual=" + productWithUpDirectionEntity.getId());

        // Get all the auctionsSeriesList where productId is greater than or equal to UPDATED_PRODUCT_ID
        defaultAuctionsSeriesShouldNotBeFound("productId.greaterThanOrEqual=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByProductIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where productId is less than or equal to DEFAULT_PRODUCT_ID
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("productId.lessThanOrEqual=" + productWithUpDirectionEntity.getId());

        // Get all the auctionsSeriesList where productId is less than or equal to SMALLER_PRODUCT_ID
        defaultAuctionsSeriesShouldNotBeFound("productId.lessThanOrEqual=" + (productWithUpDirectionEntity.getId() - 1));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByProductIdIsLessThanSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where productId is less than DEFAULT_PRODUCT_ID
        defaultAuctionsSeriesShouldNotBeFound("productId.lessThan=" + productWithUpDirectionEntity.getId());

        // Get all the auctionsSeriesList where productId is less than UPDATED_PRODUCT_ID
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("productId.lessThan=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByProductIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where productId is greater than DEFAULT_PRODUCT_ID
        defaultAuctionsSeriesShouldNotBeFound("productId.greaterThan=" + productWithUpDirectionEntity.getId());

        // Get all the auctionsSeriesList where productId is greater than SMALLER_PRODUCT_ID
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("productId.greaterThan=" + (productWithUpDirectionEntity.getId() - 1));
    }


    @Test
    @Transactional
    public void getAllAuctionsSeriesByAuctionTypeIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where auctionType equals to DEFAULT_AUCTION_TYPE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("type.equals=" + AUCTION_CAPACITY_TYPE);

        // Get all the auctionsSeriesList where auctionType equals to UPDATED_AUCTION_TYPE
        defaultAuctionsSeriesShouldNotBeFound("type.equals=" + AUCTION_ENERGY_TYPE);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByAuctionTypeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where auctionType not equals to DEFAULT_AUCTION_TYPE
        defaultAuctionsSeriesShouldNotBeFound("type.notEquals=" + AUCTION_CAPACITY_TYPE);

        // Get all the auctionsSeriesList where auctionType not equals to UPDATED_AUCTION_TYPE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("type.notEquals=" + AUCTION_ENERGY_TYPE);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByAuctionTypeIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where auctionType in DEFAULT_AUCTION_TYPE or UPDATED_AUCTION_TYPE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("type.in=" + AUCTION_CAPACITY_TYPE + "," + AUCTION_ENERGY_TYPE);

        // Get all the auctionsSeriesList where auctionType equals to UPDATED_AUCTION_TYPE
        defaultAuctionsSeriesShouldNotBeFound("type.in=" + AUCTION_ENERGY_TYPE);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByAuctionTypeIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where auctionType is not null
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("type.specified=true");

        // Get all the auctionsSeriesList where auctionType is null
        defaultAuctionsSeriesShouldNotBeFound("type.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyGateOpeningTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyGateOpeningTime equals to DEFAULT_ENERGY_GATE_OPENING_TIME
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyGateOpeningTime.equals=" + DEFAULT_ENERGY_GATE_OPENING_TIME);

        // Get all the auctionsSeriesList where energyGateOpeningTime equals to UPDATED_ENERGY_GATE_OPENING_TIME
        defaultAuctionsSeriesShouldNotBeFound("energyGateOpeningTime.equals=" + UPDATED_ENERGY_GATE_OPENING_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyGateOpeningTimeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyGateOpeningTime not equals to DEFAULT_ENERGY_GATE_OPENING_TIME
        defaultAuctionsSeriesShouldNotBeFound("energyGateOpeningTime.notEquals=" + DEFAULT_ENERGY_GATE_OPENING_TIME);

        // Get all the auctionsSeriesList where energyGateOpeningTime not equals to UPDATED_ENERGY_GATE_OPENING_TIME
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyGateOpeningTime.notEquals=" + UPDATED_ENERGY_GATE_OPENING_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyGateOpeningTimeIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyGateOpeningTime in DEFAULT_ENERGY_GATE_OPENING_TIME or UPDATED_ENERGY_GATE_OPENING_TIME
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyGateOpeningTime.in=" + DEFAULT_ENERGY_GATE_OPENING_TIME + "," + UPDATED_ENERGY_GATE_OPENING_TIME);

        // Get all the auctionsSeriesList where energyGateOpeningTime equals to UPDATED_ENERGY_GATE_OPENING_TIME
        defaultAuctionsSeriesShouldNotBeFound("energyGateOpeningTime.in=" + UPDATED_ENERGY_GATE_OPENING_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyGateOpeningTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyGateOpeningTime is not null
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyGateOpeningTime.specified=true");

        // Get all the auctionsSeriesList where energyGateOpeningTime is null
        defaultAuctionsSeriesShouldNotBeFound("energyGateOpeningTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyGateClosureTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyGateClosureTime equals to DEFAULT_ENERGY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyGateClosureTime.equals=" + DEFAULT_ENERGY_GATE_CLOSURE_TIME);

        // Get all the auctionsSeriesList where energyGateClosureTime equals to UPDATED_ENERGY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesShouldNotBeFound("energyGateClosureTime.equals=" + UPDATED_ENERGY_GATE_CLOSURE_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyGateClosureTimeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyGateClosureTime not equals to DEFAULT_ENERGY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesShouldNotBeFound("energyGateClosureTime.notEquals=" + DEFAULT_ENERGY_GATE_CLOSURE_TIME);

        // Get all the auctionsSeriesList where energyGateClosureTime not equals to UPDATED_ENERGY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyGateClosureTime.notEquals=" + UPDATED_ENERGY_GATE_CLOSURE_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyGateClosureTimeIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyGateClosureTime in DEFAULT_ENERGY_GATE_CLOSURE_TIME or UPDATED_ENERGY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyGateClosureTime.in=" + DEFAULT_ENERGY_GATE_CLOSURE_TIME + "," + UPDATED_ENERGY_GATE_CLOSURE_TIME);

        // Get all the auctionsSeriesList where energyGateClosureTime equals to UPDATED_ENERGY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesShouldNotBeFound("energyGateClosureTime.in=" + UPDATED_ENERGY_GATE_CLOSURE_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyGateClosureTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyGateClosureTime is not null
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyGateClosureTime.specified=true");

        // Get all the auctionsSeriesList where energyGateClosureTime is null
        defaultAuctionsSeriesShouldNotBeFound("energyGateClosureTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityGateOpeningTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityGateOpeningTime equals to DEFAULT_CAPACITY_GATE_OPENING_TIME
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityGateOpeningTime.equals=" + DEFAULT_CAPACITY_GATE_OPENING_TIME);

        // Get all the auctionsSeriesList where capacityGateOpeningTime equals to UPDATED_CAPACITY_GATE_OPENING_TIME
        defaultAuctionsSeriesShouldNotBeFound("capacityGateOpeningTime.equals=" + UPDATED_CAPACITY_GATE_OPENING_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityGateOpeningTimeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityGateOpeningTime not equals to DEFAULT_CAPACITY_GATE_OPENING_TIME
        defaultAuctionsSeriesShouldNotBeFound("capacityGateOpeningTime.notEquals=" + DEFAULT_CAPACITY_GATE_OPENING_TIME);

        // Get all the auctionsSeriesList where capacityGateOpeningTime not equals to UPDATED_CAPACITY_GATE_OPENING_TIME
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityGateOpeningTime.notEquals=" + UPDATED_CAPACITY_GATE_OPENING_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityGateOpeningTimeIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityGateOpeningTime in DEFAULT_CAPACITY_GATE_OPENING_TIME or UPDATED_CAPACITY_GATE_OPENING_TIME
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityGateOpeningTime.in=" + DEFAULT_CAPACITY_GATE_OPENING_TIME + "," + UPDATED_CAPACITY_GATE_OPENING_TIME);

        // Get all the auctionsSeriesList where capacityGateOpeningTime equals to UPDATED_CAPACITY_GATE_OPENING_TIME
        defaultAuctionsSeriesShouldNotBeFound("capacityGateOpeningTime.in=" + UPDATED_CAPACITY_GATE_OPENING_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityGateOpeningTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityGateOpeningTime is not null
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityGateOpeningTime.specified=true");

        // Get all the auctionsSeriesList where capacityGateOpeningTime is null
        defaultAuctionsSeriesShouldNotBeFound("capacityGateOpeningTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityGateClosureTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityGateClosureTime equals to DEFAULT_CAPACITY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityGateClosureTime.equals=" + DEFAULT_CAPACITY_GATE_CLOSURE_TIME);

        // Get all the auctionsSeriesList where capacityGateClosureTime equals to UPDATED_CAPACITY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesShouldNotBeFound("capacityGateClosureTime.equals=" + DEFAULT_CAPACITY_GATE_CLOSURE_TIME.plus(1, DAYS));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityGateClosureTimeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityGateClosureTime not equals to DEFAULT_CAPACITY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesShouldNotBeFound("capacityGateClosureTime.notEquals=" + DEFAULT_CAPACITY_GATE_CLOSURE_TIME);

        // Get all the auctionsSeriesList where capacityGateClosureTime not equals to UPDATED_CAPACITY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityGateClosureTime.notEquals=" + DEFAULT_CAPACITY_GATE_CLOSURE_TIME.plus(1, DAYS));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityGateClosureTimeIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityGateClosureTime in DEFAULT_CAPACITY_GATE_CLOSURE_TIME or UPDATED_CAPACITY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityGateClosureTime.in=" + DEFAULT_CAPACITY_GATE_CLOSURE_TIME + "," + Instant.now());

        // Get all the auctionsSeriesList where capacityGateClosureTime equals to UPDATED_CAPACITY_GATE_CLOSURE_TIME
        defaultAuctionsSeriesShouldNotBeFound("capacityGateClosureTime.in=" + Instant.now());
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityGateClosureTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityGateClosureTime is not null
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityGateClosureTime.specified=true");

        // Get all the auctionsSeriesList where capacityGateClosureTime is null
        defaultAuctionsSeriesShouldNotBeFound("capacityGateClosureTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredCapacityIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where minDesiredCapacity equals to DEFAULT_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("minDesiredCapacity.equals=" + DEFAULT_MIN_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where minDesiredCapacity equals to UPDATED_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredCapacity.equals=" + UPDATED_MIN_DESIRED_CAPACITY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredCapacityIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where minDesiredCapacity not equals to DEFAULT_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredCapacity.notEquals=" + DEFAULT_MIN_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where minDesiredCapacity not equals to UPDATED_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("minDesiredCapacity.notEquals=" + UPDATED_MIN_DESIRED_CAPACITY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredCapacityIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where minDesiredCapacity in DEFAULT_MIN_DESIRED_CAPACITY or UPDATED_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("minDesiredCapacity.in=" + DEFAULT_MIN_DESIRED_CAPACITY + "," + UPDATED_MIN_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where minDesiredCapacity equals to UPDATED_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredCapacity.in=" + UPDATED_MIN_DESIRED_CAPACITY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredCapacityIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where minDesiredCapacity is not null
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("minDesiredCapacity.specified=true");

        // Get all the auctionsSeriesList where minDesiredCapacity is null
        defaultAuctionsSeriesShouldNotBeFound("minDesiredCapacity.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredCapacityIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where minDesiredCapacity is greater than or equal to DEFAULT_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("minDesiredCapacity.greaterThanOrEqual=" + DEFAULT_MIN_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where minDesiredCapacity is greater than or equal to UPDATED_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredCapacity.greaterThanOrEqual=" + UPDATED_MIN_DESIRED_CAPACITY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredCapacityIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where minDesiredCapacity is less than or equal to DEFAULT_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("minDesiredCapacity.lessThanOrEqual=" + DEFAULT_MIN_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where minDesiredCapacity is less than or equal to SMALLER_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredCapacity.lessThanOrEqual=" + DEFAULT_MIN_DESIRED_CAPACITY.add(BigDecimal.valueOf(-1)));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredCapacityIsLessThanSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where minDesiredCapacity is less than DEFAULT_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredCapacity.lessThan=" + DEFAULT_MIN_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where minDesiredCapacity is less than UPDATED_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("minDesiredCapacity.lessThan=" + UPDATED_MIN_DESIRED_CAPACITY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredCapacityIsGreaterThanSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where minDesiredCapacity is greater than DEFAULT_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredCapacity.greaterThan=" + DEFAULT_MIN_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where minDesiredCapacity is greater than SMALLER_MIN_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("minDesiredCapacity.greaterThan=" + DEFAULT_MIN_DESIRED_CAPACITY.add(BigDecimal.valueOf(-1)));
    }


    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredCapacityIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where maxDesiredCapacity equals to DEFAULT_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("maxDesiredCapacity.equals=" + DEFAULT_MAX_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where maxDesiredCapacity equals to UPDATED_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredCapacity.equals=" + UPDATED_MAX_DESIRED_CAPACITY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredCapacityIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where maxDesiredCapacity not equals to DEFAULT_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredCapacity.notEquals=" + DEFAULT_MAX_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where maxDesiredCapacity not equals to UPDATED_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("maxDesiredCapacity.notEquals=" + UPDATED_MAX_DESIRED_CAPACITY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredCapacityIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where maxDesiredCapacity in DEFAULT_MAX_DESIRED_CAPACITY or UPDATED_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("maxDesiredCapacity.in=" + DEFAULT_MAX_DESIRED_CAPACITY + "," + UPDATED_MAX_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where maxDesiredCapacity equals to UPDATED_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredCapacity.in=" + UPDATED_MAX_DESIRED_CAPACITY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredCapacityIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where maxDesiredCapacity is not null
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("maxDesiredCapacity.specified=true");

        // Get all the auctionsSeriesList where maxDesiredCapacity is null
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredCapacity.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredCapacityIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where maxDesiredCapacity is greater than or equal to DEFAULT_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("maxDesiredCapacity.greaterThanOrEqual=" + DEFAULT_MAX_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where maxDesiredCapacity is greater than or equal to UPDATED_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredCapacity.greaterThanOrEqual=" + DEFAULT_MAX_DESIRED_CAPACITY.add(BigDecimal.valueOf(1)));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredCapacityIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where maxDesiredCapacity is less than or equal to DEFAULT_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("maxDesiredCapacity.lessThanOrEqual=" + DEFAULT_MAX_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where maxDesiredCapacity is less than or equal to SMALLER_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredCapacity.lessThanOrEqual=" + DEFAULT_MAX_DESIRED_CAPACITY.add(BigDecimal.valueOf(-1)));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredCapacityIsLessThanSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where maxDesiredCapacity is less than DEFAULT_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredCapacity.lessThan=" + DEFAULT_MAX_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where maxDesiredCapacity is less than UPDATED_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("maxDesiredCapacity.lessThan=" + DEFAULT_MAX_DESIRED_CAPACITY.add(BigDecimal.valueOf(1)));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredCapacityIsGreaterThanSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where maxDesiredCapacity is greater than DEFAULT_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredCapacity.greaterThan=" + DEFAULT_MAX_DESIRED_CAPACITY);

        // Get all the auctionsSeriesList where maxDesiredCapacity is greater than SMALLER_MAX_DESIRED_CAPACITY
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("maxDesiredCapacity.greaterThan=" + DEFAULT_MAX_DESIRED_CAPACITY.add(BigDecimal.valueOf(-1)));
    }


    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredEnergyIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where minDesiredEnergy equals to DEFAULT_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("minDesiredEnergy.equals=" + DEFAULT_MIN_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where minDesiredEnergy equals to UPDATED_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredEnergy.equals=" + UPDATED_MIN_DESIRED_ENERGY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredEnergyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where minDesiredEnergy not equals to DEFAULT_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredEnergy.notEquals=" + DEFAULT_MIN_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where minDesiredEnergy not equals to UPDATED_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("minDesiredEnergy.notEquals=" + UPDATED_MIN_DESIRED_ENERGY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredEnergyIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where minDesiredEnergy in DEFAULT_MIN_DESIRED_ENERGY or UPDATED_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("minDesiredEnergy.in=" + DEFAULT_MIN_DESIRED_ENERGY + "," + UPDATED_MIN_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where minDesiredEnergy equals to UPDATED_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredEnergy.in=" + UPDATED_MIN_DESIRED_ENERGY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredEnergyIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where minDesiredEnergy is not null
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("minDesiredEnergy.specified=true");

        // Get all the auctionsSeriesList where minDesiredEnergy is null
        defaultAuctionsSeriesShouldNotBeFound("minDesiredEnergy.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredEnergyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where minDesiredEnergy is greater than or equal to DEFAULT_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("minDesiredEnergy.greaterThanOrEqual=" + DEFAULT_MIN_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where minDesiredEnergy is greater than or equal to UPDATED_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredEnergy.greaterThanOrEqual=" + UPDATED_MIN_DESIRED_ENERGY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredEnergyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where minDesiredEnergy is less than or equal to DEFAULT_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("minDesiredEnergy.lessThanOrEqual=" + DEFAULT_MIN_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where minDesiredEnergy is less than or equal to SMALLER_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredEnergy.lessThanOrEqual=" + DEFAULT_MIN_DESIRED_ENERGY.add(BigDecimal.valueOf(-1)));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredEnergyIsLessThanSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where minDesiredEnergy is less than DEFAULT_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredEnergy.lessThan=" + DEFAULT_MIN_DESIRED_ENERGY.intValue());

        // Get all the auctionsSeriesList where minDesiredEnergy is less than UPDATED_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("minDesiredEnergy.lessThan=" + DEFAULT_MIN_DESIRED_ENERGY.add(BigDecimal.valueOf(10)).intValue());
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMinDesiredEnergyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where minDesiredEnergy is greater than DEFAULT_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("minDesiredEnergy.greaterThan=" + DEFAULT_MIN_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where minDesiredEnergy is greater than SMALLER_MIN_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("minDesiredEnergy.greaterThan=" + DEFAULT_MIN_DESIRED_ENERGY.add(BigDecimal.valueOf(-1)));
    }


    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredEnergyIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where maxDesiredEnergy equals to DEFAULT_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("maxDesiredEnergy.equals=" + DEFAULT_MAX_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where maxDesiredEnergy equals to UPDATED_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredEnergy.equals=" + UPDATED_MAX_DESIRED_ENERGY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredEnergyIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where maxDesiredEnergy not equals to DEFAULT_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredEnergy.notEquals=" + DEFAULT_MAX_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where maxDesiredEnergy not equals to UPDATED_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("maxDesiredEnergy.notEquals=" + UPDATED_MAX_DESIRED_ENERGY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredEnergyIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where maxDesiredEnergy in DEFAULT_MAX_DESIRED_ENERGY or UPDATED_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("maxDesiredEnergy.in=" + DEFAULT_MAX_DESIRED_ENERGY + "," + UPDATED_MAX_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where maxDesiredEnergy equals to UPDATED_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredEnergy.in=" + UPDATED_MAX_DESIRED_ENERGY);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredEnergyIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where maxDesiredEnergy is not null
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("maxDesiredEnergy.specified=true");

        // Get all the auctionsSeriesList where maxDesiredEnergy is null
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredEnergy.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredEnergyIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where maxDesiredEnergy is greater than or equal to DEFAULT_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("maxDesiredEnergy.greaterThanOrEqual=" + DEFAULT_MAX_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where maxDesiredEnergy is greater than or equal to UPDATED_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredEnergy.greaterThanOrEqual=" + DEFAULT_MAX_DESIRED_ENERGY.add(BigDecimal.valueOf(1L)));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredEnergyIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where maxDesiredEnergy is less than or equal to DEFAULT_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("maxDesiredEnergy.lessThanOrEqual=" + DEFAULT_MAX_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where maxDesiredEnergy is less than or equal to SMALLER_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredEnergy.lessThanOrEqual=" + DEFAULT_MAX_DESIRED_ENERGY.add(BigDecimal.valueOf(-1L)));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredEnergyIsLessThanSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where maxDesiredEnergy is less than DEFAULT_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredEnergy.lessThan=" + DEFAULT_MAX_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where maxDesiredEnergy is less than UPDATED_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("maxDesiredEnergy.lessThan=" + DEFAULT_MAX_DESIRED_ENERGY.add(BigDecimal.valueOf(1L)));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByMaxDesiredEnergyIsGreaterThanSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where maxDesiredEnergy is greater than DEFAULT_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesShouldNotBeFound("maxDesiredEnergy.greaterThan=" + DEFAULT_MAX_DESIRED_ENERGY);

        // Get all the auctionsSeriesList where maxDesiredEnergy is greater than SMALLER_MAX_DESIRED_ENERGY
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("maxDesiredEnergy.greaterThan=" + DEFAULT_MAX_DESIRED_ENERGY.add(BigDecimal.valueOf(-1L)));
    }


    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityAvailabilityFromIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityAvailabilityFrom equals to DEFAULT_CAPACITY_AVAILABILITY_FROM
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityAvailabilityFrom.equals=" + DEFAULT_CAPACITY_AVAILABILITY_FROM.toString());

        // Get all the auctionsSeriesList where capacityAvailabilityFrom equals to UPDATED_CAPACITY_AVAILABILITY_FROM
        defaultAuctionsSeriesShouldNotBeFound("capacityAvailabilityFrom.equals=" + UPDATED_CAPACITY_AVAILABILITY_FROM.toString());
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityAvailabilityFromIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityAvailabilityFrom not equals to DEFAULT_CAPACITY_AVAILABILITY_FROM
        defaultAuctionsSeriesShouldNotBeFound("capacityAvailabilityFrom.notEquals=" + DEFAULT_CAPACITY_AVAILABILITY_FROM.toString());

        // Get all the auctionsSeriesList where capacityAvailabilityFrom not equals to UPDATED_CAPACITY_AVAILABILITY_FROM
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityAvailabilityFrom.notEquals=" + UPDATED_CAPACITY_AVAILABILITY_FROM.toString());
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityAvailabilityFromIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityAvailabilityFrom in DEFAULT_CAPACITY_AVAILABILITY_FROM or UPDATED_CAPACITY_AVAILABILITY_FROM
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityAvailabilityFrom.in=" + DEFAULT_CAPACITY_AVAILABILITY_FROM + "," + UPDATED_CAPACITY_AVAILABILITY_FROM);

        // Get all the auctionsSeriesList where capacityAvailabilityFrom equals to UPDATED_CAPACITY_AVAILABILITY_FROM
        defaultAuctionsSeriesShouldNotBeFound("capacityAvailabilityFrom.in=" + UPDATED_CAPACITY_AVAILABILITY_FROM);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityAvailabilityFromIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityAvailabilityFrom is not null
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityAvailabilityFrom.specified=true");

        // Get all the auctionsSeriesList where capacityAvailabilityFrom is null
        defaultAuctionsSeriesShouldNotBeFound("capacityAvailabilityFrom.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityAvailabilityToIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityAvailabilityTo equals to DEFAULT_CAPACITY_AVAILABILITY_TO
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityAvailabilityTo.equals=" + DEFAULT_CAPACITY_AVAILABILITY_TO);

        // Get all the auctionsSeriesList where capacityAvailabilityTo equals to UPDATED_CAPACITY_AVAILABILITY_TO
        defaultAuctionsSeriesShouldNotBeFound("capacityAvailabilityTo.equals=" + UPDATED_CAPACITY_AVAILABILITY_TO);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityAvailabilityToIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityAvailabilityTo not equals to DEFAULT_CAPACITY_AVAILABILITY_TO
        defaultAuctionsSeriesShouldNotBeFound("capacityAvailabilityTo.notEquals=" + DEFAULT_CAPACITY_AVAILABILITY_TO);

        // Get all the auctionsSeriesList where capacityAvailabilityTo not equals to UPDATED_CAPACITY_AVAILABILITY_TO
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityAvailabilityTo.notEquals=" + UPDATED_CAPACITY_AVAILABILITY_TO);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityAvailabilityToIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityAvailabilityTo in DEFAULT_CAPACITY_AVAILABILITY_TO or UPDATED_CAPACITY_AVAILABILITY_TO
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityAvailabilityTo.in=" + DEFAULT_CAPACITY_AVAILABILITY_TO + "," + UPDATED_CAPACITY_AVAILABILITY_TO);

        // Get all the auctionsSeriesList where capacityAvailabilityTo equals to UPDATED_CAPACITY_AVAILABILITY_TO
        defaultAuctionsSeriesShouldNotBeFound("capacityAvailabilityTo.in=" + UPDATED_CAPACITY_AVAILABILITY_TO);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCapacityAvailabilityToIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where capacityAvailabilityTo is not null
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("capacityAvailabilityTo.specified=true");

        // Get all the auctionsSeriesList where capacityAvailabilityTo is null
        defaultAuctionsSeriesShouldNotBeFound("capacityAvailabilityTo.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyAvailabilityFromIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyAvailabilityFrom equals to DEFAULT_ENERGY_AVAILABILITY_FROM
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyAvailabilityFrom.equals=" + DEFAULT_ENERGY_AVAILABILITY_FROM);

        // Get all the auctionsSeriesList where energyAvailabilityFrom equals to UPDATED_ENERGY_AVAILABILITY_FROM
        defaultAuctionsSeriesShouldNotBeFound("energyAvailabilityFrom.equals=" + UPDATED_ENERGY_AVAILABILITY_FROM);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyAvailabilityFromIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyAvailabilityFrom not equals to DEFAULT_ENERGY_AVAILABILITY_FROM
        defaultAuctionsSeriesShouldNotBeFound("energyAvailabilityFrom.notEquals=" + DEFAULT_ENERGY_AVAILABILITY_FROM);

        // Get all the auctionsSeriesList where energyAvailabilityFrom not equals to UPDATED_ENERGY_AVAILABILITY_FROM
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyAvailabilityFrom.notEquals=" + UPDATED_ENERGY_AVAILABILITY_FROM);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyAvailabilityFromIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyAvailabilityFrom in DEFAULT_ENERGY_AVAILABILITY_FROM or UPDATED_ENERGY_AVAILABILITY_FROM
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyAvailabilityFrom.in=" + DEFAULT_ENERGY_AVAILABILITY_FROM + "," + UPDATED_ENERGY_AVAILABILITY_FROM);

        // Get all the auctionsSeriesList where energyAvailabilityFrom equals to UPDATED_ENERGY_AVAILABILITY_FROM
        defaultAuctionsSeriesShouldNotBeFound("energyAvailabilityFrom.in=" + UPDATED_ENERGY_AVAILABILITY_FROM);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyAvailabilityFromIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyAvailabilityFrom is not null
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyAvailabilityFrom.specified=true");

        // Get all the auctionsSeriesList where energyAvailabilityFrom is null
        defaultAuctionsSeriesShouldNotBeFound("energyAvailabilityFrom.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyAvailabilityToIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyAvailabilityTo equals to DEFAULT_ENERGY_AVAILABILITY_TO
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyAvailabilityTo.equals=" + DEFAULT_ENERGY_AVAILABILITY_TO);

        // Get all the auctionsSeriesList where energyAvailabilityTo equals to UPDATED_ENERGY_AVAILABILITY_TO
        defaultAuctionsSeriesShouldNotBeFound("energyAvailabilityTo.equals=" + UPDATED_ENERGY_AVAILABILITY_TO);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyAvailabilityToIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyAvailabilityTo not equals to DEFAULT_ENERGY_AVAILABILITY_TO
        defaultAuctionsSeriesShouldNotBeFound("energyAvailabilityTo.notEquals=" + DEFAULT_ENERGY_AVAILABILITY_TO);

        // Get all the auctionsSeriesList where energyAvailabilityTo not equals to UPDATED_ENERGY_AVAILABILITY_TO
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyAvailabilityTo.notEquals=" + UPDATED_ENERGY_AVAILABILITY_TO);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyAvailabilityToIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyAvailabilityTo in DEFAULT_ENERGY_AVAILABILITY_TO or UPDATED_ENERGY_AVAILABILITY_TO
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyAvailabilityTo.in=" + DEFAULT_ENERGY_AVAILABILITY_TO + "," + UPDATED_ENERGY_AVAILABILITY_TO);

        // Get all the auctionsSeriesList where energyAvailabilityTo equals to UPDATED_ENERGY_AVAILABILITY_TO
        defaultAuctionsSeriesShouldNotBeFound("energyAvailabilityTo.in=" + UPDATED_ENERGY_AVAILABILITY_TO);
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByEnergyAvailabilityToIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesEnergy);

        // Get all the auctionsSeriesList where energyAvailabilityTo is not null
        defaultAuctionsSeriesWithTypeEnergyShouldBeFound("energyAvailabilityTo.specified=true");

        // Get all the auctionsSeriesList where energyAvailabilityTo is null
        defaultAuctionsSeriesShouldNotBeFound("energyAvailabilityTo.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByFirstAuctionDateIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where firstAuctionDate equals to DEFAULT_FIRST_AUCTION_DATE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("firstAuctionDate.equals=" + DEFAULT_FIRST_AUCTION_DATE);

        // Get all the auctionsSeriesList where firstAuctionDate equals to UPDATED_FIRST_AUCTION_DATE
        defaultAuctionsSeriesShouldNotBeFound("firstAuctionDate.equals=" + Instant.now());
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByFirstAuctionDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where firstAuctionDate not equals to DEFAULT_FIRST_AUCTION_DATE
        defaultAuctionsSeriesShouldNotBeFound("firstAuctionDate.notEquals=" + DEFAULT_FIRST_AUCTION_DATE);

        // Get all the auctionsSeriesList where firstAuctionDate not equals to UPDATED_FIRST_AUCTION_DATE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("firstAuctionDate.notEquals=" + Instant.now());
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByFirstAuctionDateIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where firstAuctionDate in DEFAULT_FIRST_AUCTION_DATE or UPDATED_FIRST_AUCTION_DATE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("firstAuctionDate.in=" + DEFAULT_FIRST_AUCTION_DATE + "," + Instant.now());

        // Get all the auctionsSeriesList where firstAuctionDate equals to UPDATED_FIRST_AUCTION_DATE
        defaultAuctionsSeriesShouldNotBeFound("firstAuctionDate.in=" + Instant.now());
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByFirstAuctionDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where firstAuctionDate is not null
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("firstAuctionDate.specified=true");

        // Get all the auctionsSeriesList where firstAuctionDate is null
        defaultAuctionsSeriesShouldNotBeFound("firstAuctionDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByLastAuctionDateIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where lastAuctionDate equals to DEFAULT_LAST_AUCTION_DATE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("lastAuctionDate.equals=" + DEFAULT_LAST_AUCTION_DATE);

        // Get all the auctionsSeriesList where lastAuctionDate equals to UPDATED_LAST_AUCTION_DATE
        defaultAuctionsSeriesShouldNotBeFound("lastAuctionDate.equals=" + Instant.now());
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByLastAuctionDateIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where lastAuctionDate not equals to DEFAULT_LAST_AUCTION_DATE
        defaultAuctionsSeriesShouldNotBeFound("lastAuctionDate.notEquals=" + DEFAULT_LAST_AUCTION_DATE);

        // Get all the auctionsSeriesList where lastAuctionDate not equals to UPDATED_LAST_AUCTION_DATE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("lastAuctionDate.notEquals=" + Instant.now());
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByLastAuctionDateIsInShouldWork() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where lastAuctionDate in DEFAULT_LAST_AUCTION_DATE or UPDATED_LAST_AUCTION_DATE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("lastAuctionDate.in=" + DEFAULT_LAST_AUCTION_DATE + "," + Instant.now());

        // Get all the auctionsSeriesList where lastAuctionDate equals to UPDATED_LAST_AUCTION_DATE
        defaultAuctionsSeriesShouldNotBeFound("lastAuctionDate.in=" + Instant.now());
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByLastAuctionDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where lastAuctionDate is not null
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("lastAuctionDate.specified=true");

        // Get all the auctionsSeriesList where lastAuctionDate is null
        defaultAuctionsSeriesShouldNotBeFound("lastAuctionDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where auctionType equals to DEFAULT_AUCTION_TYPE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("name.equals=" + seriesCapacity.getName());

        // Get all the auctionsSeriesList where auctionType equals to UPDATED_AUCTION_TYPE
        defaultAuctionsSeriesShouldNotBeFound("name.equals=" + RandomStringUtils.random(10));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where auctionType not equals to DEFAULT_AUCTION_TYPE
        defaultAuctionsSeriesShouldNotBeFound("name.notEquals=" + seriesCapacity.getName());

        // Get all the auctionsSeriesList where auctionType not equals to UPDATED_AUCTION_TYPE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("name.notEquals=" + RandomStringUtils.random(10));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCreatedByIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where auctionType equals to DEFAULT_AUCTION_TYPE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("createdBy.equals=" + seriesCapacity.getCreatedBy());

        // Get all the auctionsSeriesList where auctionType equals to UPDATED_AUCTION_TYPE
        defaultAuctionsSeriesShouldNotBeFound("createdBy.equals=" + RandomStringUtils.random(10));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByCreatedByIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where auctionType not equals to DEFAULT_AUCTION_TYPE
        defaultAuctionsSeriesShouldNotBeFound("createdBy.notEquals=" + seriesCapacity.getCreatedBy());

        // Get all the auctionsSeriesList where auctionType not equals to UPDATED_AUCTION_TYPE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("createdBy.notEquals=" + RandomStringUtils.random(10));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByLastModifiedByIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where auctionType equals to DEFAULT_AUCTION_TYPE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("lastModifiedBy.equals=" + seriesCapacity.getLastModifiedBy());

        // Get all the auctionsSeriesList where auctionType equals to UPDATED_AUCTION_TYPE
        defaultAuctionsSeriesShouldNotBeFound("lastModifiedBy.equals=" + RandomStringUtils.random(10));
    }

    @Test
    @Transactional
    public void getAllAuctionsSeriesByLastModifiedByIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        // Get all the auctionsSeriesList where auctionType not equals to DEFAULT_AUCTION_TYPE
        defaultAuctionsSeriesShouldNotBeFound("lastModifiedBy.notEquals=" + seriesCapacity.getLastModifiedBy());

        // Get all the auctionsSeriesList where auctionType not equals to UPDATED_AUCTION_TYPE
        defaultAuctionsSeriesWithTypeCapacityShouldBeFound("lastModifiedBy.notEquals=" + RandomStringUtils.random(10));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAuctionsSeriesWithTypeEnergyShouldBeFound(String filter) throws Exception {
        restAuctionsSeriesMockMvc.perform(get("/api/admin/auctions-series?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(seriesEnergy.getId().intValue())))
            .andExpect(jsonPath("$.[*].product.id").value(hasItem(productWithUndefinedDirectionEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(AUCTION_ENERGY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(seriesEnergy.getName())))
            .andExpect(jsonPath("$.[*].energyGateOpeningTime").value(hasItem(DEFAULT_ENERGY_GATE_OPENING_TIME.toString())))
            .andExpect(jsonPath("$.[*].energyGateClosureTime").value(hasItem(DEFAULT_ENERGY_GATE_CLOSURE_TIME.toString())))
            .andExpect(jsonPath("$.[*].minDesiredEnergy").value(hasItem(DEFAULT_MIN_DESIRED_ENERGY.intValue())))
            .andExpect(jsonPath("$.[*].maxDesiredEnergy").value(hasItem(DEFAULT_MAX_DESIRED_ENERGY.intValue())))
            .andExpect(jsonPath("$.[*].energyAvailabilityFrom").value(hasItem(DEFAULT_ENERGY_AVAILABILITY_FROM.toString())))
            .andExpect(jsonPath("$.[*].energyAvailabilityTo").value(hasItem(DEFAULT_ENERGY_AVAILABILITY_TO.toString())))
            .andExpect(jsonPath("$.[*].firstAuctionDate").value(hasItem(DEFAULT_FIRST_AUCTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastAuctionDate").value(hasItem(DEFAULT_LAST_AUCTION_DATE.toString())));
    }

    private void defaultAuctionsSeriesWithTypeCapacityShouldBeFound(String filter) throws Exception {
        restAuctionsSeriesMockMvc.perform(get("/api/admin/auctions-series?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(seriesCapacity.getId().intValue())))
            .andExpect(jsonPath("$.[*].product.id").value(hasItem(productWithUpDirectionEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].type").value(hasItem(AUCTION_CAPACITY_TYPE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(seriesCapacity.getName())))
            .andExpect(jsonPath("$.[*].capacityGateOpeningTime").value(hasItem(DEFAULT_CAPACITY_GATE_OPENING_TIME.toString())))
            .andExpect(jsonPath("$.[*].capacityGateClosureTime").value(hasItem(DEFAULT_CAPACITY_GATE_CLOSURE_TIME.toString())))
            .andExpect(jsonPath("$.[*].minDesiredCapacity").value(hasItem(DEFAULT_MIN_DESIRED_CAPACITY.intValue())))
            .andExpect(jsonPath("$.[*].maxDesiredCapacity").value(hasItem(DEFAULT_MAX_DESIRED_CAPACITY.intValue())))
            .andExpect(jsonPath("$.[*].capacityAvailabilityFrom").value(hasItem(DEFAULT_CAPACITY_AVAILABILITY_FROM.toString())))
            .andExpect(jsonPath("$.[*].capacityAvailabilityTo").value(hasItem(DEFAULT_CAPACITY_AVAILABILITY_TO.toString())))
            .andExpect(jsonPath("$.[*].firstAuctionDate").value(hasItem(DEFAULT_FIRST_AUCTION_DATE.toString())))
            .andExpect(jsonPath("$.[*].lastAuctionDate").value(hasItem(DEFAULT_LAST_AUCTION_DATE.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAuctionsSeriesShouldNotBeFound(String filter) throws Exception {
        restAuctionsSeriesMockMvc.perform(get("/api/admin/auctions-series?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    public void getNonExistingAuctionsSeries() throws Exception {
        // Get the auctionsSeries
        restAuctionsSeriesMockMvc.perform(get("/api/admin/auctions-series/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateAuctionsSeries() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        int databaseSizeBeforeUpdate = auctionsSeriesRepository.findAll().size();

        // Update the auctionsSeries
        AuctionsSeriesEntity updatedAuctionsSeriesEntity = auctionsSeriesRepository.findById(seriesCapacity.getId()).get();
        // Disconnect from session so that the updates on updatedAuctionsSeriesEntity are not directly saved in db
        em.detach(updatedAuctionsSeriesEntity);
        updatedAuctionsSeriesEntity.setCapacityGateOpeningTime(UPDATED_CAPACITY_GATE_OPENING_TIME);
        updatedAuctionsSeriesEntity.setCapacityGateClosureTime(UPDATED_CAPACITY_GATE_CLOSURE_TIME);
        updatedAuctionsSeriesEntity.setMinDesiredCapacity(UPDATED_MIN_DESIRED_CAPACITY);
        updatedAuctionsSeriesEntity.setMaxDesiredCapacity(UPDATED_MAX_DESIRED_CAPACITY);
        updatedAuctionsSeriesEntity.setCapacityAvailabilityFrom(UPDATED_CAPACITY_AVAILABILITY_FROM);
        updatedAuctionsSeriesEntity.setCapacityAvailabilityTo(UPDATED_CAPACITY_AVAILABILITY_TO);
        updatedAuctionsSeriesEntity.setFirstAuctionDate(INCREASED_FIRST_AUCTION_DATE);
        updatedAuctionsSeriesEntity.setLastAuctionDate(INCREASED_LAST_AUCTION_DATE);
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(updatedAuctionsSeriesEntity);

        restAuctionsSeriesMockMvc.perform(put("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(status().isOk());

        // Validate the AuctionsSeries in the database
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeUpdate);
        AuctionsSeriesEntity testAuctionsSeries = auctionsSeriesList.get(auctionsSeriesList.size() - 1);
        assertThat(testAuctionsSeries.getCapacityGateOpeningTime()).isEqualTo(UPDATED_CAPACITY_GATE_OPENING_TIME.toString());
        assertThat(testAuctionsSeries.getCapacityGateClosureTime()).isEqualTo(UPDATED_CAPACITY_GATE_CLOSURE_TIME.toString());
        assertThat(testAuctionsSeries.getMinDesiredCapacity()).isEqualTo(UPDATED_MIN_DESIRED_CAPACITY);
        assertThat(testAuctionsSeries.getMaxDesiredCapacity()).isEqualTo(UPDATED_MAX_DESIRED_CAPACITY);
        assertThat(testAuctionsSeries.getCapacityAvailabilityFrom()).isEqualTo(UPDATED_CAPACITY_AVAILABILITY_FROM);
        assertThat(testAuctionsSeries.getCapacityAvailabilityTo()).isEqualTo(UPDATED_CAPACITY_AVAILABILITY_TO);
        assertThat(testAuctionsSeries.getFirstAuctionDate()).isEqualTo(INCREASED_FIRST_AUCTION_DATE.toString());
        assertThat(testAuctionsSeries.getLastAuctionDate()).isEqualTo(INCREASED_LAST_AUCTION_DATE.toString());
        assertThat(testAuctionsSeries.getEnergyGateOpeningTime()).isNull();
        assertThat(testAuctionsSeries.getEnergyGateClosureTime()).isNull();
        assertThat(testAuctionsSeries.getMinDesiredEnergy()).isNull();
        assertThat(testAuctionsSeries.getMaxDesiredEnergy()).isNull();
        assertThat(testAuctionsSeries.getEnergyAvailabilityFrom()).isNull();
        assertThat(testAuctionsSeries.getEnergyAvailabilityTo()).isNull();
    }

    @Test
    @Transactional
    public void updateNonExistingAuctionsSeries() throws Exception {
        int databaseSizeBeforeUpdate = auctionsSeriesRepository.findAll().size();

        // Create the AuctionsSeries
        AuctionsSeriesDTO auctionsSeriesDTO = auctionsSeriesMapper.toDto(seriesCapacity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuctionsSeriesMockMvc.perform(put("/api/admin/auctions-series")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(auctionsSeriesDTO)))
            .andExpect(status().isBadRequest());

        // Validate the AuctionsSeries in the database
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteAuctionsSeries() throws Exception {
        // Initialize the database
        auctionsSeriesRepository.saveAndFlush(seriesCapacity);

        int databaseSizeBeforeDelete = auctionsSeriesRepository.findAll().size();

        // Delete the auctionsSeries
        restAuctionsSeriesMockMvc.perform(delete("/api/admin/auctions-series/{id}", seriesCapacity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<AuctionsSeriesEntity> auctionsSeriesList = auctionsSeriesRepository.findAll();
        assertThat(auctionsSeriesList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
