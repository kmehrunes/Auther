package org.auther.api.routes;

import org.auther.api.dto.AccountDTO;
import org.auther.api.dto.PermissionDTO;
import org.auther.api.dto.PermissionGroupDTO;
import org.auther.api.dto.TokensDTO;
import org.auther.service.model.AccountBO;
import org.auther.service.model.PermissionBO;
import org.auther.service.model.PermissionGroupBO;
import org.auther.service.model.TokensBO;
import org.mapstruct.Mapper;

@Mapper
public interface RestMapper {
    AccountBO toBO(AccountDTO accountDTO);
    AccountDTO toDTO(AccountBO accountBO);

    TokensBO toBO(TokensDTO tokensDTO);
    TokensDTO toDTO(TokensBO tokensBO);

    PermissionGroupBO toBO(PermissionGroupDTO permissionGroupDTO);
    PermissionGroupDTO toDTO(PermissionGroupBO permissionGroupBO);

    PermissionBO toBO(PermissionDTO permissionDTO);
    PermissionDTO toDTO(PermissionBO permissionBO);
}