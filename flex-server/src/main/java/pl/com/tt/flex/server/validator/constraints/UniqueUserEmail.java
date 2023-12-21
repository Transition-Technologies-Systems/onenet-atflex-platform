package pl.com.tt.flex.server.validator.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUserEmailValidator.class)
public @interface UniqueUserEmail {
    String message() default "{UniqueUserEmail}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
