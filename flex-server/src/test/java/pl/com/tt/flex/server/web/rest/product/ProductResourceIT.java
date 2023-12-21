package pl.com.tt.flex.server.web.rest.product;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.product.type.Direction;
import pl.com.tt.flex.model.service.dto.product.type.ProductBidSizeUnit;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.enumeration.CreationSource;
import pl.com.tt.flex.server.repository.product.ProductRepository;
import pl.com.tt.flex.server.service.product.dto.ProductDTO;
import pl.com.tt.flex.server.web.rest.TestUtil;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the {@link ProductResource} REST controller.
 */

public abstract class ProductResourceIT {

    protected static final String DEFAULT_FULL_NAME = "AAAAAAAAAA";
    protected static final String UPDATED_FULL_NAME = "BBBBBBBBBB";

    protected static final String DEFAULT_SHORT_NAME = "AAAAAAAAAA";
    protected static final String UPDATED_SHORT_NAME = "BBBBBBBBBB";

    protected static final Boolean DEFAULT_LOCATIONAL = false;
    protected static final Boolean UPDATED_LOCATIONAL = true;

    protected static final BigDecimal DEFAULT_MIN_BID_SIZE = new BigDecimal(50);
    protected static final BigDecimal UPDATED_MIN_BID_SIZE = new BigDecimal(60);
    protected static final BigDecimal SMALLER_MIN_BID_SIZE = new BigDecimal(1 - 1);

    protected static final BigDecimal DEFAULT_MAX_BID_SIZE = new BigDecimal(100);
    protected static final BigDecimal UPDATED_MAX_BID_SIZE = new BigDecimal(90);
    protected static final BigDecimal SMALLER_MAX_BID_SIZE = new BigDecimal(1 - 1);

    protected static final Integer DEFAULT_MAX_FULL_ACTIVATION_TIME = 60;
    protected static final Integer UPDATED_MAX_FULL_ACTIVATION_TIME = 120;

    protected static final Integer DEFAULT_MIN_REQUIRED_DELIVERY_DURATION = 60;
    protected static final Integer UPDATED_MIN_REQUIRED_DELIVERY_DURATION = 120;

    protected static final Boolean DEFAULT_ACTIVE = false;
    protected static final Boolean UPDATED_ACTIVE = true;

    protected static final Boolean DEFAULT_BALANCING = true;
    protected static final Boolean UPDATE_BALANCING = true;

    protected static final Instant DEFAULT_VALID_FROM = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    protected static final Instant UPDATED_VALID_FROM = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

    protected static final Instant DEFAULT_VALID_TO = Instant.now().plus(10, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    protected static final Instant UPDATED_VALID_TO = Instant.now().plus(11, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

    protected static final Long DEFAULT_VERSION = 0L;
    protected static final Long UPDATED_VERSION = 1L;

    protected static final Boolean DEFAULT_AGGREAGTION_ALLOWED = true;

    protected static final ProductBidSizeUnit DEFAULT_PRODUCT_BID_SIZE_UNIT = ProductBidSizeUnit.kW;
    protected static final ProductBidSizeUnit UPDATED_PRODUCT_BID_SIZE_UNIT = ProductBidSizeUnit.KWH;

    protected static final String DEFAULT_CREATED_BY = "admin";
    protected static final String DEFAULT_LAST_MODIFIED_BY = "admin";

    protected static final Direction DEFAULT_DIRECTION = Direction.UNDEFINED;

    protected ProductEntity productEntity;
    protected ProductDTO productDTO;

    private final ProductRepository productRepository;
    private final EntityManager em;
    private final MockMvc restProductMockMvc;
    private final String requestUri;

    private static UserEntity tsoUser;

    public ProductResourceIT(ProductRepository productRepository, EntityManager em,
                             MockMvc restProductMockMvc, String requestUri) {
        this.productRepository = productRepository;
        this.em = em;
        this.restProductMockMvc = restProductMockMvc;
        this.requestUri = requestUri;
    }

    public static ProductEntity createEntity(EntityManager em) {
        tsoUser = createUserTsoAndPso(em);
        ProductEntity entity = ProductEntity.builder()
            .fullName(DEFAULT_FULL_NAME)
            .shortName(DEFAULT_SHORT_NAME)
            .locational(DEFAULT_LOCATIONAL)
            .minBidSize(DEFAULT_MIN_BID_SIZE)
            .maxBidSize(DEFAULT_MAX_BID_SIZE)
            .maxFullActivationTime(DEFAULT_MAX_FULL_ACTIVATION_TIME)
            .minRequiredDeliveryDuration(DEFAULT_MIN_REQUIRED_DELIVERY_DURATION)
            .active(DEFAULT_ACTIVE)
            .validFrom(DEFAULT_VALID_FROM)
            .validTo(DEFAULT_VALID_TO)
            .bidSizeUnit(DEFAULT_PRODUCT_BID_SIZE_UNIT)
            .psoUser(tsoUser)
            .ssoUsers(Set.of(tsoUser))
            .direction(DEFAULT_DIRECTION)
            .files(new HashSet<>())
            .build();

        entity.setCreatedBy(DEFAULT_CREATED_BY);
        entity.setLastModifiedBy(DEFAULT_LAST_MODIFIED_BY);
        return entity;
    }

    private static UserEntity createUserTsoAndPso(EntityManager em) {
        String tsoLogin = "tso-account";
        List<UserEntity> allUsers = TestUtil.findAll(em, UserEntity.class);
        if (allUsers.isEmpty() || allUsers.stream().noneMatch(u -> u.getLogin().equals(tsoLogin))) {
            tsoUser = new UserEntity();
            tsoUser.setLogin(tsoLogin);
            tsoUser.setEmail("tso-account@example.com");
            tsoUser.setPassword(RandomStringUtils.random(60));
            tsoUser.setActivated(true);
            tsoUser.setCreationSource(CreationSource.SYSTEM);
            tsoUser.getRoles().add(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR);
            em.persist(tsoUser);
            em.flush();
        } else {
            tsoUser = allUsers.stream().filter(u -> u.getLogin().equals(tsoLogin)).findFirst().get();
        }
        return tsoUser;
    }

    public static ProductDTO createDto(EntityManager em) {
        tsoUser = createUserTsoAndPso(em);
        return ProductDTO.builder()
            .fullName(DEFAULT_FULL_NAME)
            .shortName(DEFAULT_SHORT_NAME)
            .locational(DEFAULT_LOCATIONAL)
            .minBidSize(DEFAULT_MIN_BID_SIZE)
            .maxBidSize(DEFAULT_MAX_BID_SIZE)
            .maxFullActivationTime(DEFAULT_MAX_FULL_ACTIVATION_TIME)
            .minRequiredDeliveryDuration(DEFAULT_MIN_REQUIRED_DELIVERY_DURATION)
            .active(DEFAULT_ACTIVE)
            .validFrom(DEFAULT_VALID_FROM)
            .validTo(DEFAULT_VALID_TO)
            .balancing(DEFAULT_BALANCING)
            .version(null)
            .bidSizeUnit(DEFAULT_PRODUCT_BID_SIZE_UNIT)
            .direction(DEFAULT_DIRECTION)
            .psoUserId(tsoUser.getId())
            .ssoUserIds(Collections.singletonList(tsoUser.getId()))
            .build();
    }

    @BeforeEach
    public void initTest() {
        productEntity = createEntity(em);
        productDTO = createDto(em);
    }

    @Test
    @Transactional
    public void getAllProducts() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList
        restProductMockMvc.perform(get(requestUri + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(productEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].fullName").value(hasItem(DEFAULT_FULL_NAME)))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)))
            .andExpect(jsonPath("$.[*].locational").value(hasItem(DEFAULT_LOCATIONAL)))
            .andExpect(jsonPath("$.[*].minBidSize").value(hasItem(DEFAULT_MIN_BID_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].maxBidSize").value(hasItem(DEFAULT_MAX_BID_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].maxFullActivationTime").value(hasItem(DEFAULT_MAX_FULL_ACTIVATION_TIME)))
            .andExpect(jsonPath("$.[*].minRequiredDeliveryDuration").value(hasItem(DEFAULT_MIN_REQUIRED_DELIVERY_DURATION)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validTo").value(hasItem(DEFAULT_VALID_TO.toString())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(productEntity.getVersion().intValue())));
    }

    @Test
    @Transactional
    public void getProduct() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get the product
        restProductMockMvc.perform(get(requestUri + "/{id}", productEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(productEntity.getId().intValue()))
            .andExpect(jsonPath("$.fullName").value(DEFAULT_FULL_NAME))
            .andExpect(jsonPath("$.shortName").value(DEFAULT_SHORT_NAME))
            .andExpect(jsonPath("$.locational").value(DEFAULT_LOCATIONAL))
            .andExpect(jsonPath("$.minBidSize").value(DEFAULT_MIN_BID_SIZE.intValue()))
            .andExpect(jsonPath("$.maxBidSize").value(DEFAULT_MAX_BID_SIZE.intValue()))
            .andExpect(jsonPath("$.maxFullActivationTime").value(DEFAULT_MAX_FULL_ACTIVATION_TIME.toString()))
            .andExpect(jsonPath("$.minRequiredDeliveryDuration").value(DEFAULT_MIN_REQUIRED_DELIVERY_DURATION.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE))
            .andExpect(jsonPath("$.validFrom").value(DEFAULT_VALID_FROM.toString()))
            .andExpect(jsonPath("$.validTo").value(DEFAULT_VALID_TO.toString()))
            .andExpect(jsonPath("$.version").value(productEntity.getVersion().intValue()));
    }


    @Test
    @Transactional
    public void getProductsByIdFiltering() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        Long id = productEntity.getId();

        defaultProductShouldBeFound("id.equals=" + id);
        defaultProductShouldNotBeFound("id.notEquals=" + id);

        defaultProductShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultProductShouldNotBeFound("id.greaterThan=" + id);

        defaultProductShouldBeFound("id.lessThanOrEqual=" + id);
        defaultProductShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllProductsByFullNameIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where fullName equals to DEFAULT_FULL_NAME
        defaultProductShouldBeFound("fullName.equals=" + DEFAULT_FULL_NAME);

        // Get all the productList where fullName equals to UPDATED_FULL_NAME
        defaultProductShouldNotBeFound("fullName.equals=" + UPDATED_FULL_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByFullNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where fullName not equals to DEFAULT_FULL_NAME
        defaultProductShouldNotBeFound("fullName.notEquals=" + DEFAULT_FULL_NAME);

        // Get all the productList where fullName not equals to UPDATED_FULL_NAME
        defaultProductShouldBeFound("fullName.notEquals=" + UPDATED_FULL_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByFullNameIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where fullName in DEFAULT_FULL_NAME or UPDATED_FULL_NAME
        defaultProductShouldBeFound("fullName.in=" + DEFAULT_FULL_NAME + "," + UPDATED_FULL_NAME);

        // Get all the productList where fullName equals to UPDATED_FULL_NAME
        defaultProductShouldNotBeFound("fullName.in=" + UPDATED_FULL_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByFullNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where fullName is not null
        defaultProductShouldBeFound("fullName.specified=true");

        // Get all the productList where fullName is null
        defaultProductShouldNotBeFound("fullName.specified=false");
    }

    @Test
    @Transactional
    public void getAllProductsByFullNameContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where fullName contains DEFAULT_FULL_NAME
        defaultProductShouldBeFound("fullName.contains=" + DEFAULT_FULL_NAME);

        // Get all the productList where fullName contains UPDATED_FULL_NAME
        defaultProductShouldNotBeFound("fullName.contains=" + UPDATED_FULL_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByFullNameNotContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where fullName does not contain DEFAULT_FULL_NAME
        defaultProductShouldNotBeFound("fullName.doesNotContain=" + DEFAULT_FULL_NAME);

        // Get all the productList where fullName does not contain UPDATED_FULL_NAME
        defaultProductShouldBeFound("fullName.doesNotContain=" + UPDATED_FULL_NAME);
    }


    @Test
    @Transactional
    public void getAllProductsByShortNameIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where shortName equals to DEFAULT_SHORT_NAME
        defaultProductShouldBeFound("shortName.equals=" + DEFAULT_SHORT_NAME);

        // Get all the productList where shortName equals to UPDATED_SHORT_NAME
        defaultProductShouldNotBeFound("shortName.equals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByShortNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where shortName not equals to DEFAULT_SHORT_NAME
        defaultProductShouldNotBeFound("shortName.notEquals=" + DEFAULT_SHORT_NAME);

        // Get all the productList where shortName not equals to UPDATED_SHORT_NAME
        defaultProductShouldBeFound("shortName.notEquals=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByShortNameIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where shortName in DEFAULT_SHORT_NAME or UPDATED_SHORT_NAME
        defaultProductShouldBeFound("shortName.in=" + DEFAULT_SHORT_NAME + "," + UPDATED_SHORT_NAME);

        // Get all the productList where shortName equals to UPDATED_SHORT_NAME
        defaultProductShouldNotBeFound("shortName.in=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByShortNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where shortName is not null
        defaultProductShouldBeFound("shortName.specified=true");

        // Get all the productList where shortName is null
        defaultProductShouldNotBeFound("shortName.specified=false");
    }

    @Test
    @Transactional
    public void getAllProductsByShortNameContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where shortName contains DEFAULT_SHORT_NAME
        defaultProductShouldBeFound("shortName.contains=" + DEFAULT_SHORT_NAME);

        // Get all the productList where shortName contains UPDATED_SHORT_NAME
        defaultProductShouldNotBeFound("shortName.contains=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByShortNameNotContainsSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where shortName does not contain DEFAULT_SHORT_NAME
        defaultProductShouldNotBeFound("shortName.doesNotContain=" + DEFAULT_SHORT_NAME);

        // Get all the productList where shortName does not contain UPDATED_SHORT_NAME
        defaultProductShouldBeFound("shortName.doesNotContain=" + UPDATED_SHORT_NAME);
    }

    @Test
    @Transactional
    public void getAllProductsByIsLocationalIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where locational equals to DEFAULT_LOCATIONAL
        defaultProductShouldBeFound("locational.equals=" + DEFAULT_LOCATIONAL);

        // Get all the productList where locational equals to UPDATED_LOCATIONAL
        defaultProductShouldNotBeFound("locational.equals=" + UPDATED_LOCATIONAL);
    }

    @Test
    @Transactional
    public void getAllProductsByIsLocationalIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where locational not equals to DEFAULT_LOCATIONAL
        defaultProductShouldNotBeFound("locational.notEquals=" + DEFAULT_LOCATIONAL);

        // Get all the productList where locational not equals to UPDATED_LOCATIONAL
        defaultProductShouldBeFound("locational.notEquals=" + UPDATED_LOCATIONAL);
    }

    @Test
    @Transactional
    public void getAllProductsByIsLocationalIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where locational in DEFAULT_LOCATIONAL or UPDATED_LOCATIONAL
        defaultProductShouldBeFound("locational.in=" + DEFAULT_LOCATIONAL + "," + UPDATED_LOCATIONAL);

        // Get all the productList where locational equals to UPDATED_LOCATIONAL
        defaultProductShouldNotBeFound("locational.in=" + UPDATED_LOCATIONAL);
    }

    @Test
    @Transactional
    public void getAllProductsByIsLocationalIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where locational is not null
        defaultProductShouldBeFound("locational.specified=true");

        // Get all the productList where locational is null
        defaultProductShouldNotBeFound("locational.specified=false");
    }

    @Test
    @Transactional
    public void getAllProductsByMinBidSizeIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minBidSize equals to DEFAULT_MIN_BID_SIZE
        defaultProductShouldBeFound("minBidSize.equals=" + DEFAULT_MIN_BID_SIZE);

        // Get all the productList where minBidSize equals to UPDATED_MIN_BID_SIZE
        defaultProductShouldNotBeFound("minBidSize.equals=" + UPDATED_MIN_BID_SIZE);
    }

    @Test
    @Transactional
    public void getAllProductsByMinBidSizeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minBidSize not equals to DEFAULT_MIN_BID_SIZE
        defaultProductShouldNotBeFound("minBidSize.notEquals=" + DEFAULT_MIN_BID_SIZE);

        // Get all the productList where minBidSize not equals to UPDATED_MIN_BID_SIZE
        defaultProductShouldBeFound("minBidSize.notEquals=" + UPDATED_MIN_BID_SIZE);
    }

    @Test
    @Transactional
    public void getAllProductsByMinBidSizeIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minBidSize in DEFAULT_MIN_BID_SIZE or UPDATED_MIN_BID_SIZE
        defaultProductShouldBeFound("minBidSize.in=" + DEFAULT_MIN_BID_SIZE + "," + UPDATED_MIN_BID_SIZE);

        // Get all the productList where minBidSize equals to UPDATED_MIN_BID_SIZE
        defaultProductShouldNotBeFound("minBidSize.in=" + UPDATED_MIN_BID_SIZE);
    }

    @Test
    @Transactional
    public void getAllProductsByMinBidSizeIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minBidSize is not null
        defaultProductShouldBeFound("minBidSize.specified=true");

        // Get all the productList where minBidSize is null
        defaultProductShouldNotBeFound("minBidSize.specified=false");
    }

    @Test
    @Transactional
    public void getAllProductsByMinBidSizeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minBidSize is greater than or equal to DEFAULT_MIN_BID_SIZE
        defaultProductShouldBeFound("minBidSize.greaterThanOrEqual=" + DEFAULT_MIN_BID_SIZE);

        // Get all the productList where minBidSize is greater than or equal to UPDATED_MIN_BID_SIZE
        defaultProductShouldNotBeFound("minBidSize.greaterThanOrEqual=" + UPDATED_MIN_BID_SIZE);
    }

    @Test
    @Transactional
    public void getAllProductsByMinBidSizeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minBidSize is less than or equal to DEFAULT_MIN_BID_SIZE
        defaultProductShouldBeFound("minBidSize.lessThanOrEqual=" + DEFAULT_MIN_BID_SIZE);

        // Get all the productList where minBidSize is less than or equal to SMALLER_MIN_BID_SIZE
        defaultProductShouldNotBeFound("minBidSize.lessThanOrEqual=" + SMALLER_MIN_BID_SIZE);
    }

    @Test
    @Transactional
    public void getAllProductsByMinBidSizeIsLessThanSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minBidSize is less than DEFAULT_MIN_BID_SIZE
        defaultProductShouldNotBeFound("minBidSize.lessThan=" + DEFAULT_MIN_BID_SIZE);

        // Get all the productList where minBidSize is less than UPDATED_MIN_BID_SIZE
        defaultProductShouldBeFound("minBidSize.lessThan=" + UPDATED_MIN_BID_SIZE);
    }

    @Test
    @Transactional
    public void getAllProductsByMinBidSizeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minBidSize is greater than DEFAULT_MIN_BID_SIZE
        defaultProductShouldNotBeFound("minBidSize.greaterThan=" + DEFAULT_MIN_BID_SIZE);

        // Get all the productList where minBidSize is greater than SMALLER_MIN_BID_SIZE
        defaultProductShouldBeFound("minBidSize.greaterThan=" + SMALLER_MIN_BID_SIZE);
    }


    @Test
    @Transactional
    public void getAllProductsByMaxBidSizeIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxBidSize equals to DEFAULT_MAX_BID_SIZE
        defaultProductShouldBeFound("maxBidSize.equals=" + DEFAULT_MAX_BID_SIZE);

        // Get all the productList where maxBidSize equals to UPDATED_MAX_BID_SIZE
        defaultProductShouldNotBeFound("maxBidSize.equals=" + UPDATED_MAX_BID_SIZE);
    }

    @Test
    @Transactional
    public void getAllProductsByMaxBidSizeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxBidSize not equals to DEFAULT_MAX_BID_SIZE
        defaultProductShouldNotBeFound("maxBidSize.notEquals=" + DEFAULT_MAX_BID_SIZE);

        // Get all the productList where maxBidSize not equals to UPDATED_MAX_BID_SIZE
        defaultProductShouldBeFound("maxBidSize.notEquals=" + UPDATED_MAX_BID_SIZE);
    }

    @Test
    @Transactional
    public void getAllProductsByMaxBidSizeIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxBidSize in DEFAULT_MAX_BID_SIZE or UPDATED_MAX_BID_SIZE
        defaultProductShouldBeFound("maxBidSize.in=" + DEFAULT_MAX_BID_SIZE + "," + UPDATED_MAX_BID_SIZE);

        // Get all the productList where maxBidSize equals to UPDATED_MAX_BID_SIZE
        defaultProductShouldNotBeFound("maxBidSize.in=" + UPDATED_MAX_BID_SIZE);
    }

    @Test
    @Transactional
    public void getAllProductsByMaxBidSizeIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxBidSize is not null
        defaultProductShouldBeFound("maxBidSize.specified=true");

        // Get all the productList where maxBidSize is null
        defaultProductShouldNotBeFound("maxBidSize.specified=false");
    }

    @Test
    @Transactional
    public void getAllProductsByMaxBidSizeIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxBidSize is greater than or equal to DEFAULT_MAX_BID_SIZE
        defaultProductShouldBeFound("maxBidSize.greaterThanOrEqual=" + DEFAULT_MAX_BID_SIZE);

        // Get all the productList where maxBidSize is greater than or equal to UPDATED_MAX_BID_SIZE
        defaultProductShouldNotBeFound("maxBidSize.greaterThanOrEqual=" + (DEFAULT_MAX_BID_SIZE.add(BigDecimal.valueOf(10L))));
    }

    @Test
    @Transactional
    public void getAllProductsByMaxBidSizeIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxBidSize is less than or equal to DEFAULT_MAX_BID_SIZE
        defaultProductShouldBeFound("maxBidSize.lessThanOrEqual=" + DEFAULT_MAX_BID_SIZE);

        // Get all the productList where maxBidSize is less than or equal to SMALLER_MAX_BID_SIZE
        defaultProductShouldNotBeFound("maxBidSize.lessThanOrEqual=" + SMALLER_MAX_BID_SIZE);
    }

    @Test
    @Transactional
    public void getAllProductsByMaxBidSizeIsLessThanSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxBidSize is less than DEFAULT_MAX_BID_SIZE
        defaultProductShouldNotBeFound("maxBidSize.lessThan=" + DEFAULT_MAX_BID_SIZE);

        // Get all the productList where maxBidSize is less than UPDATED_MAX_BID_SIZE
        defaultProductShouldBeFound("maxBidSize.lessThan=" + (DEFAULT_MAX_BID_SIZE.add(BigDecimal.valueOf(10L))));
    }

    @Test
    @Transactional
    public void getAllProductsByMaxBidSizeIsGreaterThanSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxBidSize is greater than DEFAULT_MAX_BID_SIZE
        defaultProductShouldNotBeFound("maxBidSize.greaterThan=" + DEFAULT_MAX_BID_SIZE);

        // Get all the productList where maxBidSize is greater than SMALLER_MAX_BID_SIZE
        defaultProductShouldBeFound("maxBidSize.greaterThan=" + SMALLER_MAX_BID_SIZE);
    }

    @Test
    @Transactional
    public void getAllProductsByMaxFullActivationTimeIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxFullActivationTime equals to DEFAULT_MIN_DURATION
        defaultProductShouldBeFound("maxFullActivationTime.equals=" + DEFAULT_MAX_FULL_ACTIVATION_TIME);

        // Get all the productList where maxFullActivationTime equals to UPDATED_MIN_DURATION
        defaultProductShouldNotBeFound("maxFullActivationTime.equals=" + UPDATED_MAX_FULL_ACTIVATION_TIME);
    }

    @Test
    @Transactional
    public void getAllProductsByMaxFullActivationTimeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxFullActivationTime not equals to DEFAULT_MIN_DURATION
        defaultProductShouldNotBeFound("maxFullActivationTime.notEquals=" + DEFAULT_MAX_FULL_ACTIVATION_TIME);

        // Get all the productList where maxFullActivationTime not equals to UPDATED_MIN_DURATION
        defaultProductShouldBeFound("maxFullActivationTime.notEquals=" + UPDATED_MAX_FULL_ACTIVATION_TIME);
    }

    @Test
    @Transactional
    public void getAllProductsByMaxFullActivationTimeIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxFullActivationTime in DEFAULT_MIN_DURATION or UPDATED_MIN_DURATION
        defaultProductShouldBeFound("maxFullActivationTime.in=" + DEFAULT_MAX_FULL_ACTIVATION_TIME + "," + UPDATED_MAX_FULL_ACTIVATION_TIME);

        // Get all the productList where maxFullActivationTime equals to UPDATED_MIN_DURATION
        defaultProductShouldNotBeFound("maxFullActivationTime.in=" + UPDATED_MAX_FULL_ACTIVATION_TIME);
    }

    @Test
    @Transactional
    public void getAllProductsByMaxFullActivationTimeIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where maxFullActivationTime is not null
        defaultProductShouldBeFound("maxFullActivationTime.specified=true");

        // Get all the productList where maxFullActivationTime is null
        defaultProductShouldNotBeFound("maxFullActivationTime.specified=false");
    }

    @Test
    @Transactional
    public void getAllProductsByMinRequiredDeliveryDurationIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minRequiredDeliveryDuration equals to DEFAULT_MAX_DURATION
        defaultProductShouldBeFound("minRequiredDeliveryDuration.equals=" + DEFAULT_MIN_REQUIRED_DELIVERY_DURATION);

        // Get all the productList where minRequiredDeliveryDuration equals to UPDATED_MAX_DURATION
        defaultProductShouldNotBeFound("minRequiredDeliveryDuration.equals=" + UPDATED_MIN_REQUIRED_DELIVERY_DURATION);
    }

    @Test
    @Transactional
    public void getAllProductsByMinRequiredDeliveryDurationIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minRequiredDeliveryDuration not equals to DEFAULT_MAX_DURATION
        defaultProductShouldNotBeFound("minRequiredDeliveryDuration.notEquals=" + DEFAULT_MIN_REQUIRED_DELIVERY_DURATION);

        // Get all the productList where minRequiredDeliveryDuration not equals to UPDATED_MAX_DURATION
        defaultProductShouldBeFound("minRequiredDeliveryDuration.notEquals=" + UPDATED_MIN_REQUIRED_DELIVERY_DURATION);
    }

    @Test
    @Transactional
    public void getAllProductsByMinRequiredDeliveryDurationIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minRequiredDeliveryDuration in DEFAULT_MAX_DURATION or UPDATED_MAX_DURATION
        defaultProductShouldBeFound("minRequiredDeliveryDuration.in=" + DEFAULT_MIN_REQUIRED_DELIVERY_DURATION + "," + UPDATED_MIN_REQUIRED_DELIVERY_DURATION);

        // Get all the productList where minRequiredDeliveryDuration equals to UPDATED_MAX_DURATION
        defaultProductShouldNotBeFound("minRequiredDeliveryDuration.in=" + UPDATED_MIN_REQUIRED_DELIVERY_DURATION);
    }

    @Test
    @Transactional
    public void getAllProductsByMinRequiredDeliveryDurationIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where minRequiredDeliveryDuration is not null
        defaultProductShouldBeFound("minRequiredDeliveryDuration.specified=true");

        // Get all the productList where minRequiredDeliveryDuration is null
        defaultProductShouldNotBeFound("minRequiredDeliveryDuration.specified=false");
    }

    @Test
    @Transactional
    public void getAllProductsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where active equals to DEFAULT_ACTIVE
        defaultProductShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the productList where active equals to UPDATED_ACTIVE
        defaultProductShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllProductsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where active not equals to DEFAULT_ACTIVE
        defaultProductShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the productList where active not equals to UPDATED_ACTIVE
        defaultProductShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllProductsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultProductShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the productList where active equals to UPDATED_ACTIVE
        defaultProductShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllProductsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where active is not null
        defaultProductShouldBeFound("active.specified=true");

        // Get all the productList where active is null
        defaultProductShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllProductsByValidFromIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where validFrom equals to DEFAULT_VALID_FROM
        defaultProductShouldBeFound("validFrom.equals=" + DEFAULT_VALID_FROM);

        // Get all the productList where validFrom equals to UPDATED_VALID_FROM
        defaultProductShouldNotBeFound("validFrom.equals=" + Instant.ofEpochMilli(0));
    }

    @Test
    @Transactional
    public void getAllProductsByValidFromIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where validFrom not equals to DEFAULT_VALID_FROM
        defaultProductShouldNotBeFound("validFrom.notEquals=" + DEFAULT_VALID_FROM);

        // Get all the productList where validFrom not equals to UPDATED_VALID_FROM
        defaultProductShouldBeFound("validFrom.notEquals=" + Instant.ofEpochMilli(0));
    }

    @Test
    @Transactional
    public void getAllProductsByValidFromIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where validFrom in DEFAULT_VALID_FROM or UPDATED_VALID_FROM
        defaultProductShouldBeFound("validFrom.in=" + DEFAULT_VALID_FROM + "," + UPDATED_VALID_FROM);

        // Get all the productList where validFrom equals to UPDATED_VALID_FROM
        defaultProductShouldNotBeFound("validFrom.in=" + Instant.ofEpochMilli(0));
    }

    @Test
    @Transactional
    public void getAllProductsByValidFromIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where validFrom is not null
        defaultProductShouldBeFound("validFrom.specified=true");

        // Get all the productList where validFrom is null
        defaultProductShouldNotBeFound("validFrom.specified=false");
    }

    @Test
    @Transactional
    public void getAllProductsByValidToIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where validTo equals to DEFAULT_VALID_TO
        defaultProductShouldBeFound("validTo.equals=" + DEFAULT_VALID_TO);

        // Get all the productList where validTo equals to UPDATED_VALID_TO
        defaultProductShouldNotBeFound("validTo.equals=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllProductsByValidToIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where validTo not equals to DEFAULT_VALID_TO
        defaultProductShouldNotBeFound("validTo.notEquals=" + DEFAULT_VALID_TO);

        // Get all the productList where validTo not equals to UPDATED_VALID_TO
        defaultProductShouldBeFound("validTo.notEquals=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllProductsByValidToIsInShouldWork() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where validTo in DEFAULT_VALID_TO or UPDATED_VALID_TO
        defaultProductShouldBeFound("validTo.in=" + DEFAULT_VALID_TO + "," + UPDATED_VALID_TO);

        // Get all the productList where validTo equals to UPDATED_VALID_TO
        defaultProductShouldNotBeFound("validTo.in=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllProductsByValidToIsNullOrNotNull() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where validTo is not null
        defaultProductShouldBeFound("validTo.specified=true");

        // Get all the productList where validTo is null
        defaultProductShouldNotBeFound("validTo.specified=false");
    }

    @Test
    @Transactional
    public void getAllProductsByVersionIsEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where version equals to DEFAULT_VERSION
        defaultProductShouldBeFound("version.equals=" + productEntity.getVersion().intValue());

        // Get all the productList where version equals to UPDATED_VERSION
        defaultProductShouldNotBeFound("version.equals=" + productEntity.getVersion().intValue() + 1);
    }

    @Test
    @Transactional
    public void getAllProductsByVersionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        productRepository.saveAndFlush(productEntity);

        // Get all the productList where version not equals to DEFAULT_VERSION
        defaultProductShouldNotBeFound("version.notEquals=" + productEntity.getVersion().intValue());

        // Get all the productList where version not equals to UPDATED_VERSION
        defaultProductShouldBeFound("version.notEquals=" + productEntity.getVersion().intValue() + 1);
    }

    @Test
    @Transactional
    public void checkProductEntityVersionIncrementingSequentially() {
        ProductEntity save1 = productRepository.saveAndFlush(productEntity);
        assertThat(save1.getVersion()).isEqualTo(DEFAULT_VERSION);

        em.detach(save1);
        ProductEntity save2 = productRepository.saveAndFlush(save1);
        assertThat(save2.getVersion()).isEqualTo(DEFAULT_VERSION + 1);

        em.detach(save2);
        ProductEntity save3 = productRepository.saveAndFlush(save2);
        assertThat(save3.getVersion()).isEqualTo(DEFAULT_VERSION + 2);
    }

    @Test
    @Transactional
    public void checkProductVersionIncrementingAsynchronously() throws InterruptedException {
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultProductShouldBeFound(String filter) throws Exception {
        restProductMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(productEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].fullName").value(hasItem(DEFAULT_FULL_NAME)))
            .andExpect(jsonPath("$.[*].shortName").value(hasItem(DEFAULT_SHORT_NAME)))
            .andExpect(jsonPath("$.[*].locational").value(hasItem(DEFAULT_LOCATIONAL)))
            .andExpect(jsonPath("$.[*].minBidSize").value(hasItem(DEFAULT_MIN_BID_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].maxBidSize").value(hasItem(DEFAULT_MAX_BID_SIZE.intValue())))
            .andExpect(jsonPath("$.[*].maxFullActivationTime").value(hasItem(DEFAULT_MAX_FULL_ACTIVATION_TIME)))
            .andExpect(jsonPath("$.[*].minRequiredDeliveryDuration").value(hasItem(DEFAULT_MIN_REQUIRED_DELIVERY_DURATION)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validTo").value(hasItem(DEFAULT_VALID_TO.toString())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(productEntity.getVersion().intValue())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultProductShouldNotBeFound(String filter) throws Exception {
        restProductMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @Transactional
    public void getNonExistingProduct() throws Exception {
        // Get the product
        restProductMockMvc.perform(get(requestUri + "/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }
}
