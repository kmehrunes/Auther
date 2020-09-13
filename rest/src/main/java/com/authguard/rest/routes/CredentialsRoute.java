package com.authguard.rest.routes;

import com.authguard.api.dto.entities.CredentialsDTO;
import com.authguard.api.dto.entities.UserIdentifierDTO;
import com.authguard.api.dto.requests.CreateCredentialsRequestDTO;
import com.authguard.api.dto.requests.UserIdentifiersRequestDTO;
import com.authguard.api.routes.CredentialsApi;
import com.authguard.rest.mappers.RestJsonMapper;
import com.authguard.rest.mappers.RestMapper;
import com.authguard.rest.util.BodyHandler;
import com.authguard.service.CredentialsService;
import com.authguard.service.model.UserIdentifierBO;
import com.google.inject.Inject;
import io.javalin.http.Context;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CredentialsRoute extends CredentialsApi {
    private final RestMapper restMapper;
    private final CredentialsService credentialsService;

    private final BodyHandler<CreateCredentialsRequestDTO> credentialsRequestBodyHandler;
    private final BodyHandler<UserIdentifiersRequestDTO> userIdentifiersRequestBodyHandler;

    @Inject
    public CredentialsRoute(final RestMapper restMapper, final CredentialsService credentialsService) {
        this.restMapper = restMapper;
        this.credentialsService = credentialsService;

        this.credentialsRequestBodyHandler = new BodyHandler.Builder<>(CreateCredentialsRequestDTO.class)
                .build();
        this.userIdentifiersRequestBodyHandler = new BodyHandler.Builder<>(UserIdentifiersRequestDTO.class)
                .build();
    }

    public void create(final Context context) {
        final CreateCredentialsRequestDTO request = credentialsRequestBodyHandler.getValidated(context);

        final Optional<CredentialsDTO> created = Optional.of(restMapper.toBO(request))
                .map(credentialsService::create)
                .map(restMapper::toDTO);

        if (created.isPresent()) {
            context.status(201).json(created.get());
        } else {
            context.status(400).result("Failed to create account");
        }
    }

    public void update(final Context context) {
        final CredentialsDTO credentials = RestJsonMapper.asClass(context.body(), CredentialsDTO.class);

        if (credentials.getPlainPassword() != null) {
            context.status(400).result("Password cannot be updated using regular update");
            return;
        }

        final String credentialsId = context.pathParam("id");

        final Optional<CredentialsDTO> updated = Optional.of(credentials.withId(credentialsId))
                .map(restMapper::toBO)
                .flatMap(credentialsService::update)
                .map(restMapper::toDTO);

        if (updated.isPresent()) {
            context.status(400).json(updated.get());
        } else {
            context.status(404);
        }
    }

    public void updatePassword(final Context context) {
        final CredentialsDTO credentials = RestJsonMapper.asClass(context.body(), CredentialsDTO.class);
        final String credentialsId = context.pathParam("id");

        final Optional<CredentialsDTO> updated = Optional.of(credentials.withId(credentialsId))
                .map(restMapper::toBO)
                .flatMap(credentialsService::updatePassword)
                .map(restMapper::toDTO);

        if (updated.isPresent()) {
            context.status(400).json(updated.get());
        } else {
            context.status(404);
        }
    }

    public void addIdentifiers(final Context context) {
        final String credentialsId = context.pathParam("id");
        final UserIdentifiersRequestDTO request = userIdentifiersRequestBodyHandler.getValidated(context);
        final List<UserIdentifierBO> identifiers = request.getIdentifiers().stream()
                .map(restMapper::toBO)
                .collect(Collectors.toList());

        credentialsService.addIdentifiers(credentialsId, identifiers)
                .map(restMapper::toDTO)
                .ifPresentOrElse(context::json, () -> context.status(404));
    }

    public void removeIdentifiers(final Context context) {
        final String credentialsId = context.pathParam("id");
        final UserIdentifiersRequestDTO request = userIdentifiersRequestBodyHandler.getValidated(context);
        final List<String> identifiers = request.getIdentifiers().stream()
                .map(UserIdentifierDTO::getIdentifier)
                .collect(Collectors.toList());

        credentialsService.removeIdentifiers(credentialsId, identifiers)
                .map(restMapper::toDTO)
                .ifPresentOrElse(context::json, () -> context.status(404));
    }

    public void getById(final Context context) {
        final Optional<CredentialsDTO> credentials = credentialsService.getById(context.pathParam("id"))
                .map(restMapper::toDTO);

        if (credentials.isPresent()) {
            context.json(credentials.get());
        } else {
            context.status(404);
        }
    }

    public void removeById(final Context context) {
        final Optional<CredentialsDTO> credentials = credentialsService.delete(context.pathParam("id"))
                .map(restMapper::toDTO);

        if (credentials.isPresent()) {
            context.json(credentials.get());
        } else {
            context.status(404);
        }
    }
}