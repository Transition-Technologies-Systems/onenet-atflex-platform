package pl.com.tt.flex.server.web.rest.settlement;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_SETTLEMENT_MANAGE;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_SETTLEMENT_VIEW;
import static pl.com.tt.flex.server.domain.screen.enumeration.Screen.ADMIN_SETTLEMENT;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.dataimport.ImportDataException;
import pl.com.tt.flex.server.service.settlement.SettlementService;
import pl.com.tt.flex.server.service.settlement.SettlementViewQueryService;
import pl.com.tt.flex.server.service.settlement.dto.SettlementCriteria;
import pl.com.tt.flex.server.service.settlement.dto.SettlementEditDTO;
import pl.com.tt.flex.server.service.settlement.dto.SettlementViewDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.settlement.SettlementFileValidator;

@Slf4j
@RestController
@RequestMapping("/api/admin/settlements")
public class SettlementResourceAdmin extends SettlementResource {

    public SettlementResourceAdmin(SettlementViewQueryService settlementViewQueryService, SettlementService settlementService,
                                   UserService userService, SettlementFileValidator settlementFileValidator) {
        super(settlementViewQueryService, settlementService, userService, settlementFileValidator);
    }

    @GetMapping
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SETTLEMENT_VIEW + "\")")
    public ResponseEntity<List<SettlementViewDTO>> getAllSettlements(SettlementCriteria criteria, Pageable pageable) {
        log.debug("FLEX-ADMIN - REST request to get Settlements by criteria: {}", criteria);
        return super.getAllSettlements(criteria, pageable);
    }

    @GetMapping("/{id}/min")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SETTLEMENT_VIEW + "\")")
    public ResponseEntity<SettlementEditDTO> getSettlementMin(@PathVariable Long id) {
        log.debug("FLEX-ADMIN - REST request to get Settlement min by id: {}", id);
        return ResponseEntity.ok(settlementService.getSettlementMin(id));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SETTLEMENT_VIEW + "\")")
    public ResponseEntity<SettlementViewDTO> getSettlementView(@PathVariable Long id) {
        log.debug("FLEX-ADMIN - REST request to get Settlement view by id: {}", id);
        return super.getSettlementView(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SETTLEMENT_MANAGE + "\")")
    @ResponseStatus(HttpStatus.OK)
    public void updateSettlement(@PathVariable Long id, @RequestBody SettlementEditDTO settlementMin) {
        log.debug("FLEX-ADMIN - REST request to update Settlement by id: {}", id);
        settlementService.updateSettlement(id, settlementMin);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SETTLEMENT_VIEW + "\")")
    public ResponseEntity<FileDTO> exportAllSettlements(SettlementCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export Settlements by criteria: {}", criteria);
        return super.exportAllSettlements(criteria, pageable, ADMIN_SETTLEMENT, false);
    }

    @GetMapping("/export/displayed-data")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SETTLEMENT_VIEW + "\")")
    public ResponseEntity<FileDTO> exportAllSettlementsDisplayedData(SettlementCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-ADMIN - REST request to export Settlements displayed data by criteria: {}", criteria);
        return super.exportAllSettlements(criteria, pageable, ADMIN_SETTLEMENT, true);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SETTLEMENT_MANAGE + "\")")
    @ResponseStatus(HttpStatus.CREATED)
    public void importSettlement(@RequestPart(value = "file") MultipartFile[] multipartFiles, @RequestParam boolean force) throws ObjectValidationException, IOException, ImportDataException {
        log.debug("FLEX-ADMIN - REST request to import Settlement files with force: {}", force);
        super.importSettlement(multipartFiles, force);
    }

}
