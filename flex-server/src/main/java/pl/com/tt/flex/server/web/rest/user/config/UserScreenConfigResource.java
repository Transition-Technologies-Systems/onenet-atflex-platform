package pl.com.tt.flex.server.web.rest.user.config;

import io.github.jhipster.web.util.HeaderUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.user.config.screen.UserScreenConfigService;
import pl.com.tt.flex.server.service.user.config.screen.dto.UserScreenConfigDTO;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static java.util.Objects.isNull;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_SYS_USER_SCREEN_CONFIG;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserScreenConfigResource {

    public static final String ENTITY_NAME = "userScreenConfig";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;


    private final UserScreenConfigService userScreenConfigService;

    /**
     * {@code GET  /user-screen-configs/current-user} : get screen config by current logged user.
     *
     * @param screen The name of the screen for which the configuration is searched for.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the user screen config in body.
     */
    @GetMapping("/user-screen-configs/current-user")
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_USER_SCREEN_CONFIG + "\")")
    public ResponseEntity<UserScreenConfigDTO> getForCurrentUserByScreen(@RequestParam("screen") Screen screen) {
        log.info("REST request to get UserScreenConfigDTO for current user and screen {}", screen);
        Optional<UserScreenConfigDTO> optUserScreenConfig = userScreenConfigService.getForCurrentUserByScreen(screen);
        return optUserScreenConfig.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * {@code POST  /user-screen-configs}  : Creates new user screen configuration / Updates existing configuration.
     *
     * @param userScreenConfigDTO the user screen config to save.
     * @return the {@link ResponseEntity} with status {@code 201 (Created) / } and with saved body user screen configuration
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/user-screen-configs")
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_USER_SCREEN_CONFIG + "\")")
    public ResponseEntity<UserScreenConfigDTO> save(@Valid @RequestBody UserScreenConfigDTO userScreenConfigDTO) throws URISyntaxException {
        boolean isNew = isNull(userScreenConfigDTO.getId());
        userScreenConfigService.getForCurrentUserByScreen(userScreenConfigDTO.getScreen()).ifPresent(dbDto -> userScreenConfigDTO.setId(dbDto.getId())); //front moze nie podac id
        UserScreenConfigDTO result = userScreenConfigService.save(userScreenConfigDTO);
        if (isNew) {
            return ResponseEntity.created(new URI("/api/user-screen-config/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString())).body(result);
        }
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString())).body(result);
    }
}
