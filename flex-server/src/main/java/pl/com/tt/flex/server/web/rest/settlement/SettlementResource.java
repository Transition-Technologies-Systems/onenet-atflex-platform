package pl.com.tt.flex.server.web.rest.settlement;

import static org.springframework.data.domain.Sort.Direction.DESC;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SETTLEMENTS_NOTHING_TO_EXPORT;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.service.settlement.SettlementService;
import pl.com.tt.flex.server.service.settlement.SettlementViewQueryService;
import pl.com.tt.flex.server.service.settlement.dto.SettlementCriteria;
import pl.com.tt.flex.server.service.settlement.dto.SettlementViewDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.settlement.SettlementFileValidator;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class SettlementResource {

    public static final String ENTITY_NAME = "settlement";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final SettlementViewQueryService settlementViewQueryService;
    protected final SettlementService settlementService;
    protected final UserService userService;
    private final SettlementFileValidator settlementFileValidator;

    protected ResponseEntity<List<SettlementViewDTO>> getAllSettlements(SettlementCriteria criteria, Pageable pageable) {
        Page<SettlementViewDTO> page = settlementViewQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    protected ResponseEntity<SettlementViewDTO> getSettlementView(Long id) {
        return ResponseEntity.ok(settlementService.getSettlementView(id));
    }

    protected ResponseEntity<SettlementViewDTO> getSettlementView(Long id, Long fspId) {
        return ResponseEntity.ok(settlementService.getSettlementView(id, fspId));
    }

    protected ResponseEntity<FileDTO> exportAllSettlements(SettlementCriteria criteria, Pageable pageable, Screen screen, boolean isOnlyDisplayedData) throws IOException {
        int size = (int) settlementViewQueryService.countByCriteria(criteria);
        if (size == 0) {
            throw new ObjectValidationException("Nothing to export", SETTLEMENTS_NOTHING_TO_EXPORT);
        }
        PageRequest pageRequest = PageRequest.of(0, size, getSortOrDefault(pageable));
        Page<SettlementViewDTO> page = settlementViewQueryService.findByCriteria(criteria, pageRequest);
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        Pair<Instant, Instant> acceptedDeliveryPeriod = Pair.of(criteria.getAcceptedDeliveryPeriodFrom().getGreaterThanOrEqual(), criteria.getAcceptedDeliveryPeriodTo().getLessThanOrEqual());
        FileDTO file = settlementService.exportSettlementsToFile(page.getContent(), langKey, isOnlyDisplayedData, screen, acceptedDeliveryPeriod);
        return ResponseEntity.ok().body(file);
    }

    protected void importSettlement(MultipartFile[] multipartFiles, boolean force) throws ObjectValidationException, IOException, ImportDataException {
        settlementFileValidator.checkValid(multipartFiles, force);
        settlementService.importSettlementUpdates(multipartFiles);
    }

    private Sort getSortOrDefault(Pageable pageable) {
        return Optional.of(pageable.getSort())
            .filter(Sort::isSorted)
            .orElse(Sort.by(DESC, "id"));
    }
}
