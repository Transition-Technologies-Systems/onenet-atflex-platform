package pl.com.tt.flex.server.repository.schedulingUnit;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitProposalEntity;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalStatus;
import pl.com.tt.flex.server.domain.schedulingUnit.enumeration.SchedulingUnitProposalType;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data repository for the SchedulingUnitProposalEntity.
 */
@SuppressWarnings("unused")
@Repository
public interface SchedulingUnitProposalRepository extends AbstractJpaRepository<SchedulingUnitProposalEntity, Long> {

    /**
     * {@link SchedulingUnitProposalStatus#NEW}
     * {@link SchedulingUnitProposalStatus#CONNECTED_WITH_OTHER}
     */
    @Modifying
    @Query("UPDATE SchedulingUnitProposalEntity sup SET sup.status = 'CONNECTED_WITH_OTHER' WHERE sup.unit.id = :unitId and sup.status = 'NEW'")
    void rejectProposalsForUnit(@Param("unitId") Long unitId);

    @Modifying
    @Query("UPDATE NotificationUserEntity SET read = true WHERE id IN " +
        "(SELECT nu.id FROM NotificationUserEntity nu " +
        "JOIN NotificationEntity n ON nu.notification.id = n.id " +
        "JOIN NotificationParamEntity np ON np.name = 'SCHEDULING_UNIT_PROPOSAL_ID' AND np.value = :proposalId " +
        "WHERE nu.notification.id = n.id AND np.notification.id = n.id) " +
        "AND read = false")
    void markProposalNotificationAsRead(@Param("proposalId") String proposalId);

    @Query("SELECT u FROM UnitEntity u" +
        " WHERE u.fsp.id = :fspId" +
        " AND u.certified = true" +
        " AND u.active = true" +
        " AND u.schedulingUnit.id IS NULL")
    List<UnitEntity> getFspDersNotJoinedToAnySchedulingUnit(@Param("fspId") Long fspId);

    /**
     * @see SchedulingUnitProposalStatus#NEW
     * @see SchedulingUnitProposalStatus#ACCEPTED
     */
    @Modifying
    @Query("UPDATE SchedulingUnitProposalEntity sup SET sup.status = 'ACCEPTED' WHERE sup.id = :proposalId AND sup.status = 'NEW'")
    int acceptProposal(@Param("proposalId") Long proposalId);

    /**
     * @see SchedulingUnitProposalStatus#NEW
     * @see SchedulingUnitProposalStatus#REJECTED
     */
    @Modifying
    @Query("UPDATE SchedulingUnitProposalEntity sup SET sup.status = 'REJECTED', sup.statusSortOrder = 2 WHERE sup.id = :proposalId AND sup.status = 'NEW'")
    void rejectProposal(@Param("proposalId") Long proposalId);

    /**
     * @see SchedulingUnitProposalStatus#NEW
     * @see SchedulingUnitProposalStatus#CANCELLED
     */
    @Modifying
    @Query("UPDATE SchedulingUnitProposalEntity sup SET sup.status = 'CANCELLED', sup.statusSortOrder = 2 WHERE sup.id = :proposalId AND sup.status = 'NEW'")
    void cancelProposal(@Param("proposalId") Long proposalId);

    @Query(value = "SELECT DISTINCT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(sup.bsp.id, sup.bsp.companyName, sup.bsp.role) " +
        "FROM SchedulingUnitProposalEntity sup WHERE sup.unit.fsp.id = :fspId AND sup.proposalType = :proposalType ORDER BY sup.bsp.companyName")
    List<FspCompanyMinDTO> findAllBspsUsedInFspProposals(@Param("fspId") Long fspId, @Param("proposalType") SchedulingUnitProposalType proposalType);

    @Query(value = "SELECT DISTINCT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(sup.unit.fsp.id, sup.unit.fsp.companyName, sup.unit.fsp.role) " +
        "FROM SchedulingUnitProposalEntity sup WHERE sup.bsp.id = :bspId AND sup.proposalType = :proposalType ORDER BY sup.unit.fsp.companyName")
    List<FspCompanyMinDTO> findAllFspsUsedInBspProposals(@Param("bspId") Long bspId, @Param("proposalType") SchedulingUnitProposalType proposalType);

    @Query(value = "SELECT DISTINCT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(sup.bsp.id, sup.bsp.companyName, sup.bsp.role) " +
        "FROM SchedulingUnitProposalEntity sup WHERE sup.proposalType = :proposalType ORDER BY sup.bsp.companyName")
    List<FspCompanyMinDTO> findAllBspsUsedInAllFspsProposals(@Param("proposalType") SchedulingUnitProposalType proposalType);

    @Query(value = "SELECT DISTINCT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(sup.unit.fsp.id, sup.unit.fsp.companyName, sup.unit.fsp.role) " +
        "FROM SchedulingUnitProposalEntity sup WHERE sup.proposalType = :proposalType ORDER BY sup.unit.fsp.companyName")
    List<FspCompanyMinDTO> findAllFspsUsedInAllBspsProposals(@Param("proposalType") SchedulingUnitProposalType proposalType);

    @Query(value = "SELECT DISTINCT u.schedulingUnit.id FROM UnitEntity u WHERE u.schedulingUnit IS NOT NULL AND u.fsp.id = :fspId ")
    List<Long> findAllWithJoinedDersOfFsp(@Param("fspId") Long fspId);

    @Query(value = "SELECT DISTINCT u.schedulingUnit.schedulingUnitType.id FROM UnitEntity u WHERE u.schedulingUnit IS NOT NULL AND u.fsp.id = :fspId ")
    List<Long> findAllUnitTypesWithJoinedDersOfFsp(@Param("fspId") Long fspId);

    boolean existsByUnitIdAndBspIdAndProposalTypeAndStatus(Long unitId, Long bspId, SchedulingUnitProposalType request, SchedulingUnitProposalStatus status);

    Optional<SchedulingUnitProposalEntity> findOneByUnitIdAndSchedulingUnitIdAndProposalTypeAndStatusIn(Long unitId, Long schedulingUnitId,
        SchedulingUnitProposalType proposalType, List<SchedulingUnitProposalStatus> status);

    Optional<SchedulingUnitProposalEntity> findOneByUnitIdAndBspIdAndProposalTypeAndStatusIn(Long unitId, Long schedulingUnitId,
        SchedulingUnitProposalType proposalType, List<SchedulingUnitProposalStatus> status);
}
