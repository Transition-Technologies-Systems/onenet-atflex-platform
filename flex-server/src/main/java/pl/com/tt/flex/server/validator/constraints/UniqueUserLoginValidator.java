package pl.com.tt.flex.server.validator.constraints;

import lombok.AllArgsConstructor;
import pl.com.tt.flex.server.repository.user.UserRepository;
import pl.com.tt.flex.server.service.user.dto.UserDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@AllArgsConstructor
public class UniqueUserLoginValidator implements ConstraintValidator<UniqueUserLogin, UserDTO> {

    private UserRepository userRepository;

    @Override
    public boolean isValid(UserDTO userDTO, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Login is not unique!").addPropertyNode("login").addConstraintViolation();

        if (Objects.isNull(userDTO.getId())){
            return !userRepository.existsByLoginIgnoreCase(userDTO.getLogin());
        }
        return !userRepository.existsByLoginIgnoreCaseAndIdNot(userDTO.getLogin(), userDTO.getId());
    }
}
