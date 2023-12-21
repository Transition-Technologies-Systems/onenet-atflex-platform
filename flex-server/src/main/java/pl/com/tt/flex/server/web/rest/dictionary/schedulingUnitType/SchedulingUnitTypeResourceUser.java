package pl.com.tt.flex.server.web.rest.dictionary.schedulingUnitType;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.com.tt.flex.server.domain.schedulingUnit.SchedulingUnitTypeEntity;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.SchedulingUnitTypeQueryService;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.SchedulingUnitTypeService;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeCriteria;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeDTO;
import pl.com.tt.flex.server.service.dictionary.schedulingUnitType.dto.SchedulingUnitTypeMinDTO;
import pl.com.tt.flex.server.service.schedulingUnit.SchedulingUnitService;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;

import java.util.List;

import static pl.com.tt.flex.model.security.permission.Authority.FLEX_USER_SCHEDULING_UNIT_TYPE_VIEW;
import static pl.com.tt.flex.server.config.Constants.FLEX_USER_APP_NAME;

/**
 * REST controller for managing {@link SchedulingUnitTypeEntity} for USER.
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
public class SchedulingUnitTypeResourceUser extends SchedulingUnitTypeResource {

    protected final SchedulingUnitService schedulingUnitService;
    protected final UserService userService;
    public SchedulingUnitTypeResourceUser(SchedulingUnitTypeQueryService schedulingUnitTypeQueryService, SchedulingUnitTypeService schedulingUnitTypeService, SchedulingUnitService schedulingUnitService, UserService userService) {
        super(schedulingUnitTypeQueryService, schedulingUnitTypeService);
        this.schedulingUnitService = schedulingUnitService;
        this.userService = userService;
    }

    /**
     * {@code GET  user/su-types} : get all the schedulingUnitTypes.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of schedulingUnitTypes in body.
     */
    @GetMapping("/su-types")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_TYPE_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitTypeDTO>> getAllSchedulingUnitTypes(SchedulingUnitTypeCriteria criteria, Pageable pageable) {
        log.debug("{} - REST request to get SchedulingUnitTypes by criteria: {}", FLEX_USER_APP_NAME, criteria);
        return super.getAllSchedulingUnitTypes(criteria, pageable);
    }

    /**
     * {@code GET  user/su-types/minimal} : get all the schedulingUnitTypes minimal
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of schedulingUnitTypes in body.
     */
    @GetMapping("/su-types/minimal")
    @PreAuthorize("hasAuthority(\"" + FLEX_USER_SCHEDULING_UNIT_TYPE_VIEW + "\")")
    public ResponseEntity<List<SchedulingUnitTypeMinDTO>> getSchedulingUnitTypesMinDto() {
        log.debug("{} - REST request to get SchedulingUnitTypes minimal", FLEX_USER_APP_NAME);
        UserDTO user = userService.getCurrentUserDTO().orElseThrow(() -> new SchedulingUnitTypeResourceException("Current logged-in user not found"));
        return ResponseEntity.ok(schedulingUnitTypeService.getSchedulingUnitTypesMinimalByUserRole(user));
    }

    protected static class SchedulingUnitTypeResourceException extends RuntimeException {
        protected SchedulingUnitTypeResourceException(String message) {
            super(message);
        }
    }
}
