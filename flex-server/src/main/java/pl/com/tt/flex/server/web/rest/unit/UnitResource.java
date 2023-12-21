package pl.com.tt.flex.server.web.rest.unit;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.web.util.HeaderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.tt.flex.model.service.dto.file.FileDTO;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.screen.enumeration.Screen;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity_;
import pl.com.tt.flex.server.service.unit.UnitQueryService;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitCriteria;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;
import pl.com.tt.flex.server.service.unit.dto.UnitMinDTO;
import pl.com.tt.flex.server.service.unit.mapper.UnitGeoLocationMapper;
import pl.com.tt.flex.server.service.unit.mapper.UnitMapper;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.unit.UnitValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.UNIQUE_UNIT_NAME;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.UNIT_NOTHING_TO_EXPORT;

/**
 * Common REST controller for managing {@link UnitEntity} for all web modules.
 */
@Slf4j
@RestController
@RequestMapping("/api")
public class UnitResource {

    public static final String ENTITY_NAME = "unit";

    @Value("${jhipster.clientApp.name}")
    protected String applicationName;

    protected final UnitService unitService;
    protected final UnitQueryService unitQueryService;
    protected final UnitMapper unitMapper;
    protected final UnitGeoLocationMapper unitGeoLocationMapper;
    protected final UnitValidator unitValidator;
    protected final UserService userService;

    public UnitResource(UnitService unitService, UnitQueryService unitQueryService, UnitMapper unitMapper, UnitGeoLocationMapper unitGeoLocationMapper,
        UnitValidator unitValidator, UserService userService) {
        this.unitService = unitService;
        this.unitQueryService = unitQueryService;
        this.unitMapper = unitMapper;
        this.unitGeoLocationMapper = unitGeoLocationMapper;
        this.unitValidator = unitValidator;
        this.userService = userService;
    }

    protected ResponseEntity<UnitDTO> createUnit(UnitDTO unitDTO) throws ObjectValidationException, URISyntaxException {
        if (unitDTO.getId() != null) {
            throw new BadRequestAlertException("A new unit cannot already have an ID", ENTITY_NAME, "idexists");
        }
        unitDTO.setName(StringUtils.normalizeSpace(unitDTO.getName()));
        if (unitService.existsByNameLowerCase(unitDTO.getName())) {
            throw new ObjectValidationException("DER with given name already exists.", UNIQUE_UNIT_NAME);
        }
        unitValidator.checkIfUserCanCreateUnits();
        unitValidator.checkValid(unitDTO);
        UnitDTO result = unitService.save(unitMapper.toEntity(unitDTO));
        unitService.sendInformingAboutUnitCreation(unitDTO, result);
        return ResponseEntity.created(new URI("/api/units/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString())).body(result);
    }

    protected ResponseEntity<UnitDTO> updateUnit(UnitDTO unitDTO) throws ObjectValidationException {
        if (unitDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        unitValidator.checkModifiable(unitDTO);
        UnitDTO oldUnit = unitService.findById(unitDTO.getId()).get();
        UnitDTO result = unitService.save(unitMapper.toEntity(unitDTO));
        unitService.sendInformingAboutUnitModification(oldUnit, result);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, unitDTO.getId().toString())).body(result);
    }

    protected ResponseEntity<List<UnitMinDTO>> getAllUnitsMinimal(UnitCriteria unitCriteria) {
        unitCriteria.setCertified((BooleanFilter) new BooleanFilter().setEquals(true));
        unitCriteria.setValidTo((InstantFilter) new InstantFilter().setGreaterThan(InstantUtil.now()));
        return ResponseEntity.ok(unitQueryService.findMinByCriteria(unitCriteria, Sort.by(UnitEntity_.NAME)));
    }

    protected ResponseEntity<UnitDTO> getUnit(Long id) {
        Optional<UnitDTO> optUnitDTO = unitService.findById(id);
        if (optUnitDTO.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        UnitDTO unitDTO = optUnitDTO.get();
        unitDTO.setGeoLocations(unitGeoLocationMapper.toDto(unitService.findGeoLocationsOfUnit(unitDTO.getId())));
        return ResponseEntity.ok(unitDTO);
    }

    protected ResponseEntity<Void> deleteUnit(Long id) throws ObjectValidationException {
        unitValidator.checkDeletable(id);
        unitService.delete(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }

    // Pobrane listy obiektow za pomoca metod findByCriteria(Criteria criteria, Sort sort) oraz findByCriteria(Criteria criteria, Pageable pageable)
    // roznia sie w kolejnosci sortowania obiektow. Z tego wzgledu do pobierania listy obiektow do eksportu uzyto metody findByCriteria(Criteria criteria, Pageable pageable)
    // tej samej co do pobrania listy obiektow na front (do widoku).
    protected ResponseEntity<FileDTO> exportUnits(UnitCriteria criteria, Pageable pageable, Screen screen, boolean isOnlyDisplayedData) throws IOException {
        int size = (int) unitQueryService.countByCriteria(criteria);
        if (size == 0) {
            throw new ObjectValidationException("Nothing to export", UNIT_NOTHING_TO_EXPORT);
        }
        PageRequest pageRequest = PageRequest.of(0, size, pageable.getSort());
        Page<UnitDTO> unitPage = unitQueryService.findByCriteria(criteria, pageRequest);
        return ResponseEntity.ok().body(unitService.exportUnitsToFile(unitPage.getContent(), isOnlyDisplayedData, screen));
    }

    protected ResponseEntity<List<UnitMinDTO>> getAllByFspId(Long fspId) throws URISyntaxException, ObjectValidationException {
        return ResponseEntity.ok(unitService.getAllByFspId(fspId));
    }

    /**
     * Endpoint pobierający jednostki do modali w subportfolio. Podczas dodawania pobierane są jednostki danego fspa, a podczas edycji jednostki danego fspa
     * i przypisane do edytowanego subportfolio
     */
    protected ResponseEntity<List<UnitMinDTO>> getAllForSubportfolioModalSelect(Long fspaId, Long subportfolioId) throws URISyntaxException, ObjectValidationException {
        List<UnitMinDTO> unitMinDTOS = unitService.getAllForSubportfolioModalSelect(fspaId, subportfolioId);
        return ResponseEntity.ok(unitMinDTOS);
    }

    protected static class UnitResourceException extends RuntimeException {
        protected UnitResourceException(String message) {
            super(message);
        }
    }
}
