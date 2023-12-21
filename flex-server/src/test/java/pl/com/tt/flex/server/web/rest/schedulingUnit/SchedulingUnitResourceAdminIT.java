package pl.com.tt.flex.server.web.rest.schedulingUnit;

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
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitRepository;
import pl.com.tt.flex.server.service.dictionary.localizationType.mapper.LocalizationTypeMapper;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.user.dto.UserDTO;

import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.Set;

import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_ADMIN;

@SpringBootTest(classes = FlexserverApp.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = {FLEX_ADMIN_SCHEDULING_UNIT_VIEW, FLEX_ADMIN_SCHEDULING_UNIT_MANAGE, FLEX_ADMIN_SCHEDULING_UNIT_DELETE})
public class SchedulingUnitResourceAdminIT extends SchedulingUnitResourceIT {

    private final SchedulingUnitRepository schedulingUnitRepository;
    private final SchedulingUnitMapper schedulingUnitMapper;
    private final MockMvc restSchedulingUnitMockMvc;
    private final EntityManager em;
    private final static String requestUri = "/api/admin/scheduling-units";

    @Autowired
    public SchedulingUnitResourceAdminIT(SchedulingUnitRepository schedulingUnitRepository, SchedulingUnitMapper schedulingUnitMapper,
                                         EntityManager em, MockMvc restSchedulingUnitMockMvc, LocalizationTypeMapper localizationTypeMapper) {
        super(schedulingUnitRepository, schedulingUnitMapper, localizationTypeMapper, em, restSchedulingUnitMockMvc, requestUri);
        this.schedulingUnitMapper = schedulingUnitMapper;
        this.schedulingUnitRepository = schedulingUnitRepository;
        this.restSchedulingUnitMockMvc = restSchedulingUnitMockMvc;
        this.em = em;
    }

    @Override
    protected void mockedCurrentLoggedUser() {
        mockedCurrentLoggedUser(101L, "admin", Set.of(ROLE_ADMIN));
    }

    private void mockedCurrentLoggedUser(long id, String login, Set<Role> roles) {
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
    }

    private void mockedCurrentLoggedUserDto(Long id, String login, Set<Role> roles) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setLogin(login);
        user.setRoles(roles);
        Mockito.when(mockUserService.getCurrentUserDTO()).thenReturn(Optional.of(user));
    }
}
