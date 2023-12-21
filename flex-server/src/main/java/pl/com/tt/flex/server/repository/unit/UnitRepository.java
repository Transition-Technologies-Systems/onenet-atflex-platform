package pl.com.tt.flex.server.repository.unit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data  repository for the UnitEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface UnitRepository extends AbstractJpaRepository<UnitEntity, Long> {

    @Query("SELECT u FROM UnitEntity u WHERE u.active = true AND SYS_EXTRACT_UTC(SYSTIMESTAMP) NOT BETWEEN u.validFrom AND u.validTo")
    List<UnitEntity> findUnitsToDeactivateByValidFromToDates();

    @Query("SELECT u FROM UnitEntity u WHERE u.active = false AND SYS_EXTRACT_UTC(SYSTIMESTAMP) BETWEEN u.validFrom AND u.validTo")
    List<UnitEntity> findUnitsToActivateByValidFromToDates();

    // Sprawdzamy czy istnieje jakis DER z wskazanego Subportfolio, ktory jest polaczony z innym BSP niz wskazany
    boolean existsBySubportfolioIdAndSchedulingUnitBspIdNot(Long subportfolioId, Long bspId);

    //Pobieranie derów do multiselecta w modalu dodawania i edycji subportfolio. Podczas dodawania pobieramy dery które nie są przypisane do żadnego subportfolio i zostały stworzone przez danego fspa (fspaId), podczas edycji pobieramy dery stworzone przez danego fspa oraz przypięte do danego subportfolio (subportfolioId)
    @Query("SELECT u FROM UnitEntity u WHERE u.active = true AND u.fsp.id = :fspaId AND (u.subportfolio is null OR u.subportfolio.id = :subportfolioId) AND u.certified = true")
    List<UnitEntity> getAllForSubportfolioModalSelect(@Param("fspaId") Long fspId, @Param("subportfolioId") Long subportfolioId);

    List<UnitEntity> findByFspIdAndSubportfolioNotNull(@Param("fspId") Long fspId);

    @Query("SELECT u FROM UnitEntity u JOIN SubportfolioEntity sp ON u.subportfolio.id = sp.id AND sp.fspa.id = :fspId WHERE sp.certified = 1 AND u.active = 1")
    List<UnitEntity> findCertifiedDersByFspId(@Param("fspId") Long fspId);

    Optional<UnitEntity> findByIdAndFspId(Long unitId, Long fspId);

    List<UnitEntity> findBySchedulingUnitIdOrderByIdDesc(Long schedulingUnitId);

    List<UnitEntity> findBySchedulingUnitIdAndFspIdOrderByIdDesc(Long schedulingUnitId, Long fspId);

    //jeśli w liście derów (derIds) przekazywanej przy tworzeniu/edycji subportfolio z frontu istnieją dery,
    // które mają fspaId inne niż fspaId podane w subportfolio to je zliczamy (następnie jeśli wystąpiły takie dery to powinien polecieć błąd)
    @Query("SELECT COUNT(u) FROM UnitEntity u WHERE u.fsp.id != :fspId AND u.id IN (:derIds)")
    Long countFspIdAndSubportfolioDTODerIds(@Param("fspId") Long fspId, @Param("derIds") List<Long> subportfolioDTODerIds);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    boolean existsBySchedulingUnitId(Long id);

    List<UnitEntity> findAllByFspIdAndCertifiedTrue(@Param("fspId") Long fspId);

    /**
     * Pobieranie derów do okna edycji flex potential. W multiselekcie mają zostać wyświetlone dery przypisane do edytowanego potencjału
     * oraz dery utworzone przez wybranego fspa, które są przypisane do subportfolio
     */
    @Query(value = "SELECT NEW pl.com.tt.flex.server.service.unit.dto.UnitMinDTO(u.id, u.name, u.subportfolio.name) FROM UnitEntity u WHERE u.fsp.id = :fspId AND u.subportfolio.id != null AND u.certified = true")
    List<UnitMinDTO> getUnitsForFlexPotentialModalSelect(@Param("fspId") Long fspaId);

    boolean existsByFspIdAndSchedulingUnitBspId(Long fspId, Long bspId);

    boolean existsByFspIdAndSchedulingUnitBspIdNot(Long fspId, Long bspId);

    @Query("SELECT NEW pl.com.tt.flex.server.service.unit.dto.UnitMinDTO(u.id, u.name) FROM UnitEntity u " +
        "WHERE u.subportfolio.id = :subportfolioId AND u.schedulingUnit IS NULL")
    List<UnitMinDTO> findAllBySubportfolioIdAndSchedulingUnitIsNull(@Param("subportfolioId") Long subportfolioId);

    @Query("SELECT NEW pl.com.tt.flex.server.service.unit.dto.UnitMinDTO(u.id, u.name, u.fsp.id) FROM UnitEntity u WHERE u.id IN(:dersToRemove)")
    List<UnitMinDTO> findDersNameAndFsp(@Param("dersToRemove") List<Long> dersToRemove);

    /**
     * Produkty na który wskazany DER posiada "Flex register"
     */
    @Query(value = "SELECT fp.product_id FROM FLEX_POTENTIAL_UNITS fpu " +
        "JOIN FLEX_POTENTIAL fp ON fp.id = fpu.flex_potential_id " +
        "WHERE fpu.unit_id = :derId AND fp.is_register = 1", nativeQuery = true)
    List<Long> findDerRegisteredPotentialsProductsIds(@Param("derId") Long derId);

    @Query("SELECT NEW pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO(u.fsp.id, u.fsp.companyName, u.fsp.role) FROM UnitEntity u WHERE u.id = :derId")
    FspCompanyMinDTO getDerFspMin(@Param("derId") Long derId);

    boolean existsByIdInAndFspIdAndSchedulingUnitBspIdNot(List<Long> dersIds, Long fspId, Long bspId);

    @Query("SELECT NEW pl.com.tt.flex.server.service.unit.dto.UnitMinDTO(u.id, u.name, u.fsp.id, u.sourcePower, u.pMin, u.directionOfDeviation) FROM UnitEntity u WHERE LOWER(u.name) = LOWER(:name) AND u.certified = true")
    Optional<UnitMinDTO> findUnitByName(@Param("name") String name);

    Optional<UnitEntity> findByName(String name);

    @Query("SELECT NEW pl.com.tt.flex.server.service.unit.dto.UnitMinDTO(u.id, u.name, u.fsp.id, u.sourcePower, u.pMin, u.directionOfDeviation) FROM UnitEntity u WHERE u.code = :code AND u.fsp.companyName = :fspName")
    Optional<UnitMinDTO> findByCodeAndFspName(@Param("code") String code, @Param("fspName") String fspName);

    @Query("select count(*) from UnitEntity u where u.certified = true")
    BigDecimal countCertified();

    boolean existsByIdInAndSchedulingUnitNotNull(List<Long> dersIds);

    @Query("SELECT NEW pl.com.tt.flex.server.service.unit.dto.UnitMinDTO(u.id, u.name) FROM UnitEntity u " +
        "WHERE u.subportfolio IS NULL AND u.fsp.id = :fspaId AND u.certified = true")
	List<UnitMinDTO> findAllByFspIdAndSubportfolioIdNullAndCertifiedTrue(@Param("fspaId") Long fspaId);

    boolean existsByFspIdAndSubportfolioNullAndSchedulingUnitNullAndCertifiedTrueAndActiveTrue(Long fspId);
}
