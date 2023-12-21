package pl.com.tt.flex.server.web.rest.unit;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.unit.*;
import pl.com.tt.flex.server.domain.unit.enumeration.DerType;
import pl.com.tt.flex.server.repository.unit.UnitRepository;
import pl.com.tt.flex.server.service.dictionary.DictionaryListener;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioService;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.fsp.FspResourceAdminIT;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.com.tt.flex.model.service.dto.localization.LocalizationType.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

/**
 * Integration tests for the {@link UnitResource} REST controller.
 */
public abstract class UnitResourceIT {

    protected static final String DEFAULT_NAME = "AAAAAAAAAA";
    protected static final String UPDATED_NAME = "BBBBBBBBBB";

    protected static final String DEFAULT_CODE = "AAAAAAAAAA";
    protected static final String UPDATED_CODE = "BBBBBBBBBB";

    public static final String LOCALIZATION_NAME = "COUPLING_POINT_ID";
    public static final String POWER_STATION_NAME = "Z7601197";

    protected static final Boolean DEFAULT_AGGREGATED = false;
    protected static final Boolean UPDATED_AGGREGATED = true;

    protected static final Long DEFAULT_FSP_ID = 1L;
    protected static final Long UPDATED_FSP_ID = 2L;

    protected static final Instant DEFAULT_VALID_FROM = Instant.now().plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    protected static final Instant UPDATED_VALID_FROM = Instant.now().plus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

    protected static final Instant DEFAULT_VALID_TO = Instant.now().plus(12, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
    protected static final Instant UPDATED_VALID_TO = Instant.now().plus(13, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);

    protected static final Boolean DEFAULT_ACTIVE = false;
    protected static final Boolean UPDATED_ACTIVE = true;

    protected static final Boolean DEFAULT_CERTIFIED = false;
    protected static final Boolean UPDATED_CERTIFIED = true;

    protected static final BigDecimal DEFAULT_CONNECTION_POWER = BigDecimal.ONE;

    protected static final BigDecimal DEFAULT_SOURCE_POWER = BigDecimal.ONE;

    protected static final BigDecimal DEFAULT_P_MIN = BigDecimal.valueOf(0);
    protected static final BigDecimal DEFAULT_Q_MIN = BigDecimal.valueOf(0);
    protected static final BigDecimal DEFAULT_Q_MAX = BigDecimal.valueOf(100);

    protected static final UnitDirectionOfDeviation DEFAULT_DIRECTION_OF_DEVIATION = UnitDirectionOfDeviation.BOTH;

    protected static final String DEFAULT_PPE = "100";

    protected static final Long DEFAULT_VERSION = 1L;
    protected static final Long UPDATED_VERSION = 2L;
    protected static final Long SMALLER_VERSION = 1L - 1L;

    protected static Long FSP_ID;
    protected static Long DER_TYPE_RECEPTION_ID;
    protected static Long DER_TYPE_GENERATION_ID;
    protected static Long DER_TYPE_ENERGY_STORAGE_ID;
    protected static Long CPI_ID;
    protected static Long POWER_STATION_ID;
    protected static Long PCLV_ID;

    private final UnitRepository unitRepository;
    private final UnitMapper unitMapper;
    private final EntityManager em;
    private final MockMvc restUnitMockMvc;
    private final String requestUri;

    protected UnitEntity unitEntity;

    @MockBean
    protected UserService mockUserService;
    @Mock
    protected DictionaryListener mockDictionaryListener;
    @MockBean
    private FlexPotentialService mockFlexPotentialService;
    @MockBean
    protected SubportfolioService mockSubportfolioService;
    @MockBean
    protected SchedulingUnitService mockSchedulingUnitService;

    public UnitResourceIT(UnitRepository unitRepository, UnitMapper unitMapper,
                          EntityManager em, MockMvc restUnitMockMvc,
                          String requestUri) {
        this.unitRepository = unitRepository;
        this.unitMapper = unitMapper;
        this.restUnitMockMvc = restUnitMockMvc;
        this.requestUri = requestUri;
        this.em = em;
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static UnitEntity createEntity(EntityManager em) {
        FspEntity fspEntity = getFspEntity(em);
        LocalizationTypeEntity powerStation = getPowerStation(em);
        LocalizationTypeEntity couplingPointId = getCouplingPointId(em);
        LocalizationTypeEntity pointOfConnectionWithLv = getPointOfConnectionWithLv(em);
        return UnitEntity.builder()
            .name(DEFAULT_NAME)
            .code(DEFAULT_CODE)
            .fsp(fspEntity)
            .aggregated(DEFAULT_AGGREGATED)
            .validFrom(DEFAULT_VALID_FROM)
            .validTo(DEFAULT_VALID_TO)
            .active(DEFAULT_ACTIVE)
            .certified(DEFAULT_CERTIFIED)
            .connectionPower(DEFAULT_CONNECTION_POWER)
            .sourcePower(DEFAULT_SOURCE_POWER)
            .directionOfDeviation(DEFAULT_DIRECTION_OF_DEVIATION)
            .qMin(DEFAULT_Q_MIN)
            .qMax(DEFAULT_Q_MAX)
            .pMin(DEFAULT_P_MIN)
            .ppe(DEFAULT_PPE)
            .geoLocations(Collections.singleton(createGeoLocations()))
            .derTypeGeneration(createDerTypeGeneration(em))
            .derTypeEnergyStorage(createDerTypeEnergyStorage(em))
            .derTypeReception(createDerTypeReception(em))
            .powerStationTypes(Collections.singleton(powerStation))
            .couplingPointIdTypes(Collections.singleton(couplingPointId))
            .pointOfConnectionWithLvTypes(Collections.singleton(pointOfConnectionWithLv))
            .version(DEFAULT_VERSION)
            .build();
    }

    protected static LocalizationTypeEntity getPowerStation(EntityManager em) {
        LocalizationTypeEntity powerStationType;
        List<LocalizationTypeEntity> allLocalizationTypes = TestUtil.findAll(em, LocalizationTypeEntity.class);
        if (allLocalizationTypes.isEmpty() || allLocalizationTypes.stream().noneMatch(l -> l.getName().equals(POWER_STATION_NAME))) {
            powerStationType = new LocalizationTypeEntity();
            powerStationType.setType(POWER_STATION_ML_LV_NUMBER);
            powerStationType.setName(POWER_STATION_NAME);
            em.persist(powerStationType);
            em.flush();
            POWER_STATION_ID = powerStationType.getId();
        } else {
            powerStationType = allLocalizationTypes.stream().filter(l -> l.getName().equals(POWER_STATION_NAME)).findFirst().get();
        }
        return powerStationType;
    }

    protected static LocalizationTypeEntity getCouplingPointId(EntityManager em) {
        LocalizationTypeEntity couplingPointIdType;
        List<LocalizationTypeEntity> allLocalizationTypes = TestUtil.findAll(em, LocalizationTypeEntity.class);
        if (allLocalizationTypes.isEmpty() || allLocalizationTypes.stream().noneMatch(l -> l.getName().equals(LOCALIZATION_NAME))) {
            couplingPointIdType = new LocalizationTypeEntity();
            couplingPointIdType.setType(COUPLING_POINT_ID);
            couplingPointIdType.setName(LOCALIZATION_NAME);
            em.persist(couplingPointIdType);
            em.flush();
            CPI_ID = couplingPointIdType.getId();
        } else {
            couplingPointIdType = allLocalizationTypes.stream().filter(l -> l.getName().equals(LOCALIZATION_NAME)).findFirst().get();
        }
        return couplingPointIdType;
    }

    protected static LocalizationTypeEntity getPointOfConnectionWithLv(EntityManager em) {
        LocalizationTypeEntity pointOfConnectionWithLvType;
        List<LocalizationTypeEntity> allLocalizationTypes = TestUtil.findAll(em, LocalizationTypeEntity.class);
        String localizationName = POINT_OF_CONNECTION_WITH_LV.name();
        if (allLocalizationTypes.isEmpty() || allLocalizationTypes.stream().noneMatch(l -> l.getName().equals(localizationName))) {
            pointOfConnectionWithLvType = new LocalizationTypeEntity();
            pointOfConnectionWithLvType.setType(POINT_OF_CONNECTION_WITH_LV);
            pointOfConnectionWithLvType.setName(POINT_OF_CONNECTION_WITH_LV.name());
            em.persist(pointOfConnectionWithLvType);
            em.flush();
            PCLV_ID = pointOfConnectionWithLvType.getId();
        } else {
            pointOfConnectionWithLvType = allLocalizationTypes.stream().filter(l -> l.getName().equals(localizationName)).findFirst().get();
        }
        return pointOfConnectionWithLvType;
    }

    protected static FspEntity getFspEntity(EntityManager em) {
        FspEntity fspEntity;
        if (TestUtil.findAll(em, FspEntity.class).isEmpty()) {
            fspEntity = FspResourceAdminIT.createEntity(em);
            em.persist(fspEntity);
            em.flush();
        } else {
            fspEntity = TestUtil.findAll(em, FspEntity.class).get(0);
        }
        FSP_ID = fspEntity.getId();
        return fspEntity;
    }

    public static DerTypeEntity createDerTypeEnergyStorage(EntityManager em) {
        DerTypeEntity derTypeEntity = new DerTypeEntity();
        derTypeEntity.setType(DerType.ENERGY_STORAGE);
        derTypeEntity.setDescriptionEn("ENERGY_STORAGE");
        derTypeEntity.setDescriptionPl("ENERGY_STORAGE");
        derTypeEntity.setSderPoint(BigDecimal.ONE);
        em.persist(derTypeEntity);
        em.flush();
        DER_TYPE_ENERGY_STORAGE_ID = derTypeEntity.getId();
        return derTypeEntity;
    }

    public static DerTypeEntity createDerTypeReception(EntityManager em) {
        DerTypeEntity derTypeEntity = new DerTypeEntity();
        derTypeEntity.setType(DerType.RECEPTION);
        derTypeEntity.setDescriptionEn("RECEPTION");
        derTypeEntity.setDescriptionPl("RECEPTION");
        derTypeEntity.setSderPoint(BigDecimal.ONE);
        em.persist(derTypeEntity);
        em.flush();
        DER_TYPE_RECEPTION_ID = derTypeEntity.getId();
        return derTypeEntity;
    }

    public static DerTypeEntity createDerTypeGeneration(EntityManager em) {
        DerTypeEntity derTypeEntity = new DerTypeEntity();
        derTypeEntity.setType(DerType.GENERATION);
        derTypeEntity.setDescriptionEn("GENERATION");
        derTypeEntity.setDescriptionPl("GENERATION");
        derTypeEntity.setSderPoint(BigDecimal.ONE);
        em.persist(derTypeEntity);
        em.flush();
        DER_TYPE_GENERATION_ID = derTypeEntity.getId();
        return derTypeEntity;
    }

    public static UnitGeoLocationEntity createGeoLocations() {
        UnitGeoLocationEntity unitGeoLocationEntity = new UnitGeoLocationEntity();
        unitGeoLocationEntity.setLongitude("20");
        unitGeoLocationEntity.setLatitude("20");
        return unitGeoLocationEntity;
    }

    @NotNull
    protected static LocalizationTypeDTO getLocalizationTypeDTO(LocalizationTypeEntity couplingPointId) {
        LocalizationTypeDTO localizationTypeDTO = new LocalizationTypeDTO();
        localizationTypeDTO.setType(couplingPointId.getType());
        localizationTypeDTO.setName(couplingPointId.getName());
        localizationTypeDTO.setId(couplingPointId.getId());
        return localizationTypeDTO;
    }

    @BeforeEach
    public void initTest() {
        MockitoAnnotations.initMocks(this);
        unitEntity = createEntity(em);
        mockedCurrentLoggedUser();
    }

    @Test
    @Transactional
    public void createUnitWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = unitRepository.findAll().size();

        // Create the Unit with an existing ID
        unitEntity.setId(1L);
        UnitDTO unitDTO = unitMapper.toDto(unitEntity);

        // An entity with an existing ID cannot be created, so this API call must fail
        restUnitMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Unit in the database
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeCreate);
    }


    @Test
    @Transactional
    public void getAllUnits() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList
        restUnitMockMvc.perform(get(requestUri + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(unitEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].aggregated").value(hasItem(DEFAULT_AGGREGATED.booleanValue())))
            .andExpect(jsonPath("$.[*].fspId").value(hasItem(unitEntity.getFsp().getId().intValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validTo").value(hasItem(DEFAULT_VALID_TO.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].certified").value(hasItem(DEFAULT_CERTIFIED.booleanValue())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(unitEntity.getVersion().intValue())));
    }

    @Test
    @Transactional
    public void updateUnit_shouldNotDeactivateBecauseUnitRelatedToActivePotential() throws Exception {
        // Initialize the database
        unitEntity.setActive(true);
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Update the unit
        UnitEntity updatedUnitEntity = unitRepository.findById(unitEntity.getId()).get();
        // Disconnect from session so that the updates on updatedUnitEntity are not directly saved in db
        UnitDTO unitDTO = unitMapper.toDto(updatedUnitEntity);
        unitDTO.setActive(false);

        // Returned active FlexPotential related to updated Unit.
        Mockito.when(mockFlexPotentialService.findActiveByUnit(any())).thenReturn(singletonList(1L));

        restUnitMockMvc.perform(put(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(jsonPath("$.errorKey").value(CANNOT_DEACTIVATE_BECAUSE_OF_ACTIVE_FLEX_POTENTIALS))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateUnit_shouldNotDeactivateBecauseSubportfolioHasJoinedUnit() throws Exception {
        // Initialize the database
        unitEntity.setActive(true);
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Update the unit
        UnitEntity updatedUnitEntity = unitRepository.findById(unitEntity.getId()).get();
        // Disconnect from session so that the updates on updatedUnitEntity are not directly saved in db
        UnitDTO unitDTO = unitMapper.toDto(updatedUnitEntity);
        unitDTO.setActive(false);

        // Returned active FlexPotential related to updated Unit.
        Mockito.when(mockSubportfolioService.findByUnit(any())).thenReturn(singletonList(1L));

        restUnitMockMvc.perform(put(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(jsonPath("$.errorKey").value(CANNOT_DEACTIVATE_BECAUSE_OF_JOINED_SUBPORTFOLIOS))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void getUnitsByIdFiltering() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        Long id = unitEntity.getId();

        defaultUnitShouldBeFound("id.equals=" + id);
        defaultUnitShouldNotBeFound("id.notEquals=" + id);

        defaultUnitShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultUnitShouldNotBeFound("id.greaterThan=" + id);

        defaultUnitShouldBeFound("id.lessThanOrEqual=" + id);
        defaultUnitShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllUnitsByNameIsEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where name equals to DEFAULT_NAME
        defaultUnitShouldBeFound("name.equals=" + DEFAULT_NAME);

        // Get all the unitList where name equals to UPDATED_NAME
        defaultUnitShouldNotBeFound("name.equals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllUnitsByNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where name not equals to DEFAULT_NAME
        defaultUnitShouldNotBeFound("name.notEquals=" + DEFAULT_NAME);

        // Get all the unitList where name not equals to UPDATED_NAME
        defaultUnitShouldBeFound("name.notEquals=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllUnitsByNameIsInShouldWork() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where name in DEFAULT_NAME or UPDATED_NAME
        defaultUnitShouldBeFound("name.in=" + DEFAULT_NAME + "," + UPDATED_NAME);

        // Get all the unitList where name equals to UPDATED_NAME
        defaultUnitShouldNotBeFound("name.in=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllUnitsByNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where name is not null
        defaultUnitShouldBeFound("name.specified=true");

        // Get all the unitList where name is null
        defaultUnitShouldNotBeFound("name.specified=false");
    }

    @Test
    @Transactional
    public void getAllUnitsByNameContainsSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where name contains DEFAULT_NAME
        defaultUnitShouldBeFound("name.contains=" + DEFAULT_NAME);

        // Get all the unitList where name contains UPDATED_NAME
        defaultUnitShouldNotBeFound("name.contains=" + UPDATED_NAME);
    }

    @Test
    @Transactional
    public void getAllUnitsByNameNotContainsSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where name does not contain DEFAULT_NAME
        defaultUnitShouldNotBeFound("name.doesNotContain=" + DEFAULT_NAME);

        // Get all the unitList where name does not contain UPDATED_NAME
        defaultUnitShouldBeFound("name.doesNotContain=" + UPDATED_NAME);
    }


    @Test
    @Transactional
    public void getAllUnitsByCodeIsEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where code equals to DEFAULT_CODE
        defaultUnitShouldBeFound("code.equals=" + DEFAULT_CODE);

        // Get all the unitList where code equals to UPDATED_CODE
        defaultUnitShouldNotBeFound("code.equals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllUnitsByCodeIsNotEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where code not equals to DEFAULT_CODE
        defaultUnitShouldNotBeFound("code.notEquals=" + DEFAULT_CODE);

        // Get all the unitList where code not equals to UPDATED_CODE
        defaultUnitShouldBeFound("code.notEquals=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllUnitsByCodeIsInShouldWork() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where code in DEFAULT_CODE or UPDATED_CODE
        defaultUnitShouldBeFound("code.in=" + DEFAULT_CODE + "," + UPDATED_CODE);

        // Get all the unitList where code equals to UPDATED_CODE
        defaultUnitShouldNotBeFound("code.in=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllUnitsByCodeIsNullOrNotNull() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where code is not null
        defaultUnitShouldBeFound("code.specified=true");

        // Get all the unitList where code is null
        defaultUnitShouldNotBeFound("code.specified=false");
    }

    @Test
    @Transactional
    public void getAllUnitsByCodeContainsSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where code contains DEFAULT_CODE
        defaultUnitShouldBeFound("code.contains=" + DEFAULT_CODE);

        // Get all the unitList where code contains UPDATED_CODE
        defaultUnitShouldNotBeFound("code.contains=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllUnitsByCodeNotContainsSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where code does not contain DEFAULT_CODE
        defaultUnitShouldNotBeFound("code.doesNotContain=" + DEFAULT_CODE);

        // Get all the unitList where code does not contain UPDATED_CODE
        defaultUnitShouldBeFound("code.doesNotContain=" + UPDATED_CODE);
    }

    @Test
    @Transactional
    public void getAllUnitsByAggregatedIsEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where aggregated equals to DEFAULT_AGGREGATED
        defaultUnitShouldBeFound("aggregated.equals=" + DEFAULT_AGGREGATED);

        // Get all the unitList where aggregated equals to UPDATED_AGGREGATED
        defaultUnitShouldNotBeFound("aggregated.equals=" + UPDATED_AGGREGATED);
    }

    @Test
    @Transactional
    public void getAllUnitsByAggregatedIsNotEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where aggregated not equals to DEFAULT_AGGREGATED
        defaultUnitShouldNotBeFound("aggregated.notEquals=" + DEFAULT_AGGREGATED);

        // Get all the unitList where aggregated not equals to UPDATED_AGGREGATED
        defaultUnitShouldBeFound("aggregated.notEquals=" + UPDATED_AGGREGATED);
    }

    @Test
    @Transactional
    public void getAllUnitsByAggregatedIsInShouldWork() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where aggregated in DEFAULT_AGGREGATED or UPDATED_AGGREGATED
        defaultUnitShouldBeFound("aggregated.in=" + DEFAULT_AGGREGATED + "," + UPDATED_AGGREGATED);

        // Get all the unitList where aggregated equals to UPDATED_AGGREGATED
        defaultUnitShouldNotBeFound("aggregated.in=" + UPDATED_AGGREGATED);
    }

    @Test
    @Transactional
    public void getAllUnitsByAggregatedIsNullOrNotNull() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where aggregated is not null
        defaultUnitShouldBeFound("aggregated.specified=true");

        // Get all the unitList where aggregated is null
        defaultUnitShouldNotBeFound("aggregated.specified=false");
    }

    @Test
    @Transactional
    public void getAllUnitsByValidFromIsEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where validFrom equals to DEFAULT_VALID_FROM
        defaultUnitShouldBeFound("validFrom.equals=" + DEFAULT_VALID_FROM);

        // Get all the unitList where validFrom equals to UPDATED_VALID_FROM
        defaultUnitShouldNotBeFound("validFrom.equals=" + UPDATED_VALID_FROM);
    }

    @Test
    @Transactional
    public void getAllUnitsByValidFromIsNotEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where validFrom not equals to DEFAULT_VALID_FROM
        defaultUnitShouldNotBeFound("validFrom.notEquals=" + DEFAULT_VALID_FROM);

        // Get all the unitList where validFrom not equals to UPDATED_VALID_FROM
        defaultUnitShouldBeFound("validFrom.notEquals=" + UPDATED_VALID_FROM);
    }

    @Test
    @Transactional
    public void getAllUnitsByValidFromIsInShouldWork() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where validFrom in DEFAULT_VALID_FROM or UPDATED_VALID_FROM
        defaultUnitShouldBeFound("validFrom.in=" + DEFAULT_VALID_FROM + "," + UPDATED_VALID_FROM);

        // Get all the unitList where validFrom equals to UPDATED_VALID_FROM
        defaultUnitShouldNotBeFound("validFrom.in=" + UPDATED_VALID_FROM);
    }

    @Test
    @Transactional
    public void getAllUnitsByValidFromIsNullOrNotNull() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where validFrom is not null
        defaultUnitShouldBeFound("validFrom.specified=true");

        // Get all the unitList where validFrom is null
        defaultUnitShouldNotBeFound("validFrom.specified=false");
    }

    @Test
    @Transactional
    public void getAllUnitsByValidToIsEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where validTo equals to DEFAULT_VALID_TO
        defaultUnitShouldBeFound("validTo.equals=" + DEFAULT_VALID_TO);

        // Get all the unitList where validTo equals to UPDATED_VALID_TO
        defaultUnitShouldNotBeFound("validTo.equals=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllUnitsByValidToIsNotEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where validTo not equals to DEFAULT_VALID_TO
        defaultUnitShouldNotBeFound("validTo.notEquals=" + DEFAULT_VALID_TO);

        // Get all the unitList where validTo not equals to UPDATED_VALID_TO
        defaultUnitShouldBeFound("validTo.notEquals=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllUnitsByValidToIsInShouldWork() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where validTo in DEFAULT_VALID_TO or UPDATED_VALID_TO
        defaultUnitShouldBeFound("validTo.in=" + DEFAULT_VALID_TO + "," + UPDATED_VALID_TO);

        // Get all the unitList where validTo equals to UPDATED_VALID_TO
        defaultUnitShouldNotBeFound("validTo.in=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllUnitsByValidToIsNullOrNotNull() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where validTo is not null
        defaultUnitShouldBeFound("validTo.specified=true");

        // Get all the unitList where validTo is null
        defaultUnitShouldNotBeFound("validTo.specified=false");
    }

    @Test
    @Transactional
    public void getAllUnitsByActiveIsEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where active equals to DEFAULT_ACTIVE
        defaultUnitShouldBeFound("active.equals=" + DEFAULT_ACTIVE);

        // Get all the unitList where active equals to UPDATED_ACTIVE
        defaultUnitShouldNotBeFound("active.equals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllUnitsByActiveIsNotEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where active not equals to DEFAULT_ACTIVE
        defaultUnitShouldNotBeFound("active.notEquals=" + DEFAULT_ACTIVE);

        // Get all the unitList where active not equals to UPDATED_ACTIVE
        defaultUnitShouldBeFound("active.notEquals=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllUnitsByActiveIsInShouldWork() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where active in DEFAULT_ACTIVE or UPDATED_ACTIVE
        defaultUnitShouldBeFound("active.in=" + DEFAULT_ACTIVE + "," + UPDATED_ACTIVE);

        // Get all the unitList where active equals to UPDATED_ACTIVE
        defaultUnitShouldNotBeFound("active.in=" + UPDATED_ACTIVE);
    }

    @Test
    @Transactional
    public void getAllUnitsByActiveIsNullOrNotNull() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where active is not null
        defaultUnitShouldBeFound("active.specified=true");

        // Get all the unitList where active is null
        defaultUnitShouldNotBeFound("active.specified=false");
    }

    @Test
    @Transactional
    public void getAllUnitsByCertifiedIsEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where certified equals to DEFAULT_CERTIFIED
        defaultUnitShouldBeFound("certified.equals=" + DEFAULT_CERTIFIED);

        // Get all the unitList where certified equals to UPDATED_CERTIFIED
        defaultUnitShouldNotBeFound("certified.equals=" + UPDATED_CERTIFIED);
    }

    @Test
    @Transactional
    public void getAllUnitsByCertifiedIsNotEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where certified not equals to DEFAULT_CERTIFIED
        defaultUnitShouldNotBeFound("certified.notEquals=" + DEFAULT_CERTIFIED);

        // Get all the unitList where certified not equals to UPDATED_CERTIFIED
        defaultUnitShouldBeFound("certified.notEquals=" + UPDATED_CERTIFIED);
    }

    @Test
    @Transactional
    public void getAllUnitsByCertifiedIsInShouldWork() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where certified in DEFAULT_CERTIFIED or UPDATED_CERTIFIED
        defaultUnitShouldBeFound("certified.in=" + DEFAULT_CERTIFIED + "," + UPDATED_CERTIFIED);

        // Get all the unitList where certified equals to UPDATED_CERTIFIED
        defaultUnitShouldNotBeFound("certified.in=" + UPDATED_CERTIFIED);
    }

    @Test
    @Transactional
    public void getAllUnitsByCertifiedIsNullOrNotNull() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where certified is not null
        defaultUnitShouldBeFound("certified.specified=true");

        // Get all the unitList where certified is null
        defaultUnitShouldNotBeFound("certified.specified=false");
    }

    @Test
    @Transactional
    public void getAllUnitsByVersionIsEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where version equals to DEFAULT_VERSION
        defaultUnitShouldBeFound("version.equals=" + unitEntity.getVersion());

        // Get all the unitList where version equals to UPDATED_VERSION
        defaultUnitShouldNotBeFound("version.equals=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllUnitsByVersionIsNotEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where version not equals to DEFAULT_VERSION
        defaultUnitShouldNotBeFound("version.notEquals=" + unitEntity.getVersion());

        // Get all the unitList where version not equals to UPDATED_VERSION
        defaultUnitShouldBeFound("version.notEquals=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllUnitsByVersionIsInShouldWork() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where version in DEFAULT_VERSION or UPDATED_VERSION
        defaultUnitShouldBeFound("version.in=" + unitEntity.getVersion() + "," + Long.MAX_VALUE);

        // Get all the unitList where version equals to UPDATED_VERSION
        defaultUnitShouldNotBeFound("version.in=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllUnitsByVersionIsNullOrNotNull() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where version is not null
        defaultUnitShouldBeFound("version.specified=true");

        // Get all the unitList where version is null
        defaultUnitShouldNotBeFound("version.specified=false");
    }

    @Test
    @Transactional
    public void getAllUnitsByVersionIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where version is greater than or equal to DEFAULT_VERSION
        defaultUnitShouldBeFound("version.greaterThanOrEqual=" + unitEntity.getVersion());

        // Get all the unitList where version is greater than or equal to UPDATED_VERSION
        defaultUnitShouldNotBeFound("version.greaterThanOrEqual=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllUnitsByVersionIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where version is less than or equal to DEFAULT_VERSION
        defaultUnitShouldBeFound("version.lessThanOrEqual=" + unitEntity.getVersion());

        // Get all the unitList where version is less than or equal to SMALLER_VERSION
        defaultUnitShouldNotBeFound("version.lessThanOrEqual=" + SMALLER_VERSION);
    }

    @Test
    @Transactional
    public void getAllUnitsByVersionIsLessThanSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where version is less than DEFAULT_VERSION
        defaultUnitShouldNotBeFound("version.lessThan=" + SMALLER_VERSION);

        // Get all the unitList where version is less than UPDATED_VERSION
        defaultUnitShouldBeFound("version.lessThan=" + unitEntity.getVersion() + 1);
    }

    @Test
    @Transactional
    public void getAllUnitsByVersionIsGreaterThanSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where version is greater than DEFAULT_VERSION
        defaultUnitShouldNotBeFound("version.greaterThan=" + Long.MAX_VALUE);

        // Get all the unitList where version is greater than SMALLER_VERSION
        defaultUnitShouldBeFound("version.greaterThan=" + SMALLER_VERSION);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    protected void defaultUnitShouldBeFound(String filter) throws Exception {
        restUnitMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(unitEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].code").value(hasItem(DEFAULT_CODE)))
            .andExpect(jsonPath("$.[*].aggregated").value(hasItem(DEFAULT_AGGREGATED.booleanValue())))
            .andExpect(jsonPath("$.[*].fspId").value(hasItem(unitEntity.getFsp().getId().intValue())))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validTo").value(hasItem(DEFAULT_VALID_TO.toString())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(DEFAULT_ACTIVE.booleanValue())))
            .andExpect(jsonPath("$.[*].certified").value(hasItem(DEFAULT_CERTIFIED.booleanValue())))
            .andExpect(jsonPath("$.[*].version").value(hasItem(unitEntity.getVersion().intValue())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    protected void defaultUnitShouldNotBeFound(String filter) throws Exception {
        restUnitMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    @Transactional
    public void updateUnit() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        int databaseSizeBeforeUpdate = unitRepository.findAll().size();

        // Update the unit
        UnitEntity updatedUnitEntity = unitRepository.findById(unitEntity.getId()).get();
        // Disconnect from session so that the updates on updatedUnitEntity are not directly saved in db
        em.detach(updatedUnitEntity);
        updatedUnitEntity.setName(UPDATED_NAME);
        updatedUnitEntity.setCode(UPDATED_CODE);
        updatedUnitEntity.setAggregated(UPDATED_AGGREGATED);
        updatedUnitEntity.setValidFrom(UPDATED_VALID_FROM);
        updatedUnitEntity.setValidTo(UPDATED_VALID_TO);
        UnitDTO unitDTO = unitMapper.toDto(updatedUnitEntity);

        restUnitMockMvc.perform(put(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(status().isOk());

        // Validate the Unit in the database
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeUpdate);
        UnitEntity testUnit = unitList.get(unitList.size() - 1);
        assertThat(testUnit.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testUnit.getCode()).isEqualTo(UPDATED_CODE);
        assertThat(testUnit.isAggregated()).isEqualTo(UPDATED_AGGREGATED);
        assertThat(testUnit.getValidFrom()).isEqualTo(UPDATED_VALID_FROM);
        assertThat(testUnit.getValidTo()).isEqualTo(UPDATED_VALID_TO);
        assertThat(testUnit.isActive()).isEqualTo(UPDATED_ACTIVE);
        assertThat(testUnit.isCertified()).isEqualTo(UPDATED_CERTIFIED);
        assertThat(testUnit.getVersion()).isEqualTo(updatedUnitEntity.getVersion());
    }

    @Test
    @Transactional
    public void deleteUnit() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        int databaseSizeBeforeDelete = unitRepository.findAll().size();

        // Delete the unit
        restUnitMockMvc.perform(delete(requestUri + "/{id}", unitEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void deleteUnit_shouldNotDeletedBecauseFlexPotentialJoinedUnit() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        int databaseSizeBeforeDelete = unitRepository.findAll().size();

        // Returned active FlexPotential related to updated Unit.
        Mockito.when(mockFlexPotentialService.findByUnit(any())).thenReturn(singletonList(1L));

        // Delete the unit
        restUnitMockMvc.perform(delete(requestUri + "/{id}", unitEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(UNIT_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_FLEX_POTENTIALS))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void deleteUnit_shouldNotDeletedBecauseSchedulingUnitJoinedUnit() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        int databaseSizeBeforeDelete = unitRepository.findAll().size();

        // Returned active FlexPotential related to updated Unit.
        Mockito.when(mockSchedulingUnitService.findByUnit(any())).thenReturn(Optional.of(new SchedulingUnitMinDTO(1L, "fakeSchedulingUnit")));

        // Delete the unit
        restUnitMockMvc.perform(delete(requestUri + "/{id}", unitEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(UNIT_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_SCHEDULING_UNITS))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void deleteUnit_shouldNotDeletedBecauseSubportfolioJoinedUnit() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        int databaseSizeBeforeDelete = unitRepository.findAll().size();

        // Returned active FlexPotential related to updated Unit.
        Mockito.when(mockSubportfolioService.findByUnit(any())).thenReturn(singletonList(1L));

        // Delete the unit
        restUnitMockMvc.perform(delete(requestUri + "/{id}", unitEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(UNIT_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_SUBPORTFOLIOS))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void updateUnit_shouldNotActiveBecauseDateNowIsNotBetweenValidToAndFrom() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Update the unit
        UnitEntity updatedUnitEntity = unitRepository.findById(unitEntity.getId()).get();
        // Disconnect from session so that the updates on updatedUnitEntity are not directly saved in db
        UnitDTO unitDTO = unitMapper.toDto(updatedUnitEntity);
        unitDTO.setActive(true);
        unitDTO.setValidFrom(Instant.now().plus(5, DAYS).truncatedTo(SECONDS));
        unitDTO.setValidTo(Instant.now().plus(10, DAYS).truncatedTo(SECONDS));

        restUnitMockMvc.perform(put(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(jsonPath("$.errorKey").value(UNIT_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES))
            .andExpect(status().isBadRequest());
    }

    abstract void mockedCurrentLoggedUser();
}
