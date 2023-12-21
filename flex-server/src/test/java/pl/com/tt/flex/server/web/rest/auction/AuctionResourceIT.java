//package pl.com.tt.flex.server.web.rest;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.transaction.annotation.Transactional;
//import pl.com.tt.flex.server.FlexserverApp;
//import pl.com.tt.flex.server.domain.auction.AuctionEntity;
//import pl.com.tt.flex.server.domain.auction.type.AuctionStatus;
//import pl.com.tt.flex.server.domain.auction.type.AuctionType;
//import pl.com.tt.flex.server.repository.auction.AuctionRepository;
//import pl.com.tt.flex.server.service.auction.AuctionQueryService;
//import pl.com.tt.flex.server.service.auction.AuctionService;
//import pl.com.tt.flex.server.service.auction.dto.AuctionDTO;
//import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionMapper;
//
//import javax.persistence.EntityManager;
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.time.temporal.ChronoUnit;
//import java.util.List;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.hamcrest.Matchers.hasItem;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Integration tests for the {@link AuctionResource} REST controller.
// */
//@SpringBootTest(classes = FlexserverApp.class)
//@AutoConfigureMockMvc
//@WithMockUser
//public class AuctionResourceIT {
//
//    private static final String DEFAULT_NAME = "AAAAAAAAAA";
//    private static final String UPDATED_NAME = "BBBBBBBBBB";
//
//    private static final AuctionType DEFAULT_AUCTION_TYPE = AuctionType.ENERGY;
//    private static final AuctionType UPDATED_AUCTION_TYPE = AuctionType.CAPACITY;
//
//    private static final AuctionStatus DEFAULT_STATUS = AuctionStatus.NEW;
//    private static final AuctionStatus UPDATED_STATUS = AuctionStatus.OPEN;
//
//    private static final  DEFAULT_PRODUCT_ID = 1L;
//    private static final Long UPDATED_PRODUCT_ID = 2L;
//    private static final Long SMALLER_PRODUCT_ID = 1L - 1L;
//
//    private static final Instant DEFAULT_GATE_OPENING_TIME = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_GATE_OPENING_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Instant DEFAULT_GATE_CLOSURE_TIME = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_GATE_CLOSURE_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Instant DEFAULT_ENERGY_GATE_OPENING_TIME = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_ENERGY_GATE_OPENING_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Instant DEFAULT_ENERGY_GATE_CLOSURE_TIME = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_ENERGY_GATE_CLOSURE_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Instant DEFAULT_CAPACITY_GATE_OPENING_TIME = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_CAPACITY_GATE_OPENING_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Instant DEFAULT_CAPACITY_GATE_CLOSURE_TIME = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_CAPACITY_GATE_CLOSURE_TIME = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final BigDecimal DEFAULT_MIN_DESIRED_CAPACITY = new BigDecimal(1);
//    private static final BigDecimal UPDATED_MIN_DESIRED_CAPACITY = new BigDecimal(2);
//    private static final BigDecimal SMALLER_MIN_DESIRED_CAPACITY = new BigDecimal(1 - 1);
//
//    private static final BigDecimal DEFAULT_MAX_DESIRED_CAPACITY = new BigDecimal(1);
//    private static final BigDecimal UPDATED_MAX_DESIRED_CAPACITY = new BigDecimal(2);
//    private static final BigDecimal SMALLER_MAX_DESIRED_CAPACITY = new BigDecimal(1 - 1);
//
//    private static final BigDecimal DEFAULT_MIN_DESIRED_ENERGY = new BigDecimal(1);
//    private static final BigDecimal UPDATED_MIN_DESIRED_ENERGY = new BigDecimal(2);
//    private static final BigDecimal SMALLER_MIN_DESIRED_ENERGY = new BigDecimal(1 - 1);
//
//    private static final BigDecimal DEFAULT_MAX_DESIRED_ENERGY = new BigDecimal(1);
//    private static final BigDecimal UPDATED_MAX_DESIRED_ENERGY = new BigDecimal(2);
//    private static final BigDecimal SMALLER_MAX_DESIRED_ENERGY = new BigDecimal(1 - 1);
//
//    private static final Instant DEFAULT_AVAILABILITY_FROM = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_AVAILABILITY_FROM = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Instant DEFAULT_AVAILABILITY_TO = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_AVAILABILITY_TO = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Instant DEFAULT_CAPACITY_AVAILABILITY_FROM = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_CAPACITY_AVAILABILITY_FROM = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Instant DEFAULT_CAPACITY_AVAILABILITY_TO = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_CAPACITY_AVAILABILITY_TO = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Instant DEFAULT_ENERGY_AVAILABILITY_FROM = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_ENERGY_AVAILABILITY_FROM = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Instant DEFAULT_ENERGY_AVAILABILITY_TO = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_ENERGY_AVAILABILITY_TO = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Long DEFAULT_VERSION = 1L;
//    private static final Long UPDATED_VERSION = 2L;
//    private static final Long SMALLER_VERSION = 1L - 1L;
//
//    @Autowired
//    private AuctionRepository auctionRepository;
//
//    @Autowired
//    private AuctionMapper auctionMapper;
//
//    @Autowired
//    private AuctionService auctionService;
//
//    @Autowired
//    private AuctionQueryService auctionQueryService;
//
//    @Autowired
//    private EntityManager em;
//
//    @Autowired
//    private MockMvc restAuctionMockMvc;
//
//    private AuctionEntity auctionEntity;
//
//    /**
//     * Create an entity for this test.
//     * <p>
//     * This is a static method, as tests for other entities might also need it,
//     * if they test an entity which requires the current entity.
//     */
//    public static AuctionEntity createEntity(EntityManager em) {
//        AuctionEntity auctionEntity = AuctionEntity.builder()
//            .name(DEFAULT_NAME)
//            .auctionType(DEFAULT_AUCTION_TYPE)
//            .status(DEFAULT_STATUS)
//            .productId(DEFAULT_PRODUCT_ID)
//            .gateOpeningTime(DEFAULT_GATE_OPENING_TIME)
//            .gateClosureTime(DEFAULT_GATE_CLOSURE_TIME)
//            .energyGateOpeningTime(DEFAULT_ENERGY_GATE_OPENING_TIME)
//            .energyGateClosureTime(DEFAULT_ENERGY_GATE_CLOSURE_TIME)
//            .capacityGateOpeningTime(DEFAULT_CAPACITY_GATE_OPENING_TIME)
//            .capacityGateClosureTime(DEFAULT_CAPACITY_GATE_CLOSURE_TIME)
//            .minDesiredCapacity(DEFAULT_MIN_DESIRED_CAPACITY)
//            .maxDesiredCapacity(DEFAULT_MAX_DESIRED_CAPACITY)
//            .minDesiredEnergy(DEFAULT_MIN_DESIRED_ENERGY)
//            .maxDesiredEnergy(DEFAULT_MAX_DESIRED_ENERGY)
//            .availabilityFrom(DEFAULT_AVAILABILITY_FROM)
//            .availabilityTo(DEFAULT_AVAILABILITY_TO)
//            .capacityAvailabilityFrom(DEFAULT_CAPACITY_AVAILABILITY_FROM)
//            .capacityAvailabilityTo(DEFAULT_CAPACITY_AVAILABILITY_TO)
//            .energyAvailabilityFrom(DEFAULT_ENERGY_AVAILABILITY_FROM)
//            .energyAvailabilityTo(DEFAULT_ENERGY_AVAILABILITY_TO)
//            .version(DEFAULT_VERSION)
//            .build();
//        return auctionEntity;
//    }
//
//    /**
//     * Create an updated entity for this test.
//     * <p>
//     * This is a static method, as tests for other entities might also need it,
//     * if they test an entity which requires the current entity.
//     */
//    public static AuctionEntity createUpdatedEntity(EntityManager em) {
//        AuctionEntity auctionEntity = AuctionEntity.builder()
//            .name(UPDATED_NAME)
//            .auctionType(UPDATED_AUCTION_TYPE)
//            .status(UPDATED_STATUS)
//            .productId(UPDATED_PRODUCT_ID)
//            .gateOpeningTime(UPDATED_GATE_OPENING_TIME)
//            .gateClosureTime(UPDATED_GATE_CLOSURE_TIME)
//            .energyGateOpeningTime(UPDATED_ENERGY_GATE_OPENING_TIME)
//            .energyGateClosureTime(UPDATED_ENERGY_GATE_CLOSURE_TIME)
//            .capacityGateOpeningTime(UPDATED_CAPACITY_GATE_OPENING_TIME)
//            .capacityGateClosureTime(UPDATED_CAPACITY_GATE_CLOSURE_TIME)
//            .minDesiredCapacity(UPDATED_MIN_DESIRED_CAPACITY)
//            .maxDesiredCapacity(UPDATED_MAX_DESIRED_CAPACITY)
//            .minDesiredEnergy(UPDATED_MIN_DESIRED_ENERGY)
//            .maxDesiredEnergy(UPDATED_MAX_DESIRED_ENERGY)
//            .availabilityFrom(UPDATED_AVAILABILITY_FROM)
//            .availabilityTo(UPDATED_AVAILABILITY_TO)
//            .capacityAvailabilityFrom(UPDATED_CAPACITY_AVAILABILITY_FROM)
//            .capacityAvailabilityTo(UPDATED_CAPACITY_AVAILABILITY_TO)
//            .energyAvailabilityFrom(UPDATED_ENERGY_AVAILABILITY_FROM)
//            .energyAvailabilityTo(UPDATED_ENERGY_AVAILABILITY_TO)
//            .version(UPDATED_VERSION)
//            .build();
//        return auctionEntity;
//    }
//
//    @BeforeEach
//    public void initTest() {
//        auctionEntity = createEntity(em);
//    }
//
//    @Test
//    @Transactional
//    public void createAuction() throws Exception {
//        int databaseSizeBeforeCreate = auctionRepository.findAll().size();
//        // Create the Auction
//        AuctionDTO auctionDTO = auctionMapper.toDto(auctionEntity);
//        restAuctionMockMvc.perform(post("/api/auctions")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(TestUtil.convertObjectToJsonBytes(auctionDTO)))
//            .andExpect(status().isCreated());
//
//        // Validate the Auction in the database
//        List<AuctionEntity> auctionList = auctionRepository.findAll();
//        assertThat(auctionList).hasSize(databaseSizeBeforeCreate + 1);
//        AuctionEntity testAuction = auctionList.get(auctionList.size() - 1);
//        assertThat(testAuction.getName()).isEqualTo(DEFAULT_NAME);
//        assertThat(testAuction.getAuctionType()).isEqualTo(DEFAULT_AUCTION_TYPE);
//        assertThat(testAuction.getStatus()).isEqualTo(DEFAULT_STATUS);
//        assertThat(testAuction.getProductId()).isEqualTo(DEFAULT_PRODUCT_ID);
//        assertThat(testAuction.getGateOpeningTime()).isEqualTo(DEFAULT_GATE_OPENING_TIME);
//        assertThat(testAuction.getGateClosureTime()).isEqualTo(DEFAULT_GATE_CLOSURE_TIME);
//        assertThat(testAuction.getEnergyGateOpeningTime()).isEqualTo(DEFAULT_ENERGY_GATE_OPENING_TIME);
//        assertThat(testAuction.getEnergyGateClosureTime()).isEqualTo(DEFAULT_ENERGY_GATE_CLOSURE_TIME);
//        assertThat(testAuction.getCapacityGateOpeningTime()).isEqualTo(DEFAULT_CAPACITY_GATE_OPENING_TIME);
//        assertThat(testAuction.getCapacityGateClosureTime()).isEqualTo(DEFAULT_CAPACITY_GATE_CLOSURE_TIME);
//        assertThat(testAuction.getMinDesiredCapacity()).isEqualTo(DEFAULT_MIN_DESIRED_CAPACITY);
//        assertThat(testAuction.getMaxDesiredCapacity()).isEqualTo(DEFAULT_MAX_DESIRED_CAPACITY);
//        assertThat(testAuction.getMinDesiredEnergy()).isEqualTo(DEFAULT_MIN_DESIRED_ENERGY);
//        assertThat(testAuction.getMaxDesiredEnergy()).isEqualTo(DEFAULT_MAX_DESIRED_ENERGY);
//        assertThat(testAuction.getAvailabilityFrom()).isEqualTo(DEFAULT_AVAILABILITY_FROM);
//        assertThat(testAuction.getAvailabilityTo()).isEqualTo(DEFAULT_AVAILABILITY_TO);
//        assertThat(testAuction.getCapacityAvailabilityFrom()).isEqualTo(DEFAULT_CAPACITY_AVAILABILITY_FROM);
//        assertThat(testAuction.getCapacityAvailabilityTo()).isEqualTo(DEFAULT_CAPACITY_AVAILABILITY_TO);
//        assertThat(testAuction.getEnergyAvailabilityFrom()).isEqualTo(DEFAULT_ENERGY_AVAILABILITY_FROM);
//        assertThat(testAuction.getEnergyAvailabilityTo()).isEqualTo(DEFAULT_ENERGY_AVAILABILITY_TO);
//        assertThat(testAuction.getVersion()).isEqualTo(DEFAULT_VERSION);
//    }
//
//    @Test
//    @Transactional
//    public void createAuctionWithExistingId() throws Exception {
//        int databaseSizeBeforeCreate = auctionRepository.findAll().size();
//
//        // Create the Auction with an existing ID
//        auctionEntity.setId(1L);
//        AuctionDTO auctionDTO = auctionMapper.toDto(auctionEntity);
//
//        // An entity with an existing ID cannot be created, so this API call must fail
//        restAuctionMockMvc.perform(post("/api/auctions")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(TestUtil.convertObjectToJsonBytes(auctionDTO)))
//            .andExpect(status().isBadRequest());
//
//        // Validate the Auction in the database
//        List<AuctionEntity> auctionList = auctionRepository.findAll();
//        assertThat(auctionList).hasSize(databaseSizeBeforeCreate);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllAuctions() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList
//        restAuctionMockMvc.perform(get("/api/auctions?sort=id,desc"))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.[*].id").value(hasItem(auctionEntity.getId().intValue())))
//            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
//            .andExpect(jsonPath("$.[*].auctionType").value(hasItem(DEFAULT_AUCTION_TYPE.toString())))
//            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
//            .andExpect(jsonPath("$.[*].productId").value(hasItem(DEFAULT_PRODUCT_ID)))
//            .andExpect(jsonPath("$.[*].gateOpeningTime").value(hasItem(DEFAULT_GATE_OPENING_TIME.toString())))
//            .andExpect(jsonPath("$.[*].gateClosureTime").value(hasItem(DEFAULT_GATE_CLOSURE_TIME.toString())))
//            .andExpect(jsonPath("$.[*].energyGateOpeningTime").value(hasItem(DEFAULT_ENERGY_GATE_OPENING_TIME.toString())))
//            .andExpect(jsonPath("$.[*].energyGateClosureTime").value(hasItem(DEFAULT_ENERGY_GATE_CLOSURE_TIME.toString())))
//            .andExpect(jsonPath("$.[*].capacityGateOpeningTime").value(hasItem(DEFAULT_CAPACITY_GATE_OPENING_TIME.toString())))
//            .andExpect(jsonPath("$.[*].capacityGateClosureTime").value(hasItem(DEFAULT_CAPACITY_GATE_CLOSURE_TIME.toString())))
//            .andExpect(jsonPath("$.[*].minDesiredCapacity").value(hasItem(DEFAULT_MIN_DESIRED_CAPACITY.intValue())))
//            .andExpect(jsonPath("$.[*].maxDesiredCapacity").value(hasItem(DEFAULT_MAX_DESIRED_CAPACITY.intValue())))
//            .andExpect(jsonPath("$.[*].minDesiredEnergy").value(hasItem(DEFAULT_MIN_DESIRED_ENERGY.intValue())))
//            .andExpect(jsonPath("$.[*].maxDesiredEnergy").value(hasItem(DEFAULT_MAX_DESIRED_ENERGY.intValue())))
//            .andExpect(jsonPath("$.[*].availabilityFrom").value(hasItem(DEFAULT_AVAILABILITY_FROM.toString())))
//            .andExpect(jsonPath("$.[*].availabilityTo").value(hasItem(DEFAULT_AVAILABILITY_TO.toString())))
//            .andExpect(jsonPath("$.[*].capacityAvailabilityFrom").value(hasItem(DEFAULT_CAPACITY_AVAILABILITY_FROM.toString())))
//            .andExpect(jsonPath("$.[*].capacityAvailabilityTo").value(hasItem(DEFAULT_CAPACITY_AVAILABILITY_TO.toString())))
//            .andExpect(jsonPath("$.[*].energyAvailabilityFrom").value(hasItem(DEFAULT_ENERGY_AVAILABILITY_FROM.toString())))
//            .andExpect(jsonPath("$.[*].energyAvailabilityTo").value(hasItem(DEFAULT_ENERGY_AVAILABILITY_TO.toString())))
//            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION.intValue())));
//    }
//
//    @Test
//    @Transactional
//    public void getAuction() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get the auction
//        restAuctionMockMvc.perform(get("/api/auctions/{id}", auctionEntity.getId()))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.id").value(auctionEntity.getId().intValue()))
//            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
//            .andExpect(jsonPath("$.auctionType").value(DEFAULT_AUCTION_TYPE.toString()))
//            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
//            .andExpect(jsonPath("$.productId").value(DEFAULT_PRODUCT_ID))
//            .andExpect(jsonPath("$.gateOpeningTime").value(DEFAULT_GATE_OPENING_TIME.toString()))
//            .andExpect(jsonPath("$.gateClosureTime").value(DEFAULT_GATE_CLOSURE_TIME.toString()))
//            .andExpect(jsonPath("$.energyGateOpeningTime").value(DEFAULT_ENERGY_GATE_OPENING_TIME.toString()))
//            .andExpect(jsonPath("$.energyGateClosureTime").value(DEFAULT_ENERGY_GATE_CLOSURE_TIME.toString()))
//            .andExpect(jsonPath("$.capacityGateOpeningTime").value(DEFAULT_CAPACITY_GATE_OPENING_TIME.toString()))
//            .andExpect(jsonPath("$.capacityGateClosureTime").value(DEFAULT_CAPACITY_GATE_CLOSURE_TIME.toString()))
//            .andExpect(jsonPath("$.minDesiredCapacity").value(DEFAULT_MIN_DESIRED_CAPACITY.intValue()))
//            .andExpect(jsonPath("$.maxDesiredCapacity").value(DEFAULT_MAX_DESIRED_CAPACITY.intValue()))
//            .andExpect(jsonPath("$.minDesiredEnergy").value(DEFAULT_MIN_DESIRED_ENERGY.intValue()))
//            .andExpect(jsonPath("$.maxDesiredEnergy").value(DEFAULT_MAX_DESIRED_ENERGY.intValue()))
//            .andExpect(jsonPath("$.availabilityFrom").value(DEFAULT_AVAILABILITY_FROM.toString()))
//            .andExpect(jsonPath("$.availabilityTo").value(DEFAULT_AVAILABILITY_TO.toString()))
//            .andExpect(jsonPath("$.capacityAvailabilityFrom").value(DEFAULT_CAPACITY_AVAILABILITY_FROM.toString()))
//            .andExpect(jsonPath("$.capacityAvailabilityTo").value(DEFAULT_CAPACITY_AVAILABILITY_TO.toString()))
//            .andExpect(jsonPath("$.energyAvailabilityFrom").value(DEFAULT_ENERGY_AVAILABILITY_FROM.toString()))
//            .andExpect(jsonPath("$.energyAvailabilityTo").value(DEFAULT_ENERGY_AVAILABILITY_TO.toString()))
//            .andExpect(jsonPath("$.version").value(DEFAULT_VERSION.intValue()));
//    }
//
//
//    @Test
//    @Transactional
//    public void getAuctionsByIdFiltering() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        Long id = auctionEntity.getId();
//
//        defaultAuctionShouldBeFound("id.equals=" + id);
//        defaultAuctionShouldNotBeFound("id.notEquals=" + id);
//
//        defaultAuctionShouldBeFound("id.greaterThanOrEqual=" + id);
//        defaultAuctionShouldNotBeFound("id.greaterThan=" + id);
//
//        defaultAuctionShouldBeFound("id.lessThanOrEqual=" + id);
//        defaultAuctionShouldNotBeFound("id.lessThan=" + id);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByNameIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where name equals to DEFAULT_NAME
//        defaultAuctionShouldBeFound("name.equals=" + DEFAULT_NAME);
//
//        // Get all the auctionList where name equals to UPDATED_NAME
//        defaultAuctionShouldNotBeFound("name.equals=" + UPDATED_NAME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByNameIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where name not equals to DEFAULT_NAME
//        defaultAuctionShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);
//
//        // Get all the auctionList where name not equals to UPDATED_NAME
//        defaultAuctionShouldBeFound("name.notEquals=" + UPDATED_NAME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByNameIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where name in DEFAULT_NAME or UPDATED_NAME
//        defaultAuctionShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);
//
//        // Get all the auctionList where name equals to UPDATED_NAME
//        defaultAuctionShouldNotBeFound("name.in=" + UPDATED_NAME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByNameIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where name is not null
//        defaultAuctionShouldBeFound("name.specified=true");
//
//        // Get all the auctionList where name is null
//        defaultAuctionShouldNotBeFound("name.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByNameContainsSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where name contains DEFAULT_NAME
//        defaultAuctionShouldBeFound("name.contains=" + DEFAULT_NAME);
//
//        // Get all the auctionList where name contains UPDATED_NAME
//        defaultAuctionShouldNotBeFound("name.contains=" + UPDATED_NAME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByNameNotContainsSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where name does not contain DEFAULT_NAME
//        defaultAuctionShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);
//
//        // Get all the auctionList where name does not contain UPDATED_NAME
//        defaultAuctionShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCreationDateIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where creationDate is not null
//        defaultAuctionShouldBeFound("creationDate.specified=true");
//
//        // Get all the auctionList where creationDate is null
//        defaultAuctionShouldNotBeFound("creationDate.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAuctionTypeIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where auctionType equals to DEFAULT_AUCTION_TYPE
//        defaultAuctionShouldBeFound("auctionType.equals=" + DEFAULT_AUCTION_TYPE);
//
//        // Get all the auctionList where auctionType equals to UPDATED_AUCTION_TYPE
//        defaultAuctionShouldNotBeFound("auctionType.equals=" + UPDATED_AUCTION_TYPE);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAuctionTypeIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where auctionType not equals to DEFAULT_AUCTION_TYPE
//        defaultAuctionShouldNotBeFound("auctionType.notEquals=" + DEFAULT_AUCTION_TYPE);
//
//        // Get all the auctionList where auctionType not equals to UPDATED_AUCTION_TYPE
//        defaultAuctionShouldBeFound("auctionType.notEquals=" + UPDATED_AUCTION_TYPE);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAuctionTypeIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where auctionType in DEFAULT_AUCTION_TYPE or UPDATED_AUCTION_TYPE
//        defaultAuctionShouldBeFound("auctionType.in=" + DEFAULT_AUCTION_TYPE + "," + UPDATED_AUCTION_TYPE);
//
//        // Get all the auctionList where auctionType equals to UPDATED_AUCTION_TYPE
//        defaultAuctionShouldNotBeFound("auctionType.in=" + UPDATED_AUCTION_TYPE);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAuctionTypeIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where auctionType is not null
//        defaultAuctionShouldBeFound("auctionType.specified=true");
//
//        // Get all the auctionList where auctionType is null
//        defaultAuctionShouldNotBeFound("auctionType.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByStatusIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where status equals to DEFAULT_STATUS
//        defaultAuctionShouldBeFound("status.equals=" + DEFAULT_STATUS);
//
//        // Get all the auctionList where status equals to UPDATED_STATUS
//        defaultAuctionShouldNotBeFound("status.equals=" + UPDATED_STATUS);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByStatusIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where status not equals to DEFAULT_STATUS
//        defaultAuctionShouldNotBeFound("status.notEquals=" + DEFAULT_STATUS);
//
//        // Get all the auctionList where status not equals to UPDATED_STATUS
//        defaultAuctionShouldBeFound("status.notEquals=" + UPDATED_STATUS);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByStatusIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where status in DEFAULT_STATUS or UPDATED_STATUS
//        defaultAuctionShouldBeFound("status.in=" + DEFAULT_STATUS + "," + UPDATED_STATUS);
//
//        // Get all the auctionList where status equals to UPDATED_STATUS
//        defaultAuctionShouldNotBeFound("status.in=" + UPDATED_STATUS);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByStatusIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where status is not null
//        defaultAuctionShouldBeFound("status.specified=true");
//
//        // Get all the auctionList where status is null
//        defaultAuctionShouldNotBeFound("status.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByProductIdIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where productId equals to DEFAULT_PRODUCT_ID
//        defaultAuctionShouldBeFound("productId.equals=" + DEFAULT_PRODUCT_ID);
//
//        // Get all the auctionList where productId equals to UPDATED_PRODUCT_ID
//        defaultAuctionShouldNotBeFound("productId.equals=" + UPDATED_PRODUCT_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByProductIdIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where productId not equals to DEFAULT_PRODUCT_ID
//        defaultAuctionShouldNotBeFound("productId.notEquals=" + DEFAULT_PRODUCT_ID);
//
//        // Get all the auctionList where productId not equals to UPDATED_PRODUCT_ID
//        defaultAuctionShouldBeFound("productId.notEquals=" + UPDATED_PRODUCT_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByProductIdIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where productId in DEFAULT_PRODUCT_ID or UPDATED_PRODUCT_ID
//        defaultAuctionShouldBeFound("productId.in=" + DEFAULT_PRODUCT_ID + "," + UPDATED_PRODUCT_ID);
//
//        // Get all the auctionList where productId equals to UPDATED_PRODUCT_ID
//        defaultAuctionShouldNotBeFound("productId.in=" + UPDATED_PRODUCT_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByProductIdIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where productId is not null
//        defaultAuctionShouldBeFound("productId.specified=true");
//
//        // Get all the auctionList where productId is null
//        defaultAuctionShouldNotBeFound("productId.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByProductIdIsGreaterThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where productId is greater than or equal to DEFAULT_PRODUCT_ID
//        defaultAuctionShouldBeFound("productId.greaterThanOrEqual=" + DEFAULT_PRODUCT_ID);
//
//        // Get all the auctionList where productId is greater than or equal to UPDATED_PRODUCT_ID
//        defaultAuctionShouldNotBeFound("productId.greaterThanOrEqual=" + UPDATED_PRODUCT_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByProductIdIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where productId is less than or equal to DEFAULT_PRODUCT_ID
//        defaultAuctionShouldBeFound("productId.lessThanOrEqual=" + DEFAULT_PRODUCT_ID);
//
//        // Get all the auctionList where productId is less than or equal to SMALLER_PRODUCT_ID
//        defaultAuctionShouldNotBeFound("productId.lessThanOrEqual=" + SMALLER_PRODUCT_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByProductIdIsLessThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where productId is less than DEFAULT_PRODUCT_ID
//        defaultAuctionShouldNotBeFound("productId.lessThan=" + DEFAULT_PRODUCT_ID);
//
//        // Get all the auctionList where productId is less than UPDATED_PRODUCT_ID
//        defaultAuctionShouldBeFound("productId.lessThan=" + UPDATED_PRODUCT_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByProductIdIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where productId is greater than DEFAULT_PRODUCT_ID
//        defaultAuctionShouldNotBeFound("productId.greaterThan=" + DEFAULT_PRODUCT_ID);
//
//        // Get all the auctionList where productId is greater than SMALLER_PRODUCT_ID
//        defaultAuctionShouldBeFound("productId.greaterThan=" + SMALLER_PRODUCT_ID);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByGateOpeningTimeIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where gateOpeningTime equals to DEFAULT_GATE_OPENING_TIME
//        defaultAuctionShouldBeFound("gateOpeningTime.equals=" + DEFAULT_GATE_OPENING_TIME);
//
//        // Get all the auctionList where gateOpeningTime equals to UPDATED_GATE_OPENING_TIME
//        defaultAuctionShouldNotBeFound("gateOpeningTime.equals=" + UPDATED_GATE_OPENING_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByGateOpeningTimeIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where gateOpeningTime not equals to DEFAULT_GATE_OPENING_TIME
//        defaultAuctionShouldNotBeFound("gateOpeningTime.notEquals=" + DEFAULT_GATE_OPENING_TIME);
//
//        // Get all the auctionList where gateOpeningTime not equals to UPDATED_GATE_OPENING_TIME
//        defaultAuctionShouldBeFound("gateOpeningTime.notEquals=" + UPDATED_GATE_OPENING_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByGateOpeningTimeIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where gateOpeningTime in DEFAULT_GATE_OPENING_TIME or UPDATED_GATE_OPENING_TIME
//        defaultAuctionShouldBeFound("gateOpeningTime.in=" + DEFAULT_GATE_OPENING_TIME + "," + UPDATED_GATE_OPENING_TIME);
//
//        // Get all the auctionList where gateOpeningTime equals to UPDATED_GATE_OPENING_TIME
//        defaultAuctionShouldNotBeFound("gateOpeningTime.in=" + UPDATED_GATE_OPENING_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByGateOpeningTimeIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where gateOpeningTime is not null
//        defaultAuctionShouldBeFound("gateOpeningTime.specified=true");
//
//        // Get all the auctionList where gateOpeningTime is null
//        defaultAuctionShouldNotBeFound("gateOpeningTime.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByGateClosureTimeIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where gateClosureTime equals to DEFAULT_GATE_CLOSURE_TIME
//        defaultAuctionShouldBeFound("gateClosureTime.equals=" + DEFAULT_GATE_CLOSURE_TIME);
//
//        // Get all the auctionList where gateClosureTime equals to UPDATED_GATE_CLOSURE_TIME
//        defaultAuctionShouldNotBeFound("gateClosureTime.equals=" + UPDATED_GATE_CLOSURE_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByGateClosureTimeIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where gateClosureTime not equals to DEFAULT_GATE_CLOSURE_TIME
//        defaultAuctionShouldNotBeFound("gateClosureTime.notEquals=" + DEFAULT_GATE_CLOSURE_TIME);
//
//        // Get all the auctionList where gateClosureTime not equals to UPDATED_GATE_CLOSURE_TIME
//        defaultAuctionShouldBeFound("gateClosureTime.notEquals=" + UPDATED_GATE_CLOSURE_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByGateClosureTimeIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where gateClosureTime in DEFAULT_GATE_CLOSURE_TIME or UPDATED_GATE_CLOSURE_TIME
//        defaultAuctionShouldBeFound("gateClosureTime.in=" + DEFAULT_GATE_CLOSURE_TIME + "," + UPDATED_GATE_CLOSURE_TIME);
//
//        // Get all the auctionList where gateClosureTime equals to UPDATED_GATE_CLOSURE_TIME
//        defaultAuctionShouldNotBeFound("gateClosureTime.in=" + UPDATED_GATE_CLOSURE_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByGateClosureTimeIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where gateClosureTime is not null
//        defaultAuctionShouldBeFound("gateClosureTime.specified=true");
//
//        // Get all the auctionList where gateClosureTime is null
//        defaultAuctionShouldNotBeFound("gateClosureTime.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyGateOpeningTimeIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyGateOpeningTime equals to DEFAULT_ENERGY_GATE_OPENING_TIME
//        defaultAuctionShouldBeFound("energyGateOpeningTime.equals=" + DEFAULT_ENERGY_GATE_OPENING_TIME);
//
//        // Get all the auctionList where energyGateOpeningTime equals to UPDATED_ENERGY_GATE_OPENING_TIME
//        defaultAuctionShouldNotBeFound("energyGateOpeningTime.equals=" + UPDATED_ENERGY_GATE_OPENING_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyGateOpeningTimeIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyGateOpeningTime not equals to DEFAULT_ENERGY_GATE_OPENING_TIME
//        defaultAuctionShouldNotBeFound("energyGateOpeningTime.notEquals=" + DEFAULT_ENERGY_GATE_OPENING_TIME);
//
//        // Get all the auctionList where energyGateOpeningTime not equals to UPDATED_ENERGY_GATE_OPENING_TIME
//        defaultAuctionShouldBeFound("energyGateOpeningTime.notEquals=" + UPDATED_ENERGY_GATE_OPENING_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyGateOpeningTimeIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyGateOpeningTime in DEFAULT_ENERGY_GATE_OPENING_TIME or UPDATED_ENERGY_GATE_OPENING_TIME
//        defaultAuctionShouldBeFound("energyGateOpeningTime.in=" + DEFAULT_ENERGY_GATE_OPENING_TIME + "," + UPDATED_ENERGY_GATE_OPENING_TIME);
//
//        // Get all the auctionList where energyGateOpeningTime equals to UPDATED_ENERGY_GATE_OPENING_TIME
//        defaultAuctionShouldNotBeFound("energyGateOpeningTime.in=" + UPDATED_ENERGY_GATE_OPENING_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyGateOpeningTimeIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyGateOpeningTime is not null
//        defaultAuctionShouldBeFound("energyGateOpeningTime.specified=true");
//
//        // Get all the auctionList where energyGateOpeningTime is null
//        defaultAuctionShouldNotBeFound("energyGateOpeningTime.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyGateClosureTimeIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyGateClosureTime equals to DEFAULT_ENERGY_GATE_CLOSURE_TIME
//        defaultAuctionShouldBeFound("energyGateClosureTime.equals=" + DEFAULT_ENERGY_GATE_CLOSURE_TIME);
//
//        // Get all the auctionList where energyGateClosureTime equals to UPDATED_ENERGY_GATE_CLOSURE_TIME
//        defaultAuctionShouldNotBeFound("energyGateClosureTime.equals=" + UPDATED_ENERGY_GATE_CLOSURE_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyGateClosureTimeIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyGateClosureTime not equals to DEFAULT_ENERGY_GATE_CLOSURE_TIME
//        defaultAuctionShouldNotBeFound("energyGateClosureTime.notEquals=" + DEFAULT_ENERGY_GATE_CLOSURE_TIME);
//
//        // Get all the auctionList where energyGateClosureTime not equals to UPDATED_ENERGY_GATE_CLOSURE_TIME
//        defaultAuctionShouldBeFound("energyGateClosureTime.notEquals=" + UPDATED_ENERGY_GATE_CLOSURE_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyGateClosureTimeIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyGateClosureTime in DEFAULT_ENERGY_GATE_CLOSURE_TIME or UPDATED_ENERGY_GATE_CLOSURE_TIME
//        defaultAuctionShouldBeFound("energyGateClosureTime.in=" + DEFAULT_ENERGY_GATE_CLOSURE_TIME + "," + UPDATED_ENERGY_GATE_CLOSURE_TIME);
//
//        // Get all the auctionList where energyGateClosureTime equals to UPDATED_ENERGY_GATE_CLOSURE_TIME
//        defaultAuctionShouldNotBeFound("energyGateClosureTime.in=" + UPDATED_ENERGY_GATE_CLOSURE_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyGateClosureTimeIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyGateClosureTime is not null
//        defaultAuctionShouldBeFound("energyGateClosureTime.specified=true");
//
//        // Get all the auctionList where energyGateClosureTime is null
//        defaultAuctionShouldNotBeFound("energyGateClosureTime.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityGateOpeningTimeIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityGateOpeningTime equals to DEFAULT_CAPACITY_GATE_OPENING_TIME
//        defaultAuctionShouldBeFound("capacityGateOpeningTime.equals=" + DEFAULT_CAPACITY_GATE_OPENING_TIME);
//
//        // Get all the auctionList where capacityGateOpeningTime equals to UPDATED_CAPACITY_GATE_OPENING_TIME
//        defaultAuctionShouldNotBeFound("capacityGateOpeningTime.equals=" + UPDATED_CAPACITY_GATE_OPENING_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityGateOpeningTimeIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityGateOpeningTime not equals to DEFAULT_CAPACITY_GATE_OPENING_TIME
//        defaultAuctionShouldNotBeFound("capacityGateOpeningTime.notEquals=" + DEFAULT_CAPACITY_GATE_OPENING_TIME);
//
//        // Get all the auctionList where capacityGateOpeningTime not equals to UPDATED_CAPACITY_GATE_OPENING_TIME
//        defaultAuctionShouldBeFound("capacityGateOpeningTime.notEquals=" + UPDATED_CAPACITY_GATE_OPENING_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityGateOpeningTimeIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityGateOpeningTime in DEFAULT_CAPACITY_GATE_OPENING_TIME or UPDATED_CAPACITY_GATE_OPENING_TIME
//        defaultAuctionShouldBeFound("capacityGateOpeningTime.in=" + DEFAULT_CAPACITY_GATE_OPENING_TIME + "," + UPDATED_CAPACITY_GATE_OPENING_TIME);
//
//        // Get all the auctionList where capacityGateOpeningTime equals to UPDATED_CAPACITY_GATE_OPENING_TIME
//        defaultAuctionShouldNotBeFound("capacityGateOpeningTime.in=" + UPDATED_CAPACITY_GATE_OPENING_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityGateOpeningTimeIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityGateOpeningTime is not null
//        defaultAuctionShouldBeFound("capacityGateOpeningTime.specified=true");
//
//        // Get all the auctionList where capacityGateOpeningTime is null
//        defaultAuctionShouldNotBeFound("capacityGateOpeningTime.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityGateClosureTimeIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityGateClosureTime equals to DEFAULT_CAPACITY_GATE_CLOSURE_TIME
//        defaultAuctionShouldBeFound("capacityGateClosureTime.equals=" + DEFAULT_CAPACITY_GATE_CLOSURE_TIME);
//
//        // Get all the auctionList where capacityGateClosureTime equals to UPDATED_CAPACITY_GATE_CLOSURE_TIME
//        defaultAuctionShouldNotBeFound("capacityGateClosureTime.equals=" + UPDATED_CAPACITY_GATE_CLOSURE_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityGateClosureTimeIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityGateClosureTime not equals to DEFAULT_CAPACITY_GATE_CLOSURE_TIME
//        defaultAuctionShouldNotBeFound("capacityGateClosureTime.notEquals=" + DEFAULT_CAPACITY_GATE_CLOSURE_TIME);
//
//        // Get all the auctionList where capacityGateClosureTime not equals to UPDATED_CAPACITY_GATE_CLOSURE_TIME
//        defaultAuctionShouldBeFound("capacityGateClosureTime.notEquals=" + UPDATED_CAPACITY_GATE_CLOSURE_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityGateClosureTimeIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityGateClosureTime in DEFAULT_CAPACITY_GATE_CLOSURE_TIME or UPDATED_CAPACITY_GATE_CLOSURE_TIME
//        defaultAuctionShouldBeFound("capacityGateClosureTime.in=" + DEFAULT_CAPACITY_GATE_CLOSURE_TIME + "," + UPDATED_CAPACITY_GATE_CLOSURE_TIME);
//
//        // Get all the auctionList where capacityGateClosureTime equals to UPDATED_CAPACITY_GATE_CLOSURE_TIME
//        defaultAuctionShouldNotBeFound("capacityGateClosureTime.in=" + UPDATED_CAPACITY_GATE_CLOSURE_TIME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityGateClosureTimeIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityGateClosureTime is not null
//        defaultAuctionShouldBeFound("capacityGateClosureTime.specified=true");
//
//        // Get all the auctionList where capacityGateClosureTime is null
//        defaultAuctionShouldNotBeFound("capacityGateClosureTime.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredCapacityIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredCapacity equals to DEFAULT_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("minDesiredCapacity.equals=" + DEFAULT_MIN_DESIRED_CAPACITY);
//
//        // Get all the auctionList where minDesiredCapacity equals to UPDATED_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("minDesiredCapacity.equals=" + UPDATED_MIN_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredCapacityIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredCapacity not equals to DEFAULT_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("minDesiredCapacity.notEquals=" + DEFAULT_MIN_DESIRED_CAPACITY);
//
//        // Get all the auctionList where minDesiredCapacity not equals to UPDATED_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("minDesiredCapacity.notEquals=" + UPDATED_MIN_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredCapacityIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredCapacity in DEFAULT_MIN_DESIRED_CAPACITY or UPDATED_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("minDesiredCapacity.in=" + DEFAULT_MIN_DESIRED_CAPACITY + "," + UPDATED_MIN_DESIRED_CAPACITY);
//
//        // Get all the auctionList where minDesiredCapacity equals to UPDATED_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("minDesiredCapacity.in=" + UPDATED_MIN_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredCapacityIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredCapacity is not null
//        defaultAuctionShouldBeFound("minDesiredCapacity.specified=true");
//
//        // Get all the auctionList where minDesiredCapacity is null
//        defaultAuctionShouldNotBeFound("minDesiredCapacity.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredCapacityIsGreaterThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredCapacity is greater than or equal to DEFAULT_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("minDesiredCapacity.greaterThanOrEqual=" + DEFAULT_MIN_DESIRED_CAPACITY);
//
//        // Get all the auctionList where minDesiredCapacity is greater than or equal to UPDATED_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("minDesiredCapacity.greaterThanOrEqual=" + UPDATED_MIN_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredCapacityIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredCapacity is less than or equal to DEFAULT_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("minDesiredCapacity.lessThanOrEqual=" + DEFAULT_MIN_DESIRED_CAPACITY);
//
//        // Get all the auctionList where minDesiredCapacity is less than or equal to SMALLER_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("minDesiredCapacity.lessThanOrEqual=" + SMALLER_MIN_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredCapacityIsLessThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredCapacity is less than DEFAULT_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("minDesiredCapacity.lessThan=" + DEFAULT_MIN_DESIRED_CAPACITY);
//
//        // Get all the auctionList where minDesiredCapacity is less than UPDATED_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("minDesiredCapacity.lessThan=" + UPDATED_MIN_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredCapacityIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredCapacity is greater than DEFAULT_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("minDesiredCapacity.greaterThan=" + DEFAULT_MIN_DESIRED_CAPACITY);
//
//        // Get all the auctionList where minDesiredCapacity is greater than SMALLER_MIN_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("minDesiredCapacity.greaterThan=" + SMALLER_MIN_DESIRED_CAPACITY);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredCapacityIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredCapacity equals to DEFAULT_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("maxDesiredCapacity.equals=" + DEFAULT_MAX_DESIRED_CAPACITY);
//
//        // Get all the auctionList where maxDesiredCapacity equals to UPDATED_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("maxDesiredCapacity.equals=" + UPDATED_MAX_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredCapacityIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredCapacity not equals to DEFAULT_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("maxDesiredCapacity.notEquals=" + DEFAULT_MAX_DESIRED_CAPACITY);
//
//        // Get all the auctionList where maxDesiredCapacity not equals to UPDATED_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("maxDesiredCapacity.notEquals=" + UPDATED_MAX_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredCapacityIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredCapacity in DEFAULT_MAX_DESIRED_CAPACITY or UPDATED_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("maxDesiredCapacity.in=" + DEFAULT_MAX_DESIRED_CAPACITY + "," + UPDATED_MAX_DESIRED_CAPACITY);
//
//        // Get all the auctionList where maxDesiredCapacity equals to UPDATED_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("maxDesiredCapacity.in=" + UPDATED_MAX_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredCapacityIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredCapacity is not null
//        defaultAuctionShouldBeFound("maxDesiredCapacity.specified=true");
//
//        // Get all the auctionList where maxDesiredCapacity is null
//        defaultAuctionShouldNotBeFound("maxDesiredCapacity.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredCapacityIsGreaterThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredCapacity is greater than or equal to DEFAULT_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("maxDesiredCapacity.greaterThanOrEqual=" + DEFAULT_MAX_DESIRED_CAPACITY);
//
//        // Get all the auctionList where maxDesiredCapacity is greater than or equal to UPDATED_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("maxDesiredCapacity.greaterThanOrEqual=" + UPDATED_MAX_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredCapacityIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredCapacity is less than or equal to DEFAULT_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("maxDesiredCapacity.lessThanOrEqual=" + DEFAULT_MAX_DESIRED_CAPACITY);
//
//        // Get all the auctionList where maxDesiredCapacity is less than or equal to SMALLER_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("maxDesiredCapacity.lessThanOrEqual=" + SMALLER_MAX_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredCapacityIsLessThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredCapacity is less than DEFAULT_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("maxDesiredCapacity.lessThan=" + DEFAULT_MAX_DESIRED_CAPACITY);
//
//        // Get all the auctionList where maxDesiredCapacity is less than UPDATED_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("maxDesiredCapacity.lessThan=" + UPDATED_MAX_DESIRED_CAPACITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredCapacityIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredCapacity is greater than DEFAULT_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldNotBeFound("maxDesiredCapacity.greaterThan=" + DEFAULT_MAX_DESIRED_CAPACITY);
//
//        // Get all the auctionList where maxDesiredCapacity is greater than SMALLER_MAX_DESIRED_CAPACITY
//        defaultAuctionShouldBeFound("maxDesiredCapacity.greaterThan=" + SMALLER_MAX_DESIRED_CAPACITY);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredEnergyIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredEnergy equals to DEFAULT_MIN_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("minDesiredEnergy.equals=" + DEFAULT_MIN_DESIRED_ENERGY);
//
//        // Get all the auctionList where minDesiredEnergy equals to UPDATED_MIN_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("minDesiredEnergy.equals=" + UPDATED_MIN_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredEnergyIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredEnergy not equals to DEFAULT_MIN_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("minDesiredEnergy.notEquals=" + DEFAULT_MIN_DESIRED_ENERGY);
//
//        // Get all the auctionList where minDesiredEnergy not equals to UPDATED_MIN_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("minDesiredEnergy.notEquals=" + UPDATED_MIN_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredEnergyIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredEnergy in DEFAULT_MIN_DESIRED_ENERGY or UPDATED_MIN_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("minDesiredEnergy.in=" + DEFAULT_MIN_DESIRED_ENERGY + "," + UPDATED_MIN_DESIRED_ENERGY);
//
//        // Get all the auctionList where minDesiredEnergy equals to UPDATED_MIN_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("minDesiredEnergy.in=" + UPDATED_MIN_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredEnergyIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredEnergy is not null
//        defaultAuctionShouldBeFound("minDesiredEnergy.specified=true");
//
//        // Get all the auctionList where minDesiredEnergy is null
//        defaultAuctionShouldNotBeFound("minDesiredEnergy.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredEnergyIsGreaterThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredEnergy is greater than or equal to DEFAULT_MIN_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("minDesiredEnergy.greaterThanOrEqual=" + DEFAULT_MIN_DESIRED_ENERGY);
//
//        // Get all the auctionList where minDesiredEnergy is greater than or equal to UPDATED_MIN_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("minDesiredEnergy.greaterThanOrEqual=" + UPDATED_MIN_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredEnergyIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredEnergy is less than or equal to DEFAULT_MIN_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("minDesiredEnergy.lessThanOrEqual=" + DEFAULT_MIN_DESIRED_ENERGY);
//
//        // Get all the auctionList where minDesiredEnergy is less than or equal to SMALLER_MIN_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("minDesiredEnergy.lessThanOrEqual=" + SMALLER_MIN_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredEnergyIsLessThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredEnergy is less than DEFAULT_MIN_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("minDesiredEnergy.lessThan=" + DEFAULT_MIN_DESIRED_ENERGY);
//
//        // Get all the auctionList where minDesiredEnergy is less than UPDATED_MIN_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("minDesiredEnergy.lessThan=" + UPDATED_MIN_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMinDesiredEnergyIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where minDesiredEnergy is greater than DEFAULT_MIN_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("minDesiredEnergy.greaterThan=" + DEFAULT_MIN_DESIRED_ENERGY);
//
//        // Get all the auctionList where minDesiredEnergy is greater than SMALLER_MIN_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("minDesiredEnergy.greaterThan=" + SMALLER_MIN_DESIRED_ENERGY);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredEnergyIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredEnergy equals to DEFAULT_MAX_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("maxDesiredEnergy.equals=" + DEFAULT_MAX_DESIRED_ENERGY);
//
//        // Get all the auctionList where maxDesiredEnergy equals to UPDATED_MAX_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("maxDesiredEnergy.equals=" + UPDATED_MAX_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredEnergyIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredEnergy not equals to DEFAULT_MAX_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("maxDesiredEnergy.notEquals=" + DEFAULT_MAX_DESIRED_ENERGY);
//
//        // Get all the auctionList where maxDesiredEnergy not equals to UPDATED_MAX_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("maxDesiredEnergy.notEquals=" + UPDATED_MAX_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredEnergyIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredEnergy in DEFAULT_MAX_DESIRED_ENERGY or UPDATED_MAX_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("maxDesiredEnergy.in=" + DEFAULT_MAX_DESIRED_ENERGY + "," + UPDATED_MAX_DESIRED_ENERGY);
//
//        // Get all the auctionList where maxDesiredEnergy equals to UPDATED_MAX_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("maxDesiredEnergy.in=" + UPDATED_MAX_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredEnergyIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredEnergy is not null
//        defaultAuctionShouldBeFound("maxDesiredEnergy.specified=true");
//
//        // Get all the auctionList where maxDesiredEnergy is null
//        defaultAuctionShouldNotBeFound("maxDesiredEnergy.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredEnergyIsGreaterThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredEnergy is greater than or equal to DEFAULT_MAX_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("maxDesiredEnergy.greaterThanOrEqual=" + DEFAULT_MAX_DESIRED_ENERGY);
//
//        // Get all the auctionList where maxDesiredEnergy is greater than or equal to UPDATED_MAX_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("maxDesiredEnergy.greaterThanOrEqual=" + UPDATED_MAX_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredEnergyIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredEnergy is less than or equal to DEFAULT_MAX_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("maxDesiredEnergy.lessThanOrEqual=" + DEFAULT_MAX_DESIRED_ENERGY);
//
//        // Get all the auctionList where maxDesiredEnergy is less than or equal to SMALLER_MAX_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("maxDesiredEnergy.lessThanOrEqual=" + SMALLER_MAX_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredEnergyIsLessThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredEnergy is less than DEFAULT_MAX_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("maxDesiredEnergy.lessThan=" + DEFAULT_MAX_DESIRED_ENERGY);
//
//        // Get all the auctionList where maxDesiredEnergy is less than UPDATED_MAX_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("maxDesiredEnergy.lessThan=" + UPDATED_MAX_DESIRED_ENERGY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByMaxDesiredEnergyIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where maxDesiredEnergy is greater than DEFAULT_MAX_DESIRED_ENERGY
//        defaultAuctionShouldNotBeFound("maxDesiredEnergy.greaterThan=" + DEFAULT_MAX_DESIRED_ENERGY);
//
//        // Get all the auctionList where maxDesiredEnergy is greater than SMALLER_MAX_DESIRED_ENERGY
//        defaultAuctionShouldBeFound("maxDesiredEnergy.greaterThan=" + SMALLER_MAX_DESIRED_ENERGY);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityFromIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityFrom equals to DEFAULT_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("availabilityFrom.equals=" + DEFAULT_AVAILABILITY_FROM);
//
//        // Get all the auctionList where availabilityFrom equals to UPDATED_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("availabilityFrom.equals=" + UPDATED_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityFromIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityFrom not equals to DEFAULT_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("availabilityFrom.notEquals=" + DEFAULT_AVAILABILITY_FROM);
//
//        // Get all the auctionList where availabilityFrom not equals to UPDATED_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("availabilityFrom.notEquals=" + UPDATED_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityFromIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityFrom in DEFAULT_AVAILABILITY_FROM or UPDATED_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("availabilityFrom.in=" + DEFAULT_AVAILABILITY_FROM + "," + UPDATED_AVAILABILITY_FROM);
//
//        // Get all the auctionList where availabilityFrom equals to UPDATED_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("availabilityFrom.in=" + UPDATED_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityFromIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityFrom is not null
//        defaultAuctionShouldBeFound("availabilityFrom.specified=true");
//
//        // Get all the auctionList where availabilityFrom is null
//        defaultAuctionShouldNotBeFound("availabilityFrom.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityFromIsGreaterThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityFrom is greater than or equal to DEFAULT_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("availabilityFrom.greaterThanOrEqual=" + DEFAULT_AVAILABILITY_FROM);
//
//        // Get all the auctionList where availabilityFrom is greater than or equal to UPDATED_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("availabilityFrom.greaterThanOrEqual=" + UPDATED_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityFromIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityFrom is less than or equal to DEFAULT_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("availabilityFrom.lessThanOrEqual=" + DEFAULT_AVAILABILITY_FROM);
//
//        // Get all the auctionList where availabilityFrom is less than or equal to SMALLER_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("availabilityFrom.lessThanOrEqual=" + UPDATED_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityFromIsLessThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityFrom is less than DEFAULT_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("availabilityFrom.lessThan=" + DEFAULT_AVAILABILITY_FROM);
//
//        // Get all the auctionList where availabilityFrom is less than UPDATED_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("availabilityFrom.lessThan=" + UPDATED_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityFromIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityFrom is greater than DEFAULT_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("availabilityFrom.greaterThan=" + DEFAULT_AVAILABILITY_FROM);
//
//        // Get all the auctionList where availabilityFrom is greater than SMALLER_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("availabilityFrom.greaterThan=" + UPDATED_AVAILABILITY_FROM);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityToIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityTo equals to DEFAULT_AVAILABILITY_TO
//        defaultAuctionShouldBeFound("availabilityTo.equals=" + DEFAULT_AVAILABILITY_TO);
//
//        // Get all the auctionList where availabilityTo equals to UPDATED_AVAILABILITY_TO
//        defaultAuctionShouldNotBeFound("availabilityTo.equals=" + UPDATED_AVAILABILITY_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityToIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityTo not equals to DEFAULT_AVAILABILITY_TO
//        defaultAuctionShouldNotBeFound("availabilityTo.notEquals=" + DEFAULT_AVAILABILITY_TO);
//
//        // Get all the auctionList where availabilityTo not equals to UPDATED_AVAILABILITY_TO
//        defaultAuctionShouldBeFound("availabilityTo.notEquals=" + UPDATED_AVAILABILITY_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityToIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityTo in DEFAULT_AVAILABILITY_TO or UPDATED_AVAILABILITY_TO
//        defaultAuctionShouldBeFound("availabilityTo.in=" + DEFAULT_AVAILABILITY_TO + "," + UPDATED_AVAILABILITY_TO);
//
//        // Get all the auctionList where availabilityTo equals to UPDATED_AVAILABILITY_TO
//        defaultAuctionShouldNotBeFound("availabilityTo.in=" + UPDATED_AVAILABILITY_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByAvailabilityToIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where availabilityTo is not null
//        defaultAuctionShouldBeFound("availabilityTo.specified=true");
//
//        // Get all the auctionList where availabilityTo is null
//        defaultAuctionShouldNotBeFound("availabilityTo.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityAvailabilityFromIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityAvailabilityFrom equals to DEFAULT_CAPACITY_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("capacityAvailabilityFrom.equals=" + DEFAULT_CAPACITY_AVAILABILITY_FROM);
//
//        // Get all the auctionList where capacityAvailabilityFrom equals to UPDATED_CAPACITY_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("capacityAvailabilityFrom.equals=" + UPDATED_CAPACITY_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityAvailabilityFromIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityAvailabilityFrom not equals to DEFAULT_CAPACITY_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("capacityAvailabilityFrom.notEquals=" + DEFAULT_CAPACITY_AVAILABILITY_FROM);
//
//        // Get all the auctionList where capacityAvailabilityFrom not equals to UPDATED_CAPACITY_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("capacityAvailabilityFrom.notEquals=" + UPDATED_CAPACITY_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityAvailabilityFromIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityAvailabilityFrom in DEFAULT_CAPACITY_AVAILABILITY_FROM or UPDATED_CAPACITY_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("capacityAvailabilityFrom.in=" + DEFAULT_CAPACITY_AVAILABILITY_FROM + "," + UPDATED_CAPACITY_AVAILABILITY_FROM);
//
//        // Get all the auctionList where capacityAvailabilityFrom equals to UPDATED_CAPACITY_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("capacityAvailabilityFrom.in=" + UPDATED_CAPACITY_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityAvailabilityFromIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityAvailabilityFrom is not null
//        defaultAuctionShouldBeFound("capacityAvailabilityFrom.specified=true");
//
//        // Get all the auctionList where capacityAvailabilityFrom is null
//        defaultAuctionShouldNotBeFound("capacityAvailabilityFrom.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityAvailabilityToIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityAvailabilityTo equals to DEFAULT_CAPACITY_AVAILABILITY_TO
//        defaultAuctionShouldBeFound("capacityAvailabilityTo.equals=" + DEFAULT_CAPACITY_AVAILABILITY_TO);
//
//        // Get all the auctionList where capacityAvailabilityTo equals to UPDATED_CAPACITY_AVAILABILITY_TO
//        defaultAuctionShouldNotBeFound("capacityAvailabilityTo.equals=" + UPDATED_CAPACITY_AVAILABILITY_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityAvailabilityToIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityAvailabilityTo not equals to DEFAULT_CAPACITY_AVAILABILITY_TO
//        defaultAuctionShouldNotBeFound("capacityAvailabilityTo.notEquals=" + DEFAULT_CAPACITY_AVAILABILITY_TO);
//
//        // Get all the auctionList where capacityAvailabilityTo not equals to UPDATED_CAPACITY_AVAILABILITY_TO
//        defaultAuctionShouldBeFound("capacityAvailabilityTo.notEquals=" + UPDATED_CAPACITY_AVAILABILITY_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityAvailabilityToIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityAvailabilityTo in DEFAULT_CAPACITY_AVAILABILITY_TO or UPDATED_CAPACITY_AVAILABILITY_TO
//        defaultAuctionShouldBeFound("capacityAvailabilityTo.in=" + DEFAULT_CAPACITY_AVAILABILITY_TO + "," + UPDATED_CAPACITY_AVAILABILITY_TO);
//
//        // Get all the auctionList where capacityAvailabilityTo equals to UPDATED_CAPACITY_AVAILABILITY_TO
//        defaultAuctionShouldNotBeFound("capacityAvailabilityTo.in=" + UPDATED_CAPACITY_AVAILABILITY_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByCapacityAvailabilityToIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where capacityAvailabilityTo is not null
//        defaultAuctionShouldBeFound("capacityAvailabilityTo.specified=true");
//
//        // Get all the auctionList where capacityAvailabilityTo is null
//        defaultAuctionShouldNotBeFound("capacityAvailabilityTo.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityFromIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityFrom equals to DEFAULT_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("energyAvailabilityFrom.equals=" + DEFAULT_ENERGY_AVAILABILITY_FROM);
//
//        // Get all the auctionList where energyAvailabilityFrom equals to UPDATED_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("energyAvailabilityFrom.equals=" + UPDATED_ENERGY_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityFromIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityFrom not equals to DEFAULT_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("energyAvailabilityFrom.notEquals=" + DEFAULT_ENERGY_AVAILABILITY_FROM);
//
//        // Get all the auctionList where energyAvailabilityFrom not equals to UPDATED_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("energyAvailabilityFrom.notEquals=" + UPDATED_ENERGY_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityFromIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityFrom in DEFAULT_ENERGY_AVAILABILITY_FROM or UPDATED_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("energyAvailabilityFrom.in=" + DEFAULT_ENERGY_AVAILABILITY_FROM + "," + UPDATED_ENERGY_AVAILABILITY_FROM);
//
//        // Get all the auctionList where energyAvailabilityFrom equals to UPDATED_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("energyAvailabilityFrom.in=" + UPDATED_ENERGY_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityFromIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityFrom is not null
//        defaultAuctionShouldBeFound("energyAvailabilityFrom.specified=true");
//
//        // Get all the auctionList where energyAvailabilityFrom is null
//        defaultAuctionShouldNotBeFound("energyAvailabilityFrom.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityFromIsGreaterThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityFrom is greater than or equal to DEFAULT_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("energyAvailabilityFrom.greaterThanOrEqual=" + DEFAULT_ENERGY_AVAILABILITY_FROM);
//
//        // Get all the auctionList where energyAvailabilityFrom is greater than or equal to UPDATED_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("energyAvailabilityFrom.greaterThanOrEqual=" + UPDATED_ENERGY_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityFromIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityFrom is less than or equal to DEFAULT_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("energyAvailabilityFrom.lessThanOrEqual=" + DEFAULT_ENERGY_AVAILABILITY_FROM);
//
//        // Get all the auctionList where energyAvailabilityFrom is less than or equal to SMALLER_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("energyAvailabilityFrom.lessThanOrEqual=" + UPDATED_ENERGY_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityFromIsLessThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityFrom is less than DEFAULT_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("energyAvailabilityFrom.lessThan=" + DEFAULT_ENERGY_AVAILABILITY_FROM);
//
//        // Get all the auctionList where energyAvailabilityFrom is less than UPDATED_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("energyAvailabilityFrom.lessThan=" + UPDATED_ENERGY_AVAILABILITY_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityFromIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityFrom is greater than DEFAULT_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldNotBeFound("energyAvailabilityFrom.greaterThan=" + DEFAULT_ENERGY_AVAILABILITY_FROM);
//
//        // Get all the auctionList where energyAvailabilityFrom is greater than SMALLER_ENERGY_AVAILABILITY_FROM
//        defaultAuctionShouldBeFound("energyAvailabilityFrom.greaterThan=" + UPDATED_ENERGY_AVAILABILITY_FROM);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityToIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityTo equals to DEFAULT_ENERGY_AVAILABILITY_TO
//        defaultAuctionShouldBeFound("energyAvailabilityTo.equals=" + DEFAULT_ENERGY_AVAILABILITY_TO);
//
//        // Get all the auctionList where energyAvailabilityTo equals to UPDATED_ENERGY_AVAILABILITY_TO
//        defaultAuctionShouldNotBeFound("energyAvailabilityTo.equals=" + UPDATED_ENERGY_AVAILABILITY_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityToIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityTo not equals to DEFAULT_ENERGY_AVAILABILITY_TO
//        defaultAuctionShouldNotBeFound("energyAvailabilityTo.notEquals=" + DEFAULT_ENERGY_AVAILABILITY_TO);
//
//        // Get all the auctionList where energyAvailabilityTo not equals to UPDATED_ENERGY_AVAILABILITY_TO
//        defaultAuctionShouldBeFound("energyAvailabilityTo.notEquals=" + UPDATED_ENERGY_AVAILABILITY_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityToIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityTo in DEFAULT_ENERGY_AVAILABILITY_TO or UPDATED_ENERGY_AVAILABILITY_TO
//        defaultAuctionShouldBeFound("energyAvailabilityTo.in=" + DEFAULT_ENERGY_AVAILABILITY_TO + "," + UPDATED_ENERGY_AVAILABILITY_TO);
//
//        // Get all the auctionList where energyAvailabilityTo equals to UPDATED_ENERGY_AVAILABILITY_TO
//        defaultAuctionShouldNotBeFound("energyAvailabilityTo.in=" + UPDATED_ENERGY_AVAILABILITY_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByEnergyAvailabilityToIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where energyAvailabilityTo is not null
//        defaultAuctionShouldBeFound("energyAvailabilityTo.specified=true");
//
//        // Get all the auctionList where energyAvailabilityTo is null
//        defaultAuctionShouldNotBeFound("energyAvailabilityTo.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByVersionIsEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where version equals to DEFAULT_VERSION
//        defaultAuctionShouldBeFound("version.equals=" + DEFAULT_VERSION);
//
//        // Get all the auctionList where version equals to UPDATED_VERSION
//        defaultAuctionShouldNotBeFound("version.equals=" + UPDATED_VERSION);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByVersionIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where version not equals to DEFAULT_VERSION
//        defaultAuctionShouldNotBeFound("version.notEquals=" + DEFAULT_VERSION);
//
//        // Get all the auctionList where version not equals to UPDATED_VERSION
//        defaultAuctionShouldBeFound("version.notEquals=" + UPDATED_VERSION);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByVersionIsInShouldWork() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where version in DEFAULT_VERSION or UPDATED_VERSION
//        defaultAuctionShouldBeFound("version.in=" + DEFAULT_VERSION + "," + UPDATED_VERSION);
//
//        // Get all the auctionList where version equals to UPDATED_VERSION
//        defaultAuctionShouldNotBeFound("version.in=" + UPDATED_VERSION);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByVersionIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where version is not null
//        defaultAuctionShouldBeFound("version.specified=true");
//
//        // Get all the auctionList where version is null
//        defaultAuctionShouldNotBeFound("version.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByVersionIsGreaterThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where version is greater than or equal to DEFAULT_VERSION
//        defaultAuctionShouldBeFound("version.greaterThanOrEqual=" + DEFAULT_VERSION);
//
//        // Get all the auctionList where version is greater than or equal to UPDATED_VERSION
//        defaultAuctionShouldNotBeFound("version.greaterThanOrEqual=" + UPDATED_VERSION);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByVersionIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where version is less than or equal to DEFAULT_VERSION
//        defaultAuctionShouldBeFound("version.lessThanOrEqual=" + DEFAULT_VERSION);
//
//        // Get all the auctionList where version is less than or equal to SMALLER_VERSION
//        defaultAuctionShouldNotBeFound("version.lessThanOrEqual=" + SMALLER_VERSION);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByVersionIsLessThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where version is less than DEFAULT_VERSION
//        defaultAuctionShouldNotBeFound("version.lessThan=" + DEFAULT_VERSION);
//
//        // Get all the auctionList where version is less than UPDATED_VERSION
//        defaultAuctionShouldBeFound("version.lessThan=" + UPDATED_VERSION);
//    }
//
//    @Test
//    @Transactional
//    public void getAllAuctionsByVersionIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        // Get all the auctionList where version is greater than DEFAULT_VERSION
//        defaultAuctionShouldNotBeFound("version.greaterThan=" + DEFAULT_VERSION);
//
//        // Get all the auctionList where version is greater than SMALLER_VERSION
//        defaultAuctionShouldBeFound("version.greaterThan=" + SMALLER_VERSION);
//    }
//
//    /**
//     * Executes the search, and checks that the default entity is returned.
//     */
//    private void defaultAuctionShouldBeFound(String filter) throws Exception {
//        restAuctionMockMvc.perform(get("/api/auctions?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.[*].id").value(hasItem(auctionEntity.getId().intValue())))
//            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
//            .andExpect(jsonPath("$.[*].auctionType").value(hasItem(DEFAULT_AUCTION_TYPE.toString())))
//            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
//            .andExpect(jsonPath("$.[*].productId").value(hasItem(DEFAULT_PRODUCT_ID)))
//            .andExpect(jsonPath("$.[*].gateOpeningTime").value(hasItem(DEFAULT_GATE_OPENING_TIME.toString())))
//            .andExpect(jsonPath("$.[*].gateClosureTime").value(hasItem(DEFAULT_GATE_CLOSURE_TIME.toString())))
//            .andExpect(jsonPath("$.[*].energyGateOpeningTime").value(hasItem(DEFAULT_ENERGY_GATE_OPENING_TIME.toString())))
//            .andExpect(jsonPath("$.[*].energyGateClosureTime").value(hasItem(DEFAULT_ENERGY_GATE_CLOSURE_TIME.toString())))
//            .andExpect(jsonPath("$.[*].capacityGateOpeningTime").value(hasItem(DEFAULT_CAPACITY_GATE_OPENING_TIME.toString())))
//            .andExpect(jsonPath("$.[*].capacityGateClosureTime").value(hasItem(DEFAULT_CAPACITY_GATE_CLOSURE_TIME.toString())))
//            .andExpect(jsonPath("$.[*].minDesiredCapacity").value(hasItem(DEFAULT_MIN_DESIRED_CAPACITY.intValue())))
//            .andExpect(jsonPath("$.[*].maxDesiredCapacity").value(hasItem(DEFAULT_MAX_DESIRED_CAPACITY.intValue())))
//            .andExpect(jsonPath("$.[*].minDesiredEnergy").value(hasItem(DEFAULT_MIN_DESIRED_ENERGY.intValue())))
//            .andExpect(jsonPath("$.[*].maxDesiredEnergy").value(hasItem(DEFAULT_MAX_DESIRED_ENERGY.intValue())))
//            .andExpect(jsonPath("$.[*].availabilityFrom").value(hasItem(DEFAULT_AVAILABILITY_FROM.toString())))
//            .andExpect(jsonPath("$.[*].availabilityTo").value(hasItem(DEFAULT_AVAILABILITY_TO.toString())))
//            .andExpect(jsonPath("$.[*].capacityAvailabilityFrom").value(hasItem(DEFAULT_CAPACITY_AVAILABILITY_FROM.toString())))
//            .andExpect(jsonPath("$.[*].capacityAvailabilityTo").value(hasItem(DEFAULT_CAPACITY_AVAILABILITY_TO.toString())))
//            .andExpect(jsonPath("$.[*].energyAvailabilityFrom").value(hasItem(DEFAULT_ENERGY_AVAILABILITY_FROM.toString())))
//            .andExpect(jsonPath("$.[*].energyAvailabilityTo").value(hasItem(DEFAULT_ENERGY_AVAILABILITY_TO.toString())))
//            .andExpect(jsonPath("$.[*].version").value(hasItem(DEFAULT_VERSION.intValue())));
//
//        // Check, that the count call also returns 1
//        restAuctionMockMvc.perform(get("/api/auctions/count?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(content().string("1"));
//    }
//
//    /**
//     * Executes the search, and checks that the default entity is not returned.
//     */
//    private void defaultAuctionShouldNotBeFound(String filter) throws Exception {
//        restAuctionMockMvc.perform(get("/api/auctions?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$").isArray())
//            .andExpect(jsonPath("$").isEmpty());
//
//        // Check, that the count call also returns 0
//        restAuctionMockMvc.perform(get("/api/auctions/count?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(content().string("0"));
//    }
//
//    @Test
//    @Transactional
//    public void getNonExistingAuction() throws Exception {
//        // Get the auction
//        restAuctionMockMvc.perform(get("/api/auctions/{id}", Long.MAX_VALUE))
//            .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @Transactional
//    public void updateAuction() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        int databaseSizeBeforeUpdate = auctionRepository.findAll().size();
//
//        // Update the auction
//        AuctionEntity updatedAuctionEntity = auctionRepository.findById(auctionEntity.getId()).get();
//        // Disconnect from session so that the updates on updatedAuctionEntity are not directly saved in db
//        em.detach(updatedAuctionEntity);
//        updatedAuctionEntity.setName(UPDATED_NAME);
//        updatedAuctionEntity.setAuctionType(UPDATED_AUCTION_TYPE);
//        updatedAuctionEntity.setStatus(UPDATED_STATUS);
//        updatedAuctionEntity.setProductId(UPDATED_PRODUCT_ID);
//        updatedAuctionEntity.setGateOpeningTime(UPDATED_GATE_OPENING_TIME);
//        updatedAuctionEntity.setGateClosureTime(UPDATED_GATE_CLOSURE_TIME);
//        updatedAuctionEntity.setEnergyGateOpeningTime(UPDATED_ENERGY_GATE_OPENING_TIME);
//        updatedAuctionEntity.setEnergyGateClosureTime(UPDATED_ENERGY_GATE_CLOSURE_TIME);
//        updatedAuctionEntity.setCapacityGateOpeningTime(UPDATED_CAPACITY_GATE_OPENING_TIME);
//        updatedAuctionEntity.setCapacityGateClosureTime(UPDATED_CAPACITY_GATE_CLOSURE_TIME);
//        updatedAuctionEntity.setMinDesiredCapacity(UPDATED_MIN_DESIRED_CAPACITY);
//        updatedAuctionEntity.setMaxDesiredCapacity(UPDATED_MAX_DESIRED_CAPACITY);
//        updatedAuctionEntity.setMinDesiredEnergy(UPDATED_MIN_DESIRED_ENERGY);
//        updatedAuctionEntity.setMaxDesiredEnergy(UPDATED_MAX_DESIRED_ENERGY);
//        updatedAuctionEntity.setAvailabilityFrom(UPDATED_AVAILABILITY_FROM);
//        updatedAuctionEntity.setAvailabilityTo(UPDATED_AVAILABILITY_TO);
//        updatedAuctionEntity.setCapacityAvailabilityFrom(UPDATED_CAPACITY_AVAILABILITY_FROM);
//        updatedAuctionEntity.setCapacityAvailabilityTo(UPDATED_CAPACITY_AVAILABILITY_TO);
//        updatedAuctionEntity.setEnergyAvailabilityFrom(UPDATED_ENERGY_AVAILABILITY_FROM);
//        updatedAuctionEntity.setEnergyAvailabilityTo(UPDATED_ENERGY_AVAILABILITY_TO);
//        updatedAuctionEntity.setVersion(UPDATED_VERSION);
//        AuctionDTO auctionDTO = auctionMapper.toDto(updatedAuctionEntity);
//
//        restAuctionMockMvc.perform(put("/api/auctions")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(TestUtil.convertObjectToJsonBytes(auctionDTO)))
//            .andExpect(status().isOk());
//
//        // Validate the Auction in the database
//        List<AuctionEntity> auctionList = auctionRepository.findAll();
//        assertThat(auctionList).hasSize(databaseSizeBeforeUpdate);
//        AuctionEntity testAuction = auctionList.get(auctionList.size() - 1);
//        assertThat(testAuction.getName()).isEqualTo(UPDATED_NAME);
//        assertThat(testAuction.getAuctionType()).isEqualTo(UPDATED_AUCTION_TYPE);
//        assertThat(testAuction.getStatus()).isEqualTo(UPDATED_STATUS);
//        assertThat(testAuction.getProductId()).isEqualTo(UPDATED_PRODUCT_ID);
//        assertThat(testAuction.getGateOpeningTime()).isEqualTo(UPDATED_GATE_OPENING_TIME);
//        assertThat(testAuction.getGateClosureTime()).isEqualTo(UPDATED_GATE_CLOSURE_TIME);
//        assertThat(testAuction.getEnergyGateOpeningTime()).isEqualTo(UPDATED_ENERGY_GATE_OPENING_TIME);
//        assertThat(testAuction.getEnergyGateClosureTime()).isEqualTo(UPDATED_ENERGY_GATE_CLOSURE_TIME);
//        assertThat(testAuction.getCapacityGateOpeningTime()).isEqualTo(UPDATED_CAPACITY_GATE_OPENING_TIME);
//        assertThat(testAuction.getCapacityGateClosureTime()).isEqualTo(UPDATED_CAPACITY_GATE_CLOSURE_TIME);
//        assertThat(testAuction.getMinDesiredCapacity()).isEqualTo(UPDATED_MIN_DESIRED_CAPACITY);
//        assertThat(testAuction.getMaxDesiredCapacity()).isEqualTo(UPDATED_MAX_DESIRED_CAPACITY);
//        assertThat(testAuction.getMinDesiredEnergy()).isEqualTo(UPDATED_MIN_DESIRED_ENERGY);
//        assertThat(testAuction.getMaxDesiredEnergy()).isEqualTo(UPDATED_MAX_DESIRED_ENERGY);
//        assertThat(testAuction.getAvailabilityFrom()).isEqualTo(UPDATED_AVAILABILITY_FROM);
//        assertThat(testAuction.getAvailabilityTo()).isEqualTo(UPDATED_AVAILABILITY_TO);
//        assertThat(testAuction.getCapacityAvailabilityFrom()).isEqualTo(UPDATED_CAPACITY_AVAILABILITY_FROM);
//        assertThat(testAuction.getCapacityAvailabilityTo()).isEqualTo(UPDATED_CAPACITY_AVAILABILITY_TO);
//        assertThat(testAuction.getEnergyAvailabilityFrom()).isEqualTo(UPDATED_ENERGY_AVAILABILITY_FROM);
//        assertThat(testAuction.getEnergyAvailabilityTo()).isEqualTo(UPDATED_ENERGY_AVAILABILITY_TO);
//        assertThat(testAuction.getVersion()).isEqualTo(UPDATED_VERSION);
//    }
//
//    @Test
//    @Transactional
//    public void updateNonExistingAuction() throws Exception {
//        int databaseSizeBeforeUpdate = auctionRepository.findAll().size();
//
//        // Create the Auction
//        AuctionDTO auctionDTO = auctionMapper.toDto(auctionEntity);
//
//        // If the entity doesn't have an ID, it will throw BadRequestAlertException
//        restAuctionMockMvc.perform(put("/api/auctions")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(TestUtil.convertObjectToJsonBytes(auctionDTO)))
//            .andExpect(status().isBadRequest());
//
//        // Validate the Auction in the database
//        List<AuctionEntity> auctionList = auctionRepository.findAll();
//        assertThat(auctionList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    public void deleteAuction() throws Exception {
//        // Initialize the database
//        auctionRepository.saveAndFlush(auctionEntity);
//
//        int databaseSizeBeforeDelete = auctionRepository.findAll().size();
//
//        // Delete the auction
//        restAuctionMockMvc.perform(delete("/api/auctions/{id}", auctionEntity.getId())
//            .accept(MediaType.APPLICATION_JSON))
//            .andExpect(status().isNoContent());
//
//        // Validate the database contains one less item
//        List<AuctionEntity> auctionList = auctionRepository.findAll();
//        assertThat(auctionList).hasSize(databaseSizeBeforeDelete - 1);
//    }
//}
