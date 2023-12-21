package pl.com.tt.flex.server.repository.localizationType;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.com.tt.flex.model.service.dto.localization.LocalizationType;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.repository.AbstractJpaRepository;

import java.util.List;

/**
 * Spring Data  repository for the LocalizationTypeEntity entity.
 */
@SuppressWarnings("unused")
@Repository
public interface LocalizationTypeRepository extends AbstractJpaRepository<LocalizationTypeEntity, Long> {

    boolean existsByNameAndType(String name, LocalizationType type);

    List<LocalizationTypeEntity> findAllByTypeInOrderByName(List<LocalizationType> localizationTypes);

    @Query("SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM UnitEntity unit " +
        "JOIN unit.couplingPointIdTypes cpiType " +
        "WHERE cpiType.id = :localizationTypeId")
    boolean existUnitWithCouplingPointTypeId(@Param("localizationTypeId") Long localizationTypeId);

    @Query("SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM UnitEntity unit " +
        "JOIN unit.powerStationTypes psType " +
        "WHERE psType.id = :localizationTypeId")
    boolean existUnitWithPowerStationTypeId(@Param("localizationTypeId") Long localizationTypeId);

    @Query("SELECT CASE WHEN count(*) > 0 THEN true ELSE false END FROM SubportfolioEntity subportfolio " +
        "JOIN subportfolio.couplingPointIdTypes cpiType " +
        "WHERE cpiType.id = :localizationTypeId")
    boolean existSubportfolioWithCouplingPointTypeId(@Param("localizationTypeId") Long localizationTypeId);

    @Query("SELECT u.couplingPointIdTypes FROM UnitEntity u WHERE u.id IN :unitIds")
    List<LocalizationTypeEntity> findAllByUnitIds(@Param("unitIds") List<Long> unitIds);
}
