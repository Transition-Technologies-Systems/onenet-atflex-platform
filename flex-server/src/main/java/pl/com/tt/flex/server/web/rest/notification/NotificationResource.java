package pl.com.tt.flex.server.web.rest.notification;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_VIEW_NOTIFICATION;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_VIEW_NOTIFICATION;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.server.service.notification.NotificationService;
import pl.com.tt.flex.server.service.notification.NotificationUserQueryService;
import pl.com.tt.flex.server.service.notification.dto.NotificationCriteria;
import pl.com.tt.flex.server.service.notification.dto.NotificationDTO;
import pl.com.tt.flex.server.service.user.UserService;

@Slf4j
@RestController
@RequestMapping("/api")
public class NotificationResource {

    private static final String ENTITY_NAME = "flexserverNotification";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final NotificationService notificationService;

    private final NotificationUserQueryService notificationUserQueryService;

    private final UserService userService;

    public NotificationResource(final NotificationService notificationService, final NotificationUserQueryService notificationUserQueryService, final UserService userService) {
        this.notificationService = notificationService;
        this.notificationUserQueryService = notificationUserQueryService;
        this.userService = userService;
    }

    /**
     * {@code GET  /notifications} : get all the notifications for current logged user.
     * websocket
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of notifications in body.
     */
    @GetMapping("/notifications")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_VIEW_NOTIFICATION + "\") or hasAuthority(\"" + FLEX_ADMIN_VIEW_NOTIFICATION + "\")")
    public ResponseEntity<List<NotificationDTO>> getAllNotificationsForCurrentUser(NotificationCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Notifications by criteria: {}", criteria);
        setCriteriaToFindNotificationsForCurrentLoggedUser(criteria);
        Page<NotificationDTO> page = notificationUserQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    private void setCriteriaToFindNotificationsForCurrentLoggedUser(NotificationCriteria criteria) {
        LongFilter notificationUserId = new LongFilter();
        notificationUserId.setEquals(userService.getCurrentUser().getId());
        criteria.setNotificationUserId(notificationUserId);
    }

    @GetMapping(value = "/notifications/count-not-read")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_VIEW_NOTIFICATION + "\") or hasAuthority(\"" + FLEX_ADMIN_VIEW_NOTIFICATION + "\")")
    public ResponseEntity<MinimalDTO<Long, Long>> countNotRead() {
        log.debug("REST request to get count of not read notifications");
        Long countNotRead = notificationService.countNotRead();
        return ResponseEntity.ok().body(new MinimalDTO<>(null, countNotRead));
    }

    @GetMapping("/notifications/mark-as-read")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_VIEW_NOTIFICATION + "\") or hasAuthority(\"" + FLEX_ADMIN_VIEW_NOTIFICATION + "\")")
    public ResponseEntity<Void> markAsRead(@RequestParam("ids") List<Long> notificationIds) {
        notificationService.markAsRead(notificationIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notifications/mark-all-as-read")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_VIEW_NOTIFICATION + "\") or hasAuthority(\"" + FLEX_ADMIN_VIEW_NOTIFICATION + "\")")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.ok().build();
    }
}
