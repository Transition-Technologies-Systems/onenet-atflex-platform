package pl.com.tt.flex.server.validator.constraints;

import lombok.AllArgsConstructor;
import pl.com.tt.flex.server.service.fsp.FspService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@AllArgsConstructor
public class UniqueCompanyNameValidator implements ConstraintValidator<UniqueCompanyName, String> {

    private FspService fspService;

    @Override
    public boolean isValid(String companyName, ConstraintValidatorContext context) {
        return !fspService.existsByCompanyName(companyName);
    }
}
