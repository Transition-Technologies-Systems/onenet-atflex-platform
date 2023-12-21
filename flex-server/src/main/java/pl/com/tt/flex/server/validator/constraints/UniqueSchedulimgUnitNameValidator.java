package pl.com.tt.flex.server.validator.constraints;

import lombok.AllArgsConstructor;
import pl.com.tt.flex.server.repository.schedulingUnit.SchedulingUnitRepository;
import pl.com.tt.flex.server.service.schedulingUnit.dto.SchedulingUnitDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

@AllArgsConstructor
public class UniqueSchedulimgUnitNameValidator implements ConstraintValidator<UniqueScheduligUnitName, SchedulingUnitDTO> {

    private SchedulingUnitRepository schedulingUnitRepository;

    @Override
    public boolean isValid(SchedulingUnitDTO schedulingUnitDTO, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Scheduling unit name is not unique!").addPropertyNode("name").addConstraintViolation();

        return !schedulingUnitWithThisNameExists(schedulingUnitDTO.getName(), Optional.ofNullable(schedulingUnitDTO.getId()));
    }

    private boolean schedulingUnitWithThisNameExists(String name, Optional<Long> optId) {
        return optId.map(id -> schedulingUnitRepository.existsByNameIgnoreCaseAndIdNot(name, id))
            .orElseGet(() -> schedulingUnitRepository.existsByNameIgnoreCase(name));
    }
}
