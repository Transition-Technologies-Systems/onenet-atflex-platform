package pl.com.tt.flex.server.validator.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueCompanyNameValidator.class)
public @interface UniqueCompanyName {
    String message() default "{UniqueCompanyName}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
