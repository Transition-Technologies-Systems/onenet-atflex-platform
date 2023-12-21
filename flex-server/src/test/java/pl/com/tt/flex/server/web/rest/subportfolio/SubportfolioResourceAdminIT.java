package pl.com.tt.flex.server.web.rest.subportfolio;

import org.apache.commons.lang.RandomStringUtils;
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
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.enumeration.CreationSource;
import pl.com.tt.flex.server.repository.subportfolio.SubportfolioRepository;
import pl.com.tt.flex.server.repository.unit.UnitRepository;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;
import pl.com.tt.flex.server.service.subportfolio.mapper.SubportfolioMapper;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.user.UserResourceIT;

import javax.persistence.EntityManager;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.model.security.permission.Role.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = {FLEX_ADMIN_SUBPORTFOLIO_VIEW, FLEX_ADMIN_SUBPORTFOLIO_MANAGE, FLEX_ADMIN_SUBPORTFOLIO_DELETE})
public class SubportfolioResourceAdminIT extends SubportfolioResourceIT {

    private final SubportfolioRepository subportfolioRepository;
    private final UnitRepository unitRepository;
    private final SubportfolioMapper subportfolioMapper;
    private final MockMvc restSubportfolioMockMvc;
    private final EntityManager em;
    private static final String requestUri = "/api/admin/subportfolio";
    private UserEntity userEntity;

    @Autowired
    public SubportfolioResourceAdminIT(SubportfolioRepository subportfolioRepository, SubportfolioMapper subportfolioMapper,
                                       EntityManager em, UnitRepository unitRepository, MockMvc restSubportfolioMockMvc) {
        super(subportfolioRepository, subportfolioMapper, em, restSubportfolioMockMvc, requestUri);
        this.subportfolioRepository = subportfolioRepository;
        this.unitRepository = unitRepository;
        this.restSubportfolioMockMvc = restSubportfolioMockMvc;
        this.subportfolioMapper = subportfolioMapper;
        this.em = em;
    }

    @Test
    @Transactional
    public void createSubportfolio_shouldNotCreateBecauseTsoNotHasPermission() throws Exception {
        int databaseSizeBeforeCreate = subportfolioRepository.findAll().size();

        mockedCurrentLoggedUser("tso", Set.of(ROLE_TRANSMISSION_SYSTEM_OPERATOR));

        SubportfolioDTO subportfolioDTO = subportfolioMapper.toDto(subportfolioEntity);
        MockMultipartFile multipartProduct = new MockMultipartFile("subportfolioDTO", "subportfolioDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(subportfolioDTO));

        restSubportfolioMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/create")
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(SUBPORTFOLIO_CANNOT_BE_CREATED_BY_TSO_AND_DSO))
            .andExpect(status().isBadRequest());

        // Validate the database contains the same items
        List<SubportfolioEntity> subportfolioEntityList = subportfolioRepository.findAll();
        assertThat(subportfolioEntityList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void createSubportfolio_shouldNotCreateBecauseDsoNotHasPermission() throws Exception {
        int databaseSizeBeforeCreate = subportfolioRepository.findAll().size();

        mockedCurrentLoggedUser("dso", Set.of(ROLE_DISTRIBUTION_SYSTEM_OPERATOR));

        SubportfolioDTO subportfolioDTO = subportfolioMapper.toDto(subportfolioEntity);
        MockMultipartFile multipartProduct = new MockMultipartFile("subportfolioDTO", "subportfolioDTO", MediaType.APPLICATION_JSON_VALUE, TestUtil.convertObjectToJsonBytes(subportfolioDTO));

        restSubportfolioMockMvc.perform(MockMvcRequestBuilders.multipart(requestUri + "/create")
            .file(multipartProduct))
            .andExpect(jsonPath("$.errorKey").value(SUBPORTFOLIO_CANNOT_BE_CREATED_BY_TSO_AND_DSO))
            .andExpect(status().isBadRequest());

        // Validate the database contains the same items
        List<SubportfolioEntity> subportfolioEntityList = subportfolioRepository.findAll();
        assertThat(subportfolioEntityList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void deleteSubportfolio_shouldNotDeleteDSOHasNoPermission() throws Exception {
        // Initialize the database
        // Nie mozna usuwac Subportfolio ktore jest aktywne i ktore ma podpiete DERy
        subportfolioEntity.setActive(false);
        subportfolioEntity.setUnits(Collections.singleton(unitEntity));
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        // DSO nie moze usuwac Subportfolio
        mockedCurrentLoggedUser("dso", Collections.singleton(ROLE_DISTRIBUTION_SYSTEM_OPERATOR));
        int databaseSizeBeforeDelete = subportfolioRepository.findAll().size();

        // Delete the subportfolio
        restSubportfolioMockMvc.perform(delete(requestUri + "/{id}", subportfolioEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(SUBPORTFOLIO_CANNOT_BE_DELETED_BY_TSO_AND_DSO))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<SubportfolioEntity> flexPotentialList = subportfolioRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void deleteSubportfolio_shouldNotDeleteTSOHasNoPermission() throws Exception {
        // Initialize the database
        // Nie mozna usuwac Subportfolio ktore jest aktywne i ktore ma podpiete DERy
        subportfolioEntity.setActive(false);
        subportfolioEntity.setUnits(Collections.singleton(unitEntity));
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        // TSO nie moze usuwac Subportfolio
        mockedCurrentLoggedUser("tso", Collections.singleton(ROLE_TRANSMISSION_SYSTEM_OPERATOR));
        int databaseSizeBeforeDelete = subportfolioRepository.findAll().size();

        // Delete the subportfolio
        restSubportfolioMockMvc.perform(delete(requestUri + "/{id}", subportfolioEntity.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.errorKey").value(SUBPORTFOLIO_CANNOT_BE_DELETED_BY_TSO_AND_DSO))
            .andExpect(status().isBadRequest());

        // Validate the database contains one less item
        List<SubportfolioEntity> flexPotentialList = subportfolioRepository.findAll();
        assertThat(flexPotentialList).hasSize(databaseSizeBeforeDelete);
    }

    @Test
    @Transactional
    public void getAllSubportfolioByFspaIdFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        defaultSubportfolioShouldBeFound("fspaId.equals=" + fspaEntity.getId());
        defaultSubportfolioShouldNotBeFound("fspaId.equals=" + Long.MAX_VALUE);

        defaultSubportfolioShouldBeFound("fspaId.greaterThan=" + (fspaEntity.getId() - 1L));
        defaultSubportfolioShouldNotBeFound("fspaId.greaterThan=" + Long.MAX_VALUE);

        defaultSubportfolioShouldBeFound("fspaId.lessThan=" + (fspaEntity.getId() + 1L));
        defaultSubportfolioShouldNotBeFound("fspaId.lessThan=" + -1L);

        defaultSubportfolioShouldBeFound("fspaId.greaterThanOrEqual=" + fspaEntity.getId());
        defaultSubportfolioShouldNotBeFound("fspaId.greaterThanOrEqual=" + Long.MAX_VALUE);

        defaultSubportfolioShouldBeFound("fspaId.lessThanOrEqual=" + fspaEntity.getId());
        defaultSubportfolioShouldNotBeFound("fspaId.lessThanOrEqual=" + -1L);
    }

    @Test
    @Transactional
    public void getAllSubportfolioByFspaRepresentativeCompanyNameFiltering() throws Exception {
        // Initialize the database
        subportfolioRepository.saveAndFlush(subportfolioEntity);

        defaultSubportfolioShouldBeFound("fspaRepresentativeCompanyName.equals=" + fspaEntity.getCompanyName());
        defaultSubportfolioShouldNotBeFound("fspaRepresentativeCompanyName.equals=" + RandomStringUtils.random(10));

        defaultSubportfolioShouldBeFound("fspaRepresentativeCompanyName.notEquals=" + RandomStringUtils.random(10));
        defaultSubportfolioShouldNotBeFound("fspaRepresentativeCompanyName.notEquals=" + fspaEntity.getCompanyName());

        defaultSubportfolioShouldBeFound("fspaRepresentativeCompanyName.contains=" + fspaEntity.getCompanyName());
        defaultSubportfolioShouldNotBeFound("fspaRepresentativeCompanyName.contains=" + RandomStringUtils.random(10));

        defaultSubportfolioShouldBeFound("fspaRepresentativeCompanyName.doesNotContain=" + RandomStringUtils.random(10));
        defaultSubportfolioShouldNotBeFound("fspaRepresentativeCompanyName.doesNotContain=" + fspaEntity.getCompanyName());

        defaultSubportfolioShouldBeFound("fspaRepresentativeCompanyName.in=" + fspaEntity.getCompanyName() + "," + RandomStringUtils.random(10));
        defaultSubportfolioShouldNotBeFound("fspaRepresentativeCompanyName.in=" + RandomStringUtils.random(10));
    }

    @Override
    void mockedCurrentLoggedUser() {
        mockedCurrentLoggedUser("test", Collections.singleton(ROLE_ADMIN));
    }

    @Transactional
    void mockedCurrentLoggedUser(String login, Set<Role> roles) {
        userEntity =  UserResourceIT.createEntity(em);
        userEntity.setRoles(roles);
        userEntity.setLogin(login);
        mockedCurrentLoggedUserEntity(1000L, userEntity.getLogin(), userEntity.getRoles());
        mockedCurrentLoggedUserDto(1000L, userEntity.getLogin(), userEntity.getRoles());
    }

    private void mockedCurrentLoggedUserEntity(Long id, String login, Set<Role> roles) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setLogin(login);
        user.setRoles(roles);
        user.setCreationSource(CreationSource.SYSTEM);
        Mockito.when(mockUserService.getCurrentUser()).thenReturn(user);
        Mockito.when(mockUserService.findOneByLogin(any())).thenReturn(Optional.of(user));
    }

    private void mockedCurrentLoggedUserDto(Long id, String login, Set<Role> roles) {
        UserDTO user = new UserDTO();
        user.setFspId(id);
        user.setLogin(login);
        user.setRoles(roles);
        Mockito.when(mockUserService.getCurrentUserDTO()).thenReturn(Optional.of(user));
    }
}
