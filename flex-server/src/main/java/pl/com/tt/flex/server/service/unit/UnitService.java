package pl.com.tt.flex.server.service.unit;

import pl.com.tt.flex.model.service.dto.fsp.FspCompanyMinDTO;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.unit.UnitGeoLocationEntity;
import pl.com.tt.flex.server.service.AbstractService;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link UnitEntity}.
 */
public interface UnitService extends AbstractService<UnitEntity, UnitDTO, Long> {

    List<UnitGeoLocationEntity> findGeoLocationsOfUnit(Long unitId);

    UnitDTO save(UnitEntity unitEntity);

    FileDTO exportUnitsToFile(List<UnitDTO> unitsToExport, boolean isOnlyDisplayedData, Screen screen) throws IOException;

    void deactivateUnitsByValidFromToDates();

    void activateUnitsByValidFromToDates();

    void sendInformingAboutUnitCreation(UnitDTO unitRequest, UnitDTO unitResult);

    void sendInformingAboutUnitModification(UnitDTO oldUnit, UnitDTO result);

    List<UnitMinDTO> getAllForSubportfolioModalSelect(Long fspaId, Long subportfolioId);

    List<UnitMinDTO> getSchedulingUnitDers(Long schedulingUnitId);

    List<UnitMinDTO> getFspSchedulingUnitDers(Long schedulingUnitId, Long fspId);

    boolean existsByNameLowerCaseAndIdNot(String name, Long id);

    boolean existsByNameLowerCase(String name);

    List<UnitMinDTO> getAllByFspId(Long fpId);

    boolean existsByFspIdAndSchedulingUnitBspId(Long fspId, Long bspId);

    boolean existsByFspIdAndSchedulingUnitBspIdNot(Long fspId, Long bspId);

    List<UnitMinDTO> findAllBySubportfolioIdAndSchedulingUnitIsNull(Long subportfolioId);

	boolean existsBySubportfolioIdAndSchedulingUnitBspIdNot(Long subportfolioId, Long bspId);

	List<UnitMinDTO> findDersNameAndFsp(List<Long> dersToRemove);

    Optional<UnitMinDTO> findUnitByNameIgnoreCase(String derName);

    List<Long> findDerRegisteredPotentialsProductsIds(Long derId);

    FspCompanyMinDTO getDerFspMin(Long derId);

    UnitEntity getById(Long id);

    UnitEntity getByName(String name);

    Optional<UnitMinDTO> findByCodeAndFspCompanyName(String unitCode, String fspCompanyName);

    List<UnitMinDTO> findAllWithoutSubportfolioByFspId(Long fspaId);

	boolean existsByFspIdAndNoSubportfolioAndNoSchedulingUnit(Long fspId);
}
