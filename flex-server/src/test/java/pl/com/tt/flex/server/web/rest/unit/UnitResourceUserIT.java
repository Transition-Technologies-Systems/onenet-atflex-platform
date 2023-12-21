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
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.unit.DerTypeEntity;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_FLEX_SERVICE_PROVIDER;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = {FLEX_USER_UNIT_VIEW, FLEX_USER_UNIT_MANAGE, FLEX_USER_UNIT_DELETE})
public class UnitResourceUserIT extends UnitResourceIT {

    private final UnitRepository unitRepository;
    private final MockMvc restUnitMockMvc;
    private final UnitMapper unitMapper;
    private final EntityManager em;
    private final static String requestUri = "/api/user/units";

    @Autowired
    public UnitResourceUserIT(UnitRepository unitRepository, UnitMapper unitMapper,
                              EntityManager em, MockMvc restUnitMockMvc) {
        super(unitRepository, unitMapper, em, restUnitMockMvc, requestUri);
        this.unitRepository = unitRepository;
        this.unitMapper = unitMapper;
        this.restUnitMockMvc = restUnitMockMvc;
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
    public void getUnit_shouldThrowUnauthorizedBecauseUnitNotBelongsToUser() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);
        // Mocked user who does not belong to the unit
        mockedCurrentLoggedUserDto(FSP_ID + 1, "fsp_user", ROLE_FLEX_SERVICE_PROVIDER);
        // Get the unit
        restUnitMockMvc.perform(get(requestUri + "/{id}", unitEntity.getId()))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void updateUnit_shouldNotUpdateBecauseUnitNotBelongsToUser() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Mocked user who does not belong to the update entity
        mockedCurrentLoggedUserDto(FSP_ID + 1, "fsp_user", ROLE_FLEX_SERVICE_PROVIDER);

        // Update the unit
        UnitEntity updatedUnitEntity = unitRepository.findById(unitEntity.getId()).get();
        // Disconnect from session so that the updates on updatedUnitEntity are not directly saved in db
        UnitDTO unitDTO = unitMapper.toDto(updatedUnitEntity);

        restUnitMockMvc.perform(put(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    public void updateUnit_shouldNotUpdateBecauseUserHasNoAuthorityToModifyCertification() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Update the unit
        UnitEntity updatedUnitEntity = unitRepository.findById(unitEntity.getId()).get();
        // Disconnect from session so that the updates on updatedUnitEntity are not directly saved in db
        UnitDTO unitDTO = unitMapper.toDto(updatedUnitEntity);
        unitDTO.setCertified(true);

        restUnitMockMvc.perform(put(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(jsonPath("$.errorKey").value(USER_HAS_NO_AUTHORITY_TO_MODIFY_UNIT_CERTIFIED))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void updateUnit_shouldNotUpdateBecauseUserHasNoAuthorityToModifyFsp() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        // Update the unit
        UnitEntity updatedUnitEntity = unitRepository.findById(unitEntity.getId()).get();
        // Disconnect from session so that the updates on updatedUnitEntity are not directly saved in db
        UnitDTO unitDTO = unitMapper.toDto(updatedUnitEntity);
        unitDTO.setFspId(unitDTO.getFspId() + 1);

        restUnitMockMvc.perform(put(requestUri)
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(unitDTO)))
            .andExpect(jsonPath("$.errorKey").value(CANNOT_MODIFY_UNIT_FSP_BY_FSP_USER))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    public void deleteUnit_shouldNotDeletedBecauseUserHasNoAuthorityToDelete() throws Exception {
        // Initialize the database
        unitEntity = unitRepository.saveAndFlush(unitEntity);

        int databaseSizeBeforeDelete = unitRepository.findAll().size();

        // Current logged user has no authority to delete unit.
        mockedCurrentLoggedUserEntity("dso-user", Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR);

        // Delete the unit
        restUnitMockMvc.perform(delete(requestUri + "/{id}", unitEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<UnitEntity> unitList = unitRepository.findAll();
        assertThat(unitList).hasSize(databaseSizeBeforeDelete);
    }

    @Override
    void mockedCurrentLoggedUser() {
        mockedCurrentLoggedUserDto(FSP_ID, "fsp_user", ROLE_FLEX_SERVICE_PROVIDER);
        mockedCurrentLoggedUserEntity("fsp_user", ROLE_FLEX_SERVICE_PROVIDER);

    }

    private void mockedCurrentLoggedUserEntity(String login, Role role) {
        UserEntity fspUser = new UserEntity();
        fspUser.setLogin(login);
        fspUser.setRoles(Collections.singleton(role));
        fspUser.setCreationSource(CreationSource.SYSTEM);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(fspUser);
    }

    private void mockedCurrentLoggedUserDto(Long fspId, String login, Role role) {
        UserDTO fspDtoUser = new UserDTO();
        fspDtoUser.setFspId(fspId);
        fspDtoUser.setLogin(login);
        fspDtoUser.setRoles(Collections.singleton(role));
        Mockito.when(mockUserService.getCurrentUserDTO()).thenReturn(Optional.of(fspDtoUser));
    }
}
