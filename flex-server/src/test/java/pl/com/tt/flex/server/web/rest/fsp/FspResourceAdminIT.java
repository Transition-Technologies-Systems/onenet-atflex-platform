package pl.com.tt.flex.server.web.rest.fsp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.enumeration.CreationSource;
import pl.com.tt.flex.server.repository.fsp.FspRepository;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.fsp.FspQueryService;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.fsp.mapper.FspMapper;
import pl.com.tt.flex.server.service.potential.FlexPotentialQueryService;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.user.UserResourceIT;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

/**
 * Integration tests for the {@link FspResourceAdmin} REST controller.
 */
@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = {FLEX_ADMIN_FSP_VIEW, FLEX_ADMIN_FSP_MANAGE, FLEX_ADMIN_FSP_DELETE})
public class FspResourceAdminIT {

    private static final String DEFAULT_COMPANY_NAME = "AAAAAAAAAA";
    private static final String UPDATED_COMPANY_NAME = "BBBBBBBBBB";

    private static final Role DEFAULT_ROLE = Role.ROLE_FLEX_SERVICE_PROVIDER;

    private static final Instant DEFAULT_VALID_FROM = Instant.now().truncatedTo(SECONDS);
    private static final Instant UPDATED_VALID_FROM = Instant.now().plus(1, DAYS).truncatedTo(SECONDS);

    private static final Instant DEFAULT_VALID_TO = Instant.now().plus(10, DAYS).truncatedTo(SECONDS);
    private static final Instant UPDATED_VALID_TO = Instant.now().plus(11, DAYS).truncatedTo(SECONDS);

    @Autowired
    private FspRepository fspRepository;

    @Autowired
    private FspMapper fspMapper;

    @Autowired
    private FspService fspService;

    @Autowired
    private FspQueryService fspQueryService;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restFspMockMvc;

    @MockBean
    protected UserService mockUserService;

    @MockBean
    private FlexPotentialQueryService mockFlexPotentialQueryService;

    private FspEntity fspEntity;

    /**
     * Create an entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FspEntity createEntity(EntityManager em) {
        FspEntity fspEntity = FspEntity.builder()
            .validFrom(DEFAULT_VALID_FROM)
            .validTo(DEFAULT_VALID_TO)
            .role(DEFAULT_ROLE)
            .companyName(DEFAULT_COMPANY_NAME)
            .active(true).build();
        // Add required entity
        UserEntity userEntity = createUserEntity(em, fspEntity);
        fspEntity.setOwner(userEntity);
        fspEntity.setUsers(Set.of(userEntity));
        return fspEntity;
    }

    private static UserEntity createUserEntity(EntityManager em, FspEntity fspEntity) {
        UserEntity userEntity;
        if (TestUtil.findAll(em, UserEntity.class).isEmpty()) {
            userEntity = UserResourceIT.createEntity(em);
            userEntity.setFsp(fspEntity);
            em.persist(userEntity);
            em.flush();
        } else {
            userEntity = TestUtil.findAll(em, UserEntity.class).get(0);
        }
        return userEntity;
    }

    /**
     * Create an updated entity for this test.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static FspEntity createUpdatedEntity(EntityManager em) {
        FspEntity fspEntity = FspEntity.builder()
            .validFrom(UPDATED_VALID_FROM)
            .validTo(UPDATED_VALID_TO).build();
        // Add required entity
        UserEntity userEntity = createUserEntity(em, fspEntity);
        fspEntity.setOwner(userEntity);
        return fspEntity;
    }

    @BeforeEach
    public void initTest() {
        fspEntity = createEntity(em);
        mockedCurrentLoggedUserEntity("admin", Role.ROLE_ADMIN);
    }

    @Test
    @Transactional
    public void updateFsp_shouldUpdateWithUserRoleTSO() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);
        mockedCurrentLoggedUserDto("tso", Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR);
        findFspOwnerMock();

        //TSO moze edytowac tylko pole 'agreementWithTso'
        boolean agreementWithTsoBeforeUpdate = fspEntity.isAgreementWithTso();
        boolean agreementWithTsoAfterUpdate = !agreementWithTsoBeforeUpdate;
        boolean activeBeforeUpdate = fspEntity.isActive();
        boolean activeAfterUpdate = !activeBeforeUpdate;

        FspDTO fspDTO = fspMapper.toDto(fspEntity);
        fspDTO.setAgreementWithTso(agreementWithTsoAfterUpdate);
        fspDTO.setActive(activeAfterUpdate);


        restFspMockMvc.perform(put("/api/fsps")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(fspDTO)))
            .andExpect(jsonPath("$.agreementWithTso").value(agreementWithTsoAfterUpdate))
            .andExpect(jsonPath("$.active").value(activeBeforeUpdate))
            .andExpect(status().isOk());
    }


    @Test
    @Transactional
    public void updateFsp_shouldUpdateWithUserRoleMO() throws Exception {
        // Initialize the database
        fspEntity.setActive(false);
        fspRepository.saveAndFlush(fspEntity);
        UserEntity userEntity = createUserEntity(em, fspEntity);
        em.persist(userEntity);
        em.flush();
        mockedCurrentLoggedUserDto("mo", Role.ROLE_MARKET_OPERATOR);
        findFspOwnerMock();
        Optional<FspEntity> byId = fspRepository.findById(fspEntity.getId());

        //MO moze edytowac kazde pole
        boolean agreementWithTsoBeforeUpdate = fspEntity.isAgreementWithTso();
        boolean agreementWithTsoAfterUpdate = !agreementWithTsoBeforeUpdate;
        boolean activeBeforeUpdate = fspEntity.isActive();
        boolean activeAfterUpdate = !activeBeforeUpdate;

        FspDTO fspDTO = fspMapper.toDto(fspEntity);
        fspDTO.setAgreementWithTso(agreementWithTsoAfterUpdate);
        fspDTO.setActive(activeAfterUpdate);

        restFspMockMvc.perform(put("/api/fsps")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(fspDTO)))
            .andExpect(jsonPath("$.agreementWithTso").value(agreementWithTsoAfterUpdate))
            .andExpect(jsonPath("$.active").value(activeAfterUpdate))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void updateFsp_shouldUpdateWithUserRoleAdmin() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);
        mockedCurrentLoggedUserDto("admin", Role.ROLE_ADMIN);
        findFspOwnerMock();

        //MO moze edytowac kazde pole
        boolean agreementWithTsoBeforeUpdate = fspEntity.isAgreementWithTso();
        boolean agreementWithTsoAfterUpdate = !agreementWithTsoBeforeUpdate;
        boolean activeBeforeUpdate = fspEntity.isActive();
        boolean activeAfterUpdate = !activeBeforeUpdate;

        FspDTO fspDTO = fspMapper.toDto(fspEntity);
        fspDTO.setAgreementWithTso(agreementWithTsoAfterUpdate);
        fspDTO.setActive(activeAfterUpdate);


        restFspMockMvc.perform(put("/api/fsps")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(fspDTO)))
            .andExpect(jsonPath("$.agreementWithTso").value(agreementWithTsoAfterUpdate))
            .andExpect(jsonPath("$.active").value(activeAfterUpdate))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void updateFsp_shouldNotDeactivateBecauseUnitRelatedToActivePotential() throws Exception {
        // Initialize the database
        fspEntity.setActive(true);
        fspRepository.saveAndFlush(fspEntity);
        mockedCurrentLoggedUserDto("mo", Role.ROLE_ADMIN);
        findFspOwnerMock();

        FspDTO fspDTO = fspMapper.toDto(fspEntity);
        fspDTO.setActive(false);

        //Mock, zwraca aktywny FlexPotential podpięty pod FSP
        Mockito.when(mockFlexPotentialQueryService.findByCriteria(ArgumentMatchers.any())).thenReturn(Collections.singletonList(new FlexPotentialDTO()));

        restFspMockMvc.perform(put("/api/fsps")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(fspDTO)))
            .andExpect(jsonPath("$.errorKey").value(CANNOT_DEACTIVATE_BECAUSE_OF_ACTIVE_FLEX_POTENTIALS))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateFsp_shouldNotUpdateBecauseValidToIsBeforeValidFrom() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);
        mockedCurrentLoggedUserDto("mo", Role.ROLE_ADMIN);
        findFspOwnerMock();

        FspDTO fspDTO = fspMapper.toDto(fspEntity);
        fspDTO.setValidFrom(Instant.now().plus(1, DAYS));
        fspDTO.setValidTo(Instant.now().minus(1, DAYS));

        restFspMockMvc.perform(put("/api/fsps")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(fspDTO)))
            .andExpect(jsonPath("$.errorKey").value(FROM_DATE_AFTER_TO_DATE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateFsp_shouldNotActiveBecauseDateNowIsNotBetweenFromAndToDates() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);
        mockedCurrentLoggedUserDto("mo", Role.ROLE_ADMIN);
        findFspOwnerMock();

        FspDTO fspDTO = fspMapper.toDto(fspEntity);
        fspDTO.setActive(true);
        fspDTO.setValidFrom(Instant.now().plus(1, DAYS));
        fspDTO.setValidTo(Instant.now().plus(2, DAYS));

        restFspMockMvc.perform(put("/api/fsps")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(fspDTO)))
            .andExpect(jsonPath("$.errorKey").value(FSP_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateFsp_shouldNotUpdateBecauseValidFromIsBeforeCreatedDate() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);
        mockedCurrentLoggedUserDto("mo", Role.ROLE_ADMIN);
        findFspOwnerMock();

        FspDTO fspDTO = fspMapper.toDto(fspEntity);
        fspDTO.setValidFrom(Instant.now().minus(1, DAYS));

        //Mock, zwraca aktywny FlexPotential podpięty pod FSP
        Mockito.when(mockFlexPotentialQueryService.findByCriteria(ArgumentMatchers.any())).thenReturn(Collections.singletonList(new FlexPotentialDTO()));

        restFspMockMvc.perform(put("/api/fsps")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(fspDTO)))
            .andExpect(jsonPath("$.errorKey").value(FROM_DATE_BEFORE_CREATED_DATE))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateFsp_shouldNotDeleteBecauseJoinedToFlexPotential() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);
        mockedCurrentLoggedUserDto("mo", Role.ROLE_ADMIN);
        findFspOwnerMock();

        //Mock, zwraca aktywny FlexPotential podpięty pod FSP
        Mockito.when(mockFlexPotentialQueryService.findByCriteria(ArgumentMatchers.any())).thenReturn(Collections.singletonList(new FlexPotentialDTO()));

        restFspMockMvc.perform(delete("/api/fsps/" + fspEntity.getId())
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(FSP_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_FLEX_POTENTIALS))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void getAllFsps() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList
        restFspMockMvc.perform(get("/api/fsps?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fspEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].companyName").value(hasItem(DEFAULT_COMPANY_NAME)))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validTo").value(hasItem(DEFAULT_VALID_TO.toString())));
    }

    @Test
    @Transactional
    public void getFsp() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get the fsp
        restFspMockMvc.perform(get("/api/fsps/{id}", fspEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(fspEntity.getId().intValue()))
            .andExpect(jsonPath("$.companyName").value(DEFAULT_COMPANY_NAME))
            .andExpect(jsonPath("$.validFrom").value(DEFAULT_VALID_FROM.toString()))
            .andExpect(jsonPath("$.validTo").value(DEFAULT_VALID_TO.toString()));
    }


    @Test
    @Transactional
    public void getFspsByIdFiltering() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        Long id = fspEntity.getId();

        defaultFspShouldBeFound("id.equals=" + id);
        defaultFspShouldNotBeFound("id.notEquals=" + id);

        defaultFspShouldBeFound("id.greaterThanOrEqual=" + id);
        defaultFspShouldNotBeFound("id.greaterThan=" + id);

        defaultFspShouldBeFound("id.lessThanOrEqual=" + id);
        defaultFspShouldNotBeFound("id.lessThan=" + id);
    }


    @Test
    @Transactional
    public void getAllFspsByCompanyNameIsEqualToSomething() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where representativeCompanyName equals to DEFAULT_COMPANY_NAME
        defaultFspShouldBeFound("representativeCompanyName.equals=" + DEFAULT_COMPANY_NAME);

        // Get all the fspList where representativeCompanyName equals to UPDATED_COMPANY_NAME
        defaultFspShouldNotBeFound("representativeCompanyName.equals=" + UPDATED_COMPANY_NAME);
    }

    @Test
    @Transactional
    public void getAllFspsByCompanyNameIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where representativeCompanyName not equals to DEFAULT_COMPANY_NAME
        defaultFspShouldNotBeFound("representativeCompanyName.notEquals=" + DEFAULT_COMPANY_NAME);

        // Get all the fspList where representativeCompanyName not equals to UPDATED_COMPANY_NAME
        defaultFspShouldBeFound("representativeCompanyName.notEquals=" + UPDATED_COMPANY_NAME);
    }

    @Test
    @Transactional
    public void getAllFspsByCompanyNameIsInShouldWork() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where representativeCompanyName in DEFAULT_COMPANY_NAME or UPDATED_COMPANY_NAME
        defaultFspShouldBeFound("representativeCompanyName.in=" + DEFAULT_COMPANY_NAME + "," + UPDATED_COMPANY_NAME);

        // Get all the fspList where representativeCompanyName equals to UPDATED_COMPANY_NAME
        defaultFspShouldNotBeFound("representativeCompanyName.in=" + UPDATED_COMPANY_NAME);
    }

    @Test
    @Transactional
    public void getAllFspsByCompanyNameIsNullOrNotNull() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where representativeCompanyName is not null
        defaultFspShouldBeFound("representativeCompanyName.specified=true");

        // Get all the fspList where representativeCompanyName is null
        defaultFspShouldNotBeFound("representativeCompanyName.specified=false");
    }

    @Test
    @Transactional
    public void getAllFspsByCompanyNameContainsSomething() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where representativeCompanyName contains DEFAULT_COMPANY_NAME
        defaultFspShouldBeFound("representativeCompanyName.contains=" + DEFAULT_COMPANY_NAME);

        // Get all the fspList where representativeCompanyName contains UPDATED_COMPANY_NAME
        defaultFspShouldNotBeFound("representativeCompanyName.contains=" + UPDATED_COMPANY_NAME);
    }

    @Test
    @Transactional
    public void getAllFspsByCompanyNameNotContainsSomething() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where representativeCompanyName does not contain DEFAULT_COMPANY_NAME
        defaultFspShouldNotBeFound("representativeCompanyName.doesNotContain=" + DEFAULT_COMPANY_NAME);

        // Get all the fspList where representativeCompanyName does not contain UPDATED_COMPANY_NAME
        defaultFspShouldBeFound("representativeCompanyName.doesNotContain=" + UPDATED_COMPANY_NAME);
    }


    @Test
    @Transactional
    public void getAllFspsByValidFromIsEqualToSomething() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where validFrom equals to DEFAULT_VALID_FROM
        defaultFspShouldBeFound("validFrom.equals=" + DEFAULT_VALID_FROM);

        // Get all the fspList where validFrom equals to UPDATED_VALID_FROM
        defaultFspShouldNotBeFound("validFrom.equals=" + UPDATED_VALID_FROM);
    }

    @Test
    @Transactional
    public void getAllFspsByValidFromIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where validFrom not equals to DEFAULT_VALID_FROM
        defaultFspShouldNotBeFound("validFrom.notEquals=" + DEFAULT_VALID_FROM);

        // Get all the fspList where validFrom not equals to UPDATED_VALID_FROM
        defaultFspShouldBeFound("validFrom.notEquals=" + UPDATED_VALID_FROM);
    }

    @Test
    @Transactional
    public void getAllFspsByValidFromIsInShouldWork() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where validFrom in DEFAULT_VALID_FROM or UPDATED_VALID_FROM
        defaultFspShouldBeFound("validFrom.in=" + DEFAULT_VALID_FROM + "," + UPDATED_VALID_FROM);

        // Get all the fspList where validFrom equals to UPDATED_VALID_FROM
        defaultFspShouldNotBeFound("validFrom.in=" + UPDATED_VALID_FROM);
    }

    @Test
    @Transactional
    public void getAllFspsByValidFromIsNullOrNotNull() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where validFrom is not null
        defaultFspShouldBeFound("validFrom.specified=true");

        // Get all the fspList where validFrom is null
        defaultFspShouldNotBeFound("validFrom.specified=false");
    }

    @Test
    @Transactional
    public void getAllFspsByValidToIsEqualToSomething() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where validTo equals to DEFAULT_VALID_TO
        defaultFspShouldBeFound("validTo.equals=" + DEFAULT_VALID_TO);

        // Get all the fspList where validTo equals to UPDATED_VALID_TO
        defaultFspShouldNotBeFound("validTo.equals=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllFspsByValidToIsNotEqualToSomething() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where validTo not equals to DEFAULT_VALID_TO
        defaultFspShouldNotBeFound("validTo.notEquals=" + DEFAULT_VALID_TO);

        // Get all the fspList where validTo not equals to UPDATED_VALID_TO
        defaultFspShouldBeFound("validTo.notEquals=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllFspsByValidToIsInShouldWork() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where validTo in DEFAULT_VALID_TO or UPDATED_VALID_TO
        defaultFspShouldBeFound("validTo.in=" + DEFAULT_VALID_TO + "," + UPDATED_VALID_TO);

        // Get all the fspList where validTo equals to UPDATED_VALID_TO
        defaultFspShouldNotBeFound("validTo.in=" + UPDATED_VALID_TO);
    }

    @Test
    @Transactional
    public void getAllFspsByValidToIsNullOrNotNull() throws Exception {
        // Initialize the database
        fspRepository.saveAndFlush(fspEntity);

        // Get all the fspList where validTo is not null
        defaultFspShouldBeFound("validTo.specified=true");

        // Get all the fspList where validTo is null
        defaultFspShouldNotBeFound("validTo.specified=false");
    }

    @Test
    @Transactional
    public void getAllFspsByOwnerIsEqualToSomething() throws Exception {
        // Get already existing entity
        UserEntity owner = fspEntity.getOwner();
        fspRepository.saveAndFlush(fspEntity);
        Long ownerId = owner.getId();

        // Get all the fspList where owner equals to ownerId
        defaultFspShouldBeFound("ownerId.equals=" + ownerId);

        // Get all the fspList where owner equals to ownerId + 1
        defaultFspShouldNotBeFound("ownerId.equals=" + (ownerId + 1));
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultFspShouldBeFound(String filter) throws Exception {
        restFspMockMvc.perform(get("/api/fsps?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(fspEntity.getId().intValue())))
            .andExpect(jsonPath("$.[*].companyName").value(hasItem(DEFAULT_COMPANY_NAME)))
            .andExpect(jsonPath("$.[*].validFrom").value(hasItem(DEFAULT_VALID_FROM.toString())))
            .andExpect(jsonPath("$.[*].validTo").value(hasItem(DEFAULT_VALID_TO.toString())));

        // Check, that the count call also returns 1
        restFspMockMvc.perform(get("/api/fsps/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultFspShouldNotBeFound(String filter) throws Exception {
        restFspMockMvc.perform(get("/api/fsps?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restFspMockMvc.perform(get("/api/fsps/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    public void getNonExistingFsp() throws Exception {
        // Get the fsp
        restFspMockMvc.perform(get("/api/fsps/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    private void mockedCurrentLoggedUserDto(String login, Role role) {
        UserDTO fspDtoUser = new UserDTO();
        fspDtoUser.setId(1000L);
        fspDtoUser.setLogin(login);
        fspDtoUser.setRoles(Collections.singleton(role));
        Mockito.when(mockUserService.getCurrentUserDTO()).thenReturn(Optional.of(fspDtoUser));
        MinimalDTO<Long, String> minimalDTO = new MinimalDTO<>(fspDtoUser.getId(), fspDtoUser.getLogin());
        Mockito.when(mockUserService.getUsersByLogin(any())).thenReturn(Collections.singletonList(minimalDTO));
    }

    private void mockedCurrentLoggedUserEntity(String login, Role role) {
        UserEntity user = new UserEntity();
        user.setId(1000L);
        user.setLogin(login);
        user.setRoles(Collections.singleton(role));
        user.setCreationSource(CreationSource.SYSTEM);
        Mockito.when(mockUserService.findOneByLogin(any())).thenReturn(Optional.of(user));
    }

    private void findFspOwnerMock() {
        UserEntity owner = fspEntity.getOwner();
        Mockito.when(mockUserService.findOne(any())).thenReturn(Optional.of(owner));
    }
}
