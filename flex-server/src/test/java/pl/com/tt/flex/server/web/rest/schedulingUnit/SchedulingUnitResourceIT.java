package pl.com.tt.flex.server.web.rest.schedulingUnit;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitRepository;
import pl.com.tt.flex.server.service.dictionary.localizationType.mapper.LocalizationTypeMapper;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.fsp.FspResourceAdminIT;
import pl.com.tt.flex.server.web.rest.product.ProductResourceIT;
import pl.com.tt.flex.server.web.rest.unit.UnitResourceIT;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.com.tt.flex.model.service.dto.localization.LocalizationType.COUPLING_POINT_ID;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_DELETE_SCHEDULING_UNIT_BECAUSE_IT_IS_ACTIVE;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SCHEDULING_UNIT_CANNOT_BE_CERTIFIED_BECAUSE_IT_IS_NOT_MARKED_AS_READY_FOR_TESTS;

public abstract class SchedulingUnitResourceIT {

    private final static String SCHEDULING_UNIT_NAME = "schedulingUnit - test";
    private final static String SCHEDULING_UNIT_UPDATE_NAME = "schedulingUnit - update test";

    private final static boolean SCHEDULING_UNIT_ACTIVE = false;
    private final static boolean SCHEDULING_UNIT_UPDATE_ACTIVE = true;

    private final static boolean SCHEDULING_UNIT_READY_FOR_TEST = false;

    private final static boolean SCHEDULING_UNIT_CERTIFIED = false;

    private final static String SCHEDULING_UNIT_TYPE_DESCRIPTION_EN = "Scheduling Unit Type Test";
    private final static String SCHEDULING_UNIT_TYPE_DESCRIPTION_PL = "Testowy typ Jednostki Grafikowej";

    private final SchedulingUnitRepository schedulingUnitRepository;
    private final SchedulingUnitMapper schedulingUnitMapper;
    private final LocalizationTypeMapper localizationTypeMapper;
    private final EntityManager em;
    private final MockMvc restSchedulingUnitMockMvc;
    private final String requestUri;

    @MockBean
    protected UserService mockUserService;

    protected SchedulingUnitEntity schedulingUnitEntity;
    protected static FspEntity bspEntity;
    protected static UnitEntity unitEntity;
    protected static SchedulingUnitTypeEntity schedulingUnitType;
    protected static LocalizationTypeEntity couplingPointIdType;

    public SchedulingUnitResourceIT(SchedulingUnitRepository schedulingUnitRepository, SchedulingUnitMapper schedulingUnitMapper,
                                    LocalizationTypeMapper localizationTypeMapper, EntityManager em, MockMvc restSchedulingUnitMockMvc, String requestUri) {
        this.schedulingUnitRepository = schedulingUnitRepository;
        this.schedulingUnitMapper = schedulingUnitMapper;
        this.localizationTypeMapper = localizationTypeMapper;
        this.em = em;
        this.restSchedulingUnitMockMvc = restSchedulingUnitMockMvc;
        this.requestUri = requestUri;
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SchedulingUnitEntity createEntity(EntityManager em) {
        bspEntity = createBspEntity(em);
        schedulingUnitType = createSchedulingUnitType(em);
        SchedulingUnitEntity schedulingUnitEntity = new SchedulingUnitEntity();
        schedulingUnitEntity.setName(SCHEDULING_UNIT_NAME);
        schedulingUnitEntity.setActive(SCHEDULING_UNIT_ACTIVE);
        schedulingUnitEntity.setCertified(SCHEDULING_UNIT_CERTIFIED);
        schedulingUnitEntity.setReadyForTests(SCHEDULING_UNIT_READY_FOR_TEST);
        schedulingUnitEntity.setSchedulingUnitType(schedulingUnitType);
        schedulingUnitEntity.setBsp(bspEntity);
        return schedulingUnitEntity;
    }

    private static FspEntity createBspEntity(EntityManager em) {
        FspEntity fspEntity;
        if (TestUtil.findAll(em, FspEntity.class).isEmpty()) {
            fspEntity = FspResourceAdminIT.createEntity(em);
            fspEntity.setActive(true);
            fspEntity.setRole(Role.ROLE_BALANCING_SERVICE_PROVIDER);
        } else {
            fspEntity = TestUtil.findAll(em, FspEntity.class).get(0);
            fspEntity.setActive(true);
            fspEntity.setRole(Role.ROLE_BALANCING_SERVICE_PROVIDER);
        }
        em.persist(fspEntity);
        em.flush();
        return fspEntity;
    }

    private static SchedulingUnitTypeEntity createSchedulingUnitType(EntityManager em) {
        SchedulingUnitTypeEntity schedulingUnitTypeEntity = new SchedulingUnitTypeEntity();
        schedulingUnitTypeEntity.setDescriptionEn(SCHEDULING_UNIT_TYPE_DESCRIPTION_EN);
        schedulingUnitTypeEntity.setDescriptionPl(SCHEDULING_UNIT_TYPE_DESCRIPTION_PL);
        schedulingUnitTypeEntity.setProducts(Collections.singleton(createProductEntity(em)));
        em.persist(schedulingUnitTypeEntity);
        em.flush();
        return schedulingUnitTypeEntity;
    }

    private static LocalizationTypeEntity createCouplingPointIdType(EntityManager em) {
        LocalizationTypeEntity couplingPointIdType = new LocalizationTypeEntity();
        couplingPointIdType.setType(COUPLING_POINT_ID);
        couplingPointIdType.setName(COUPLING_POINT_ID.name() + "-test");
        em.persist(couplingPointIdType);
        em.flush();
        return couplingPointIdType;
    }

    private static UnitEntity createUnitEntity(EntityManager em) {
        UnitEntity unitEntity;
        if (TestUtil.findAll(em, UnitEntity.class).isEmpty()) {
            unitEntity = UnitResourceIT.createEntity(em);
            unitEntity.setActive(true);
            unitEntity.setCertified(true);
            em.persist(unitEntity);
            em.flush();
        } else {
            unitEntity = TestUtil.findAll(em, UnitEntity.class).get(0);
            unitEntity.setActive(true);
            unitEntity.setCertified(true);
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

    @BeforeEach
    public void initTest() {
        schedulingUnitEntity = createEntity(em);
        couplingPointIdType = createCouplingPointIdType(em);
        unitEntity = createUnitEntity(em);
        mockedCurrentLoggedUser();
    }

    protected abstract void mockedCurrentLoggedUser();

    @Test
    @Transactional
    public void createSchedulingUnit() throws Exception {
        int databaseSizeBeforeCreate = schedulingUnitRepository.findAll().size();
        // Create the SchedulingUnit
        SchedulingUnitDTO schedulingUnitDTO = schedulingUnitMapper.toDto(schedulingUnitEntity);
        MockMultipartFile multipartProduct = new MockMultipartFile("schedulingUnitDTO", "schedulingUnitDTO",
            MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(schedulingUnitDTO));
        restSchedulingUnitMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/create")
            .file(multipartProduct))
            .andExpect(status().isCreated());

        // Validate the SchedulingUnit in the database
        List<SchedulingUnitEntity> schedulingUnitEntityList = schedulingUnitRepository.findAll();
        assertThat(schedulingUnitEntityList).hasSize(databaseSizeBeforeCreate + 1);
        SchedulingUnitEntity testSchedulingUnit = schedulingUnitEntityList.get(schedulingUnitEntityList.size() - 1);
        assertThat(testSchedulingUnit.getId()).isNotNull();
        assertThat(testSchedulingUnit.getName()).isEqualTo(SCHEDULING_UNIT_NAME);
        assertThat(testSchedulingUnit.isActive()).isEqualTo(SCHEDULING_UNIT_ACTIVE);
        assertThat(testSchedulingUnit.isCertified()).isEqualTo(SCHEDULING_UNIT_CERTIFIED);
        assertThat(testSchedulingUnit.isReadyForTests()).isEqualTo(SCHEDULING_UNIT_READY_FOR_TEST);
        assertThat(testSchedulingUnit.getSchedulingUnitType()).isEqualTo(schedulingUnitType);
        assertThat(testSchedulingUnit.getNumberOfDers()).isNull();
        assertThat(testSchedulingUnit.getBsp()).isEqualTo(bspEntity);
        assertThat(testSchedulingUnit.getCouplingPoints()).isEmpty();
        assertThat(testSchedulingUnit.getPrimaryCouplingPoint()).isNull();
    }

    @Test
    @Transactional
    public void createSchedulingUnit_shouldNotCertifiedBecauseNotMarketReadyForTest() throws Exception {
        int databaseSizeBeforeCreate = schedulingUnitRepository.findAll().size();

        SchedulingUnitDTO schedulingUnitDTO = schedulingUnitMapper.toDto(schedulingUnitEntity);
        schedulingUnitDTO.setCertified(true);
        schedulingUnitDTO.setReadyForTests(false);

        MockMultipartFile multipartProduct = new MockMultipartFile("schedulingUnitDTO", "schedulingUnitDTO", MediaType.APPLICATION_JSON_VALUE,
            TestUtil.convertObjectToJsonBytes(schedulingUnitDTO));

        restSchedulingUnitMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/create")
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(SCHEDULING_UNIT_CANNOT_BE_CERTIFIED_BECAUSE_IT_IS_NOT_MARKED_AS_READY_FOR_TESTS))
            .andExpect(status().isBadRequest());

        // Validate the database contains the same items
        List<SchedulingUnitEntity> schedulingUnitEntityList = schedulingUnitRepository.findAll();
        assertThat(schedulingUnitEntityList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void updateSchedulingUnit() throws Exception {
        SchedulingUnitEntity savedSchedulingUnit = schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);

        int databaseSizeBeforeUpdate = schedulingUnitRepository.findAll().size();

        SchedulingUnitEntity schedulingUnitEntity = schedulingUnitRepository.findById(savedSchedulingUnit.getId()).get();

        SchedulingUnitDTO updatedSchedulingUnitDTO = schedulingUnitMapper.toDto(schedulingUnitEntity);
        updatedSchedulingUnitDTO.setName(SCHEDULING_UNIT_UPDATE_NAME);
        updatedSchedulingUnitDTO.setActive(SCHEDULING_UNIT_UPDATE_ACTIVE);
        LocalizationTypeDTO localizationTypeDTO = localizationTypeMapper.toDto(couplingPointIdType);
        updatedSchedulingUnitDTO.setCouplingPoints(Collections.singletonList(localizationTypeDTO));
        updatedSchedulingUnitDTO.setPrimaryCouplingPoint(localizationTypeDTO);

        MockMultipartFile multipartProduct = new MockMultipartFile("schedulingUnitDTO", "schedulingUnitDTO",
            MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(updatedSchedulingUnitDTO));
        restSchedulingUnitMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/update")
            .file(multipartProduct))
            .andExpect(status().isOk());

        // Validate the SchedulingUnit in the database
        List<SchedulingUnitEntity> schedulingUnitEntityList = schedulingUnitRepository.findAll();
        assertThat(schedulingUnitEntityList).hasSize(databaseSizeBeforeUpdate);
        SchedulingUnitEntity testSchedulingUnit = schedulingUnitEntityList.get(schedulingUnitEntityList.size() - 1);
        assertThat(testSchedulingUnit.getId()).isNotNull();
        assertThat(testSchedulingUnit.getName()).isEqualTo(SCHEDULING_UNIT_UPDATE_NAME);
        assertThat(testSchedulingUnit.isActive()).isEqualTo(SCHEDULING_UNIT_UPDATE_ACTIVE);
        assertThat(testSchedulingUnit.isCertified()).isFalse();
        assertThat(testSchedulingUnit.isReadyForTests()).isFalse();
        assertThat(testSchedulingUnit.getSchedulingUnitType()).isEqualTo(schedulingUnitType);
        assertThat(testSchedulingUnit.getNumberOfDers()).isNull();
        assertThat(testSchedulingUnit.getBsp()).isEqualTo(bspEntity);
        assertThat(testSchedulingUnit.getPrimaryCouplingPoint()).isEqualTo(couplingPointIdType);
    }

    @Test
    @Transactional
    public void deleteSchedulingUnit() throws Exception {
        // Initialize the database
        // Nie mozna usuwac SchedulingUnit ktore jest aktywne
        schedulingUnitEntity.setActive(false);
        schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);
        em.detach(schedulingUnitEntity);

        int databaseSizeBeforeDelete = schedulingUnitRepository.findAll().size();

        // Delete the SchedulingUnit
        restSchedulingUnitMockMvc.perform(delete(requestUri + "/{id}", schedulingUnitEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<SchedulingUnitEntity> schedulingUnitEntityList = schedulingUnitRepository.findAll();
        assertThat(schedulingUnitEntityList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void deleteSchedulingUnit_shouldNotDeleteBecauseSchedulingUnitIsActive() throws Exception {
        // Initialize the database
        // Nie mozna usuwac SchedulingUnit ktore jest aktywne
        schedulingUnitEntity.setActive(true);
        schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);
        em.detach(schedulingUnitEntity);

        int databaseSizeBeforeDelete = schedulingUnitRepository.findAll().size();

        // Delete the SchedulingUnit
        restSchedulingUnitMockMvc.perform(delete(requestUri + "/{id}", schedulingUnitEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(CANNOT_DELETE_SCHEDULING_UNIT_BECAUSE_IT_IS_ACTIVE))
            .andExpect(status().isBadRequest());

        List<SchedulingUnitEntity> schedulingUnitEntityList = schedulingUnitRepository.findAll();
        assertThat(schedulingUnitEntityList).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void getAllSchedulingUnitByIdFiltering() throws Exception {
        // Initialize the database
        schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);

        defaultSchedulingUnitShouldBeFound("id.equals=" + schedulingUnitEntity.getId());
        defaultSchedulingUnitShouldNotBeFound("id.equals=" + Long.MAX_VALUE);

        defaultSchedulingUnitShouldBeFound("id.greaterThan=" + (schedulingUnitEntity.getId() - 1L));
        defaultSchedulingUnitShouldNotBeFound("id.greaterThan=" + Long.MAX_VALUE);

        defaultSchedulingUnitShouldBeFound("id.lessThan=" + (schedulingUnitEntity.getId() + 1L));
        defaultSchedulingUnitShouldNotBeFound("id.lessThan=" + -1L);

        defaultSchedulingUnitShouldBeFound("id.greaterThanOrEqual=" + schedulingUnitEntity.getId());
        defaultSchedulingUnitShouldNotBeFound("id.greaterThanOrEqual=" + Long.MAX_VALUE);

        defaultSchedulingUnitShouldBeFound("id.lessThanOrEqual=" + schedulingUnitEntity.getId());
        defaultSchedulingUnitShouldNotBeFound("id.lessThanOrEqual=" + -1L);
    }

    @Test
    @Transactional
    public void getAllSchedulingUnitByNameFiltering() throws Exception {
        // Initialize the database
        schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);

        defaultSchedulingUnitShouldBeFound("name.equals=" + schedulingUnitEntity.getName());
        defaultSchedulingUnitShouldNotBeFound("name.equals=" + RandomStringUtils.random(10));

        defaultSchedulingUnitShouldBeFound("name.notEquals=" + RandomStringUtils.random(10));
        defaultSchedulingUnitShouldNotBeFound("name.notEquals=" + schedulingUnitEntity.getName());

        defaultSchedulingUnitShouldBeFound("name.in=" + schedulingUnitEntity.getName() + "," + RandomStringUtils.random(10));
        defaultSchedulingUnitShouldNotBeFound("name.in=" + RandomStringUtils.random(10));

        defaultSchedulingUnitShouldBeFound("name.contains=" + schedulingUnitEntity.getName());
        defaultSchedulingUnitShouldNotBeFound("name.contains=" + RandomStringUtils.random(10));
    }

    @Test
    @Transactional
    public void getAllSchedulingUnitByBspRepresentativeCompanyNameFiltering() throws Exception {
        // Initialize the database
        schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);

        defaultSchedulingUnitShouldBeFound("bspRepresentativeCompanyName.equals=" + schedulingUnitEntity.getBsp().getCompanyName());
        defaultSchedulingUnitShouldNotBeFound("bspRepresentativeCompanyName.equals=" + RandomStringUtils.random(10));

        defaultSchedulingUnitShouldBeFound("bspRepresentativeCompanyName.notEquals=" + RandomStringUtils.random(10));
        defaultSchedulingUnitShouldNotBeFound("bspRepresentativeCompanyName.notEquals=" + schedulingUnitEntity.getBsp().getCompanyName());

        defaultSchedulingUnitShouldBeFound("bspRepresentativeCompanyName.in=" + schedulingUnitEntity.getBsp().getCompanyName() + "," + RandomStringUtils.random(10));
        defaultSchedulingUnitShouldNotBeFound("bspRepresentativeCompanyName.in=" + RandomStringUtils.random(10));

        defaultSchedulingUnitShouldBeFound("bspRepresentativeCompanyName.contains=" + schedulingUnitEntity.getBsp().getCompanyName());
        defaultSchedulingUnitShouldNotBeFound("bspRepresentativeCompanyName.contains=" + RandomStringUtils.random(10));
    }

    @Test
    @Transactional
    public void getAllSchedulingUnitBySchedulingUnitTypeIdFiltering() throws Exception {
        // Initialize the database
        schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);

        defaultSchedulingUnitShouldBeFound("schedulingUnitTypeId.equals=" + schedulingUnitEntity.getSchedulingUnitType().getId());
        defaultSchedulingUnitShouldNotBeFound("schedulingUnitTypeId.equals=" + Long.MAX_VALUE);

        defaultSchedulingUnitShouldBeFound("schedulingUnitTypeId.greaterThan=" + (schedulingUnitEntity.getSchedulingUnitType().getId() - 1L));
        defaultSchedulingUnitShouldNotBeFound("schedulingUnitTypeId.greaterThan=" + Long.MAX_VALUE);

        defaultSchedulingUnitShouldBeFound("schedulingUnitTypeId.lessThan=" + (schedulingUnitEntity.getSchedulingUnitType().getId() + 1L));
        defaultSchedulingUnitShouldNotBeFound("schedulingUnitTypeId.lessThan=" + -1L);

        defaultSchedulingUnitShouldBeFound("schedulingUnitTypeId.greaterThanOrEqual=" + schedulingUnitEntity.getSchedulingUnitType().getId());
        defaultSchedulingUnitShouldNotBeFound("schedulingUnitTypeId.greaterThanOrEqual=" + Long.MAX_VALUE);

        defaultSchedulingUnitShouldBeFound("schedulingUnitTypeId.lessThanOrEqual=" + schedulingUnitEntity.getSchedulingUnitType().getId());
        defaultSchedulingUnitShouldNotBeFound("schedulingUnitTypeId.lessThanOrEqual=" + -1L);
    }

    @Test
    @Transactional
    public void getAllSchedulingUnitByActiveFiltering() throws Exception {
        // Initialize the database
        schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);

        defaultSchedulingUnitShouldBeFound("active.equals=" + schedulingUnitEntity.isActive());
        defaultSchedulingUnitShouldNotBeFound("active.equals=" + !schedulingUnitEntity.isActive());

        defaultSchedulingUnitShouldBeFound("active.notEquals=" + !schedulingUnitEntity.isActive());
        defaultSchedulingUnitShouldNotBeFound("active.notEquals=" + schedulingUnitEntity.isActive());

        defaultSchedulingUnitShouldBeFound("active.in=" + schedulingUnitEntity.isActive() + "," + !schedulingUnitEntity.isActive());
        defaultSchedulingUnitShouldNotBeFound("active.in=" + !schedulingUnitEntity.isActive());
    }

    @Test
    @Transactional
    public void getAllSchedulingUnitByCertifiedFiltering() throws Exception {
        // Initialize the database
        schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);

        defaultSchedulingUnitShouldBeFound("certified.equals=" + schedulingUnitEntity.isCertified());
        defaultSchedulingUnitShouldNotBeFound("certified.equals=" + !schedulingUnitEntity.isCertified());

        defaultSchedulingUnitShouldBeFound("certified.notEquals=" + !schedulingUnitEntity.isCertified());
        defaultSchedulingUnitShouldNotBeFound("certified.notEquals=" + schedulingUnitEntity.isCertified());

        defaultSchedulingUnitShouldBeFound("certified.in=" + schedulingUnitEntity.isCertified() + "," + !schedulingUnitEntity.isCertified());
        defaultSchedulingUnitShouldNotBeFound("certified.in=" + !schedulingUnitEntity.isCertified());
    }

    @Test
    @Transactional
    public void getAllSchedulingUnitByReadyForTestsFiltering() throws Exception {
        // Initialize the database
        schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);

        defaultSchedulingUnitShouldBeFound("readyForTests.equals=" + schedulingUnitEntity.isReadyForTests());
        defaultSchedulingUnitShouldNotBeFound("readyForTests.equals=" + !schedulingUnitEntity.isReadyForTests());

        defaultSchedulingUnitShouldBeFound("readyForTests.notEquals=" + !schedulingUnitEntity.isReadyForTests());
        defaultSchedulingUnitShouldNotBeFound("readyForTests.notEquals=" + schedulingUnitEntity.isReadyForTests());

        defaultSchedulingUnitShouldBeFound("readyForTests.in=" + schedulingUnitEntity.isReadyForTests() + "," + !schedulingUnitEntity.isReadyForTests());
        defaultSchedulingUnitShouldNotBeFound("readyForTests.in=" + !schedulingUnitEntity.isReadyForTests());
    }

    @Transactional
    public void getAllSchedulingUnitByCreatedByFiltering() throws Exception {
        // Initialize the database
        schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);

        defaultSchedulingUnitShouldBeFound("createdBy.equals=" + schedulingUnitEntity.getCreatedBy());
        defaultSchedulingUnitShouldNotBeFound("createdBy.equals=" + RandomStringUtils.random(10));

        defaultSchedulingUnitShouldBeFound("createdBy.notEquals=" + RandomStringUtils.random(10));
        defaultSchedulingUnitShouldNotBeFound("createdBy.notEquals=" + schedulingUnitEntity.getCreatedBy());

        defaultSchedulingUnitShouldBeFound("createdBy.in=" + schedulingUnitEntity.getCreatedBy() + "," + RandomStringUtils.random(10));
        defaultSchedulingUnitShouldNotBeFound("createdBy.in=" + RandomStringUtils.random(10));

        defaultSchedulingUnitShouldBeFound("createdBy.contains=" + schedulingUnitEntity.getCreatedBy());
        defaultSchedulingUnitShouldNotBeFound("createdBy.contains=" + RandomStringUtils.random(10));
    }

    @Transactional
    public void getAllSchedulingUnitByLastModifiedByFiltering() throws Exception {
        // Initialize the database
        schedulingUnitRepository.saveAndFlush(schedulingUnitEntity);

        defaultSchedulingUnitShouldBeFound("lastModifiedBy.equals=" + schedulingUnitEntity.getLastModifiedBy());
        defaultSchedulingUnitShouldNotBeFound("lastModifiedBy.equals=" + RandomStringUtils.random(10));

        defaultSchedulingUnitShouldBeFound("lastModifiedBy.notEquals=" + RandomStringUtils.random(10));
        defaultSchedulingUnitShouldNotBeFound("lastModifiedBy.notEquals=" + schedulingUnitEntity.getLastModifiedBy());

        defaultSchedulingUnitShouldBeFound("lastModifiedBy.in=" + schedulingUnitEntity.getLastModifiedBy() + "," + RandomStringUtils.random(10));
        defaultSchedulingUnitShouldNotBeFound("lastModifiedBy.in=" + RandomStringUtils.random(10));

        defaultSchedulingUnitShouldBeFound("lastModifiedBy.contains=" + schedulingUnitEntity.getLastModifiedBy());
        defaultSchedulingUnitShouldNotBeFound("lastModifiedBy.contains=" + RandomStringUtils.random(10));
    }


    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultSchedulingUnitShouldBeFound(String filter) throws Exception {
        restSchedulingUnitMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(schedulingUnitEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(schedulingUnitEntity.getName())))
            .andExpect(jsonPath("$.[*].active").value(hasItem(schedulingUnitEntity.isActive())))
            .andExpect(jsonPath("$.[*].schedulingUnitType.id").value(hasItem(schedulingUnitEntity.getSchedulingUnitType().getId().intValue())))
            .andExpect(jsonPath("$.[*].bsp.id").value(hasItem(bspEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].readyForTests").value(hasItem(schedulingUnitEntity.isReadyForTests())))
            .andExpect(jsonPath("$.[*].certified").value(hasItem(schedulingUnitEntity.isCertified())))
            .andExpect(jsonPath("$.[*].units").value(hasItem(empty())))
            .andExpect(jsonPath("$.[*].couplingPoints").value(hasItem(empty())))
            .andExpect(jsonPath("$.[*].createdBy").value(hasItem(notNullValue())))
            .andExpect(jsonPath("$.[*].createdDate").value(hasItem(notNullValue())))
            .andExpect(jsonPath("$.[*].lastModifiedBy").value(hasItem(notNullValue())))
            .andExpect(jsonPath("$.[*].lastModifiedDate").value(hasItem(notNullValue())));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultSchedulingUnitShouldNotBeFound(String filter) throws Exception {
        restSchedulingUnitMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }
}
