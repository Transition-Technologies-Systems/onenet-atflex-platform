package pl.com.tt.flex.server.validator.schedulingUnit;

import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.DOC;
import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.DOCX;
import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.PDF;
import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.TXT;
import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.XLS;
import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.XLSX;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_CHANGE_SCHEDULING_UNIT_TYPE_BECAUSE_DER_IS_ALREADY_JOINED_TO_IT;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_CHANGE_SCHEDULING_UNIT_TYPE_BECAUSE_IT_IS_MARKED_AS_READY_FOR_TESTS;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_DELETE_SCHEDULING_UNIT_BECAUSE_IT_HAS_JOINED_DERS;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_DELETE_SCHEDULING_UNIT_BECAUSE_IT_HAS_PROPOSALS_DERS;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_DELETE_SCHEDULING_UNIT_BECAUSE_IT_IS_ACTIVE;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_MOVE_EMPTY_SCHEDULING_UNIT_TO_FLEX_REGISTER;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_REMOVE_CERTIFICATION_FROM_SU_WITH_ONGOING_DELIVERY;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.CANNOT_REMOVE_CERTIFICATION_WHILE_SU_HAS_OPEN_AUCTION_OFFER;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SCHEDULING_UNIT_CANNOT_BE_CERTIFIED_BECAUSE_IT_IS_NOT_MARKED_AS_READY_FOR_TESTS;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SCHEDULING_UNIT_CANNOT_BE_MARKED_AS_READY_FOR_TESTS_WITH_NO_DERS_ASSIGNED;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SCHEDULING_UNIT_CANNOT_REMOVE_DERS_FROM_SU_MARKED_AS_CERTIFIED;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.SCHEDULING_UNIT_CANNOT_REMOVE_DERS_FROM_SU_MARKED_AS_READY_FOR_TESTS;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.USER_HAS_NO_AUTHORITY_TO_MODIFY_SCHEDULING_UNIT_CERTIFIED;
import static pl.com.tt.flex.server.web.rest.schedulingUnit.SchedulingUnitResource.SCHEDULING_UNIT_ENTITY_NAME;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import lombok.RequiredArgsConstructor;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.auction.offer.AuctionDayAheadOfferRepository;
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitRepository;
import pl.com.tt.flex.server.repository.unit.UnitRepository;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.validator.AbstractFileValidator;
import pl.com.tt.flex.server.validator.ObjectValidator;

@Component
@RequiredArgsConstructor
public class SchedulingUnitValidator extends AbstractFileValidator implements ObjectValidator<SchedulingUnitDTO, Long> {

    private static final Set<FileExtension> SUPPORTED_FILE_EXTENSIONS = Sets.newHashSet(DOC, DOCX, PDF, TXT, XLS, XLSX);

    private final SchedulingUnitRepository schedulingUnitRepository;
    private final UnitRepository unitRepository;
    private final UserService userService;
    private final AuctionDayAheadOfferRepository auctionDayAheadOfferRepository;

    @Override
    public void checkValid(SchedulingUnitDTO schedulingUnitDTO) throws ObjectValidationException {
        validCertificate(schedulingUnitDTO);
        validReadyForTests(schedulingUnitDTO);
    }

    @Override
    public void checkModifiable(SchedulingUnitDTO schedulingUnitDTO) throws ObjectValidationException {
        checkValid(schedulingUnitDTO);
        SchedulingUnitEntity schedulingUnitEntity = schedulingUnitRepository.findById(schedulingUnitDTO.getId()).get();
        validCertifiedModification(schedulingUnitDTO, schedulingUnitEntity);
        validSchedulingUnitTypeModification(schedulingUnitDTO, schedulingUnitEntity);
        checkIfSuCanBeMovedToFlexRegister(schedulingUnitDTO, schedulingUnitEntity);
    }

    @Override
    public void checkDeletable(Long id) throws ObjectValidationException {
        SchedulingUnitEntity schedulingUnitEntity = schedulingUnitRepository.findById(id).get();
        if (schedulingUnitEntity.isActive()) {
            throw new ObjectValidationException("Cannot remove active SchedulingUnit", CANNOT_DELETE_SCHEDULING_UNIT_BECAUSE_IT_IS_ACTIVE,
                SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_DELETED_ERROR, id);
        }
        // blokujemy usuwanie SU w przypdaku gdy ma podpiete DERy lub ma propozycje DERow
        if (schedulingUnitEntity.getNumberOfDers() > 0) {
            throw new ObjectValidationException("Cannot remove because Scheduling Unit has joined DERs", CANNOT_DELETE_SCHEDULING_UNIT_BECAUSE_IT_HAS_JOINED_DERS,
                SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_DELETED_ERROR, id);
        }
        if (schedulingUnitEntity.getNumberOfDersProposals() > 0) {
            throw new ObjectValidationException("Cannot remove because Scheduling Unit has proposals DERs", CANNOT_DELETE_SCHEDULING_UNIT_BECAUSE_IT_HAS_PROPOSALS_DERS,
                SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_DELETED_ERROR, id);
        }
    }

    /**
     * Jeżeli scheduling unit został oznaczony jako gotowy do testów i/lub jest certyfikowany, lecz nie zawiera DERów,
     * to wyrzucić wyjątek.
     */
    private void checkIfSuCanBeMovedToFlexRegister(SchedulingUnitDTO schedulingUnitDTO, SchedulingUnitEntity schedulingUnitEntityFromDb) throws ObjectValidationException {
        if ((schedulingUnitDTO.isReadyForTests() || schedulingUnitDTO.isCertified()) && schedulingUnitEntityFromDb.getNumberOfDers() == 0) {
            throw new ObjectValidationException("Cannot move Scheduling Unit with no DERs to flex register", CANNOT_MOVE_EMPTY_SCHEDULING_UNIT_TO_FLEX_REGISTER,
                SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_UPDATED_ERROR, schedulingUnitDTO.getId());
        }
    }

    public boolean isSchedulingUnitBelongsToCurrentFspUser(Long schedulingUnitId) {
        SchedulingUnitEntity schedulingUnitEntity = schedulingUnitRepository.findById(schedulingUnitId)
            .orElseThrow(() -> new RuntimeException("SchedulingUnit not found with id: " + schedulingUnitId));
        UserDTO fspUser = userService.getCurrentUserDTO().orElseThrow(() -> new RuntimeException("Current logged-in user not found"));
        return schedulingUnitEntity.getBsp().getId().equals(fspUser.getFspId());
    }

    /**
     * Only Transmission System Operator (TSO) and Technical Administrator (TA) Users can modify field 'certified' in SchedulingUnit.
     */
    private void validCertifiedModification(SchedulingUnitDTO modifiedScheduling, SchedulingUnitEntity dbScheduling) throws ObjectValidationException {
        UserEntity currentUser = userService.getCurrentUser();
        if (isCertifiedChanged(modifiedScheduling, dbScheduling) && !(currentUser.hasRole(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR) || currentUser.hasRole(Role.ROLE_ADMIN))) {
            throw new ObjectValidationException("User is not authorized to modify field 'certified'",
                USER_HAS_NO_AUTHORITY_TO_MODIFY_SCHEDULING_UNIT_CERTIFIED,
                SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_UPDATED_ERROR, modifiedScheduling.getId());
        }
        boolean isCertificationRemoved = isCertificationRemoved(modifiedScheduling, dbScheduling);
        if (isCertificationRemoved && isUsedInOpenAuctionOffer(dbScheduling.getId())) {
            throw new ObjectValidationException("Cannot remove certification from scheduling unit used in auction offer with status 'Pending' or 'Volumes Verified'",
                CANNOT_REMOVE_CERTIFICATION_WHILE_SU_HAS_OPEN_AUCTION_OFFER,
                SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_UPDATED_ERROR, modifiedScheduling.getId());
        }
        if (isCertificationRemoved && isUsedInAuctionWithOngoingDeliveryPeriod(dbScheduling.getId())) {
            throw new ObjectValidationException("Cannot remove certification from scheduling unit used in auction with ongoing delivery period",
                CANNOT_REMOVE_CERTIFICATION_FROM_SU_WITH_ONGOING_DELIVERY,
                SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_UPDATED_ERROR, modifiedScheduling.getId());
        }
    }

    private boolean isCertifiedChanged(SchedulingUnitDTO modifiedScheduling, SchedulingUnitEntity dbScheduling) {
        return modifiedScheduling.isCertified() != dbScheduling.isCertified();
    }

    private boolean isCertificationRemoved(SchedulingUnitDTO modifiedScheduling, SchedulingUnitEntity dbScheduling) {
        return dbScheduling.isCertified() && !modifiedScheduling.isCertified();
    }

    private boolean isUsedInOpenAuctionOffer(Long schedulingUnitId) {
        return auctionDayAheadOfferRepository.existsBySchedulingUnitIdAndStatusPendingOrVolumesVerified(schedulingUnitId);
    }

    private boolean isUsedInAuctionWithOngoingDeliveryPeriod(Long schedulingUnitId) {
        return auctionDayAheadOfferRepository.existsBySchedulingUnitIdAndStatusNotRejectedAndDeliveryDateAfter(schedulingUnitId, Instant.now().minus(Duration.ofDays(1)));
    }

    private void validSchedulingUnitTypeModification(SchedulingUnitDTO modifiedScheduling, SchedulingUnitEntity dbScheduling) throws ObjectValidationException {
        if (isSchedulingUnitTypeChanged(modifiedScheduling, dbScheduling)) {
            if (dbScheduling.isReadyForTests()) {
                throw new ObjectValidationException("Cannot change SchedulingUnit Type because it is marked as ready for tests",
                    CANNOT_CHANGE_SCHEDULING_UNIT_TYPE_BECAUSE_IT_IS_MARKED_AS_READY_FOR_TESTS,
                    SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_UPDATED_ERROR, modifiedScheduling.getId());
            }
            if (unitRepository.existsBySchedulingUnitId(modifiedScheduling.getId())) {
                throw new ObjectValidationException("Cannot change SchedulingUnit Type because some DER (Unit) is already joined to it",
                    CANNOT_CHANGE_SCHEDULING_UNIT_TYPE_BECAUSE_DER_IS_ALREADY_JOINED_TO_IT,
                    SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_UPDATED_ERROR, modifiedScheduling.getId());
            }
        }
    }

    private boolean isSchedulingUnitTypeChanged(SchedulingUnitDTO modifiedScheduling, SchedulingUnitEntity dbScheduling) {
        return !modifiedScheduling.getSchedulingUnitType().getId().equals(dbScheduling.getSchedulingUnitType().getId());
    }

    private void validCertificate(SchedulingUnitDTO schedulingUnitDTO) throws ObjectValidationException {
        if (schedulingUnitDTO.isCertified() && !schedulingUnitDTO.isReadyForTests()) {
            throw new ObjectValidationException("SchedulingUnit cannot be certified if it is not marked as ready for tests",
                SCHEDULING_UNIT_CANNOT_BE_CERTIFIED_BECAUSE_IT_IS_NOT_MARKED_AS_READY_FOR_TESTS,
                SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_UPDATED_ERROR, schedulingUnitDTO.getId());
        }
    }

    private void validReadyForTests(SchedulingUnitDTO schedulingUnitDTO) throws ObjectValidationException {
        if (schedulingUnitDTO.isReadyForTests()) {
            if (!schedulingUnitRepository.existsByIdAndUnitsAssigned(schedulingUnitDTO.getId())) {
                throw new ObjectValidationException("SchedulingUnit cannot be marked as ready for tests with no Ders assigned",
                    SCHEDULING_UNIT_CANNOT_BE_MARKED_AS_READY_FOR_TESTS_WITH_NO_DERS_ASSIGNED,
                    SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_UPDATED_ERROR, schedulingUnitDTO.getId());
            }
        }
    }

    public static void checkIfAnyDersCanBeRemovedFromSchedulingUnit(SchedulingUnitEntity schedulingUnit) throws ObjectValidationException {
        if (schedulingUnit.isReadyForTests()) {
            throw new ObjectValidationException("Cannot remove Ders from SchedulingUnit because SchedulingUnit is marked as Ready for tests",
                SCHEDULING_UNIT_CANNOT_REMOVE_DERS_FROM_SU_MARKED_AS_READY_FOR_TESTS,
                SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_UPDATED_ERROR, schedulingUnit.getId());
        }
        if (schedulingUnit.isCertified()) {
            throw new ObjectValidationException("Cannot remove Ders from SchedulingUnit because SchedulingUnit is marked as Certified",
                SCHEDULING_UNIT_CANNOT_REMOVE_DERS_FROM_SU_MARKED_AS_CERTIFIED,
                SCHEDULING_UNIT_ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_UPDATED_ERROR, schedulingUnit.getId());
        }
    }

    @Override
    protected Set<FileExtension> getSupportedFileExtensions() {
        return SUPPORTED_FILE_EXTENSIONS;
    }

    @Override
    protected String getEntityName() {
        return SCHEDULING_UNIT_ENTITY_NAME;
    }
}
