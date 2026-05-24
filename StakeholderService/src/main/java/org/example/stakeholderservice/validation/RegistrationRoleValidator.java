package org.example.stakeholderservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.stakeholderservice.model.Role;

public class RegistrationRoleValidator implements ConstraintValidator<ValidRegistrationRole, Role> {

    @Override
    public boolean isValid(Role role, ConstraintValidatorContext context) {
        if (role == null) {
            return true;
        }
        return role == Role.MANAGER || role == Role.TEAM_MEMBER;
    }
}
