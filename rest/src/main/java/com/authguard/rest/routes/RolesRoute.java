package com.authguard.rest.routes;

import com.google.inject.Inject;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.Context;
import com.authguard.rest.access.ActorRole;
import com.authguard.rest.dto.PermissionsRequestDTO;
import com.authguard.rest.dto.RoleDTO;
import com.authguard.service.RolesService;
import com.authguard.service.model.PermissionBO;
import com.authguard.service.model.RoleBO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.javalin.apibuilder.ApiBuilder.post;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.core.security.SecurityUtil.roles;

public class RolesRoute implements EndpointGroup {
    private final RolesService rolesService;
    private final RestMapper restMapper;

    @Inject
    public RolesRoute(final RolesService rolesService, final RestMapper restMapper) {
        this.rolesService = rolesService;
        this.restMapper = restMapper;
    }

    @Override
    public void addEndpoints() {
        post("/", this::create, roles(ActorRole.of("admin")));
        get("/:name", this::getByName, roles(ActorRole.of("admin")));
        post("/:name/permissions/grant", this::grantPermissions, roles(ActorRole.of("admin")));
        post("/:name/permissions/revoke", this::revokePermissions, roles(ActorRole.of("admin")));
    }

    private void create(final Context context) {
        final RoleDTO role = context.bodyAsClass(RoleDTO.class);

        if (role.getPermissions() != null) {
            context.status(400).result("Permissions cannot be added when a role is created");
            return;
        }

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

    private void grantPermissions(final Context context) {
        final String roleName = context.pathParam("name");
        final PermissionsRequestDTO permissionsRequest = context.bodyAsClass(PermissionsRequestDTO.class);
        final List<PermissionBO> permissions = permissionsRequest.getPermissions().stream()
                .map(restMapper::toBO)
                .collect(Collectors.toList());

        final Optional<RoleBO> updated = rolesService.grantPermissions(roleName, permissions);

        if (updated.isPresent()) {
            context.status(200).json(updated.get());
        } else {
            context.status(400);
        }
    }

    private void revokePermissions(final Context context) {
        final String roleName = context.pathParam("name");
        final PermissionsRequestDTO permissionsRequest = context.bodyAsClass(PermissionsRequestDTO.class);
        final List<PermissionBO> permissions = permissionsRequest.getPermissions().stream()
                .map(restMapper::toBO)
                .collect(Collectors.toList());

        final Optional<RoleBO> updated = rolesService.revokePermissions(roleName, permissions);

        if (updated.isPresent()) {
            context.status(200).json(updated.get());
        } else {
            context.status(400);
        }
    }

}