package dev.project.bedtimestory.validation;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = UniqueValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Unique {
    String message() default "Field must be unique";
    Class<?>[] groups() default {};
    Class<?>[] payload() default {};
    String columnName();
    String tableName();
}
