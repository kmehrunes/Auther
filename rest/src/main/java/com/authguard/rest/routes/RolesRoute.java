package com.authguard.rest.routes;

import com.authguard.api.dto.entities.RoleDTO;
import com.authguard.api.dto.requests.CreateRoleRequestDTO;
import com.authguard.api.dto.requests.PermissionsRequest;
import com.authguard.api.dto.requests.PermissionsRequestDTO;
import com.authguard.rest.access.ActorRoles;
import com.authguard.rest.util.BodyHandler;
import com.authguard.service.RolesService;
import com.authguard.service.model.PermissionBO;
import com.authguard.service.model.RoleBO;
import com.google.inject.Inject;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.*;

public class RolesRoute implements EndpointGroup {
    private final RolesService rolesService;
    private final RestMapper restMapper;

    private final BodyHandler<CreateRoleRequestDTO> createRoleRequestBodyHandler;
    private final BodyHandler<PermissionsRequestDTO> permissionsRequestBodyHandler;

    @Inject
    public RolesRoute(final RolesService rolesService, final RestMapper restMapper) {
        this.rolesService = rolesService;
        this.restMapper = restMapper;

        this.createRoleRequestBodyHandler = new BodyHandler.Builder<>(CreateRoleRequestDTO.class)
                .build();
        this.permissionsRequestBodyHandler = new BodyHandler.Builder<>(PermissionsRequestDTO.class)
                .build();
    }

    @Override
    public void addEndpoints() {
        post("/", this::create, ActorRoles.adminClient());
        get("/:name", this::getByName, ActorRoles.adminClient());
        patch("/:name/permissions", this::updatePermissions, ActorRoles.adminClient());
    }

    private void create(final Context context) {
        final CreateRoleRequestDTO role = createRoleRequestBodyHandler.getValidated(context);

        final RoleDTO created = Optional.of(role)
                .map(restMapper::toBO)
                .map(rolesService::create)
                .map(restMapper::toDTO)
                .orElseThrow();

        context.status(201).json(created);
    }

    private void getByName(final Context context) {
        final String name = context.pathParam("name");

        final Optional<RoleDTO> role = rolesService.getRoleByName(name)
                .map(restMapper::toDTO);

        if (role.isPresent()) {
            context.status(200).json(role.get());
        } else {
            context.status(404);
        }
    }

    private void updatePermissions(final Context context) {
        final String roleName = context.pathParam("name");
        final PermissionsRequestDTO request = permissionsRequestBodyHandler.getValidated(context);

        final List<PermissionBO> permissions = request.getPermissions().stream()
                .map(restMapper::toBO)
                .collect(Collectors.toList());

        final Optional<RoleBO> updated;

        if (request.getAction() == PermissionsRequest.Action.GRANT) {
             updated = rolesService.grantPermissions(roleName, permissions);
        } else {
            updated = rolesService.revokePermissions(roleName, permissions);
        }

        if (updated.isPresent()) {
            context.status(200).json(updated.get());
        } else {
            context.status(400);
        }
    }
}
