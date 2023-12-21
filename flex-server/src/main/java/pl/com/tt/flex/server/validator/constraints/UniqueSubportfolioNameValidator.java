package pl.com.tt.flex.server.validator.constraints;

import lombok.AllArgsConstructor;
import pl.com.tt.flex.server.repository.subportfolio.SubportfolioRepository;
import pl.com.tt.flex.server.service.subportfolio.dto.SubportfolioDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@AllArgsConstructor
public class UniqueSubportfolioNameValidator implements ConstraintValidator<UniqueSubportfolioName, SubportfolioDTO> {

    private SubportfolioRepository subportfolioRepository;

    @Override
    public boolean isValid(SubportfolioDTO subportfolioDTO, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Subportfolio name is not unique!").addPropertyNode("name").addConstraintViolation();

        return !schedulingUnitWithThisNameExists(subportfolioDTO.getName(), Optional.ofNullable(subportfolioDTO.getId()));
    }

    private boolean schedulingUnitWithThisNameExists(String name, Optional<Long> optId) {
        return optId.map(id -> subportfolioRepository.existsByNameIgnoreCaseAndIdNot(name, id))
            .orElseGet(() -> subportfolioRepository.existsByNameIgnoreCase(name));
    }
}
