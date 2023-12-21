package pl.com.tt.flex.server.validator.constraints;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import pl.com.tt.flex.server.service.unit.UnitService;
import pl.com.tt.flex.server.service.unit.dto.UnitDTO;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

@AllArgsConstructor
public class UniqueUnitNameValidator implements ConstraintValidator<UniqueUnitName, UnitDTO> {

    private UnitService unitService;

    @Override
    public boolean isValid(UnitDTO unitDTO, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate("Unit name is not unique!").addPropertyNode("name").addConstraintViolation();

        String unitName = StringUtils.normalizeSpace(unitDTO.getName());
        if (Objects.isNull(unitDTO.getId())) {
            return !unitService.existsByNameLowerCase(unitName);

        }
        return !unitService.existsByNameLowerCaseAndIdNot(unitName, unitDTO.getId());

    }
}
