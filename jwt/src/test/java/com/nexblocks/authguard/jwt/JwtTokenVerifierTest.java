package com.nexblocks.authguard.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nexblocks.authguard.service.config.JwtConfig;
import com.nexblocks.authguard.service.config.StrategyConfig;
import com.nexblocks.authguard.service.exceptions.ServiceAuthorizationException;
import com.nexblocks.authguard.service.model.AccountBO;
import com.nexblocks.authguard.service.model.AuthResponseBO;
import com.nexblocks.authguard.service.model.PermissionBO;
import io.vavr.control.Either;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenVerifierTest {
    private static final String ALGORITHM = "HMAC256";
    private static final String KEY = "src/test/resources/hmac256.pem";
    private static final String ISSUER = "test";

    private JtiProvider jtiProvider;

    private final static EasyRandom RANDOM = new EasyRandom(new EasyRandomParameters().collectionSizeRange(1, 4));

    private JwtConfig jwtConfig() {
        return JwtConfig.builder()
                .algorithm(ALGORITHM)
                .privateKey(KEY)
                .issuer(ISSUER)
                .build();
    }

    private StrategyConfig strategyConfig(final boolean useJti) {
        return StrategyConfig.builder()
                .tokenLife("5m")
                .useJti(useJti)
                .includePermissions(true)
                .build();
    }

    private JwtTokenVerifier newVerifierInstance(final StrategyConfig strategyConfig) {
        jtiProvider = Mockito.mock(JtiProvider.class);

        final JwtConfig jwtConfig = jwtConfig();
        final Algorithm algorithm = JwtConfigParser.parseAlgorithm(jwtConfig.getAlgorithm(), jwtConfig.getPublicKey(),
                jwtConfig.getPrivateKey());

        return new JwtTokenVerifier(strategyConfig, jtiProvider, algorithm);
    }

    private AuthResponseBO generateToken(final JwtConfig jwtConfig, final AccountBO account, final String jti) {
        final Algorithm algorithm = JwtConfigParser.parseAlgorithm(jwtConfig.getAlgorithm(), jwtConfig.getPublicKey(),
                jwtConfig.getPrivateKey());
        final JwtGenerator jwtGenerator = new JwtGenerator(jwtConfig);

        final JWTCreator.Builder tokenBuilder = jwtGenerator.generateUnsignedToken(account, Duration.ofMinutes(5));

        if (jti != null) {
            tokenBuilder.withJWTId(jti);
        }

        final String token = tokenBuilder.sign(algorithm);
        final String refreshToken = jwtGenerator.generateRandomRefreshToken();

        return AuthResponseBO.builder()
                .token(token)
                .refreshToken(refreshToken)
                .build();
    }
    
    @Test
    void validate() {
        final StrategyConfig strategyConfig = strategyConfig(false);
        final JwtConfig jwtConfig = jwtConfig();

        final JwtTokenVerifier jwtTokenVerifier = newVerifierInstance(strategyConfig);

        final AccountBO account = RANDOM.nextObject(AccountBO.class);
        final AuthResponseBO tokens = generateToken(jwtConfig, account, null);
        final Either<Exception, DecodedJWT> validatedToken = jwtTokenVerifier.verify(tokens.getToken().toString());

        assertThat(validatedToken.isRight()).isTrue();
        verifyToken(validatedToken.get(), account.getId(), null, null, null);
    }

    @Test
    void validateExpired() {
        final StrategyConfig strategyConfig = strategyConfig(false);
        final JwtConfig jwtConfig = jwtConfig();

        final AccountBO account = RANDOM.nextObject(AccountBO.class);

        final Algorithm algorithm = JwtConfigParser.parseAlgorithm(jwtConfig.getAlgorithm(), jwtConfig.getPublicKey(),
                jwtConfig.getPrivateKey());
        final JwtGenerator jwtGenerator = new JwtGenerator(jwtConfig);

        final String token = jwtGenerator.generateUnsignedToken(account, Duration.ofMinutes(5))
                .withExpiresAt(Date.from(Instant.now().minusSeconds(60)))
                .sign(algorithm);

        final JwtTokenVerifier jwtTokenVerifier = newVerifierInstance(strategyConfig);

        final Either<Exception, DecodedJWT> validatedToken = jwtTokenVerifier.verify(token);

        assertThat(validatedToken.isLeft()).isTrue();
        assertThat(validatedToken.getLeft()).isInstanceOf(ServiceAuthorizationException.class);
    }

    @Test
    void validateWithJti() {
        final StrategyConfig strategyConfig = strategyConfig(true);
        final JwtConfig jwtConfig = jwtConfig();

        final JwtTokenVerifier jwtTokenVerifier = newVerifierInstance(strategyConfig);

        final String jti = UUID.randomUUID().toString();

        Mockito.when(jtiProvider.next()).thenReturn(jti);
        Mockito.when(jtiProvider.validate(jti)).thenReturn(true);

        final AccountBO account = RANDOM.nextObject(AccountBO.class);
        final AuthResponseBO tokens = generateToken(jwtConfig, account, jti);
        final Either<Exception, DecodedJWT> validatedToken = jwtTokenVerifier.verify(tokens.getToken().toString());

        assertThat(validatedToken.isRight()).isTrue();
        verifyToken(validatedToken.get(), account.getId(), jti, null, null);
    }

    @Test
    void validateWithJtiBlacklisted() {
        final StrategyConfig strategyConfig = strategyConfig(true);
        final JwtConfig jwtConfig = jwtConfig();

        final JwtTokenVerifier jwtTokenVerifier = newVerifierInstance(strategyConfig);

        final String jti = UUID.randomUUID().toString();

        Mockito.when(jtiProvider.next()).thenReturn(jti);
        Mockito.when(jtiProvider.validate(jti)).thenReturn(false);

        final AccountBO account = RANDOM.nextObject(AccountBO.class);
        final AuthResponseBO tokens = generateToken(jwtConfig, account, jti);
        final Either<Exception, DecodedJWT> validatedToken = jwtTokenVerifier.verify(tokens.getToken().toString());

        assertThat(validatedToken.isLeft());
    }

    @Test
    void validateWithAlgNone() {
        final StrategyConfig strategyConfig = strategyConfig(false);
        final JwtConfig jwtConfig = jwtConfig();

        final JwtTokenVerifier jwtTokenVerifier = newVerifierInstance(strategyConfig);

        final AccountBO account = RANDOM.nextObject(AccountBO.class);
        final AuthResponseBO tokens = generateToken(jwtConfig, account, null);
        final String payload = tokens.getToken().toString().split("\\.")[1];
        final String maliciousToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + payload + ".signature";

        assertThat(jwtTokenVerifier.verify(maliciousToken)).isEmpty();
    }

    private void verifyToken(final DecodedJWT decodedJWT, final String subject, final String jti, final List<PermissionBO> permissions,
                             final List<String> scopes) {
        final JWTVerifier verifier = JWT.require(JwtConfigParser.parseAlgorithm(ALGORITHM, null, KEY))
                .build();

        verifier.verify(decodedJWT);

        assertThat(decodedJWT.getIssuer()).isEqualTo(ISSUER);
        assertThat(decodedJWT.getSubject()).isEqualTo(subject);

        if (jti != null) {
            assertThat(decodedJWT.getId()).isEqualTo(jti);
        }

        if (permissions != null) {
            assertThat(decodedJWT.getClaim("permissions").asArray(String.class)).hasSameSizeAs(permissions);
        }

        if (scopes != null) {
            assertThat(decodedJWT.getClaim("scopes").asArray(String.class)).containsExactlyInAnyOrder(scopes.toArray(new String[0]));
        }
    }
}