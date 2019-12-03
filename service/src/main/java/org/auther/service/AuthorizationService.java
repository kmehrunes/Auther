package org.auther.service;

import org.auther.service.model.TokensBO;

import java.util.Optional;

public interface AuthorizationService {
    /**
     * Authorize a user using an ID token.
     * @param header Authorization header (must be Bearer)
     * @return
     */
    Optional<TokensBO> authorize(String header);

    /**
     * Refresh an access token using the refresh token
     * @param refreshToken
     * @return
     */
    Optional<TokensBO> refresh(String refreshToken);
}
