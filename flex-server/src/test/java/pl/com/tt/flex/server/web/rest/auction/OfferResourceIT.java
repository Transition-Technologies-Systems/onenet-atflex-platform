//package pl.com.tt.flex.server.web.rest.auction;
//
//import pl.com.tt.flex.server.FlexserverApp;
//import pl.com.tt.flex.server.domain.auction.OfferEntity;
//import pl.com.tt.flex.server.repository.auction.OfferRepository;
//import pl.com.tt.flex.server.service.auction.offer.OfferService;
//import pl.com.tt.flex.server.service.auction.offer.dto.OfferDTO;
//import pl.com.tt.flex.server.service.auction.offer.mapper.OfferMapper;
//import pl.com.tt.flex.server.service.auction.offer.OfferQueryService;
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
//import pl.com.tt.flex.server.web.rest.auction.offer.OfferResource;
//import pl.com.tt.flex.server.web.rest.TestUtil;
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
// * Integration tests for the {@link OfferResource} REST controller.
// */
//@SpringBootTest(classes = FlexserverApp.class)
//@AutoConfigureMockMvc
//@WithMockUser
//public class OfferResourceIT {
//
//    private static final Long DEFAULT_POTENTIAL_ID = 1L;
//    private static final Long UPDATED_POTENTIAL_ID = 2L;
//    private static final Long SMALLER_POTENTIAL_ID = 1L - 1L;
//
//    private static final BigDecimal DEFAULT_PRICE = new BigDecimal(1);
//    private static final BigDecimal UPDATED_PRICE = new BigDecimal(2);
//    private static final BigDecimal SMALLER_PRICE = new BigDecimal(1 - 1);
//
//    private static final BigDecimal DEFAULT_VOLUME = new BigDecimal(1);
//    private static final BigDecimal UPDATED_VOLUME = new BigDecimal(2);
//    private static final BigDecimal SMALLER_VOLUME = new BigDecimal(1 - 1);
//
//    private static final Boolean DEFAULT_VOLUME_DIVISIBILITY = false;
//    private static final Boolean UPDATED_VOLUME_DIVISIBILITY = true;
//
//    private static final Instant DEFAULT_DELIVERY_PERIOD_FROM = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_DELIVERY_PERIOD_FROM = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Instant DEFAULT_DELIVERY_PERIOD_TO = Instant.ofEpochMilli(0L);
//    private static final Instant UPDATED_DELIVERY_PERIOD_TO = Instant.now().truncatedTo(ChronoUnit.MILLIS);
//
//    private static final Boolean DEFAULT_DELIVERY_PERIOD_DIVISIBILITY = false;
//    private static final Boolean UPDATED_DELIVERY_PERIOD_DIVISIBILITY = true;
//
//    @Autowired
//    private OfferRepository offerRepository;
//
//    @Autowired
//    private OfferMapper offerMapper;
//
//    @Autowired
//    private OfferService offerService;
//
//    @Autowired
//    private OfferQueryService offerQueryService;
//
//    @Autowired
//    private EntityManager em;
//
//    @Autowired
//    private MockMvc restOfferMockMvc;
//
//    private OfferEntity offerEntity;
//
//    /**
//     * Create an entity for this test.
//     *
//     * This is a static method, as tests for other entities might also need it,
//     * if they test an entity which requires the current entity.
//     */
//    public static OfferEntity createEntity(EntityManager em) {
//        OfferEntity offerEntity = new OfferEntity()
//            .potentialId(DEFAULT_POTENTIAL_ID)
//            .price(DEFAULT_PRICE)
//            .volume(DEFAULT_VOLUME)
//            .volumeDivisibility(DEFAULT_VOLUME_DIVISIBILITY)
//            .deliveryPeriodFrom(DEFAULT_DELIVERY_PERIOD_FROM)
//            .deliveryPeriodTo(DEFAULT_DELIVERY_PERIOD_TO)
//            .deliveryPeriodDivisibility(DEFAULT_DELIVERY_PERIOD_DIVISIBILITY);
//        return offerEntity;
//    }
//    /**
//     * Create an updated entity for this test.
//     *
//     * This is a static method, as tests for other entities might also need it,
//     * if they test an entity which requires the current entity.
//     */
//    public static OfferEntity createUpdatedEntity(EntityManager em) {
//        OfferEntity offerEntity = new OfferEntity()
//            .potentialId(UPDATED_POTENTIAL_ID)
//            .price(UPDATED_PRICE)
//            .volume(UPDATED_VOLUME)
//            .volumeDivisibility(UPDATED_VOLUME_DIVISIBILITY)
//            .deliveryPeriodFrom(UPDATED_DELIVERY_PERIOD_FROM)
//            .deliveryPeriodTo(UPDATED_DELIVERY_PERIOD_TO)
//            .deliveryPeriodDivisibility(UPDATED_DELIVERY_PERIOD_DIVISIBILITY);
//        return offerEntity;
//    }
//
//    @BeforeEach
//    public void initTest() {
//        offerEntity = createEntity(em);
//    }
//
//    @Test
//    @Transactional
//    public void createOffer() throws Exception {
//        int databaseSizeBeforeCreate = offerRepository.findAll().size();
//        // Create the Offer
//        OfferDTO offerDTO = offerMapper.toDto(offerEntity);
//        restOfferMockMvc.perform(post("/api/offers")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(TestUtil.convertObjectToJsonBytes(offerDTO)))
//            .andExpect(status().isCreated());
//
//        // Validate the Offer in the database
//        List<OfferEntity> offerList = offerRepository.findAll();
//        assertThat(offerList).hasSize(databaseSizeBeforeCreate + 1);
//        OfferEntity testOffer = offerList.get(offerList.size() - 1);
//        assertThat(testOffer.getPotentialId()).isEqualTo(DEFAULT_POTENTIAL_ID);
//        assertThat(testOffer.getPrice()).isEqualTo(DEFAULT_PRICE);
//        assertThat(testOffer.getVolume()).isEqualTo(DEFAULT_VOLUME);
//        assertThat(testOffer.isVolumeDivisibility()).isEqualTo(DEFAULT_VOLUME_DIVISIBILITY);
//        assertThat(testOffer.getDeliveryPeriodFrom()).isEqualTo(DEFAULT_DELIVERY_PERIOD_FROM);
//        assertThat(testOffer.getDeliveryPeriodTo()).isEqualTo(DEFAULT_DELIVERY_PERIOD_TO);
//        assertThat(testOffer.isDeliveryPeriodDivisibility()).isEqualTo(DEFAULT_DELIVERY_PERIOD_DIVISIBILITY);
//    }
//
//    @Test
//    @Transactional
//    public void createOfferWithExistingId() throws Exception {
//        int databaseSizeBeforeCreate = offerRepository.findAll().size();
//
//        // Create the Offer with an existing ID
//        offerEntity.setId(1L);
//        OfferDTO offerDTO = offerMapper.toDto(offerEntity);
//
//        // An entity with an existing ID cannot be created, so this API call must fail
//        restOfferMockMvc.perform(post("/api/offers")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(TestUtil.convertObjectToJsonBytes(offerDTO)))
//            .andExpect(status().isBadRequest());
//
//        // Validate the Offer in the database
//        List<OfferEntity> offerList = offerRepository.findAll();
//        assertThat(offerList).hasSize(databaseSizeBeforeCreate);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllOffers() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList
//        restOfferMockMvc.perform(get("/api/offers?sort=id,desc"))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.[*].id").value(hasItem(offerEntity.getId().intValue())))
//            .andExpect(jsonPath("$.[*].potentialId").value(hasItem(DEFAULT_POTENTIAL_ID.intValue())))
//            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.intValue())))
//            .andExpect(jsonPath("$.[*].volume").value(hasItem(DEFAULT_VOLUME.intValue())))
//            .andExpect(jsonPath("$.[*].volumeDivisibility").value(hasItem(DEFAULT_VOLUME_DIVISIBILITY.booleanValue())))
//            .andExpect(jsonPath("$.[*].deliveryPeriodFrom").value(hasItem(DEFAULT_DELIVERY_PERIOD_FROM.toString())))
//            .andExpect(jsonPath("$.[*].deliveryPeriodTo").value(hasItem(DEFAULT_DELIVERY_PERIOD_TO.toString())))
//            .andExpect(jsonPath("$.[*].deliveryPeriodDivisibility").value(hasItem(DEFAULT_DELIVERY_PERIOD_DIVISIBILITY.booleanValue())));
//    }
//
//    @Test
//    @Transactional
//    public void getOffer() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get the offer
//        restOfferMockMvc.perform(get("/api/offers/{id}", offerEntity.getId()))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.id").value(offerEntity.getId().intValue()))
//            .andExpect(jsonPath("$.potentialId").value(DEFAULT_POTENTIAL_ID.intValue()))
//            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE.intValue()))
//            .andExpect(jsonPath("$.volume").value(DEFAULT_VOLUME.intValue()))
//            .andExpect(jsonPath("$.volumeDivisibility").value(DEFAULT_VOLUME_DIVISIBILITY.booleanValue()))
//            .andExpect(jsonPath("$.deliveryPeriodFrom").value(DEFAULT_DELIVERY_PERIOD_FROM.toString()))
//            .andExpect(jsonPath("$.deliveryPeriodTo").value(DEFAULT_DELIVERY_PERIOD_TO.toString()))
//            .andExpect(jsonPath("$.deliveryPeriodDivisibility").value(DEFAULT_DELIVERY_PERIOD_DIVISIBILITY.booleanValue()));
//    }
//
//
//    @Test
//    @Transactional
//    public void getOffersByIdFiltering() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        Long id = offerEntity.getId();
//
//        defaultOfferShouldBeFound("id.equals=" + id);
//        defaultOfferShouldNotBeFound("id.notEquals=" + id);
//
//        defaultOfferShouldBeFound("id.greaterThanOrEqual=" + id);
//        defaultOfferShouldNotBeFound("id.greaterThan=" + id);
//
//        defaultOfferShouldBeFound("id.lessThanOrEqual=" + id);
//        defaultOfferShouldNotBeFound("id.lessThan=" + id);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllOffersByPotentialIdIsEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where potentialId equals to DEFAULT_POTENTIAL_ID
//        defaultOfferShouldBeFound("potentialId.equals=" + DEFAULT_POTENTIAL_ID);
//
//        // Get all the offerList where potentialId equals to UPDATED_POTENTIAL_ID
//        defaultOfferShouldNotBeFound("potentialId.equals=" + UPDATED_POTENTIAL_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPotentialIdIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where potentialId not equals to DEFAULT_POTENTIAL_ID
//        defaultOfferShouldNotBeFound("potentialId.notEquals=" + DEFAULT_POTENTIAL_ID);
//
//        // Get all the offerList where potentialId not equals to UPDATED_POTENTIAL_ID
//        defaultOfferShouldBeFound("potentialId.notEquals=" + UPDATED_POTENTIAL_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPotentialIdIsInShouldWork() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where potentialId in DEFAULT_POTENTIAL_ID or UPDATED_POTENTIAL_ID
//        defaultOfferShouldBeFound("potentialId.in=" + DEFAULT_POTENTIAL_ID + "," + UPDATED_POTENTIAL_ID);
//
//        // Get all the offerList where potentialId equals to UPDATED_POTENTIAL_ID
//        defaultOfferShouldNotBeFound("potentialId.in=" + UPDATED_POTENTIAL_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPotentialIdIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where potentialId is not null
//        defaultOfferShouldBeFound("potentialId.specified=true");
//
//        // Get all the offerList where potentialId is null
//        defaultOfferShouldNotBeFound("potentialId.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPotentialIdIsGreaterThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where potentialId is greater than or equal to DEFAULT_POTENTIAL_ID
//        defaultOfferShouldBeFound("potentialId.greaterThanOrEqual=" + DEFAULT_POTENTIAL_ID);
//
//        // Get all the offerList where potentialId is greater than or equal to UPDATED_POTENTIAL_ID
//        defaultOfferShouldNotBeFound("potentialId.greaterThanOrEqual=" + UPDATED_POTENTIAL_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPotentialIdIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where potentialId is less than or equal to DEFAULT_POTENTIAL_ID
//        defaultOfferShouldBeFound("potentialId.lessThanOrEqual=" + DEFAULT_POTENTIAL_ID);
//
//        // Get all the offerList where potentialId is less than or equal to SMALLER_POTENTIAL_ID
//        defaultOfferShouldNotBeFound("potentialId.lessThanOrEqual=" + SMALLER_POTENTIAL_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPotentialIdIsLessThanSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where potentialId is less than DEFAULT_POTENTIAL_ID
//        defaultOfferShouldNotBeFound("potentialId.lessThan=" + DEFAULT_POTENTIAL_ID);
//
//        // Get all the offerList where potentialId is less than UPDATED_POTENTIAL_ID
//        defaultOfferShouldBeFound("potentialId.lessThan=" + UPDATED_POTENTIAL_ID);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPotentialIdIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where potentialId is greater than DEFAULT_POTENTIAL_ID
//        defaultOfferShouldNotBeFound("potentialId.greaterThan=" + DEFAULT_POTENTIAL_ID);
//
//        // Get all the offerList where potentialId is greater than SMALLER_POTENTIAL_ID
//        defaultOfferShouldBeFound("potentialId.greaterThan=" + SMALLER_POTENTIAL_ID);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllOffersByPriceIsEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where price equals to DEFAULT_PRICE
//        defaultOfferShouldBeFound("price.equals=" + DEFAULT_PRICE);
//
//        // Get all the offerList where price equals to UPDATED_PRICE
//        defaultOfferShouldNotBeFound("price.equals=" + UPDATED_PRICE);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPriceIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where price not equals to DEFAULT_PRICE
//        defaultOfferShouldNotBeFound("price.notEquals=" + DEFAULT_PRICE);
//
//        // Get all the offerList where price not equals to UPDATED_PRICE
//        defaultOfferShouldBeFound("price.notEquals=" + UPDATED_PRICE);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPriceIsInShouldWork() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where price in DEFAULT_PRICE or UPDATED_PRICE
//        defaultOfferShouldBeFound("price.in=" + DEFAULT_PRICE + "," + UPDATED_PRICE);
//
//        // Get all the offerList where price equals to UPDATED_PRICE
//        defaultOfferShouldNotBeFound("price.in=" + UPDATED_PRICE);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPriceIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where price is not null
//        defaultOfferShouldBeFound("price.specified=true");
//
//        // Get all the offerList where price is null
//        defaultOfferShouldNotBeFound("price.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPriceIsGreaterThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where price is greater than or equal to DEFAULT_PRICE
//        defaultOfferShouldBeFound("price.greaterThanOrEqual=" + DEFAULT_PRICE);
//
//        // Get all the offerList where price is greater than or equal to UPDATED_PRICE
//        defaultOfferShouldNotBeFound("price.greaterThanOrEqual=" + UPDATED_PRICE);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPriceIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where price is less than or equal to DEFAULT_PRICE
//        defaultOfferShouldBeFound("price.lessThanOrEqual=" + DEFAULT_PRICE);
//
//        // Get all the offerList where price is less than or equal to SMALLER_PRICE
//        defaultOfferShouldNotBeFound("price.lessThanOrEqual=" + SMALLER_PRICE);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPriceIsLessThanSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where price is less than DEFAULT_PRICE
//        defaultOfferShouldNotBeFound("price.lessThan=" + DEFAULT_PRICE);
//
//        // Get all the offerList where price is less than UPDATED_PRICE
//        defaultOfferShouldBeFound("price.lessThan=" + UPDATED_PRICE);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByPriceIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where price is greater than DEFAULT_PRICE
//        defaultOfferShouldNotBeFound("price.greaterThan=" + DEFAULT_PRICE);
//
//        // Get all the offerList where price is greater than SMALLER_PRICE
//        defaultOfferShouldBeFound("price.greaterThan=" + SMALLER_PRICE);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeIsEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volume equals to DEFAULT_VOLUME
//        defaultOfferShouldBeFound("volume.equals=" + DEFAULT_VOLUME);
//
//        // Get all the offerList where volume equals to UPDATED_VOLUME
//        defaultOfferShouldNotBeFound("volume.equals=" + UPDATED_VOLUME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volume not equals to DEFAULT_VOLUME
//        defaultOfferShouldNotBeFound("volume.notEquals=" + DEFAULT_VOLUME);
//
//        // Get all the offerList where volume not equals to UPDATED_VOLUME
//        defaultOfferShouldBeFound("volume.notEquals=" + UPDATED_VOLUME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeIsInShouldWork() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volume in DEFAULT_VOLUME or UPDATED_VOLUME
//        defaultOfferShouldBeFound("volume.in=" + DEFAULT_VOLUME + "," + UPDATED_VOLUME);
//
//        // Get all the offerList where volume equals to UPDATED_VOLUME
//        defaultOfferShouldNotBeFound("volume.in=" + UPDATED_VOLUME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volume is not null
//        defaultOfferShouldBeFound("volume.specified=true");
//
//        // Get all the offerList where volume is null
//        defaultOfferShouldNotBeFound("volume.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeIsGreaterThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volume is greater than or equal to DEFAULT_VOLUME
//        defaultOfferShouldBeFound("volume.greaterThanOrEqual=" + DEFAULT_VOLUME);
//
//        // Get all the offerList where volume is greater than or equal to UPDATED_VOLUME
//        defaultOfferShouldNotBeFound("volume.greaterThanOrEqual=" + UPDATED_VOLUME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeIsLessThanOrEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volume is less than or equal to DEFAULT_VOLUME
//        defaultOfferShouldBeFound("volume.lessThanOrEqual=" + DEFAULT_VOLUME);
//
//        // Get all the offerList where volume is less than or equal to SMALLER_VOLUME
//        defaultOfferShouldNotBeFound("volume.lessThanOrEqual=" + SMALLER_VOLUME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeIsLessThanSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volume is less than DEFAULT_VOLUME
//        defaultOfferShouldNotBeFound("volume.lessThan=" + DEFAULT_VOLUME);
//
//        // Get all the offerList where volume is less than UPDATED_VOLUME
//        defaultOfferShouldBeFound("volume.lessThan=" + UPDATED_VOLUME);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeIsGreaterThanSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volume is greater than DEFAULT_VOLUME
//        defaultOfferShouldNotBeFound("volume.greaterThan=" + DEFAULT_VOLUME);
//
//        // Get all the offerList where volume is greater than SMALLER_VOLUME
//        defaultOfferShouldBeFound("volume.greaterThan=" + SMALLER_VOLUME);
//    }
//
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeDivisibilityIsEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volumeDivisibility equals to DEFAULT_VOLUME_DIVISIBILITY
//        defaultOfferShouldBeFound("volumeDivisibility.equals=" + DEFAULT_VOLUME_DIVISIBILITY);
//
//        // Get all the offerList where volumeDivisibility equals to UPDATED_VOLUME_DIVISIBILITY
//        defaultOfferShouldNotBeFound("volumeDivisibility.equals=" + UPDATED_VOLUME_DIVISIBILITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeDivisibilityIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volumeDivisibility not equals to DEFAULT_VOLUME_DIVISIBILITY
//        defaultOfferShouldNotBeFound("volumeDivisibility.notEquals=" + DEFAULT_VOLUME_DIVISIBILITY);
//
//        // Get all the offerList where volumeDivisibility not equals to UPDATED_VOLUME_DIVISIBILITY
//        defaultOfferShouldBeFound("volumeDivisibility.notEquals=" + UPDATED_VOLUME_DIVISIBILITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeDivisibilityIsInShouldWork() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volumeDivisibility in DEFAULT_VOLUME_DIVISIBILITY or UPDATED_VOLUME_DIVISIBILITY
//        defaultOfferShouldBeFound("volumeDivisibility.in=" + DEFAULT_VOLUME_DIVISIBILITY + "," + UPDATED_VOLUME_DIVISIBILITY);
//
//        // Get all the offerList where volumeDivisibility equals to UPDATED_VOLUME_DIVISIBILITY
//        defaultOfferShouldNotBeFound("volumeDivisibility.in=" + UPDATED_VOLUME_DIVISIBILITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByVolumeDivisibilityIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where volumeDivisibility is not null
//        defaultOfferShouldBeFound("volumeDivisibility.specified=true");
//
//        // Get all the offerList where volumeDivisibility is null
//        defaultOfferShouldNotBeFound("volumeDivisibility.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodFromIsEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodFrom equals to DEFAULT_DELIVERY_PERIOD_FROM
//        defaultOfferShouldBeFound("deliveryPeriodFrom.equals=" + DEFAULT_DELIVERY_PERIOD_FROM);
//
//        // Get all the offerList where deliveryPeriodFrom equals to UPDATED_DELIVERY_PERIOD_FROM
//        defaultOfferShouldNotBeFound("deliveryPeriodFrom.equals=" + UPDATED_DELIVERY_PERIOD_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodFromIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodFrom not equals to DEFAULT_DELIVERY_PERIOD_FROM
//        defaultOfferShouldNotBeFound("deliveryPeriodFrom.notEquals=" + DEFAULT_DELIVERY_PERIOD_FROM);
//
//        // Get all the offerList where deliveryPeriodFrom not equals to UPDATED_DELIVERY_PERIOD_FROM
//        defaultOfferShouldBeFound("deliveryPeriodFrom.notEquals=" + UPDATED_DELIVERY_PERIOD_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodFromIsInShouldWork() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodFrom in DEFAULT_DELIVERY_PERIOD_FROM or UPDATED_DELIVERY_PERIOD_FROM
//        defaultOfferShouldBeFound("deliveryPeriodFrom.in=" + DEFAULT_DELIVERY_PERIOD_FROM + "," + UPDATED_DELIVERY_PERIOD_FROM);
//
//        // Get all the offerList where deliveryPeriodFrom equals to UPDATED_DELIVERY_PERIOD_FROM
//        defaultOfferShouldNotBeFound("deliveryPeriodFrom.in=" + UPDATED_DELIVERY_PERIOD_FROM);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodFromIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodFrom is not null
//        defaultOfferShouldBeFound("deliveryPeriodFrom.specified=true");
//
//        // Get all the offerList where deliveryPeriodFrom is null
//        defaultOfferShouldNotBeFound("deliveryPeriodFrom.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodToIsEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodTo equals to DEFAULT_DELIVERY_PERIOD_TO
//        defaultOfferShouldBeFound("deliveryPeriodTo.equals=" + DEFAULT_DELIVERY_PERIOD_TO);
//
//        // Get all the offerList where deliveryPeriodTo equals to UPDATED_DELIVERY_PERIOD_TO
//        defaultOfferShouldNotBeFound("deliveryPeriodTo.equals=" + UPDATED_DELIVERY_PERIOD_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodToIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodTo not equals to DEFAULT_DELIVERY_PERIOD_TO
//        defaultOfferShouldNotBeFound("deliveryPeriodTo.notEquals=" + DEFAULT_DELIVERY_PERIOD_TO);
//
//        // Get all the offerList where deliveryPeriodTo not equals to UPDATED_DELIVERY_PERIOD_TO
//        defaultOfferShouldBeFound("deliveryPeriodTo.notEquals=" + UPDATED_DELIVERY_PERIOD_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodToIsInShouldWork() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodTo in DEFAULT_DELIVERY_PERIOD_TO or UPDATED_DELIVERY_PERIOD_TO
//        defaultOfferShouldBeFound("deliveryPeriodTo.in=" + DEFAULT_DELIVERY_PERIOD_TO + "," + UPDATED_DELIVERY_PERIOD_TO);
//
//        // Get all the offerList where deliveryPeriodTo equals to UPDATED_DELIVERY_PERIOD_TO
//        defaultOfferShouldNotBeFound("deliveryPeriodTo.in=" + UPDATED_DELIVERY_PERIOD_TO);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodToIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodTo is not null
//        defaultOfferShouldBeFound("deliveryPeriodTo.specified=true");
//
//        // Get all the offerList where deliveryPeriodTo is null
//        defaultOfferShouldNotBeFound("deliveryPeriodTo.specified=false");
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodDivisibilityIsEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodDivisibility equals to DEFAULT_DELIVERY_PERIOD_DIVISIBILITY
//        defaultOfferShouldBeFound("deliveryPeriodDivisibility.equals=" + DEFAULT_DELIVERY_PERIOD_DIVISIBILITY);
//
//        // Get all the offerList where deliveryPeriodDivisibility equals to UPDATED_DELIVERY_PERIOD_DIVISIBILITY
//        defaultOfferShouldNotBeFound("deliveryPeriodDivisibility.equals=" + UPDATED_DELIVERY_PERIOD_DIVISIBILITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodDivisibilityIsNotEqualToSomething() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodDivisibility not equals to DEFAULT_DELIVERY_PERIOD_DIVISIBILITY
//        defaultOfferShouldNotBeFound("deliveryPeriodDivisibility.notEquals=" + DEFAULT_DELIVERY_PERIOD_DIVISIBILITY);
//
//        // Get all the offerList where deliveryPeriodDivisibility not equals to UPDATED_DELIVERY_PERIOD_DIVISIBILITY
//        defaultOfferShouldBeFound("deliveryPeriodDivisibility.notEquals=" + UPDATED_DELIVERY_PERIOD_DIVISIBILITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodDivisibilityIsInShouldWork() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodDivisibility in DEFAULT_DELIVERY_PERIOD_DIVISIBILITY or UPDATED_DELIVERY_PERIOD_DIVISIBILITY
//        defaultOfferShouldBeFound("deliveryPeriodDivisibility.in=" + DEFAULT_DELIVERY_PERIOD_DIVISIBILITY + "," + UPDATED_DELIVERY_PERIOD_DIVISIBILITY);
//
//        // Get all the offerList where deliveryPeriodDivisibility equals to UPDATED_DELIVERY_PERIOD_DIVISIBILITY
//        defaultOfferShouldNotBeFound("deliveryPeriodDivisibility.in=" + UPDATED_DELIVERY_PERIOD_DIVISIBILITY);
//    }
//
//    @Test
//    @Transactional
//    public void getAllOffersByDeliveryPeriodDivisibilityIsNullOrNotNull() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        // Get all the offerList where deliveryPeriodDivisibility is not null
//        defaultOfferShouldBeFound("deliveryPeriodDivisibility.specified=true");
//
//        // Get all the offerList where deliveryPeriodDivisibility is null
//        defaultOfferShouldNotBeFound("deliveryPeriodDivisibility.specified=false");
//    }
//    /**
//     * Executes the search, and checks that the default entity is returned.
//     */
//    private void defaultOfferShouldBeFound(String filter) throws Exception {
//        restOfferMockMvc.perform(get("/api/offers?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$.[*].id").value(hasItem(offerEntity.getId().intValue())))
//            .andExpect(jsonPath("$.[*].potentialId").value(hasItem(DEFAULT_POTENTIAL_ID.intValue())))
//            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE.intValue())))
//            .andExpect(jsonPath("$.[*].volume").value(hasItem(DEFAULT_VOLUME.intValue())))
//            .andExpect(jsonPath("$.[*].volumeDivisibility").value(hasItem(DEFAULT_VOLUME_DIVISIBILITY.booleanValue())))
//            .andExpect(jsonPath("$.[*].deliveryPeriodFrom").value(hasItem(DEFAULT_DELIVERY_PERIOD_FROM.toString())))
//            .andExpect(jsonPath("$.[*].deliveryPeriodTo").value(hasItem(DEFAULT_DELIVERY_PERIOD_TO.toString())))
//            .andExpect(jsonPath("$.[*].deliveryPeriodDivisibility").value(hasItem(DEFAULT_DELIVERY_PERIOD_DIVISIBILITY.booleanValue())));
//
//        // Check, that the count call also returns 1
//        restOfferMockMvc.perform(get("/api/offers/count?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(content().string("1"));
//    }
//
//    /**
//     * Executes the search, and checks that the default entity is not returned.
//     */
//    private void defaultOfferShouldNotBeFound(String filter) throws Exception {
//        restOfferMockMvc.perform(get("/api/offers?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(jsonPath("$").isArray())
//            .andExpect(jsonPath("$").isEmpty());
//
//        // Check, that the count call also returns 0
//        restOfferMockMvc.perform(get("/api/offers/count?sort=id,desc&" + filter))
//            .andExpect(status().isOk())
//            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
//            .andExpect(content().string("0"));
//    }
//
//    @Test
//    @Transactional
//    public void getNonExistingOffer() throws Exception {
//        // Get the offer
//        restOfferMockMvc.perform(get("/api/offers/{id}", Long.MAX_VALUE))
//            .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @Transactional
//    public void updateOffer() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        int databaseSizeBeforeUpdate = offerRepository.findAll().size();
//
//        // Update the offer
//        OfferEntity updatedOfferEntity = offerRepository.findById(offerEntity.getId()).get();
//        // Disconnect from session so that the updates on updatedOfferEntity are not directly saved in db
//        em.detach(updatedOfferEntity);
//        updatedOfferEntity
//            .potentialId(UPDATED_POTENTIAL_ID)
//            .price(UPDATED_PRICE)
//            .volume(UPDATED_VOLUME)
//            .volumeDivisibility(UPDATED_VOLUME_DIVISIBILITY)
//            .deliveryPeriodFrom(UPDATED_DELIVERY_PERIOD_FROM)
//            .deliveryPeriodTo(UPDATED_DELIVERY_PERIOD_TO)
//            .deliveryPeriodDivisibility(UPDATED_DELIVERY_PERIOD_DIVISIBILITY);
//        OfferDTO offerDTO = offerMapper.toDto(updatedOfferEntity);
//
//        restOfferMockMvc.perform(put("/api/offers")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(TestUtil.convertObjectToJsonBytes(offerDTO)))
//            .andExpect(status().isOk());
//
//        // Validate the Offer in the database
//        List<OfferEntity> offerList = offerRepository.findAll();
//        assertThat(offerList).hasSize(databaseSizeBeforeUpdate);
//        OfferEntity testOffer = offerList.get(offerList.size() - 1);
//        assertThat(testOffer.getPotentialId()).isEqualTo(UPDATED_POTENTIAL_ID);
//        assertThat(testOffer.getPrice()).isEqualTo(UPDATED_PRICE);
//        assertThat(testOffer.getVolume()).isEqualTo(UPDATED_VOLUME);
//        assertThat(testOffer.isVolumeDivisibility()).isEqualTo(UPDATED_VOLUME_DIVISIBILITY);
//        assertThat(testOffer.getDeliveryPeriodFrom()).isEqualTo(UPDATED_DELIVERY_PERIOD_FROM);
//        assertThat(testOffer.getDeliveryPeriodTo()).isEqualTo(UPDATED_DELIVERY_PERIOD_TO);
//        assertThat(testOffer.isDeliveryPeriodDivisibility()).isEqualTo(UPDATED_DELIVERY_PERIOD_DIVISIBILITY);
//    }
//
//    @Test
//    @Transactional
//    public void updateNonExistingOffer() throws Exception {
//        int databaseSizeBeforeUpdate = offerRepository.findAll().size();
//
//        // Create the Offer
//        OfferDTO offerDTO = offerMapper.toDto(offerEntity);
//
//        // If the entity doesn't have an ID, it will throw BadRequestAlertException
//        restOfferMockMvc.perform(put("/api/offers")
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(TestUtil.convertObjectToJsonBytes(offerDTO)))
//            .andExpect(status().isBadRequest());
//
//        // Validate the Offer in the database
//        List<OfferEntity> offerList = offerRepository.findAll();
//        assertThat(offerList).hasSize(databaseSizeBeforeUpdate);
//    }
//
//    @Test
//    @Transactional
//    public void deleteOffer() throws Exception {
//        // Initialize the database
//        offerRepository.saveAndFlush(offerEntity);
//
//        int databaseSizeBeforeDelete = offerRepository.findAll().size();
//
//        // Delete the offer
//        restOfferMockMvc.perform(delete("/api/offers/{id}", offerEntity.getId())
//            .accept(MediaType.APPLICATION_JSON))
//            .andExpect(status().isNoContent());
//
//        // Validate the database contains one less item
//        List<OfferEntity> offerList = offerRepository.findAll();
//        assertThat(offerList).hasSize(databaseSizeBeforeDelete - 1);
//    }
//}
