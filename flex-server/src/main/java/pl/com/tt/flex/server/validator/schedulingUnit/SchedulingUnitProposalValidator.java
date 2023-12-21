package pl.com.tt.flex.server.validator.schedulingUnit;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.domain.product.ProductEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalStatus;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalType;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.fsp.FspRepository;
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitProposalRepository;
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitRepository;
import pl.com.tt.flex.server.repository.subportfolio.SubportfolioRepository;
import pl.com.tt.flex.server.repository.unit.UnitRepository;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalDTO;
import pl.com.tt.flex.server.service.user.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent.SCHEDULING_UNIT_PROPOSAL_CREATED_ERROR;
import static pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent.SCHEDULING_UNIT_PROPOSAL_UPDATED_ERROR;
import static pl.com.tt.flex.model.security.permission.Role.*;
import static pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalStatus.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;
import static pl.com.tt.flex.server.web.rest.schedulingUnit.SchedulingUnitProposalResource.SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME;

@Component
@RequiredArgsConstructor
public class SchedulingUnitProposalValidator {

    private final SchedulingUnitProposalRepository schedulingUnitProposalRepository;
    private final SchedulingUnitRepository schedulingUnitRepository;
    private final UnitRepository unitRepository;
    private final FspRepository fspRepository;
    private final SubportfolioRepository subportfolioRepository;
    private final UserService userService;

    public static final List<SchedulingUnitProposalStatus> STATUSES_ALLOWING_RESEND = Lists.newArrayList(NEW, REJECTED, CANCELLED);

    /**
     * Sprawdzenie czy SchedulingUnitProposal moze zostac utworzone lub wyslane ponownie.
     */
    public void validIfProposalCanBeSend(SchedulingUnitProposalDTO proposalDTO) throws ObjectValidationException {
        ActivityEvent activityEventError = isNull(proposalDTO.getId()) ? SCHEDULING_UNIT_PROPOSAL_CREATED_ERROR : SCHEDULING_UNIT_PROPOSAL_UPDATED_ERROR;
        validProposal(proposalDTO, activityEventError);
        checkIfProposalAlreadyExistsFromOppositeSide(proposalDTO, activityEventError);
        if (nonNull(proposalDTO.getId())) {
            validStatusForResend(proposalDTO, activityEventError);
        }
    }

    public void validIfProposalCanBeAccepted(SchedulingUnitProposalDTO proposalDTO) throws ObjectValidationException {
        validProposal(proposalDTO, SCHEDULING_UNIT_PROPOSAL_UPDATED_ERROR);
        //sprawdzic produkty balancing
    }

    private void validProposal(SchedulingUnitProposalDTO proposalDTO, ActivityEvent activityEventError) throws ObjectValidationException {
//        BSP - owner of SchedulingUnit
//        FSP/FSPA - owner of Unit
        validProposedUnit(proposalDTO, activityEventError);
        validProposedSchedulingUnit(proposalDTO, activityEventError);
        validProducts(proposalDTO, activityEventError);
    }

    /**
     * DER może zostać połączony z SchedulingUnit tylko i wyłącznie wtedy, jeżeli posiada "Flex register" (potencjal oznaczony jako zarejestrowany)
     * na wszystkie produkty, które są w typie jednostki grafikowej.
     */
    private void validProducts(SchedulingUnitProposalDTO proposalDTO, ActivityEvent activityEventError) throws ObjectValidationException {
        if(nonNull(proposalDTO.getSchedulingUnitId())){
            List<Long> derPotentialsProductIds = unitRepository.findDerRegisteredPotentialsProductsIds(proposalDTO.getUnitId());
            SchedulingUnitEntity schedulingUnit = schedulingUnitRepository.findById(proposalDTO.getSchedulingUnitId()).get();
            if (!derPotentialsProductIds.containsAll(schedulingUnit.getSchedulingUnitType().getProducts().stream().map(ProductEntity::getId).collect(Collectors.toSet()))) {
                throw new ObjectValidationException("DER cannot be joined to SchedulingUnit because DER is not linked with all the Products that are in the SchedulingUnit type",
                    SCHEDULING_UNIT_PROPOSAL_CANNOT_JOIN_DER_WITH_SU_BECAUSE_DER_IS_NOT_LINKED_WITH_ALL_PRODUCTS_THAT_ARE_IN_SU_TYPE, SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME, activityEventError, proposalDTO.getId());
            }
        }
    }

    /**
     * Walidacja na wysyłanie zaproszenia, jeżeli istnieje już propozycja, oraz na odwrót - na wysyłanie propozycji, jeżeli istnieje już zaproszenie.
     * W obu przypadkach zaproszenie/propozycja nie są tworzone. Bierzemy pod uwage tylko status NEW.
     */
    private void checkIfProposalAlreadyExistsFromOppositeSide(SchedulingUnitProposalDTO proposalDTO, ActivityEvent activityEventError) throws ObjectValidationException {
        if (proposalDTO.getProposalType().equals(SchedulingUnitProposalType.INVITATION) &&
            schedulingUnitProposalRepository.existsByUnitIdAndBspIdAndProposalTypeAndStatus(proposalDTO.getUnitId(), proposalDTO.getBspId(),
                SchedulingUnitProposalType.REQUEST, SchedulingUnitProposalStatus.NEW)) {
            throw new ObjectValidationException("SchedulingUnitProposal already exists from opposite side (Request created by FSP/A)",
                SCHEDULING_UNIT_PROPOSAL_CANNOT_CREATE_PROPOSITION_BECAUSE_ALREADY_EXITSTS_FROM_OPPOSITE_SIDE, SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME, activityEventError, proposalDTO.getId());
        } else if (proposalDTO.getProposalType().equals(SchedulingUnitProposalType.REQUEST) &&
            schedulingUnitProposalRepository.existsByUnitIdAndBspIdAndProposalTypeAndStatus(proposalDTO.getUnitId(), proposalDTO.getBspId(),
                SchedulingUnitProposalType.INVITATION, SchedulingUnitProposalStatus.NEW)) {
            throw new ObjectValidationException("SchedulingUnitProposal already exists from opposite side (Ivitation created by BSP)",
                SCHEDULING_UNIT_PROPOSAL_CANNOT_CREATE_INVITATION_BECAUSE_ALREADY_EXITSTS_FROM_OPPOSITE_SIDE, SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME, activityEventError, proposalDTO.getId());
        }
    }

    /**
     * Mozna wysylac ponownie wszystkie zaproszenia/propozycje oprocz juz zaakceptowanych (status ACCEPTED, CONNECTED_WITH_OTHER).
     *
     * @see SchedulingUnitProposalStatus#ACCEPTED
     * @see SchedulingUnitProposalStatus#CONNECTED_WITH_OTHER
     */
    private void validStatusForResend(SchedulingUnitProposalDTO proposalDTO, ActivityEvent activityEventError) throws ObjectValidationException {
        if (!STATUSES_ALLOWING_RESEND.contains(proposalDTO.getStatus())) {
            throw new ObjectValidationException("Cannot resend SchedulingUnitProposal with status: " + proposalDTO.getStatus(),
                SCHEDULING_UNIT_PROPOSAL_CANNOT_RESEND_BECAUSE_OF_WRONG_CURRENT_STATUS,
                SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME, activityEventError, proposalDTO.getId());
        }
    }

    /**
     * If SchedulingUnit is ready for tests then it is no allowed to add new DERs (Units) to it.
     */
    private void checkIfProposedSchedulingIsReadyForTests(SchedulingUnitEntity proposedScheduling, SchedulingUnitProposalDTO proposalDTO, ActivityEvent activityEventError)
        throws ObjectValidationException {
        if (proposedScheduling.isReadyForTests()) {
            throw new ObjectValidationException("Cannot create/accept SchedulingUnitProposal because SchedulingUnit is marked as ready for tests",
                CANNOT_CREATE_OR_ACCEPT_SCHEDULING_UNIT_PROPOSAL_BECAUSE_SCHEDULING_UNIT_IS_MARKED_AS_READY_FOR_TEST,
                SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME, activityEventError, proposalDTO.getId());
        }
    }

    private void checkIfUnitIsAlreadyJoinedToAnySchedulingUnit(SchedulingUnitProposalDTO proposalDTO, UnitEntity proposedUnit, ActivityEvent activityEventError)
        throws ObjectValidationException {
        if (nonNull(proposedUnit.getSchedulingUnit()) && nonNull(proposedUnit.getSchedulingUnit().getId())) {
//            FSP Unit can be joined to only one SchedulingUnit
            throw new ObjectValidationException("Cannot create SchedulingUnitProposal because Unit is already joined to another SchedulingUnit",
                UNIT_IS_ALREADY_JOINED_TO_SCHEDULING_UNIT, SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME, activityEventError, proposalDTO.getId());
        }
    }

    private void validProposedUnit(SchedulingUnitProposalDTO proposalDTO, ActivityEvent activityEventError) throws ObjectValidationException {
        UnitEntity proposedUnit = unitRepository.findById(proposalDTO.getUnitId()).get();
        FspEntity proposedBsp = getBspFromProposal(proposalDTO);
        checkIfUnitIsAlreadyJoinedToAnySchedulingUnit(proposalDTO, proposedUnit, activityEventError);
        FspEntity proposedUnitFsp = fspRepository.findById(proposedUnit.getFsp().getId()).get();
        if (proposedUnitFsp.getRole().equals(ROLE_FLEX_SERVICE_PROVIDER)) {
//            Unit belongs to FSP
            validProposedFSPUnit(proposalDTO, proposedUnitFsp, proposedBsp, activityEventError);
        } else if (proposedUnitFsp.getRole().equals(ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
//           Unit belongs to FSPA
            validProposedFSPAUnit(proposalDTO, proposedUnit, proposedBsp, activityEventError);
        } else {
            throw new RuntimeException("SchedulingUnitProposalValidator checkValid() Proposed Unit's FSP role is not valid for making a SchedulingUnitProposal");
        }
    }

    private FspEntity getBspFromProposal(SchedulingUnitProposalDTO proposalDTO) {
        if (nonNull(proposalDTO.getSchedulingUnitId())) {
            SchedulingUnitEntity proposedScheduling = schedulingUnitRepository.findById(proposalDTO.getSchedulingUnitId()).get();
            return proposedScheduling.getBsp();
        }
        return fspRepository.findById(proposalDTO.getBspId()).get();
    }

    private void validProposedFSPUnit(SchedulingUnitProposalDTO proposalDTO, FspEntity proposedUnitFsp, FspEntity proposedBsp,
        ActivityEvent activityEventError) throws ObjectValidationException {
        if (unitRepository.existsByFspIdAndSchedulingUnitBspIdNot(proposedUnitFsp.getId(), proposedBsp.getId())) {
//            FSP can only register its Units with SchedulingUnits belonging to only one BSP
            throw new ObjectValidationException("FSP owner of proposed Unit has already joined other Units to another BSP",
                FSP_OWNER_OF_PROPOSED_UNIT_HAS_ALREADY_JOINED_OTHER_UNITS_TO_ANOTHER_BSP, SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME, activityEventError, proposalDTO.getId());
        }
    }

    private void validProposedFSPAUnit(SchedulingUnitProposalDTO proposalDTO, UnitEntity proposedUnit, FspEntity proposedBsp,
        ActivityEvent activityEventError) throws ObjectValidationException {
//        W przypadku FSPA jeżeli już jakaś SU danego BSP ma połączenie z DERem FSPA należącym do tego samego subportfolio,
//        to inny DER tego FSPA z tego samego subportfolio może być połączony tylko z tą SU
        List<Long> allDersFromDerSubportfolio = subportfolioRepository.findAllDerIdsFromDerSubportfolio(proposedUnit.getId());
        Optional<SchedulingUnitMinDTO> optSchedulingUnitFromOneOfSubportfolioDers = schedulingUnitRepository.findFirstByUnitIn(allDersFromDerSubportfolio);
        if (optSchedulingUnitFromOneOfSubportfolioDers.isPresent()) {
            if (unitRepository.existsByIdInAndFspIdAndSchedulingUnitBspIdNot(allDersFromDerSubportfolio, proposedUnit.getFsp().getId(), proposedBsp.getId())) {
                throw new ObjectValidationException("Some Unit from Subportfolio of proposed Unit is already joined to another BSP",
                    SOME_UNIT_FROM_SUBPORTFOLIO_OF_PROPOSED_UNIT_IS_ALREADY_JOINED_TO_ANOTHER_BSP, SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME,
                    activityEventError, proposalDTO.getId());
            }
            if (nonNull(proposalDTO.getSchedulingUnitId()) && !optSchedulingUnitFromOneOfSubportfolioDers.get().getId().equals(proposalDTO.getSchedulingUnitId())) {
                throw new ObjectValidationException("Some Unit from Subportfolio of proposed Unit is already joined to another Scheduling unit belonging to proposed BSP",
                    SOME_UNIT_FROM_SUBPORTFOLIO_OF_PROPOSED_UNIT_IS_ALREADY_JOINED_TO_ANOTHER_SCHEDULING_UNIT_BELONGING_TO_PROPOSED_BSP, SCHEDULING_UNIT_PROPOSAL_ENTITY_NAME,
                    activityEventError, proposalDTO.getId());
            }
        }
    }

    private void validProposedSchedulingUnit(SchedulingUnitProposalDTO proposalDTO, ActivityEvent activityEventError) throws ObjectValidationException {
        if (nonNull(proposalDTO.getSchedulingUnitId())) {
            SchedulingUnitEntity proposedScheduling = schedulingUnitRepository.findById(proposalDTO.getSchedulingUnitId()).get();
            checkIfProposedSchedulingIsReadyForTests(proposedScheduling, proposalDTO, activityEventError);
        }
    }

    /**
     * Is SchedulingUnitProposal belongs to current logged in users's Fsp
     */
    public boolean isProposalBelongsToCurrentFsp(SchedulingUnitProposalDTO proposal) {
        UserEntity currentUser = userService.getCurrentUser();
        Long currentUsersFspId = currentUser.getFsp().getId();
        if (currentUser.hasRole(ROLE_BALANCING_SERVICE_PROVIDER)) {
            return proposal.getBspId().equals(currentUsersFspId);
        } else if (currentUser.hasRole(ROLE_FLEX_SERVICE_PROVIDER) || currentUser.hasRole(ROLE_FLEX_SERVICE_PROVIDER_AGGREGATED)) {
            return unitRepository.findByIdAndFspId(proposal.getUnitId(), currentUsersFspId).isPresent();
        }
        return false;
    }
}
