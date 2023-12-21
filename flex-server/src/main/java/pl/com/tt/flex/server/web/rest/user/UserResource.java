package pl.com.tt.flex.server.web.rest.user;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.common.errors.mail.EmailAlreadyUsedException;
import pl.com.tt.flex.server.config.Constants;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.security.SecurityUtils;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.user.UserOnlineService;
import pl.com.tt.flex.server.service.user.UserQueryService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserCriteria;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.service.user.dto.UserMinDTO;
import pl.com.tt.flex.server.validator.user.UserValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;
import pl.com.tt.flex.server.web.rest.errors.user.LoginAlreadyUsedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing users.
 * <p>
 * This class accesses the {@link UserEntity} entity, and needs to fetch its collection of authorities.
 * <p>
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 * <p>
 * We use a View Model and a DTO for 3 reasons:
 * <ul>
 * <li>We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.</li>
 * <li> Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).</li>
 * <li> As this manages users, for security reasons, we'd rather have a DTO layer.</li>
 * </ul>
 * <p>
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserResource {

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final UserService userService;
    private final UserOnlineService userOnlineService;
    private final UserValidator userValidator;
    private final UserQueryService userQueryService;

    public static final String ENTITY_NAME = "user";

    /**
     * {@code POST  /users}  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user, or with status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws URISyntaxException       if the Location URI syntax is incorrect.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the login or email is already in use.
     */
    @PostMapping("/users")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_USER_MANAGE + "\")")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody UserDTO userDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("REST request to save User : {}", userDTO);

        userValidator.checkCreateRequest(userDTO);
        UserDTO newUser = userService.createUser(userDTO);

        return ResponseEntity.created(new URI("/api/users/" + newUser.getLogin()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, newUser.getId().toString())).body(newUser);
    }

    /**
     * {@code PUT /users} : Updates an existing User.
     *
     * @param userDTO the user to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already in use.
     */
    @PutMapping("/users")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_USER_MANAGE + "\") or hasAuthority(\"" + FLEX_USER_USER_MANAGE + "\")")
    public ResponseEntity<UserDTO> updateUser(@Valid @RequestBody UserDTO userDTO) throws ObjectValidationException {
        log.debug("REST request to update User : {}", userDTO);
        UserEntity oldUser = userService.findOne(userDTO.getId()).get();
        userValidator.checkUpdateRequest(userDTO);
        UserDTO updatedUser = userService.updateUser(userDTO);
        userService.registerNewNotificationForUserEdition(oldUser, updatedUser);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, updatedUser.getId().toString())).body(updatedUser);
    }

    /**
     * {@code GET /users} : get all users.
     *
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_USER_VIEW + "\")")
    public ResponseEntity<List<UserDTO>> getAllUsers(UserCriteria criteria, Pageable pageable) {
        findNotDeletedUserFilter(criteria);
        Page<UserDTO> page = userQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    private void findNotDeletedUserFilter(UserCriteria criteria) {
        BooleanFilter isNotDeletedFilter = new BooleanFilter();
        isNotDeletedFilter.setEquals(false);
        criteria.setDeleted(isNotDeletedFilter);
    }

    /**
     * {@code GET /users/minimal} : get all users minimal data.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/users/minimal")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_USER_VIEW + "\") or hasAuthority(\"" + FLEX_USER_VIEW + "\")")
    public ResponseEntity<List<UserMinDTO>> getAllUsersMinimal(UserCriteria criteria) {
        criteria.setDeleted((BooleanFilter) new BooleanFilter().setEquals(false));
        return ResponseEntity.ok(userQueryService.findMinByCriteria(criteria));
    }

    /**
     * {@code GET /users/get-pso-sso} : get all users minimal data for pSo and sSo selects.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @GetMapping("/users/get-pso-sso")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_LOGIN + "\") or hasAuthority(\"" + FLEX_ADMIN_LOGIN + "\")")
    public ResponseEntity<List<UserMinDTO>> getUsersForPsoAndSso() {
        return ResponseEntity.ok(userService.getUsersForPsoAndSso());
    }

    /**
     * {@code POST /users/change-lang/:langKey} : change current logged user language.
     *
     * @param langKey the language code.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PostMapping("/users/change-lang/{langKey}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_LOGIN + "\") or hasAuthority(\"" + FLEX_ADMIN_LOGIN + "\")")
    public ResponseEntity<Void> changeLanguage(@PathVariable String langKey) {
        userService.changeUserLanguage(langKey);
        return ResponseEntity.ok().build();
    }


    /**
     * Gets a list of all roles.
     *
     * @return a string list of all roles.
     */
    @GetMapping("/users/roles")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_USER_VIEW + "\")")
    public List<Role> getRoles() {
        return Arrays.asList(Role.values());
    }

    /**
     * Gets a list of all FSP platform roles.
     *
     * @return a string list of all roles.
     */
    @GetMapping("/users/roles/fsp-organisations")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_USER_VIEW + "\")")
    public Set<Role> getFspOrganisationsRoles() {
        return Role.FSP_ORGANISATIONS_ROLES;
    }

    /**
     * {@code GET /users/:login} : get the "login" user.
     *
     * @param login the login of the user to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "login" user, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/users/{login:" + Constants.LOGIN_REGEX + "}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_USER_VIEW + "\") or hasAuthority(\"" + FLEX_USER_VIEW + "\")")
    public ResponseEntity<UserDTO> getUser(@PathVariable String login) {
        log.debug("REST request to get User : {}", login);
        return ResponseUtil.wrapOrNotFound(userService.getUserByLogin(login));
    }

    /**
     * {@code DELETE /users/:id} : delete the User with "id".
     *
     * @param id the id of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_USER_DELETE + "\")")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) throws ObjectValidationException {
        log.debug("REST request to delete User: id={}", id);
        userValidator.checkDeletable(id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    /**
     * {@code PUT /users/logout-current} : delete the Online User record.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PutMapping("/users/logout-current")
    public ResponseEntity logoutCurrentUser(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> currentUserLogin = SecurityUtils.getCurrentUserLogin();
        if (currentUserLogin.isPresent()) {
            log.info("REST request to logout current user {}", currentUserLogin.get());
            userOnlineService.logout(currentUserLogin.get());
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                new SecurityContextLogoutHandler().logout(request, response, auth);
            }
        }
        return ResponseEntity.ok(null);
    }

    @GetMapping("/users/manual")
    public ResponseEntity<List<MinimalDTO<String, String>>> getListOfManualFiles() {
        log.debug("REST request to get list of manual files");
        return ResponseEntity.ok().body(userService.getListOfManualFiles());
    }


    @GetMapping("/users/manual/{filename}")
    public ResponseEntity<FileDTO> getManualFile(@PathVariable String filename) throws IOException {
        log.debug("REST request to get a file: {}", filename);
        Optional<FileDTO> fileDTO = userService.getManualFile(filename);
        return ResponseUtil.wrapOrNotFound(fileDTO);
    }

    @GetMapping("/users/rules/{type}")
    public ResponseEntity<FileDTO> getRulesFile(@PathVariable RulesFileType type) throws IOException {
        log.debug("REST request to get a rules file: {}", type);
        Optional<FileDTO> fileDTO = userService.getRulesFile(type);
        return ResponseUtil.wrapOrNotFound(fileDTO);
    }

//********************************************************************************** PROFILE ************************************************************************************

    @GetMapping("/users/profile-data")
    public ResponseEntity<UserDTO> getUserProfileData() {
        log.debug("REST request to get user profile data");
        UserDTO userDTO = userService.getCurrentUserDTO().orElseThrow(() -> new RuntimeException("Current logged-in user not found"));
        return ResponseEntity.ok().body(userDTO);
    }

}
