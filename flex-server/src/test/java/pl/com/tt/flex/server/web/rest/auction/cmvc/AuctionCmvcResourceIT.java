package pl.com.tt.flex.server.web.rest.auction.cmvc;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionStatus;
import pl.com.tt.flex.model.service.dto.auction.type.AuctionType;
import pl.com.tt.flex.server.domain.auction.cmvc.AuctionCmvcEntity;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.repository.auction.cmvc.AuctionCmvcRepository;
import pl.com.tt.flex.server.service.auction.cmvc.mapper.AuctionCmvcMapper;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.fsp.FspResourceAdminIT;
import pl.com.tt.flex.server.web.rest.product.ProductResourceIT;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.com.tt.flex.model.service.dto.localization.LocalizationType.POWER_STATION_ML_LV_NUMBER;
import static pl.com.tt.flex.server.web.rest.InstantTestUtil.getInstantWithSpecifiedHourAndMinute;


public abstract class AuctionCmvcResourceIT {

    protected static final String DEFAULT_NAME = "AAAAAAAAAA";
    protected static final String UPDATED_NAME = "BBBBBBBBBB";

    protected static final Instant DEFAULT_DELIVERY_DATE_FROM = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(2, DAYS), 1, 0);
    protected static final Instant UPDATED_DELIVERY_DATE_FROM = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(2, DAYS), 2, 0);

    protected static final Instant DEFAULT_DELIVERY_DATE_TO = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(2, DAYS), 20, 0);
    protected static final Instant UPDATED_DELIVERY_DATE_TO = getInstantWithSpecifiedHourAndMinute(Instant.now().plus(2, DAYS), 21, 0);

    protected static final Instant DEFAULT_GATE_OPENING_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now(), 1, 0);
    protected static final Instant UPDATED_GATE_OPENING_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now(), 2, 0);

    protected static final Instant DEFAULT_GATE_CLOSURE_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now(), 20, 0);
    protected static final Instant UPDATED_GATE_CLOSURE_TIME = getInstantWithSpecifiedHourAndMinute(Instant.now(), 21, 0);

    protected static final BigDecimal DEFAULT_MIN_DESIRED_POWER = new BigDecimal(1);
    protected static final BigDecimal UPDATED_MIN_DESIRED_POWER = new BigDecimal(2);
    protected static final BigDecimal SMALLER_MIN_DESIRED_POWER = new BigDecimal(1 - 1);

    protected static final BigDecimal DEFAULT_MAX_DESIRED_POWER = new BigDecimal(1);
    protected static final BigDecimal UPDATED_MAX_DESIRED_POWER = new BigDecimal(2);

    protected static final AuctionStatus OPEN_AUCTION_STATUS = AuctionStatus.OPEN;
    protected static final AuctionStatus NEW_AUCTION_STATUS = AuctionStatus.NEW;

    protected static final String LOCALIZATION_POWER_STATION_NAME = "LOCALIZATION_POWER_STATION";

    @Autowired
    private AuctionCmvcRepository auctionCmvcRepository;

    @Autowired
    private AuctionCmvcMapper auctionCmvcMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restAuctionCmvcMockMvc;

    @MockBean
    FspService mockFspService;

    protected AuctionCmvcEntity auctionCmvcEntity;
    private final String requestUri;

    public AuctionCmvcResourceIT(AuctionCmvcRepository auctionCmvcRepository, AuctionCmvcMapper auctionCmvcMapper,
                                 EntityManager em, MockMvc restAuctionCmvcMockMvc, String requestUri) {
        this.auctionCmvcRepository = auctionCmvcRepository;
        this.auctionCmvcMapper = auctionCmvcMapper;
        this.restAuctionCmvcMockMvc = restAuctionCmvcMockMvc;
        this.em = em;
        this.requestUri = requestUri;
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static AuctionCmvcEntity createEntity(EntityManager em) {
        AuctionCmvcEntity auctionCmvcEntity = new AuctionCmvcEntity();
        auctionCmvcEntity.setName(DEFAULT_NAME);
        auctionCmvcEntity.setLocalization(Set.of(getPowerStation(em)));
        auctionCmvcEntity.setDeliveryDateFrom(DEFAULT_DELIVERY_DATE_FROM);
        auctionCmvcEntity.setDeliveryDateTo(DEFAULT_DELIVERY_DATE_TO);
        auctionCmvcEntity.setGateOpeningTime(DEFAULT_GATE_OPENING_TIME);
        auctionCmvcEntity.setGateClosureTime(DEFAULT_GATE_CLOSURE_TIME);
        auctionCmvcEntity.setMinDesiredPower(DEFAULT_MIN_DESIRED_POWER);
        auctionCmvcEntity.setMaxDesiredPower(DEFAULT_MAX_DESIRED_POWER);
        // Add required entity
        ProductEntity product;
        if (TestUtil.findAll(em, ProductEntity.class).isEmpty()) {
            product = ProductResourceIT.createEntity(em);
        } else {
            product = TestUtil.findAll(em, ProductEntity.class).get(0);
        }
        product.setActive(true);
        product.setValidTo(Instant.now().plus(100, DAYS));
        em.persist(product);
        em.flush();
        auctionCmvcEntity.setProduct(product);
        return auctionCmvcEntity;
    }

    private static LocalizationTypeEntity getPowerStation(EntityManager em) {
        LocalizationTypeEntity powerStationType = new LocalizationTypeEntity();
        powerStationType.setType(POWER_STATION_ML_LV_NUMBER);
        powerStationType.setName(LOCALIZATION_POWER_STATION_NAME);
        em.persist(powerStationType);
        em.flush();
        return powerStationType;
    }

    @BeforeEach
    public void initTest() {
        auctionCmvcEntity = createEntity(em);
        mockedFsp();
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcs() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList
        restAuctionCmvcMockMvc.perform(get(requestUri + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(auctionCmvcEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].localization.[*].name").value(hasItem(LOCALIZATION_POWER_STATION_NAME)))
            .andExpect(jsonPath("$.[*].deliveryDateFrom").value(hasItem(DEFAULT_DELIVERY_DATE_FROM.toString())))
            .andExpect(jsonPath("$.[*].deliveryDateTo").value(hasItem(DEFAULT_DELIVERY_DATE_TO.toString())))
            .andExpect(jsonPath("$.[*].gateOpeningTime").value(hasItem(DEFAULT_GATE_OPENING_TIME.toString())))
            .andExpect(jsonPath("$.[*].gateClosureTime").value(hasItem(DEFAULT_GATE_CLOSURE_TIME.toString())))
            .andExpect(jsonPath("$.[*].minDesiredPower").value(hasItem(DEFAULT_MIN_DESIRED_POWER.intValue())))
            .andExpect(jsonPath("$.[*].maxDesiredPower").value(hasItem(DEFAULT_MAX_DESIRED_POWER.intValue())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(OPEN_AUCTION_STATUS.toString())))
            .andExpect(jsonPath("$.[*].auctionType").value(hasItem(AuctionType.CAPACITY.toString())));
    }

    @Test
    @Transactional
    public void getAuctionCmvc() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get the auctionCmvc
        restAuctionCmvcMockMvc.perform(get(requestUri + "/{id}", auctionCmvcEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(auctionCmvcEntity.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.localization.[*].name").value(hasItem(LOCALIZATION_POWER_STATION_NAME)))
            .andExpect(jsonPath("$.deliveryDateFrom").value(DEFAULT_DELIVERY_DATE_FROM.toString()))
            .andExpect(jsonPath("$.deliveryDateTo").value(DEFAULT_DELIVERY_DATE_TO.toString()))
            .andExpect(jsonPath("$.gateOpeningTime").value(DEFAULT_GATE_OPENING_TIME.toString()))
            .andExpect(jsonPath("$.gateClosureTime").value(DEFAULT_GATE_CLOSURE_TIME.toString()))
            .andExpect(jsonPath("$.minDesiredPower").value(DEFAULT_MIN_DESIRED_POWER.intValue()))
            .andExpect(jsonPath("$.maxDesiredPower").value(DEFAULT_MAX_DESIRED_POWER.toString()))
            .andExpect(jsonPath("$.auctionType").value(AuctionType.CAPACITY.toString()));
    }


    @Test
    @Transactional
    public void getAuctionCmvcsByIdFiltering() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        Long id = auctionCmvcEntity.getId();

        defaultAuctionCmvcShouldBeFound("id.equals=" + id);
        defaultAuctionCmvcShouldNotBeFound("id.notEquals=" + id);

        defaultAuctionCmvcShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultAuctionCmvcShouldNotBeFound("id.greaterThan=" + id);

        defaultAuctionCmvcShouldBeFound("id.lessThanOrEqual=" + id);
        defaultAuctionCmvcShouldNotBeFound("id.lessThan=" + id);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where name equals to DEFAULT_NAME
        defaultAuctionCmvcShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the auctionCmvcList where name equals to UPDATED_NAME
        defaultAuctionCmvcShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where name not equals to DEFAULT_NAME
        defaultAuctionCmvcShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the auctionCmvcList where name not equals to UPDATED_NAME
        defaultAuctionCmvcShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where name in DEFAULT_NAME or UPDATED_NAME
        defaultAuctionCmvcShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the auctionCmvcList where name equals to UPDATED_NAME
        defaultAuctionCmvcShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where name is not null
        defaultAuctionCmvcShouldBeFound("name.specified=true");

        // Get all the auctionCmvcList where name is null
        defaultAuctionCmvcShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByNameContainsSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where name contains DEFAULT_NAME
        defaultAuctionCmvcShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the auctionCmvcList where name contains UPDATED_NAME
        defaultAuctionCmvcShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where name does not contain DEFAULT_NAME
        defaultAuctionCmvcShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the auctionCmvcList where name does not contain UPDATED_NAME
        defaultAuctionCmvcShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByLocalizationContainsSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where localization contains DEFAULT_LOCALIZATION
        defaultAuctionCmvcShouldBeFound("localization.contains=" + LOCALIZATION_POWER_STATION_NAME);

        // Get all the auctionCmvcList where localization contains RandomStringUtils.random(10)
        defaultAuctionCmvcShouldNotBeFound("localization.contains=" + RandomStringUtils.random(10));
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByLocalizationNotContainsSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where localization does not contain DEFAULT_LOCALIZATION
        defaultAuctionCmvcShouldNotBeFound("localization.doesNotContain=" + LOCALIZATION_POWER_STATION_NAME);

        // Get all the auctionCmvcList where localization does not contain RandomStringUtils.random(10)
        defaultAuctionCmvcShouldBeFound("localization.doesNotContain=" + RandomStringUtils.random(10));
    }


    @Test
    @Transactional
    public void getAllAuctionCmvcsByDeliveryDateFromIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where deliveryDateFrom equals to DEFAULT_DELIVERY_DATE_FROM
        defaultAuctionCmvcShouldBeFound("deliveryDateFrom.equals=" + DEFAULT_DELIVERY_DATE_FROM);

        // Get all the auctionCmvcList where deliveryDateFrom equals to UPDATED_DELIVERY_DATE_FROM
        defaultAuctionCmvcShouldNotBeFound("deliveryDateFrom.equals=" + UPDATED_DELIVERY_DATE_FROM);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByDeliveryDateFromIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where deliveryDateFrom not equals to DEFAULT_DELIVERY_DATE_FROM
        defaultAuctionCmvcShouldNotBeFound("deliveryDateFrom.notEquals=" + DEFAULT_DELIVERY_DATE_FROM);

        // Get all the auctionCmvcList where deliveryDateFrom not equals to UPDATED_DELIVERY_DATE_FROM
        defaultAuctionCmvcShouldBeFound("deliveryDateFrom.notEquals=" + UPDATED_DELIVERY_DATE_FROM);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByDeliveryDateFromIsInShouldWork() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where deliveryDateFrom in DEFAULT_DELIVERY_DATE_FROM or UPDATED_DELIVERY_DATE_FROM
        defaultAuctionCmvcShouldBeFound("deliveryDateFrom.in=" + DEFAULT_DELIVERY_DATE_FROM + "," + UPDATED_DELIVERY_DATE_FROM);

        // Get all the auctionCmvcList where deliveryDateFrom equals to UPDATED_DELIVERY_DATE_FROM
        defaultAuctionCmvcShouldNotBeFound("deliveryDateFrom.in=" + UPDATED_DELIVERY_DATE_FROM);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByDeliveryDateFromIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where deliveryDateFrom is not null
        defaultAuctionCmvcShouldBeFound("deliveryDateFrom.specified=true");

        // Get all the auctionCmvcList where deliveryDateFrom is null
        defaultAuctionCmvcShouldNotBeFound("deliveryDateFrom.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByDeliveryDateToIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where deliveryDateTo equals to DEFAULT_DELIVERY_DATE_TO
        defaultAuctionCmvcShouldBeFound("deliveryDateTo.equals=" + DEFAULT_DELIVERY_DATE_TO);

        // Get all the auctionCmvcList where deliveryDateTo equals to UPDATED_DELIVERY_DATE_TO
        defaultAuctionCmvcShouldNotBeFound("deliveryDateTo.equals=" + UPDATED_DELIVERY_DATE_TO);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByDeliveryDateToIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where deliveryDateTo not equals to DEFAULT_DELIVERY_DATE_TO
        defaultAuctionCmvcShouldNotBeFound("deliveryDateTo.notEquals=" + DEFAULT_DELIVERY_DATE_TO);

        // Get all the auctionCmvcList where deliveryDateTo not equals to UPDATED_DELIVERY_DATE_TO
        defaultAuctionCmvcShouldBeFound("deliveryDateTo.notEquals=" + UPDATED_DELIVERY_DATE_TO);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByDeliveryDateToIsInShouldWork() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where deliveryDateTo in DEFAULT_DELIVERY_DATE_TO or UPDATED_DELIVERY_DATE_TO
        defaultAuctionCmvcShouldBeFound("deliveryDateTo.in=" + DEFAULT_DELIVERY_DATE_TO + "," + UPDATED_DELIVERY_DATE_TO);

        // Get all the auctionCmvcList where deliveryDateTo equals to UPDATED_DELIVERY_DATE_TO
        defaultAuctionCmvcShouldNotBeFound("deliveryDateTo.in=" + UPDATED_DELIVERY_DATE_TO);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByDeliveryDateToIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where deliveryDateTo is not null
        defaultAuctionCmvcShouldBeFound("deliveryDateTo.specified=true");

        // Get all the auctionCmvcList where deliveryDateTo is null
        defaultAuctionCmvcShouldNotBeFound("deliveryDateTo.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByGateOpeningTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where gateOpeningTime equals to DEFAULT_GATE_OPENING_TIME
        defaultAuctionCmvcShouldBeFound("gateOpeningTime.equals=" + DEFAULT_GATE_OPENING_TIME);

        // Get all the auctionCmvcList where gateOpeningTime equals to UPDATED_GATE_OPENING_TIME
        defaultAuctionCmvcShouldNotBeFound("gateOpeningTime.equals=" + UPDATED_GATE_OPENING_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByGateOpeningTimeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where gateOpeningTime not equals to DEFAULT_GATE_OPENING_TIME
        defaultAuctionCmvcShouldNotBeFound("gateOpeningTime.notEquals=" + DEFAULT_GATE_OPENING_TIME);

        // Get all the auctionCmvcList where gateOpeningTime not equals to UPDATED_GATE_OPENING_TIME
        defaultAuctionCmvcShouldBeFound("gateOpeningTime.notEquals=" + UPDATED_GATE_OPENING_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByGateOpeningTimeIsInShouldWork() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where gateOpeningTime in DEFAULT_GATE_OPENING_TIME or UPDATED_GATE_OPENING_TIME
        defaultAuctionCmvcShouldBeFound("gateOpeningTime.in=" + DEFAULT_GATE_OPENING_TIME + "," + UPDATED_GATE_OPENING_TIME);

        // Get all the auctionCmvcList where gateOpeningTime equals to UPDATED_GATE_OPENING_TIME
        defaultAuctionCmvcShouldNotBeFound("gateOpeningTime.in=" + UPDATED_GATE_OPENING_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByGateOpeningTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where gateOpeningTime is not null
        defaultAuctionCmvcShouldBeFound("gateOpeningTime.specified=true");

        // Get all the auctionCmvcList where gateOpeningTime is null
        defaultAuctionCmvcShouldNotBeFound("gateOpeningTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByGateClosureTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where gateClosureTime equals to DEFAULT_GATE_CLOSURE_TIME
        defaultAuctionCmvcShouldBeFound("gateClosureTime.equals=" + DEFAULT_GATE_CLOSURE_TIME);

        // Get all the auctionCmvcList where gateClosureTime equals to UPDATED_GATE_CLOSURE_TIME
        defaultAuctionCmvcShouldNotBeFound("gateClosureTime.equals=" + UPDATED_GATE_CLOSURE_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByGateClosureTimeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where gateClosureTime not equals to DEFAULT_GATE_CLOSURE_TIME
        defaultAuctionCmvcShouldNotBeFound("gateClosureTime.notEquals=" + DEFAULT_GATE_CLOSURE_TIME);

        // Get all the auctionCmvcList where gateClosureTime not equals to UPDATED_GATE_CLOSURE_TIME
        defaultAuctionCmvcShouldBeFound("gateClosureTime.notEquals=" + UPDATED_GATE_CLOSURE_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByGateClosureTimeIsInShouldWork() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where gateClosureTime in DEFAULT_GATE_CLOSURE_TIME or UPDATED_GATE_CLOSURE_TIME
        defaultAuctionCmvcShouldBeFound("gateClosureTime.in=" + DEFAULT_GATE_CLOSURE_TIME + "," + UPDATED_GATE_CLOSURE_TIME);

        // Get all the auctionCmvcList where gateClosureTime equals to UPDATED_GATE_CLOSURE_TIME
        defaultAuctionCmvcShouldNotBeFound("gateClosureTime.in=" + UPDATED_GATE_CLOSURE_TIME);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByGateClosureTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where gateClosureTime is not null
        defaultAuctionCmvcShouldBeFound("gateClosureTime.specified=true");

        // Get all the auctionCmvcList where gateClosureTime is null
        defaultAuctionCmvcShouldNotBeFound("gateClosureTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByMinDesiredPowerIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where minDesiredPower equals to DEFAULT_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldBeFound("minDesiredPower.equals=" + DEFAULT_MIN_DESIRED_POWER);

        // Get all the auctionCmvcList where minDesiredPower equals to UPDATED_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldNotBeFound("minDesiredPower.equals=" + UPDATED_MIN_DESIRED_POWER);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByMinDesiredPowerIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where minDesiredPower not equals to DEFAULT_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldNotBeFound("minDesiredPower.notEquals=" + DEFAULT_MIN_DESIRED_POWER);

        // Get all the auctionCmvcList where minDesiredPower not equals to UPDATED_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldBeFound("minDesiredPower.notEquals=" + UPDATED_MIN_DESIRED_POWER);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByMinDesiredPowerIsInShouldWork() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where minDesiredPower in DEFAULT_MIN_DESIRED_POWER or UPDATED_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldBeFound("minDesiredPower.in=" + DEFAULT_MIN_DESIRED_POWER + "," + UPDATED_MIN_DESIRED_POWER);

        // Get all the auctionCmvcList where minDesiredPower equals to UPDATED_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldNotBeFound("minDesiredPower.in=" + UPDATED_MIN_DESIRED_POWER);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByMinDesiredPowerIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where minDesiredPower is not null
        defaultAuctionCmvcShouldBeFound("minDesiredPower.specified=true");

        // Get all the auctionCmvcList where minDesiredPower is null
        defaultAuctionCmvcShouldNotBeFound("minDesiredPower.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByMinDesiredPowerIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where minDesiredPower is greater than or equal to DEFAULT_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldBeFound("minDesiredPower.greaterThanOrEqual=" + DEFAULT_MIN_DESIRED_POWER);

        // Get all the auctionCmvcList where minDesiredPower is greater than or equal to UPDATED_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldNotBeFound("minDesiredPower.greaterThanOrEqual=" + UPDATED_MIN_DESIRED_POWER);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByMinDesiredPowerIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where minDesiredPower is less than or equal to DEFAULT_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldBeFound("minDesiredPower.lessThanOrEqual=" + DEFAULT_MIN_DESIRED_POWER);

        // Get all the auctionCmvcList where minDesiredPower is less than or equal to SMALLER_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldNotBeFound("minDesiredPower.lessThanOrEqual=" + SMALLER_MIN_DESIRED_POWER);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByMinDesiredPowerIsLessThanSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where minDesiredPower is less than DEFAULT_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldNotBeFound("minDesiredPower.lessThan=" + DEFAULT_MIN_DESIRED_POWER);

        // Get all the auctionCmvcList where minDesiredPower is less than UPDATED_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldBeFound("minDesiredPower.lessThan=" + UPDATED_MIN_DESIRED_POWER);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByMinDesiredPowerIsGreaterThanSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where minDesiredPower is greater than DEFAULT_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldNotBeFound("minDesiredPower.greaterThan=" + DEFAULT_MIN_DESIRED_POWER);

        // Get all the auctionCmvcList where minDesiredPower is greater than SMALLER_MIN_DESIRED_POWER
        defaultAuctionCmvcShouldBeFound("minDesiredPower.greaterThan=" + SMALLER_MIN_DESIRED_POWER);
    }


    @Test
    @Transactional
    public void getAllAuctionCmvcsByMaxDesiredPowerIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where maxDesiredPower equals to DEFAULT_MAX_DESIRED_POWER
        defaultAuctionCmvcShouldBeFound("maxDesiredPower.equals=" + DEFAULT_MAX_DESIRED_POWER);

        // Get all the auctionCmvcList where maxDesiredPower equals to UPDATED_MAX_DESIRED_POWER
        defaultAuctionCmvcShouldNotBeFound("maxDesiredPower.equals=" + UPDATED_MAX_DESIRED_POWER);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByMaxDesiredPowerIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where maxDesiredPower not equals to DEFAULT_MAX_DESIRED_POWER
        defaultAuctionCmvcShouldNotBeFound("maxDesiredPower.notEquals=" + DEFAULT_MAX_DESIRED_POWER);

        // Get all the auctionCmvcList where maxDesiredPower not equals to UPDATED_MAX_DESIRED_POWER
        defaultAuctionCmvcShouldBeFound("maxDesiredPower.notEquals=" + UPDATED_MAX_DESIRED_POWER);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByMaxDesiredPowerIsInShouldWork() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where maxDesiredPower in DEFAULT_MAX_DESIRED_POWER or UPDATED_MAX_DESIRED_POWER
        defaultAuctionCmvcShouldBeFound("maxDesiredPower.in=" + DEFAULT_MAX_DESIRED_POWER + "," + UPDATED_MAX_DESIRED_POWER);

        // Get all the auctionCmvcList where maxDesiredPower equals to UPDATED_MAX_DESIRED_POWER
        defaultAuctionCmvcShouldNotBeFound("maxDesiredPower.in=" + UPDATED_MAX_DESIRED_POWER);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByMaxDesiredPowerIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where maxDesiredPower is not null
        defaultAuctionCmvcShouldBeFound("maxDesiredPower.specified=true");

        // Get all the auctionCmvcList where maxDesiredPower is null
        defaultAuctionCmvcShouldNotBeFound("maxDesiredPower.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByStatusIsEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where status equals to DEFAULT_STATUS
        defaultAuctionCmvcShouldBeFound("status.equals=" + OPEN_AUCTION_STATUS);

        // Get all the auctionCmvcList where status equals to UPDATED_STATUS
        defaultAuctionCmvcShouldNotBeFound("status.equals=" + NEW_AUCTION_STATUS);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByStatusIsNotEqualToSomething() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where status not equals to DEFAULT_STATUS
        defaultAuctionCmvcShouldNotBeFound("status.notEquals=" + OPEN_AUCTION_STATUS);

        // Get all the auctionCmvcList where status not equals to UPDATED_STATUS
        defaultAuctionCmvcShouldBeFound("status.notEquals=" + NEW_AUCTION_STATUS);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByStatusIsInShouldWork() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where status in DEFAULT_STATUS or UPDATED_STATUS
        defaultAuctionCmvcShouldBeFound("status.in=" + OPEN_AUCTION_STATUS + "," + NEW_AUCTION_STATUS);

        // Get all the auctionCmvcList where status equals to UPDATED_STATUS
        defaultAuctionCmvcShouldNotBeFound("status.in=" + NEW_AUCTION_STATUS);
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByStatusIsNullOrNotNull() throws Exception {
        // Initialize the database
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where status is not null
        defaultAuctionCmvcShouldBeFound("status.specified=true");

        // Get all the auctionCmvcList where status is null
        defaultAuctionCmvcShouldNotBeFound("status.specified=false");
    }

    @Test
    @Transactional
    public void getAllAuctionCmvcsByProductNameEqualToSomething() throws Exception {
        // Get already existing entity
        ProductEntity product = auctionCmvcEntity.getProduct();
        auctionCmvcRepository.saveAndFlush(auctionCmvcEntity);

        // Get all the auctionCmvcList where product equals to productId
        defaultAuctionCmvcShouldBeFound("productName.equals=" + product.getShortName());

        // Get all the auctionCmvcList where product equals to productId + 1
        defaultAuctionCmvcShouldNotBeFound("productName.equals=" + RandomStringUtils.random(10));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultAuctionCmvcShouldBeFound(String filter) throws Exception {
        restAuctionCmvcMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(auctionCmvcEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").isNotEmpty())
            .andExpect(jsonPath("$.[*].localization.[*].name").value(hasItem(LOCALIZATION_POWER_STATION_NAME)))
            .andExpect(jsonPath("$.[*].deliveryDateFrom").value(hasItem(DEFAULT_DELIVERY_DATE_FROM.toString())))
            .andExpect(jsonPath("$.[*].deliveryDateTo").value(hasItem(DEFAULT_DELIVERY_DATE_TO.toString())))
            .andExpect(jsonPath("$.[*].gateOpeningTime").value(hasItem(DEFAULT_GATE_OPENING_TIME.toString())))
            .andExpect(jsonPath("$.[*].gateClosureTime").value(hasItem(DEFAULT_GATE_CLOSURE_TIME.toString())))
            .andExpect(jsonPath("$.[*].minDesiredPower").value(hasItem(DEFAULT_MIN_DESIRED_POWER.intValue())))
            .andExpect(jsonPath("$.[*].maxDesiredPower").value(hasItem(DEFAULT_MAX_DESIRED_POWER.intValue())))
            .andExpect(jsonPath("$.[*].status").isNotEmpty())
            .andExpect(jsonPath("$.[*].auctionType").value(hasItem(AuctionType.CAPACITY.toString())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultAuctionCmvcShouldNotBeFound(String filter) throws Exception {
        restAuctionCmvcMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    public void getNonExistingAuctionCmvc() throws Exception {
        // Get the auctionCmvc
        restAuctionCmvcMockMvc.perform(get(requestUri + "/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    private void mockedFsp() {
        FspEntity entity = FspResourceAdminIT.createEntity(em);
        Mockito.when(mockFspService.findFspOfUser(anyLong(), anyString())).thenReturn(Optional.of(entity));
    }
}
