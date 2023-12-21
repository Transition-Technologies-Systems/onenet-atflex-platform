package pl.com.tt.flex.server.repository.schedulingUnit;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data  repository for the SchedulingUnitEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SchedulingUnitRepository extends AbstractJpaRepository<SchedulingUnitEntity, Long> {

    Optional<SchedulingUnitEntity> findByNameIgnoreCase(String name);

    Optional<SchedulingUnitEntity> findByIdAndBspId(Long id, Long fspId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO(su.id, su.name) FROM SchedulingUnitEntity su JOIN su.units u WHERE u.id = :unitId")
    Optional<SchedulingUnitMinDTO> findByUnit(@Param("unitId") Long unitId);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO(sue.id, sue.name) FROM SchedulingUnitEntity sue " +
        "WHERE sue.bsp.id = :bspId AND sue.active = true AND sue.readyForTests = false AND sue.certified = false")
    List<SchedulingUnitMinDTO> findByBspIdAndActiveTrueAndReadyForTestsFalseAndCertifiedFalse(@Param("bspId") Long bspId);

    @Query("SELECT su FROM SchedulingUnitEntity su " +
        "join su.schedulingUnitType.products prod " +
        "WHERE su.bsp.id = :bspId AND prod.id in (:productId) AND su.certified = true AND su.active = true")
    List<SchedulingUnitEntity> findAllRegisteredSchedulingUnitsForBspAndProduct(@Param("bspId") Long bspId, @Param("productId") Long productId);

    List<SchedulingUnitEntity> findAllByBspIdAndActiveTrueAndReadyForTestsFalseAndCertifiedFalse(Long bspId);

    boolean existsByIdAndBspId(Long schedulingUnitId, Long bspId);

    @Query("SELECT CASE WHEN count(su)> 0 THEN true ELSE false END FROM SchedulingUnitEntity su " +
        "join su.schedulingUnitType.products prod " +
        "WHERE su.bsp.id = :bspId AND prod.id in (:productId) AND su.certified = true AND su.active = true")
    boolean existsActiveCertifiedByBspIdAndProductId(@Param("bspId") Long bspId, @Param("productId") Long productId);

    @Query("SELECT CASE WHEN count(su)> 0 THEN true ELSE false END FROM SchedulingUnitEntity su " +
        "join su.schedulingUnitType.products prod " +
        "WHERE prod.id in (:productId) AND su.certified = true AND su.active = true")
    boolean existsActiveCertifiedByProductId(@Param("productId") Long productId);

    @Query("SELECT CASE WHEN count(su)> 0 THEN true ELSE false END FROM SchedulingUnitEntity su " +
        "join su.schedulingUnitType.products prod " +
        "WHERE su.id = :schedulingUnitId AND prod.id = :productId")
    boolean existsBySchedulingUnitIdAndProductId(@Param("schedulingUnitId") Long schedulingUnitId, @Param("productId") Long productId);

    @Modifying
    @Query("UPDATE UnitEntity u SET u.schedulingUnit = null WHERE u.schedulingUnit.id = :schedulingUnitId AND u.id IN (:dersToRemove)")
    void removeDersFromSchedulingUnit(@Param("dersToRemove") List<Long> dersToRemove, @Param("schedulingUnitId") Long schedulingUnitId);

    @Query("SELECT su.bsp.id FROM SchedulingUnitEntity su WHERE su.id = :schedulingUnitId")
	Long getBspIdOfScheduling(@Param("schedulingUnitId") Long schedulingUnitId);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.schedulingUnit.SchedulingUnitMinDTO(su.id, su.name) " +
        "FROM SchedulingUnitEntity su JOIN su.units u WHERE u.id in (:derIds) AND ROWNUM = 1")
    Optional<SchedulingUnitMinDTO> findFirstByUnitIn(@Param("derIds") List<Long> derIds);

    @Query("SELECT CASE WHEN count(u)> 0 THEN true ELSE false END FROM UnitEntity u " +
        "WHERE u.schedulingUnit.id = :schedulingUnitId")
    boolean existsByIdAndUnitsAssigned(@Param("schedulingUnitId") Long schedulingUnitId);

    @Query("SELECT CASE WHEN count(u)> 0 THEN true ELSE false END " +
        "FROM SchedulingUnitEntity su JOIN su.units u " +
        "WHERE u.id = :unitId AND su.readyForTests = true")
    boolean existsByUnitIdAndReadyForTestsTrue(@Param("unitId") Long unitId);
}
