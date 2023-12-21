package pl.com.tt.flex.server.service.schedulingUnit;

import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitFileEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalType;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDropdownSelectDTO;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitProposalDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link SchedulingUnitEntity}.
 */
public interface SchedulingUnitService extends AbstractService<SchedulingUnitEntity, SchedulingUnitDTO, Long> {

    SchedulingUnitDTO update(SchedulingUnitDTO schedulingUnitDTO, List<Long> dersToRemove, List<Long> filesToRemove) throws ObjectValidationException;

    Optional<SchedulingUnitFileEntity> getSchedulingUnitFileByFileId(Long fileId);

    List<FileDTO> getZipWithAllFilesOfSchedulingUnit(Long schedulingUnitId);

    Optional<SchedulingUnitDTO> findByIdAndBspId(Long id, Long fspId);

    Optional<SchedulingUnitFileEntity> getSchedulingUnitFileByFileIdAndSchedulingUnitBspId(Long fileId, Long fspId);

    FileDTO exportSchedulingUnitToFile(List<SchedulingUnitDTO> schedulingUnitToExport, boolean isOnlyDisplayedData, Screen screen) throws IOException;

    SchedulingUnitProposalDTO createOrResendSchedulingUnitProposal(SchedulingUnitProposalDTO proposalDTO) throws ObjectValidationException;

    void notifyUsersThatSchedulingUnitIsReadyForTests(SchedulingUnitDTO schedulingUnitDTO, List<UserEntity> recipients);

    void acceptSchedulingUnitProposalByFsp(Long proposalId) throws ObjectValidationException;

    void acceptSchedulingUnitProposalByBsp(Long proposalId, Long schedulingUnitId) throws ObjectValidationException;

    void rejectSchedulingUnitProposal(Long id);

    void cancelSchedulingUnitProposal(Long proposalId);

    List<UnitMinDTO> getSchedulingUnitDers(Long schedulingUnitId);

    List<UnitMinDTO> getSchedulingUnitDersForFsp(Long schedulingUnitId, Long fspId);

    List<UnitMinDTO> getAvailableFspDersForNewSchedulingUnitProposal(Long fspId, Long bspId);

    boolean isFspJoinedWithOtherBspBySchedulingUnit(Long fspId, Long bspId);

    List<UnitMinDTO> findAvailableFspaSubportfolioDersForNewSchedulingUnitProposal(Long subportfolioId, Long bspId, Long fspaId);

    Optional<SchedulingUnitMinDTO> findByUnit(Long unitId);

    boolean existsByUnitIdAndReadyForTestsTrue(Long unitId);

    boolean canDerBeAddedToBspSchedulingUnits(Long derId, Long bspId);

    boolean isAllFspaSubportfoliosJoinedWithOtherBspBySchedulingUnit(Long fspId, Long bspId);

    List<SchedulingUnitMinDTO> getAllCurrentBspSchedulingUnitsToWhichOnesPointedDerCanBeJoined(Long bspId, Long derId);

	List<SchedulingUnitDropdownSelectDTO> findAllRegisteredSchedulingUnitsForBspAndProduct(Long bspId, Long productId);

    boolean existsBySchedulingUnitIdAndBspId(Long schedulingUnitId, Long bspId);

    boolean existsActiveCertifiedByUserAndProductId(UserEntity user, Long productId);

    boolean existsActiveCertifiedByProductId(Long productId);

    boolean isFspJoinedWithBspBySchedulingUnit(Long fspId, Long bspId);

    boolean existsBySchedulingUnitIdAndProductId(Long schedulingUnitId, Long productId);

    Optional<SchedulingUnitProposalDTO> findSchedulingUnitProposalById(Long proposalId);

    List<FspCompanyMinDTO> findAllBspsUsedInFspProposals(Long fspId, SchedulingUnitProposalType proposalType);

    List<FspCompanyMinDTO> findAllFspsUsedInBspProposals(Long bspId, SchedulingUnitProposalType proposalType);

    List<FspCompanyMinDTO> findAllBspsUsedInAllFspsProposals(SchedulingUnitProposalType proposalType);

    List<FspCompanyMinDTO> findAllFspsUsedInAllBspsProposals(SchedulingUnitProposalType proposalType);

    List<Long> findAllWithJoinedDersOfFsp(Long fspId);

    void registerNewNotificationForSchedulingUnitCreation(SchedulingUnitDTO schedulingUnitDTO);

    void registerNewNotificationForSchedulingUnitEdition(SchedulingUnitDTO modifiedScheduling, SchedulingUnitDTO oldScheduling);

    void sendMailInformingAboutSchedulingUnitCreation(SchedulingUnitDTO result);

    void sendMailInformingAboutSchedulingUnitModification(SchedulingUnitDTO oldSchedulingUnit, SchedulingUnitDTO modifiedScheduling);

    void sendNotificationInformingAboutRegistered(SchedulingUnitDTO result);

    void sendMailInformingAboutRegistered(SchedulingUnitDTO result);

    Long findOwnerBspId(Long schedulingUnitId);
}
