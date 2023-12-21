package pl.com.tt.flex.server.web.rest.activityMonitor;

import io.github.jhipster.service.filter.StringFilter;
import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityMonitorEntity;
import pl.com.tt.flex.server.service.activityMonitor.ActivityMonitorQueryService;
import pl.com.tt.flex.server.service.activityMonitor.ActivityMonitorService;
import pl.com.tt.flex.server.service.activityMonitor.dto.ActivityMonitorCriteria;
import pl.com.tt.flex.server.service.activityMonitor.dto.ActivityMonitorDTO;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.List;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_SYS_ACTIVITY_MONITOR_VIEW;

/**
 * REST controller for managing {@link ActivityMonitorEntity}.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class ActivityMonitorResource {

    public static final String ENTITY_NAME = "activityMonitor";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ActivityMonitorService activityMonitorService;
    private final ActivityMonitorQueryService activityMonitorQueryService;
    private final UserService userService;

    public ActivityMonitorResource(ActivityMonitorService activityMonitorService, ActivityMonitorQueryService activityMonitorQueryService, UserService userService) {
        this.activityMonitorService = activityMonitorService;
        this.activityMonitorQueryService = activityMonitorQueryService;
        this.userService = userService;
    }

    /**
     * {@code POST  /activity-monitor}  : Create a new activity notification for current logged-in user.
     *
     * @param activityMonitorDTO the ActivityMonitorDTO to create.
     * @return the {@link ResponseEntity} with status {@code 200 (Ok)}
     */
    @PostMapping("/activity-monitor")
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_ACTIVITY_MONITOR_VIEW + "\")")
    public ResponseEntity<Void> create(@RequestBody ActivityMonitorDTO activityMonitorDTO) {
        activityMonitorDTO.setLogin(getCurrentLoggedUserLogin());
        activityMonitorService.save(activityMonitorDTO);
        return ResponseEntity.ok(null);
    }

    /**
     * {@code GET  /activity-monitor}  : Get all activity notification for current logged-in user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of activityMonitor in body.
     */
    @GetMapping("/activity-monitor")
    @PreAuthorize("hasAuthority(\"" + FLEX_SYS_ACTIVITY_MONITOR_VIEW + "\")")
    public ResponseEntity<List<ActivityMonitorDTO>> getAllActivityMonitor(ActivityMonitorCriteria criteria, Pageable pageable) {
        criteria.setLogin((StringFilter) new StringFilter().setEquals(getCurrentLoggedUserLogin()));
        Page<ActivityMonitorDTO> page = activityMonitorQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    private String getCurrentLoggedUserLogin() {
        return userService.getCurrentUser().getLogin();
    }

}
