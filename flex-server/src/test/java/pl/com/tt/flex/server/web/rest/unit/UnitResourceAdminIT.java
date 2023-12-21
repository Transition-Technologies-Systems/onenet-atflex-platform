package pl.com.tt.flex.server.web.rest.unit;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.unit.enumeration.DerType;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.enumeration.CreationSource;
import pl.com.tt.flex.server.repository.unit.UnitRepository;
import pl.com.tt.flex.server.service.dictionary.derType.DerTypeService;
import pl.com.tt.flex.server.service.dictionary.derType.dto.DerTypeMinDTO;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitGeoLocationDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.web.rest.TestUtil;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_ADMIN;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = {FLEX_ADMIN_UNIT_VIEW, FLEX_ADMIN_UNIT_MANAGE, FLEX_ADMIN_UNIT_DELETE})
public class UnitResourceAdminIT extends UnitResourceIT {

    private final UnitRepository unitRepository;
    private final MockMvc restUnitMockMvc;
    private final UnitMapper unitMapper;
    private final EntityManager em;
    private final static String requestUri = "/api/admin/units";

    @Autowired
    public UnitResourceAdminIT(UnitRepository unitRepository, UnitMapper unitMapper,
                               EntityManager em, MockMvc restUnitMockMvc) {
        super(unitRepository, unitMapper, em, restUnitMockMvc, requestUri);
        this.unitRepository = unitRepository;
        this.restUnitMockMvc = restUnitMockMvc;
        this.unitMapper = unitMapper;
        this.em = em;
    }

    public static UnitDTO createUnitDto(EntityManager em) {
        FspDTO fspDTO = new FspDTO();
        fspDTO.setId(FSP_ID);

        DerTypeMinDTO derTypeEnergyStorage = new DerTypeMinDTO();
        derTypeEnergyStorage.setId(DER_TYPE_ENERGY_STORAGE_ID);

        DerTypeMinDTO derTypeReceptionDTO = new DerTypeMinDTO();
        derTypeReceptionDTO.setId(DER_TYPE_RECEPTION_ID);

        DerTypeMinDTO derTypeGenerationDTO = new DerTypeMinDTO();
        derTypeGenerationDTO.setId(DER_TYPE_GENERATION_ID);

        UnitDTO unitDTO = new UnitDTO();
        unitDTO.setFsp(fspDTO);
        unitDTO.setFspId(fspDTO.getId());
        unitDTO.setName(DEFAULT_NAME);
        unitDTO.setActive(DEFAULT_ACTIVE);
        unitDTO.setCertified(DEFAULT_CERTIFIED);
        unitDTO.setValidFrom(DEFAULT_VALID_FROM);
        unitDTO.setValidTo(DEFAULT_VALID_TO);
        unitDTO.setConnectionPower(DEFAULT_CONNECTION_POWER);
        unitDTO.setSourcePower(DEFAULT_SOURCE_POWER);
        UnitGeoLocationDTO unitGeoLocationDTO = new UnitGeoLocationDTO();
        unitGeoLocationDTO.setLongitude("20");
        unitGeoLocationDTO.setLatitude("20");
        unitDTO.setGeoLocations(singletonList(unitGeoLocationDTO));
        LocalizationTypeEntity couplingPointId = getCouplingPointId(em);
        LocalizationTypeDTO couplingPointDTO = getLocalizationTypeDTO(couplingPointId);
        unitDTO.setCouplingPointIdTypes(singletonList(couplingPointDTO));
        LocalizationTypeEntity powerStation = getPowerStation(em);
        LocalizationTypeDTO powerStationDTO = getLocalizationTypeDTO(powerStation);
        LocalizationTypeEntity pointOfConnectionWithLv = getPointOfConnectionWithLv(em);
        LocalizationTypeDTO pointOfConnectionWithLvDTO = getLocalizationTypeDTO(pointOfConnectionWithLv);
        unitDTO.setPowerStationTypes(singletonList(powerStationDTO));
        unitDTO.setPointOfConnectionWithLvTypes(singletonList(pointOfConnectionWithLvDTO));
        unitDTO.setDirectionOfDeviation(DEFAULT_DIRECTION_OF_DEVIATION);
        unitDTO.setDerTypeEnergyStorage(derTypeEnergyStorage);
        unitDTO.setDerTypeGeneration(derTypeGenerationDTO);
        unitDTO.setDerTypeReception(derTypeReceptionDTO);
        unitDTO.setPpe(DEFAULT_PPE);
        unitDTO.setPMin(DEFAULT_P_MIN);
        unitDTO.setQMin(DEFAULT_Q_MIN);
        unitDTO.setQMax(DEFAULT_Q_MAX);
        return unitDTO;
    }

    @Test
    @Transactional
    public void createUnit() throws Exception {
        int databaseSizeBeforeCreate = unitRepository.findAll().size();
        // Create the Unit
        UnitDTO unitDTO = createUnitDto(em);
        restUnitMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(status().isCreated());

        // Validate the Unit in the database
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeCreate + 1);
        UnitEntity testUnit = unitList.get(unitList.size() - 1);
        assertThat(testUnit.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testUnit.isAggregated()).isEqualTo(DEFAULT_AGGREGATED);
        assertThat(testUnit.getValidFrom()).isEqualTo(DEFAULT_VALID_FROM);
        assertThat(testUnit.getValidTo()).isEqualTo(DEFAULT_VALID_TO);
        assertThat(testUnit.isActive()).isEqualTo(DEFAULT_ACTIVE);
        assertThat(testUnit.isCertified()).isEqualTo(DEFAULT_CERTIFIED);
        assertThat(testUnit.getVersion()).isEqualTo(DEFAULT_VERSION);
    }

    @Test
    @Transactional
    public void getUnit() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get the unit
        restUnitMockMvc.perform(get(requestUri + "/{id}", unitEntity.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(unitEntity.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.code").value(DEFAULT_CODE))
            .andExpect(jsonPath("$.aggregated").value(DEFAULT_AGGREGATED.booleanValue()))
            .andExpect(jsonPath("$.fspId").value(unitEntity.getFsp().getId().intValue()))
            .andExpect(jsonPath("$.validFrom").value(DEFAULT_VALID_FROM.toString()))
            .andExpect(jsonPath("$.validTo").value(DEFAULT_VALID_TO.toString()))
            .andExpect(jsonPath("$.active").value(DEFAULT_ACTIVE.booleanValue()))
            .andExpect(jsonPath("$.certified").value(DEFAULT_CERTIFIED.booleanValue()))
            .andExpect(jsonPath("$.version").value(unitEntity.getVersion().intValue()));
    }

    @Test
    @Transactional
    public void getAllUnitsByFspNotContainsSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where fsp does not contain DEFAULT_FSP
        defaultUnitShouldNotBeFound("fspId.notEquals=" + unitEntity.getFsp().getId());

        // Get all the unitList where fsp does not contain UPDATED_FSP
        defaultUnitShouldBeFound("fspId.notEquals=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllUnitsByFspIsInShouldWork() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where fsp in DEFAULT_FSP or UPDATED_FSP
        defaultUnitShouldBeFound("fspId.in=" + FSP_ID + "," + UPDATED_FSP_ID);

        // Get all the unitList where fsp equals to UPDATED_FSP
        defaultUnitShouldNotBeFound("fspId.in=" + UPDATED_FSP_ID);
    }

    @Test
    @Transactional
    public void getAllUnitsByFspIsNotEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where fsp not equals to DEFAULT_FSP
        defaultUnitShouldNotBeFound("fspId.notEquals=" + FSP_ID);

        // Get all the unitList where fsp not equals to UPDATED_FSP
        defaultUnitShouldBeFound("fspId.notEquals=" + UPDATED_FSP_ID);
    }

    @Test
    @Transactional
    public void getAllUnitsByFspIsEqualToSomething() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where fsp equals to DEFAULT_FSP
        defaultUnitShouldBeFound("fspId.equals=" + unitEntity.getFsp().getId());

        // Get all the unitList where fsp equals to UPDATED_FSP
        defaultUnitShouldNotBeFound("fspId.equals=" + UPDATED_FSP_ID);
    }

    @Test
    @Transactional
    public void getAllUnitsByFspIsNullOrNotNull() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Get all the unitList where fsp is not null
        defaultUnitShouldBeFound("fspId.specified=true");

        // Get all the unitList where fsp is null
        defaultUnitShouldNotBeFound("fspId.specified=false");
    }

    @Test
    @Transactional
    public void getNonExistingUnit() throws Exception {
        // Get the unit
        restUnitMockMvc.perform(get(requestUri + "/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateNonExistingUnit() throws Exception {
        int databaseSizeBeforeUpdate = unitRepository.findAll().size();

        // Create the Unit
        UnitDTO unitDTO = unitMapper.toDto(unitEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restUnitMockMvc.perform(put(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Unit in the database
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void updateUnit_shouldNotDeactivateBecauseSubportfolioHasJoinedUnit() throws Exception {
        // Initialize the database
        unitEntity.setCertified(true);
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Update the unit
        UnitEntity updatedUnitEntity = unitRepository.findById(unitEntity.getId()).get();
        // Disconnect from session so that the updates on updatedUnitEntity are not directly saved in db
        UnitDTO unitDTO = unitMapper.toDto(updatedUnitEntity);
        unitDTO.setCertified(false);

        // Returned active FlexPotential related to updated Unit.
        Mockito.when(mockSubportfolioService.findByUnit(any())).thenReturn(Collections.singletonList(1L));

        restUnitMockMvc.perform(put(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(jsonPath("$.errorKey").value(CANNOT_REMOVE_CERTIFICATION_BECAUSE_OF_JOINED_SUBPORTFOLIOS))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void createUnit_shouldNotCreateBecauseDerTypeIsNotSelected() throws Exception {
        int databaseSizeBeforeCreate = unitRepository.findAll().size();
        // Create the Unit
        UnitDTO unitDTO = createUnitDto(em);
        unitDTO.setDerTypeReception(null);
        unitDTO.setDerTypeEnergyStorage(null);
        unitDTO.setDerTypeGeneration(null);

        restUnitMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(jsonPath("$.errorKey").value(UNIT_AT_LEAST_ONE_DER_TYPE_MUST_BE_SELECTED_FOR_DER))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUnit_shouldNotCreateBecauseDerTypeReceptionIsInvalid() throws Exception {
        int databaseSizeBeforeCreate = unitRepository.findAll().size();

        // Unit with invalid DerTypeReception
        UnitDTO unitDTO = createUnitDto(em);
        DerTypeEntity derTypeEnergyStorage = createDerTypeEnergyStorage(em);
        unitDTO.setDerTypeReception(new DerTypeMinDTO(derTypeEnergyStorage.getId(), derTypeEnergyStorage.getType(), derTypeEnergyStorage.getDescriptionEn()));

        DerTypeService mock = Mockito.mock(DerTypeService.class);
        Mockito.when(mock.existsByIdAndType(unitDTO.getDerTypeReception().getId(), DerType.RECEPTION)).thenReturn(false);

        restUnitMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(jsonPath("$.errorKey").value(UNIT_DER_TYPE_RECEPTION_IS_NOT_OF_RECEPTION_TYPE))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUnit_shouldNotCreateBecauseDerTypeGenerationIsInvalid() throws Exception {
        int databaseSizeBeforeCreate = unitRepository.findAll().size();

        // Unit with invalid DerTypeGeneration
        UnitDTO unitDTO = createUnitDto(em);
        DerTypeEntity derTypeEnergyStorage = createDerTypeEnergyStorage(em);
        unitDTO.setDerTypeGeneration(new DerTypeMinDTO(derTypeEnergyStorage.getId(), derTypeEnergyStorage.getType(), derTypeEnergyStorage.getDescriptionEn()));

        DerTypeService mock = Mockito.mock(DerTypeService.class);
        Mockito.when(mock.existsByIdAndType(unitDTO.getDerTypeGeneration().getId(), DerType.GENERATION)).thenReturn(false);

        restUnitMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(jsonPath("$.errorKey").value(UNIT_DER_TYPE_GENERATION_IS_NOT_OF_RECEPTION_TYPE))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createUnit_shouldNotCreateBecauseDerTypeEnergyStorageIsInvalid() throws Exception {
        int databaseSizeBeforeCreate = unitRepository.findAll().size();

        // Unit with invalid DerTypeGeneration
        UnitDTO unitDTO = createUnitDto(em);
        DerTypeEntity derTypeGeneration = createDerTypeGeneration(em);
        unitDTO.setDerTypeEnergyStorage(new DerTypeMinDTO(derTypeGeneration.getId(), derTypeGeneration.getType(), derTypeGeneration.getDescriptionEn()));

        DerTypeService mock = Mockito.mock(DerTypeService.class);
        Mockito.when(mock.existsByIdAndType(unitDTO.getDerTypeEnergyStorage().getId(), DerType.ENERGY_STORAGE)).thenReturn(false);

        restUnitMockMvc.perform(post(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(jsonPath("$.errorKey").value(UNIT_DER_TYPE_ENERGY_STORAGE_IS_NOT_OF_RECEPTION_TYPE))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeCreate);
    }

    @Override
    void mockedCurrentLoggedUser() {
        UserEntity adminUser = new UserEntity();
        adminUser.setCreationSource(CreationSource.SYSTEM);
        adminUser.setLogin("admin_user");
        adminUser.setRoles(Collections.singleton(ROLE_ADMIN));

        UserDTO adminDtoUser = new UserDTO();
        adminDtoUser.setLogin("admin_user");
        adminDtoUser.setRoles(Collections.singleton(ROLE_ADMIN));

        Mockito.when(mockUserService.getCurrentUser()).thenReturn(adminUser);
        Mockito.when(mockUserService.getCurrentUserDTO()).thenReturn(Optional.of(adminDtoUser));
    }
}
