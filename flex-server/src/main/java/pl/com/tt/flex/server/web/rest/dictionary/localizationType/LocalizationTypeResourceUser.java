package pl.com.tt.flex.server.web.rest.dictionary.localizationType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.com.tt.flex.model.service.dto.localization.LocalizationType;
import pl.com.tt.flex.model.service.dto.localization.LocalizationTypeDTO;
import pl.com.tt.flex.server.domain.unit.LocalizationTypeEntity;
import pl.com.tt.flex.server.service.dictionary.localizationType.LocalizationTypeService;

import java.util.List;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_LOCALIZATION_TYPE_VIEW;

/**
 * REST controller for managing {@link LocalizationTypeEntity} for FLEX-USER web module
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class LocalizationTypeResourceUser {

    public static final String ENTITY_NAME = "localizationType";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final LocalizationTypeService localizationTypeService;

    public LocalizationTypeResourceUser(LocalizationTypeService localizationTypeService) {
        this.localizationTypeService = localizationTypeService;
    }

    /**
     * {@code GET  /localization-types/get-by-type} : get all the localization dictionary by type.
     *
     * @param types the types of Localization dictionary. Example : {@link LocalizationType#COUPLING_POINT_ID}
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of localizationTypes in body.
     */
    @GetMapping("/localization-types/get-by-type")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_LOCALIZATION_TYPE_VIEW + "\")")
    public ResponseEntity<List<LocalizationTypeDTO>> getAllByType(@RequestParam List<LocalizationType> types) {
        log.debug("FLEX-USER - REST request to get LocalizationTypes by types: {}", types);
        List<LocalizationTypeDTO> localizationTypeDTOS = localizationTypeService.findAllByTypes(types);
        return ResponseEntity.ok().body(localizationTypeDTOS);
    }

    @GetMapping("/localization-types/get-by-unit-ids")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_LOCALIZATION_TYPE_VIEW + "\")")
    public ResponseEntity<List<LocalizationTypeDTO>> getAllByUnitId(@RequestParam List<Long> unitIds) {
        log.debug("FLEX-ADMIN - REST request to get LocalizationTypes by unitIds");
        List<LocalizationTypeDTO> localizationTypeDTOS = localizationTypeService.findAllByUnitIds(unitIds);
        return ResponseEntity.ok().body(localizationTypeDTOS);
    }
}
