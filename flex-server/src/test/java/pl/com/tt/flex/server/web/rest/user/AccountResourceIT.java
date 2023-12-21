package pl.com.tt.flex.server.web.rest.user;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.NestedServletException;
import pl.com.tt.flex.server.FlexserverApp;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.domain.user.enumeration.CreationSource;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.PasswordChangeDTO;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.web.rest.TestUtil;
import pl.com.tt.flex.server.web.rest.errors.user.UsernameNotFoundException;
import pl.com.tt.flex.server.web.rest.vm.user.KeyAndPasswordVM;
import pl.com.tt.flex.server.web.rest.vm.user.ManagedUserVM;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_USER_CHANGE_PASSWORD;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_ADMIN;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_SET_PASSWORD_DUE_TO_INCORRECT_LENGTH;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SECURITY_KEY_IS_INVALID_OR_EXPIRED;

/**
 * Integration tests for the {@link AccountResource} REST controller.
 */
@AutoConfigureMockMvc
@WithMockUser(value = AccountResourceIT.TEST_USER_LOGIN)
@SpringBootTest(classes = FlexserverApp.class)
public class AccountResourceIT {
    static final String TEST_USER_LOGIN = "test";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc restAccountMockMvc;

    @Test
    @WithUnauthenticatedMockUser
    public void testNonAuthenticatedUser() throws Exception {
        restAccountMockMvc.perform(get("/api/authenticate")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(""));
    }

    @Test
    public void testAuthenticatedUser() throws Exception {
        restAccountMockMvc.perform(get("/api/authenticate")
            .with(request -> {
                request.setRemoteUser(TEST_USER_LOGIN);
                return request;
            })
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().string(TEST_USER_LOGIN));
    }

    @Test
    public void testGetExistingAccount() throws Exception {
        Set<Role> roles = new HashSet<>();
        roles.add(ROLE_ADMIN);

        UserDTO user = new UserDTO();
        user.setLogin(TEST_USER_LOGIN);
        user.setFirstName("john");
        user.setLastName("doe");
        user.setEmail("john.doe@jhipster.com");
        user.setLangKey("en");
        user.setRoles(roles);
        userService.createUser(user);

        restAccountMockMvc.perform(get("/api/account")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.login").value(TEST_USER_LOGIN))
            .andExpect(jsonPath("$.firstName").value("john"))
            .andExpect(jsonPath("$.lastName").value("doe"))
            .andExpect(jsonPath("$.email").value("john.doe@jhipster.com"))
            .andExpect(jsonPath("$.langKey").value("en"))
            .andExpect(jsonPath("$.roles").value(ROLE_ADMIN.name()));
    }

    @Test
    public void testGetUnknownAccount() throws Exception {
        restAccountMockMvc.perform(get("/api/account")
            .accept(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("User could not be found"));
    }

    @Transactional
    public void testRegisterValid() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("test-register-valid");
        validUser.setPassword("password");
        validUser.setFirstName("Alice");
        validUser.setLastName("Test");
        validUser.setEmail("test-register-valid@example.com");
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setRoles(Collections.singleton(ROLE_ADMIN));
        assertThat(userRepository.findOneByLoginAndDeletedIsFalse("test-register-valid").isPresent()).isFalse();

        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        assertThat(userRepository.findOneByLoginAndDeletedIsFalse("test-register-valid").isPresent()).isTrue();
    }

    @Transactional
    public void testRegisterInvalidLogin() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("funky-log(n");// <-- invalid
        invalidUser.setPassword("password");
        invalidUser.setFirstName("Funky");
        invalidUser.setLastName("One");
        invalidUser.setEmail("funky@example.com");
        invalidUser.setActivated(true);
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setRoles(Collections.singleton(ROLE_ADMIN));

        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<UserEntity> user = userRepository.findOneByEmailAndActivatedTrueIgnoreCase("funky@example.com");
        assertThat(user.isPresent()).isFalse();
    }

    @Transactional
    public void testRegisterInvalidEmail() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword("password");
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("invalid");// <-- invalid
        invalidUser.setActivated(true);
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setRoles(Collections.singleton(ROLE_ADMIN));

        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<UserEntity> user = userRepository.findOneByLoginAndDeletedIsFalse("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Transactional
    public void testRegisterInvalidPassword() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword("123");// password with only 3 digits
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("bob@example.com");
        invalidUser.setActivated(true);
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setRoles(Collections.singleton(ROLE_ADMIN));

        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<UserEntity> user = userRepository.findOneByLoginAndDeletedIsFalse("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Transactional
    public void testRegisterNullPassword() throws Exception {
        ManagedUserVM invalidUser = new ManagedUserVM();
        invalidUser.setLogin("bob");
        invalidUser.setPassword(null);// invalid null password
        invalidUser.setFirstName("Bob");
        invalidUser.setLastName("Green");
        invalidUser.setEmail("bob@example.com");
        invalidUser.setActivated(true);
        invalidUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        invalidUser.setRoles(Collections.singleton(ROLE_ADMIN));

        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(invalidUser)))
            .andExpect(status().isBadRequest());

        Optional<UserEntity> user = userRepository.findOneByLoginAndDeletedIsFalse("bob");
        assertThat(user.isPresent()).isFalse();
    }

    @Transactional
    public void testRegisterDuplicateLogin() throws Exception {
        // First registration
        ManagedUserVM firstUser = new ManagedUserVM();
        firstUser.setLogin("alice");
        firstUser.setPassword("password");
        firstUser.setFirstName("Alice");
        firstUser.setLastName("Something");
        firstUser.setEmail("alice@example.com");
        firstUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        firstUser.setRoles(Collections.singleton(ROLE_ADMIN));

        // Duplicate login, different email
        ManagedUserVM secondUser = new ManagedUserVM();
        secondUser.setLogin(firstUser.getLogin());
        secondUser.setPassword(firstUser.getPassword());
        secondUser.setFirstName(firstUser.getFirstName());
        secondUser.setLastName(firstUser.getLastName());
        secondUser.setEmail("alice2@example.com");
        secondUser.setLangKey(firstUser.getLangKey());
        secondUser.setCreatedBy(firstUser.getCreatedBy());
        secondUser.setCreatedDate(firstUser.getCreatedDate());
        secondUser.setLastModifiedBy(firstUser.getLastModifiedBy());
        secondUser.setLastModifiedDate(firstUser.getLastModifiedDate());
        secondUser.setRoles(new HashSet<>(firstUser.getRoles()));

        // First user
        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(firstUser)))
            .andExpect(status().isCreated());

        // Second (non activated) user
        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().isCreated());

        Optional<UserEntity> testUser = userRepository.findOneByEmailAndActivatedTrueIgnoreCase("alice2@example.com");
        assertThat(testUser.isPresent()).isTrue();
        testUser.get().setActivated(true);
        userRepository.save(testUser.get());

        // Second (already activated) user
        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().is4xxClientError());
    }

    @Transactional
    public void testRegisterDuplicateEmail() throws Exception {
        // First user
        ManagedUserVM firstUser = new ManagedUserVM();
        firstUser.setLogin("test-register-duplicate-email");
        firstUser.setPassword("password");
        firstUser.setFirstName("Alice");
        firstUser.setLastName("Test");
        firstUser.setEmail("test-register-duplicate-email@example.com");
        firstUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        firstUser.setRoles(Collections.singleton(ROLE_ADMIN));

        // Register first user
        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(firstUser)))
            .andExpect(status().isCreated());

        Optional<UserEntity> testUser1 = userRepository.findOneByLoginAndDeletedIsFalse("test-register-duplicate-email");
        assertThat(testUser1.isPresent()).isTrue();

        // Duplicate email, different login
        ManagedUserVM secondUser = new ManagedUserVM();
        secondUser.setLogin("test-register-duplicate-email-2");
        secondUser.setPassword(firstUser.getPassword());
        secondUser.setFirstName(firstUser.getFirstName());
        secondUser.setLastName(firstUser.getLastName());
        secondUser.setEmail(firstUser.getEmail());
        secondUser.setLangKey(firstUser.getLangKey());
        secondUser.setRoles(new HashSet<>(firstUser.getRoles()));

        // Register second (non activated) user
        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().isCreated());

        Optional<UserEntity> testUser2 = userRepository.findOneByLoginAndDeletedIsFalse("test-register-duplicate-email");
        assertThat(testUser2.isPresent()).isFalse();

        Optional<UserEntity> testUser3 = userRepository.findOneByLoginAndDeletedIsFalse("test-register-duplicate-email-2");
        assertThat(testUser3.isPresent()).isTrue();

        // Duplicate email - with uppercase email address
        ManagedUserVM userWithUpperCaseEmail = new ManagedUserVM();
        userWithUpperCaseEmail.setId(firstUser.getId());
        userWithUpperCaseEmail.setLogin("test-register-duplicate-email-3");
        userWithUpperCaseEmail.setPassword(firstUser.getPassword());
        userWithUpperCaseEmail.setFirstName(firstUser.getFirstName());
        userWithUpperCaseEmail.setLastName(firstUser.getLastName());
        userWithUpperCaseEmail.setEmail("TEST-register-duplicate-email@example.com");
        userWithUpperCaseEmail.setLangKey(firstUser.getLangKey());
        userWithUpperCaseEmail.setRoles(new HashSet<>(firstUser.getRoles()));

        // Register third (not activated) user
        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(userWithUpperCaseEmail)))
            .andExpect(status().isCreated());

        Optional<UserEntity> testUser4 = userRepository.findOneByLoginAndDeletedIsFalse("test-register-duplicate-email-3");
        assertThat(testUser4.isPresent()).isTrue();
        assertThat(testUser4.get().getEmail()).isEqualTo("test-register-duplicate-email@example.com");

        testUser4.get().setActivated(true);
        userService.updateUser((new UserDTO(testUser4.get())));

        // Register 4th (already activated) user
        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(secondUser)))
            .andExpect(status().is4xxClientError());
    }

    @Transactional
    public void testRegisterAdminIsIgnored() throws Exception {
        ManagedUserVM validUser = new ManagedUserVM();
        validUser.setLogin("badguy");
        validUser.setPassword("password");
        validUser.setFirstName("Bad");
        validUser.setLastName("Guy");
        validUser.setEmail("badguy@example.com");
        validUser.setActivated(true);
        validUser.setLangKey(Constants.DEFAULT_LANGUAGE);
        validUser.setRoles(Collections.singleton(ROLE_ADMIN));

        restAccountMockMvc.perform(
            post("/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(validUser)))
            .andExpect(status().isCreated());

        Optional<UserEntity> userDup = userRepository.findOneByLoginAndDeletedFalse("badguy");
        assertThat(userDup.isPresent()).isTrue();
        assertThat(userDup.get().getRoles()).hasSize(1)
            .containsExactly(Role.valueOf(ROLE_ADMIN.name()));
    }

    @Transactional
    public void testActivateAccount() throws Exception {
        final String activationKey = "some activation key";
        UserEntity user = new UserEntity();
        user.setLogin("activate-account");
        user.setEmail("activate-account@example.com");
        user.setPassword(passwordEncoder.encode(RandomStringUtils.random(60)));
        user.setActivated(false);
        user.setActivationKey(activationKey);
        user.setCreationSource(CreationSource.SYSTEM);

        userRepository.saveAndFlush(user);

        restAccountMockMvc.perform(get("/api/activate?key={activationKey}", activationKey))
            .andExpect(status().isOk());

        user = userRepository.findOneByLoginAndDeletedIsFalse(user.getLogin()).orElse(null);
        assertThat(user.isActivated()).isTrue();
    }

    @Transactional
    public void testActivateAccountWithWrongKey() throws Exception {
        restAccountMockMvc.perform(get("/api/activate?key=wrongActivationKey"))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @Transactional
    @WithMockUser(value = "change-password-wrong-existing-password", authorities = FLEX_ADMIN_USER_CHANGE_PASSWORD)
    public void testChangePasswordWrongExistingPassword() throws Exception {
        UserEntity user = new UserEntity();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-wrong-existing-password");
        user.setEmail("change-password-wrong-existing-password@example.com");
        user.setCreationSource(CreationSource.SYSTEM);
        userRepository.saveAndFlush(user);

        restAccountMockMvc.perform(post("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO("1" + currentPassword, "new password")))
        )
            .andExpect(status().isBadRequest());

        UserEntity updatedUser = userRepository.findOneByLoginAndDeletedIsFalse("change-password-wrong-existing-password").orElse(null);
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isFalse();
        assertThat(passwordEncoder.matches(currentPassword, updatedUser.getPassword())).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(value = "change-password", authorities = FLEX_ADMIN_USER_CHANGE_PASSWORD)
    public void testChangePassword() throws Exception {
        UserEntity user = new UserEntity();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password");
        user.setEmail("change-password@example.com");
        user.setCreationSource(CreationSource.SYSTEM);
        userRepository.saveAndFlush(user);

        restAccountMockMvc.perform(post("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, "new password")))
        )
            .andExpect(status().isOk());

        UserEntity updatedUser = userRepository.findOneByLoginAndDeletedIsFalse("change-password").orElse(null);
        assertThat(passwordEncoder.matches("new password", updatedUser.getPassword())).isTrue();
    }

    @Test
    @Transactional
    @WithMockUser(value = "change-password-too-small", authorities = FLEX_ADMIN_USER_CHANGE_PASSWORD)
    public void testChangePasswordTooSmall() throws Exception {
        UserEntity user = new UserEntity();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-too-small");
        user.setEmail("change-password-too-small@example.com");
        user.setCreationSource(CreationSource.SYSTEM);
        userRepository.saveAndFlush(user);

        String newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MIN_LENGTH - 1);

        restAccountMockMvc.perform(post("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, newPassword)))
        )
            .andExpect(status().isBadRequest());

        UserEntity updatedUser = userRepository.findOneByLoginAndDeletedIsFalse("change-password-too-small").orElse(null);
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @Transactional
    @WithMockUser(value = "change-password-too-long", authorities = FLEX_ADMIN_USER_CHANGE_PASSWORD)
    public void testChangePasswordTooLong() throws Exception {
        UserEntity user = new UserEntity();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-too-long");
        user.setEmail("change-password-too-long@example.com");
        user.setCreationSource(CreationSource.SYSTEM);
        userRepository.saveAndFlush(user);

        String newPassword = RandomStringUtils.random(ManagedUserVM.PASSWORD_MAX_LENGTH + 1);

        restAccountMockMvc.perform(post("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, newPassword)))
        )
            .andExpect(status().isBadRequest());

        UserEntity updatedUser = userRepository.findOneByLoginAndDeletedIsFalse("change-password-too-long").orElse(null);
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @Transactional
    @WithMockUser(value = "change-password-empty", authorities = FLEX_ADMIN_USER_CHANGE_PASSWORD)
    public void testChangePasswordEmpty() throws Exception {
        UserEntity user = new UserEntity();
        String currentPassword = RandomStringUtils.random(60);
        user.setPassword(passwordEncoder.encode(currentPassword));
        user.setLogin("change-password-empty");
        user.setEmail("change-password-empty@example.com");
        user.setCreationSource(CreationSource.SYSTEM);
        userRepository.saveAndFlush(user);

        restAccountMockMvc.perform(post("/api/account/change-password")
            .contentType(MediaType.APPLICATION_JSON)
            .content(TestUtil.convertObjectToJsonBytes(new PasswordChangeDTO(currentPassword, "")))
        )
            .andExpect(status().isBadRequest());

        UserEntity updatedUser = userRepository.findOneByLoginAndDeletedIsFalse("change-password-empty").orElse(null);
        assertThat(updatedUser.getPassword()).isEqualTo(user.getPassword());
    }

    @Test
    @Transactional
    public void testRequestPasswordReset() throws Exception {
        UserEntity user = new UserEntity();
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setLogin("password-reset");
        user.setEmail("password-reset@example.com");
        user.setCreationSource(CreationSource.SYSTEM);
        userRepository.saveAndFlush(user);

        restAccountMockMvc.perform(post("/api/account/reset-password/init")
            .content("password-reset@example.com")
        )
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void testRequestPasswordResetUpperCaseEmail() throws Exception {
        UserEntity user = new UserEntity();
        user.setPassword(RandomStringUtils.random(60));
        user.setActivated(true);
        user.setLogin("password-reset-upper-case");
        user.setEmail("password-reset-upper-case@example.com");
        user.setCreationSource(CreationSource.SYSTEM);
        userRepository.saveAndFlush(user);

        restAccountMockMvc.perform(post("/api/account/reset-password/init")
            .content("password-reset-upper-case@EXAMPLE.COM")
        )
            .andExpect(status().isOk());
    }

    @Test
    public void testRequestPasswordResetWrongEmail() throws Exception {
        restAccountMockMvc.perform(
            post("/api/account/reset-password/init")
                .content("password-reset-wrong-email@example.com"))
            .andExpect(status().isOk());
    }

    @Test
    @Transactional
    public void testFinishPasswordReset() throws Exception {
        UserEntity user = new UserEntity();
        user.setPassword(RandomStringUtils.random(60));
        user.setLogin("finish-password-reset");
        user.setEmail("finish-password-reset@example.com");
        user.setResetDate(Instant.now().plusSeconds(60));
        user.setResetKey("reset key");
        user.setCreationSource(CreationSource.SYSTEM);
        userRepository.saveAndFlush(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("new password");

        restAccountMockMvc.perform(
            post("/api/account/reset-password/finish")
                .contentType(MediaType.APPLICATION_JSON)
                .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)))
            .andExpect(status().isOk());

        UserEntity updatedUser = userRepository.findOneByLoginAndDeletedIsFalse(user.getLogin()).orElse(null);
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isTrue();
    }

    @Test
    @Transactional
    public void testFinishPasswordResetTooSmall() throws Exception {
        UserEntity user = new UserEntity();
        user.setPassword(RandomStringUtils.random(60));
        user.setLogin("finish-password-reset-too-small");
        user.setEmail("finish-password-reset-too-small@example.com");
        user.setResetDate(Instant.now().plusSeconds(60));
        user.setResetKey("reset key too small");
        user.setCreationSource(CreationSource.SYSTEM);
        userRepository.saveAndFlush(user);

        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey(user.getResetKey());
        keyAndPassword.setNewPassword("foo");

        ObjectValidationException thrown = (ObjectValidationException) Assertions.assertThrows(NestedServletException.class, () -> {
            restAccountMockMvc.perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)));
        }).getRootCause();

        assertThat(thrown.getMsgKey()).isEqualTo(CANNOT_SET_PASSWORD_DUE_TO_INCORRECT_LENGTH);
        UserEntity updatedUser = userRepository.findOneByLoginAndDeletedIsFalse(user.getLogin()).orElse(null);
        assertThat(passwordEncoder.matches(keyAndPassword.getNewPassword(), updatedUser.getPassword())).isFalse();
    }

    @Test
    @Transactional
    public void testFinishPasswordResetWrongKey() throws Exception {
        KeyAndPasswordVM keyAndPassword = new KeyAndPasswordVM();
        keyAndPassword.setKey("wrong reset key");
        keyAndPassword.setNewPassword("new password");

        ObjectValidationException thrown = (ObjectValidationException) Assertions.assertThrows(NestedServletException.class, () -> {
            restAccountMockMvc.perform(
                post("/api/account/reset-password/finish")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(keyAndPassword)));
        }).getRootCause();

        assertThat(thrown.getMsgKey()).isEqualTo(SECURITY_KEY_IS_INVALID_OR_EXPIRED);
    }
}
