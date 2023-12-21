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
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialDTO;
import pl.com.tt.flex.server.service.potential.mapper.FlexPotentialMapper;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.web.rest.TestUtil;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.web.rest.potential.FlexPotentialResourceIT.DEFAULT_USERNAME;

@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(value = DEFAULT_USERNAME, authorities = {FLEX_USER_FP_VIEW, FLEX_USER_FP_MANAGE, FLEX_USER_FP_DELETE})
public class FlexPotentialResourceUserIT extends FlexPotentialResourceIT {

    private final FlexPotentialRepository flexPotentialRepository;
    private final MockMvc restFlexPotentialMockMvc;
    private final FlexPotentialMapper flexPotentialMapper;
    private final static String requestUri = "/api/user/flex-potentials";

    @Autowired
    public FlexPotentialResourceUserIT(FlexPotentialRepository flexPotentialRepository, FlexPotentialMapper flexPotentialMapper,
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
            .andExpect(status().is5xxServerError());

        // Validate the FlexPotential in the database
        List<FlexPotentialEntity> flexPotentialList = flexPotentialRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    public void getNonExistingFlexPotential() throws Exception {
        // Get the flexPotential
        restFlexPotentialMockMvc.perform(get(requestUri + "/{id}", Long.MAX_VALUE))
            .andExpect(status().is5xxServerError());
    }

    @Override
    void mockedCurrentLoggedUser() {
        mockedCurrentLoggedUser(1L, "fsp_user", Set.of(Role.ROLE_FLEX_SERVICE_PROVIDER));

    }

    protected void mockedCurrentLoggedUser(long id, String login, Set<Role> roles) {
        mockedCurrentLoggedUserDto(id, login, roles);
        mockedCurrentLoggedUserEntity(login, roles);

    }

    private void mockedCurrentLoggedUserEntity(String login, Set<Role> roles) {
        UserEntity fspUser = new UserEntity();
        fspUser.setLogin(login);
        fspUser.setRoles(roles);
        fspUser.setCreationSource(CreationSource.SYSTEM);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(fspUser);
    }

    private void mockedCurrentLoggedUserDto(Long fspId, String login, Set<Role> roles) {
        UserDTO fspDtoUser = new UserDTO();
        fspDtoUser.setFspId(fspId);
        fspDtoUser.setLogin(login);
        fspDtoUser.setRoles(roles);
        Mockito.when(mockUserService.getCurrentUserDTO()).thenReturn(Optional.of(fspDtoUser));
    }
}
