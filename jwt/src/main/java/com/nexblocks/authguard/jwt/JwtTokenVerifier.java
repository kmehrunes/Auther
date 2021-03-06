package com.nexblocks.authguard.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Payload;
import com.nexblocks.authguard.service.auth.AuthVerifier;
import com.nexblocks.authguard.service.config.StrategyConfig;
import com.nexblocks.authguard.service.exceptions.ServiceAuthorizationException;
import com.nexblocks.authguard.service.exceptions.codes.ErrorCode;
import com.nexblocks.authguard.service.model.EntityType;
import io.vavr.control.Either;

public class JwtTokenVerifier implements AuthVerifier {
    private final StrategyConfig strategy;
    private final JtiProvider jti;
    private final JWTVerifier verifier;

    public JwtTokenVerifier(final StrategyConfig strategy, final JtiProvider jti,
                            final Algorithm algorithm) {
        this.strategy = strategy;
        this.jti = jti;

        this.verifier = JWT.require(algorithm).build();
    }

    public JwtTokenVerifier(final StrategyConfig strategy, final Algorithm algorithm) {
        this.strategy = strategy;
        this.jti = null;

        this.verifier = JWT.require(algorithm).build();
    }

    Either<Exception, DecodedJWT> verify(final String token) {
        try {
            final DecodedJWT decoded = JWT.decode(token);
            final DecodedJWT verified = verifier.verify(decoded);

            if (this.verifyJti(verified)) {
                return Either.right(verified);
            } else {
                return Either.left(new ServiceAuthorizationException(ErrorCode.INVALID_TOKEN, "Invalid JTI", EntityType.ACCOUNT,
                        verified.getSubject()));
            }
        } catch (final JWTVerificationException e) {
            return Either.left(new ServiceAuthorizationException(ErrorCode.GENERIC_AUTH_FAILURE, "Invalid JWT"));
        }
    }

    private boolean verifyJti(final DecodedJWT decoded) {
        return !strategy.useJti() || jti.validate(decoded.getId());
    }

    @Override
    public Either<Exception, String> verifyAccountToken(String token) {
        return verify(token).map(Payload::getSubject);
    }
}
