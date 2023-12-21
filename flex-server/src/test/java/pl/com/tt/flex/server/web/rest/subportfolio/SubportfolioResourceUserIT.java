package pl.com.tt.flex.server.web.rest.subportfolio;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.enumeration.CreationSource;
import pl.com.tt.flex.server.repository.subportfolio.SubportfolioRepository;
import pl.com.tt.flex.server.service.subportfolio.mapper.SubportfolioMapper;
import pl.com.tt.flex.server.service.user.dto.UserDTO;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED;

@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = {FLEX_USER_SUBPORTFOLIO_VIEW, FLEX_USER_SUBPORTFOLIO_MANAGE, FLEX_USER_SUBPORTFOLIO_DELETE})
public class SubportfolioResourceUserIT extends SubportfolioResourceIT {

    private static final String requestUri = "/api/user/subportfolio";

    @Autowired
    public SubportfolioResourceUserIT(SubportfolioRepository subportfolioRepository, SubportfolioMapper subportfolioMapper,
                                      EntityManager em, MockMvc restSubportfolioMockMvc) {
        super(subportfolioRepository, subportfolioMapper, em, restSubportfolioMockMvc, requestUri);
    }

    @Override
    void mockedCurrentLoggedUser() {
        mockedCurrentLoggedUser(fspaEntity.getId(), "fspa", Set.of(ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED));
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
    }

    private void mockedCurrentLoggedUserDto(Long id, String login, Set<Role> roles) {
        UserDTO user = new UserDTO();
        user.setFspId(id);
        user.setLogin(login);
        user.setRoles(roles);
        Mockito.when(mockUserService.getCurrentUserDTO()).thenReturn(Optional.of(user));
    }
}
