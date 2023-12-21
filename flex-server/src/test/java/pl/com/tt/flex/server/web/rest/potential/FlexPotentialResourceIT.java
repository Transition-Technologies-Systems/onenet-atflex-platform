package pl.com.tt.flex.server.web.rest.potential;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.product.ProductMinDTO;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.fsp.FspResourceAdminIT;
import pl.com.tt.flex.server.web.rest.product.ProductResourceIT;
import pl.com.tt.flex.server.web.rest.unit.UnitResourceIT;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

/**
 * Integration tests for the {@link FlexPotentialResource} REST controller.
 */
public abstract class FlexPotentialResourceIT {

    private static final Long SMALLER_PRODUCT_ID = 1L - 1L;

    private static final Long SMALLER_FSP_ID = 1L - 1L;

    private static final BigDecimal DEFAULT_VOLUME = new BigDecimal(60);
    private static final BigDecimal UPDATED_VOLUME = new BigDecimal(80);
    private static final BigDecimal SMALLER_VOLUME = new BigDecimal(1 - 1);

    private static final ProductBidSizeUnit DEFAULT_VOLUME_UNIT = ProductBidSizeUnit.kW;
    private static final ProductBidSizeUnit UPDATED_VOLUME_UNIT = ProductBidSizeUnit.KWH;

    private static final Instant DEFAULT_VALID_FROM = Instant.now().truncatedTo(SECONDS);
    private static final Instant UPDATED_VALID_FROM = Instant.now().truncatedTo(SECONDS);

    private static final Instant DEFAULT_VALID_TO = Instant.now().plus(10, DAYS).truncatedTo(SECONDS);
    private static final Instant UPDATED_VALID_TO = Instant.now().plus(11, DAYS).truncatedTo(SECONDS);

    private static final Boolean DEFAULT_ACTIVE = false;
    private static final Boolean UPDATED_ACTIVE = true;

    private static final Boolean DEFAULT_PRODUCT_PREQ_NEEDED = false;
    private static final Boolean UPDATED_PRODUCT_PREQ_NEEDED = true;

    private static final Boolean DEFAULT_PRODUCT_PREQUALIFICATION = false;
    private static final Boolean UPDATED_PRODUCT_PREQUALIFICATION = true;

    private static final Boolean DEFAULT_STATIC_GRID_PREQ_NEEDED = false;
    private static final Boolean UPDATED_STATIC_GRID_PREQ_NEEDED = true;

    private static final Boolean DEFAULT_STATIC_GRID_PREQUALIFICATION = false;
    private static final Boolean UPDATED_STATIC_GRID_PREQUALIFICATION = true;

    private static final Long DEFAULT_VERSION = 1L;
    private static final Long UPDATED_VERSION = 2L;

    private static final Integer DEFAULT_FULL_ACTIVATION_TIME = 60;

    private static final Integer DEFAULT_MIN_DELIVERY_DURATION = 60;

    private static final Boolean DEFAULT_AGGREGATED = true;

    static final String DEFAULT_USERNAME = "fspUser";

    private final FlexPotentialRepository flexPotentialRepository;
    private final FlexPotentialMapper flexPotentialMapper;
    private final EntityManager em;
    private final MockMvc restFlexPotentialMockMvc;
    private final String requestUri;

    @MockBean
    protected UserService mockUserService;

    protected FlexPotentialEntity flexPotentialEntity;
    protected static ProductEntity productEntity;
    protected static FspEntity fspEntity;
    protected static UnitEntity unitEntity;

    public FlexPotentialResourceIT(FlexPotentialRepository flexPotentialRepository, FlexPotentialMapper flexPotentialMapper,
                                   EntityManager em, MockMvc restFlexPotentialMockMvc, String requestUri) {
        this.flexPotentialRepository = flexPotentialRepository;
        this.flexPotentialMapper = flexPotentialMapper;
        this.em = em;
        this.restFlexPotentialMockMvc = restFlexPotentialMockMvc;
        this.requestUri = requestUri;
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FlexPotentialEntity createEntity(EntityManager em) {

        unitEntity = createUnitEntity(em);
        productEntity = createProductEntity(em);
        fspEntity = createFspEntity(em);
        return FlexPotentialEntity.builder()
            .fsp(fspEntity)
            .volume(DEFAULT_VOLUME)
            .volumeUnit(DEFAULT_VOLUME_UNIT)
            .validFrom(DEFAULT_VALID_FROM)
            .validTo(DEFAULT_VALID_TO)
            .active(DEFAULT_ACTIVE)
            .units(Set.of(unitEntity))
            .productPrequalification(DEFAULT_PRODUCT_PREQUALIFICATION)
            .staticGridPrequalification(DEFAULT_STATIC_GRID_PREQUALIFICATION)
            .fullActivationTime(DEFAULT_FULL_ACTIVATION_TIME)
            .minDeliveryDuration(DEFAULT_MIN_DELIVERY_DURATION)
            .aggregated(DEFAULT_AGGREGATED)
            .product(productEntity)
            .files(new HashSet<>())
            .createdByRole(fspEntity.getRole().toString())
            .build();
    }

    private static FspEntity createFspEntity(EntityManager em) {
        FspEntity fspEntity;
        if (TestUtil.findAll(em, FspEntity.class).isEmpty()) {
            fspEntity = FspResourceAdminIT.createEntity(em);
            fspEntity.setActive(true);
            em.persist(fspEntity);
            em.flush();
        } else {
            fspEntity = TestUtil.findAll(em, FspEntity.class).get(0);
        }
        return fspEntity;
    }

    private static UnitEntity createUnitEntity(EntityManager em) {
        UnitEntity unitEntity;
        if (TestUtil.findAll(em, UnitEntity.class).isEmpty()) {
            unitEntity = UnitResourceIT.createEntity(em);
            unitEntity.setActive(true);
            em.persist(unitEntity);
            em.flush();
        } else {
            unitEntity = TestUtil.findAll(em, UnitEntity.class).get(0);
            unitEntity.setActive(true);
            em.persist(unitEntity);
            em.flush();
        }
        return unitEntity;
    }

    private static UnitEntity createNoActiveUnitEntity(EntityManager em) {
        UnitEntity unitEntity;
        if (TestUtil.findAll(em, UnitEntity.class).isEmpty()) {
            unitEntity = UnitResourceIT.createEntity(em);
            unitEntity.setActive(false);
            em.persist(unitEntity);
            em.flush();
        } else {
            unitEntity = TestUtil.findAll(em, UnitEntity.class).get(0);
            unitEntity.setActive(false);
            em.persist(unitEntity);
            em.flush();
        }
        return unitEntity;
    }

    private static ProductEntity createProductEntity(EntityManager em) {
        ProductEntity productEntity;
        if (TestUtil.findAll(em, ProductEntity.class).isEmpty()) {
            productEntity = ProductResourceIT.createEntity(em);
            productEntity.setActive(true);
            productEntity.setValidFrom(Instant.now().minus(2, DAYS).truncatedTo(SECONDS));
            productEntity.setValidTo(Instant.now().plus(10, DAYS).truncatedTo(SECONDS));
            em.persist(productEntity);
            em.flush();
        } else {
            productEntity = TestUtil.findAll(em, ProductEntity.class).get(0);
            productEntity.setActive(true);
            em.persist(productEntity);
            em.flush();
        }
        return productEntity;
    }

    private static ProductEntity createNoActiveProductEntity(EntityManager em) {
        ProductEntity productEntity;
        if (TestUtil.findAll(em, ProductEntity.class).isEmpty()) {
            productEntity = ProductResourceIT.createEntity(em);
            productEntity.setActive(false);
            productEntity.setValidFrom(Instant.now().minus(2, DAYS).truncatedTo(SECONDS));
            em.persist(productEntity);
            em.flush();
        } else {
            productEntity = TestUtil.findAll(em, ProductEntity.class).get(0);
            productEntity.setActive(false);
            em.persist(productEntity);
            em.flush();
        }
        return productEntity;
    }

    @BeforeEach
    public void initTest() {
        flexPotentialEntity = createEntity(em);
        mockedCurrentLoggedUser();
    }

    abstract void mockedCurrentLoggedUser();

    @Test
    @Transactional
    public void createFlexPotential() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(status().isCreated());

        // Validate the FlexPotential in the database
        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate + 1);
        FlexPotentialEntity testFlexPotential = flexPotentialList.get(flexPotentialList.size() - 1);
        assertThat(testFlexPotential.getVolume()).isEqualTo(DEFAULT_VOLUME);
        assertThat(testFlexPotential.getVolumeUnit()).isEqualTo(DEFAULT_VOLUME_UNIT);
        assertThat(testFlexPotential.getValidFrom()).isEqualTo(DEFAULT_VALID_FROM);
        assertThat(testFlexPotential.getValidTo()).isEqualTo(DEFAULT_VALID_TO);
        assertThat(testFlexPotential.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testFlexPotential.isProductPrequalification()).isEqualTo(DEFAULT_PRODUCT_PREQUALIFICATION);
        assertThat(testFlexPotential.isStaticGridPrequalification()).isEqualTo(DEFAULT_STATIC_GRID_PREQUALIFICATION);
        assertThat(testFlexPotential.getVersion()).isEqualTo(DEFAULT_VERSION);
    }

    @Test
    @Transactional
    public void createFlexPotentialWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();

        // Create the FlexPotential with an existing ID
        flexPotentialEntity.setId(1L);
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);

        // An entity with an existing ID cannot be created, so this API call must fail
        restFlexPotentialMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(flexPotentialDTO)))
            .andExpect(status().isBadRequest());

        // Validate the FlexPotential in the database
        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllFlexPotentials() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList
        restFlexPotentialMockMvc.perform(get(requestUri + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(flexPotentialEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].product.id").value(hasItem(productEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].fsp.id").value(hasItem(fspEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].units.[*].id").value(hasItem(unitEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].volume").value(hasItem(DEFAULT_VOLUME.intValue())))
            .andExpect(jsonPath("$.[*].volumeUnit").value(hasItem(DEFAULT_VOLUME_UNIT.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(flexPotentialEntity.getLastModifiedDate().toString())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(DEFAULT_USERNAME)))
            .andExpect(jsonPath("$.[*].lastModifiedBy").value(hasItem(DEFAULT_USERNAME)))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validTo").value(hasItem(DEFAULT_VALID_TO.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].productPreqNeeded").value(hasItem(DEFAULT_PRODUCT_PREQ_NEEDED)))
            .andExpect(jsonPath("$.[*].productPrequalification").value(hasItem(DEFAULT_PRODUCT_PREQUALIFICATION)))
            .andExpect(jsonPath("$.[*].staticGridPrequalification").value(hasItem(DEFAULT_STATIC_GRID_PREQ_NEEDED)))
            .andExpect(jsonPath("$.[*].staticGridPrequalification").value(hasItem(DEFAULT_STATIC_GRID_PREQUALIFICATION)))
            .andExpect(jsonPath("$.[*].version").value(hasItem(flexPotentialEntity.getVersion().intValue())));
    }

    @Test
    @Transactional
    public void getFlexPotential() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get the flexPotential
        restFlexPotentialMockMvc.perform(get(requestUri + "/{id}", flexPotentialEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(flexPotentialEntity.getId().intValue()))
            .andExpect(jsonPath("$.product.id").value(productEntity.getId().intValue()))
            .andExpect(jsonPath("$.fsp.id").value(fspEntity.getId().intValue()))
            .andExpect(jsonPath("$.units.[*].id").value(unitEntity.getId().intValue()))
            .andExpect(jsonPath("$.volume").value(DEFAULT_VOLUME.intValue()))
            .andExpect(jsonPath("$.volumeUnit").value(DEFAULT_VOLUME_UNIT.toString()))
            .andExpect(jsonPath("$.lastModifiedDate").value(flexPotentialEntity.getLastModifiedDate().toString()))
            .andExpect(jsonPath("$.createdBy").value(DEFAULT_USERNAME))
            .andExpect(jsonPath("$.lastModifiedBy").value(DEFAULT_USERNAME))
            .andExpect(jsonPath("$.validFrom").value(DEFAULT_VALID_FROM.toString()))
            .andExpect(jsonPath("$.validTo").value(DEFAULT_VALID_TO.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE))
            .andExpect(jsonPath("$.productPreqNeeded").value(DEFAULT_PRODUCT_PREQ_NEEDED))
            .andExpect(jsonPath("$.productPrequalification").value(DEFAULT_PRODUCT_PREQUALIFICATION))
            .andExpect(jsonPath("$.staticGridPrequalification").value(DEFAULT_STATIC_GRID_PREQ_NEEDED))
            .andExpect(jsonPath("$.staticGridPrequalification").value(DEFAULT_STATIC_GRID_PREQUALIFICATION))
            .andExpect(jsonPath("$.version").value(flexPotentialEntity.getVersion().intValue()));
    }


    @Test
    @Transactional
    public void getFlexPotentialsByIdFiltering() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        Long id = flexPotentialEntity.getId();

        defaultFlexPotentialShouldBeFound("id.equals=" + id);
        defaultFlexPotentialShouldNotBeFound("id.notEquals=" + id);

        defaultFlexPotentialShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultFlexPotentialShouldNotBeFound("id.greaterThan=" + id);

        defaultFlexPotentialShouldBeFound("id.lessThanOrEqual=" + id);
        defaultFlexPotentialShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllFlexPotentialsByProductIdIsEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productId equals to DEFAULT_PRODUCT_ID
        defaultFlexPotentialShouldBeFound("productId.equals=" + productEntity.getId());

        // Get all the flexPotentialList where productId equals to UPDATED_PRODUCT_ID
        defaultFlexPotentialShouldNotBeFound("productId.equals=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByProductIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productId not equals to DEFAULT_PRODUCT_ID
        defaultFlexPotentialShouldNotBeFound("productId.notEquals=" + productEntity.getId());

        // Get all the flexPotentialList where productId not equals to UPDATED_PRODUCT_ID
        defaultFlexPotentialShouldBeFound("productId.notEquals=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByProductIdIsInShouldWork() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productId in DEFAULT_PRODUCT_ID or UPDATED_PRODUCT_ID
        defaultFlexPotentialShouldBeFound("productId.in=" + productEntity.getId() + "," + Long.MAX_VALUE);

        // Get all the flexPotentialList where productId equals to UPDATED_PRODUCT_ID
        defaultFlexPotentialShouldNotBeFound("productId.in=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByProductIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productId is not null
        defaultFlexPotentialShouldBeFound("productId.specified=true");

        // Get all the flexPotentialList where productId is null
        defaultFlexPotentialShouldNotBeFound("productId.specified=false");
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByProductIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productId is greater than or equal to DEFAULT_PRODUCT_ID
        defaultFlexPotentialShouldBeFound("productId.greaterThanOrEqual=" + productEntity.getId());

        // Get all the flexPotentialList where productId is greater than or equal to UPDATED_PRODUCT_ID
        defaultFlexPotentialShouldNotBeFound("productId.greaterThanOrEqual=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByProductIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productId is less than or equal to DEFAULT_PRODUCT_ID
        defaultFlexPotentialShouldBeFound("productId.lessThanOrEqual=" + productEntity.getId());

        // Get all the flexPotentialList where productId is less than or equal to SMALLER_PRODUCT_ID
        defaultFlexPotentialShouldNotBeFound("productId.lessThanOrEqual=" + SMALLER_PRODUCT_ID);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByProductIdIsLessThanSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productId is less than DEFAULT_PRODUCT_ID
        defaultFlexPotentialShouldNotBeFound("productId.lessThan=" + productEntity.getId());

        // Get all the flexPotentialList where productId is less than UPDATED_PRODUCT_ID
        defaultFlexPotentialShouldBeFound("productId.lessThan=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByProductIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productId is greater than DEFAULT_PRODUCT_ID
        defaultFlexPotentialShouldNotBeFound("productId.greaterThan=" + productEntity.getId());

        // Get all the flexPotentialList where productId is greater than SMALLER_PRODUCT_ID
        defaultFlexPotentialShouldBeFound("productId.greaterThan=" + SMALLER_PRODUCT_ID);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeIsEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volume equals to DEFAULT_VOLUME
        defaultFlexPotentialShouldBeFound("volume.equals=" + DEFAULT_VOLUME);

        // Get all the flexPotentialList where volume equals to UPDATED_VOLUME
        defaultFlexPotentialShouldNotBeFound("volume.equals=" + UPDATED_VOLUME);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volume not equals to DEFAULT_VOLUME
        defaultFlexPotentialShouldNotBeFound("volume.notEquals=" + DEFAULT_VOLUME);

        // Get all the flexPotentialList where volume not equals to UPDATED_VOLUME
        defaultFlexPotentialShouldBeFound("volume.notEquals=" + UPDATED_VOLUME);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeIsInShouldWork() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volume in DEFAULT_VOLUME or UPDATED_VOLUME
        defaultFlexPotentialShouldBeFound("volume.in=" + DEFAULT_VOLUME + "," + UPDATED_VOLUME);

        // Get all the flexPotentialList where volume equals to UPDATED_VOLUME
        defaultFlexPotentialShouldNotBeFound("volume.in=" + UPDATED_VOLUME);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeIsNullOrNotNull() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volume is not null
        defaultFlexPotentialShouldBeFound("volume.specified=true");

        // Get all the flexPotentialList where volume is null
        defaultFlexPotentialShouldNotBeFound("volume.specified=false");
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volume is greater than or equal to DEFAULT_VOLUME
        defaultFlexPotentialShouldBeFound("volume.greaterThanOrEqual=" + DEFAULT_VOLUME);

        // Get all the flexPotentialList where volume is greater than or equal to UPDATED_VOLUME
        defaultFlexPotentialShouldNotBeFound("volume.greaterThanOrEqual=" + UPDATED_VOLUME);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volume is less than or equal to DEFAULT_VOLUME
        defaultFlexPotentialShouldBeFound("volume.lessThanOrEqual=" + DEFAULT_VOLUME);

        // Get all the flexPotentialList where volume is less than or equal to SMALLER_VOLUME
        defaultFlexPotentialShouldNotBeFound("volume.lessThanOrEqual=" + SMALLER_VOLUME);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeIsLessThanSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volume is less than DEFAULT_VOLUME
        defaultFlexPotentialShouldNotBeFound("volume.lessThan=" + DEFAULT_VOLUME);

        // Get all the flexPotentialList where volume is less than UPDATED_VOLUME
        defaultFlexPotentialShouldBeFound("volume.lessThan=" + UPDATED_VOLUME);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volume is greater than DEFAULT_VOLUME
        defaultFlexPotentialShouldNotBeFound("volume.greaterThan=" + DEFAULT_VOLUME);

        // Get all the flexPotentialList where volume is greater than SMALLER_VOLUME
        defaultFlexPotentialShouldBeFound("volume.greaterThan=" + SMALLER_VOLUME);
    }


    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeUnitIsEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volumeUnit equals to DEFAULT_VOLUME_UNIT
        defaultFlexPotentialShouldBeFound("volumeUnit.equals=" + DEFAULT_VOLUME_UNIT);

        // Get all the flexPotentialList where volumeUnit equals to UPDATED_VOLUME_UNIT
        defaultFlexPotentialShouldNotBeFound("volumeUnit.equals=" + UPDATED_VOLUME_UNIT);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeUnitIsNotEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volumeUnit not equals to DEFAULT_VOLUME_UNIT
        defaultFlexPotentialShouldNotBeFound("volumeUnit.notEquals=" + DEFAULT_VOLUME_UNIT);

        // Get all the flexPotentialList where volumeUnit not equals to UPDATED_VOLUME_UNIT
        defaultFlexPotentialShouldBeFound("volumeUnit.notEquals=" + UPDATED_VOLUME_UNIT);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeUnitIsInShouldWork() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volumeUnit in DEFAULT_VOLUME_UNIT or UPDATED_VOLUME_UNIT
        defaultFlexPotentialShouldBeFound("volumeUnit.in=" + DEFAULT_VOLUME_UNIT + "," + UPDATED_VOLUME_UNIT);

        // Get all the flexPotentialList where volumeUnit equals to UPDATED_VOLUME_UNIT
        defaultFlexPotentialShouldNotBeFound("volumeUnit.in=" + UPDATED_VOLUME_UNIT);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVolumeUnitIsNullOrNotNull() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where volumeUnit is not null
        defaultFlexPotentialShouldBeFound("volumeUnit.specified=true");

        // Get all the flexPotentialList where volumeUnit is null
        defaultFlexPotentialShouldNotBeFound("volumeUnit.specified=false");
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByCreationDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where creationDate is not null
        defaultFlexPotentialShouldBeFound("createdDate.specified=true");

        // Get all the flexPotentialList where creationDate is null
        defaultFlexPotentialShouldNotBeFound("createdDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByCreatedDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialEntity = flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where creationDate is not null
        defaultFlexPotentialShouldBeFound("createdDate.greaterThanOrEqual=" + flexPotentialEntity.getCreatedDate().truncatedTo(SECONDS));

        // Get all the flexPotentialList where creationDate is null
        defaultFlexPotentialShouldNotBeFound("createdDate.greaterThanOrEqual=" + Instant.now().plus(2, DAYS));
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByLastModifiedDateIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialEntity = flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where creationDate is not null
        defaultFlexPotentialShouldBeFound("lastModifiedDate.greaterThanOrEqual=" + flexPotentialEntity.getLastModifiedDate().truncatedTo(SECONDS));

        // Get all the flexPotentialList where creationDate is null
        defaultFlexPotentialShouldNotBeFound("lastModifiedDate.greaterThanOrEqual=" + Instant.now().plus(2, DAYS));
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByLastModifiedDateIsNullOrNotNull() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where lastModifiedDate is not null
        defaultFlexPotentialShouldBeFound("lastModifiedDate.specified=true");

        // Get all the flexPotentialList where lastModifiedDate is null
        defaultFlexPotentialShouldNotBeFound("lastModifiedDate.specified=false");
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByValidFromIsEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where validFrom equals to DEFAULT_VALID_FROM
        defaultFlexPotentialShouldBeFound("validFrom.equals=" + flexPotentialEntity.getValidFrom());

        // Get all the flexPotentialList where validFrom equals to UPDATED_VALID_FROM
        defaultFlexPotentialShouldNotBeFound("validFrom.equals=" + Instant.ofEpochMilli(0));
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByValidFromIsNotEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where validFrom not equals to DEFAULT_VALID_FROM
        defaultFlexPotentialShouldNotBeFound("validFrom.notEquals=" + flexPotentialEntity.getValidFrom());

        // Get all the flexPotentialList where validFrom not equals to UPDATED_VALID_FROM
        defaultFlexPotentialShouldBeFound("validFrom.notEquals=" + Instant.ofEpochMilli(0));
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByValidFromIsInShouldWork() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where validFrom in DEFAULT_VALID_FROM or UPDATED_VALID_FROM
        defaultFlexPotentialShouldBeFound("validFrom.in=" + flexPotentialEntity.getValidFrom() + "," + Instant.ofEpochMilli(0));

        // Get all the flexPotentialList where validFrom equals to UPDATED_VALID_FROM
        defaultFlexPotentialShouldNotBeFound("validFrom.in=" + Instant.ofEpochMilli(0));
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByValidFromIsNullOrNotNull() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where validFrom is not null
        defaultFlexPotentialShouldBeFound("validFrom.specified=true");

        // Get all the flexPotentialList where validFrom is null
        defaultFlexPotentialShouldNotBeFound("validFrom.specified=false");
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByValidToIsEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where validTo equals to DEFAULT_VALID_TO
        defaultFlexPotentialShouldBeFound("validTo.equals=" + DEFAULT_VALID_TO);

        // Get all the flexPotentialList where validTo equals to UPDATED_VALID_TO
        defaultFlexPotentialShouldNotBeFound("validTo.equals=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByValidToIsNotEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where validTo not equals to DEFAULT_VALID_TO
        defaultFlexPotentialShouldNotBeFound("validTo.notEquals=" + DEFAULT_VALID_TO);

        // Get all the flexPotentialList where validTo not equals to UPDATED_VALID_TO
        defaultFlexPotentialShouldBeFound("validTo.notEquals=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByValidToIsInShouldWork() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where validTo in DEFAULT_VALID_TO or UPDATED_VALID_TO
        defaultFlexPotentialShouldBeFound("validTo.in=" + DEFAULT_VALID_TO + "," + UPDATED_VALID_TO);

        // Get all the flexPotentialList where validTo equals to UPDATED_VALID_TO
        defaultFlexPotentialShouldNotBeFound("validTo.in=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByValidToIsNullOrNotNull() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where validTo is not null
        defaultFlexPotentialShouldBeFound("validTo.specified=true");

        // Get all the flexPotentialList where validTo is null
        defaultFlexPotentialShouldNotBeFound("validTo.specified=false");
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where active equals to DEFAULT_ACTIVE
        defaultFlexPotentialShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the flexPotentialList where active equals to UPDATED_ACTIVE
        defaultFlexPotentialShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where active not equals to DEFAULT_ACTIVE
        defaultFlexPotentialShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the flexPotentialList where active not equals to UPDATED_ACTIVE
        defaultFlexPotentialShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultFlexPotentialShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the flexPotentialList where active equals to UPDATED_ACTIVE
        defaultFlexPotentialShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where active is not null
        defaultFlexPotentialShouldBeFound("active.specified=true");

        // Get all the flexPotentialList where active is null
        defaultFlexPotentialShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByProductPrequalificationIsEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productPrequalification equals to DEFAULT_PRODUCT_PREQUALIFICATION
        defaultFlexPotentialShouldBeFound("productPrequalification.equals=" + DEFAULT_PRODUCT_PREQUALIFICATION);

        // Get all the flexPotentialList where productPrequalification equals to UPDATED_PRODUCT_PREQUALIFICATION
        defaultFlexPotentialShouldNotBeFound("productPrequalification.equals=" + UPDATED_PRODUCT_PREQUALIFICATION);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByProductPrequalificationIsNotEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productPrequalification not equals to DEFAULT_PRODUCT_PREQUALIFICATION
        defaultFlexPotentialShouldNotBeFound("productPrequalification.notEquals=" + DEFAULT_PRODUCT_PREQUALIFICATION);

        // Get all the flexPotentialList where productPrequalification not equals to UPDATED_PRODUCT_PREQUALIFICATION
        defaultFlexPotentialShouldBeFound("productPrequalification.notEquals=" + UPDATED_PRODUCT_PREQUALIFICATION);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByProductPrequalificationIsInShouldWork() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productPrequalification in DEFAULT_PRODUCT_PREQUALIFICATION or UPDATED_PRODUCT_PREQUALIFICATION
        defaultFlexPotentialShouldBeFound("productPrequalification.in=" + DEFAULT_PRODUCT_PREQUALIFICATION + "," + UPDATED_PRODUCT_PREQUALIFICATION);

        // Get all the flexPotentialList where productPrequalification equals to UPDATED_PRODUCT_PREQUALIFICATION
        defaultFlexPotentialShouldNotBeFound("productPrequalification.in=" + UPDATED_PRODUCT_PREQUALIFICATION);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByProductPrequalificationIsNullOrNotNull() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where productPrequalification is not null
        defaultFlexPotentialShouldBeFound("productPrequalification.specified=true");

        // Get all the flexPotentialList where productPrequalification is null
        defaultFlexPotentialShouldNotBeFound("productPrequalification.specified=false");
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVersionIsEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where version equals to DEFAULT_VERSION
        defaultFlexPotentialShouldBeFound("version.equals=" + flexPotentialEntity.getVersion());

        // Get all the flexPotentialList where version equals to UPDATED_VERSION
        defaultFlexPotentialShouldNotBeFound("version.equals=" + UPDATED_VERSION);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVersionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where version not equals to DEFAULT_VERSION
        defaultFlexPotentialShouldNotBeFound("version.notEquals=" + flexPotentialEntity.getVersion());

        // Get all the flexPotentialList where version not equals to UPDATED_VERSION
        defaultFlexPotentialShouldBeFound("version.notEquals=" + UPDATED_VERSION);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVersionIsInShouldWork() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where version in DEFAULT_VERSION or UPDATED_VERSION
        defaultFlexPotentialShouldBeFound("version.in=" + flexPotentialEntity.getVersion() + "," + UPDATED_VERSION);

        // Get all the flexPotentialList where version equals to UPDATED_VERSION
        defaultFlexPotentialShouldNotBeFound("version.in=" + UPDATED_VERSION);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVersionIsNullOrNotNull() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where version is not null
        defaultFlexPotentialShouldBeFound("version.specified=true");

        // Get all the flexPotentialList where version is null
        defaultFlexPotentialShouldNotBeFound("version.specified=false");
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVersionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where version is greater than or equal to DEFAULT_VERSION
        defaultFlexPotentialShouldBeFound("version.greaterThanOrEqual=" + flexPotentialEntity.getVersion());

        // Get all the flexPotentialList where version is greater than or equal to UPDATED_VERSION
        defaultFlexPotentialShouldNotBeFound("version.greaterThanOrEqual=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVersionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where version is less than or equal to DEFAULT_VERSION
        defaultFlexPotentialShouldBeFound("version.lessThanOrEqual=" + flexPotentialEntity.getVersion());

        // Get all the flexPotentialList where version is less than or equal to SMALLER_VERSION
        defaultFlexPotentialShouldNotBeFound("version.lessThanOrEqual=" + (flexPotentialEntity.getVersion() - 1));
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVersionIsLessThanSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where version is less than DEFAULT_VERSION
        defaultFlexPotentialShouldNotBeFound("version.lessThan=" + flexPotentialEntity.getVersion());

        // Get all the flexPotentialList where version is less than UPDATED_VERSION
        defaultFlexPotentialShouldBeFound("version.lessThan=" + UPDATED_VERSION);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByVersionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where version is greater than DEFAULT_VERSION
        defaultFlexPotentialShouldNotBeFound("version.greaterThan=" + flexPotentialEntity.getVersion());

        // Get all the flexPotentialList where version is greater than SMALLER_VERSION
        defaultFlexPotentialShouldBeFound("version.greaterThan=" + (flexPotentialEntity.getVersion() - 1));
    }


    @Test
    @Transactional
    public void getAllFlexPotentialsByProductIsEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);
        Long productId = productEntity.getId();

        // Get all the flexPotentialList where product equals to productId
        defaultFlexPotentialShouldBeFound("productId.equals=" + productId);

        // Get all the flexPotentialList where product equals to productId + 1
        defaultFlexPotentialShouldNotBeFound("productId.equals=" + (productId + 1));
    }


    @Test
    @Transactional
    public void getAllFlexPotentialsByUnitNameIsEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);
        Long unitId = unitEntity.getId();

        // Get all the flexPotentialList where unit equals to unitId
        defaultFlexPotentialShouldBeFound("unitName.equals=" + unitEntity.getName());

        // Get all the flexPotentialList where unit equals to unitId + 1
        defaultFlexPotentialShouldNotBeFound("unitName.equals=" + (unitEntity.getName() + "AAA"));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    protected void defaultFlexPotentialShouldBeFound(String filter) throws Exception {
        restFlexPotentialMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(flexPotentialEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].product.id").value(hasItem(productEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].fsp.id").value(hasItem(fspEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].units.[*].id").value(hasItem(unitEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].volume").value(hasItem(DEFAULT_VOLUME.intValue())))
            .andExpect(jsonPath("$.[*].volumeUnit").value(hasItem(DEFAULT_VOLUME_UNIT.toString())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(flexPotentialEntity.getLastModifiedDate().toString())))
            .andExpect(jsonPath("$.[*].createdByRole").value(hasItem(flexPotentialEntity.getCreatedByRole())))
            .andExpect(jsonPath("$.[*].lastModifiedByRole").value(hasItem(flexPotentialEntity.getLastModifiedByRole())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validTo").value(hasItem(DEFAULT_VALID_TO.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].productPreqNeeded").value(hasItem(DEFAULT_PRODUCT_PREQ_NEEDED.booleanValue())))
            .andExpect(jsonPath("$.[*].productPrequalification").value(hasItem(DEFAULT_PRODUCT_PREQUALIFICATION.booleanValue())))
            .andExpect(jsonPath("$.[*].staticGridPrequalification").value(hasItem(DEFAULT_STATIC_GRID_PREQ_NEEDED.booleanValue())))
            .andExpect(jsonPath("$.[*].staticGridPrequalification").value(hasItem(DEFAULT_STATIC_GRID_PREQUALIFICATION.booleanValue())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(flexPotentialEntity.getVersion().intValue())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    protected void defaultFlexPotentialShouldNotBeFound(String filter) throws Exception {
        restFlexPotentialMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

//    @Test
//    @Transactional
//    public void updateFlexPotential() throws Exception {
//        // Initialize the database
//        flexPotentialRepository.saveAndFlush(flexPotentialEntity);
//
//        int databaseSizeBeforeUpdate = flexPotentialRepository.findAll().size();
//
//        // Update the flexPotential
//        FlexPotentialEntity updatedFlexPotentialEntity = flexPotentialRepository.findById(flexPotentialEntity.getId()).get();
//        // Disconnect from session so that the updates on updatedFlexPotentialEntity are not directly saved in db
//        em.detach(updatedFlexPotentialEntity);
//        updatedFlexPotentialEntity.setVolume(UPDATED_VOLUME);
//        updatedFlexPotentialEntity.setVolumeUnit(UPDATED_VOLUME_UNIT);
//        updatedFlexPotentialEntity.setValidFrom(UPDATED_VALID_FROM);
//        updatedFlexPotentialEntity.setValidTo(UPDATED_VALID_TO);
//        updatedFlexPotentialEntity.setActive(UPDATED_ACTIVE);
//        updatedFlexPotentialEntity.setProductPrequalification(UPDATED_PRODUCT_PREQUALIFICATION);
//        updatedFlexPotentialEntity.setStaticGridPrequalification(UPDATED_STATIC_GRID_PREQUALIFICATION);
//        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(updatedFlexPotentialEntity);
//
//        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
//        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/update")
//            .file(multipartProduct))
//            .andExpect(status().isOk());
//
//        // Validate the FlexPotential in the database
//        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
//        assertThat(flexPotentialList).hasSize(databaseSizeBeforeUpdate);
//        FlexPotentialEntity testFlexPotential = flexPotentialList.get(flexPotentialList.size() - 1);
//        assertThat(testFlexPotential.getVolume()).isEqualTo(UPDATED_VOLUME);
//        assertThat(testFlexPotential.getVolumeUnit()).isEqualTo(UPDATED_VOLUME_UNIT);
//        assertThat(testFlexPotential.getValidFrom()).isEqualTo(UPDATED_VALID_FROM);
//        assertThat(testFlexPotential.getValidTo()).isEqualTo(UPDATED_VALID_TO);
//        assertThat(testFlexPotential.isActive()).isEqualTo(UPDATED_ACTIVE);
//        assertThat(testFlexPotential.isProductPrequalification()).isEqualTo(UPDATED_PRODUCT_PREQUALIFICATION);
//        assertThat(testFlexPotential.isStaticGridPrequalification()).isEqualTo(UPDATED_STATIC_GRID_PREQUALIFICATION);
//        assertThat(testFlexPotential.getVersion()).isEqualTo(flexPotentialDTO.getVersion() + 1);
//    }

    @Test
    @Transactional
    public void deleteFlexPotential() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        int databaseSizeBeforeDelete = flexPotentialRepository.findAll().size();

        // Delete the flexPotential
        restFlexPotentialMockMvc.perform(delete(requestUri + "/{id}", flexPotentialEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void createFlexPotential_shouldNotCreatedBecauseValidFromIsBeforeCreatedDate() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);
        flexPotentialDTO.setValidFrom(Instant.now().minus(1, DAYS).truncatedTo(SECONDS));

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(FROM_DATE_BEFORE_CREATED_DATE))
            .andExpect(status().isBadRequest());

        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createFlexPotential_shouldNotCreatedBecauseValidToIsBeforeValidFrom() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);
        flexPotentialDTO.setValidFrom(Instant.now().plus(2, DAYS).truncatedTo(SECONDS));
        flexPotentialDTO.setValidTo(Instant.now().plus(1, DAYS).truncatedTo(SECONDS));

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(FROM_DATE_AFTER_TO_DATE))
            .andExpect(status().isBadRequest());

        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createFlexPotential_shouldNotCreatedBecauseExceedsExpiryDateOfUnit() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);
        flexPotentialDTO.setValidTo(Instant.now().plus(30, DAYS).truncatedTo(SECONDS));

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(FP_EXCEEDS_THE_EXPIRY_DATE_OF_THE_UNIT))
            .andExpect(status().isBadRequest());

        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createFlexPotential_shouldNotActiveBecausePresentDateIsNotBetweenValidFromAndValidTo() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);
        flexPotentialDTO.setValidFrom(Instant.now().plus(1, DAYS).truncatedTo(SECONDS));
        flexPotentialDTO.setValidTo(Instant.now().plus(2, DAYS).truncatedTo(SECONDS));
        flexPotentialDTO.setActive(true);

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(FP_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES))
            .andExpect(status().isBadRequest());

        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createFlexPotential_shouldNotActiveBecauseFullActivationTimeIsHigherThanProductMaxFullActivationTime() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);
        flexPotentialDTO.setFullActivationTime(productEntity.getMaxFullActivationTime() + 1);

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(FP_FULL_ACTIVATION_TIME_CANNOT_BE_HIGHER_THAN_PRODUCT_MAX_FULL_ACTIVATION_TIME))
            .andExpect(status().isBadRequest());

        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createFlexPotential_shouldNotActiveBecauseMinDeliveryDurationCannotBeLessThanProductMinReqDuration() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);
        flexPotentialDTO.setMinDeliveryDuration(productEntity.getMinRequiredDeliveryDuration() - 1);

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(FP_MIN_DELIVERY_DURATION_CANNOT_BE_LESS_THAN_PRODUCT_MIN_REQUIRED_DELIVERY_DURATION))
            .andExpect(status().isBadRequest());

        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createFlexPotential_shouldNotActiveBecauseVolumeIsLessThanProductMinBidSize() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);
        flexPotentialDTO.setVolume(productEntity.getMinBidSize().add(BigDecimal.valueOf(-10L)));

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(FP_VOLUME_IS_NOT_BETWEEN_MIN_MAX_PRODUCT_BID_SIZE))
            .andExpect(status().isBadRequest());

        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createFlexPotential_shouldNotActiveBecauseVolumeIsGreaterThanProductMaxBidSize() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);
        flexPotentialDTO.setVolume(productEntity.getMaxBidSize().add(BigDecimal.valueOf(10L)));

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(FP_VOLUME_IS_NOT_BETWEEN_MIN_MAX_PRODUCT_BID_SIZE))
            .andExpect(status().isBadRequest());

        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createFlexPotential_shouldNotActiveBecauseNotDersSelected() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);
        flexPotentialDTO.setUnitIds(Collections.emptyList());

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value("idnull"))
            .andExpect(status().isBadRequest());

        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createFlexPotential_shouldNotActiveBecauseProductIsNonActive() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);
        // Create non active Product
        ProductEntity noActiveProductEntity = createNoActiveProductEntity(em);
        ProductMinDTO nonActiveProduct = new ProductMinDTO();
        nonActiveProduct.setId(noActiveProductEntity.getId());
        flexPotentialDTO.setProduct(nonActiveProduct);
        flexPotentialDTO.setActive(true);

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(FP_PRODUCT_IS_NOT_ACTIVE))
            .andExpect(status().isBadRequest());

        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createFlexPotential_shouldNotActiveBecauseUnitIsNonActive() throws Exception {
        int databaseSizeBeforeCreate = flexPotentialRepository.findAll().size();
        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);
        // Create non active Unit
        UnitEntity noActiveUnitEntity = createNoActiveUnitEntity(em);
        flexPotentialDTO.setUnitIds(Collections.singletonList(noActiveUnitEntity.getId()));
        flexPotentialDTO.setActive(true);

        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri)
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(FP_UNIT_IS_NOT_ACTIVE))
            .andExpect(status().isBadRequest());

        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeCreate);
    }
}
