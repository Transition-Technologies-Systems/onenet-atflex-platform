package pl.com.tt.flex.server.validator.constraints;

import lombok.AllArgsConstructor;
import pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus;
import pl.com.tt.flex.server.repository.user.registration.FspUserRegistrationRepository;
import pl.com.tt.flex.server.service.user.UserService;
import pl.com.tt.flex.server.service.user.dto.UserDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;
import java.util.Set;

import static pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus.REJECTED_BY_MO;
import static pl.com.tt.flex.server.domain.user.registration.enumeration.FspUserRegistrationStatus.WITHDRAWN_BY_FSP;

@AllArgsConstructor
public class UniqueUserEmailValidator implements ConstraintValidator<UniqueUserEmail, UserDTO> {

    private UserService userService;
    private FspUserRegistrationRepository fspUserRegistrationRepository;

    @Override
    public boolean isValid(UserDTO userDTO, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Email exists in system!").addPropertyNode("email").addConstraintViolation();

        Set<FspUserRegistrationStatus> registrationNotSuccessfulStatuses = Set.of(WITHDRAWN_BY_FSP, REJECTED_BY_MO);
        if (Objects.isNull(userDTO.getId())) {
            return !userService.existsByEmailIgnoreCase(userDTO.getEmail()) && !fspUserRegistrationRepository.existsByEmailIgnoreCaseAndStatusNotIn(userDTO.getEmail(), registrationNotSuccessfulStatuses);
        } else {
            return !userService.existsByEmailIgnoreCaseAndIdNot(userDTO.getEmail(), userDTO.getId());
        }
    }
}
