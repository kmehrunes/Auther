package com.authguard.service.passwords;

import com.authguard.service.exceptions.ServiceException;
import com.authguard.service.exceptions.codes.ErrorCode;
import com.authguard.service.passwords.Violation;

import java.util.List;
import java.util.stream.Collectors;

public class ServiceInvalidPasswordException extends ServiceException {
    public ServiceInvalidPasswordException(final List<Violation> violations) {
        super(ErrorCode.INVALID_PASSWORD, violations.stream()
                .map(Violation::getMessage)
                .collect(Collectors.joining(", "))
        );
    }
}