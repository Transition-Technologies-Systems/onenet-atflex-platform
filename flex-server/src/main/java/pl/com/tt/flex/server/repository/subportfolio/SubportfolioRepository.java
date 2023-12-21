package pl.com.tt.flex.server.repository.subportfolio;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioMinDTO;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data  repository for the SubportfolioEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SubportfolioRepository extends AbstractJpaRepository<SubportfolioEntity, Long> {

    Optional<SubportfolioEntity> findByIdAndFspaId(Long id, Long fspId);

    @Query("SELECT s.id FROM SubportfolioEntity s JOIN s.units u WHERE u.id = :unitId")
    List<Long> findByUnit(@Param("unitId") Long unitId);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	List<SubportfolioEntity> findAllByFspaId(Long fspaId);

    @Query("SELECT DISTINCT s FROM SubportfolioEntity s WHERE s.certified = true AND s.fspa.id = :fspaId")
    List<SubportfolioEntity> findAllCertifiedByFspaId(@Param("fspaId") Long fspaId);

    boolean existsByIdAndFspaId(Long subportfolioId, Long fspaId);

    @Query("SELECT DISTINCT s.name FROM SubportfolioEntity s")
    List<String> findNames();

    @Query("SELECT NEW pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioMinDTO(s.id, s.name) FROM SubportfolioEntity s " +
        "WHERE s.fspa.id = :fspaId AND s.certified = true AND s.active = true ORDER BY s.name")
    List<SubportfolioMinDTO> findAllFspaCertifiedSubportfoliosMin(@Param("fspaId") Long fspaId);

    @Modifying
    @Query("UPDATE UnitEntity u SET u.subportfolio = null WHERE u.subportfolio.id = :subportfolioId")
	void detachAllDersFromSubportfolio(@Param("subportfolioId") Long subportfolioId);

    @Query("SELECT u.id FROM UnitEntity u WHERE u.subportfolio.id IN (SELECT u2.subportfolio.id FROM UnitEntity u2 WHERE u2.id = :derId)")
    List<Long> findAllDerIdsFromDerSubportfolio(@Param("derId") Long derId);

    @Query("SELECT u.id FROM UnitEntity u WHERE u.subportfolio.id = :subportfolioId")
    List<Long> findAllDerIdsFromSubportfolio(@Param("subportfolioId") Long subportfolioId);
}
