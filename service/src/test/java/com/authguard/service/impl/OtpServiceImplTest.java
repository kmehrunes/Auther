package com.authguard.service.impl;

import com.authguard.config.ConfigContext;
import com.authguard.service.ExchangeService;
import com.authguard.service.config.OtpConfig;
import com.authguard.dal.model.OneTimePasswordDO;
import com.authguard.service.model.TokensBO;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

class OtpServiceImplTest {
    private final EasyRandom random = new EasyRandom(new EasyRandomParameters().collectionSizeRange(1, 4));

    private ExchangeService mockExchangeService;

    private OtpServiceImpl otpService;

    void setup(final OtpConfig otpConfig) {
        mockExchangeService = Mockito.mock(ExchangeService.class);

        final ConfigContext configContext = Mockito.mock(ConfigContext.class);

        Mockito.when(configContext.asConfigBean(OtpConfig.class)).thenReturn(otpConfig);

        otpService = new OtpServiceImpl(mockExchangeService, configContext);
    }

    @Test
    void authenticate() {
        final OtpConfig otpConfig = OtpConfig.builder()
                .generateToken("accessToken")
                .build();

        setup(otpConfig);

        final OneTimePasswordDO otp = random.nextObject(OneTimePasswordDO.class);
        final TokensBO tokens = random.nextObject(TokensBO.class);

        final String otpToken = otp.getId() + ":" + otp.getPassword();

        Mockito.when(mockExchangeService.exchange(otpToken, "otp", otpConfig.getGenerateToken()))
                .thenReturn(tokens);

        final TokensBO generated = otpService.authenticate(otp.getId(), otp.getPassword());

        assertThat(generated).isEqualTo(tokens);
    }
}