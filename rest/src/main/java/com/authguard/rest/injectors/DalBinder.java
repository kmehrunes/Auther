package com.authguard.rest.injectors;

import com.authguard.dal.*;
import com.google.inject.AbstractModule;
import java.util.Collection;
import com.authguard.injection.ClassSearch;

public class DalBinder extends AbstractModule {

    private final DynamicBinder dynamicBinder;

    public DalBinder(final Collection<String> searchPackages) {
        dynamicBinder = new DynamicBinder(new ClassSearch(searchPackages));
    }

    @Override
    public void configure() {
        bind(CredentialsRepository.class).to(dynamicBinder.findBindingsFor(CredentialsRepository.class));
        bind(CredentialsAuditRepository.class).to(dynamicBinder.findBindingsFor(CredentialsAuditRepository.class));
        bind(AccountsRepository.class).to(dynamicBinder.findBindingsFor(AccountsRepository.class));
        bind(ApplicationsRepository.class).to(dynamicBinder.findBindingsFor(ApplicationsRepository.class));
        bind(ApiKeysRepository.class).to(dynamicBinder.findBindingsFor(ApiKeysRepository.class));
        bind(PermissionsRepository.class).to(dynamicBinder.findBindingsFor(PermissionsRepository.class));
        bind(RolesRepository.class).to(dynamicBinder.findBindingsFor(RolesRepository.class));
        bind(AccountTokensRepository.class).to(dynamicBinder.findBindingsFor(AccountTokensRepository.class));
        bind(OtpRepository.class).to(dynamicBinder.findBindingsFor(OtpRepository.class));
    }
}