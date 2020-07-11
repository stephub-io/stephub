package io.stephub.server.api.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {IRegexValidator.class})
public @interface ValidRegex {
    int minGroupCount() default 0;

    String message() default "Invalid RegEx";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
