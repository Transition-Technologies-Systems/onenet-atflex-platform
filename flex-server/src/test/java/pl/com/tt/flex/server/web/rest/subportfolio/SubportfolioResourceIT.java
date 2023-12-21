package pl.com.tt.flex.server.web.rest.subportfolio;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.repository.subportfolio.SubportfolioRepository;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;
import pl.com.tt.flex.server.service.subportfolio.mapper.SubportfolioMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.fsp.FspResourceAdminIT;
import pl.com.tt.flex.server.web.rest.product.ProductResourceIT;
import pl.com.tt.flex.server.web.rest.unit.UnitResourceIT;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.com.tt.flex.model.service.dto.localization.LocalizationType.COUPLING_POINT_ID;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

public abstract class SubportfolioResourceIT {

    private final static String SUBPORTFOLIO_DEFAULT_NAME = "Subportfolio-test";

    private final static boolean SUBPORTFOLIO_DEFAULT_ACTIVE = true;

    private final static boolean SUBPORTFOLIO_DEFAULT_CERTIFIED = true;

    private final static Instant SUBPORTFOLIO_DEFAULT_VALID_FROM = Instant.now().minus(2, DAYS).truncatedTo(SECONDS);

    private final static Instant SUBPORTFOLIO_DEFAULT_VALID_TO = Instant.now().minus(2, DAYS).truncatedTo(SECONDS);

    private final static String SUBPORTFOLIO_DEFAULT_MRID = "MRID";

    private final static String LOCALIZATION_DEFAULT_NAME = "Subportfolio-localization";

    private final SubportfolioRepository subportfolioRepository;
    private final SubportfolioMapper subportfolioMapper;
    private final EntityManager em;
    private final MockMvc restSubportfolioMockMvc;
    private final String requestUri;

    @MockBean
    protected UserService mockUserService;

    protected SubportfolioEntity subportfolioEntity;
    protected static ProductEntity productEntity;
    protected static FspEntity fspaEntity;
    protected static UnitEntity unitEntity;
    protected static LocalizationTypeEntity localizationCouplingPointId;

    public SubportfolioResourceIT(SubportfolioRepository subportfolioRepository, SubportfolioMapper subportfolioMapper,
                                  EntityManager em, MockMvc restSubportfolioMockMvc, String requestUri) {
        this.subportfolioRepository = subportfolioRepository;
        this.subportfolioMapper = subportfolioMapper;
        this.em = em;
        this.restSubportfolioMockMvc = restSubportfolioMockMvc;
        this.requestUri = requestUri;
    }

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static SubportfolioEntity createEntity(EntityManager em) {
        unitEntity = createUnitEntity(em);
        productEntity = createProductEntity(em);
        fspaEntity = createFspaEntity(em);
        localizationCouplingPointId = createLocalizationCouplingPointId(em);
        return SubportfolioEntity.builder()
            .name(SUBPORTFOLIO_DEFAULT_NAME)
            .active(SUBPORTFOLIO_DEFAULT_ACTIVE)
            .certified(SUBPORTFOLIO_DEFAULT_CERTIFIED)
            .couplingPointIdTypes(Collections.singleton(localizationCouplingPointId))
            .files(new HashSet<>())
            .validFrom(SUBPORTFOLIO_DEFAULT_VALID_FROM)
            .validTo(SUBPORTFOLIO_DEFAULT_VALID_TO)
            .fspa(fspaEntity)
            .units(Collections.singleton(unitEntity))
            .mrid(SUBPORTFOLIO_DEFAULT_MRID)
            .build();
    }

    private static FspEntity createFspaEntity(EntityManager em) {
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

    private static LocalizationTypeEntity createLocalizationCouplingPointId(EntityManager em) {
        LocalizationTypeEntity localizationTypeEntity = new LocalizationTypeEntity();
        localizationTypeEntity.setType(COUPLING_POINT_ID);
        localizationTypeEntity.setName(LOCALIZATION_DEFAULT_NAME);
        em.persist(localizationTypeEntity);
        em.flush();
        return localizationTypeEntity;
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
        subportfolioEntity = createEntity(em);
        mockedCurrentLoggedUser();
    }

    abstract void mockedCurrentLoggedUser();

    @Test
    @Transactional
    public void createSubportfolio() throws Exception {
        int databaseSizeBeforeCreate = subportfolioRepository.findAll().size();
        // Create the Subportfolio
        SubportfolioDTO subportfolioDTO = subportfolioMapper.toDto(subportfolioEntity);

        MockMultipartFile multipartSubportfolio = new MockMultipartFile("subportfolioDTO", "subportfolioDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(subportfolioDTO));
        restSubportfolioMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/create")
            .file(multipartSubportfolio))
            .andExpect(status().isCreated());

        // Validate the Subportfolio in the database
        List<SubportfolioEntity> subportfolioEntityList = subportfolioRepository.findAll();
        assertThat(subportfolioEntityList).hasSize(databaseSizeBeforeCreate + 1);
        SubportfolioEntity testSubportfolio = subportfolioEntityList.get(subportfolioEntityList.size() - 1);
        assertThat(testSubportfolio.getId()).isNotNull();
        assertThat(testSubportfolio.getName()).isEqualTo(SUBPORTFOLIO_DEFAULT_NAME);
        assertThat(testSubportfolio.isActive()).isEqualTo(SUBPORTFOLIO_DEFAULT_ACTIVE);
        assertThat(testSubportfolio.isCertified()).isEqualTo(SUBPORTFOLIO_DEFAULT_CERTIFIED);
        assertThat(testSubportfolio.getValidFrom()).isEqualTo(SUBPORTFOLIO_DEFAULT_VALID_FROM);
        assertThat(testSubportfolio.getValidTo()).isEqualTo(SUBPORTFOLIO_DEFAULT_VALID_TO);
        assertThat(testSubportfolio.getMrid()).isEqualTo(SUBPORTFOLIO_DEFAULT_MRID);
        assertThat(testSubportfolio.getCouplingPointIdTypes()).isNotNull();
    }

    @Test
    @Transactional
    public void certifiedSubportfolio() throws Exception {
        // Initialize the database
        // Nie mozna usuwac Subportfolio ktore jest aktywne i ktore ma podpiete DERy
        subportfolioEntity.setActive(true);
        subportfolioEntity.setCertified(true);
        subportfolioRepository.saveAndFlush(subportfolioEntity);
        em.detach(subportfolioEntity);
        SubportfolioDTO subportfolioDTO = subportfolioMapper.toDto(subportfolioEntity);

        // Certified the subportfolio
        MockMultipartFile multipartSubportfolio = new MockMultipartFile("subportfolioDTO", "subportfolioDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(subportfolioDTO));
        restSubportfolioMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/update")
            .file(multipartSubportfolio))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void certifiedSubportfolio_cannotCertifiedBecauseDerIsNotCertified() throws Exception {
        // Initialize the database
        unitEntity = TestUtil.findAll(em, UnitEntity.class).get(0);

        subportfolioEntity.setActive(false);
        subportfolioEntity.setUnits(Collections.singleton(unitEntity));
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        unitEntity.setActive(true);
        unitEntity.setCertified(false);
        em.persist(unitEntity);

        connectSuportfolioToUnit(unitEntity, subportfolioEntity);

        // Certified the subportfolio
        SubportfolioDTO subportfolioDTO = subportfolioMapper.toDto(subportfolioEntity);
        MockMultipartFile multipartSubportfolio = new MockMultipartFile("subportfolioDTO", "subportfolioDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(subportfolioDTO));
        restSubportfolioMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/update")
            .file(multipartSubportfolio))
            .andExpect(jsonPath("$.errorKey").value(CANNOT_CERTIFY))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void deleteSubportfolio() throws Exception {
        // Initialize the database
        // Nie mozna usuwac Subportfolio ktore jest aktywne i ktore ma podpiete DERy
        subportfolioEntity.setActive(false);
        subportfolioEntity.setUnits(new HashSet<>());
        subportfolioRepository.saveAndFlush(subportfolioEntity);
        em.detach(subportfolioEntity);

        int databaseSizeBeforeDelete = subportfolioRepository.findAll().size();

        // Delete the subportfolio
        restSubportfolioMockMvc.perform(delete(requestUri + "/{id}", subportfolioEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<SubportfolioEntity> flexPotentialList = subportfolioRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void deleteSubportfolio_shouldNotDeleteBecauseSubportfolioIsActive() throws Exception {
        // Initialize the database
        // Nie mozna usuwac Subportfolio ktore jest aktywne i ktore ma podpiete DERy
        subportfolioEntity.setActive(true);
        subportfolioEntity.setUnits(new HashSet<>());
        subportfolioRepository.saveAndFlush(subportfolioEntity);
        em.detach(subportfolioEntity);

        int databaseSizeBeforeDelete = subportfolioRepository.findAll().size();

        // Delete the subportfolio
        restSubportfolioMockMvc.perform(delete(requestUri + "/{id}", subportfolioEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(CANNOT_DELETE_ACTIVE_SUBPORTFOLIO))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<SubportfolioEntity> flexPotentialList = subportfolioRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void deleteSubportfolio_shouldNotDeleteBecauseContainingDers() throws Exception {
        // Initialize the database
        // Nie mozna usuwac Subportfolio ktore jest aktywne i ktore ma podpiete DERy
        unitEntity = TestUtil.findAll(em, UnitEntity.class).get(0);

        subportfolioEntity.setActive(false);
        subportfolioEntity.setUnits(Collections.singleton(unitEntity));
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        // Podpiecie Subportfolio do DERa
        connectSuportfolioToUnit(unitEntity, subportfolioEntity);

        em.detach(subportfolioEntity);
        em.detach(unitEntity);

        int databaseSizeBeforeDelete = subportfolioRepository.findAll().size();

        // Delete the subportfolio
        restSubportfolioMockMvc.perform(delete(requestUri + "/{id}", subportfolioEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(CANNOT_DELETE_SUBPORTFOLIO_CONTAINING_DERS))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<SubportfolioEntity> flexPotentialList = subportfolioRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void getAllSubportfolioByIdFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        defaultSubportfolioShouldBeFound("id.equals=" + subportfolioEntity.getId());
        defaultSubportfolioShouldNotBeFound("id.equals=" + Long.MAX_VALUE);

        defaultSubportfolioShouldBeFound("id.greaterThan=" + (subportfolioEntity.getId() - 1L));
        defaultSubportfolioShouldNotBeFound("id.greaterThan=" + Long.MAX_VALUE);

        defaultSubportfolioShouldBeFound("id.lessThan=" + (subportfolioEntity.getId() + 1L));
        defaultSubportfolioShouldNotBeFound("id.lessThan=" + -1L);

        defaultSubportfolioShouldBeFound("id.greaterThanOrEqual=" + subportfolioEntity.getId());
        defaultSubportfolioShouldNotBeFound("id.greaterThanOrEqual=" + Long.MAX_VALUE);

        defaultSubportfolioShouldBeFound("id.lessThanOrEqual=" + subportfolioEntity.getId());
        defaultSubportfolioShouldNotBeFound("id.lessThanOrEqual=" + -1L);
    }

    @Test
    @Transactional
    public void getAllSubportfolioByNameFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        defaultSubportfolioShouldBeFound("name.equals=" + subportfolioEntity.getName());
        defaultSubportfolioShouldNotBeFound("name.equals=" + RandomStringUtils.random(10));

        defaultSubportfolioShouldBeFound("name.notEquals=" + RandomStringUtils.random(10));
        defaultSubportfolioShouldNotBeFound("name.notEquals=" + subportfolioEntity.getName());

        defaultSubportfolioShouldBeFound("name.contains=" + subportfolioEntity.getName());
        defaultSubportfolioShouldNotBeFound("name.contains=" + RandomStringUtils.random(10));

        defaultSubportfolioShouldBeFound("name.doesNotContain=" + RandomStringUtils.random(10));
        defaultSubportfolioShouldNotBeFound("name.doesNotContain=" + subportfolioEntity.getName());

        defaultSubportfolioShouldBeFound("name.in=" + subportfolioEntity.getName() + "," + RandomStringUtils.random(10));
        defaultSubportfolioShouldNotBeFound("name.in=" + RandomStringUtils.random(10));
    }

    @Test
    @Transactional
    public void getAllSubportfolioByActiveFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        defaultSubportfolioShouldBeFound("active.equals=" + subportfolioEntity.isActive());
        defaultSubportfolioShouldNotBeFound("active.equals=" + !subportfolioEntity.isActive());

        defaultSubportfolioShouldBeFound("active.notEquals=" + !subportfolioEntity.isActive());
        defaultSubportfolioShouldNotBeFound("active.notEquals=" + subportfolioEntity.isActive());

        defaultSubportfolioShouldBeFound("active.in=" + subportfolioEntity.isActive() + "," + !subportfolioEntity.isActive());
        defaultSubportfolioShouldNotBeFound("active.in=" + !subportfolioEntity.isActive());

    }

    @Test
    @Transactional
    public void getAllSubportfolioByCerifiedFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        defaultSubportfolioShouldBeFound("certified.equals=" + subportfolioEntity.isCertified());
        defaultSubportfolioShouldNotBeFound("certified.equals=" + !subportfolioEntity.isCertified());

        defaultSubportfolioShouldBeFound("certified.notEquals=" + !subportfolioEntity.isCertified());
        defaultSubportfolioShouldNotBeFound("certified.notEquals=" + subportfolioEntity.isCertified());

        defaultSubportfolioShouldBeFound("certified.in=" + subportfolioEntity.isCertified() + "," + !subportfolioEntity.isCertified());
        defaultSubportfolioShouldNotBeFound("certified.in=" + !subportfolioEntity.isCertified());
    }

    @Test
    @Transactional
    public void getAllSubportfolioByNumberOfDersFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);
        connectSuportfolioToUnit(unitEntity, subportfolioEntity);
        em.detach(subportfolioEntity);
        final int numberOfDers = 1;

        defaultSubportfolioShouldBeFound("numberOfDers.equals=" + numberOfDers);
        defaultSubportfolioShouldNotBeFound("numberOfDers.equals=" + numberOfDers + 1);

        defaultSubportfolioShouldBeFound("numberOfDers.greaterThan=" + (numberOfDers - 1));
        defaultSubportfolioShouldNotBeFound("numberOfDers.greaterThan=" + Integer.MAX_VALUE);

        defaultSubportfolioShouldBeFound("numberOfDers.lessThan=" + (numberOfDers + 1));
        defaultSubportfolioShouldNotBeFound("numberOfDers.lessThan=" + -1);

        defaultSubportfolioShouldBeFound("numberOfDers.greaterThanOrEqual=" + numberOfDers);
        defaultSubportfolioShouldNotBeFound("numberOfDers.greaterThanOrEqual=" + Integer.MAX_VALUE);

        defaultSubportfolioShouldBeFound("numberOfDers.lessThanOrEqual=" + numberOfDers);
        defaultSubportfolioShouldNotBeFound("numberOfDers.lessThanOrEqual=" + -1);
    }

    @Test
    @Transactional
    public void getAllSubportfolioByCouplingPointIdTypesFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);
        connectSuportfolioToUnit(unitEntity, subportfolioEntity);
        em.detach(subportfolioEntity);

        defaultSubportfolioShouldBeFound("couplingPointIdTypes.equals=" + localizationCouplingPointId.getId());
        defaultSubportfolioShouldNotBeFound("couplingPointIdTypes.equals=" + localizationCouplingPointId.getId() + 1);

        defaultSubportfolioShouldBeFound("couplingPointIdTypes.greaterThan=" + (localizationCouplingPointId.getId() - 1));
        defaultSubportfolioShouldNotBeFound("couplingPointIdTypes.greaterThan=" + Integer.MAX_VALUE);

        defaultSubportfolioShouldBeFound("couplingPointIdTypes.lessThan=" + (localizationCouplingPointId.getId() + 1));
        defaultSubportfolioShouldNotBeFound("couplingPointIdTypes.lessThan=" + -1);

        defaultSubportfolioShouldBeFound("couplingPointIdTypes.greaterThanOrEqual=" + localizationCouplingPointId.getId());
        defaultSubportfolioShouldNotBeFound("couplingPointIdTypes.greaterThanOrEqual=" + Integer.MAX_VALUE);

        defaultSubportfolioShouldBeFound("couplingPointIdTypes.lessThanOrEqual=" + localizationCouplingPointId.getId());
        defaultSubportfolioShouldNotBeFound("couplingPointIdTypes.lessThanOrEqual=" + -1);
    }

    @Test
    @Transactional
    public void getAllSubportfolioByMridFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);
        connectSuportfolioToUnit(unitEntity, subportfolioEntity);
        em.detach(subportfolioEntity);

        defaultSubportfolioShouldBeFound("mrid.equals=" + subportfolioEntity.getMrid());
        defaultSubportfolioShouldNotBeFound("mrid.equals=" + RandomStringUtils.random(10));

        defaultSubportfolioShouldBeFound("mrid.notEquals=" + RandomStringUtils.random(10));
        defaultSubportfolioShouldNotBeFound("mrid.notEquals=" + subportfolioEntity.getMrid());

        defaultSubportfolioShouldBeFound("mrid.contains=" + subportfolioEntity.getMrid());
        defaultSubportfolioShouldNotBeFound("mrid.contains=" + RandomStringUtils.random(10));

        defaultSubportfolioShouldBeFound("mrid.doesNotContain=" + RandomStringUtils.random(10));
        defaultSubportfolioShouldNotBeFound("mrid.doesNotContain=" + subportfolioEntity.getMrid());

        defaultSubportfolioShouldBeFound("mrid.in=" + subportfolioEntity.getMrid() + "," + RandomStringUtils.random(10));
        defaultSubportfolioShouldNotBeFound("mrid.in=" + RandomStringUtils.random(10));
    }

    @Test
    @Transactional
    public void getAllSubportfolioByValidFromFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        defaultSubportfolioShouldBeFound("validFrom.equals=" + subportfolioEntity.getValidFrom());
        defaultSubportfolioShouldNotBeFound("validFrom.equals=" + subportfolioEntity.getValidFrom().plus(1, DAYS));

        defaultSubportfolioShouldBeFound("validFrom.notEquals=" + subportfolioEntity.getValidFrom().plus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("validFrom.notEquals=" + subportfolioEntity.getValidFrom());

        defaultSubportfolioShouldBeFound("validFrom.greaterThan=" + subportfolioEntity.getValidFrom().minus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("validFrom.greaterThan=" + subportfolioEntity.getValidFrom());

        defaultSubportfolioShouldBeFound("validFrom.lessThan=" + subportfolioEntity.getValidFrom().plus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("validFrom.lessThan=" + subportfolioEntity.getValidFrom());

        defaultSubportfolioShouldBeFound("validFrom.greaterThanOrEqual=" + subportfolioEntity.getValidFrom());
        defaultSubportfolioShouldNotBeFound("validFrom.greaterThanOrEqual=" + subportfolioEntity.getValidFrom().plus(1, DAYS));

        defaultSubportfolioShouldBeFound("validFrom.lessThanOrEqual=" + subportfolioEntity.getValidFrom());
        defaultSubportfolioShouldNotBeFound("validFrom.lessThanOrEqual=" + subportfolioEntity.getValidFrom().minus(1, DAYS));
    }

    @Test
    @Transactional
    public void getAllSubportfolioByValidToFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        defaultSubportfolioShouldBeFound("validTo.greaterThan=" + subportfolioEntity.getValidTo().minus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("validTo.greaterThan=" + subportfolioEntity.getValidTo());

        defaultSubportfolioShouldBeFound("validTo.lessThan=" + subportfolioEntity.getValidTo().plus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("validTo.lessThan=" + subportfolioEntity.getValidTo());

        defaultSubportfolioShouldBeFound("validTo.greaterThanOrEqual=" + subportfolioEntity.getValidTo());
        defaultSubportfolioShouldNotBeFound("validTo.greaterThanOrEqual=" + subportfolioEntity.getValidTo().plus(1, DAYS));

        defaultSubportfolioShouldBeFound("validTo.lessThanOrEqual=" + subportfolioEntity.getValidTo());
        defaultSubportfolioShouldNotBeFound("validTo.lessThanOrEqual=" + subportfolioEntity.getValidTo().minus(1, DAYS));
    }

    @Test
    @Transactional
    public void getAllSubportfolioByCreatedDateFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        defaultSubportfolioShouldBeFound("createdDate.greaterThan=" + subportfolioEntity.getCreatedDate().minus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("createdDate.greaterThan=" + subportfolioEntity.getCreatedDate().plus(1, DAYS));

        defaultSubportfolioShouldBeFound("createdDate.lessThan=" + subportfolioEntity.getCreatedDate().plus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("createdDate.lessThan=" + subportfolioEntity.getCreatedDate().minus(1, DAYS));

        defaultSubportfolioShouldBeFound("createdDate.greaterThanOrEqual=" + subportfolioEntity.getCreatedDate().minus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("createdDate.greaterThanOrEqual=" + subportfolioEntity.getCreatedDate().plus(1, DAYS));

        defaultSubportfolioShouldBeFound("createdDate.lessThanOrEqual=" + subportfolioEntity.getCreatedDate().plus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("createdDate.lessThanOrEqual=" + subportfolioEntity.getCreatedDate().minus(1, DAYS));
    }

    @Test
    @Transactional
    public void getAllSubportfolioByLastModifiedDateFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        defaultSubportfolioShouldBeFound("lastModifiedDate.greaterThan=" + subportfolioEntity.getLastModifiedDate().minus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("lastModifiedDate.greaterThan=" + subportfolioEntity.getLastModifiedDate().plus(1, DAYS));

        defaultSubportfolioShouldBeFound("lastModifiedDate.lessThan=" + subportfolioEntity.getLastModifiedDate().plus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("lastModifiedDate.lessThan=" + subportfolioEntity.getLastModifiedDate().minus(1, DAYS));

        defaultSubportfolioShouldBeFound("lastModifiedDate.greaterThanOrEqual=" + subportfolioEntity.getLastModifiedDate().minus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("lastModifiedDate.greaterThanOrEqual=" + subportfolioEntity.getLastModifiedDate().plus(1, DAYS));

        defaultSubportfolioShouldBeFound("lastModifiedDate.lessThanOrEqual=" + subportfolioEntity.getLastModifiedDate().plus(1, DAYS));
        defaultSubportfolioShouldNotBeFound("lastModifiedDate.lessThanOrEqual=" + subportfolioEntity.getLastModifiedDate().minus(1, DAYS));
    }

    protected void defaultSubportfolioShouldBeFound(String filter) throws Exception {
        restSubportfolioMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(subportfolioEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(SUBPORTFOLIO_DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].active").value(hasItem(SUBPORTFOLIO_DEFAULT_ACTIVE)))
            .andExpect(jsonPath("$.[*].certified").value(hasItem(SUBPORTFOLIO_DEFAULT_CERTIFIED)))
            .andExpect(jsonPath("$.[*].couplingPointIdTypes.[*].name").value(hasItem(LOCALIZATION_DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(SUBPORTFOLIO_DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validTo").value(hasItem(SUBPORTFOLIO_DEFAULT_VALID_TO.toString())))
            .andExpect(jsonPath("$.[*].fspa").isNotEmpty())
            .andExpect(jsonPath("$.[*].units.[*].name").value(hasItem(unitEntity.getName())))
            .andExpect(jsonPath("$.[*].mrid").value(hasItem(SUBPORTFOLIO_DEFAULT_MRID)));
    }

    protected void defaultSubportfolioShouldNotBeFound(String filter) throws Exception {
        restSubportfolioMockMvc.perform(get(requestUri + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }


    private void connectSuportfolioToUnit(UnitEntity unitEntity, SubportfolioEntity subportfolioEntity) {
        // Podpiecie Subportfolio do DERa, ktory nie jest certyfikowany
        unitEntity.setSubportfolio(subportfolioEntity);
        em.persist(unitEntity);

        subportfolioEntity.setUnits(Collections.singleton(unitEntity));
        em.persist(subportfolioEntity);
        em.flush();
    }

}
