package pl.com.tt.flex.server.validator.fsp;

import io.github.jhipster.service.filter.BooleanFilter;
import io.github.jhipster.service.filter.LongFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.fsp.FspEntity;
import pl.com.tt.flex.server.repository.fsp.FspRepository;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.service.fsp.dto.FspDTO;
import pl.com.tt.flex.server.service.fsp.mapper.FspMapper;
import pl.com.tt.flex.server.service.potential.FlexPotentialQueryService;
import pl.com.tt.flex.server.service.potential.dto.FlexPotentialCriteria;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;
import pl.com.tt.flex.server.util.InstantUtil;
import pl.com.tt.flex.server.validator.ObjectValidator;
import pl.com.tt.flex.server.web.rest.errors.BadRequestAlertException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static java.util.Objects.nonNull;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;
import static pl.com.tt.flex.server.web.rest.fsp.FspResourceAdmin.ENTITY_NAME;

@Component
@RequiredArgsConstructor
public class FspValidator implements ObjectValidator<FspDTO, Long> {

    // constant for validation errors not handled in frontend layer
    public static final String ERR_VALIDATION = "error.validation.fsp";

    private final FspRepository fspRepository;
    private final UserService userService;
    private final FlexPotentialQueryService flexPotentialQueryService;
    private final FspMapper fspMapper;

    @Override
    public void checkValid(FspDTO fspDTO) throws ObjectValidationException {
        validDates(fspDTO);
        validActive(fspDTO);
        validRole(fspDTO);
    }

    @Override
    public void checkModifiable(FspDTO fspDTO) throws ObjectValidationException {
        checkValid(fspDTO);
        if (isDeactivateOperation(fspDTO) && fspHasJoinedActiveFlexPotentials(fspDTO)) {
            throw new ObjectValidationException("The service provider's FSP cannot be deactivated because of active FlexPotentials",
                CANNOT_DEACTIVATE_BECAUSE_OF_ACTIVE_FLEX_POTENTIALS, ENTITY_NAME, getActivityEvent(fspDTO), fspDTO.getId());
        }
    }

    public boolean isDeactivateOperation(FspDTO fspDTO) {
        FspEntity fspDb = fspRepository.findById(fspDTO.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return !fspDTO.isActive() && fspDb.isActive();
    }

    public boolean isActivateOperation(FspDTO fspDTO) {
        FspEntity fspDb = fspRepository.findById(fspDTO.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return fspDTO.isActive() && !fspDb.isActive();
    }

    @Override
    public void checkDeletable(Long fspId) throws ObjectValidationException {
        if (fspHasJoinedFlexPotentials(fspId)) {
            throw new ObjectValidationException("All FlexPotentials provided by the FSP must be removed before removing FSP",
                FSP_CANNOT_BE_DELETE_BECAUSE_OF_JOINED_FLEX_POTENTIALS, ENTITY_NAME, ActivityEvent.FSP_DELETED_ERROR, fspId);
        }
    }

    private void validActive(FspDTO fspDTO) throws ObjectValidationException {
        Instant now = InstantUtil.now();
        if (fspDTO.isActive() &&
            (fspDTO.getValidFrom().isAfter(now) ||
                (nonNull(fspDTO.getValidTo()) && fspDTO.getValidTo().isBefore(now))
            )) {
            throw new ObjectValidationException("Fsp cannot be active because present date is not between validFrom and validTo dates",
                FSP_CANNOT_BE_ACTIVE_BECAUSE_DATE_NOW_IS_NOT_BETWEEN_VALID_FROM_TO_DATES, ENTITY_NAME,
                getActivityEvent(fspDTO), fspDTO.getId());
        }
    }

    public void validUpdateRequest(FspDTO fspDTO) throws ObjectValidationException {
        if (fspDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        fspRepository.findById(fspDTO.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        userService.findOne(fspDTO.getRepresentative().getId()).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot find fsp's owner [userId: " + fspDTO.getRepresentative() + " ]"));
        checkModifiable(fspDTO);
    }

    private boolean fspHasJoinedActiveFlexPotentials(FspDTO fspDTO) {
        FlexPotentialCriteria flexPotentialCriteria = new FlexPotentialCriteria();
        flexPotentialCriteria.setActive((BooleanFilter) new BooleanFilter().setEquals(true));
        flexPotentialCriteria.setFspId((LongFilter) new LongFilter().setEquals(fspDTO.getId()));
        return !flexPotentialQueryService.findByCriteria(flexPotentialCriteria).isEmpty();
    }

    private boolean fspHasJoinedFlexPotentials(Long fspId) {
        FlexPotentialCriteria flexPotentialCriteria = new FlexPotentialCriteria();
        flexPotentialCriteria.setFspId((LongFilter) new LongFilter().setEquals(fspId));
        return !flexPotentialQueryService.findByCriteria(flexPotentialCriteria).isEmpty();
    }

    private final ActivityEvent getActivityEvent(FspDTO fspDTO) {
        return fspDTO.getId() == null ? ActivityEvent.FSP_CREATED_ERROR : ActivityEvent.FSP_UPDATED_ERROR;
    }

    private void validDates(FspDTO fspDTO) throws ObjectValidationException {
        Instant createdDate;
        if (fspDTO.getId() != null) {
            createdDate = fspRepository.findById(fspDTO.getId()).get().getCreatedDate();
        } else {
            createdDate = fspDTO.getCreatedDate();
        }
        // daty validFrom/validTo sprawdzamy dla pelnych godzin (11:00, 12:00 itd.)
        Instant createdDateHours = createdDate.truncatedTo(ChronoUnit.HOURS);
        Instant validFromHours = fspDTO.getValidFrom().truncatedTo(ChronoUnit.HOURS);
        if (validFromHours.isBefore(createdDateHours)) {
            throw new ObjectValidationException("ValidFrom is before createdDate", FROM_DATE_BEFORE_CREATED_DATE, ENTITY_NAME, getActivityEvent(fspDTO), fspDTO.getId());
        }
        if (nonNull(fspDTO.getValidTo())) {
            Instant validToHours = fspDTO.getValidTo().truncatedTo(ChronoUnit.HOURS);
            if (validFromHours.plus(1, ChronoUnit.HOURS).isAfter(validToHours)) {
                throw new ObjectValidationException("ValidTo has to be higher than validFrom", FROM_DATE_AFTER_TO_DATE, ENTITY_NAME, getActivityEvent(fspDTO), fspDTO.getId());
            }
        }
    }

    private void validRole(FspDTO fspDTO) throws ObjectValidationException {
        if (!Role.FSP_ORGANISATIONS_ROLES.contains(fspDTO.getRole())) {
            throw new ObjectValidationException("Selected role is not intended for organisations of FSP platform", ERR_VALIDATION, ENTITY_NAME);
        }
    }

    //fsp moga edytowac uzytkownicy z rolami TSO, MO, TA(Admin)
    //maja oni w swoich kontenerach uprawnienie: FLEX_ADMIN_FSP_MANAGE
    //pola ktore uzytkownik moze edytowac zaleza od roli uzytkownika
    //TSO jedynie moze edytwoać pole agreementWithTso, MO i TA mogą edytować wszystkie pola
    //metoda zwraca DTO edytowanego FSP pobranego z bazy danych, nadpisane o te pola z formularza edycji, ktore aktualny uzytkownik moze edytowac
    public FspDTO overwriteOnlyAllowedFspDtoFieldsForCurrentUser(FspDTO modifiedFsp) {
        UserDTO currentUser = userService.getCurrentUserDTO().get();
        if (currentUser.hasRole(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR) && !currentUser.hasRole(Role.ROLE_ADMIN)) {
            // TSO moze jedynie modyfikowac pole agreementWithTso
            FspDTO dbFsp = fspMapper.toDto(fspRepository.findById(modifiedFsp.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND)));
            dbFsp.setAgreementWithTso(modifiedFsp.isAgreementWithTso());
            return dbFsp;
        } else if (currentUser.hasRole(Role.ROLE_MARKET_OPERATOR) || currentUser.hasRole(Role.ROLE_ADMIN)) {
            //MO i ADMIN moga edytowac wszystkie pola
            return modifiedFsp;
        }
        //uzytkownicy z innymi rolami nie moga edytowac fsp
        return null;
    }
}
