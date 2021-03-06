package com.nexblocks.authguard.api.dto.validation.validators;

import com.nexblocks.authguard.api.dto.requests.RolesRequestDTO;
import com.nexblocks.authguard.api.dto.validation.Validator;
import com.nexblocks.authguard.api.dto.validation.fluent.FluentValidator;
import com.nexblocks.authguard.api.dto.validation.violations.Violation;

import java.util.List;

public class RolesRequestValidator implements Validator<RolesRequestDTO> {
    @Override
    public List<Violation> validate(final RolesRequestDTO obj) {
        return FluentValidator.begin()
                .validate("roles", obj.getRoles(), Constraints.required, Constraints.hasItems)
                .validate("action", obj.getAction(), Constraints.required)
                .getViolations();
    }
}
