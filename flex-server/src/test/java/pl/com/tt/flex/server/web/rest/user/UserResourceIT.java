package pl.com.tt.flex.server.web.rest.user;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.enumeration.CreationSource;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.service.user.mapper.UserMapper;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.vm.user.ManagedUserVM;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_CHANGE_EMAIL_FOR_EXISTING_USER;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_CHANGE_LOGIN_FOR_EXISTING_USER;

/**
 * Integration tests for the {@link UserResource} REST controller.
 */
@AutoConfigureMockMvc
@SpringBootTest(classes = FlexserverApp.class)
public class UserResourceIT {

    private static final String DEFAULT_LOGIN = "johndoe";
    private static final String UPDATED_LOGIN = "jhipster";

    private static final Long DEFAULT_ID = 1L;

    private static final String DEFAULT_PASSWORD = "passjohndoe";
    private static final String UPDATED_PASSWORD = "passjhipster";

    private static final String DEFAULT_EMAIL = "johndoe@localhost";
    private static final String UPDATED_EMAIL = "jhipster@localhost";

    private static final String DEFAULT_FIRSTNAME = "john";
    private static final String UPDATED_FIRSTNAME = "jhipsterFirstName";

    private static final String DEFAULT_LASTNAME = "doe";
    private static final String UPDATED_LASTNAME = "jhipsterLastName";

    private static final String DEFAULT_LANGKEY = "en";
    private static final String UPDATED_LANGKEY = "fr";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MockMvc restUserMockMvc;

    private UserEntity user;

    @BeforeEach
    public void setup() {
        cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).clear();
        cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE).clear();
    }

    /**
     * Create a User.
     * <p>
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which has a required relationship to the User entity.
     */
    public static UserEntity createEntity(EntityManager em) {
        UserEntity user = new UserEntity();
        user.setLogin(DEFAULT_LOGIN + RandomStringUtils.randomAlphabetic(5));
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setEmail(RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL);
        user.setFirstName(DEFAULT_FIRSTNAME);
        user.setLastName(DEFAULT_LASTNAME);
        user.setLangKey(DEFAULT_LANGKEY);
        user.setCreationSource(CreationSource.SYSTEM);
        return user;
    }

    @BeforeEach
    public void initTest() {
        user = createEntity(em);
        user.setLogin(DEFAULT_LOGIN);
        user.setEmail(DEFAULT_EMAIL);
    }

    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_MANAGE)
    public void createUser() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        // Create the User
        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(DEFAULT_LOGIN);
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        restUserMockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isCreated());

        // Validate the User in the database
        assertPersistedUsers(users -> {
            assertThat(users).hasSize(databaseSizeBeforeCreate + 1);
            UserEntity testUser = users.get(users.size() - 1);
            assertThat(testUser.getLogin()).isEqualTo(DEFAULT_LOGIN);
            assertThat(testUser.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
            assertThat(testUser.getLastName()).isEqualTo(DEFAULT_LASTNAME);
            assertThat(testUser.getEmail()).isEqualTo(DEFAULT_EMAIL);
            assertThat(testUser.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        });
    }

    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_MANAGE)
    public void createUserWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(1L);
        managedUserVM.setLogin(DEFAULT_LOGIN);
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        // An entity with an existing ID cannot be created, so this API call must fail
        restUserMockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_MANAGE)
    public void createUserWithExistingLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin(DEFAULT_LOGIN);// this login should already be used
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail("anothermail@localhost");
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        // Create the User
        restUserMockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_MANAGE)
    public void createUserWithExistingEmail() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeCreate = userRepository.findAll().size();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setLogin("anotherlogin");
        managedUserVM.setPassword(DEFAULT_PASSWORD);
        managedUserVM.setFirstName(DEFAULT_FIRSTNAME);
        managedUserVM.setLastName(DEFAULT_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);// this email should already be used
        managedUserVM.setActivated(true);
        managedUserVM.setLangKey(DEFAULT_LANGKEY);
        managedUserVM.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        // Create the User
        restUserMockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());

        // Validate the User in the database
        assertPersistedUsers(users -> assertThat(users).hasSize(databaseSizeBeforeCreate));
    }

    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_VIEW)
    public void getAllUsers() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Get all the users
        restUserMockMvc.perform(get("/api/users?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].login").value(hasItem(DEFAULT_LOGIN)))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRSTNAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LASTNAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].langKey").value(hasItem(DEFAULT_LANGKEY)));
    }

    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_VIEW)
    void getAllUsersSortedByParameters() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

//        restUserMockMvc.perform(get("/api/users?sort=resetKey,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
//        restUserMockMvc.perform(get("/api/users?sort=password,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
//        restUserMockMvc.perform(get("/api/users?sort=resetKey,id,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isBadRequest());
        restUserMockMvc.perform(get("/api/users?sort=id,desc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

//    @Test
    @Transactional
    @WithMockUser(authorities = {FLEX_ADMIN_USER_VIEW, FLEX_USER_VIEW})
    public void getUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        assertThat(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).get(user.getLogin())).isNull();

        // Get the user
        restUserMockMvc.perform(get("/api/users/{login}", user.getLogin()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.login").value(user.getLogin()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRSTNAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LASTNAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.langKey").value(DEFAULT_LANGKEY));

        assertThat(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).get(user.getLogin())).isNotNull();
    }

    @Test
    @Transactional
    @WithMockUser(authorities = {FLEX_ADMIN_USER_VIEW, FLEX_USER_VIEW})
    public void getNonExistingUser() throws Exception {
        restUserMockMvc.perform(get("/api/users/unknown"))
            .andExpect(status().isNotFound());
    }

    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_MANAGE)
    public void updateUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        UserEntity updatedUser = userRepository.findById(user.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin(updatedUser.getLogin());
        managedUserVM.setPassword(UPDATED_PASSWORD);
        managedUserVM.setFirstName(UPDATED_FIRSTNAME);
        managedUserVM.setLastName(UPDATED_LASTNAME);
        managedUserVM.setEmail(UPDATED_EMAIL);
        managedUserVM.setActivated(updatedUser.isActivated());
        managedUserVM.setLangKey(UPDATED_LANGKEY);
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        restUserMockMvc.perform(put("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isOk());

        // Validate the User in the database
        assertPersistedUsers(users -> {
            assertThat(users).hasSize(databaseSizeBeforeUpdate);
            UserEntity testUser = users.get(users.size() - 1);
            assertThat(testUser.getFirstName()).isEqualTo(UPDATED_FIRSTNAME);
            assertThat(testUser.getLastName()).isEqualTo(UPDATED_LASTNAME);
            assertThat(testUser.getEmail()).isEqualTo(UPDATED_EMAIL);
            assertThat(testUser.getLangKey()).isEqualTo(UPDATED_LANGKEY);
        });
    }



    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_MANAGE)
    public void updateUserLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        UserEntity updatedUser = userRepository.findById(user.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin(UPDATED_LOGIN);
        managedUserVM.setPassword(UPDATED_PASSWORD);
        managedUserVM.setFirstName(UPDATED_FIRSTNAME);
        managedUserVM.setLastName(UPDATED_LASTNAME);
        managedUserVM.setEmail(DEFAULT_EMAIL);
        managedUserVM.setActivated(updatedUser.isActivated());
        managedUserVM.setLangKey(UPDATED_LANGKEY);
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        restUserMockMvc.perform(put("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("error." + CANNOT_CHANGE_LOGIN_FOR_EXISTING_USER));
    }

    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_MANAGE)
    public void updateUserEmail() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);
        int databaseSizeBeforeUpdate = userRepository.findAll().size();

        // Update the user
        UserEntity updatedUser = userRepository.findById(user.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin(DEFAULT_LOGIN);
        managedUserVM.setPassword(UPDATED_PASSWORD);
        managedUserVM.setFirstName(UPDATED_FIRSTNAME);
        managedUserVM.setLastName(UPDATED_LASTNAME);
        managedUserVM.setEmail(UPDATED_EMAIL);
        managedUserVM.setActivated(updatedUser.isActivated());
        managedUserVM.setLangKey(UPDATED_LANGKEY);
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        restUserMockMvc.perform(put("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error." + CANNOT_CHANGE_EMAIL_FOR_EXISTING_USER));
    }

    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_MANAGE)
    public void updateUserExistingEmail() throws Exception {
        // Initialize the database with 2 users
        userRepository.saveAndFlush(user);

        UserEntity anotherUser = new UserEntity();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setFirstName("java");
        anotherUser.setLastName("hipster");
        anotherUser.setLangKey("en");
        anotherUser.setCreationSource(CreationSource.SYSTEM);
        userRepository.saveAndFlush(anotherUser);

        // Update the user
        UserEntity updatedUser = userRepository.findById(user.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin(updatedUser.getLogin());
        managedUserVM.setPassword(updatedUser.getPassword());
        managedUserVM.setFirstName(updatedUser.getFirstName());
        managedUserVM.setLastName(updatedUser.getLastName());
        managedUserVM.setEmail("jhipster@localhost");// this email should already be used by anotherUser
        managedUserVM.setActivated(updatedUser.isActivated());
        managedUserVM.setLangKey(updatedUser.getLangKey());
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        restUserMockMvc.perform(put("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_MANAGE)
    public void updateUserExistingLogin() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        UserEntity anotherUser = new UserEntity();
        anotherUser.setLogin("jhipster");
        anotherUser.setPassword(RandomStringUtils.random(60));
        anotherUser.setActivated(true);
        anotherUser.setEmail("jhipster@localhost");
        anotherUser.setFirstName("java");
        anotherUser.setLastName("hipster");
        anotherUser.setLangKey("en");
        anotherUser.setCreationSource(CreationSource.SYSTEM);
        userRepository.saveAndFlush(anotherUser);

        // Update the user
        UserEntity updatedUser = userRepository.findById(user.getId()).get();

        ManagedUserVM managedUserVM = new ManagedUserVM();
        managedUserVM.setId(updatedUser.getId());
        managedUserVM.setLogin("jhipster");// this login should already be used by anotherUser
        managedUserVM.setPassword(updatedUser.getPassword());
        managedUserVM.setFirstName(updatedUser.getFirstName());
        managedUserVM.setLastName(updatedUser.getLastName());
        managedUserVM.setEmail(updatedUser.getEmail());
        managedUserVM.setActivated(updatedUser.isActivated());
        managedUserVM.setLangKey(updatedUser.getLangKey());
        managedUserVM.setCreatedBy(updatedUser.getCreatedBy());
        managedUserVM.setCreatedDate(updatedUser.getCreatedDate());
        managedUserVM.setLastModifiedBy(updatedUser.getLastModifiedBy());
        managedUserVM.setLastModifiedDate(updatedUser.getLastModifiedDate());
        managedUserVM.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        restUserMockMvc.perform(put("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(managedUserVM)))
            .andExpect(status().isBadRequest());
    }

//    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_DELETE)
    public void deleteUser() throws Exception {
        // Initialize the database
        userRepository.saveAndFlush(user);

        // Delete the user
        restUserMockMvc.perform(delete("/api/users/{id}", user.getId())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        assertThat(cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).get(user.getLogin())).isNull();

        // Validate the database is empty
        assertPersistedUsers(users -> assertThat(users.stream().filter(u -> u.getId().equals(user.getId())).findFirst().get().isDeleted()).isTrue());
    }

    @Test
    @Transactional
    @WithMockUser(authorities = FLEX_ADMIN_USER_VIEW)
    public void getAllRoles() throws Exception {
        restUserMockMvc.perform(get("/api/users/roles")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").value(Matchers.hasItems(Role.ROLE_ADMIN.name(), Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR.name(), Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR.name(),
                Role.ROLE_MARKET_OPERATOR.name(), Role.ROLE_FLEX_SERVICE_PROVIDER.name(), Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED.name(), Role.ROLE_ANONYMOUS.name())));
    }

    @Test
    public void testUserEquals() throws Exception {
        TestUtil.equalsVerifier(UserEntity.class);
        UserEntity user1 = new UserEntity();
        user1.setId(1L);
        UserEntity user2 = new UserEntity();
        user2.setId(user1.getId());
        assertThat(user1).isEqualTo(user2);
        user2.setId(2L);
        assertThat(user1).isNotEqualTo(user2);
        user1.setId(null);
        assertThat(user1).isNotEqualTo(user2);
    }

    @Test
    public void testUserDTOtoUser() {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(DEFAULT_ID);
        userDTO.setLogin(DEFAULT_LOGIN);
        userDTO.setFirstName(DEFAULT_FIRSTNAME);
        userDTO.setLastName(DEFAULT_LASTNAME);
        userDTO.setEmail(DEFAULT_EMAIL);
        userDTO.setActivated(true);
        userDTO.setLangKey(DEFAULT_LANGKEY);
        userDTO.setCreatedBy(DEFAULT_LOGIN);
        userDTO.setLastModifiedBy(DEFAULT_LOGIN);
        userDTO.setRoles(Collections.singleton(Role.ROLE_ADMIN));

        UserEntity user = userMapper.userDTOToUser(userDTO);
        assertThat(user.getId()).isEqualTo(DEFAULT_ID);
        assertThat(user.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(user.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(user.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(user.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(user.isActivated()).isEqualTo(true);
        assertThat(user.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(user.getCreatedBy()).isNull();
        assertThat(user.getCreatedDate()).isNotNull();
        assertThat(user.getLastModifiedBy()).isNull();
        assertThat(user.getLastModifiedDate()).isNotNull();
        assertThat(user.getRoles()).extracting("name").containsExactly(Role.ROLE_ADMIN.name());
    }

    @Test
    public void testUserToUserDTO() {
        user.setId(DEFAULT_ID);
        user.setCreatedBy(DEFAULT_LOGIN);
        user.setCreatedDate(Instant.now());
        user.setLastModifiedBy(DEFAULT_LOGIN);
        user.setLastModifiedDate(Instant.now());
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_ADMIN);
        user.setRoles(roles);

        UserDTO userDTO = userMapper.userToUserDTO(user);

        assertThat(userDTO.getId()).isEqualTo(DEFAULT_ID);
        assertThat(userDTO.getLogin()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getFirstName()).isEqualTo(DEFAULT_FIRSTNAME);
        assertThat(userDTO.getLastName()).isEqualTo(DEFAULT_LASTNAME);
        assertThat(userDTO.getEmail()).isEqualTo(DEFAULT_EMAIL);
        assertThat(userDTO.isActivated()).isEqualTo(true);
        assertThat(userDTO.getLangKey()).isEqualTo(DEFAULT_LANGKEY);
        assertThat(userDTO.getCreatedBy()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getCreatedDate()).isEqualTo(user.getCreatedDate());
        assertThat(userDTO.getLastModifiedBy()).isEqualTo(DEFAULT_LOGIN);
        assertThat(userDTO.getLastModifiedDate()).isEqualTo(user.getLastModifiedDate());
        assertThat(userDTO.getRoles()).containsExactly(Role.ROLE_ADMIN);
        assertThat(userDTO.toString()).isNotNull();
    }

    @Test
    public void testRoleEquals() {
        Role roleA = Role.ROLE_ADMIN;
        assertThat(roleA).isEqualTo(roleA);
        assertThat(roleA).isNotEqualTo(null);
        assertThat(roleA).isNotEqualTo(new Object());
        assertThat(roleA.toString()).isNotNull();

        Role roleB = Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR;
        assertThat(roleA).isNotEqualTo(roleB);

        roleA = Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR;
        assertThat(roleA).isEqualTo(roleB);
        assertThat(roleA.hashCode()).isEqualTo(roleB.hashCode());
    }

    private void assertPersistedUsers(Consumer<List<UserEntity>> userAssertion) {
        userAssertion.accept(userRepository.findAll());
    }
}
