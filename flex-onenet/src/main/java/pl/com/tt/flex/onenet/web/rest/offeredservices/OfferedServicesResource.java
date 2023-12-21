package pl.com.tt.flex.onenet.web.rest.offeredservices;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_OFFERED_SERVICES_VIEW;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.onenet.service.offeredservices.OfferedServicesQueryService;
import pl.com.tt.flex.onenet.service.offeredservices.OfferedServicesService;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServiceDTO;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServiceMinDTO;
import pl.com.tt.flex.onenet.service.offeredservices.dto.OfferedServicesCriteria;

@Slf4j
@RestController
@RequestMapping("/api/admin/offered-services")
public class OfferedServicesResource {

	private final OfferedServicesService offeredServicesService;
	private final OfferedServicesQueryService offeredServicesQueryService;

	public OfferedServicesResource(final OfferedServicesService offeredServicesService,
								   final OfferedServicesQueryService offeredServicesQueryService) {
		this.offeredServicesService = offeredServicesService;
		this.offeredServicesQueryService = offeredServicesQueryService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_OFFERED_SERVICES_VIEW + "\")")
	public ResponseEntity<List<OfferedServiceDTO>> getAll(OfferedServicesCriteria criteria, Pageable pageable) {
		log.debug("FLEX-ADMIN - REST request to get offered services for active onenet user");
		Page<OfferedServiceDTO> page = offeredServicesQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
		return ResponseEntity.ok().headers(headers).body(page.getContent());
	}

	@GetMapping("/download/file-schema/{id}")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_OFFERED_SERVICES_VIEW + "\")")
	public ResponseEntity<FileDTO> downloadFileSchema(@PathVariable Long id) {
		log.debug("FLEX-ADMIN - REST request to download file schema for offered service: {}", id);
		return ResponseEntity.ok().body(offeredServicesService.getFileSchema(id));
	}

	@GetMapping("/download/file-schema-sample/{id}")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_OFFERED_SERVICES_VIEW + "\")")
	public ResponseEntity<FileDTO> downloadFileSchemaSample(@PathVariable Long id) {
		log.debug("FLEX-ADMIN - REST request to download file schema sample for offered service: {}", id);
		return ResponseEntity.ok().body(offeredServicesService.getFileSchemaSample(id));
	}

	@GetMapping("/get-all-min")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_OFFERED_SERVICES_VIEW + "\")")
	public ResponseEntity<List<OfferedServiceMinDTO>> getAllMin() {
		log.debug("FLEX-ADMIN - REST request to get min dto of all offered services for active user");
		return ResponseEntity.ok().body(offeredServicesService.getAllMinDtoForActiveUser());
	}

}
