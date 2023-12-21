package pl.com.tt.flex.server.web.rest.user.config;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_SYS_USER_EMAIL_CONFIG;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.server.domain.email.enumeration.EmailType;
import pl.com.tt.flex.server.service.user.config.email.UserEmailConfigService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-email-configs")
public class UserEmailConfigResource {

    private final UserEmailConfigService userEmailConfigService;

    @GetMapping
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_USER_EMAIL_CONFIG + "\")")
    public ResponseEntity<Map<EmailType, Boolean>> getForCurrentUser() {
        log.info("REST request to get emails config for current user");
        return ResponseEntity.ok(userEmailConfigService.getConfigForCurrentUser());
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_USER_EMAIL_CONFIG + "\")")
    public void save(@RequestBody Map<EmailType, Boolean> config) {
        log.info("REST request to save emails config for current user");
        userEmailConfigService.saveConfigForCurrentUser(config);
    }

}
