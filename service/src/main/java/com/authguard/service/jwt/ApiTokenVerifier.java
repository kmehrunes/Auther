package com.authguard.service.jwt;

import com.auth0.jwt.algorithms.Algorithm;
import com.authguard.config.ConfigContext;
import com.authguard.service.AuthTokenVerfier;
import com.authguard.service.config.JwtConfig;
import com.authguard.service.config.StrategyConfig;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.util.Optional;

public class ApiTokenVerifier implements AuthTokenVerfier {
    private final JwtTokenVerifier jwtVerifier;

    @Inject
    public ApiTokenVerifier(final JtiProvider jtiProvider, final @Named("jwt") ConfigContext configContext) {
        final JwtConfig jwtConfig = configContext.asConfigBean(JwtConfig.class);
        final StrategyConfig strategy = StrategyConfig.builder().useJti(true).build();
        final Algorithm algorithm = JwtConfigParser.parseAlgorithm(jwtConfig.getAlgorithm(), jwtConfig.getPublicKey(),
                jwtConfig.getPrivateKey());

        this.jwtVerifier = new JwtTokenVerifier(strategy, jtiProvider, algorithm);
    }

    @Override
    public Optional<String> verifyAccountToken(final String token) {
        return jwtVerifier.verifyAccountToken(token);
    }
}
