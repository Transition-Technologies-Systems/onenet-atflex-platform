package pl.com.tt.flex.server.web.rest.user;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.common.errors.user.AccountResourceException;
import pl.com.tt.flex.server.common.errors.user.InvalidPasswordException;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.service.mail.MailService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.PasswordChangeDTO;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.web.rest.vm.user.KeyAndPasswordVM;
import pl.com.tt.flex.server.web.rest.vm.user.ManagedUserVM;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

import static pl.com.tt.flex.model.security.permission.Authority.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;

/**
 * REST controller for managing the current user's account.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class AccountResource {

    private final UserService userService;
    private final MailService mailService;

    public AccountResource(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
    }

    /**
     * {@code GET  /account/activate-and-set-password} : activate new user and set password.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws AccountResourceException {@code 400 (Bad Request)} if the user couldn't be activated.
     */
    @PostMapping("/account/activate-and-set-password")
    public void activateAccountAndSetPassword(@RequestBody KeyAndPasswordVM keyAndPassword) throws ObjectValidationException {
        log.debug("Rest request for activate new user by key and set password");
        if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
            throw new ObjectValidationException("Wrong password length", CANNOT_SET_PASSWORD_DUE_TO_INCORRECT_LENGTH);
        }
        if(userService.findOneByLogin(keyAndPassword.getNewLogin()).isPresent()){
            throw new ObjectValidationException("Login already exists", ERR_LOGIN_ALREADY_EXISTS);
        }
        Optional<UserEntity> user = userService.activateAndSetPassword(keyAndPassword.getKey(), keyAndPassword.getNewPassword(), keyAndPassword.getNewLogin());
        if (user.isEmpty()) {
            throw new ObjectValidationException("No user was found for this activation key: " + keyAndPassword.getKey(),
                SECURITY_KEY_IS_INVALID_OR_EXPIRED);
        }
    }

    /**
     * {@code GET  /authenticate} : check if the user is authenticated, and return its login.
     *
     * @param request the HTTP request.
     * @return the login if the user is authenticated.
     */
    @GetMapping("/authenticate")
    public String isAuthenticated(HttpServletRequest request) {
        log.debug("REST request to check if the current user is authenticated");
        return request.getRemoteUser();
    }

    /**
     * {@code GET  /account} : get the current user.
     *
     * @return the current user.
     * @throws AccountResourceException {@code 400 (Bad Request)} if the user couldn't be returned.
     */
    @GetMapping("/account")
    public UserDTO getAccount() {
        return userService.getCurrentUserDTO().orElseThrow(() -> new AccountResourceException("User could not be found"));
    }

    /**
     * {@code POST  /account/change-password} : changes the current user's password.
     *
     * @param passwordChangeDto current and new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the new password is incorrect.
     */
    @PostMapping(path = "/account/change-password")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_USER_CHANGE_PASSWORD + "\") or hasAuthority(\"" + FLEX_USER_CHANGE_PASSWORD + "\")")
    public void changePassword(@RequestBody PasswordChangeDTO passwordChangeDto) {
        if (!checkPasswordLength(passwordChangeDto.getNewPassword())) {
            throw new InvalidPasswordException();
        }
        userService.changePassword(passwordChangeDto.getCurrentPassword(), passwordChangeDto.getNewPassword());
    }

    /**
     * {@code POST   /account/reset-password/init} : Send an email to reset the password of the user.
     *
     * @param mail the mail of the user.
     * @param langKey if it's not null then override user's locale settings
     */
    @PostMapping(path = "/account/reset-password/init")
    public void requestPasswordReset(@RequestBody String mail, @RequestParam(required = false) String langKey) {
        Optional<UserEntity> user = userService.requestPasswordReset(mail);
        if (user.isPresent()) {
            mailService.sendPasswordResetMail(user.get(), langKey);
        } else {
            // Pretend the request has been successful to prevent checking which emails really exist
            // but log that an invalid attempt has been made
            log.warn("Password reset requested for non existing mail");
        }
    }

    /**
     * {@code POST   /account/reset-password/finish} : Finish to reset the password of the user.
     *
     * @param keyAndPassword the generated key and the new password.
     * @throws InvalidPasswordException {@code 400 (Bad Request)} if the password is incorrect.
     * @throws AccountResourceException {@code 400 (Bad Request)} if the password could not be reset.
     */
    @PostMapping(path = "/account/reset-password/finish")
    public void finishPasswordReset(@RequestBody KeyAndPasswordVM keyAndPassword) throws ObjectValidationException {
        if (!checkPasswordLength(keyAndPassword.getNewPassword())) {
            throw new ObjectValidationException("Wrong password length", CANNOT_SET_PASSWORD_DUE_TO_INCORRECT_LENGTH);
        }
        Optional<UserEntity> user =
            userService.completePasswordReset(keyAndPassword.getNewPassword(), keyAndPassword.getKey());

        if (user.isEmpty()) {
            throw new ObjectValidationException("Reset key is invalid or expired: " + keyAndPassword.getKey(), SECURITY_KEY_IS_INVALID_OR_EXPIRED);
        }
    }

    private static boolean checkPasswordLength(String password) {
        return !StringUtils.isEmpty(password) &&
            password.length() >= ManagedUserVM.PASSWORD_MIN_LENGTH &&
            password.length() <= ManagedUserVM.PASSWORD_MAX_LENGTH;
    }
}
