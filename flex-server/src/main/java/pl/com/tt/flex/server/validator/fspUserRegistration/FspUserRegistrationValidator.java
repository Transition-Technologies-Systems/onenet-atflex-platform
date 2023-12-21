package pl.com.tt.flex.server.validator.fspUserRegistration;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import pl.com.tt.flex.server.common.errors.ObjectValidationException;
import pl.com.tt.flex.server.domain.common.enumeration.FileExtension;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus;
import pl.com.tt.flex.model.security.permission.Role;
import pl.com.tt.flex.server.service.user.registration.dto.FspUserRegistrationDTO;
import pl.com.tt.flex.server.validator.AbstractFileValidator;
import pl.com.tt.flex.server.web.rest.errors.common.WrongActualStatusException;
import pl.com.tt.flex.server.web.rest.user.registration.FspUserRegistrationResource;

import java.util.Set;

import static pl.com.tt.flex.server.domain.common.enumeration.FileExtension.*;

@Slf4j
@Component
public class FspUserRegistrationValidator extends AbstractFileValidator {

    // constant for validation errors not handled in frontend layer
    public static final String ERR_VALIDATION = "error.validation.fspUserRegistration";

    private static final Set<FileExtension> SUPPORTED_FILE_EXTENSIONS = Sets.newHashSet(DOC, DOCX, PDF, TXT, XLS, XLSX);

    public void validActualStatusOfRegistration(FspUserRegistrationStatus actualStatus, Set<FspUserRegistrationStatus> expectedStatuses) {
        if (!expectedStatuses.contains(actualStatus)) {
            log.error("Wrong actual status of fspUserRegistration");
            throw new WrongActualStatusException(StringUtils.join(expectedStatuses, "/"), actualStatus.name());
        }
    }

    public void checkRole(FspUserRegistrationDTO fspUserRegistrationDTO) throws ObjectValidationException {
        if (!Role.FSP_ORGANISATIONS_ROLES.contains(fspUserRegistrationDTO.getUserTargetRole())) {
            throw new ObjectValidationException("Selected role is not intended for organisations of FSP platform", ERR_VALIDATION, FspUserRegistrationResource.ENTITY_NAME);
        }
    }

    public void checkRulesField(FspUserRegistrationDTO fspUserRegistrationDTO) throws ObjectValidationException {
        if (!fspUserRegistrationDTO.isRulesConfirmation() && !fspUserRegistrationDTO.isRodoConfirmation()) {
            throw new ObjectValidationException("User not confirmation the platform rules", ERR_VALIDATION, FspUserRegistrationResource.ENTITY_NAME);
        }
    }

    @Override
    protected Set<FileExtension> getSupportedFileExtensions() {
        return SUPPORTED_FILE_EXTENSIONS;
    }

    @Override
    protected String getEntityName() {
        return FspUserRegistrationResource.ENTITY_NAME;
    }

}
