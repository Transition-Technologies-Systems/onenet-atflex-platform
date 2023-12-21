package pl.com.tt.flex.server.web.rest.fsp;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.service.fsp.FspQueryService;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.fsp.dto.FspCriteria;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_BSP_VIEW;
import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_LOGIN;
import static pl.com.tt.flex.server.domain.screen.enumeration.Screen.USER_BSP;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.FSP_NOTHING_TO_EXPORT;

/**
 * REST controller for managing {@link FspEntity} for FLEX-USER web module - Fsp is created with new User as a result of accepted FspUserRegistration
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class FspResourceUser {

    public static final String ENTITY_NAME = "fsp";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final FspQueryService fspQueryService;

    private final SchedulingUnitService schedulingUnitService;

    private final UserService userService;

    private final FlexPotentialService flexPotentialService;

    private final FspService fspService;

    private final UnitService unitService;

    public FspResourceUser(FspQueryService fspQueryService, SchedulingUnitService schedulingUnitService, UserService userService,
                           FlexPotentialService flexPotentialService, FspService fspService, UnitService unitService) {
        this.fspQueryService = fspQueryService;
        this.schedulingUnitService = schedulingUnitService;
        this.userService = userService;
        this.flexPotentialService = flexPotentialService;
        this.fspService = fspService;
        this.unitService = unitService;
    }

    /**
     * {@code GET  /fsps/get-bsps} : get all the bsps (fsp with role ROLE_BALANCING_SERVICE_PROVIDER).
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of bsps in body.
     */
    @GetMapping("/fsps/get-bsps")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_BSP_VIEW + "\")")
    public ResponseEntity<List<FspDTO>> getAllBsps(FspCriteria criteria, Pageable pageable) {
        log.debug("REST request to get all the bsps (fsp with role ROLE_BALANCING_SERVICE_PROVIDER) by criteria");
        criteria.setDeleted((BooleanFilter) new BooleanFilter().setEquals(false));
        criteria.setRole((FspCriteria.RoleFilter) new FspCriteria.RoleFilter().setEquals(Role.ROLE_BALANCING_SERVICE_PROVIDER));
        Page<FspDTO> page = fspQueryService.findByCriteria(criteria, pageable);
        UserEntity currentUser = userService.getCurrentUser();
        if (currentUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER) || currentUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            setFlagsForAllBspsInformingAboutConnectionWithFsp(page, currentUser);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /fsps/export/all} : export all fsps data to file.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_BSP_VIEW + "\")")
    @GetMapping("/fsps/get-bsps/export/all")
    public ResponseEntity<FileDTO> exportAllFsps(FspCriteria criteria, Pageable pageable) throws IOException {
        log.debug("REST request to export fsps");
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        criteria.setDeleted((BooleanFilter) new BooleanFilter().setEquals(false));
        criteria.setRole((FspCriteria.RoleFilter) new FspCriteria.RoleFilter().setEquals(Role.ROLE_BALANCING_SERVICE_PROVIDER));
        int size = (int) fspQueryService.countByCriteria(criteria);
        if (size == 0) {
            throw new ObjectValidationException("Nothing to export", FSP_NOTHING_TO_EXPORT);
        }
        PageRequest pageRequest = PageRequest.of(0, size, pageable.getSort());
        Page<FspDTO> fspDTOPage = fspQueryService.findByCriteria(criteria, pageRequest);
        return ResponseEntity.ok().body(fspService.exportFspsToFile(fspDTOPage.getContent(), langKey, false, USER_BSP));
    }

    /**
     * {@code GET  /fsps/export/displayed-data} : export fsps to file.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_BSP_VIEW + "\")")
    @GetMapping("/fsps/get-bsps/export/displayed-data")
    public ResponseEntity<FileDTO> exportDisplayedFsps(FspCriteria criteria, Pageable pageable) throws IOException {
        log.debug("REST request to export fsps");
        String langKey = userService.getLangKeyForCurrentLoggedUser();
        criteria.setDeleted((BooleanFilter) new BooleanFilter().setEquals(false));
        criteria.setRole((FspCriteria.RoleFilter) new FspCriteria.RoleFilter().setEquals(Role.ROLE_BALANCING_SERVICE_PROVIDER));
        int size = (int) fspQueryService.countByCriteria(criteria);
        if (size == 0) {
            throw new ObjectValidationException("Nothing to export", FSP_NOTHING_TO_EXPORT);
        }
        PageRequest pageRequest = PageRequest.of(0, size, pageable.getSort());
        Page<FspDTO> fspDTOPage = fspQueryService.findByCriteria(criteria, pageRequest);
        return ResponseEntity.ok().body(fspService.exportFspsToFile(fspDTOPage.getContent(), langKey, true, USER_BSP));
    }

    /**
     * Ustawienie dla kazdego z BSP flagi informujacej wskazanego FSP/A:
     * - czy jakis z jego DERow jest dolaczony do przynajmniej jednego SchedulingUnit tego BSP (jesli true to wiersz ma kolor zielony)
     * - czy ma mozliwosc dolaczenia przynajmniej jednego z swoich DERow do SchedulingUnit tego BSP (jesli true to w wierszu pojawia sie przycisk do utworzenia propozycji)
     */
    private void setFlagsForAllBspsInformingAboutConnectionWithFsp(Page<FspDTO> bsps, UserEntity fspUser) {
        Long fspId = fspUser.getFsp().getId();
        bsps.stream().forEach(bsp -> {
            bsp.setFspJoinedWithBspBySchedulingUnit(schedulingUnitService.isFspJoinedWithBspBySchedulingUnit(fspId, bsp.getId()));
        });
        if (fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER)) {
            bsps.stream().forEach(bsp -> {
                boolean isFspJoinedWithOtherBsp = schedulingUnitService.isFspJoinedWithOtherBspBySchedulingUnit(fspId, bsp.getId());
                boolean isAtLeastOneDerOfFspBalanced = isAtLeastOneDerOfFspBalancedByRegisteredFlexPotentialProduct(fspId);
                bsp.setCanFspJoinToBspSchedulingUnits(!isFspJoinedWithOtherBsp && isAtLeastOneDerOfFspBalanced);
            });
        } else if (fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            bsps.stream().forEach(bsp -> {
                bsp.setCanFspJoinToBspSchedulingUnits(!schedulingUnitService.isAllFspaSubportfoliosJoinedWithOtherBspBySchedulingUnit(fspId, bsp.getId()) || unitService.existsByFspIdAndNoSubportfolioAndNoSchedulingUnit(fspId));
            });
        }
    }

    /**
     * Aby FSP mógł wysłać propozycję DERa do jednostki grafikowej musi najpierw uzyskać "Flex Register" na produkt bilansujący,
     * czyli taki, który posiada flagę "Balancing" ustawioną na "Yes". Czyli zanim FSP będzie mógł wysłać DERa, to na ten DER
     * powinno istnieć Flexibility Potential z atrybutami "Prod. preq." i "Stat. grid preq." ustawionymi na "Yes", czyli jeżeli
     * ma status Flex Register(jest w oknie "Registered flexibility potentials"). Dopiero wtedy pojawiają się ikony "plusa" w tabelach
     * w oknach "BSP" oraz "Scheduling units" oraz przycisk "Propose DER" w oknie szczegółów "Scheduling units".
     */
    private boolean isAtLeastOneDerOfFspBalancedByRegisteredFlexPotentialProduct(Long fspId) {
        return flexPotentialService.isAtLeastOneDerOfFspBalancedByRegisteredFlexPotentialProduct(fspId);
    }

    /**
     * {@code GET  /fsps/get-fsp-id} : get fsp id.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the fsp id.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_LOGIN + "\")")
    @GetMapping("/fsps/get-fsp-id")
    public ResponseEntity<Long> getFspIdForUserLogin() {
        log.debug("REST request to get FSP ID for current user: ");
        Optional<UserDTO> maybyUser = userService.getCurrentUserDTO();
        if (maybyUser.isEmpty()) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(maybyUser.get().getFspId());
    }
}
