package com.authguard.service.passwordless;

import com.authguard.dal.AccountTokensRepository;
import com.authguard.dal.model.AccountTokenDO;
import com.authguard.service.AuthTokenVerfier;
import com.authguard.service.exceptions.ServiceAuthorizationException;
import com.google.inject.Inject;

import java.time.ZonedDateTime;
import java.util.Optional;

public class PasswordlessVerifier implements AuthTokenVerfier {
    private final AccountTokensRepository accountTokensRepository;

    @Inject
    public PasswordlessVerifier(final AccountTokensRepository accountTokensRepository) {
        this.accountTokensRepository = accountTokensRepository;
    }

    @Override
    public Optional<String> verifyAccountToken(final String passwordlessToken) {
        final AccountTokenDO storedToken = accountTokensRepository.getByToken(passwordlessToken)
                .join()
                .orElseThrow(() -> new ServiceAuthorizationException("No passwordless token found for " + passwordlessToken));

        if (storedToken.getExpiresAt().isBefore(ZonedDateTime.now())) {
            throw new ServiceAuthorizationException("Expired passwordless token");
        }

        return Optional.of(storedToken.getAssociatedAccountId());
    }
}