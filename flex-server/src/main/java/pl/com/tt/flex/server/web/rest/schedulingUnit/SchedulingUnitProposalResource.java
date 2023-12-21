package pl.com.tt.flex.server.web.rest.schedulingUnit;

import io.github.jhipster.web.util.HeaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitProposalEntity;
import pl.com.tt.flex.server.service.fsp.FspService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitQueryProposalService;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalDTO;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitMapper;
import pl.com.tt.flex.server.service.schedulingUnit.mapper.SchedulingUnitProposalMapper;
import pl.com.tt.flex.server.service.subportfolio.SubportfolioService;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitProposalValidator;
import pl.com.tt.flex.server.validator.schedulingUnit.SchedulingUnitValidator;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Common REST controller for managing {@link SchedulingUnitProposalEntity} for all web modules.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class SchedulingUnitProposalResource {

    public static final String SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME = "schedulingUnitProposal";

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    protected final SchedulingUnitService schedulingUnitService;
    protected final SchedulingUnitQueryProposalService schedulingUnitQueryProposalService;
    protected final SchedulingUnitMapper schedulingUnitMapper;
    protected final SchedulingUnitValidator schedulingUnitValidator;
    protected final UserService userService;
    protected final SchedulingUnitProposalMapper schedulingUnitProposalMapper;
    protected final SchedulingUnitProposalValidator schedulingUnitProposalValidator;
    protected final FspService fspService;
    protected final SubportfolioService subportfolioService;

    public SchedulingUnitProposalResource(SchedulingUnitService schedulingUnitService, SchedulingUnitQueryProposalService schedulingUnitQueryProposalService, SchedulingUnitMapper schedulingUnitMapper,
        SchedulingUnitValidator schedulingUnitValidator, UserService userService, SchedulingUnitProposalMapper schedulingUnitProposalMapper,
        SchedulingUnitProposalValidator schedulingUnitProposalValidator, FspService fspService, SubportfolioService subportfolioService) {
        this.schedulingUnitService = schedulingUnitService;
        this.schedulingUnitQueryProposalService = schedulingUnitQueryProposalService;
        this.schedulingUnitMapper = schedulingUnitMapper;
        this.schedulingUnitValidator = schedulingUnitValidator;
        this.userService = userService;
        this.schedulingUnitProposalMapper = schedulingUnitProposalMapper;
        this.schedulingUnitProposalValidator = schedulingUnitProposalValidator;
        this.fspService = fspService;
        this.subportfolioService = subportfolioService;
    }

    protected ResponseEntity<SchedulingUnitProposalDTO> createOrResendSchedulingUnitProposal(SchedulingUnitProposalDTO schedulingUnitProposalDTO)
        throws URISyntaxException, ObjectValidationException {
        schedulingUnitProposalDTO.setSenderId(userService.getCurrentUser().getId());
        SchedulingUnitProposalDTO result = schedulingUnitService.createOrResendSchedulingUnitProposal(schedulingUnitProposalDTO);
        return ResponseEntity.created(new URI("/api/scheduling-units/proposal/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME, result.getId().toString())).body(result);
    }

    protected ResponseEntity<Map<String, List<UnitMinDTO>>> getSchedulingUnitDers(Long schedulingUnitId) {
        List<UnitMinDTO> schedulingUnitDers = schedulingUnitService.getSchedulingUnitDers(schedulingUnitId);
        Map<String, List<UnitMinDTO>> result = schedulingUnitDers.stream().collect(Collectors.groupingBy(UnitMinDTO::getFspCompanyName));
        return ResponseEntity.ok(result);
    }

    /**
     * Należy ograniczyć wybór DERów w modalach z propozycją dołączenia DERa do Jednostki grafikowej.
     * Powinny być dostępne tylko te, które faktycznie mogą być dołączone.
     */
    protected ResponseEntity<List<UnitMinDTO>> getAvailableFspDersForNewSchedulingUnitProposal(Long fspId, Long bspId) {
        List<UnitMinDTO> result;
        Role fspRole = fspService.findFspRole(fspId);
        if (fspRole.equals(Role.ROLE_FLEX_SERVICE_PROVIDER)) {
            result = schedulingUnitService.getAvailableFspDersForNewSchedulingUnitProposal(fspId, bspId);
        } else {
            log.warn("Current FSP role is forbidden to execute getAvailableFspDersForNewSchedulingUnitProposal()");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    /**
     * Należy ograniczyć wybór DERów w modalach z propozycją dołączenia DERa do Jednostki grafikowej.
     * Powinny być dostępne tylko te, które faktycznie mogą być dołączone.
     */
    protected ResponseEntity<List<UnitMinDTO>> findAvailableFspaSubportfolioDersForNewSchedulingUnitProposal(Long subportfolioId, Long bspId, Long fspaId) {
        List<UnitMinDTO> result = schedulingUnitService.findAvailableFspaSubportfolioDersForNewSchedulingUnitProposal(subportfolioId, bspId, fspaId);
        if (result.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(result);
    }

    protected static class SchedulingUnitProposalResourceException extends RuntimeException {
        protected SchedulingUnitProposalResourceException(String message) {
            super(message);
        }
    }
}
