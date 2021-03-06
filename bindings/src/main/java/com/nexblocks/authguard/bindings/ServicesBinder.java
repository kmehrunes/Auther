package com.nexblocks.authguard.bindings;

import com.nexblocks.authguard.config.ConfigContext;
import com.nexblocks.authguard.service.*;
import com.nexblocks.authguard.basic.passwords.SCryptPassword;
import com.nexblocks.authguard.basic.passwords.SecurePassword;
import com.google.inject.AbstractModule;
import com.nexblocks.authguard.service.*;
import com.nexblocks.authguard.service.impl.*;

public class ServicesBinder extends AbstractModule {
    private final ConfigContext configContext;

    public ServicesBinder(final ConfigContext configContext) {
        this.configContext = configContext;
    }

    @Override
    public void configure() {
        // essential bindings
        bind(CredentialsService.class).to(CredentialsServiceImpl.class);
        bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
        bind(ExchangeService.class).to(ExchangeServiceImpl.class);
        bind(AccountsService.class).to(AccountsServiceImpl.class);
        bind(ApplicationsService.class).to(ApplicationsServiceImpl.class);
        bind(ApiKeysService.class).to(ApiKeysServiceImpl.class);
        bind(PermissionsService.class).to(PermissionsServiceImpl.class);
        bind(RolesService.class).to(RolesServiceImpl.class);
        bind(SessionsService.class).to(SessionsServiceImpl.class);
        bind(IdempotencyService.class).to(IdempotencyServiceImpl.class);
        bind(ExchangeAttemptsService.class).to(ExchangeAttemptsServiceImpl.class);
        bind(AccountLocksService.class).to(AccountLocksServiceImpl.class);

        // should be conditional on property value
        bind(SecurePassword.class).to(SCryptPassword.class);

        // optional bindings
        if (configContext.get("verification") != null) {
            bind(VerificationService.class).to(VerificationServiceImpl.class);
        }

        if (configContext.get("otp") != null) {
            bind(OtpService.class).to(OtpServiceImpl.class);
        }

        if (configContext.get("passwordless") != null) {
            bind(PasswordlessService.class).to(PasswordlessServiceImpl.class);
        }
    }
}
