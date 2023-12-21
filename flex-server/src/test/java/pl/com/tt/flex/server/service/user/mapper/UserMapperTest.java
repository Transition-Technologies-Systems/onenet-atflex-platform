package pl.com.tt.flex.server.service.user.mapper;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.security.permission.factory.AuthoritiesContainerFactory;
import pl.com.tt.flex.server.service.user.dto.UserDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UserMapper}.
 */
@SpringBootTest(classes = FlexserverApp.class)
public class UserMapperTest {

    private static final String DEFAULT_LOGIN = "johndoe";
    private static final Long DEFAULT_ID = 1L;

    private UserMapper userMapper;
    private UserEntity user;
    private UserDTO userDto;

    @Autowired
    private AuthoritiesContainerFactory authoritiesContainerFactory;

    @BeforeEach
    public void init() {
        userMapper = new UserMapper(authoritiesContainerFactory);
        user = new UserEntity();
        user.setLogin(DEFAULT_LOGIN);
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail("johndoe@localhost");
        user.setFirstName("john");
        user.setLastName("doe");
        user.setLangKey("en");

        userDto = new UserDTO(user);
    }

    @Test
    public void usersToUserDTOsShouldMapOnlyNonNullUsers() {
        List<UserEntity> users = new ArrayList<>();
        users.add(user);
        users.add(null);

        List<UserDTO> userDTOS = userMapper.usersToUserDTOs(users);

        assertThat(userDTOS).isNotEmpty();
        assertThat(userDTOS).size().isEqualTo(1);
    }

    @Test
    public void userDTOsToUsersShouldMapOnlyNonNullUsers() {
        List<UserDTO> usersDto = new ArrayList<>();
        usersDto.add(userDto);
        usersDto.add(null);

        List<UserEntity> users = userMapper.userDTOsToUsers(usersDto);

        assertThat(users).isNotEmpty();
        assertThat(users).size().isEqualTo(1);
    }

    @Test
    public void userDTOsToUsersWithRolesShouldMapToUsersWithRolesDomain() {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_ADMIN);
        userDto.setRoles(roles);

        List<UserDTO> usersDto = new ArrayList<>();
        usersDto.add(userDto);

        List<UserEntity> users = userMapper.userDTOsToUsers(usersDto);

        assertThat(users).isNotEmpty();
        assertThat(users).size().isEqualTo(1);
        assertThat(users.get(0).getRoles()).isNotNull();
        assertThat(users.get(0).getRoles()).isNotEmpty();
        assertThat(users.get(0).getRoles().iterator().next()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    public void userDTOsToUsersMapWithNullRolesShouldReturnUserWithEmptyRoles() {
        userDto.setRoles(null);

        List<UserDTO> usersDto = new ArrayList<>();
        usersDto.add(userDto);

        List<UserEntity> users = userMapper.userDTOsToUsers(usersDto);

        assertThat(users).isNotEmpty();
        assertThat(users).size().isEqualTo(1);
        assertThat(users.get(0).getRoles()).isNotNull();
        assertThat(users.get(0).getRoles()).isEmpty();
    }

    @Test
    public void userDTOToUserMapWithRolesShouldReturnUserWithRoles() {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_ADMIN);
        userDto.setRoles(roles);

        UserEntity user = userMapper.userDTOToUser(userDto);

        assertThat(user).isNotNull();
        assertThat(user.getRoles()).isNotNull();
        assertThat(user.getRoles()).isNotEmpty();
        assertThat(user.getRoles().iterator().next()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    public void userDTOToUserMapWithNullRolesShouldReturnUserWithEmptyRoles() {
        userDto.setRoles(null);

        UserEntity user = userMapper.userDTOToUser(userDto);

        assertThat(user).isNotNull();
        assertThat(user.getRoles()).isNotNull();
        assertThat(user.getRoles()).isEmpty();
    }

    @Test
    public void userDTOToUserMapWithNullUserShouldReturnNull() {
        assertThat(userMapper.userDTOToUser(null)).isNull();
    }

    @Test
    public void testUserFromId() {
        assertThat(userMapper.userFromId(DEFAULT_ID).getId()).isEqualTo(DEFAULT_ID);
        assertThat(userMapper.userFromId(null)).isNull();
    }
}
