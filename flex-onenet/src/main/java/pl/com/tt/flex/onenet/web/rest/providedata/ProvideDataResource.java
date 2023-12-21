package pl.com.tt.flex.onenet.web.rest.providedata;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_PROVIDE_DATA_SEND;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_PROVIDE_DATA_VIEW;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.onenet.service.consumedata.dto.ConsumeDataViewCriteria;
import pl.com.tt.flex.onenet.service.providedata.ProvideDataQueryService;
import pl.com.tt.flex.onenet.service.providedata.ProvideDataService;
import pl.com.tt.flex.onenet.service.providedata.dto.ProvideDataViewDTO;

@Slf4j
@RestController
@RequestMapping("/api/admin/provide-data")
public class ProvideDataResource {

	private final ProvideDataService provideDataService;
	private final ProvideDataQueryService provideDataQueryService;

	public ProvideDataResource(final ProvideDataService provideDataService, final ProvideDataQueryService provideDataQueryService) {
		this.provideDataService = provideDataService;
		this.provideDataQueryService = provideDataQueryService;
	}

	@PostMapping
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PROVIDE_DATA_SEND + "\")")
	public ResponseEntity<Void> provideData(@RequestPart(value = "file") MultipartFile multipartFile, String title,
											String description, String filename, String dataOfferingId, String code) throws IOException {
		provideDataService.provideData(multipartFile, title, description, filename, dataOfferingId, code);
		return ResponseEntity.ok().build();
	}

	@GetMapping
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PROVIDE_DATA_VIEW + "\")")
	public ResponseEntity<List<ProvideDataViewDTO>> getAll(ConsumeDataViewCriteria criteria, Pageable pageable) {
		log.debug("FLEX-ADMIN - REST request to get provide data for active onenet user");
		Page<ProvideDataViewDTO> page = provideDataQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
		return ResponseEntity.ok().headers(headers).body(page.getContent());
	}

	@GetMapping("/{id}/file")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PROVIDE_DATA_VIEW + "\")")
	public ResponseEntity<FileDTO> downloadFileSchema(@PathVariable Long id) {
		log.debug("FLEX-ADMIN - REST request to download file for provide data id: {}", id);
		return ResponseEntity.ok().body(provideDataService.getFile(id));
	}

}