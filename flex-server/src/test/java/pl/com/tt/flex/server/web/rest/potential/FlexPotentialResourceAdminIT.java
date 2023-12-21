package pl.com.tt.flex.server.web.rest.potential;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.potential.FlexPotentialEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.enumeration.CreationSource;
import pl.com.tt.flex.server.repository.potential.FlexPotentialRepository;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.web.rest.TestUtil;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.model.security.permission.Role.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.FLEX_POTENTIAL_CANNOT_BE_DELETED_BY_TSO_AND_DSO;
import static pl.com.tt.flex.server.web.rest.potential.FlexPotentialResourceIT.DEFAULT_USERNAME;

@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(value = DEFAULT_USERNAME, authorities = {FLEX_ADMIN_FP_VIEW, FLEX_ADMIN_FP_MANAGE, FLEX_ADMIN_FP_DELETE})
public class FlexPotentialResourceAdminIT extends FlexPotentialResourceIT {

    private final FlexPotentialRepository flexPotentialRepository;
    private final MockMvc restFlexPotentialMockMvc;
    private final FlexPotentialMapper flexPotentialMapper;
    private final static String requestUri = "/api/admin/flex-potentials";

    @Autowired
    public FlexPotentialResourceAdminIT(FlexPotentialRepository flexPotentialRepository, FlexPotentialMapper flexPotentialMapper,
                                        EntityManager em, MockMvc restFlexPotentialMockMvc) {
        super(flexPotentialRepository, flexPotentialMapper, em, restFlexPotentialMockMvc, requestUri);
        this.flexPotentialRepository = flexPotentialRepository;
        this.restFlexPotentialMockMvc = restFlexPotentialMockMvc;
        this.flexPotentialMapper = flexPotentialMapper;
    }

    @Test
    @Transactional
    public void updateNonExistingFlexPotential() throws Exception {
        int databaseSizeBeforeUpdate = flexPotentialRepository.findAll().size();

        // Create the FlexPotential
        FlexPotentialDTO flexPotentialDTO = flexPotentialMapper.toDto(flexPotentialEntity);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        MockMultipartFile multipartProduct = new MockMultipartFile("flexPotentialDTO", "flexPotentialDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(flexPotentialDTO));
        restFlexPotentialMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/update")
            .file(multipartProduct))
            .andExpect(status().isBadRequest());

        // Validate the FlexPotential in the database
        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void deleteFlexPotential_shouldNotDeleteBecauseTsoNotHasPermission() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        int databaseSizeBeforeDelete = flexPotentialRepository.findAll().size();
        mockedCurrentLoggedUser(1L, "tso", Set.of(ROLE_TRANSMISSION_SYSTEM_OPERATOR));

        // Delete the flexPotential
        restFlexPotentialMockMvc.perform(delete(requestUri + "/{id}", flexPotentialEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(FLEX_POTENTIAL_CANNOT_BE_DELETED_BY_TSO_AND_DSO))
            .andExpect(status().isBadRequest());

        // Validate the database contains the same items
        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void deleteFlexPotential_shouldNotDeleteBecauseDsoNotHasPermission() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        int databaseSizeBeforeDelete = flexPotentialRepository.findAll().size();
        mockedCurrentLoggedUser(1L, "dso", Set.of(ROLE_DISTRIBUTION_SYSTEM_OPERATOR));

        // Delete the flexPotential
        restFlexPotentialMockMvc.perform(delete(requestUri + "/{id}", flexPotentialEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(FLEX_POTENTIAL_CANNOT_BE_DELETED_BY_TSO_AND_DSO))
            .andExpect(status().isBadRequest());

        // Validate the database contains the same items
        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void getNonExistingFlexPotential() throws Exception {
        // Get the flexPotential
        restFlexPotentialMockMvc.perform(get(requestUri + "/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByFspIdIsEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where fspId equals to DEFAULT_FSP_ID
        defaultFlexPotentialShouldBeFound("fspId.equals=" + fspEntity.getId());

        // Get all the flexPotentialList where fspId equals to UPDATED_FSP_ID
        defaultFlexPotentialShouldNotBeFound("fspId.equals=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByFspIdIsNotEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where fspId not equals to DEFAULT_FSP_ID
        defaultFlexPotentialShouldNotBeFound("fspId.notEquals=" + fspEntity.getId());

        // Get all the flexPotentialList where fspId not equals to UPDATED_FSP_ID
        defaultFlexPotentialShouldBeFound("fspId.notEquals=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByFspIdIsInShouldWork() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where fspId in DEFAULT_FSP_ID or UPDATED_FSP_ID
        defaultFlexPotentialShouldBeFound("fspId.in=" + fspEntity.getId() + "," + Long.MAX_VALUE);

        // Get all the flexPotentialList where fspId equals to UPDATED_FSP_ID
        defaultFlexPotentialShouldNotBeFound("fspId.in=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByFspIdIsNullOrNotNull() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where fspId is not null
        defaultFlexPotentialShouldBeFound("fspId.specified=true");

        // Get all the flexPotentialList where fspId is null
        defaultFlexPotentialShouldNotBeFound("fspId.specified=false");
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByFspIdIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where fspId is greater than or equal to DEFAULT_FSP_ID
        defaultFlexPotentialShouldBeFound("fspId.greaterThanOrEqual=" + fspEntity.getId());

        // Get all the flexPotentialList where fspId is greater than or equal to UPDATED_FSP_ID
        defaultFlexPotentialShouldNotBeFound("fspId.greaterThanOrEqual=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByFspIdIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where fspId is less than or equal to DEFAULT_FSP_ID
        defaultFlexPotentialShouldBeFound("fspId.lessThanOrEqual=" + fspEntity.getId());

        // Get all the flexPotentialList where fspId is less than or equal to SMALLER_FSP_ID
        defaultFlexPotentialShouldNotBeFound("fspId.lessThanOrEqual=" + (fspEntity.getId() - 1L));
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByFspIdIsLessThanSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where fspId is less than DEFAULT_FSP_ID
        defaultFlexPotentialShouldNotBeFound("fspId.lessThan=" + (fspEntity.getId() - 1L));

        // Get all the flexPotentialList where fspId is less than UPDATED_FSP_ID
        defaultFlexPotentialShouldBeFound("fspId.lessThan=" + Long.MAX_VALUE);
    }

    @Test
    @Transactional
    public void getAllFlexPotentialsByFspIdIsGreaterThanSomething() throws Exception {
        // Initialize the database
        flexPotentialRepository.saveAndFlush(flexPotentialEntity);

        // Get all the flexPotentialList where fspId is greater than DEFAULT_FSP_ID
        defaultFlexPotentialShouldNotBeFound("fspId.greaterThan=" + fspEntity.getId());

        // Get all the flexPotentialList where fspId is greater than SMALLER_FSP_ID
        defaultFlexPotentialShouldBeFound("fspId.greaterThan=" + (fspEntity.getId() - 1L));
    }


    @Override
    void mockedCurrentLoggedUser() {
        mockedCurrentLoggedUser(1L, "mo", Set.of(ROLE_MARKET_OPERATOR));
    }

    void mockedCurrentLoggedUser(long id, String login, Set<Role> roles) {
        mockedCurrentLoggedUserEntity(id, login, roles);
        mockedCurrentLoggedUserDto(id, login, roles);
    }

    private void mockedCurrentLoggedUserEntity(Long id, String login, Set<Role> roles) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setLogin(login);
        user.setRoles(roles);
        user.setCreationSource(CreationSource.SYSTEM);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(user);
        Mockito.when(mockUserService.findOneByLogin(any())).thenReturn(Optional.of(user));
        Mockito.when(mockUserService.getUsersByLogin(any())).thenReturn(singletonList(new MinimalDTO<>(user.getId(), user.getLogin())));
        Mockito.when(mockUserService.findOne(any())).thenReturn(Optional.of(user));
    }

    private void mockedCurrentLoggedUserDto(Long id, String login, Set<Role> roles) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setLogin(login);
        user.setRoles(roles);
        Mockito.when(mockUserService.getCurrentUserDTO()).thenReturn(Optional.of(user));
    }
}
