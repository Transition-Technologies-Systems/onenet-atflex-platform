package pl.com.tt.flex.server.web.rest.settlement;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_ADMIN_SETTLEMENT_VIEW;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_SETTLEMENT_VIEW;
import static pl.com.tt.flex.model.security.permission.Role.ROLE_BALANCING_SERVICE_PROVIDER;
import static pl.com.tt.flex.server.domain.screen.enumeration.Screen.USER_SETTLEMENT;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.service.filter.StringFilter;
import lombok.extern.slf4j.Slf4j;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.service.settlement.SettlementService;
import pl.com.tt.flex.server.service.settlement.SettlementViewQueryService;
import pl.com.tt.flex.server.service.settlement.dto.SettlementCriteria;
import pl.com.tt.flex.server.service.settlement.dto.SettlementViewDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.settlement.SettlementFileValidator;

@Slf4j
@RestController
@RequestMapping("/api/user/settlements")
public class SettlementResourceUser extends SettlementResource {

    public SettlementResourceUser(SettlementViewQueryService settlementViewQueryService, SettlementService settlementService,
                                  UserService userService, SettlementFileValidator settlementFileValidator) {
        super(settlementViewQueryService, settlementService, userService, settlementFileValidator);
    }

    @GetMapping
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SETTLEMENT_VIEW + "\")")
    public ResponseEntity<List<SettlementViewDTO>> getAllSettlements(SettlementCriteria criteria, Pageable pageable) {
        log.debug("FLEX-USER - REST request to get Settlements by criteria: {}", criteria);
        filterForCurrentUser(criteria);
        return super.getAllSettlements(criteria, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SETTLEMENT_VIEW + "\")")
    public ResponseEntity<SettlementViewDTO> getSettlementView(@PathVariable Long id) {
        log.debug("FLEX-USER - REST request to get Settlement view by id: {}", id);
        return super.getSettlementView(id, getCurrentUserFspId());
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SETTLEMENT_VIEW + "\")")
    public ResponseEntity<FileDTO> exportAllSettlements(SettlementCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-USER - REST request to export Settlements by criteria: {}", criteria);
        filterForCurrentUser(criteria);
        return super.exportAllSettlements(criteria, pageable, USER_SETTLEMENT, false);
    }

    @GetMapping("/export/displayed-data")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SETTLEMENT_VIEW + "\")")
    public ResponseEntity<FileDTO> exportAllSettlementsDisplayedData(SettlementCriteria criteria, Pageable pageable) throws IOException {
        log.debug("FLEX-USER - REST request to export Settlements displayed data by criteria: {}", criteria);
        filterForCurrentUser(criteria);
        return super.exportAllSettlements(criteria, pageable, USER_SETTLEMENT, true);
    }

    private void filterForCurrentUser(SettlementCriteria criteria) {
        if (isCurrentUserBsp()) {
            criteria.setBspCompanyName(getCompanyNameFilter());
        } else {
            criteria.setFspId(getFspIdFilter());
        }
    }

    private LongFilter getFspIdFilter() {
        LongFilter fspIdFilter = new LongFilter();
        fspIdFilter.setEquals(getCurrentUserFspId());
        return fspIdFilter;
    }

    private StringFilter getCompanyNameFilter() {
        StringFilter companyNameFilter = new StringFilter();
        companyNameFilter.setEquals(getCurrentUserCompanyName());
        return companyNameFilter;
    }

    private Long getCurrentUserFspId() {
        return Optional.ofNullable(userService.getCurrentUser().getFsp()).map(FspEntity::getId).orElse(null);
    }

    private String getCurrentUserCompanyName() {
        return Optional.ofNullable(userService.getCurrentUserFetchFsp().getFsp()).map(FspEntity::getCompanyName).orElse("");
    }

    private boolean isCurrentUserBsp() {
        return userService.getCurrentUser().hasRole(ROLE_BALANCING_SERVICE_PROVIDER);
    }

}
