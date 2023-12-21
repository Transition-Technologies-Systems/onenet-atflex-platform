package pl.com.tt.flex.flex.agno.web.resource.kdm_model;

import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.flex.agno.common.errors.ObjectValidationException;
import pl.com.tt.flex.flex.agno.domain.kdm_model.KdmModelEntity;
import pl.com.tt.flex.flex.agno.service.kdm_model.KdmModelQueryService;
import pl.com.tt.flex.flex.agno.service.kdm_model.KdmModelService;
import pl.com.tt.flex.flex.agno.service.kdm_model.KdmModelTimestampFileService;
import pl.com.tt.flex.flex.agno.service.kdm_model.dto.KdmModelCriteria;
import pl.com.tt.flex.flex.agno.util.TimestampFileUtil;
import pl.com.tt.flex.flex.agno.validator.kdm_model.KdmModelTimestampFileValidator;
import pl.com.tt.flex.flex.agno.validator.kdm_model.KdmModelValidator;
import pl.com.tt.flex.flex.agno.web.resource.error.BadRequestAlertException;
import pl.com.tt.flex.model.service.dto.MinimalDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmAreaDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelDTO;
import pl.com.tt.flex.model.service.dto.kdm_model.KdmModelTimestampFileDTO;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link KdmModelEntity} for FLEX-ADMIN web module
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class KdmModelResource {

	public static final String ENTITY_NAME = "kdmModel";

	@Value("${jhipster.clientApp.name}")
	private String applicationName;

	private final KdmModelService kdmModelService;
	private final KdmModelQueryService kdmModelQueryService;
	private final KdmModelValidator kdmModelValidator;
	private final KdmModelTimestampFileService kdmModelTimestampFileService;
	private final KdmModelTimestampFileValidator kdmModelTimestampFileValidator;

	public KdmModelResource(KdmModelService kdmModelService,
			KdmModelQueryService kdmModelQueryService,
			KdmModelValidator kdmModelValidator,
			KdmModelTimestampFileService kdmModelTimestampFileService, KdmModelTimestampFileValidator kdmModelTimestampFileValidator) {
		this.kdmModelService = kdmModelService;
		this.kdmModelQueryService = kdmModelQueryService;
		this.kdmModelValidator = kdmModelValidator;
		this.kdmModelTimestampFileService = kdmModelTimestampFileService;
		this.kdmModelTimestampFileValidator = kdmModelTimestampFileValidator;
	}

	/**
	 * {@code POST  /kdm-models} : Create a new kdmModel.
	 *
	 * @param kdmModelDTO the kdm model to create.
	 * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new kdmModel, or with status {@code 400 (Bad Request)} if the kdmModel has already an ID.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PostMapping("/kdm-models")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KDM_MODEL_MANAGE + "\")")
	public ResponseEntity<KdmModelDTO> createKdmModel(@Valid @RequestBody KdmModelDTO kdmModelDTO) throws URISyntaxException, ObjectValidationException {
		log.debug("FLEX-ADMIN - REST request to save kdmModel : {}", kdmModelDTO);
		if (kdmModelDTO.getId() != null) {
			throw new BadRequestAlertException("A new kdmModel cannot already have an ID", ENTITY_NAME, "idexists");
		}
		kdmModelValidator.checkValid(kdmModelDTO);
		KdmModelDTO result = kdmModelService.save(kdmModelDTO);
		return ResponseEntity.created(new URI("/api/kdm-models/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
				.body(result);
	}

	/**
	 * {@code PUT  /kdm-models} : Updates an existing kdmModel.
	 *
	 * @param kdmModelDTO the kdmModelDTO to update.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated kdmModel,
	 * or with status {@code 400 (Bad Request)} if the kdmModel is not valid,
	 * or with status {@code 500 (Internal Server Error)} if the kdmModel couldn't be updated.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PutMapping("/kdm-models")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KDM_MODEL_MANAGE + "\")")
	public ResponseEntity<KdmModelDTO> updateKdmModel(@Valid @RequestBody KdmModelDTO kdmModelDTO) throws URISyntaxException, ObjectValidationException {
		log.debug("FLEX-ADMIN - REST request to update kdmModel : {}", kdmModelDTO);
		if (kdmModelDTO.getId() == null) {
			throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
		}
		kdmModelValidator.checkModifiable(kdmModelDTO);
		KdmModelDTO result = kdmModelService.updateNameAndLvFlag(kdmModelDTO);
		return ResponseEntity.ok()
				.headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, kdmModelDTO.getId().toString()))
				.body(result);
	}

	/**
	 * {@code GET  /kdm-models} : get all the kdmModel.
	 *
	 * @param pageable the pagination information.
	 * @param criteria the criteria which the requested entities should match.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of kdmModel in body.
	 */
	@GetMapping("/kdm-models")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KDM_MODEL_VIEW + "\")")
	public ResponseEntity<List<KdmModelDTO>> getAllKdmModels(KdmModelCriteria criteria, Pageable pageable) {
		log.debug("FLEX-ADMIN - REST request to get kdmModel by criteria: {}", criteria);
		Page<KdmModelDTO> page = kdmModelQueryService.findByCriteria(criteria, pageable);
		HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
		return ResponseEntity.ok().headers(headers).body(page.getContent());
	}

	/**
	 * {@code GET  /kdm-models/get-all}
	 *
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of kdmModel in body.
	 */
	@GetMapping("/kdm-models/get-all")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KDM_MODEL_VIEW + "\")")
	public ResponseEntity<List<KdmAreaDTO>> getAllMin() {
		log.debug("FLEX-ADMIN - REST request to get all kdmModels");
		List<KdmAreaDTO> kdmModels = kdmModelService.getAllKdmModels();
		if (kdmModels.isEmpty()) {
			ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok().body(kdmModels);
	}

	/**
	 * {@code GET  /kdm-models/get-all-min}
	 *
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of kdmModel in body.
	 */
	@GetMapping("/kdm-models/get-all-min")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KDM_MODEL_VIEW + "\")")
	public ResponseEntity<List<MinimalDTO<Long, String>>> getAll() {
		log.debug("FLEX-ADMIN - REST request to get all kdmModels");
		List<MinimalDTO<Long, String>> kdmModelsMin = kdmModelService.getAllKdmModelsMin();
		if (kdmModelsMin.isEmpty()) {
			ResponseEntity.noContent().build();
		}
		return ResponseEntity.ok().body(kdmModelsMin);
	}

	/**
	 * {@code GET /kdm-models/:id} : get the "id" kdmModelDTO.
	 *
	 * @param id the id of the kdmModelDTO to retrieve.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the kdmModelDTO, or with status {@code 404 (Not Found)}.
	 */
	@GetMapping("/kdm-models/{id}")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KDM_MODEL_VIEW + "\")")
	public ResponseEntity<KdmModelDTO> getKdmModel(@PathVariable Long id) {
		log.debug("FLEX-ADMIN - REST request to get kdmModel : {}", id);
		Optional<KdmModelDTO> kdmModelDTO = kdmModelService.findById(id);
		return ResponseUtil.wrapOrNotFound(kdmModelDTO);
	}


	/**
	 * {@code DELETE  /kdm-models/:id} : delete the "id" kdmModel.
	 *
	 * @param id the id of the kdmModel to delete.
	 * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
	 */
	@DeleteMapping("/kdm-models/{id}")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KDM_MODEL_DELETE + "\")")
	public ResponseEntity<Void> deleteKdmModel(@PathVariable Long id) throws ObjectValidationException {
		log.debug("FLEX-ADMIN - REST request to delete kdmModel : {}", id);
		kdmModelValidator.checkDeletable(id);
		kdmModelService.delete(id);
		return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
	}

	// --------------------  Dodawanie plików do słownika KDM_MODEL -------------------------------

	/**
	 * {@code GET /kdm-models/timestamps : get the list timestamp files for kdmModel.
	 *
	 * @param kdmModelId the id of the kdmModelDTO.
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the list of timestamps.
	 */
	@GetMapping("/kdm-models/timestamps")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KDM_MODEL_VIEW + "\")")
	public ResponseEntity<List<KdmModelTimestampFileDTO>> getAllKdmModelTimestamp(@RequestParam Long kdmModelId) {
		log.debug("FLEX-ADMIN - REST request to get kdmModel timestamps by kdmModelID: {}", kdmModelId);
		List<KdmModelTimestampFileDTO> list = kdmModelTimestampFileService.findAllByKdmModelId(kdmModelId);
		return ResponseEntity.ok().body(list);
	}

	/**
	 * {@code POST  /kdm-models/timestamps/verify} : Verify validity of given timestamp file for a particular kdmModel.
	 *
	 * @return the {@link ResponseEntity} with status {@code 200 (OK)} if given file is correct, or with status {@code 400 (Bad Request)} when validation fails.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PostMapping("/kdm-models/timestamps/verify")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KDM_MODEL_VIEW + "\")")
	public ResponseEntity<Void> checkTimestampFileValid(
			@RequestPart(value = "kdmModelId") String kdmModelId,
			@RequestPart(value = "file") MultipartFile file,
			@RequestPart(value = "kdmFileId", required = false) String id,
			@RequestPart(value = "timestamp") String timestamp) throws ObjectValidationException {
		kdmModelTimestampFileValidator.checkValid(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id, file, timestamp));
		return ResponseEntity.ok().build();
	}

	/**
	 * {@code PUT  /kdm-models/timestamps} : Update all timestamp files for a particular kdmModel.
	 *
	 * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new timestamp file DTO, or with status {@code 400 (Bad Request)} if the kdmModel has already an ID.
	 * @throws URISyntaxException if the Location URI syntax is incorrect.
	 */
	@PutMapping(value = "/kdm-models/timestamps")
	@PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_KDM_MODEL_MANAGE + "\")")
	public ResponseEntity<KdmModelDTO> addTimestampFiles(
			@RequestPart String kdmModelId,
			@RequestPart(value = "file1", required = false) MultipartFile file1,
			@RequestPart(value = "file2", required = false) MultipartFile file2,
			@RequestPart(value = "file3", required = false) MultipartFile file3,
			@RequestPart(value = "file4", required = false) MultipartFile file4,
			@RequestPart(value = "file5", required = false) MultipartFile file5,
			@RequestPart(value = "file6", required = false) MultipartFile file6,
			@RequestPart(value = "file7", required = false) MultipartFile file7,
			@RequestPart(value = "file8", required = false) MultipartFile file8,
			@RequestPart(value = "file9", required = false) MultipartFile file9,
			@RequestPart(value = "file10", required = false) MultipartFile file10,
			@RequestPart(value = "file11", required = false) MultipartFile file11,
			@RequestPart(value = "file12", required = false) MultipartFile file12,
			@RequestPart(value = "file13", required = false) MultipartFile file13,
			@RequestPart(value = "file14", required = false) MultipartFile file14,
			@RequestPart(value = "file15", required = false) MultipartFile file15,
			@RequestPart(value = "file16", required = false) MultipartFile file16,
			@RequestPart(value = "file17", required = false) MultipartFile file17,
			@RequestPart(value = "file18", required = false) MultipartFile file18,
			@RequestPart(value = "file19", required = false) MultipartFile file19,
			@RequestPart(value = "file20", required = false) MultipartFile file20,
			@RequestPart(value = "file21", required = false) MultipartFile file21,
			@RequestPart(value = "file22", required = false) MultipartFile file22,
			@RequestPart(value = "file23", required = false) MultipartFile file23,
			@RequestPart(value = "file24", required = false) MultipartFile file24,
			@RequestPart(value = "file2a", required = false) MultipartFile file2a,
			@RequestPart(value = "id1") String id1,
			@RequestPart(value = "id2") String id2,
			@RequestPart(value = "id3") String id3,
			@RequestPart(value = "id4") String id4,
			@RequestPart(value = "id5") String id5,
			@RequestPart(value = "id6") String id6,
			@RequestPart(value = "id7") String id7,
			@RequestPart(value = "id8") String id8,
			@RequestPart(value = "id9") String id9,
			@RequestPart(value = "id10") String id10,
			@RequestPart(value = "id11") String id11,
			@RequestPart(value = "id12") String id12,
			@RequestPart(value = "id13") String id13,
			@RequestPart(value = "id14") String id14,
			@RequestPart(value = "id15") String id15,
			@RequestPart(value = "id16") String id16,
			@RequestPart(value = "id17") String id17,
			@RequestPart(value = "id18") String id18,
			@RequestPart(value = "id19") String id19,
			@RequestPart(value = "id20") String id20,
			@RequestPart(value = "id21") String id21,
			@RequestPart(value = "id22") String id22,
			@RequestPart(value = "id23") String id23,
			@RequestPart(value = "id24") String id24,
			@RequestPart(value = "id2a") String id2a
	) throws URISyntaxException, ObjectValidationException {

		log.info("FLEX-ADMIN - REST request to update kdmModel timestamp files. KdmModelId= {}", kdmModelId);
		List<KdmModelTimestampFileDTO> kdmModelTimestampFileDTOS = new ArrayList<>();
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id1, file1, "1"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id4, file4, "4"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id3, file3, "3"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id2, file2, "2"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id5, file5, "5"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id6, file6, "6"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id7, file7, "7"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id8, file8, "8"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id9, file9, "9"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id10, file10, "10"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id11, file11, "11"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id12, file12, "12"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id13, file13, "13"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id14, file14, "14"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id15, file15, "15"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id16, file16, "16"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id17, file17, "17"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id18, file18, "18"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id19, file19, "19"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id20, file20, "20"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id21, file21, "21"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id22, file22, "22"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id23, file23, "23"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id24, file24, "24"));
		kdmModelTimestampFileDTOS.add(TimestampFileUtil.prepareTimestampFileDto(kdmModelId, id2a, file2a, "2a"));

		kdmModelTimestampFileService.updateAllTimestampsForKdmModel(kdmModelId, kdmModelTimestampFileDTOS);

		KdmModelDTO result = kdmModelService.findById(Long.parseLong(kdmModelId)).get();
		return ResponseEntity.created(new URI("/api/kdm-models/" + result.getId()))
				.headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
				.body(result);
	}
	// --------------------  Dodawanie plików do słownika KDM_MODEL -------------------------------

}