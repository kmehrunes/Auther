package com.authguard.service.otp;

import com.authguard.config.ConfigContext;
import com.authguard.dal.OtpRepository;
import com.authguard.service.AuthProvider;
import com.authguard.service.config.ConfigParser;
import com.authguard.service.config.OtpConfig;
import com.authguard.service.mappers.ServiceMapper;
import com.authguard.service.model.AccountBO;
import com.authguard.service.model.AppBO;
import com.authguard.service.model.OneTimePasswordBO;
import com.authguard.service.model.TokensBO;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.ZonedDateTime;
import java.util.UUID;

public class OtpProvider implements AuthProvider {
    private final OtpRepository otpRepository;
    private final ServiceMapper serviceMapper;
    private final OtpConfig otpConfig;

    @Inject
    public OtpProvider(final OtpRepository otpRepository, final ServiceMapper serviceMapper,
                       final @Named("otp") ConfigContext otpConfig) {
        this.otpRepository = otpRepository;
        this.serviceMapper = serviceMapper;
        this.otpConfig = otpConfig.asConfigBean(OtpConfig.class);
    }

    @Override
    public TokensBO generateToken(final AccountBO account) {
        final String passwordId = UUID.randomUUID().toString();
        final String password = generatePassword();

        final TokensBO token = createToken(passwordId);

        final OneTimePasswordBO oneTimePassword = OneTimePasswordBO.builder()
                .id(passwordId)
                .accountId(account.getId())
                .expiresAt(ZonedDateTime.now().plus(ConfigParser.parseDuration(otpConfig.getLifeTime())))
                .password(password)
                .build();

        otpRepository.save(serviceMapper.toDO(oneTimePassword));

        return token;
    }

    @Override
    public TokensBO generateToken(final AppBO app) {
        throw new UnsupportedOperationException("OTPs cannot be generated for applications");
    }

    private TokensBO createToken(final String passwordId) {
        return TokensBO.builder()
                .type("OTP")
                .token(passwordId)
                .build();
    }

    private String generatePassword() {
        switch (otpConfig.getMode()) {
            case ALPHANUMERIC: return RandomStringUtils.randomAlphanumeric(otpConfig.getLength());
            case ALPHABETIC: return RandomStringUtils.randomAlphabetic(otpConfig.getLength());
            case NUMERIC: return RandomStringUtils.randomNumeric(otpConfig.getLength());
            default: throw new IllegalStateException("Unrecognized OTP mode " + otpConfig.getMode());
        }
    }
}