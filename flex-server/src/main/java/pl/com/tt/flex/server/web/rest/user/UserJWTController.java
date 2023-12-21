package pl.com.tt.flex.server.web.rest.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.tt.flex.server.security.jwt.JWTFilter;
import pl.com.tt.flex.server.security.jwt.TokenProvider;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.ServletRequestUtils;
import pl.com.tt.flex.server.web.rest.errors.user.GatewayLoginException;
import pl.com.tt.flex.server.web.rest.errors.user.NoPermissionToLoginException;
import pl.com.tt.flex.server.web.rest.errors.user.UserNotActivatedException;
import pl.com.tt.flex.server.web.rest.errors.user.UsernameNotFoundException;
import pl.com.tt.flex.server.web.rest.vm.user.LoginVM;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collection;
import java.util.Optional;

import static pl.com.tt.flex.server.config.Constants.*;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_LOGIN;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_LOGIN;

/**
 * Controller to authenticate users.
 */
@RestController
@RequestMapping("/api")
public class UserJWTController {

    private final TokenProvider tokenProvider;

    private final AuthenticationManagerBuilder authenticationManagerBuilder;

    private final UserService userService;

    public UserJWTController(TokenProvider tokenProvider, AuthenticationManagerBuilder authenticationManagerBuilder, UserService userService) {
        this.tokenProvider = tokenProvider;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.userService = userService;
    }

    @PostMapping("/authenticate")
    public ResponseEntity<JWTToken> authorize(@Valid @RequestBody LoginVM loginVM, HttpServletRequest request) {

        UsernamePasswordAuthenticationToken authenticationToken =
            new UsernamePasswordAuthenticationToken(loginVM.getUsername(), loginVM.getPassword());

        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
            checkIfUserHasGatewayLoginAuthority(request, authentication, loginVM);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.createToken(authentication, ServletRequestUtils.getClientIpAddr(request));
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.add(JWTFilter.AUTHORIZATION_HEADER, "Bearer " + jwt);
            userService.clearFailedLoginCounterAndSetLoginDate(loginVM.getUsername());
            return new ResponseEntity<>(new JWTToken(jwt), httpHeaders, HttpStatus.OK);
        } catch (AuthenticationException | NoPermissionToLoginException e) {
            userService.incrementFailedLoginCounter(loginVM.getUsername());
            if (e.getCause() instanceof UserNotActivatedException) {
                throw new UserNotActivatedException(e.getMessage(), ((UserNotActivatedException) e.getCause()).getMsgKey());
            } else if (e.getCause() instanceof UsernameNotFoundException) {
                throw new UsernameNotFoundException(e.getMessage(), ((UsernameNotFoundException) e.getCause()).getMsgKey());
            }
            throw e;
        }
    }

    /**
     * Object to return as body in JWT Authentication.
     */
    static class JWTToken {

        private String idToken;

        JWTToken(String idToken) {
            this.idToken = idToken;
        }

        @JsonProperty("id_token")
        String getIdToken() {
            return idToken;
        }

        void setIdToken(String idToken) {
            this.idToken = idToken;
        }
    }


    /**
     * Sprawdzenie czy uzytkownik ma uprawnienia do zalogowanie sie do danego modulu (FLEX-ADMIN / FLEX-USER)
     */
    private void checkIfUserHasGatewayLoginAuthority(HttpServletRequest request, Authentication authentication, LoginVM loginVM) {
        String gateway = Optional.ofNullable(request.getHeader(FLEX_APP_NAME_HEADER))
            .orElseThrow(() -> new GatewayLoginException("checkIfUserHasGatewayLoginPermission() 'Gateway' header is not present!"));
        boolean hasAuthority;
        switch (gateway) {
            case FLEX_ADMIN_APP_NAME: {
                hasAuthority = hasLoginAuthority(authentication.getAuthorities(), FLEX_ADMIN_LOGIN);
                break;
            }
            case FLEX_USER_APP_NAME: {
                hasAuthority = hasLoginAuthority(authentication.getAuthorities(), FLEX_USER_LOGIN);
                break;
            }
            default:
                throw new GatewayLoginException("checkIfUserHasGatewayLoginPermission() Unknown gateway: '" + gateway + "'!");
        }
        if (!hasAuthority) {
            throw new NoPermissionToLoginException(loginVM.getUsername(), gateway);
        }
    }

    private boolean hasLoginAuthority(Collection<? extends GrantedAuthority> authorities, String loginAuthority) {
        return authorities.stream().map(GrantedAuthority::getAuthority).anyMatch(loginAuthority::equals);
    }
}
