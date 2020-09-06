package com.authguard.api.dto.validation.validators;

import com.authguard.api.dto.entities.PermissionDTO;
import com.authguard.api.dto.validation.Validator;
import com.authguard.api.dto.validation.fluent.FluentValidator;
import com.authguard.api.dto.validation.violations.Violation;

import java.util.List;

public class PermissionValidator implements Validator<PermissionDTO> {
    @Override
    public List<Violation> validate(final PermissionDTO obj) {
        return FluentValidator.begin()
                .validate("group", obj.getGroup(), Constraints.required, Constraints.reasonableLength)
                .validate("name", obj.getName(), Constraints.required, Constraints.reasonableLength)
                .getViolations();
    }
}
