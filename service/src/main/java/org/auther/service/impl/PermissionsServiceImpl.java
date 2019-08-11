package org.auther.service.impl;

import org.auther.dal.PermissionsRepository;
import org.auther.service.PermissionGroupBO;
import org.auther.service.PermissionsServices;
import org.auther.service.exceptions.ServiceException;
import org.auther.service.model.PermissionBO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PermissionsServiceImpl implements PermissionsServices {
    private final PermissionsRepository permissionsRepository;
    private final ServiceMapper serviceMapper;

    public PermissionsServiceImpl(final PermissionsRepository permissionsRepository, final ServiceMapper serviceMapper) {
        this.permissionsRepository = permissionsRepository;
        this.serviceMapper = serviceMapper;
    }

    @Override
    public PermissionGroupBO createPermissionGroup(final PermissionGroupBO permissionGroup) {
        return Optional.of(permissionGroup)
                .map(serviceMapper::toDO)
                .map(permissionsRepository::createPermissionGroup)
                .map(serviceMapper::toBO)
                .orElseThrow(IllegalStateException::new);
    }

    @Override
    public PermissionBO createPermission(final PermissionBO permission) {
        permissionsRepository.getPermissionGroupByName(permission.getGroup())
                .orElseThrow(() -> new ServiceException("No permission group " + permission.getGroup() + " found"));

        return Optional.of(permission)
                .map(serviceMapper::toDO)
                .map(permissionsRepository::createPermission)
                .map(serviceMapper::toBO)
                .orElseThrow(IllegalStateException::new);
    }

    @Override
    public List<PermissionBO> getPermissions() {
        return permissionsRepository.getAllPermissions().stream()
                .map(serviceMapper::toBO)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<List<PermissionBO>> getPermissionsByGroup(final String group) {
        return permissionsRepository.getPermissions(group)
                .map(permissions -> permissions.stream()
                        .map(serviceMapper::toBO)
                        .collect(Collectors.toList())
                );
    }

    @Override
    public Optional<PermissionBO> deletePermission(final PermissionBO permission) {
        return Optional.of(permission)
                .map(serviceMapper::toDO)
                .flatMap(permissionsRepository::deletePermission)
                .map(serviceMapper::toBO);
    }
}
