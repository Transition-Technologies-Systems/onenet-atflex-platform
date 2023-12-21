package pl.com.tt.flex.server.web.rest.schedulingUnit;

import io.github.jhipster.web.util.PaginationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
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
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitQueryProposalService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalCriteria;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalMinDTO;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitProposalMapper;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioService;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitProposalValidator;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitValidator;

import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

import static pl.com.tt.flex.model.security.permission.Authority.*;

/**
 * REST controller for managing {@link SchedulingUnitProposalEntity} for FLEX-ADMIN web module.
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
public class SchedulingUnitProposalResourceAdmin extends SchedulingUnitProposalResource {

    private final UnitService unitService;

    public SchedulingUnitProposalResourceAdmin(SchedulingUnitService schedulingUnitService, SchedulingUnitQueryProposalService schedulingUnitQueryProposalService,
        SchedulingUnitMapper schedulingUnitMapper, SchedulingUnitValidator schedulingUnitValidator, UserService userService,
        SchedulingUnitProposalMapper schedulingUnitProposalMapper, SchedulingUnitProposalValidator schedulingUnitProposalValidator,
        FspService fspService, UnitService unitService, SubportfolioService subportfolioService) {
        super(schedulingUnitService, schedulingUnitQueryProposalService, schedulingUnitMapper, schedulingUnitValidator, userService, schedulingUnitProposalMapper,
            schedulingUnitProposalValidator, fspService, subportfolioService);
        this.unitService = unitService;
    }


    /**
     * {@code GET  /scheduling-units/proposal/get-all} : get all the schedulingUnitProposals.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of schedulingUnits in body.
     */
    @GetMapping("/scheduling-units/proposal/get-all")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitProposalMinDTO>> getAllSchedulingUnitProposals(SchedulingUnitProposalCriteria criteria, Pageable pageable) {
        log.debug("FLEX-ADMIN - REST request to get SchedulingUnitProposals by criteria: {}", criteria);
        Page<SchedulingUnitProposalMinDTO> page = schedulingUnitQueryProposalService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * Sprawdzenie czy mozna dodac wskazanego DERa (derId) do SchedulingUnits nalezacych do wskazanego BSP.
     * Ednpoint jest wykonywany w celu wyswietlenia przycisku 'invite der' w oknie szczegolow DERa, gdy okno to otworzyl uzytkownik BSP.
     * Po sprawdzeniu czy DER moze byc dodany, BSP wybiera jeden z swoich SchedulingUnit, do ktorego mozna dolaczac DERy.
     * Patrz 'ep get-all-bsp-su-to-which-ones-fsp-der-can-be-joined'.
     * Walidacja jest tez wykonywana bezposrednio przed dodaniem DERa do SchedulingUnit w walidatorze SchedulingUnitProposalValidator.
     */
    @GetMapping("/scheduling-units/proposal/can-der-be-added-to-bsp-scheduling-unit")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<Boolean> canDerBeAddedToBspSchedulingUnits(@RequestParam("derId") Long derId, @RequestParam("bspId") Long bspId) {
        log.debug("FLEX-USER - REST request to isCurrentFspJoinedWithBspBySchedulingUnit()");
        return ResponseEntity.ok(schedulingUnitService.canDerBeAddedToBspSchedulingUnits(derId, bspId));
    }

    /**
     * {@code GET /scheduling-units/minimal/get-all-bsp-su-to-which-ones-fsp-der-can-be-joined} : get all BSP SchedulingUnits to which ones Der can be joined.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of schedulingUnits in body.
     */
    @GetMapping("/scheduling-units/minimal/get-all-bsp-su-to-which-ones-fsp-der-can-be-joined")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<List<SchedulingUnitMinDTO>> getAllBspSchedulingUnitsToWhichOnesFspDerCanBeJoined(@RequestParam("bspId") Long bspId, @RequestParam("derId") Long derId) {
        log.debug("FLEX-ADMIN - REST request to get get all BSP SchedulingUnits to which selected FSP/A DER can be joined");
        if (!schedulingUnitService.canDerBeAddedToBspSchedulingUnits(derId, bspId)) {
            //w przypadku FSP dla wybranego BSP wyswietlamy SU gdy dany fsp nie jest przypiety do innego bsp
            //w przypadku FSPA dla wybranego BSP wyswietlamy SU gdy dane subportfolio nie jest przypiete do innego bsp, tzn jakis der tego suba nie jest juz przypiety do innego bsp
            return ResponseEntity.noContent().build();
        }
        //w okienku wysyłania zaproszenia w selekcie "BSP" dostępne wszystkie BSP, a w selekcie "Scheduling unit" pokazuje tylko te jednostki grafikowe,
        // które w swoim typie mają produkt, na który wybrany DER ma "Flex register"
        return ResponseEntity.ok(schedulingUnitService.getAllCurrentBspSchedulingUnitsToWhichOnesPointedDerCanBeJoined(bspId, derId));
    }

    /**
     * {@code POST  /scheduling-units/proposal} : FSP proposal to join its DER to BSP. Bsp picks later, in another request, to which one of his Scheduling units DER will be join.
     * If the proposal already exists, it will be sent again.
     *
     * @param schedulingUnitProposalDTO the schedulingUnitProposalDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new schedulingUnitProposalDTO,
     * or with status {@code 400 (Bad Request)} if the schedulingUnitProposalDTO has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/scheduling-units/proposal/propose-der")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<SchedulingUnitProposalDTO> proposeForBspJoiningOfDerToSchedulingUnit(@Valid @RequestBody SchedulingUnitProposalDTO schedulingUnitProposalDTO)
        throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to send SchedulingUnitProposal by FSP/A");
        // FSP/A wybieraja do ktorego BSP chca przypisac swojego DERa. Dopiero potem, przy akceptacji oferty, BSP wybiera do ktorego z swoich SchedulingUnit zostanie przypisany DER.
        schedulingUnitProposalDTO.setSchedulingUnitId(null);
        schedulingUnitProposalDTO.setProposalType(SchedulingUnitProposalType.REQUEST);
        return super.createOrResendSchedulingUnitProposal(schedulingUnitProposalDTO);
    }

    /**
     * {@code POST  /scheduling-units/proposal/invite-der} : BSP invitation for FSP of joining FSP's DER directly to BSP's SchedulingUnit.
     * If the proposal already exists, it will be sent again.
     *
     * @param schedulingUnitProposalDTO the schedulingUnitProposalDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new schedulingUnitProposalDTO,
     * or with status {@code 400 (Bad Request)} if the schedulingUnitProposalDTO has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/scheduling-units/proposal/invite-der")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<SchedulingUnitProposalDTO> inviteDerToJoinSchedulingUnit(@Valid @RequestBody SchedulingUnitProposalDTO schedulingUnitProposalDTO)
        throws URISyntaxException, ObjectValidationException {
        log.debug("FLEX-ADMIN - REST request to send SchedulingUnitProposal by BSP");
        schedulingUnitProposalDTO.setProposalType(SchedulingUnitProposalType.INVITATION);
        return super.createOrResendSchedulingUnitProposal(schedulingUnitProposalDTO);
    }

    /**
     * {@code GET  /scheduling-units} : get all available FSP (FspEntity with role FSP/FSPA) DERs which can be join to pointed BSP SchedulingUnits.
     *
     * @param fspId FSP id (FspEntity with role FSP/FSPA)
     * @param bspId BSP id (FspEntity with role BSP)
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of available DERs in body.
     * @see Role
     */
    @GetMapping("/scheduling-units/proposal/fsp/get-available-ders")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<List<UnitMinDTO>> getAvailableFspDersForNewSchedulingUnitProposal(@RequestParam("fspId") Long fspId, @RequestParam("bspId") Long bspId) {
        log.debug("FLEX-ADMIN - REST request get all available FSP (FspEntity with role FSP/FSPA) DERs which can be join to pointed BSP SchedulingUnits.");
        //w okienku proponowania DERa w selekcie "FSP" dostępne wszystkie FSP oraz FSPA, a po wyborze FSP
        // w selekcie "DER" wyświetlane tylko te DERy, które posiadają "Flex register" na produkt z "Balancing"
        return super.getAvailableFspDersForNewSchedulingUnitProposal(fspId, bspId);
    }

    /**
     * {@code GET  /scheduling-units/proposal/fspa/get-available-ders} : find all available current logged in FSPA Subportfolio DERs which can be join to pointed BSP.
     *
     * @param subportfolioId FSPA Subportfolio
     * @param bspId          BSP
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of DERs in body.
     */
    @GetMapping("/scheduling-units/proposal/fspa/get-available-ders")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_CREATE + "\")")
    public ResponseEntity<List<UnitMinDTO>> findAvailableFspaDersForNewSchedulingUnitProposal(@RequestParam(value = "subportfolioId", required = false) Long subportfolioId,
                                                                                              @RequestParam(value = "fspaId", required = false) Long fspaId, @RequestParam("bspId") Long bspId) {
        log.debug("FLEX-ADMIN - REST request to find all available pointed FSPA Subportfolio DERs which can be join to any BSP SchedulingUnits.");
        if (Objects.nonNull(subportfolioId) && unitService.existsBySubportfolioIdAndSchedulingUnitBspIdNot(subportfolioId, bspId)) {
            //W selekcie 'DER' nie wyswietlamy obiektow gdy Subportfolio danego FSPA jest polaczone z innym BSP niz ten wybrany z okna
            log.debug("findAvailableFspaDersForNewSchedulingUnitProposal() Subportfolio is already joined with other BSP");
            return ResponseEntity.noContent().build();
        }
        //Z wybranego Subportfolio w selekcie 'DER' wyswietlamy wolne DERy (niepolaczone z zadnym SchedulingUnit wybranego BSP)
        return super.findAvailableFspaSubportfolioDersForNewSchedulingUnitProposal(subportfolioId, bspId, fspaId);
    }

    /**
     * {@code GET  /proposal/reject} : reject schedulingUnitProposal by proposalId.
     *
     * @param proposalId schedulingUnitProposal id
     * @return the {@link ResponseEntity} with status {@code 200 (Ok)} or error http status
     */
    @GetMapping("/scheduling-units/proposal/reject")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_MANAGE + "\")")
    public ResponseEntity<Object> rejectSchedulingUnitProposal(@RequestParam(value = "proposalId") Long proposalId) {
        log.debug("FLEX-ADMIN - Rest request to reject schedulingUnitProposal [id: {}]", proposalId);
        SchedulingUnitProposalDTO proposal = schedulingUnitService.findSchedulingUnitProposalById(proposalId)
            .orElseThrow(() -> new SchedulingUnitProposalResourceException("No schedulingUnitProposal was found for this id: " + proposalId));
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
        log.debug("FLEX-ADMIN - Rest request to cancel schedulingUnitProposal [id: {}]", proposalId);
        SchedulingUnitProposalDTO proposal = schedulingUnitService.findSchedulingUnitProposalById(proposalId)
            .orElseThrow(() -> new SchedulingUnitProposalResourceException("No schedulingUnitProposal was found for this id: " + proposalId));
        schedulingUnitService.cancelSchedulingUnitProposal(proposal.getId());
        return ResponseEntity.ok().build();
    }

    /**
     * {@code GET  /scheduling-units/proposal/fsp/get-bsps-used-in-fsp-proposals} : find all Bsps used in all Fsps proposals
     *
     * @param proposalType invitation/proposition
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Bsps in body.
     */
    @GetMapping("/scheduling-units/proposal/fsp/get-bsps-used-in-fsp-proposals")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_VIEW + "\")")
    public ResponseEntity<List<FspCompanyMinDTO>> findAllBspsUsedInAllFspsProposals(@RequestParam(value = "proposalType") SchedulingUnitProposalType proposalType) {
        log.debug("FLEX-ADMIN - REST request to find all Bsps used in current logged in Fsp proposals [proposalType: {}]", proposalType);
        List<FspCompanyMinDTO> result = schedulingUnitService.findAllBspsUsedInAllFspsProposals(proposalType);
        return ResponseEntity.ok(result);
    }

    /**
     * {@code GET  /scheduling-units/proposal/fsp/get-bsps-used-in-fsp-proposals} : find all Fsps used in all Bsps proposals
     *
     * @param proposalType invitation/proposition
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of Bsps in body.
     */
    @GetMapping("/scheduling-units/proposal/bsp/get-fsps-used-in-bsp-proposals")
    @PreAuthorize("hasAuthority(\"" + FLEX_ADMIN_SCHEDULING_UNIT_PROPOSAL_VIEW + "\")")
    public ResponseEntity<List<FspCompanyMinDTO>> findAllFspsUsedInAllBspsProposals(@RequestParam(value = "proposalType") SchedulingUnitProposalType proposalType) {
        log.debug("FLEX-ADMIN - REST request to find all Fsps used in current logged in Bsp proposals [proposalType: {}]", proposalType);
        List<FspCompanyMinDTO> result = schedulingUnitService.findAllFspsUsedInAllBspsProposals(proposalType);
        return ResponseEntity.ok(result);
    }
}
