package com.nexblocks.authguard.rest.access;

import com.nexblocks.authguard.api.dto.entities.Error;
import com.nexblocks.authguard.basic.BasicAuthProvider;
import com.nexblocks.authguard.service.ApiKeysService;
import com.nexblocks.authguard.service.exceptions.ServiceException;
import com.nexblocks.authguard.service.exceptions.codes.ErrorCode;
import com.nexblocks.authguard.service.model.AccountBO;
import com.nexblocks.authguard.service.model.AppBO;
import com.google.inject.Inject;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.vavr.control.Either;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class AuthorizationHandler implements Handler {
    private static final Logger LOG = LoggerFactory.getLogger(AuthorizationHandler.class);

    private final BasicAuthProvider basicAuth;
    private final ApiKeysService apiKeysService;

    @Inject
    public AuthorizationHandler(final BasicAuthProvider basicAuth, final ApiKeysService apiKeysService) {
        this.basicAuth = basicAuth;
        this.apiKeysService = apiKeysService;
    }

    @Override
    public void handle(@NotNull final Context context) {
        Optional.ofNullable(context.header("Authorization"))
                .map(this::splitAuthorization)
                .ifPresent(parts -> {
                    if (parts.length == 2) {
                        populateActor(context, parts);
                    } else {
                        context.status(401).json(new Error("401", "Invalid authorization header"));
                    }
                });
    }

    private String[] splitAuthorization(final String authorization) {
        return authorization.split("\\s");
    }

    private void populateActor(final Context context, final String[] authorization) {
        switch (authorization[0]) {
            case "Basic":
                populateBasicActor(context, authorization[1]);
                break;

            case "Bearer":
                populateBearerActor(context, authorization[1]);
                return;

            default:
                context.status(401).json(new Error("401", "Unrecognized authorization scheme"));
        }
    }

    private void populateBasicActor(final Context context, final String base64Credentials) {
        final Either<Exception, AccountBO> actorAccount = basicAuth.authenticateAndGetAccount(base64Credentials);

        if (actorAccount.isRight()) {
            LOG.info("Authenticated actor {} with basic credentials", actorAccount.get().getId());
            context.attribute("actor", actorAccount.get());
        } else {
            LOG.info("Failed to authenticate actor with basic credentials");
            context.status(401).json(new Error("401", "Failed to authenticate with basic scheme"));
        }
    }

    private void populateBearerActor(final Context context, final String apiKey) {
        final Optional<AppBO> actorApp = apiKeysService.validateApiKey(apiKey);

        if (actorApp.isPresent()) {
            LOG.info("Authenticated actor {} with bearer token", actorApp.get().getId());
            context.attribute("actor", actorApp.get());
        } else {
            LOG.info("Failed to authenticate actor with bearer token");
            context.status(401).json(new Error("401", "Failed to authenticate with bearer scheme"));
        }
    }
}
