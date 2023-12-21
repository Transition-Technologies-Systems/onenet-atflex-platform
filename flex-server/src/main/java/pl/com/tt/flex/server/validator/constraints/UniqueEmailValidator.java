package pl.com.tt.flex.server.validator.constraints;

import lombok.AllArgsConstructor;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus;
import pl.com.tt.flex.server.repository.user.registration.FspUserRegistrationRepository;
import pl.com.tt.flex.server.service.user.UserService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Set;

import static pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus.REJECTED_BY_MO;
import static pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus.WITHDRAWN_BY_FSP;

@AllArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {

    private UserService userService;
    private FspUserRegistrationRepository fspUserRegistrationRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        Set<FspUserRegistrationStatus> registrationNotSuccessfulStatuses = Set.of(WITHDRAWN_BY_FSP, REJECTED_BY_MO);
        if (userService.existsByEmailIgnoreCase(email) || fspUserRegistrationRepository.existsByEmailIgnoreCaseAndStatusNotIn(email, registrationNotSuccessfulStatuses)) {
            return false;
        }
        return true;
    }
}
