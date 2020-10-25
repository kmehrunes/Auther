package com.authguard.rest;

import com.authguard.bindings.ApiRoutesBinder;
import com.authguard.bindings.ConfigBinder;
import com.authguard.config.ConfigContext;
import com.authguard.rest.bindings.MappersBinder;
import com.authguard.rest.server.AuthGuardServer;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.javalin.Javalin;

import java.util.Collections;

class TestServer {
    private int port;

    private final ConfigContext configContext;
    private final Injector injector;
    private final Javalin app;

    private AuthGuardServer authGuardServer;

    TestServer() {
        this.configContext = new ConfigurationLoader().loadFromResources();

        injector = Guice.createInjector(
                new MocksBinder(),
                new MappersBinder(),
                new ConfigBinder(configContext),
                new ApiRoutesBinder(Collections.singleton("com.authguard"), configContext));
        app = Javalin.create(javalinConfig -> {
            javalinConfig.accessManager(new TestAccessManager());
        });
    }

    void start() {
        authGuardServer = new AuthGuardServer(injector);

        this.port = app.port();

        authGuardServer.start(app, app.port());
    }

    void stop() {
        app.stop();
    }

    <T> T getMock(Class<T> clazz) {
        return injector.getInstance(clazz);
    }

    int getPort() {
        return port;
    }
}
