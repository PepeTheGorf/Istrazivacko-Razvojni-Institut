package org.example.stakeholderservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = RegistrationRoleValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRegistrationRole {

    String message() default "Role must be MANAGER or TEAM_MEMBER";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
