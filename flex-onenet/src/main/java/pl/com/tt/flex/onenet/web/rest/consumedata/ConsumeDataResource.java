package pl.com.tt.flex.onenet.web.rest.consumedata;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
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
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.onenet.service.consumedata.ConsumeDataService;
import pl.com.tt.flex.onenet.service.consumedata.ConsumeDataViewQueryService;
import pl.com.tt.flex.onenet.service.consumedata.dto.ConsumeDataViewCriteria;
import pl.com.tt.flex.onenet.service.consumedata.dto.ConsumeDataViewDTO;

import java.util.List;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_CONSUME_DATA_VIEW;

@Slf4j
@RestController
@RequestMapping("/api/admin/consume-data")
public class ConsumeDataResource {
	private ConsumeDataService consumeDataService;
	private ConsumeDataViewQueryService consumeDataViewQueryService;

	public ConsumeDataResource(ConsumeDataService consumeDataService,
			ConsumeDataViewQueryService consumeDataViewQueryService) {
		this.consumeDataService = consumeDataService;
		this.consumeDataViewQueryService = consumeDataViewQueryService;
	}

	@GetMapping
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_CONSUME_DATA_VIEW + "\")")
	public ResponseEntity<List<ConsumeDataViewDTO>> getAll(ConsumeDataViewCriteria criteria, Pageable pageable) {
		log.debug("FLEX-ADMIN - REST request to get consumed data for active onenet user");
		Page<ConsumeDataViewDTO> page = consumeDataViewQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
		return ResponseEntity.ok().headers(headers).body(page.getContent());
	}

	@GetMapping("/download/file/{onsId}")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_CONSUME_DATA_VIEW + "\")")
	public ResponseEntity<FileDTO> downloadFile(@PathVariable String onsId) {
		log.debug("FLEX-ADMIN - REST request to download consumed data file for consumed data: {}", onsId);
		return ResponseEntity.ok().body(consumeDataService.getFile(onsId));
	}
}
