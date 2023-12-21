package pl.com.tt.flex.server.validator.constraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = UniqueUserLoginValidator.class)
public @interface UniqueUserLogin {
    String message() default "{UniqueUserLogin}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
