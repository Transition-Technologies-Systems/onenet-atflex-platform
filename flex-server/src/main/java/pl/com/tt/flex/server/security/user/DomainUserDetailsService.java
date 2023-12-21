package pl.com.tt.flex.server.security.user;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.server.security.permission.factory.AuthoritiesContainerFactory;
import pl.com.tt.flex.server.web.rest.errors.ErrorConstants;
import pl.com.tt.flex.server.web.rest.errors.user.UserNotActivatedException;
import pl.com.tt.flex.server.web.rest.errors.user.UsernameNotFoundException;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Authenticate a user from the database.
 */
@Slf4j
@Component("userDetailsService")
public class DomainUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuthoritiesContainerFactory authoritiesContainerFactory;

    public DomainUserDetailsService(UserRepository userRepository, AuthoritiesContainerFactory authoritiesContainerFactory) {
        this.userRepository = userRepository;
        this.authoritiesContainerFactory = authoritiesContainerFactory;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {
        log.debug("Authenticating {}", login);

        if (new EmailValidator().isValid(login, null)) {
            return userRepository.findOneByEmailAndDeletedFalse(login)
                .filter(u -> !u.isDeleted())
                .map(user -> createSpringSecurityUser(login, user))
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + login + " was not found in the database", ErrorConstants.USER_EMAIL_NOT_FOUND));
        }

        return userRepository.findOneByLoginAndDeletedFalse(login)
            .filter(u -> !u.isDeleted())
            .map(user -> createSpringSecurityUser(login, user))
            .orElseThrow(() -> new UsernameNotFoundException("User " + login + " was not found in the database", ErrorConstants.USER_LOGIN_NOT_FOUND));

    }

    private org.springframework.security.core.userdetails.User createSpringSecurityUser(String lowercaseLogin, UserEntity user) {
        if (!user.isActivated()) {
            throw new UserNotActivatedException("User " + lowercaseLogin + " was not activated", ErrorConstants.USER_IS_NOT_ACTIVATED);
        }
        Set<String> authorities = authoritiesContainerFactory.getUserAuthorities(user.getRoles());
        List<GrantedAuthority> grantedAuthorities = authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        return new org.springframework.security.core.userdetails.User(user.getLogin(),
            user.getPassword(),
            grantedAuthorities);
    }
}
