package pl.com.tt.flex.server.web.rest.schedulingUnit;

import com.google.common.collect.Sets;
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
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitProposalEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalType;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.potential.FlexPotentialService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitQueryProposalService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalCriteria;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalMinDTO;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitProposalMapper;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitProposalValidator;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitValidator;
import pl.com.tt.flex.server.validator.unit.UnitValidator;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link SchedulingUnitProposalEntity} for FLEX-USER web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class SchedulingUnitProposalResourceUser extends SchedulingUnitProposalResource {

    private final UnitValidator unitValidator;
    private final FlexPotentialService flexPotentialService;

    public SchedulingUnitProposalResourceUser(SchedulingUnitService schedulingUnitService, SchedulingUnitQueryProposalService schedulingUnitQueryProposalService,
        SchedulingUnitMapper schedulingUnitMapper, SchedulingUnitValidator schedulingUnitValidator, UserService userService, FspService fspService,
        SchedulingUnitProposalMapper schedulingUnitProposalMapper, SchedulingUnitProposalValidator schedulingUnitProposalValidator, UnitValidator unitValidator,
        SubportfolioService subportfolioService, FlexPotentialService flexPotentialService) {
        super(schedulingUnitService, schedulingUnitQueryProposalService, schedulingUnitMapper, schedulingUnitValidator, userService, schedulingUnitProposalMapper,
            schedulingUnitProposalValidator, fspService, subportfolioService);
        this.unitValidator = unitValidator;
        this.flexPotentialService = flexPotentialService;
    }

    /**
     * {@code GET  /scheduling-units/proposal/get-all} : get all the schedulingUnitProposals.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of schedulingUnits in body.
     */
    @GetMapping("/scheduling-units/proposal/get-all")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitProposalMinDTO>> getAllSchedulingUnitProposals(SchedulingUnitProposalCriteria criteria, Pageable pageable) {
        log.debug("FLEX-USER - REST request to get SchedulingUnitProposals by criteria: {}", criteria);
        UserDTO currentUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitResource.SchedulingUnitResourceException("Current logged-in user not found"));
        if (currentUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            criteria.setBspId((LongFilter) new LongFilter().setEquals(currentUser.getFspId()));
        } else if (currentUser.hasAnyRole(Sets.newHashSet(Role.ROLE_FLEX_SERVICE_PROVIDER, Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED))) {
            criteria.setFspId((LongFilter) new LongFilter().setEquals(currentUser.getFspId()));
        }
        Page<SchedulingUnitProposalMinDTO> page = schedulingUnitQueryProposalService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code POST  /scheduling-units/proposal/propose-der} : Create SchedulingUnitProposal by FSP/A to join its DER to BSP.
     * Bsp picks later, in another request, to which one of his Scheduling units DER will be join.
     * If the proposal already exists, it will be sent again.
     *
     * @param schedulingUnitProposalDTO the schedulingUnitProposalDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new schedulingUnitProposalDTO,
     * or with status {@code 400 (Bad Request)} if the schedulingUnitProposalDTO has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/scheduling-units/proposal/propose-der")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<SchedulingUnitProposalDTO> proposeForBspJoiningOfDerToSchedulingUnit(@Valid @RequestBody SchedulingUnitProposalDTO schedulingUnitProposalDTO)
        throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-USER - REST request to send SchedulingUnitProposal by FSP/A");
        if (!unitValidator.isUnitBelongsToCurrentFspUser(schedulingUnitProposalDTO.getUnitId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // FSP/A wybieraja do ktorego BSP chca przypisac swojego DERa. Dopiero potem, przy akceptacji oferty, BSP wybiera do ktorego z swoich SchedulingUnit zostanie przypisany DER.
        schedulingUnitProposalDTO.setSchedulingUnitId(null);
        schedulingUnitProposalDTO.setProposalType(SchedulingUnitProposalType.REQUEST);
        return super.createOrResendSchedulingUnitProposal(schedulingUnitProposalDTO);
    }

    /**
     * {@code POST  /scheduling-units/proposal/invite-der} : Create SchedulingUnitProposal by BSP with invitation for FSP/A of joining FSP's DER directly to BSP's SchedulingUnit.
     * If the proposal already exists, it will be sent again.
     *
     * @param schedulingUnitProposalDTO the schedulingUnitProposalDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new schedulingUnitProposalDTO,
     * or with status {@code 400 (Bad Request)} if the schedulingUnitProposalDTO has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/scheduling-units/proposal/invite-der")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<SchedulingUnitProposalDTO> inviteDerToJoinSchedulingUnit(@Valid @RequestBody SchedulingUnitProposalDTO schedulingUnitProposalDTO)
        throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-USER - REST request to send SchedulingUnitProposal by BSP");
        if (!schedulingUnitValidator.isSchedulingUnitBelongsToCurrentFspUser(schedulingUnitProposalDTO.getSchedulingUnitId())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        schedulingUnitProposalDTO.setProposalType(SchedulingUnitProposalType.INVITATION);
        return super.createOrResendSchedulingUnitProposal(schedulingUnitProposalDTO);
    }

    /**
     * {@code GET  /scheduling-units} : get all available current logged in FSP/A's DERs which can be join to pointed BSP SchedulingUnits.
     *
     * @param bspId BSP id (FspEntity with role BSP)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of DERs in body.
     * @see Role#ROLE_BALANCING_SERVICE_PROVIDER
     */
    @GetMapping("/scheduling-units/proposal/fsp/get-available-ders")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<List<UnitMinDTO>> getAvailableFspDersForNewSchedulingUnitProposal(@RequestParam("bspId") Long bspId) {
        log.debug("FLEX-USER - REST request to get all available current logged in FSP/A's DERs which can be join to pointed BSP SchedulingUnits.");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitProposalResourceException("Current logged-in user not found"));
        return super.getAvailableFspDersForNewSchedulingUnitProposal(fspUser.getFspId(), bspId);
    }

    /**
     * {@code GET  /scheduling-units/proposal/fspa/get-available-ders} : find all available current logged in FSPA Subportfolio DERs which can be join to any BSP SchedulingUnits.
     *
     * @param subportfolioId FSPA Subportfolio
     * @param bspId          BSP
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of DERs in body.
     */
    @GetMapping("/scheduling-units/proposal/fspa/get-available-ders")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<List<UnitMinDTO>> findAvailableFspaDersForNewSchedulingUnitProposal(@RequestParam(value = "subportfolioId", required = false) Long subportfolioId,
        @RequestParam(value = "bspId") Long bspId) {
        log.debug("FLEX-USER - REST request to find all available current logged in FSPA Subportfolio DERs which can be join to any BSP SchedulingUnits.");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitProposalResourceException("Current logged-in user not found"));
        if (!fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            log.debug("Current User role is forbidden to execute findAvailableFspaDersForNewSchedulingUnitProposal()");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (Objects.nonNull(subportfolioId) && !subportfolioService.existsBySubportfolioIdAndFspaId(subportfolioId, fspUser.getFspId())) {
            log.warn("findAvailableFspaSubportfolioDersForNewSchedulingUnitProposal() FSPA user [id: {}] is not owner of Subportfolio [id: {}]", fspUser.getId(), subportfolioId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return super.findAvailableFspaSubportfolioDersForNewSchedulingUnitProposal(subportfolioId, bspId, fspUser.getFspId());
    }

    /**
     * Zablokowac przycisk tworzenia SchedulingUnitProposal dla:
     * - FSP, jesli jakikolwiek jego DER jest podpiety do SchedulingUnit innego BSP niz wskazany
     * - FSPA, jesli wszystkie jego Subportfolio sa polaczone z SchedulingUnits innego BSP.
     * <p>
     * Aby FSP mógł wysłać propozycję DERa do jednostki grafikowej musi najpierw uzyskać "Flex Register" na produkt bilansujący,
     * czyli taki, który posiada flagę "Balancing" ustawioną na "Yes". Czyli zanim FSP będzie mógł wysłać DERa, to na ten DER
     * powinno istnieć Flexibility Potential z atrybutami "Prod. preq." i "Stat. grid preq." ustawionymi na "Yes", czyli jeżeli
     * ma status Flex Register(jest w oknie "Registered flexibility potentials"). Dopiero wtedy pojawiają się ikony "plusa" w tabelach
     * w oknach "BSP" oraz "Scheduling units" oraz przycisk "Propose DER" w oknie szczegółów "Scheduling units".
     */
    @GetMapping("/scheduling-units/proposal/is-current-fsp-joined-with-other-bsp-by-scheduling-unit")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<Boolean> isCurrentFspJoinedWithOtherBspBySchedulingUnit(@RequestParam("bspId") Long bspId) {
        log.debug("FLEX-USER - REST request to isCurrentFspJoinedWithBspBySchedulingUnit()");
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitProposalResourceException("Current logged-in user not found"));
        if (fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER)) {
            boolean isFspJoinedWithOtherBsp = schedulingUnitService.isFspJoinedWithOtherBspBySchedulingUnit(fspUser.getFspId(), bspId);
            boolean isAtLeastOneDerOfFspBalanced = isAtLeastOneDerOfFspBalancedByRegisteredFlexPotentialProduct(fspUser.getFspId());
            if (isFspJoinedWithOtherBsp || !isAtLeastOneDerOfFspBalanced) {
                return ResponseEntity.ok(true);
            }
            return ResponseEntity.ok(false);
        } else if (fspUser.hasRole(Role.ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            return ResponseEntity.ok(schedulingUnitService.isAllFspaSubportfoliosJoinedWithOtherBspBySchedulingUnit(fspUser.getFspId(), bspId));
        } else {
            log.debug("Current User role is forbidden to execute isCurrentFspJoinedWithBspBySchedulingUnit()");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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
     * Sprawdzenie czy mozna dodac wskazanego DERa (derId) do SchedulingUnits nalezacych do aktualnie zalogowanego BSP.
     * Ednpoint jest wykonywany w celu wyswietlenia przycisku 'invite der' w oknie szczegolow DERa, gdy okno to otworzyl uzytkownik BSP.
     * Po sprawdzeniu czy DER moze byc dodany, BSP wybiera jeden z swoich SchedulingUnit, do ktorego mozna dolaczac DERy.
     * Patrz 'ep get-all-bsp-su-to-which-ones-fsp-der-can-be-joined'.
     * Walidacja jest tez wykonywana bezposrednio przed dodaniem DERa do SchedulingUnit w walidatorze SchedulingUnitProposalValidator.
     * <p>
     * Aby BSP mógł zaprosić DERa do jednostki grafikowej, to ten DER musi posiadać "Flex register" na produkt z flagą "Balancing".
     * Wtedy BSP po wejściu do okna "DERs" widzi "plusy" tylko przy DERach, które ten "Flex register" posiadają. To samo tyczy się przycisku "Invite DER" w oknie szczegółów DERa.
     */
    @GetMapping("/scheduling-units/proposal/can-der-be-added-to-bsp-scheduling-unit")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<Boolean> canDerBeAddedToCurrentBspSchedulingUnit(@RequestParam("derId") Long derId) {
        log.debug("FLEX-USER - REST request to isCurrentFspJoinedWithBspBySchedulingUnit()");
        UserDTO bspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitProposalResourceException("Current logged-in user not found"));
        if (bspUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            return ResponseEntity.ok(schedulingUnitService.canDerBeAddedToBspSchedulingUnits(derId, bspUser.getFspId()));
        } else {
            log.debug("Current User role is forbidden to execute canDerBeAddedToCurrentBspSchedulingUnit()");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * {@code GET  /scheduling-units/minimal} : get get all current logged in BSP SchedulingUnits to which ones pointed Der can be joined.
     * <p>
     * Patrz 'can-der-be-added-to-bsp-scheduling-unit'.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of schedulingUnits in body.
     */
    @GetMapping("/scheduling-units/minimal/get-all-bsp-su-to-which-ones-fsp-der-can-be-joined")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<List<SchedulingUnitMinDTO>> getAllCurrentBspSchedulingUnitsToWhichOnesAnyDerCanBeJoined(@RequestParam(value = "derId", required = false) Long derId) {
        log.debug("FLEX-USER - REST request to get get all current logged in BSP SchedulingUnits to which ones pointed FSP/A Der can be joined");
        UserDTO bspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitProposalResourceException("Current logged-in user not found"));
        if (!bspUser.hasRole(Role.ROLE_BALANCING_SERVICE_PROVIDER)) {
            log.warn("Current User role is forbidden to execute getAllCurrentBspSchedulingUnitsToWhichOnesAnyDerCanBeJoined()");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(schedulingUnitService.getAllCurrentBspSchedulingUnitsToWhichOnesPointedDerCanBeJoined(bspUser.getFspId(), derId));
    }

    /**
     * {@code GET  /proposal/get} : get schedulingUnitProposal by proposalId.
     *
     * @param proposalId schedulingUnitProposal id
     */
    @GetMapping("/scheduling-units/proposal/get")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_VIEW + "\")")
    public ResponseEntity<SchedulingUnitProposalDTO> getSchedulingUnitProposalById(@RequestParam(value = "proposalId") Long proposalId) {
        log.debug("Rest request to get schedulingUnitProposal by proposalId: {}", proposalId);
        SchedulingUnitProposalDTO result = schedulingUnitService.findSchedulingUnitProposalById(proposalId)
            .orElseThrow(() -> new SchedulingUnitProposalResourceException("No schedulingUnitProposal was found for this id: " + proposalId));
        if (!schedulingUnitProposalValidator.isProposalBelongsToCurrentFsp(result)) {
            log.warn("getSchedulingUnitProposalById() Current user has no access to schedulingUnitProposal [id: {}]", result.getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * {@code GET  /proposal/fsp/accept} : FSP/A accepts schedulingUnitProposal by proposalId.
     *
     * @param proposalId schedulingUnitProposal id
     * @return the {@link ResponseEntity} with status {@code 200 (Ok)} or error http status
     * @throws RuntimeException {@code 500 (Internal Server Error)} if couldn't accept schedulingUnitProposal by security key
     */
    @GetMapping("/scheduling-units/proposal/fsp/accept")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_MANAGE + "\")")
    public ResponseEntity<Object> acceptSchedulingUnitProposalByFsp(@RequestParam(value = "proposalId") Long proposalId) throws ObjectValidationException {
        log.debug("Rest request by FSP/A to accept schedulingUnitProposal [id: {}]", proposalId);
        SchedulingUnitProposalDTO proposal = schedulingUnitService.findSchedulingUnitProposalById(proposalId)
            .orElseThrow(() -> new SchedulingUnitProposalResourceException("No schedulingUnitProposal was found for this id: " + proposalId));
        if (!schedulingUnitProposalValidator.isProposalBelongsToCurrentFsp(proposal)) {
            log.warn("acceptSchedulingUnitProposalByFsp() Current user has no access to schedulingUnitProposal [id: {}]", proposal.getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        schedulingUnitService.acceptSchedulingUnitProposalByFsp(proposal.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * {@code GET  /proposal/bsp/accept} : BSP accepts schedulingUnitProposal by proposalId and selected schedulingUnitId.
     *
     * @param proposalId schedulingUnitProposal id
     * @param schedulingUnitId SchedulingUnit of BSP to which one DER from proposal will be joined
     * @return the {@link ResponseEntity} with status {@code 200 (Ok)} or error http status
     * @throws RuntimeException {@code 500 (Internal Server Error)} if couldn't accept schedulingUnitProposal by security key
     */
    @GetMapping("/scheduling-units/proposal/bsp/accept")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_MANAGE + "\")")
    public ResponseEntity<Object> acceptSchedulingUnitProposalByBsp(@RequestParam(value = "proposalId") Long proposalId, @RequestParam(value = "schedulingUnitId") Long schedulingUnitId)
        throws ObjectValidationException {
        log.debug("Rest request by BSP to accept schedulingUnitProposal [id: {}, schedulingUnitId: {}]", proposalId, schedulingUnitId);
        SchedulingUnitProposalDTO proposal = schedulingUnitService.findSchedulingUnitProposalById(proposalId)
            .orElseThrow(() -> new SchedulingUnitProposalResourceException("No schedulingUnitProposal was found for this id: " + proposalId));
        if (!schedulingUnitProposalValidator.isProposalBelongsToCurrentFsp(proposal)) {
            log.warn("acceptSchedulingUnitProposalByKey() Current user has no access to schedulingUnitProposal [id: {}]", proposal.getId());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!schedulingUnitService.existsBySchedulingUnitIdAndBspId(schedulingUnitId, proposal.getBspId())) {
            log.warn("acceptSchedulingUnitProposalByBsp() Current user has no access to schedulingUnit [id: {}]", schedulingUnitId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        schedulingUnitService.acceptSchedulingUnitProposalByBsp(proposal.getId(), schedulingUnitId);
        return ResponseEntity.ok().build();
    }

    /**
     * {@code GET  /proposal/reject} : reject schedulingUnitProposal by recipient.
     *
     * @param proposalId schedulingUnitProposal id
     * @return the {@link ResponseEntity} with status {@code 200 (Ok)} or error http status
     */
    @GetMapping("/scheduling-units/proposal/reject")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_MANAGE + "\")")
    public ResponseEntity<Object> rejectSchedulingUnitProposal(@RequestParam(value = "proposalId") Long proposalId) {
        log.debug("Rest request to reject schedulingUnitProposal [id: {}]", proposalId);
        SchedulingUnitProposalDTO proposal = schedulingUnitService.findSchedulingUnitProposalById(proposalId)
            .orElseThrow(() -> new SchedulingUnitProposalResourceException("No schedulingUnitProposal was found for this id: " + proposalId));
        if (!schedulingUnitProposalValidator.isProposalBelongsToCurrentFsp(proposal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        schedulingUnitService.rejectSchedulingUnitProposal(proposal.getId());
        return ResponseEntity.ok().build();
    }


    /**
     * {@code GET  /proposal/cancel} : cancel schedulingUnitProposal by sender.
     *
     * @param proposalId schedulingUnitProposal id
     * @return the {@link ResponseEntity} with status {@code 200 (Ok)} or error http status
     */
    @GetMapping("/scheduling-units/proposal/cancel")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_MANAGE + "\")")
    public ResponseEntity<Object> cancelSchedulingUnitProposal(@RequestParam(value = "proposalId") Long proposalId) {
        log.debug("Rest request to cancel schedulingUnitProposal [id: {}]", proposalId);
        SchedulingUnitProposalDTO proposal = schedulingUnitService.findSchedulingUnitProposalById(proposalId)
            .orElseThrow(() -> new SchedulingUnitProposalResourceException("No schedulingUnitProposal was found for this id: " + proposalId));
        if (!schedulingUnitProposalValidator.isProposalBelongsToCurrentFsp(proposal)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        schedulingUnitService.cancelSchedulingUnitProposal(proposal.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * {@code GET  /scheduling-units/proposal/fsp/get-bsps-used-in-fsp-proposals} : find all Bsps used in current logged in Fsp proposals
     *
     * @param proposalType invitation/proposition
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Bsps in body.
     */
    @GetMapping("/scheduling-units/proposal/fsp/get-bsps-used-in-fsp-proposals")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_VIEW + "\")")
    public ResponseEntity<List<FspCompanyMinDTO>> findAllBspsUsedInFspProposals(@RequestParam(value = "proposalType") SchedulingUnitProposalType proposalType) {
        log.debug("FLEX-USER - REST request to find all Bsps used in current logged in Fsp proposals [proposalType: {}]", proposalType);
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitProposalResourceException("Current logged-in user not found"));
        List<FspCompanyMinDTO> result = schedulingUnitService.findAllBspsUsedInFspProposals(fspUser.getFspId(), proposalType);
        return ResponseEntity.ok(result);
    }

    /**
     * {@code GET  /scheduling-units/proposal/fsp/get-bsps-used-in-fsp-proposals} : find all Fsps used in current logged in Bsp proposals
     *
     * @param proposalType invitation/proposition
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Bsps in body.
     */
    @GetMapping("/scheduling-units/proposal/bsp/get-fsps-used-in-bsp-proposals")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_PROPOSAL_VIEW + "\")")
    public ResponseEntity<List<FspCompanyMinDTO>> findAllFspsUsedInBspProposals(@RequestParam(value = "proposalType") SchedulingUnitProposalType proposalType) {
        log.debug("FLEX-USER - REST request to find all Fsps used in current logged in Bsp proposals [proposalType: {}]", proposalType);
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitProposalResourceException("Current logged-in user not found"));
        List<FspCompanyMinDTO> result = schedulingUnitService.findAllFspsUsedInBspProposals(fspUser.getFspId(), proposalType);
        return ResponseEntity.ok(result);
    }
}
