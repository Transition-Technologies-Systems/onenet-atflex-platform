package pl.com.tt.flex.server.web.rest.unit;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.LongFilter;
import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.unit.UnitQueryService;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitCriteria;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitGeoLocationMapper;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.validator.unit.UnitValidator;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link UnitEntity} for FLEX-USER web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class UnitResourceUser extends UnitResource {

    protected final FspService fspService;
    protected final SchedulingUnitService schedulingUnitService;


    public UnitResourceUser(UnitService unitService, UnitQueryService unitQueryService, UnitMapper unitMapper, UnitGeoLocationMapper unitGeoLocationMapper,
        UnitValidator unitValidator, UserService userService, FspService fspService, SchedulingUnitService schedulingUnitService) {
        super(unitService, unitQueryService, unitMapper, unitGeoLocationMapper, unitValidator, userService);
        this.fspService = fspService;
        this.schedulingUnitService = schedulingUnitService;
    }

    /**
     * {@code POST  /user/units} : FLEX-USER - Create a new unit.
     *
     * @param unitDTO the unitDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new unitDTO, or with status {@code 400 (Bad Request)} if the unit has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/units")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_UNIT_MANAGE + "\")")
    public ResponseEntity<UnitDTO> createUnit(@Valid @RequestBody UnitDTO unitDTO) throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-USER - REST request to save Unit : {}", unitDTO);
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new UnitResourceException("Current logged-in user not found"));
        unitDTO.setFspId(fspUser.getFspId());
        unitDTO.setCertified(false); //pole tylko do odczytu w flex-user
        return super.createUnit(unitDTO);
    }

    /**
     * {@code PUT  /admin/units} : Updates an existing unit.
     *
     * @param unitDTO the unitDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated unitDTO,
     * or with status {@code 400 (Bad Request)} if the unitDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the unitDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/units")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_UNIT_MANAGE + "\")")
    public ResponseEntity<UnitDTO> updateUnit(@Valid @RequestBody UnitDTO unitDTO) throws ObjectValidationException {
        log.debug("FLEX-USER - REST request to update Unit : {}", unitDTO);
        if (!unitValidator.isUnitBelongsToCurrentFspUser(unitDTO.getId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return super.updateUnit(unitDTO);
    }

    /**
     * {@code GET  /user/units} : FLEX-USER - get all the units for current logged in user depending on his role.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of units in body.
     */
    @GetMapping("/units")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_UNIT_VIEW + "\")")
    public ResponseEntity<List<UnitDTO>> getAllUnits(UnitCriteria criteria, Pageable pageable) {
        log.debug("REST request to get Units by criteria: {}", criteria);
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new UnitResourceException("Current logged-in user not found"));
        if (fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER) || fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            criteria.setFspId((LongFilter) new LongFilter().setEquals(fspUser.getFspId()));
        } else if (fspUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            criteria.setCertified((BooleanFilter) new BooleanFilter().setEquals(true));
        }
        Page<UnitDTO> page = unitQueryService.findByCriteria(criteria, pageable);
        if (fspUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            setFlagsForAllDersInformingIfBspCanInviteDer(page, fspUser);
        }
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * Ustawienie flagi dla kazdego DERow, informujacej aktualnie zalogowanego BSP, czy ma mozliwosc zaproszenia DERa do przynajmniej jednego z swoich SchedulingUnit
     *
     * Aby BSP mógł zaprosić DERa do jednostki grafikowej, to ten DER musi posiadać "Flex register" na produkt z flagą "Balancing".
     * Wtedy BSP po wejściu do okna "DERs" widzi "plusy" tylko przy DERach, które ten "Flex register" posiadają. To samo tyczy się przycisku "Invite DER" w oknie szczegółów DERa.
     */
    private void setFlagsForAllDersInformingIfBspCanInviteDer(Page<UnitDTO> ders, UserDTO bsp) {
        ders.stream().forEach(der -> {
                boolean canDerBeAddedToBspSchedulingUnits = schedulingUnitService.canDerBeAddedToBspSchedulingUnits(der.getId(), bsp.getFspId());
                der.setCanBspInviteFspDerToSchedulingUnit(canDerBeAddedToBspSchedulingUnits);
            }
        );
    }

    /**
     * User with role BSP has access to all Units.
     * Other Users has access to only its own Units
     */
    @GetMapping("/units/get-all")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_UNIT_VIEW + "\")")
    public ResponseEntity<List<UnitMinDTO>> getAllUnitsMinimal(UnitCriteria unitCriteria) {
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new UnitResourceException("Current logged-in user not found"));
        if (!fspUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            unitCriteria.setFspId((LongFilter) new LongFilter().setEquals(fspUser.getFspId()));
        }
        return super.getAllUnitsMinimal(unitCriteria);
    }

    /**
     * User with role BSP has access to all Units.
     * Other Users has access to only its own Units
     */
    @GetMapping("/units/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_UNIT_VIEW + "\")")
    public ResponseEntity<UnitDTO> getUnit(@PathVariable Long id) {
        log.debug("FLEX-USER - REST request to get Unit : {}", id);
        UserDTO currentFspUser = userService.getCurrentUserDTO().orElseThrow(() -> new UnitResourceException("Current logged-in user not found"));
        if (currentFspUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER) || unitValidator.isUnitBelongsToCurrentFspUser(id)) {
            return super.getUnit(id);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    /**
     * {@code DELETE  /units/:id} : delete the "id" unit.
     *
     * @param id the id of the unitDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/units/{id}")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_UNIT_DELETE + "\")")
    public ResponseEntity<Void> deleteUnit(@PathVariable Long id) throws ObjectValidationException {
        log.debug("FLEX-USER - REST request to delete Unit : {}", id);
        if (!unitValidator.isUnitBelongsToCurrentFspUser(id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return super.deleteUnit(id);
    }

    /**
     * {@code GET  /user/units/export/all} : export all units's to file by flex user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_UNIT_VIEW + "\")")
    @GetMapping("/units/export/all")
    public ResponseEntity<FileDTO> exportUnitsAll(UnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("REST request to export all units by User");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new UnitResourceException("Current logged-in user not found"));
        if (fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER) || fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            criteria.setFspId((LongFilter) new LongFilter().setEquals(fspUser.getFspId()));
        } else if (fspUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            criteria.setCertified((BooleanFilter) new BooleanFilter().setEquals(true));
        }
        return super.exportUnits(criteria, pageable, Screen.USER_UNITS, false);
    }

    /**
     * {@code GET  /user/units/export/displayed-data} : export displayed units's to file by flex user.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)}.
     */
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_UNIT_VIEW + "\")")
    @GetMapping("/units/export/displayed-data")
    public ResponseEntity<FileDTO> exportUnitsDisplayed(UnitCriteria criteria, Pageable pageable) throws IOException {
        log.debug("REST request to export all units by User");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new UnitResourceException("Current logged-in user not found"));
        if (fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER) || fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            criteria.setFspId((LongFilter) new LongFilter().setEquals(fspUser.getFspId()));
        } else if (fspUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            criteria.setCertified((BooleanFilter) new BooleanFilter().setEquals(true));
        }
        return super.exportUnits(criteria, pageable, Screen.USER_UNITS, true);
    }

    @GetMapping("units/get-all-for-subportfolio-modal-select")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_UNIT_MANAGE + "\")")
    public  ResponseEntity<List<UnitMinDTO>> getAllForSubportfolioModalSelect(@RequestParam(value = "subportfolioId", required = false) Long subportfolioId)
        throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-USER - REST request to get all Units by fspId and no subportfolio");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new UnitResourceException("Current logged-in user not found"));
        FspEntity fspEntity = fspService.findFspOfUser(fspUser.getId(), fspUser.getLogin()).orElseThrow(() -> new UnitResourceException("Cannot find fsp of current logged-in user"));
        return super.getAllForSubportfolioModalSelect(fspEntity.getId(), subportfolioId);
    }

    @GetMapping("/units/get-all-for-fsp")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_UNIT_MANAGE + "\")")
    public  ResponseEntity<List<UnitMinDTO>> getAllByFspId() throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-USER - REST request to get all Units for current logged in fsp");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new UnitResourceException("Current logged-in user not found"));
        FspEntity fspEntity = fspService.findFspOfUser(fspUser.getId(), fspUser.getLogin()).orElseThrow(() -> new UnitResourceException("Cannot find fsp of current logged-in user"));
        return super.getAllByFspId(fspEntity.getId());
    }
}
