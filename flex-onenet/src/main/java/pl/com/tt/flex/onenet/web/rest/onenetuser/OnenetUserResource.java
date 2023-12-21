package pl.com.tt.flex.onenet.web.rest.onenetuser;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_ONENET_USER_DELETE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_ONENET_USER_MANAGE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_ONENET_USER_VIEW;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.onenet.service.onenetuser.OnenetUserQueryService;
import pl.com.tt.flex.onenet.service.onenetuser.OnenetUserService;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetAuthDTO;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetUserCriteria;
import pl.com.tt.flex.onenet.service.onenetuser.dto.OnenetUserDTO;
import pl.com.tt.flex.onenet.web.rest.errors.ObjectValidationException;
import pl.com.tt.flex.onenet.web.rest.errors.OnenetSystemRequestException;

@Slf4j
@RestController
@RequestMapping("/api/admin/users")
public class OnenetUserResource {

	private final OnenetUserService onenetUserService;
	private final OnenetUserQueryService onenetUserQueryService;

	public OnenetUserResource(final OnenetUserService onenetUserService,
							  final OnenetUserQueryService onenetUserQueryService) {
		this.onenetUserService = onenetUserService;
		this.onenetUserQueryService = onenetUserQueryService;
	}

	@PostMapping("/add")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ONENET_USER_MANAGE + "\")")
	public ResponseEntity<OnenetUserDTO> addOnenetUser(@RequestBody OnenetAuthDTO onenetAuthDTO) throws OnenetSystemRequestException, ObjectValidationException {
		log.debug("FLEX-ADMIN - REST request to add onenet user by username: {}", onenetAuthDTO.getUsername());
		return ResponseEntity.ok(onenetUserService.addOnenetUser(onenetAuthDTO));
	}

	@GetMapping
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ONENET_USER_VIEW + "\")")
	public ResponseEntity<List<OnenetUserDTO>> getAll(OnenetUserCriteria criteria, Pageable pageable) {
		log.debug("FLEX-ADMIN - REST request to get onenet users by criteria: {}", criteria);
		Page<OnenetUserDTO> page = onenetUserQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
		return ResponseEntity.ok().headers(headers).body(page.getContent());
	}

	@PutMapping("/{id}/set-active")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ONENET_USER_MANAGE + "\")")
	public ResponseEntity<Void> setActive(@PathVariable Long id) throws ObjectValidationException {
		log.debug("FLEX-ADMIN - REST request to set onenet user with id {} as active", id);
		onenetUserService.setActiveUser(id);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{id}/remove")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ONENET_USER_DELETE + "\")")
	public ResponseEntity<Void> removeOnenetUser(@PathVariable Long id) throws ObjectValidationException {
		log.debug("FLEX-ADMIN - REST request to remove onenet user with id {}", id);
		onenetUserService.removeOnenetUser(id);
		return ResponseEntity.ok().build();
	}
}
