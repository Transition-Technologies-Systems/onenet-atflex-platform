package pl.com.tt.flex.server.validator.subportfolio;

import com.google.common.collect.Sets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.activityMonitor.ActivityEvent;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.domain.subportfolio.SubportfolioEntity;
import pl.com.tt.flex.server.domain.unit.UnitEntity;
import pl.com.tt.flex.server.domain.user.UserEntity;
import pl.com.tt.flex.server.repository.subportfolio.SubportfolioRepository;
import pl.com.tt.flex.server.repository.unit.UnitRepository;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.validator.AbstractFileValidator;
import pl.com.tt.flex.server.validator.ObjectValidator;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.*;
import static pl.com.tt.flex.server.web.rest.errors.ErrorConstants.*;
import static pl.com.tt.flex.server.web.rest.subportfolio.SubportfolioResource.ENTITY_NAME;

@Component
@RequiredArgsConstructor
public class SubportfolioValidator extends AbstractFileValidator implements ObjectValidator<SubportfolioDTO, Long> {

    private static final Set<FileExtension> SUPPORTED_FILE_EXTENSIONS = Sets.newHashSet(DOC, DOCX, PDF, TXT, XLS, XLSX);

    private final SubportfolioRepository subportfolioRepository;
    private final UnitRepository unitRepository;
    private final UserService userService;

    @Override
    public void checkValid(SubportfolioDTO subportfolioDTO) throws ObjectValidationException {
        checkIfUserCanCreateSubportfolio(subportfolioDTO);
        validateByDersCertified(subportfolioDTO);
        validateDersBelongingToGivenFspa(subportfolioDTO);
        checkIfSubportfolioCanBeCertified(subportfolioDTO);
        validateDersNotInSchedulingUnit(subportfolioDTO);
    }

    @Override
    public void checkModifiable(SubportfolioDTO subportfolioDTO) throws ObjectValidationException {
        checkValid(subportfolioDTO);
        validateFspaChange(subportfolioDTO);
    }

    public void checkIfUserCanCreateSubportfolio(SubportfolioDTO subportfolioDTO) throws ObjectValidationException {
        if (!Objects.nonNull(subportfolioDTO.getId())) {
            UserEntity currentUser = userService.getCurrentUser();
            if (currentUser.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR) || currentUser.hasRole(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR)) {
                throw new ObjectValidationException("Subportfolio can't be created by TSO and DSO users", SUBPORTFOLIO_CANNOT_BE_CREATED_BY_TSO_AND_DSO, ENTITY_NAME,  ActivityEvent.SUBPORTFOLIO_CREATED_ERROR, subportfolioDTO.getId());
            }
        }
    }

    private void validateByDersCertified(SubportfolioDTO subportfolioDTO) throws ObjectValidationException {
        if(subportfolioDTO.isCertified()){
            for (Long unitId : subportfolioDTO.getUnitIds()) {
                UnitEntity unitEntity = unitRepository.findById(unitId).get();
                if (!unitEntity.isCertified()) {
                    throw new ObjectValidationException("Cannot set certified. All subportfolio's ders should be set as certified", CANNOT_CERTIFY,
                        ENTITY_NAME, ActivityEvent.SUBPORTFOLIO_CREATED_ERROR, subportfolioDTO.getId());
                }
            }
        }
    }

    /**
     * Zablokować próbę certyfikowania Subportfolio bez DERów
     */
    private void checkIfSubportfolioCanBeCertified(SubportfolioDTO subportfolioDTO) throws ObjectValidationException {
        if (subportfolioDTO.isCertified() && subportfolioDTO.getUnitIds().size() == 0) {
            throw new ObjectValidationException("Cannot certify subportfolio without DERs", CANNOT_CERTIFY_SUBPORTFOLIO_WITHOUT_DERS, ENTITY_NAME,
                ActivityEvent.SUBPORTFOLIO_UPDATED_ERROR, subportfolioDTO.getId());
        }
    }

    /**
     * DERy w subportfolio mogą być przypisane tylko do danego FSPA
     */
    private void validateDersBelongingToGivenFspa(SubportfolioDTO subportfolioDTO) throws ObjectValidationException {
        Long unitEntitiesCount = unitRepository.countFspIdAndSubportfolioDTODerIds(subportfolioDTO.getFspId(), subportfolioDTO.getUnitIds());
        // Jeżeli liczba DERów z bazy danych (ID są podane w klauzuli IN) nie zgadza się z liczbą DERów podanych w DTO, wyświetl błąd
        if (unitEntitiesCount > 0) {
            throw new ObjectValidationException("Adding DERs belonging to another user is not allowed", CANNOT_ADD_DERS_NOT_BELONGING_TO_ANOTHER_FSPA);
        }
    }

    /**
     * Blokujemy edycje fspa dla istniejącego subportfolio, ponieważ dery przypisane są do konkretnego fspa
     */
    private void validateFspaChange(SubportfolioDTO subportfolioDTO) throws ObjectValidationException {
        SubportfolioEntity subportfolioEntity = subportfolioRepository.findById(subportfolioDTO.getId()).get();
        if(!subportfolioDTO.getFspId().equals(subportfolioEntity.getFspa().getId())){
            throw new ObjectValidationException("It is not allowed to change fspa of existing subportfolio ", CANNOT_MODIFY_FSPA,
                ENTITY_NAME, ActivityEvent.SUBPORTFOLIO_UPDATED_ERROR, subportfolioDTO.getId());
        }
    }

    /**
     * Blokujemy dodanie do subportfolio DERów które są już przypisane do jednostki grafikowej
     */
    private void validateDersNotInSchedulingUnit(SubportfolioDTO subportfolioDTO) {
        List<Long> dbSubportfolioDerIds = subportfolioRepository.findAllDerIdsFromSubportfolio(subportfolioDTO.getId());
        List<Long> addedDerIds = subportfolioDTO.getUnitIds().stream()
            .filter(derId -> !dbSubportfolioDerIds.contains(derId))
            .collect(Collectors.toList());
        if(unitRepository.existsByIdInAndSchedulingUnitNotNull(addedDerIds)) {
            throw new ObjectValidationException("Cannot add der that is already assigned to a scheduling unit", CANNOT_ADD_DERS_IN_SCHEDULING_UNIT,
                ENTITY_NAME, ActivityEvent.SUBPORTFOLIO_UPDATED_ERROR, subportfolioDTO.getId());
        }
    }

    @Override
    public void checkDeletable(Long id) throws ObjectValidationException {
        UserEntity currentUser = userService.getCurrentUser();
        if (currentUser.hasRole(Role.ROLE_DISTRIBUTION_SYSTEM_OPERATOR) || currentUser.hasRole(Role.ROLE_TRANSMISSION_SYSTEM_OPERATOR)) {
            throw new ObjectValidationException("Subportfolio can't be deleted by TSO and DSO users", SUBPORTFOLIO_CANNOT_BE_DELETED_BY_TSO_AND_DSO, ENTITY_NAME, ActivityEvent.SUBPORTFOLIO_DELETED_ERROR, id);
        }

        SubportfolioEntity subportfolioEntity = subportfolioRepository.findById(id).get();
        if (subportfolioEntity.isActive()) {
            throw new ObjectValidationException("Cannot remove active Subportfolio", CANNOT_DELETE_ACTIVE_SUBPORTFOLIO,
                ENTITY_NAME, ActivityEvent.SUBPORTFOLIO_DELETED_ERROR, id);
        }
        // nie można usuwać subportfolio zawierającego DERy
        if (subportfolioEntity.getNumberOfDers() > 0) {
            throw new ObjectValidationException("Cannot remove Subportfolio containing DERs", CANNOT_DELETE_SUBPORTFOLIO_CONTAINING_DERS,
                ENTITY_NAME, ActivityEvent.SCHEDULING_UNIT_DELETED_ERROR, id);
        }
    }

    @Override
    protected Set<FileExtension> getSupportedFileExtensions() {
        return SUPPORTED_FILE_EXTENSIONS;
    }

    @Override
    protected String getEntityName() {
        return ENTITY_NAME;
    }
}
