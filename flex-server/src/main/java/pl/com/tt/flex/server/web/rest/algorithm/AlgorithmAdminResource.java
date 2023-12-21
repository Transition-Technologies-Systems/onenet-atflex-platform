package pl.com.tt.flex.server.web.rest.algorithm;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_AGNO_ALGORITHM_START;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_ALGORITHM_EVALUATIONS;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_ALGORITHM_EVALUATION_OFFERS;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_ALGORITHM_STOP;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_DANO_ALGORITHM_START;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_PBCM_ALGORITHM_START;
import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType.BM;
import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType.DANO;
import static pl.com.tt.flex.model.service.dto.algorithm.AlgorithmType.PBCM;
import static pl.com.tt.flex.server.config.Constants.FLEX_ADMIN_APP_NAME;
import static pl.com.tt.flex.server.dataexport.exporter.offer.detail.enumeration.LevelOfDetail.STANDARD_DETAIL_SHEET;
import static pl.com.tt.flex.server.domain.email.enumeration.EmailType.OFFERS_USED_IN_ALGORITHM_EXPORT;
import static pl.com.tt.flex.server.domain.screen.enumeration.Screen.ADMIN_BIDS_EVALUATION;

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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationConfigDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmEvaluationViewDTO;
import pl.com.tt.flex.model.service.dto.algorithm.AlgorithmStartDTO;
import pl.com.tt.flex.model.service.dto.auction.offer.AuctionOfferViewDTO;
import pl.com.tt.flex.model.service.dto.file.FileContentDTO;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.domain.algorithm.AlgorithmEvaluationEntity;
import pl.com.tt.flex.server.service.algorithm.AlgorithmEvaluationQueryService;
import pl.com.tt.flex.server.service.algorithm.AlgorithmEvaluationService;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.capacity.CapacityAgnoAlgorithmResultsService;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.capacity.CapacityAgnoAlgorithmService;
import pl.com.tt.flex.server.service.algorithm.agnoAlgorithm.eneregy.EnergyAgnoAlgorithmService;
import pl.com.tt.flex.server.service.algorithm.danoAlgorithm.DanoAlgorithmResultsService;
import pl.com.tt.flex.server.service.algorithm.danoAlgorithm.EnergyDanoAlgorithmService;
import pl.com.tt.flex.server.service.algorithm.dto.AlgorithmEvaluationCriteria;
import pl.com.tt.flex.server.service.algorithm.view.AlgorithmEvaluationViewQueryService;
import pl.com.tt.flex.server.service.algorithm.view.dto.AlgorithmEvaluationViewCriteria;
import pl.com.tt.flex.server.service.auction.offer.AuctionOfferService;
import pl.com.tt.flex.server.service.auction.offer.dto.AuctionOfferViewCriteria;
import pl.com.tt.flex.server.service.mail.dto.NotificationResultDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.ZipUtil;
import pl.com.tt.flex.server.validator.algorithm.agnoAlgorithm.AgnoAlgorithmValidator;
import pl.com.tt.flex.server.validator.algorithm.agnoAlgorithm.AlgorithmImportValidator;

@Slf4j
@RestController
@RequestMapping("/api/admin/algorithm")
public class AlgorithmAdminResource extends AlgorithmResource {

    private final AlgorithmEvaluationService algorithmEvaluationService;
    private final AlgorithmEvaluationQueryService algorithmEvaluationQueryService;
    private final AlgorithmEvaluationViewQueryService viewQueryService;
    private final CapacityAgnoAlgorithmResultsService capacityAgnoAlgorithmResultsService;
    private final DanoAlgorithmResultsService danoAlgorithmResultsService;
    private final UserService userService;
    private final AgnoAlgorithmValidator validator;
    private final AlgorithmImportValidator algorithmImportValidator;
    private final AuctionOfferService auctionOfferService;
    private final EnergyAgnoAlgorithmService agnoAlgorithmService;
    private final CapacityAgnoAlgorithmService pbcmAlgorithmService;
    private final EnergyDanoAlgorithmService danoAlgorithmService;

    public AlgorithmAdminResource(AlgorithmEvaluationService algorithmEvaluationService, AlgorithmEvaluationViewQueryService viewQueryService,
                                  CapacityAgnoAlgorithmResultsService capacityAgnoAlgorithmResultsService, UserService userService,
                                  AlgorithmEvaluationQueryService algorithmEvaluationQueryService, DanoAlgorithmResultsService danoAlgorithmResultsService,
                                  AgnoAlgorithmValidator validator, AlgorithmImportValidator algorithmImportValidator,
                                  AuctionOfferService auctionOfferService, EnergyAgnoAlgorithmService agnoAlgorithmService,
                                  CapacityAgnoAlgorithmService pbcmAlgorithmService, EnergyDanoAlgorithmService danoAlgorithmService) {
        this.algorithmEvaluationService = algorithmEvaluationService;
        this.viewQueryService = viewQueryService;
        this.algorithmEvaluationQueryService = algorithmEvaluationQueryService;
        this.capacityAgnoAlgorithmResultsService = capacityAgnoAlgorithmResultsService;
        this.danoAlgorithmResultsService = danoAlgorithmResultsService;
        this.userService = userService;
        this.validator = validator;
        this.algorithmImportValidator = algorithmImportValidator;
        this.auctionOfferService = auctionOfferService;
        this.agnoAlgorithmService = agnoAlgorithmService;
        this.pbcmAlgorithmService = pbcmAlgorithmService;
        this.danoAlgorithmService = danoAlgorithmService;
    }


    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_AGNO_ALGORITHM_START + "\")")
    @PostMapping("/evaluations/run-agno-algorithm")
    public ResponseEntity<Void> runAgnoAlgorithm(@RequestBody AlgorithmStartDTO algStartDTO) throws ObjectValidationException, IOException {
        log.debug("{} - REST request to start agno algorithm with config : {}", FLEX_ADMIN_APP_NAME, algStartDTO);
        agnoAlgorithmService.startAlgorithm(new AlgorithmEvaluationConfigDTO(algStartDTO, BM));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_PBCM_ALGORITHM_START + "\")")
    @PostMapping("/evaluations/run-pbcm-algorithm")
    public ResponseEntity<Void> runPbcmAlgorithm(@RequestBody AlgorithmEvaluationConfigDTO algStartDTO) throws ObjectValidationException, IOException {
        log.debug("{} - REST request to start pbcm algorithm with config : {}", FLEX_ADMIN_APP_NAME, algStartDTO);
        pbcmAlgorithmService.startAlgorithm(new AlgorithmEvaluationConfigDTO(algStartDTO, PBCM));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_DANO_ALGORITHM_START + "\")")
    @PostMapping("/evaluations/run-dano-algorithm")
    public ResponseEntity<Void> runDanoAlgorithm(@RequestBody AlgorithmEvaluationConfigDTO algStartDTO) throws ObjectValidationException, IOException {
        log.debug("{} - REST request to start dano algorithm with config : {}", FLEX_ADMIN_APP_NAME, algStartDTO);
        danoAlgorithmService.startAlgorithm(new AlgorithmEvaluationConfigDTO(algStartDTO, DANO));
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_STOP + "\")")
    @PostMapping("/evaluations/cancel-evaluation/{evaluationId}")
    public ResponseEntity<Void> cancelAlgorithm(@PathVariable long evaluationId) throws ObjectValidationException {
        log.debug("{} - REST request to cancel algorithm with id : {}", FLEX_ADMIN_APP_NAME, evaluationId);
        validator.checkCancel(evaluationId);
        algorithmEvaluationService.cancelAlgorithm(evaluationId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/evaluations")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATIONS + "\")")
    public ResponseEntity<List<AlgorithmEvaluationDTO>> getAllAlgorithmEvaluations(AlgorithmEvaluationCriteria criteria, Pageable pageable) {
        log.debug("{} - REST request to get AlgorithmEvaluations by criteria: {}", FLEX_ADMIN_APP_NAME, criteria);
        Page<AlgorithmEvaluationDTO> page = algorithmEvaluationQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/evaluations/view")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATIONS + "\")")
    public ResponseEntity<List<AlgorithmEvaluationViewDTO>> getAllAlgorithmEvaluationsView(AlgorithmEvaluationViewCriteria criteria, Pageable pageable) {
        log.debug("{} - REST request to get AlgorithmEvaluations by criteria: {}", FLEX_ADMIN_APP_NAME, criteria);
        Page<AlgorithmEvaluationViewDTO> page = viewQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/evaluations/pbcm/parse-results/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATIONS + "\")")
    public ResponseEntity<Void> parsePcbmAlgorithmResults(@PathVariable Long id) throws ImportDataException, IOException, ObjectValidationException {
        log.debug("REST request to parse PCBM files for algorithm evaluation: {}", id);
        AlgorithmEvaluationEntity algorithmEvaluationEntity = algorithmEvaluationService.getAlgorithmEvaluationEntity(id);
        List<FileDTO> fileDTOS = ZipUtil.zipToFiles(algorithmEvaluationEntity.getOutputFilesZip());
        algorithmImportValidator.checkIfFilesExist(fileDTOS);
        capacityAgnoAlgorithmResultsService.parsePbcmAlgorithmResults(fileDTOS, id, userService.getLangKeyForCurrentLoggedUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/evaluations/dano/parse-results/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATIONS + "\")")
    public ResponseEntity<Void> parseDanoAlgorithmResults(@PathVariable Long id) throws ImportDataException, IOException, ObjectValidationException {
        log.debug("REST request to parse DANO files for algorithm evaluation: {}", id);
        AlgorithmEvaluationEntity algorithmEvaluationEntity = algorithmEvaluationService.getAlgorithmEvaluationEntity(id);
        List<FileDTO> fileDTOS = ZipUtil.zipToFiles(algorithmEvaluationEntity.getOutputFilesZip());
        algorithmImportValidator.checkIfFilesExist(fileDTOS);
        danoAlgorithmResultsService.parseDanoAlgorithmResults(fileDTOS, id, userService.getLangKeyForCurrentLoggedUser());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/offers/algorithm/download/input/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATION_OFFERS + "\")")
    public ResponseEntity<FileDTO> downloadInputFiles(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to download input file for algorithm evaluation: {}", id);
        FileDTO inputFilesZip = algorithmEvaluationService.findInputFilesZip(id);
        return ResponseEntity.ok().body(inputFilesZip);
    }

    @GetMapping("/offers/algorithm/download/output/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATION_OFFERS + "\")")
    public ResponseEntity<FileDTO> downloadOutputFiles(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to download output file for algorithm evaluation: {}", id);
        FileDTO outputFilesZip = algorithmEvaluationService.findOutputFilesZip(id);
        return ResponseEntity.ok().body(outputFilesZip);
    }

    @GetMapping("/offers/algorithm/get-logs/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATION_OFFERS + "\")")
    public ResponseEntity<List<FileContentDTO>> downloadLogFiles(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to get log files for algorithm evaluation: {}", id);
        List<FileContentDTO> logFiles = algorithmEvaluationService.findLogFiles(id);
        return ResponseEntity.ok().body(logFiles);
    }

    @GetMapping("/{algorithmEvaluationId}/offers")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATION_OFFERS + "\")")
    public ResponseEntity<List<AuctionOfferViewDTO>> getOffersUsedInAlgorithm(@PathVariable Long algorithmEvaluationId, AuctionOfferViewCriteria criteria, Pageable pageable) {
        log.debug("{} - REST request to get AuctionOfferViews by algorithmEvaluationId: {}", FLEX_ADMIN_APP_NAME, algorithmEvaluationId);
        Page<AuctionOfferViewDTO> page = algorithmEvaluationService.findOffersUsedInAlgorithmByCriteria(algorithmEvaluationId, criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{algorithmEvaluationId}/offers/export")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATION_OFFERS + "\")")
    public ResponseEntity<FileDTO> exportOffersUsedInAlgorithm(@PathVariable Long algorithmEvaluationId, AuctionOfferViewCriteria criteria) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export offers used in algorithm evaluation: {}", algorithmEvaluationId);
        List<AuctionOfferViewDTO> auctionOfferViewDTOS = algorithmEvaluationService.findOffersUsedInAlgorithmByCriteria(algorithmEvaluationId, criteria);
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        return ResponseEntity.ok().body(auctionOfferService.exportOffersToFile(auctionOfferViewDTOS, langKey, ADMIN_BIDS_EVALUATION, STANDARD_DETAIL_SHEET));
    }

    @GetMapping("/evaluation/{algEvaluationId}/results")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATION_OFFERS + "\")")
    public ResponseEntity<FileDTO> getResultsFile(@PathVariable Long algEvaluationId) throws IOException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to download results file for algorithm evaluation: {}", algEvaluationId);
        FileDTO file = algorithmEvaluationService.generateAgnoResultsFile(algEvaluationId);
        return ResponseEntity.ok().body(file);
    }

    @GetMapping("/{algorithmEvaluationId}/offers/export/email")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATION_OFFERS + "\")")
    public ResponseEntity<NotificationResultDTO> exportOffersUsedInAlgorithmToEmail(@PathVariable Long algorithmEvaluationId, AuctionOfferViewCriteria criteria) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export offers used in algorithm evaluation: {} and send by email", algorithmEvaluationId);
        List<AuctionOfferViewDTO> auctionOfferViewDTOS = algorithmEvaluationService.findOffersUsedInAlgorithmByCriteria(algorithmEvaluationId, criteria);
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        NotificationResultDTO notificationResultDTO = auctionOfferService.exportOffersToFileAndSendEmail(auctionOfferViewDTOS, langKey, ADMIN_BIDS_EVALUATION, STANDARD_DETAIL_SHEET, OFFERS_USED_IN_ALGORITHM_EXPORT);
        return ResponseEntity.ok(notificationResultDTO);
    }

    @GetMapping("/evaluation/{algEvaluationId}/results/email")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_ALGORITHM_EVALUATION_OFFERS + "\")")
    public ResponseEntity<NotificationResultDTO> getResultsFileByEmail(@PathVariable Long algEvaluationId) throws IOException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to email results file for algorithm evaluation: {}", algEvaluationId);
        NotificationResultDTO notificationResultDTO = algorithmEvaluationService.generateAgnoResultsFileAndSendEmail(algEvaluationId);
        return ResponseEntity.ok(notificationResultDTO);
    }
}
