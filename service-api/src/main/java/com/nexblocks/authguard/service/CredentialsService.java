package com.nexblocks.authguard.service;

import com.nexblocks.authguard.service.model.CredentialsBO;
import com.nexblocks.authguard.service.model.UserIdentifierBO;

import java.util.List;
import java.util.Optional;

public interface CredentialsService extends IdempotentCrudService<CredentialsBO> {
    Optional<CredentialsBO> getByUsername(String username);
    Optional<CredentialsBO> getByUsernameUnsafe(String username);

    Optional<CredentialsBO> updatePassword(String id, String plainPassword);
    Optional<CredentialsBO> addIdentifiers(String id, List<UserIdentifierBO> identifiers);
    Optional<CredentialsBO> removeIdentifiers(String id, List<String> identifiers);
}
