package com.nexblocks.authguard.jwt;

import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.nexblocks.authguard.config.ConfigContext;
import com.nexblocks.authguard.dal.cache.AccountTokensRepository;
import com.nexblocks.authguard.dal.model.AccountTokenDO;
import com.nexblocks.authguard.jwt.crypto.TokenEncryptorAdapter;
import com.nexblocks.authguard.service.auth.AuthProvider;
import com.nexblocks.authguard.service.auth.ProvidesToken;
import com.nexblocks.authguard.service.config.ConfigParser;
import com.nexblocks.authguard.service.config.JwtConfig;
import com.nexblocks.authguard.service.config.StrategyConfig;
import com.nexblocks.authguard.service.exceptions.ServiceAuthorizationException;
import com.nexblocks.authguard.service.exceptions.codes.ErrorCode;
import com.nexblocks.authguard.service.mappers.ServiceMapper;
import com.nexblocks.authguard.service.model.*;
import com.nexblocks.authguard.service.util.ID;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@ProvidesToken("accessToken")
public class AccessTokenProvider implements AuthProvider {
    private static final String TOKEN_TYPE = "accessToken";

    private final AccountTokensRepository accountTokensRepository;
    private final JtiProvider jti;
    private final ServiceMapper serviceMapper;
    private final TokenEncryptorAdapter tokenEncryptor;

    private final Algorithm algorithm;
    private final JwtGenerator jwtGenerator;
    private final StrategyConfig strategy;
    private final Duration tokenTtl;
    private final Duration refreshTokenTtl;
    private final boolean encrypt;

    @Inject
    public AccessTokenProvider(final AccountTokensRepository accountTokensRepository,
                               final @Named("jwt") ConfigContext jwtConfigContext,
                               final @Named("accessToken") ConfigContext accessTokenConfigContext,
                               final JtiProvider jti,
                               final TokenEncryptorAdapter tokenEncryptor,
                               final ServiceMapper serviceMapper) {
        this(accountTokensRepository,
                jwtConfigContext.asConfigBean(JwtConfig.class),
                accessTokenConfigContext.asConfigBean(StrategyConfig.class),
                jti, tokenEncryptor, serviceMapper);
    }

    public AccessTokenProvider(final AccountTokensRepository accountTokensRepository,
                               final JwtConfig jwtConfig,
                               final StrategyConfig accessTokenConfig,
                               final JtiProvider jti,
                               final TokenEncryptorAdapter tokenEncryptor,
                               final ServiceMapper serviceMapper) {
        this.accountTokensRepository = accountTokensRepository;
        this.jti = jti;
        this.tokenEncryptor = tokenEncryptor;

        this.algorithm = JwtConfigParser.parseAlgorithm(jwtConfig.getAlgorithm(), jwtConfig.getPublicKey(),
                jwtConfig.getPrivateKey());
        this.jwtGenerator = new JwtGenerator(jwtConfig);

        this.strategy = accessTokenConfig;
        this.serviceMapper = serviceMapper;
        this.tokenTtl = ConfigParser.parseDuration(strategy.getTokenLife());
        this.refreshTokenTtl = ConfigParser.parseDuration(strategy.getRefreshTokenLife());
        this.encrypt = jwtConfig.getEncryption() != null;
    }

    @Override
    public AuthResponseBO generateToken(final AccountBO account) {
        return generateToken(account, null);
    }

    @Override
    public AuthResponseBO generateToken(final AccountBO account, final TokenRestrictionsBO restrictions) {
        final JwtTokenBuilder tokenBuilder = generateAccessToken(account, restrictions);

        final String signedToken = tokenBuilder.getBuilder().sign(algorithm);
        final String finalToken = encryptIfNeeded(signedToken);
        final String refreshToken = jwtGenerator.generateRandomRefreshToken();

        storeRefreshToken(account.getId(), refreshToken, restrictions);

        return AuthResponseBO.builder()
                .id(tokenBuilder.getId().orElse(null))
                .type(TOKEN_TYPE)
                .token(finalToken)
                .refreshToken(refreshToken)
                .entityType(EntityType.ACCOUNT)
                .entityId(account.getId())
                .build();
    }

    @Override
    public AuthResponseBO generateToken(final AppBO app) {
        throw new UnsupportedOperationException("Access tokens cannot be generated for an application");
    }

    @Override
    public AuthResponseBO delete(final AuthRequestBO authRequest) {
        return deleteRefreshToken(authRequest.getToken())
                .map(accountToken -> AuthResponseBO.builder()
                        .type(TOKEN_TYPE)
                        .entityId(accountToken.getAssociatedAccountId())
                        .entityType(EntityType.ACCOUNT)
                        .refreshToken(authRequest.getToken())
                        .build())
                .orElseThrow(() -> new ServiceAuthorizationException(ErrorCode.INVALID_TOKEN, "Invalid refresh token"));
    }

    private void storeRefreshToken(final String accountId, final String refreshToken, final TokenRestrictionsBO tokenRestrictions) {
        final AccountTokenDO accountToken = AccountTokenDO.builder()
                .id(ID.generate())
                .token(refreshToken)
                .associatedAccountId(accountId)
                .expiresAt(refreshTokenExpiry())
                .tokenRestrictions(serviceMapper.toDO(tokenRestrictions)) // Mapstruct already checks for null
                .build();

        accountTokensRepository.save(accountToken)
                .join();
    }

    private Optional<AccountTokenDO> deleteRefreshToken(final String refreshToken) {
        return accountTokensRepository.deleteToken(refreshToken).join();
    }

    private JwtTokenBuilder generateAccessToken(final AccountBO account, final TokenRestrictionsBO restrictions) {
        final JwtTokenBuilder.Builder tokenBuilder = JwtTokenBuilder.builder();
        final JWTCreator.Builder jwtBuilder = jwtGenerator.generateUnsignedToken(account, tokenTtl);

        if (strategy.useJti()) {
            final String id = jti.next();
            jwtBuilder.withJWTId(id);
            tokenBuilder.id(id);
        }

        if (strategy.includePermissions()) {
            jwtBuilder.withArrayClaim("permissions", JwtPermissionsMapper.map(account, restrictions));
        }

        if (strategy.includeExternalId()) {
            jwtBuilder.withClaim("eid", account.getExternalId());
        }

        if (strategy.includeRoles()) {
            jwtBuilder.withArrayClaim("roles", account.getRoles().toArray(new String[] {}));
        }

        return tokenBuilder.builder(jwtBuilder).build();
    }

    private String encryptIfNeeded(final String token) {
        return this.encrypt
                ? tokenEncryptor.encryptAndEncode(token).get()
                : token;
    }

    private OffsetDateTime refreshTokenExpiry() {
        return OffsetDateTime.now().plus(refreshTokenTtl);
    }
}
